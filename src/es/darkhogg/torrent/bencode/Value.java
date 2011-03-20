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

/**
 * Represents a value that can be bencoded.
 * <p>
 * Even if this class is generic, it should never be used in a form different
 * than <tt>Value&lt;?&gt;</tt>. Generic support is given only for convenience when
 * implementing subclasses.
 * 
 * @author Daniel Escoz
 * @param T Basic return type for the class
 * @version 1.0.0
 */
public abstract class Value<T> {
	
	/**
	 * Creates this object with the given initial value
	 * 
	 * @param value Initial value
	 */
	protected Value ( T value ) {
		setValue( value );
	}
	
	/**
	 * Gets the current value for this object
	 * 
	 * @return This object value
	 */
	public abstract T getValue ();
	
	/**
	 * Sets a new value for this object
	 * 
	 * @param value New value
	 */
	public abstract void setValue ( T value );
	
	/**
	 * Returns the length, in bytes, this values will take up when encoded using
	 * UTF-8 as the encoding character set.
	 * <p>
	 * If the size cannot be specified, an upper bound must be returned.
	 * 
	 * @return The length of the encoded byte stream for this value
	 */
	public abstract long getEncodedLength ();
	
}
