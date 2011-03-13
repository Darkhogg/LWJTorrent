package es.darkhogg.torrent.wire;

/**
 * Represents a <i>Request</i> message of the BitTorrent protocol.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class RequestMessage extends BitTorrentMessage {
	
	/**
	 * Piece index
	 */
	private final int index;
	
	/**
	 * Offset of the block
	 */
	private final int offset;
	
	/**
	 * Length of the block
	 */
	private final int length;
	
	/**
	 * Creates a <i>Request</i> message with the given index, offset and length.
	 * 
	 * @param index Index of the piece
	 * @param offset Offset of the block within the piece
	 * @param length Length of the block.
	 */
	public RequestMessage ( int index, int offset, int length ) {
		if ( index < 0 | offset < 0 | length < 0 ) {
			throw new IllegalArgumentException();
		}
		this.index = index;
		this.offset = offset;
		this.length = length;
	}
	
	@Override
	public MessageType getMessageType () {
		return MessageType.REQUEST;
	}
	
	/**
	 * Returns the index of the piece being requested.
	 * 
	 * @return Index of the piece.
	 */
	public int getIndex () {
		return index;
	}
	
	/**
	 * Returns the offset of the block being requested.
	 * 
	 * @return Offset of the block
	 */
	public int getOffset () {
		return offset;
	}
	
	/**
	 * Returns the length of the block being requested.
	 * 
	 * @return Length of the block
	 */
	public int getLength () {
		return length;
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder( "RequestMessage" );
		sb.append( "{Type=" ).append( "Request" );
		sb.append( "; Index=" ).append( index );
		sb.append( "; Offset=" ).append( offset );
		sb.append( "; Length=" ).append( length );
		return sb.append( "}" ).toString();
	}
	
}
