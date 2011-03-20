/**
 * This package is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this package.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.darkhogg.bencode;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An object that can pull complete values from a stream. Composite values
 * are fully read and mapped into memory.
 * 
 * @author Daniel Escoz
 * @version 1.0.0
 */
public final class BencodeInputStream implements Closeable {

	private static final int INTEGER_VALUE = (int) 'i';
	private static final int LIST_VALUE = (int) 'l';
	private static final int DICTIONARY_VALUE = (int) 'd';
	private static final int STRING_VALUE_FROM = (int) '0';
	private static final int STRING_VALUE_TO = (int) '9';
	private static final int END_VALUE = (int) 'e';
	
	private final char[] charBuffer = new char[ 32 ];
	private byte[] byteBuffer = new byte[ 32 ];
	
	private final InputStream stream;
	
	/**
	 * Constructs a BencodeInputStream that uses the given InputStream as
	 * source of bencoded values
	 * 
	 * @param in Stream to wrap in this object
	 */
	public BencodeInputStream ( InputStream in ) {
		stream = in;
	}
	
	/**
	 * Constructs a BencodeInputStream that reads bencoded values from the
	 * specified file
	 * 
	 * @param file The file to open
	 * @throws FileNotFoundException if the file does not exist, is a directory
	 *         rather than a regular file, or for some other reason cannot be
	 *         opened for reading.
	 */
	public BencodeInputStream ( File file )
	throws FileNotFoundException {
		this( new FileInputStream( file ) );
	}
	
	/**
	 * Retrieves a value from this object wrapped stream
	 * 
	 * @return The next value in the stream
	 * @throws IOException If some I/O error occurs
	 */
	public Value<?> readValue ()
	throws IOException {
		int first = stream.read();
		
		if ( first == END_VALUE ) {
			return null;
			
		} else if ( first == INTEGER_VALUE ) {
			int i = 0;
			boolean end = false;
			while ( !end ) {
				int readInt = stream.read();
				if ( readInt < 0 ) {
					throw new EOFException();
				} else if ( readInt == END_VALUE ) {
					end = true;
				} else if (
					( readInt >= '0' && readInt <= '9' )
				 || ( i == 0 && readInt == '-' )
				) {
					charBuffer[ i ] = (char) readInt;
					i++;
				} else {
					throw new IOException( "Invalid number format" );
				}
			}
			
			return new IntegerValue(
				Long.valueOf( new String( charBuffer, 0, i ) )
			);

		} else if ( first == LIST_VALUE ) {
			List<Value<?>> list = new ArrayList<Value<?>>();
			
			boolean end = false;
			while ( !end ) {
				Value<?> val = readValue();
				if ( val == null ) {
					end = true;
				} else {
					list.add( val );
				}
			}
			
			return new ListValue( list );
			
		} else if ( first == DICTIONARY_VALUE ) {
			SortedMap<String,Value<?>> map = new TreeMap<String,Value<?>>();
			
			boolean end = false;
			while ( !end ) {
				Value<?> key = readValue();
				if ( key != null && !(key instanceof StringValue) ) {
					throw new IOException( "Invalid key type" );
				}
				if ( key == null ) {
					end = true;
				} else {
					Value<?> val = readValue();
					if ( val == null ) {
						throw new IOException(
							"Found key with no associated value" );
					}
					map.put(
						new String( (byte[]) key.getValue(), Bencode.UTF8 ),
						val
					);
				}
			}
			
			return new DictionaryValue( map );
			
		} else if ( first >= STRING_VALUE_FROM && first <= STRING_VALUE_TO ) {
			
			charBuffer[ 0 ] = (char) first;
			
			int i = 1;
			boolean end = false;
			while ( !end ) {
				int readInt = stream.read();
				if ( readInt < 0 ) {
					throw new EOFException();
				} else if ( readInt == ':' ) {
					end = true;
				} else if ( readInt >= '0' && readInt <= '9' ) {
					charBuffer[ i ] = (char) readInt;
					i++;
				} else {
					throw new IOException( "Invalid number format" );
				}
			}
			
			long lsize = Long.parseLong( new String( charBuffer, 0, i ) );
			if ( lsize > Integer.MAX_VALUE ) {
				throw new IOException( "String is too long!" );
			}
			
			int size = (int) lsize;
			if ( byteBuffer.length < lsize ) {
				byteBuffer = new byte[
					Math.max( size+1, byteBuffer.length*2 + 1 ) ];
			}
			int howMany = stream.read( byteBuffer, 0, size );
			
			if ( howMany != size ) {
				throw new EOFException();
			}
			
			return new StringValue( Arrays.copyOf( byteBuffer, size ) );
			
		} else {
			if ( first == -1 ) {
				throw new EOFException();
			} else {
				throw new IOException( "Unexpected byte 0x"
					+ Integer.toHexString( first ) );
			}
		}
	}
	
	@Override 
	public void close ()
	throws IOException {
		stream.close();
	}
}
