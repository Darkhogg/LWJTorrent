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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Namespace for global constants and functions of this package
 * 
 * @author Daniel Escoz
 * @version 1.0.0
 */
public class Bencode {
	
	/**
	 * UTF-8 charset used for Bencode
	 */
	public static final Charset UTF8 = Charset.forName( "UTF-8" );
	
	/**
	 * Converts a bencode value to Java standard objects.
	 * <p>
	 * <i>NOTE: This method delegates its calls to its more concrete overloads.
	 * See them for details on how <tt>Value</tt>s are converted</i>
	 * 
	 * @param val The value to be converted
	 * @return An object from the standard Java API that represents the same
	 *         information
	 */
	public Object convertFromValue ( Value<?> val ) {
		if ( val instanceof IntegerValue ) {
			return convertFromValue( (IntegerValue) val );
			
		} else if ( val instanceof StringValue ) {
			return convertFromValue( (StringValue) val );
			
		} else if ( val instanceof ListValue ) {
			return convertFromValue( (ListValue) val );
			
		} else if ( val instanceof DictionaryValue ) {
			return convertFromValue( (DictionaryValue) val );
			
		} else {
			throw new IllegalArgumentException();
			
		}
	}
	
	/**
	 * Converts a bencode integer value to a <tt>Long</tt>
	 * 
	 * @param val The value to be converted
	 * @return A <tt>Long</tt> object that represents the given value
	 */
	public Long convertFromValue ( IntegerValue val ) {
		return val.getValue();
	}
	
	/**
	 * Converts a bencode string value to a <tt>String</tt>
	 * 
	 * @param val The value to be converted
	 * @return A <tt>String</tt> object that represents the given value
	 */
	public String convertFromValue ( StringValue val ) {
		return val.toString();
	}
	
	/**
	 * Converts a bencode list value to a <tt>List</tt> of converted values
	 * 
	 * @param val The value to be converted
	 * @return A <tt>List</tt> object that represents the given value
	 */
	public List<Object> convertFromValue ( ListValue val ) {
		List<Object> retList = new ArrayList<Object>();
		List<Value<?>> valList = val.getValue();
		
		for ( Value<?> v : valList ) {
			retList.add( convertFromValue( v ) );
		}
		
		return retList;
	}
	
	/**
	 * Converts a bencode list value to a <tt>SortedMap</tt> from
	 * <tt>String</tt> to converted values
	 * 
	 * @param val The value to be converted
	 * @return A <tt>SortedMap</tt> object that represents the given value
	 */
	public SortedMap<String,Object> convertFromValue ( DictionaryValue val ) {
		SortedMap<String,Object> retMap = new TreeMap<String,Object>();
		SortedMap<String,Value<?>> valMap = val.getValue();
		
		for ( Map.Entry<String,Value<?>> me : valMap.entrySet() ) {
			retMap.put( me.getKey(), convertFromValue( me.getValue() ) );
		}
		
		return retMap;
	}
}
