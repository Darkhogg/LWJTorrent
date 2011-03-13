package es.darkhogg.wire;

import java.util.BitSet;

/**
 * Represents the <i>BitField</i> message of the BitTorrent peer protocol.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class BitFieldMessage extends BitTorrentMessage {
	
	/**
	 * Bit field of the message
	 */
	private final BitSet bitField;
	
	/**
	 * Creates a <i>BitField</i> message using the given {@link BitSet} as its
	 * bit field.
	 * <p>
	 * The bit set passed is copied so further modifications does not affect
	 * this object.
	 * 
	 * @param bitField The bit field of this method.
	 */
	public BitFieldMessage ( BitSet bitField ) {
		this.bitField = (BitSet) bitField.clone();
	}
	
	@Override
	public MessageType getMessageType () {
		return MessageType.BITFIELD;
	}
	
	/**
	 * Returns a {@link BitSet} containing the bit field of the message.
	 * <p>
	 * The returned is copied so further modifications does not affect this
	 * object.
	 * 
	 * @return The bit field of this object
	 */
	public BitSet getBitField () {
		return (BitSet) bitField.clone();
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder( "BitFieldMessage" );
		sb.append( "{Type=" ).append( "BitField" );
		sb.append( "; BitField=" ).append( bitField );
		return sb.append( "}" ).toString();
	}
}
