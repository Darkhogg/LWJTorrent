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
 * Represents a value that can be bencoded
 * 
 * @author Daniel Escoz
 * @param <T> Basic return type for the class
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
	
}
