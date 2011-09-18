package es.darkhogg.torrent.dht.krpc;

public enum MessageType {
	/** A query message */
	QUERY( "q" ),
	
	/** A response to a query */
	RESPONSE( "r" ),
	
	/** An error message */
	ERROR( "e" );
	
	/** Value for the "y" key in the message bencode */
	private final String string;
	
	/**
	 * @param string
	 *            Value for the <tt>string</tt> field
	 */
	private MessageType ( String string ) {
		this.string = string;
	}
	
	/** @return The value to use with the "y" key of the bencoded message */
	public String getString () {
		return string;
	}
}
