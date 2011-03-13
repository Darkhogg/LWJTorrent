package es.darkhogg.wire;

/**
 * Represents a message in the BitTorrent peer protocol
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public abstract class BitTorrentMessage {
	
	/**
	 * Package-private default constructor to avoid external inheritance
	 */
	BitTorrentMessage () {
		// Nothing to do
	}
	
	/**
	 * Returns the type of this message. This method should be used to
	 * determine which message arrived, and <b>not</b> the <tt>instanceof</tt>
	 * operator, as certain messages may share the same subclass due to payload
	 * similarities.
	 * 
	 * @return The type of this message
	 */
	public abstract MessageType getMessageType ();
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder( getClass().getSimpleName() );
		sb.append( "{Type=" ).append( getMessageType() );
		return sb.append( "}" ).toString();
	}
}
