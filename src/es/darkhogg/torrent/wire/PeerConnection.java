package es.darkhogg.torrent.wire;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

/**
 * Represents a connection with a peer using the BitTorrent wire protocol. This
 * class is intended as a simple abstraction over a
 * {@link java.nio.SocketChannel SocketChannel}, but does not provide any
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
	 * Shortcut method for {@link #process(long,TimeUnit,boolean)} called with a
	 * value of <tt>false</tt> as the <tt>returnOnMsg</tt> argument.
	 * 
	 * @param timeout
	 *            Maximum time to execute this method
	 * @param unit
	 *            Time unit of the <tt>timeout</tt> argument
	 * @throws IOException
	 *             if an I/O error occurs
	 * @see #process(long, TimeUnit, boolean)
	 */
	/*
	 * public void process ( long timeout, TimeUnit unit ) throws IOException {
	 * process( timeout, unit, false ); }
	 */
	
	/**
	 * Reads and writes messages from/to the remote end of this connection for
	 * the time specified in <tt>timeout</tt> and <tt>unit</tt>. If the
	 * <tt>returnOnMsg</tt> argument is <tt>true</tt>, this method returns
	 * <tt>false</tt>as soon as it receives a message. In any other case, this
	 * method returns <tt>true</tt> when the specified time has passed.
	 * 
	 * @param timeout
	 *            Maximum time to execute this method
	 * @param unit
	 *            Time unit of the <tt>timeout</tt> argument
	 * @param returnOnMsg
	 *            Whether receivig a message should end this method call
	 * @return <tt>true</tt> if, and only if, this method returns after waiting
	 *         the whole timeout
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	/*
	 * public boolean process ( long timeout, TimeUnit unit, boolean returnOnMsg
	 * ) throws IOException { long ndTime = System.nanoTime() + unit.toNanos(
	 * timeout );
	 * 
	 * try { boolean quit = false; while ( !quit | System.nanoTime() < ndTime )
	 * {
	 * 
	 * // Send the full output while ( !outputQueue.isEmpty() ) {
	 * outputBuffer.clear(); BitTorrentMessage msg = outputQueue.remove();
	 * BitTorrentMessageEncoder.encodeMessageToBuffer( outputBuffer, msg );
	 * outputBuffer.flip(); channel.write( outputBuffer ); }
	 * 
	 * // Receive input... if ( inputState == 0 ) { inputBuffer.clear().limit( 1
	 * ); inputState = 1; } if ( inputState == 1 ) { channel.read( inputBuffer
	 * ); if ( inputBuffer.remaining() == 0 ) { inputBuffer.flip();
	 * nextMessageLength = 29 + ( (int) ( inputBuffer.get() ) & 0xFF );
	 * inputBuffer.limit( nextMessageLength ); inputState = 2; } } if (
	 * inputState == 2 ) { channel.read( inputBuffer ); if (
	 * inputBuffer.remaining() == 0 ) { inputBuffer.flip(); BitTorrentMessage
	 * msg = BitTorrentMessageDecoder.decodeHandShakeStartFromBuffer(
	 * inputBuffer ); inputQueue.add( msg ); inputBuffer.clear().limit( 20 );
	 * inputState = 3; } } if ( inputState == 3 ) { channel.read( inputBuffer );
	 * if ( inputBuffer.remaining() == 0 ) { inputBuffer.flip();
	 * BitTorrentMessage msg =
	 * BitTorrentMessageDecoder.decodeHandShakeEndFromBuffer( inputBuffer );
	 * inputQueue.add( msg ); inputBuffer.clear().limit( 4 ); inputState = 4; }
	 * } if ( inputState == 4 ) { channel.read( inputBuffer ); if (
	 * inputBuffer.remaining() == 0 ) { inputBuffer.flip(); nextMessageLength =
	 * inputBuffer.getInt(); inputBuffer.clear().limit( nextMessageLength );
	 * inputState = 5; } } if ( inputState == 5 ) { channel.read( inputBuffer );
	 * if ( inputBuffer.remaining() == 0 ) { inputBuffer.flip();
	 * BitTorrentMessage msg = BitTorrentMessageDecoder.decodeMessageFromBuffer(
	 * inputBuffer, nextMessageLength ); inputQueue.add( msg );
	 * inputBuffer.clear().limit( 4 ); inputState = 4; quit = returnOnMsg; } }
	 * 
	 * Thread.sleep( 16 ); }
	 * 
	 * return !quit; } catch ( InterruptedException e ) { // Close the
	 * connection close(); return false; } }
	 */
	
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
	 * Note that this method will only look for <i>regular</i> messages. In
	 * order to receive <i>handshake</i> messages, you must use
	 * {@link #receiveHandShakeStart()} and {@link #receiveHandShakeEnd()}
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
	 * @return <tt>true</tt> if this connection is closed, <tt>false</tt>
	 *         otherwise
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
	
	/**
	 * Creates a new <tt>PeerConnection</tt> using an already connected socket
	 * channel.
	 * <p>
	 * The connection will use <tt>ibufSize</tt> and <tt>obufSize</tt> as the
	 * size for the input and output buffers. The actual size is <i>at least</i>
	 * the values given here, but this method ensures that a <i>Piece</i>
	 * message with a block of size <tt>ibuf</tt>/<tt>obuf</tt> can be
	 * respectively sent/received.
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
	 * The connection will use <tt>ibufSize</tt> and <tt>obufSize</tt> as the
	 * size for the input and output buffers. The actual size is <i>at least</i>
	 * the values given here, but this method ensures that a <i>Piece</i>
	 * message with a block of size <tt>ibuf</tt>/<tt>obuf</tt> can be
	 * respectively sent/received.
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
