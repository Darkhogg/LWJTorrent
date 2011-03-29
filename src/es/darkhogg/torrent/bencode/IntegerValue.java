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
 * Wraps the bencode integer type as long primitives
 * 
 * @author Daniel Escoz
 * @version 1.0.0
 */
public final class IntegerValue extends Value<Long> {
	
	private Long value;
	
	/**
	 * Creates this object with the given initial value
	 * 
	 * @param value Initial value
	 */
	public IntegerValue ( Long value ) {
		super( value );
	}
	
	public Long getValue () {
		return value;
	}
	
	public void setValue ( Long value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		this.value = value;
	}
	
	@Override
	public String toString () {
		return value.toString();
	}

	@Override
	public long getEncodedLength ()  {
		double val = value.doubleValue();
		return 3 + (long) Math.log10( Math.abs( val ) ) + (val<0?1:0);
	}
	
}
