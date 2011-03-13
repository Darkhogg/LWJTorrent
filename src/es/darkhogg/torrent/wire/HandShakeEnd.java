package es.darkhogg.torrent.wire;

import java.util.Arrays;

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
	private final byte[] peerId;
	
	/**
	 * Constructs this message with the given peer ID.
	 * 
	 * @param peerId Peer ID of this handshake
	 * @throws NullPointerException if <tt>peerId</tt> is <tt>null</tt>
	 * @throws IllegalArgumentException if <tt>peerId</tt> has a length
	 *         different than 20
	 */
	public HandShakeEnd ( byte[] peerId ) {
		if ( peerId.length != 20 ) {
			throw new IllegalArgumentException();
		}
		
		this.peerId = Arrays.copyOf( peerId, peerId.length );
	}
	
	@Override
	public MessageType getMessageType () {
		return MessageType.HANDSHAKE_END;
	}
	
	/**
	 * Returns the 20-byte peer ID of this message.
	 * 
	 * @return Peer ID of the handshake
	 */
	public byte[] getPeerId () {
		return Arrays.copyOf( peerId, peerId.length );
	}

}
