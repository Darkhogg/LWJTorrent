package es.darkhogg.bencode;

public final class StringValue extends Value<byte[]> {
	
	private byte[] value;
	private String str;

	public StringValue ( byte[] value ) {
		super( value );
	}
	
	public StringValue ( String value ) {
		this( value.getBytes( Bencode.UTF8 ) );
	}
	
	public byte[] getValue () {
		return value;
	}
	
	public void setValue ( byte[] value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		this.value = value;
		this.str = new String( value, Bencode.UTF8 );
	}
	
	public void setStringValue ( String value ) {
		setValue( value.getBytes( Bencode.UTF8 ) );
	}
	
	@Override
	public String toString () {
		return "\"" + str + "\"";
	}
}
