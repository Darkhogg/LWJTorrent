package es.darkhogg.bencode;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public final class DictionaryValue extends Value<SortedMap<String,Value<?>>> {

	private SortedMap<String, Value<?>> value;
	
	public DictionaryValue () {
		super( new TreeMap<String,Value<?>>() );
	}
	
	public DictionaryValue ( SortedMap<String,Value<?>> value ) {
		super( value );
	}
	
	@Override
	public SortedMap<String, Value<?>> getValue () {
		return value;
	}

	@Override
	public void setValue ( SortedMap<String, Value<?>> value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		this.value = value;
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
