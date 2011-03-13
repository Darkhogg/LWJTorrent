package es.darkhogg.torrent.wire;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Represents a <i>Piece</i> used by the BitTorrent protocol to send a whole
 * block, that is, a subsection of an actual piece.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class PieceMessage extends BitTorrentMessage {
	
	/**
	 * Piece index
	 */
	private final int index;
	
	/**
	 * Offset of the block
	 */
	private final int offset;
	
	/**
	 * Buffer containing the piece
	 */
	private final ByteBuffer buffer;
	
	/**
	 * Creates a <i>Piece</i> message with the given index, offset and
	 * buffer.
	 * <p>
	 * The passed buffer will be copied into a new buffer. The new buffer will
	 * contain only the remaining contents of the original buffer.
	 * 
	 * @param index Index of the piece
	 * @param offset Offset of the block within the piece
	 * @param buf Contents of the block
	 */
	public PieceMessage (
		int index, int offset, ByteBuffer buf
	) {
		this( index, offset, buf, buf.remaining() );
	}
	
	/**
	 * Creates a <i>Piece</i> message with the given index, offset, buffer and
	 * length.
	 * <p>
	 * The passed buffer will be copied into a new buffer. The new buffer will
	 * contain only the first <tt>length</tt> bytes of the remaining contents
	 * of the original buffer.
	 * 
	 * @param index Index of the piece
	 * @param offset Offset of the block within the piece
	 * @param buf Contents of the block
	 * @param length Length of the block
	 */
	public PieceMessage (
		int index, int offset, ByteBuffer buf, int length
	) {
		if ( index < 0 | offset < 0 ) {
			throw new IllegalArgumentException();
		}
		if ( buf == null ) {
			throw new NullPointerException();
		}
		if ( length > buf.remaining() ) {
			throw new BufferUnderflowException();
		}
		this.index = index;
		this.offset = offset;
		
		this.buffer = ByteBuffer.allocate( length );
		buffer.put( buf );
		buffer.flip();
	}
	
	@Override
	public MessageType getMessageType () {
		return MessageType.PIECE;
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
	 * Returns the contents of the block stored in this message. The returned
	 * buffer is a read-only duplicate of the one stored in the message.
	 * 
	 * @return Contents of the block
	 */
	public ByteBuffer getPieceContents () {
		return buffer.asReadOnlyBuffer();
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder( "PieceMessage" );
		sb.append( "{Type=" ).append( "Piece" );
		sb.append( "; Index=" ).append( index );
		sb.append( "; Offset=" ).append( offset );
		sb.append( "; Length=" ).append( buffer.capacity() );
		return sb.append( "}" ).toString();
	}

}
