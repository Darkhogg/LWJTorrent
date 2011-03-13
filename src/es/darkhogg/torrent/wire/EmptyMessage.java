package es.darkhogg.torrent.wire;

/**
 * Represents a message with no payload. This class is used to represent
 * <i>Keep-Alive</i>, <i>Choke</i>, <i>Unchoke</i>, <i>Interested</i> and
 * <i>NotInterested</i> message types.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class EmptyMessage extends BitTorrentMessage {

	/**
	 * The only instance of the <i>Keep-Alive</i> message
	 */
	public static final EmptyMessage KEEP_ALIVE =
		new EmptyMessage( MessageType.KEEP_ALIVE );
	
	/**
	 * The only instance of the <i>Choke</i> message
	 */
	public static final EmptyMessage CHOKE =
		new EmptyMessage( MessageType.CHOKE );
	
	/**
	 * The only instance of the <i>Unchoke</i> message
	 */
	public static final EmptyMessage UNCHOKE =
		new EmptyMessage( MessageType.UNCHOKE );
	
	/**
	 * The only instance of the <i>Interested</i> message
	 */
	public static final EmptyMessage INTERESTED =
		new EmptyMessage( MessageType.INTERESTED );
	
	/**
	 * The only instance of the <i>NotInterested</i> message
	 */
	public static final EmptyMessage UNINTERESTED =
		new EmptyMessage( MessageType.UNINTERESTED );
	
	/**
	 * The type of this message
	 */
	private final MessageType type;
	
	/**
	 * Creates a message with a given type
	 * 
	 * @param type
	 */
	private EmptyMessage ( MessageType type ) {
		this.type = type;
	}
	
	@Override
	public MessageType getMessageType () {
		return type;
	}

}
