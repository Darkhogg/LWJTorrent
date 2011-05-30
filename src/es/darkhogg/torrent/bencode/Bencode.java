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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Namespace for global constants and functions of this package
 * 
 * @author Daniel Escoz
 * @version 1.0.1
 */
public class Bencode {
	
	/**
	 * UTF-8 charset used for Bencode
	 */
	public static final Charset UTF8 = Charset.forName( "UTF-8" );
	
	/**
	 * Gets a child element from the passed value using the strings as a path.
	 * <p>
	 * Each element of the <tt>strings</tt> parameter represents an element of
	 * the path. Dictionaries can be accessed using its string keys and lists
	 * using numeric strings. If an element doesn't exist, this method returns
	 * <tt>null</tt>.
	 * 
	 * @param from Value to get the child from
	 * @param strings Path to the child
	 * @return Child accessed from the path, or <tt>null</tt> if it doesn't
	 *         exist.
	 */
	public static Value<?> getChildValue ( Value<?> from, String... strings ) {
		Value<?> value = from;
		
		for ( String string : strings ) {
			if ( value instanceof DictionaryValue ) {
				DictionaryValue dval = (DictionaryValue) value;
				Map<String,Value<?>> map = dval.getValue();
				
				value = map.get( string );
				
			} else if ( value instanceof ListValue ) {
				ListValue lval = (ListValue) value;
				List<Value<?>> list = lval.getValue();
				int pos = 0;
				try {
					pos = Integer.parseInt( string );
				} catch ( NumberFormatException e ) {
					return null;
				}
				
				value = list.get( pos );
			} else {
				return null;
			}
		}
		
		return value;
	}
	
	/**
	 * Gets a child element from the passed value using the strings as a path,
	 * converted to an specific value type. If the value does not exist or is
	 * not of that type, this method returns <tt>null</tt>.
	 * <p>
	 * See the description of {@link #getChildValue(Value,String...)} for
	 * more information.
	 * 
	 * @param from Value to get the child from
	 * @param type Expected type of the value returned
	 * @param strings Path to the child
	 * @return Child accessed from the path, or <tt>null</tt> if it doesn't
	 *         exist or is not of the correct type.
	 * @see #getChildValue(Value,String...)
	 */
	public static <T extends Value<?>> T getChildValue ( Value<?> from,
		Class<T> type, String... strings )
	{
		Value<?> val = getChildValue( from, strings );
		
		if ( type.isInstance( val ) ) {
			return type.cast( val );
		}
		
		return null;
	}
	
	/**
	 * Converts a bencode value to a Java standard objects.
	 * <p>
	 * <i>NOTE: This method delegates its calls to its more concrete overloads.
	 * See them for details on how <tt>Value</tt>s are converted</i>
	 * 
	 * @param val The value to be converted
	 * @return An object from the standard Java API that represents the same
	 *         information
	 */
	public static Object convertFromValue ( Value<?> val ) {
		if ( val instanceof IntegerValue ) {
			return convertFromValue( (IntegerValue) val );
			
		} else if ( val instanceof StringValue ) {
			return convertFromValue( (StringValue) val );
			
		} else if ( val instanceof ListValue ) {
			return convertFromValue( (ListValue) val );
			
		} else if ( val instanceof DictionaryValue ) {
			return convertFromValue( (DictionaryValue) val );
			
		} else {
			// This exception is here for completeness
			// As Value cannot be subclassed out of this package, no other
			// classes should ever extend Value than those listed in this
			// method. This is also the reason why it is not documented
			// in a @throws tag.
			throw new IllegalArgumentException();
			
		}
	}
	
	/**
	 * Converts a bencode integer value to a <tt>Long</tt>
	 * 
	 * @param val The value to be converted
	 * @return A <tt>Long</tt> object that represents the given value
	 */
	public static Long convertFromValue ( IntegerValue val ) {
		return val.getValue();
	}
	
	/**
	 * Converts a bencode string value to a <tt>String</tt>
	 * 
	 * @param val The value to be converted
	 * @return A <tt>String</tt> object that represents the given value
	 */
	public static String convertFromValue ( StringValue val ) {
		return val.toString();
	}
	
