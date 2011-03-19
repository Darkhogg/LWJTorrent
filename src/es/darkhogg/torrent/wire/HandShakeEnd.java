package es.darkhogg.torrent.wire;

import es.darkhogg.torrent.data.PeerId;

/**
 * Represents the part of the BitTorrent protocol handshake that contains the
 * peer ID.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class HandShakeEnd extends BitTorrentMessage {

	/**
	 * Peer ID
	 */
	private final PeerId peerId;
	
	/**
	 * Constructs this message with the given peer ID.
	 * 
	 * @param peerId Peer ID of this handshake
	 * @throws NullPointerException if <tt>peerId</tt> is <tt>null</tt>
	 */
	public HandShakeEnd ( PeerId peerId ) {
		if ( peerId == null ) {
			throw new NullPointerException();
		}
		
		this.peerId = peerId;
	}
	
	@Override
	public MessageType getMessageType () {
		return MessageType.HANDSHAKE_END;
	}
	
	/**
	 * Returns the peer ID of this message.
	 * 
	 * @return Peer ID of the handshake
	 */
	public PeerId getPeerId () {
		return peerId;
	}

	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder( "HandShakeEnd" );
		sb.append( "{Type=Handshake-End; " );
		sb.append( "PeerID=" ).append( peerId );
		return sb.append( "}" ).toString();
	}
}
