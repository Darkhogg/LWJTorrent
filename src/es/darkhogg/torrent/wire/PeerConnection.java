package es.darkhogg.torrent.wire;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

/**
 * Represents a connection with a peer using the BitTorrent wire protocol. This
 * class is intended as a simple abstraction over a {@link java.nio.SocketChannel SocketChannel}, but does not provide
 * any
 * associated state.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class PeerConnection implements Closeable {
	
	/**
	 * Channel used for communication
	 */
	private final SocketChannel channel;
	
	/**
	 * Buffer used to receive messages
	 */
	private final ByteBuffer inputBuffer;
	
	/**
	 * Buffer used to send messages
	 */
	private final ByteBuffer outputBuffer;
	
	/**
	 * Creates a connection using all the required parameters
	 * 
	 * @param channel
	 *            Socket channel used for the connection
	 * @param ibufSize
	 *            Size of the input buffer
	 * @param obufSize
	 *            Size of the output buffer
	 * @throws IOException
	 *             if something goes wrong
	 */
	private PeerConnection ( SocketChannel channel, int ibufSize, int obufSize ) throws IOException {
		if ( !channel.isConnected() ) {
			throw new IllegalArgumentException( "Unconnected Socket" );
		}
		
		inputBuffer = ByteBuffer.allocate( ibufSize + 32 );
		inputBuffer.order( ByteOrder.BIG_ENDIAN ).clear().limit( 1 );
		outputBuffer = ByteBuffer.allocate( obufSize + 32 );
		outputBuffer.order( ByteOrder.BIG_ENDIAN ).clear();
		
		this.channel = channel;
		channel.configureBlocking( false );
	}
	
	/**
	 * Reads a {@link HandShakeStart} message from this connection.
	 * 
	 * @return a <tt>HandShakeStart</tt> read from this connection
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public HandShakeStart receiveHandShakeStart () throws IOException {
		// HandShake length
		inputBuffer.clear().limit( 1 );
		while ( inputBuffer.remaining() > 0 ) {
			channel.read( inputBuffer );
		}
		inputBuffer.flip();
		int length = 29 + ( inputBuffer.get() & 0xFF );
		
		// Handshake itself
		inputBuffer.limit( length );
		while ( inputBuffer.remaining() > 0 ) {
			channel.read( inputBuffer );
		}
		inputBuffer.flip();
		return BitTorrentMessageDecoder.decodeHandShakeStartFromBuffer( inputBuffer );
	}
	
	/**
	 * Reads a {@link HandShakeEnd} message from this connection.
	 * 
	 * @return a <tt>HandShakeEnd</tt> read from this connection
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public HandShakeEnd receiveHandShakeEnd () throws IOException {
		inputBuffer.clear().limit( 20 );
		while ( inputBuffer.remaining() > 0 ) {
			channel.read( inputBuffer );
		}
		inputBuffer.flip();
		return BitTorrentMessageDecoder.decodeHandShakeEndFromBuffer( inputBuffer );
	}
	
	/**
	 * Returns a message from the input queue. If no message is available, this
	 * method will wait until one arrives.
	 * <p>
	 * Note that this method will only look for <i>regular</i> messages. In order to receive <i>handshake</i> messages,
	 * you must use {@link #receiveHandShakeStart()} and {@link #receiveHandShakeEnd()}
	 * 
	 * @return A message from the input queue
	 * @throws IOException
	 *             if an error occurs during execution
	 */
	public BitTorrentMessage receiveMessage () throws IOException {
		try {
			// Message length
			inputBuffer.clear().limit( 4 );
			while ( inputBuffer.remaining() > 0 ) {
				channel.read( inputBuffer );
			}
			inputBuffer.flip();
			int length = inputBuffer.getInt();
			
			// Message itself
			inputBuffer.clear().limit( length );
			while ( inputBuffer.remaining() > 0 ) {
				channel.read( inputBuffer );
			}
			inputBuffer.flip();
			return BitTorrentMessageDecoder.decodeMessageFromBuffer( inputBuffer, length );
		} catch ( IOException e ) {
			// Channel error
			close();
			throw e;
		}
	}
	
	/**
	 * Sends a message on this connection.
	 * 
	 * @param msg
	 *            Message to be sent
	 * @throws IOException
	 *             if an error occurs during execution
	 */
	public void sendMessage ( BitTorrentMessage msg ) throws IOException {
		// Send the message
		outputBuffer.clear();
		BitTorrentMessageEncoder.encodeMessageToBuffer( outputBuffer, msg );
		outputBuffer.flip();
		channel.write( outputBuffer );
	}
	
	/**
	 * Closes this connection
	 */
	@Override
	public void close () {
		try {
			channel.close();
		} catch ( IOException e ) {
			// Do nothing
		}
	}
	
	/**
	 * Tests whether this connection is closed.
	 * 
	 * @return <tt>true</tt> if this connection is closed, <tt>false</tt> otherwise
	 */
	public boolean isClosed () {
		return !channel.isOpen();
	}
	
	/**
	 * Fallback for unclosed connections.
	 */
	@Override
	public void finalize () {
		close();
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder( "PeerConnection{" );
		
		try {
			sb.append( channel.getRemoteAddress() );
		} catch ( IOException e ) {
			sb.append( "UnknownAddress" );
		}
		
		return sb.append( "}" ).toString();
	}
	
	/**
	 * Creates a new <tt>PeerConnection</tt> using an already connected socket
	 * channel.
	 * <p>
	 * The connection will use <tt>ibufSize</tt> and <tt>obufSize</tt> as the size for the input and output buffers. The
	 * actual size is <i>at least</i> the values given here, but this method ensures that a <i>Piece</i> message with a
	 * block of size <tt>ibuf</tt>/<tt>obuf</tt> can be respectively sent/received.
	 * 
	 * @param sock
	 *            Channel to use for this connection
	 * @param ibufSize
	 *            Size of the input buffer
	 * @param obufSize
	 *            Size of the output buffer
	 * @return A new connection
	 * @throws IOException
	 *             if some I/O error occurs
	 */
	public static PeerConnection newConnection ( SocketChannel sock, int ibufSize, int obufSize ) throws IOException {
		return new PeerConnection( sock, ibufSize, obufSize );
	}
	
	/**
	 * Creates a new <tt>PeerConnection</tt> using a new channel connected to
	 * the given address
	 * <p>
	 * The connection will use <tt>ibufSize</tt> and <tt>obufSize</tt> as the size for the input and output buffers. The
	 * actual size is <i>at least</i> the values given here, but this method ensures that a <i>Piece</i> message with a
	 * block of size <tt>ibuf</tt>/<tt>obuf</tt> can be respectively sent/received.
	 * 
	 * @param addr
	 *            Address to connect the socket
	 * @param ibufSize
	 *            Size of the input buffer
	 * @param obufSize
	 *            Size of the output buffer
	 * @return A new connection
	 * @throws IOException
	 *             if some I/O error occurs
	 */
	public static PeerConnection newConnection ( SocketAddress addr, int ibufSize, int obufSize ) throws IOException {
		return new PeerConnection( SocketChannel.open( addr ), ibufSize, obufSize );
	}
}
