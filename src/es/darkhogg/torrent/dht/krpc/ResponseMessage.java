package es.darkhogg.torrent.dht.krpc;


public final class ResponseMessage extends Message {
	
	public ResponseMessage ( byte[] transaction ) {
		super( transaction, MessageType.RESPONSE );
	}
	
}
