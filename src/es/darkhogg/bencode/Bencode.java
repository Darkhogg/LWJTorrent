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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public Map<String,Object> convertFromValue ( DictionaryValue val ) {
		Map<String,Object> retMap = new HashMap<String,Object>();
		Map<String,Value<?>> valMap = val.getValue();
		
		for ( Map.Entry<String,Value<?>> me : valMap.entrySet() ) {
			retMap.put( me.getKey(), convertFromValue( me.getValue() ) );
		}
		
		return retMap;
	}
	
	/**
	 * Converts a regular object into a bencode value.
	 * <p>
	 * <i>NOTE: This method delegates its calls to its more specific overloads,
	 * excepts in the case of a <tt>Value</tt> itself, which is immediately
	 * returned. If the object cannot be converted to a <tt>Value</tt>, a
	 * <tt>ClassCastException</tt> is thrown.
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 * @throws ClassCastException if the object cannot be converted to
	 *         <tt>Value</tt>
	 */
	public Value<?> convertToValue ( Object obj ) {
		if ( obj instanceof Value<?> ) {
			return (Value<?>) obj;
			
		} else if ( obj instanceof byte[] ) {
			return convertToValue( (byte[]) obj );
			
		} else if ( obj instanceof char[] ) {
			return convertToValue( (char[]) obj );
			
		} else if ( obj instanceof CharSequence ) {
			return convertToValue( (CharSequence) obj );
			
		} else if ( obj instanceof Number ) {
			return convertToValue( (Number) obj );
			
		} else if ( obj instanceof Iterable<?> ) {
			return convertToValue( (Iterable<?>) obj );
			
		} else if ( obj instanceof Map<?,?> ) {
			return convertToValue( (Map<?,?>) obj );
			
		} else {
			throw new ClassCastException();
		}
	}
	
	/**
	 * Converts a byte array to a bencode string value by creating a new 
	 * <tt>StringValue</tt> with the given byte array
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 */
	public StringValue convertToValue ( byte[] obj ) {
		return new StringValue( obj );
	}
	
	/**
	 * Converts a char array to a bencode string value by creating a new 
	 * <tt>StringValue</tt> from a new String creted from the given array
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 */
	public StringValue convertToValue ( char[] obj ) {
		return new StringValue( new String( obj ) );
	}
	
	/**
	 * Converts a <tt>CharSequence</tt> to a bencode string value by
	 * creating a new <tt>StringValue</tt> with the String that represents
	 * the given object
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 */
	public StringValue convertToValue ( CharSequence obj ) {
		return new StringValue( obj.toString() );
	}
	
	/**
	 * Converts a number to a bencode string value by creating a new 
	 * <tt>IntegerValue</tt> with the corresponding long value
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 */
	public IntegerValue convertToValue ( Number obj ) {
		return new IntegerValue( obj.longValue() );
	}
	
	/**
	 * Converts any <tt>Iterable</tt> to a <tt>ListValue</tt> by converting the
	 * elements in the iterable using {@link convertToValue(Object)}.
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 * @throws ClassCastException if some elemnt of the iterable cannot be
	 *         converted to <tt>Value</tt>
	 */
	public ListValue convertToValue ( Iterable<?> obj ) {
		List<Value<?>> list = new ArrayList<Value<?>>();
		
		for ( Object o : obj ) {
			list.add( convertToValue( o ) );
		}
		
		return new ListValue( list );
	}
	
	/**
	 * Converts a <tt>Map</tt> to a <tt>DirectoryValue</tt> by converting the
	 * keys to Strings and converting the values using
	 * {@link convertToValue(Object)}
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 * @throws ClassCastException if some value in the mappings cannot be
	 *         converted <tt>Value</tt>
	 */
	public DictionaryValue convertToValue ( Map<?,?> obj ) {
		Map<String,Value<?>> map = new HashMap<String,Value<?>>();
		
		for ( Map.Entry<?,?> me : obj.entrySet() ) {
			map.put( me.getKey().toString(), convertToValue( me.getValue() ) );
		}
		
		return new DictionaryValue( map );
	}
}
