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

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Wraps the bencode dictionary type as a SortedMap from String to Values
 * 
 * @author Daniel Escoz
 * @version 1.0.0
 */
public final class DictionaryValue extends Value<Map<String,Value<?>>> {

	private SortedMap<String, Value<?>> value;
	
	/**
	 * Creates this object with an empty map
	 */
	public DictionaryValue () {
		super( new TreeMap<String,Value<?>>() );
	}
	
	/**
	 * Creates this object with the given initial value
	 * 
	 * @param value Initial value
	 */
	public DictionaryValue ( Map<String,Value<?>> value ) {
		super( value );
	}
	
	@Override
	public Map<String, Value<?>> getValue () {
		return Collections.unmodifiableMap( value );
	}

	@Override
	public void setValue ( Map<String,Value<?>> value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		this.value = new TreeMap<String,Value<?>>( value );
	}

	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder( "{\n" );
		
		for ( Map.Entry<String,Value<?>> me : value.entrySet() ) {
			sb.append( "  \"" );
			sb.append( me.getKey() );
			sb.append( "\":" );
			sb.append( me.getValue().toString().replace( "\n", "\n  " ) );
			sb.append( ",\n" );
		}
		
		return sb.append( '}' ).toString();
	}
}
