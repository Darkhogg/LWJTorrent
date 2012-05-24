/**
 * This package is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this package. If not, see <http://www.gnu.org/licenses/>.
 */
package es.darkhogg.torrent.bencode;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * An object that pushes bencoded values into a stream.
 * 
 * @author Daniel Escoz
 * @version 1.0.0
 */
public final class BencodeOutputStream implements Closeable, Flushable {
	
	/** The actual stream used to write bencode values */
	private final PrintStream stream;
	
	/**
	 * Constructs a BencodeOutputStream that writes bencoded values to the
	 * given OutputStream
	 * 
	 * @param out
	 *            Stream to wrap in this object
	 */
	public BencodeOutputStream ( final OutputStream out ) {
		try {
			stream = new PrintStream( out, false, Bencode.UTF8.name() );
			
		} catch ( UnsupportedEncodingException exc ) {
			// If this ever happens, something seriously wrong is going on with the JVM.
			// Notice that the charset name is obtained from an actual charset which, obviously, is supported.
			throw new AssertionError( exc );
		}
	}
	
	/**
	 * Constructs a BencodeInputStream that writes bencoded values to the
	 * specified file
	 * 
	 * @param file
	 *            The file to open
	 */
	public BencodeOutputStream ( final File file ) throws FileNotFoundException {
		this( new FileOutputStream( file ) );
	}
	
	/**
	 * Writes a bencoded value into the wrapped stream
	 * 
	 * @param value
	 *            The value to write
	 * @throws IOException
	 *             If some I/O error occurs
	 */
	public void writeValue ( final Value<?> value ) throws IOException {
		if ( value instanceof StringValue ) {
			final StringValue sv = (StringValue) value;
			stream.print( sv.getValueLength() );
			stream.print( ':' );
			stream.write( sv.getValue() );
			
		} else if ( value instanceof IntegerValue ) {
			final IntegerValue iv = (IntegerValue) value;
			stream.print( 'i' );
			stream.print( iv.getValue() );
			stream.print( 'e' );
			
		} else if ( value instanceof ListValue ) {
			final ListValue lv = (ListValue) value;
			stream.print( 'l' );
			for ( final Value<?> val : lv.getValue() ) {
				writeValue( val );
			}
			stream.print( 'e' );
			
		} else if ( value instanceof DictionaryValue ) {
			final DictionaryValue dv = (DictionaryValue) value;
			stream.print( 'd' );
			for ( final Map.Entry<String,Value<?>> me : dv.getValue().entrySet() ) {
				writeValue( new StringValue( me.getKey() ) );
				writeValue( me.getValue() );
			}
			stream.print( 'e' );
			
		} else {
			throw new IllegalArgumentException( "Invalid value type" );
		}
	}
	
	@Override
	public void close () {
		stream.close();
	}

	@Override
	public void flush () {
		stream.flush();
	}
}
