package es.darkhogg.torrent.tracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * A tracker that sends its requests using the UDP Tracker protocol.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
/* package */final class UdpTracker extends Tracker {
	
	/**
	 * Empty byte array for the datagram packets
	 */
	private static final byte[] EMPTY_BYTE_ARR = new byte[ 0 ];
	
	/**
	 * Connection ID
	 */
	private static final long CONNECTION_ID = 0x41727101980L;
	
	/**
	 * RandomObject
	 */
	private static final Random RANDOM = new Random( System.nanoTime() );
	
	/**
	 * URI which specifies where to find the tracker
	 */
	private final URI uri;
	
	/**
	 * Constructs a tracker using a given URI
	 * 
	 * @param uri
	 *            The URI for this tracker
	 */
	public UdpTracker ( URI uri ) {
		this.uri = uri;
	}
	
	@Override
	public TrackerResponse sendRequest ( TrackerRequest request, long time,
		TimeUnit unit )
	{
		String host = uri.getHost();
		int port = uri.getPort();
		
		DatagramSocket socket = null;
		try {
			InetAddress addr = InetAddress.getByName( host );
			
			// UDP Socket - already bound
			socket = new DatagramSocket();
			socket.setSoTimeout( (int) ( unit.toMillis( time ) / 2 ) );
			
			// UDP Datagram - setup of address/port
			DatagramPacket packet = new DatagramPacket( EMPTY_BYTE_ARR, 0 );
			packet.setAddress( addr );
			packet.setPort( port );
			
			// Byte Array Output/Input Streams for writing/reading the packets
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dataOut = new DataOutputStream( baos );
			DataInputStream dataIn = null;
			
			// Random Transaction ID
			int action;
			int recvTransactionId;
			long connectionId = CONNECTION_ID;
			int transactionId = RANDOM.nextInt();
			
			// STEP 1: Send the connection message
			baos.reset();
			dataOut.writeLong( connectionId );
			dataOut.writeInt( 0 );
			dataOut.writeInt( transactionId );
			packet.setData( baos.toByteArray(), 0, 16 );
			socket.send( packet );
			
			// STEP 2: Receive the connection message
			socket.receive( packet );
			dataIn =
				new DataInputStream(
					new ByteArrayInputStream( packet.getData() ) );
			action = dataIn.readInt();
			recvTransactionId = dataIn.readInt();
			connectionId = dataIn.readLong();
			if ( action != 0 || recvTransactionId != transactionId ) {
				return null;
			}
			
			// STEP 3: Send the announce message
			baos.reset();
			transactionId = RANDOM.nextInt();
			dataOut.writeLong( connectionId );
			dataOut.writeInt( 1 );
			dataOut.writeInt( transactionId );
			dataOut.write( request.getInfoHash().getBytes() );
			dataOut.write( request.getPeerId().getBytes() );
			dataOut.writeLong( request.getBytesDownloaded() );
			dataOut.writeLong( request.getBytesLeft() );
			dataOut.writeLong( request.getBytesUploaded() );
			dataOut.writeLong( request.getEvent().getEventInt() );
			if ( request.getIp() != null ) {
				dataOut.writeInt( -1 );
			} else {
				byte[] ipbuf = request.getIp().getAddress();
				if ( ipbuf.length != 4 ) {
					// IPv6 - Not supported for some reason
					dataOut.writeInt( -1 );
				} else {
					dataOut.write( ipbuf, 0, 4 );
				}
			}
			dataOut.writeInt( request.getKey() );
			dataOut.writeInt( request.getNumWant() );
			dataOut.writeShort( (short) request.getPort() );
			packet.setData( baos.toByteArray(), 0, 98 );
			socket.send( packet );
			
			// STEP 4: "Parse" the response
			packet.setData( new byte[ 20 + 6 * request.getNumWant() ] );
			socket.receive( packet );
			dataIn =
				new DataInputStream(
					new ByteArrayInputStream( packet.getData() ) );
			action = dataIn.readInt();
			recvTransactionId = dataIn.readInt();
			int interval = dataIn.readInt();
			int leechers = dataIn.readInt();
			int seeders = dataIn.readInt();
			
			int numPeersRecv = dataIn.available() / 6;
			byte[] addrBuf = new byte[ 4 ];
			List<PeerInfo> peers = new ArrayList<PeerInfo>();
			for ( int i = 0; i < numPeersRecv; i++ ) {
				int read = dataIn.read( addrBuf, 0, 4 );
				if ( read != 4 ) {
					return null;
				}
				short recvPort = dataIn.readShort();
				PeerInfo peer =
					new PeerInfo( new InetSocketAddress(
						InetAddress.getByAddress( addrBuf ), recvPort ), null );
				peers.add( peer );
			}
			
			return new TrackerResponse( false, null, "", interval, interval,
				EMPTY_BYTE_ARR, seeders, leechers, peers );
		} catch ( IOException e ) {
			return null;
		} finally {
			if ( socket != null ) {
				socket.close();
			}
		}
	}
	
	@Override
	public String toString () {
		return "UDP-Tracker(" + uri + ")";
	}
}
