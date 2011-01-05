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

/**
 * Wraps the bencoded strin value as an array of bytes. This object represents
 * bencoded string as bytes to simplify handling of binary data. The toString
 * method can retrieve a string created converting the value using UTF-8.
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
	 * UTF-8 charset
	 * 
	 * @param value Initial String value
	 */
	public StringValue ( String value ) {
		this( value.getBytes( Bencode.UTF8 ) );
	}
	
	public byte[] getValue () {
		return value;
	}
	
	public void setValue ( byte[] value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		this.value = value;
		this.str = new String( value, Bencode.UTF8 );
	}
	
	/**
	 * Sets this object value by converting the given String to bytes using the
	 * UTF-8 charset 
	 * 
	 * @param value New String value
	 */
	public void setStringValue ( String value ) {
		setValue( value.getBytes( Bencode.UTF8 ) );
	}
	
	@Override
	public String toString () {
		return "\"" + str + "\"";
	}
}
