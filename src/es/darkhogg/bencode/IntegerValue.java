package es.darkhogg.bencode;

public final class IntegerValue extends Value<Long> {
	
	private Long value;
	
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
	
}