	/**
	 * Converts a bencode list value to a <tt>List</tt> of converted values
	 * 
	 * @param val The value to be converted
	 * @return A <tt>List</tt> object that represents the given value
	 */
	public static List<Object> convertFromValue ( ListValue val ) {
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
	public static Map<String,Object> convertFromValue ( DictionaryValue val ) {
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
	 * except in the case of a <tt>Value</tt> itself, which is immediately
	 * returned. If the object cannot be converted to a <tt>Value</tt>, a
	 * <tt>ClassCastException</tt> is thrown.
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 * @throws ClassCastException if the object cannot be converted to
	 *         <tt>Value</tt>
	 */
	public static Value<?> convertToValue ( Object obj ) {
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
	public static StringValue convertToValue ( byte[] obj ) {
		return new StringValue( obj );
	}
	
	/**
	 * Converts a char array to a bencode string value by creating a new 
	 * <tt>StringValue</tt> from a new String created from the given array
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 */
	public static StringValue convertToValue ( char[] obj ) {
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
	public static StringValue convertToValue ( CharSequence obj ) {
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
	public static IntegerValue convertToValue ( Number obj ) {
		return new IntegerValue( obj.longValue() );
	}
	
	/**
	 * Converts any <tt>Iterable</tt> to a <tt>ListValue</tt> by converting the
	 * elements in the iterable using {@link Bencode#convertToValue(Object)}.
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 * @throws ClassCastException if some element of the <tt>Iterable</tt>
	 *         cannot be converted to <tt>Value</tt>
	 */
	public static ListValue convertToValue ( Iterable<?> obj ) {
		List<Value<?>> list = new ArrayList<Value<?>>();
		
		for ( Object o : obj ) {
			list.add( convertToValue( o ) );
		}
		
		return new ListValue( list );
	}
	
	/**
	 * Converts a <tt>Map</tt> to a <tt>DirectoryValue</tt> by converting the
	 * keys to Strings and converting the values using
	 * {@link Bencode#convertToValue(Object)}
	 * 
	 * @param obj Object to convert into a <tt>Value</tt>
	 * @return a value that represents the same information that the given
	 *         object
	 * @throws ClassCastException if some value in the mapping cannot be
	 *         converted to <tt>Value</tt>
	 */
	public static DictionaryValue convertToValue ( Map<?,?> obj ) {
		Map<String,Value<?>> map = new HashMap<String,Value<?>>();
		
		for ( Map.Entry<?,?> me : obj.entrySet() ) {
			map.put( me.getKey().toString(), convertToValue( me.getValue() ) );
		}
		
		return new DictionaryValue( map );
	}

	/**
	 * Returns a copy of the given <tt>Value</tt>. The returned copy is
	 * guaranteed to be independent from the passed object, in that no
	 * references to child <tt>Value</tt>s or other mutable objects are held
	 * by the copy.
	 * <p>
	 * <i>NOTE: This method delegates its calls to its more specific
	 * overloadings. See them for details on how <tt>Value</tt>s
	 * are copied.</i>
	 * 
	 * @param value Bencode value to copy
	 * @return A copy of the given <tt>value</tt>
	 */
	public static Value<?> copyOf ( Value<?> value ) {
		if ( value instanceof IntegerValue ) {
			return copyOf( (IntegerValue) value );
			
		} else if ( value instanceof StringValue ) {
			return copyOf( (StringValue) value );
			
		} else if ( value instanceof ListValue ) {
			return copyOf( (ListValue) value );
			
		} else if ( value instanceof DictionaryValue ) {
			return copyOf( (DictionaryValue) value );
			
		} else {
			// This exception is here for completeness
			// As Value cannot be subclassed out of this package, no other
			// classes should ever extend Value than those listed in this
			// method. This is also the reason why it is not documented
			// in a @throws tag.
			throw new IllegalArgumentException();
			
		}
	}
	
	/**
	 * Returns a copy of the given <tt>IntegerValue</tt>. The copy will
	 * initially have the same value as the original. Changes on the copy won't
	 * affect the original and vice-versa.
	 * 
	 * @param value Bencode integer value to copy
	 * @return A copy of the given <tt>value</tt>
	 */
	public static IntegerValue copyOf ( IntegerValue value ) {
		return new IntegerValue( value.getValue() );
	}
	
	/**
	 * Returns a copy of the given <tt>StringValue</tt>. The copy will
	 * initially have the same value as the original. Changes on the copy won't
	 * affect the original and vice-versa.
	 * 
	 * @param value Bencode string value to copy
	 * @return A copy of the given <tt>value</tt>
	 */
	public static StringValue copyOf ( StringValue value ) {
		return new StringValue( value.getValue() );
	}
	
	/**
	 * Returns a copy of the given <tt>ListValue</tt>. Changes on the copy
	 * won't affect the original and vice-versa.
	 * <p>
	 * The initial value of the copy is a <tt>List</tt> which elements are
	 * copies of the elements of the original object, in the same order.
	 * This ensures that no mutable references are shared between the original
	 * and the copy.
	 * 
	 * @param value Bencode list value to copy
	 * @return A copy of the given <tt>value</tt>
	 */
	public static ListValue copyOf ( ListValue value ) {
		List<Value<?>> lv = new ArrayList<Value<?>>();
		for ( Value<?> v : value.getValue() ) {
			lv.add( copyOf( v ) );
		}
		
		return new ListValue( lv );
	}
	
	/**
	 * Returns a copy of the given <tt>DisctionaryValue</tt>. Changes on the
	 * copy won't affect the original and vice-versa.
	 * <p>
	 * The initial value of the copy is a <tt>Map</tt> which keys are the same
	 * and its assocciated values are copies of the corresponding values of the
	 * original object. This ensures that no mutable references are shared
	 * between the original and the copy.
	 * 
	 * @param value Bencode dictionary value to copy
	 * @return A copy of the given <tt>value</tt>
	 */
	public static DictionaryValue copyOf ( DictionaryValue value ) {
		Map<String,Value<?>> mv = new HashMap<String,Value<?>>();
		for ( Map.Entry<String,Value<?>> me : value.getValue().entrySet() ) {
			mv.put( me.getKey(), copyOf( me.getValue() ) );
		}
		
		return new DictionaryValue( mv );
	}
}
