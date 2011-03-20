package es.darkhogg.torrent.wire;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import es.darkhogg.torrent.data.PeerId;
import es.darkhogg.torrent.data.Sha1Hash;

/**
 * Represents a connection with a peer using the BitTorrent wire protocol.
 * <p>
 * TODO Finish documenting it
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
	 * Peer ID sent in the initial handshake
	 */
	private PeerId localPeerId = null;
	
	/**
	 * Peer ID received in the initial handshake
	 */
	private PeerId remotePeerId = null;
	
	/**
	 * Protocol name sent in the initial handshake
	 */
	private String localProtocol = null;
	
	/**
	 * Protocol name received in the initial handshake
	 */
	private String remoteProtocol = null;
	
	/**
	 * Info Hash sent in the initial handshake
	 */
	private Sha1Hash localHash = null;
	
	/**
	 * Info Hash received in the initial handshake
	 */
	private Sha1Hash remoteHash = null;

	/**
	 * Reserved bits sent in the initial handshake
	 */
	private final BitSet localFlags = new BitSet();
	
	/**
	 * Reserved bits received in the initial handshake
	 */
	private final BitSet remoteFlags = new BitSet();

	/**
	 * Parts that the local end claims to have
	 */
	private final BitSet localClaimedPieces = new BitSet();
	
	/**
	 * Parts that the remote end claims to have
	 */
	private final BitSet remoteClaimedPieces = new BitSet();
	
	/**
	 * Whether the local end is choking
	 */
	private boolean localChoking = true;
	
	/**
	 * Whether the remote end is choking
	 */
	private boolean remoteChoking = true;
	
	/**
	 * Whether the local end is interested
	 */
	private boolean localInterested = false;
	
	/**
	 * Whether the remote end is interested
	 */
	private boolean remoteInterested = false;

	/**
	 * Whether the <i>HandShake Start</i> has been sent by the local end
	 */
	private boolean localHandShakeStarted = false;

	/**
	 * Whether the <i>HandShake Start</i> has been received from the remote end
	 */
	private boolean localHandShakeFinished = false;
	
	/**
	 * Whether the <i>HandShake End</i> has been sent by the local end
	 */
	private boolean remoteHandShakeStarted = false;
	
	/**
	 * Whether the <i>HandShake End</i> has been received from the remote end
	 */
	private boolean remoteHandShakeFinished = false;
	
	/**
	 * Buffer used to receive messages
	 */
	private final ByteBuffer inputBuffer;
	
	/**
	 * Buffer used to send messages
	 */
	private final ByteBuffer outputBuffer;

	/**
	 * Messages received but not yet processed by the client
	 */
	private final Queue<BitTorrentMessage> inputQueue =
		new LinkedList<BitTorrentMessage>();
	
	/**
	 * Messages not yet sent but scheduled to be sent
	 */
	private final Queue<BitTorrentMessage> outputQueue =
		new LinkedList<BitTorrentMessage>();
	
	/**
	 * State of the proccess method
	 */
	private int inputState = 0;
	
	/**
	 * Length of the message that is being read
	 */
	private int nextMessageLength = 0;
	
	/**
	 * Creates a connection using all the required parameters
	 * 
	 * @param channel Socket channel used for the connection
	 * @param ibufSize Size of the input buffer
	 * @param obufSize Size of the output buffer
	 * @throws IOException if something goes wrong
	 */
	private PeerConnection ( SocketChannel channel, int ibufSize, int obufSize )
	throws IOException {
		if ( !channel.isConnected() ) {
			throw new IllegalArgumentException( "Unconnected Socket" );
		}

		inputBuffer = ByteBuffer.allocate( ibufSize + 32 );
		inputBuffer.order( ByteOrder.BIG_ENDIAN );
		outputBuffer = ByteBuffer.allocate( obufSize + 32 );
		outputBuffer.order( ByteOrder.BIG_ENDIAN );
		
		this.channel = channel;
		channel.configureBlocking( false );
	}
	
	/**
	 * TODO Document this
	 * 
	 * @param timeout Maximum time to execute this method
	 * @param unit Time unit of the <tt>timeout</tt> argument
	 * @throws IOException if an I/O error occurs
	 */
	public void process ( long timeout, TimeUnit unit )
	throws IOException {
		long ndTime = System.nanoTime() + unit.toNanos( timeout );
		
		try {
			while ( System.nanoTime() < ndTime ) {
				
				// Send the full output
				while ( !outputQueue.isEmpty() ) {
					outputBuffer.clear();
					BitTorrentMessage msg = outputQueue.remove();
					processOutputMessage( msg );
					BitTorrentMessageEncoder.encodeMessageToBuffer(
						outputBuffer, msg );
					outputBuffer.flip();
					channel.write( outputBuffer );
				}
				
				// Receive input...
				if ( inputState == 0 ) {
					inputBuffer.clear().limit( 1 );
					inputState = 1;
				}
				if ( inputState == 1 ) {
					channel.read( inputBuffer );
					if ( inputBuffer.remaining() == 0 ) {
						inputBuffer.flip();
						nextMessageLength = 29 +
							( (int)(inputBuffer.get()) & 0xFF );
						inputBuffer.limit( nextMessageLength );
						inputState = 2;
					}
				}
				if ( inputState == 2 ) {
					channel.read( inputBuffer );
					if ( inputBuffer.remaining() == 0 ) {
						inputBuffer.flip();
						BitTorrentMessage msg = BitTorrentMessageDecoder
							.decodeHandShakeStartFromBuffer( inputBuffer );
						processInputMessage( msg );
						inputQueue.add( msg );
						inputBuffer.clear().limit( 20 );
						inputState = 3;
					}
				}
				if ( inputState == 3 ) {
					channel.read( inputBuffer );
					if ( inputBuffer.remaining() == 0 ) {
						inputBuffer.flip();
						BitTorrentMessage msg = BitTorrentMessageDecoder
							.decodeHandShakeEndFromBuffer( inputBuffer );
						processInputMessage( msg );
						inputQueue.add( msg );
						inputBuffer.clear().limit( 4 );
						inputState = 4;
					}
				}
				if ( inputState == 4 ) {
					channel.read( inputBuffer );
					if ( inputBuffer.remaining() == 0 ) {
						inputBuffer.flip();
						nextMessageLength = inputBuffer.getInt();
						inputBuffer.clear().limit( nextMessageLength );
						inputState = 5;
					}
				}
				if ( inputState == 5 ) {
					channel.read( inputBuffer );
					if ( inputBuffer.remaining() == 0 ) {
						inputBuffer.flip();
						BitTorrentMessage msg = 
							BitTorrentMessageDecoder.decodeMessageFromBuffer(
								inputBuffer, nextMessageLength );
						processInputMessage( msg );
						inputQueue.add( msg );
						inputBuffer.clear().limit( 4 );
						inputState = 4;
					}
				}
				
				Thread.sleep( 16 );
			}
		} catch ( InterruptedException e ) {
			// Close the connection
			close();
		}
	}
	
	/**
	 * Process a given message that has just been received
	 * 
	 * @param msg Message to process
	 */
	private void processInputMessage ( BitTorrentMessage msg ) {
		assert msg != null;
		
		switch ( msg.getMessageType() ) {
			case HANDSHAKE_START:
				HandShakeStart hss = (HandShakeStart) msg;
				remoteFlags.or( hss.getFlags() );
				remoteHash = hss.getHash();
				remoteProtocol = hss.getProtocolName();
				remoteHandShakeStarted = true;
			break;
			
			case HANDSHAKE_END:
				HandShakeEnd hse = (HandShakeEnd) msg;
				remotePeerId = hse.getPeerId();
				remoteHandShakeFinished = true;
			break;
			
			case CHOKE:
				remoteChoking = true;
			break;
			
			case UNCHOKE:
				remoteChoking = false;
			break;
			
			case INTERESTED:
				remoteInterested = true;
			break;
			
			case UNINTERESTED:
				remoteInterested = false;
			break;
			
			case HAVE:
				remoteClaimedPieces.set( ((HaveMessage) msg).getPieceIndex() );
			break;
			
			case BITFIELD:
				remoteClaimedPieces.or( ((BitFieldMessage) msg).getBitField() );
			break;
		}
	}
	
	/**
	 * Process a given message that has just been sent
	 * 
	 * @param msg Message to process
	 */
	private void processOutputMessage ( BitTorrentMessage msg ) {
		assert msg != null;
		
		switch ( msg.getMessageType() ) {
			case HANDSHAKE_START:
				HandShakeStart hss = (HandShakeStart) msg;
				localFlags.or( hss.getFlags() );
				localHash = hss.getHash();
				localProtocol = hss.getProtocolName();
				localHandShakeStarted = true;
			break;
			
			case HANDSHAKE_END:
				HandShakeEnd hse = (HandShakeEnd) msg;
				localPeerId = hse.getPeerId();
				localHandShakeFinished = true;
			break;
			
			case CHOKE:
				localChoking = true;
			break;
			
			case UNCHOKE:
				localChoking = false;
			break;
			
			case INTERESTED:
				localInterested = true;
			break;
			
			case UNINTERESTED:
				localInterested = false;
			break;
			
			case HAVE:
				localClaimedPieces.set( ((HaveMessage) msg).getPieceIndex() );
			break;
			
			case BITFIELD:
				localClaimedPieces.or( ((BitFieldMessage) msg).getBitField() );
			break;
		}
	}

	/**
	 * Adds a message to the output queue, that will be sent the next time
	 * {@link process} is called.
	 * 
	 * @param msg Message to be sent
	 */
	public void sendMessage ( BitTorrentMessage msg ) {
		if ( msg == null ) {
			throw new NullPointerException();
		}
		
		outputQueue.add( msg );
	}
	
	/**
	 * Returns a message from the input queue. If no message is available, then
	 * <tt>null</tt> is returned.
	 * 
	 * @return A message from the input queue, or <tt>null</tt> if it's empty
	 */
	public BitTorrentMessage receiveMessage () {
		return inputQueue.poll();
	}

	public PeerId getLocalPeerId () {
		return localPeerId;
	}
	
	public PeerId getRemotePeerId () {
		return remotePeerId;
	}
	
	public String getLocalProtocol () {
		return localProtocol;
	}
	
	public String getRemoteProtocol () {
		return remoteProtocol;
	}
	
	public Sha1Hash getLocalHash () {
		return localHash;
	}
	
	public Sha1Hash getRemoteHash () {
		return remoteHash;
	}

	public BitSet getLocalFlags () {
		return localFlags;
	}
	
	public BitSet getRemoteFlags () {
		return remoteFlags;
	}

	public BitSet getLocalClaimedPieces () {
		return localClaimedPieces;
	}
	
	public BitSet getRemoteClaimedPieces () {
		return remoteClaimedPieces;
	}
	
	public boolean isLocalChoking () {
		return localChoking;
	}
	
	public boolean isRemoteChoking () {
		return remoteChoking;
	}
	
	public boolean isLocalInterested () {
		return localInterested;
	}
	
	public boolean isRemoteInterested () {
		return remoteInterested;
	}
	
	public boolean isLocalHandShakeStarted () {
		return localHandShakeStarted;
	}
	
	public boolean isRemoteHandShakeStarted () {
		return remoteHandShakeStarted;
	}
	
	public boolean isLocalHandShakeFinished () {
		return localHandShakeFinished;
	}
	
	public boolean isRemoteHandShakeFinished () {
		return remoteHandShakeFinished;
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
	 * Creates a new <tt>PeerConnection</tt> using an already connected socket
	 * channel.
	 * <p>
	 * The connection will use <tt>ibufSize</tt> and <tt>obufSize</tt> as the
	 * size for the input and output buffers. The actual size is
	 * <i>at least</i> the values given here, but this method ensures that a
	 * <i>Piece</i> message with a block of size <tt>ibuf</tt>/<tt>obuf</tt>
	 * can be respectively sent/received.
	 * 
	 * @param sock Channel to use for this connection
	 * @param ibufSize Size of the input buffer
	 * @param obufSize Size of the output buffer
	 * @return A new connection
	 * @throws IOException if some I/O error occurs
	 */
	public static PeerConnection newConnection (
		SocketChannel sock, int ibufSize, int obufSize
	) throws IOException {
		return new PeerConnection( sock, ibufSize, obufSize );
	}
	
	/**
	 * Creates a new <tt>PeerConnection</tt> using a new channel connected to
	 * the given address
	 * <p>
	 * The connection will use <tt>ibufSize</tt> and <tt>obufSize</tt> as the
	 * size for the input and output buffers. The actual size is
	 * <i>at least</i> the values given here, but this method ensures that a
	 * <i>Piece</i> message with a block of size <tt>ibuf</tt>/<tt>obuf</tt>
	 * can be respectively sent/received.
	 * 
	 * @param addr Address to connect the socket
	 * @param ibufSize Size of the input buffer
	 * @param obufSize Size of the output buffer
	 * @return A new connection
	 * @throws IOException if some I/O error occurs
	 */
	public static PeerConnection newConnection (
		SocketAddress addr, int ibufSize, int obufSize
	) throws IOException {
		return new PeerConnection( SocketChannel.open( addr ), ibufSize, obufSize );
	}
	
}
