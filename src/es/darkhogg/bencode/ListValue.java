package es.darkhogg.bencode;

import java.util.ArrayList;
import java.util.List;

public final class ListValue extends Value<List<Value<?>>> {

	private List<Value<?>> value;

	public ListValue () {
		super( new ArrayList<Value<?>>() );
	}
	
	public ListValue ( List<Value<?>> value ) {
		super( value );
	}
	
	@Override
	public List<Value<?>> getValue () {
		return value;
	}

	@Override
	public void setValue ( List<Value<?>> value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		this.value = value;
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

}
