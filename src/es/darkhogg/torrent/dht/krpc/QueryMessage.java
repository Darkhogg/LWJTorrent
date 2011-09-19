package es.darkhogg.torrent.dht.krpc;


public final class QueryMessage extends Message {
	
	public QueryMessage ( byte[] transaction ) {
		super( transaction, MessageType.QUERY );
	}
	
}
