package es.darkhogg.torrent.wire;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;

/**
 * A class used to encode instances of {@link BitTorrentMessage} into
 * {@link ByteBuffer}s.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class BitTorrentMessageEncoder {
	
	/**
	 * ISO-8859-1 charset
	 */
	private final static Charset ISO_8859_1 = Charset.forName( "ISO-8859-1" );
	
	/**
	 * Private constructor
	 */
	private BitTorrentMessageEncoder () {}
	
	/**
	 * Encodes the given message into the given <tt>buffer</tt> in the format
	 * specified in the BitTorrent protocol specification.
	 * <p>
	 * If this method throws a {@link BufferOverflowException}, the actual
	 * amount of bytes written into the buffer is not determined.
	 * 
	 * @param buffer Destination buffer
	 * @param msg Message to encode
	 * @throws NullPointerException if any of the arguments is <tt>null</tt>
	 * @throws BufferOverflowException if there is not enough space to encode
	 *         the full message
	 * @throws IllegalArgumentException if <tt>msg</tt> is not a valid message
	 */
	public void encodeMessageToBuffer (
		ByteBuffer buffer, BitTorrentMessage msg
	) {
		
		// Branch
		switch ( msg.getMessageType() ) {
			case KEEP_ALIVE:
				buffer.putInt( 0 );
			break;
			
			case CHOKE:
			case UNCHOKE:
			case INTERESTED:
			case UNINTERESTED:
				buffer.putInt( 1 );
				buffer.put( (byte) msg.getMessageType().getId() );
			break;

			case HANDSHAKE_START:
				encodeHandShakeStart( buffer, (HandShakeStart) msg );
			break;
			
			case HANDSHAKE_END:
				encodeHandShakeEnd( buffer, (HandShakeEnd) msg );
			break;
			
			case HAVE:
				encodeHave( buffer, (HaveMessage) msg );
			break;
			
			case BITFIELD:
				encodeBitField( buffer, (BitFieldMessage) msg );
			break;
			
			case REQUEST:
				encodeRequest( buffer, (RequestMessage) msg );
			break;
			
			case PIECE:
				encodePiece( buffer, (PieceMessage) msg );
			break;

			case CANCEL:
				encodeCancel( buffer, (CancelMessage) msg );
			break;
			
			case PORT:
				encodePort( buffer, (PortMessage) msg );
			break;
			
			default:
				throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Encodes the given message into the given <tt>buffer</tt>
	 * 
	 * @param buffer Destination buffer
	 * @param msg Message to encode
	 * @throws BufferOverflowException if there is not enough space to encode
	 *         the full message
	 */
	private void encodeHandShakeStart ( ByteBuffer buffer, HandShakeStart msg ) {
		// Protocol
		buffer.put( (byte) msg.getProtocolName().length() );
		buffer.put( msg.getProtocolName().getBytes( ISO_8859_1 ) );
		
		// Reserved Bits
		byte[] bits = new byte[ 8 ];
		BitSet flags = msg.getFlags();
		for ( int i = 0; i < 64; i++ ) {
			if ( flags.get( i ) ) {
				int bInd = 7 - (i/8);
				int bMask = 1 << (i%8);
				bits[ bInd ] |= bMask;
			}
		}
		
		// Hash
		buffer.put( msg.getHash().getBytes() );
	}

	/**
	 * Encodes the given message into the given <tt>buffer</tt>
	 * 
	 * @param buffer Destination buffer
	 * @param msg Message to encode
	 * @throws BufferOverflowException if there is not enough space to encode
	 *         the full message
	 */
	private void encodeHandShakeEnd ( ByteBuffer buffer, HandShakeEnd msg ) {
		buffer.put( msg.getPeerId() );
	}

	/**
	 * Encodes the given message into the given <tt>buffer</tt>
	 * 
	 * @param buffer Destination buffer
	 * @param msg Message to encode
	 * @throws BufferOverflowException if there is not enough space to encode
	 *         the full message
	 */
	private void encodeHave ( ByteBuffer buffer, HaveMessage msg ) {
		buffer.putInt( 5 );
		buffer.put( (byte) msg.getMessageType().getId() );
		buffer.putInt( msg.getPieceIndex() );
	}

	/**
	 * Encodes the given message into the given <tt>buffer</tt>
	 * 
	 * @param buffer Destination buffer
	 * @param msg Message to encode
	 * @throws BufferOverflowException if there is not enough space to encode
	 *         the full message
	 */
	private void encodeBitField ( ByteBuffer buffer, BitFieldMessage msg ) {
		BitSet bitfield = msg.getBitField();
		int nbytes = ( bitfield.size() + 7 ) / 8;
		byte[] bytes = new byte[ nbytes ];
		for ( int i = 0; i < nbytes*8; i++ ) {
			if ( bitfield.get( i ) ) {
				int bInd = i%8;
				int bMask = 1 << (7-(i/8));
				bytes[ bInd ] |= bMask;
			}
		}
		
		buffer.putInt( nbytes+1 );
		buffer.put( (byte) msg.getMessageType().getId() );
		buffer.put( bytes );
	}

	/**
	 * Encodes the given message into the given <tt>buffer</tt>
	 * 
	 * @param buffer Destination buffer
	 * @param msg Message to encode
	 * @throws BufferOverflowException if there is not enough space to encode
	 *         the full message
	 */
	private void encodeRequest ( ByteBuffer buffer, RequestMessage msg ) {
		buffer.putInt( 13 );
		buffer.put( (byte) msg.getMessageType().getId() );
		buffer.putInt( msg.getIndex() );
		buffer.putInt( msg.getOffset() );
		buffer.putInt( msg.getLength() );
	}

	/**
	 * Encodes the given message into the given <tt>buffer</tt>
	 * 
	 * @param buffer Destination buffer
	 * @param msg Message to encode
	 * @throws BufferOverflowException if there is not enough space to encode
	 *         the full message
	 */
	private void encodePiece ( ByteBuffer buffer, PieceMessage msg ) {
		ByteBuffer contents = msg.getPieceContents();
		contents.rewind().limit( contents.capacity() );
		
		buffer.putInt( 9 + contents.capacity() );
		buffer.put( (byte) msg.getMessageType().getId() );
		buffer.putInt( msg.getIndex() );
		buffer.putInt( msg.getOffset() );
		buffer.put( contents );
	}

	/**
	 * Encodes the given message into the given <tt>buffer</tt>
	 * 
	 * @param buffer Destination buffer
	 * @param msg Message to encode
	 * @throws BufferOverflowException if there is not enough space to encode
	 *         the full message
	 */
	private void encodeCancel ( ByteBuffer buffer, CancelMessage msg ) {
		buffer.putInt( 13 );
		buffer.put( (byte) msg.getMessageType().getId() );
		buffer.putInt( msg.getIndex() );
		buffer.putInt( msg.getOffset() );
		buffer.putInt( msg.getLength() );
	}

	/**
	 * Encodes the given message into the given <tt>buffer</tt>
	 * 
	 * @param buffer Destination buffer
	 * @param msg Message to encode
	 * @throws BufferOverflowException if there is not enough space to encode
	 *         the full message
	 */
	private void encodePort ( ByteBuffer buffer, PortMessage msg ) {
		buffer.putInt( 3 );
		buffer.put( (byte) msg.getMessageType().getId() );
		buffer.putShort( (short) msg.getPort() );
	}
	
}