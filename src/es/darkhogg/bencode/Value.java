package es.darkhogg.bencode;

public abstract class Value<T> {
	
	public Value ( T value ) {
		setValue( value );
	}
	
	public abstract T getValue ();
	
	public abstract void setValue ( T value );
	
}
