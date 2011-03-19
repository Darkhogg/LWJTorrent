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
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class PeerConnection implements Closeable {
	
	private final SocketChannel channel;
	
	private PeerId localPeerId = null;
	private PeerId remotePeerId = null;
	
	private String localProtocol = null;
	private String remoteProtocol = null;
	
	private Sha1Hash localHash = null;
	private Sha1Hash remoteHash = null;

	private final BitSet localFlags = new BitSet();
	private final BitSet remoteFlags = new BitSet();

	private final BitSet localClaimedPieces = new BitSet();
	private final BitSet remoteClaimedPieces = new BitSet();
	
	private boolean localChoking = true;
	private boolean remoteChoking = true;
	
	private boolean localInterested = false;
	private boolean remoteInterested = false;

	private boolean localHandShakeStarted = false;
	private boolean localHandShakeFinished = false;
	private boolean remoteHandShakeStarted = false;
	private boolean remoteHandShakeFinished = false;
	
	private final ByteBuffer inputBuffer;
	private final ByteBuffer outputBuffer;

	private final Queue<BitTorrentMessage> inputQueue =
		new LinkedList<BitTorrentMessage>();
	private final Queue<BitTorrentMessage> outputQueue =
		new LinkedList<BitTorrentMessage>();
	
	private int inputState = 0;
	private int nextMessageLength = 0;
	
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

	public void sendMessage ( BitTorrentMessage msg ) {
		if ( msg == null ) {
			throw new NullPointerException();
		}
		
		outputQueue.add( msg );
	}
	
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
	
	@Override
	public void close () {
		try {
			channel.close();
		} catch ( IOException e ) {
			// Do nothing
		}
	}
	
	public static PeerConnection newConnection (
		SocketChannel sock, int ibufSize, int obufSize
	) throws IOException {
		return new PeerConnection( sock, ibufSize, obufSize );
	}
	
	public static PeerConnection newConnection (
		SocketAddress addr, int ibufSize, int obufSize
	) throws IOException {
		return new PeerConnection( SocketChannel.open( addr ), ibufSize, obufSize );
	}
	
}
