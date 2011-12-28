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
package es.darkhogg.torrent.bencode;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

/**
 * An object that pushes bencoded values into a stream.
 * 
 * @author Daniel Escoz
 * @version 1.0.0
 */
public final class BencodeOutputStream implements Closeable {
	
	private final OutputStream stream;
	private final PrintWriter printer;
	
	/**
	 * Constructs a BencodeOutputStream that writes bencoded values to the
	 * given OutputStream
	 * 
	 * @param out Stream to wrap in this object
	 */
	public BencodeOutputStream ( OutputStream out ) {
		stream = out;
		printer = new PrintWriter( new OutputStreamWriter( out, Bencode.UTF8 ) );
	}
	
	/**
	 * Constructs a BencodeInputStream that writes bencoded values to the
	 * specified file
	 * 
	 * @param file The file to open
	 */
	public BencodeOutputStream ( File file )
	throws FileNotFoundException {
		this( new FileOutputStream( file ) );
	}
	
	/**
	 * Writes a bencoded value into the wrapped stream 
	 * 
	 * @param value The value to write
	 * @throws IOException If some I/O error occurs
	 */
	public void writeValue ( Value<?> value )
	throws IOException {
		if ( value instanceof StringValue ) {
			StringValue sv = (StringValue) value;
			printer.print( sv.getValue().length );
			printer.print( ':' );
			printer.flush();
			stream.write( sv.getValue() );
			
		} else if ( value instanceof IntegerValue ) {
			IntegerValue iv = (IntegerValue) value;
			printer.print( 'i' );
			printer.print( iv.getValue() );
			printer.print( 'e' );
			printer.flush();
			
		} else if ( value instanceof ListValue ) {
			ListValue lv = (ListValue) value;
			printer.print( 'l' );
			for ( Value<?> val : lv.getValue() ) {
				writeValue( val );
			}
			printer.print( 'e' );
			printer.flush();
			
		} else if ( value instanceof DictionaryValue ) {
			DictionaryValue dv = (DictionaryValue) value;
			printer.print( 'd' );
			for ( Map.Entry<String,Value<?>> me : dv.getValue().entrySet() ) {
				writeValue( new StringValue( me.getKey() ) );
				writeValue( me.getValue() );
			}
			printer.print( 'e' );
			printer.flush();
			
		} else {
			throw new IllegalArgumentException( "Invalid value type" );
		}
	}
	
	@Override 
	public void close ()
	throws IOException {
		stream.close();
		printer.close();
	}
}
