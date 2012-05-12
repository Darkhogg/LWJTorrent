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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;

/**
 * Wraps the bencoded string value as an array of bytes. This object represents
 * bencoded string as bytes to simplify handling of binary data. The <tt>getStringValue</tt> method can retrieve a
 * string created converting the
 * value using <tt>UTF-8</tt>.
 * 
 * @author Daniel Escoz
 * @version 1.0.0
 */
public final class StringValue extends Value<byte[]> {
	
	private byte[] value;
	private String str;
	private boolean validUtf8;
	
	/**
	 * Creates this object with the given initial value
	 * 
	 * @param value
	 *            Initial value
	 */
	public StringValue ( final byte[] value ) {
		super( value );
	}
	
	/**
	 * Creates this object by converting the given String to bytes using the <tt>UTF-8</tt> charset
	 * 
	 * @param value
	 *            Initial String value
	 */
	public StringValue ( final String value ) {
		this( value.getBytes( Bencode.UTF8 ) );
	}
	
	@Override
	public byte[] getValue () {
		return Arrays.copyOf( value, value.length );
	}
	
	/** @return The length of the internal byte array of this object */
	public int getValueLength () {
		return value.length;
	}
	
	@Override
	public void setValue ( final byte[] value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		this.value = Arrays.copyOf( value, value.length );
		str = new String( value, Bencode.UTF8 );
		validUtf8 = checkValidUtf8( value );
	}
	
	/**
	 * Sets this object value by converting the given String to bytes using the <tt>UTF-8</tt> charset
	 * 
	 * @param value
	 *            New String value
	 */
	public void setStringValue ( final String value ) {
		this.value = value.getBytes( Bencode.UTF8 );
		str = value;
		validUtf8 = true;
	}
	
	/**
	 * Returns this objects value as a string. The bytes are interpreted as an
	 * UTF-8 encoded string.
	 * 
	 * @return The string represented in this object
	 */
	public String getStringValue () {
		return str;
	}
	
	/**
	 * Returns whether the {@link #getValue() value} of this object is a valid <i>UTF-8</i> byte sequence.
	 * 
	 * @return <tt>true</tt> if this object contains a valid UTF-8 encoded byte array, <tt>false</tt> otherwise.
	 */
	public boolean isValidUtf8 () {
		return validUtf8;
	}
	
	private static boolean checkValidUtf8 ( final byte[] bytes ) {
		final CharsetDecoder cd = Bencode.UTF8.newDecoder();
		
		try {
			cd.decode( ByteBuffer.wrap( bytes ) );
		} catch ( final CharacterCodingException e ) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString () {
		return "\"" + str + "\"";
	}
	
	@Override
	public long getEncodedLength () {
		return value.length + 2 + (long) Math.log10( value.length );
	}
}
