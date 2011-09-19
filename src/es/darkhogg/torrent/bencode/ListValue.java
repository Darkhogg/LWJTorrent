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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wraps the bencode list type as a List of Values
 * 
 * @author Daniel Escoz
 * @version 1.0.0
 */
public final class ListValue extends Value<List<Value<?>>> {
	
	private List<Value<?>> value;
	
	/**
	 * Creates this object with an empty list
	 */
	public ListValue () {
		super( new ArrayList<Value<?>>() );
	}
	
	/**
	 * Creates this object with the given initial value
	 * 
	 * @param value
	 *            Initial value
	 */
	public ListValue ( List<Value<?>> value ) {
		super( value );
	}
	
	@Override
	public List<Value<?>> getValue () {
		return Collections.unmodifiableList( value );
	}
	
	@Override
	public void setValue ( List<Value<?>> value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		this.value = new ArrayList<Value<?>>( value );
	}
	
	/**
	 * Adds the given <tt>val</tt>ue at the end of this list
	 * 
	 * @param val
	 *            Value to be added
	 * @see java.util.List#add(Object)
	 */
	public void add ( Value<?> val ) {
		value.add( val );
	}
	
	/**
	 * Removes the value at the <tt>index</tt> position
	 * 
	 * @param index
	 *            Index of the value to remove
	 * @return The removed value
	 * @see java.util.List#remove(int)
	 */
	public Value<?> remove ( int index ) {
		return value.remove( index );
	}
	
	/**
	 * Retrieves the value at the <tt>index</tt> position
	 * 
	 * @param index
	 *            Index of the value to retrieve
	 * @return The value at the specified <tt>index</tt>
	 * @see java.util.List#get(int)
	 */
	public Value<?> get ( int index ) {
		return value.get( index );
	}
	
	/**
	 * Returns the number of elements in this list
	 * 
	 * @return Number of elements in the list
	 * @see java.util.List#size()
	 */
	public int getSize () {
		return value.size();
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder( "[\n" );
		
		for ( Value<?> val : value ) {
			sb.append( "  " );
			sb.append( val.toString().replace( "\n", "\n  " ) );
			sb.append( ",\n" );
		}
		
		return sb.append( ']' ).toString();
	}
	
	@Override
	public long getEncodedLength () {
		long childLength = 0;
		
		for ( Value<?> val : value ) {
			childLength += val.getEncodedLength();
		}
		
		return childLength + 2;
	}
	
}
