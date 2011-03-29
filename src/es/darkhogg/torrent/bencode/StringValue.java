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

import java.util.Arrays;

/**
 * Wraps the bencoded strin value as an array of bytes. This object represents
 * bencoded string as bytes to simplify handling of binary data. The
 * <tt>toString</tt> method can retrieve a string created converting the value
 * using <tt>UTF-8</tt>.
 * 
 * @author Daniel Escoz
 * @version 1.0.0
 */
public final class StringValue extends Value<byte[]> {
	
	private byte[] value;
	private String str;
	
	/**
	 * Creates this object with the given initial value
	 * 
	 * @param value Initial value
	 */
	public StringValue ( byte[] value ) {
		super( value );
	}
	
	/**
	 * Creates this object by converting the given String to bytes using the
	 * <tt>UTF-8</tt> charset
	 * 
	 * @param value Initial String value
	 */
	public StringValue ( String value ) {
		this( value.getBytes( Bencode.UTF8 ) );
	}
	
	public byte[] getValue () {
		return Arrays.copyOf( value, value.length );
	}
	
	public void setValue ( byte[] value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		this.value = Arrays.copyOf( value, value.length );
		this.str = new String( value, Bencode.UTF8 );
	}
	
	/**
	 * Sets this object value by converting the given String to bytes using the
	 * <tt>UTF-8</tt> charset 
	 * 
	 * @param value New String value
	 */
	public void setStringValue ( String value ) {
		this.value = value.getBytes( Bencode.UTF8 );
		this.str = value;
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
	
	@Override
	public String toString () {
		return "\"" + str + "\"";
	}

	@Override
	public long getEncodedLength () {
		return value.length + 2 + (long) Math.log10( value.length );
	}
}
