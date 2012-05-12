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
	
	private SortedMap<String,Value<?>> value;
	
	/**
	 * Creates this object with an empty map
	 */
	public DictionaryValue () {
		super( new TreeMap<String,Value<?>>() );
	}
	
	/**
	 * Creates this object with the given initial value
	 * 
	 * @param value
	 *            Initial value
	 */
	public DictionaryValue ( final Map<String,Value<?>> value ) {
		super( value );
	}
	
	@Override
	public Map<String,Value<?>> getValue () {
		return Collections.unmodifiableMap( value );
	}
	
	@Override
	public void setValue ( final Map<String,Value<?>> value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		this.value = new TreeMap<String,Value<?>>( value );
	}
	
	/**
	 * Adds a new key-value pair to this dictionary. If the <tt>key</tt> already
	 * exists in this dictionary, its value is replaced.
	 * 
	 * @param key
	 *            The key to add or replace
	 * @param val
	 *            The new value for the <tt>key</tt>
	 * @return The old value for the <tt>key</tt>
	 * @see java.util.Map#put(Object,Object)
	 */
	public Value<?> put ( final String key, final Value<?> val ) {
		return value.put( key, val );
	}
	
	/**
	 * Removes the given <tt>key</tt> and its associated value from this
	 * dictionary.
	 * 
	 * @param key
	 *            Key to remove
	 * @return The value of the removed <tt>key</tt>
	 * @see java.util.Map#remove(Object)
	 */
	public Value<?> remove ( final String key ) {
		return value.remove( key );
	}
	
	/**
	 * Returns the value for the given <tt>key</tt>, or <tt>nulll</tt> if the
	 * key doesn't exist.
	 * 
	 * @param key
	 *            Key which value is to be returned
	 * @return The value associated with the <tt>key</tt>, or <tt>null</tt> if
	 *         the key is not present
	 * @see java.util.Map#get(Object)
	 */
	public Value<?> get ( final String key ) {
		return value.get( key );
	}
	
	/**
	 * Returns the number of elements in this dictionary
	 * 
	 * @return Number of elements in the dictionary
	 * @see java.util.Map#size()
	 */
	public int getSize () {
		return value.size();
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder( "{\n" );
		
		for ( final Map.Entry<String,Value<?>> me : value.entrySet() ) {
			sb.append( "  \"" );
			sb.append( me.getKey() );
			sb.append( "\":" );
			sb.append( me.getValue().toString().replace( "\n", "\n  " ) );
			sb.append( ",\n" );
		}
		
		return sb.append( '}' ).toString();
	}
	
	@Override
	public long getEncodedLength () {
		long childLength = 0;
		
		for ( final Map.Entry<String,Value<?>> me : value.entrySet() ) {
			final byte[] bytes = me.getKey().getBytes( Bencode.UTF8 );
			childLength += bytes.length + 2 + (long) Math.log10( bytes.length );
			childLength += me.getValue().getEncodedLength();
		}
		
		return childLength + 2;
	}
}
