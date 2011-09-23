package es.darkhogg.torrent.wire;

/**
 * Listener interface for receiving {@link PeerSession}-generated events.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public interface PeerListener {
	
	/**
	 * Event generated when a message is received. Events of this kind are
	 * generated <i>after</i> the <tt>peer</tt> state is updated. However, there
	 * is no guarantee that its state haven't changed afterwards.
	 * 
	 * @param peer
	 *            Peer who generated the event
	 * @param message
	 *            Message which generated the event
	 */
	public abstract void onReceive ( PeerSession peer, BitTorrentMessage message );
	
	/**
	 * Event generated when a message is sent. Events of this kind are generated
	 * <i>after</i> the message has been enqueued and the <tt>peer</tt> state is
	 * updated. However, there is no guarantee that the message is fully sent or
	 * that its state haven't changed afterwards.
	 * 
	 * @param peer
	 *            Peer who generated the event
	 * @param message
	 *            Message which generated the event
	 */
	public abstract void onSend ( PeerSession peer, BitTorrentMessage message );
	
	/**
	 * Event generated when a peer is closed. Events of this kind are generated
	 * <i>after</i> the peer is closed, just before its controlling thread is
	 * terminated.
	 * 
	 * @param peer
	 *            Peer who generated the event
	 */
	public abstract void onClose ( PeerSession peer );
	
}
