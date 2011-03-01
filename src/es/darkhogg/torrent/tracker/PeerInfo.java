package es.darkhogg.torrent.tracker;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Stores information about a peer, such as its peer ID and its IP/port.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public class PeerInfo {
	
	/**
	 * Address and port of the peer
	 */
	private final InetSocketAddress address;
	
	/**
	 * Peer ID
	 */
	private final byte[] peerId;
	
	/**
	 * Construct an object using the given <tt>address</tt> and <tt>peerId</tt>
	 * <p>
	 * The <tt>peerId</tt> parameter may be <tt>null</tt> but if not, it must be
	 * exactly 20 bytes long.
	 * 
	 * @param address The IP and port of the peer
	 * @param peerId The peer ID, or <tt>null</tt> if not known
	 */
	public PeerInfo ( InetSocketAddress address, byte[] peerId ) {
		this.address = address;
		this.peerId = peerId==null
			? null : Arrays.copyOf( peerId, peerId.length );
	}
	
	/**
	 * Returns the IP and port of this peer as an <tt>InetSocketAddress</tt>
	 * object.
	 * 
	 * @return The IP and port of this peer
	 */
	public InetSocketAddress getAddress () {
		return address;
	}
	
	/**
	 * Returns the peer ID of this peer, as a 20-byte array. If the peer ID is
	 * unknown, <tt>null</tt> is returned instead.
	 * 
	 * @return The peer ID of this peer, or <tt>null</tt> if unknown
	 */
	public byte[] getPeerId () {
		return Arrays.copyOf( peerId, peerId.length );
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder(
			PeerInfo.class.getSimpleName() );
		
		sb.append( "{Address=" );
		sb.append( address );
		
		if ( peerId != null ) {
			sb.append( "; PeerID=\"" );
			sb.append( new String( peerId, Charset.forName( "ISO-8859-1" ) ) );
			sb.append( "\"" );
		}
		
		return sb.append( "}" ).toString();
	}
}
