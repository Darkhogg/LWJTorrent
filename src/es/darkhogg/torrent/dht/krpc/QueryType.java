package es.darkhogg.torrent.dht.krpc;

public enum QueryType {
	/** A ping query */
	PING( "ping" ),
	
	/** A query to obtain contact information for a node */
	FIND_NODE( "find_node" ),
	
	/** A query to obtain peer for an infohash */
	GET_PEER( "get_peer" ),
	
	/** A query to announce yourself to other nodes */
	ANNOUNCE_PEER( "announce_peer" );
	
	/** Internal string used as the "q" value of the request */
	private final String string;
	
	/**
	 * @param string
	 *            Value for the "q" field
	 */
	private QueryType ( String string ) {
		this.string = string;
	}
	
	/** @return The value to be used on the "q" field */
	public String getString () {
		return string;
	}
}
