package es.darkhogg.torrent.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A class that represents a Peer ID, that is, a 20-bytes array that identifies
 * a peer.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class PeerID {
	
	/**
	 * LATIN-1 charset
	 */
	private static final Charset ISO_8859_1 = Charset.forName( "ISO-8859-1" );
	
	/**
	 * Peer ID as an array of bytes
	 */
	private final byte[] bytes;
	
	/**
	 * Peer ID as a String
	 */
	private final String string;
	
	/**
	 * Cached has-code
	 */
	private final int hash;
	
	/**
	 * Cached URL-encoded string
	 */
	private final String urlEncodedString;
	
	/**
	 * Constructs a peer ID using an array of bytes
	 * 
	 * @param bytes Peer ID
	 * @throws NullPointerException if <tt>bytes</tt> is <tt>null</tt>
	 * @throws IllegalArgumentException if <tt>bytes</tt> has a different
	 *         length than 20
	 */
	public PeerID ( byte[] bytes ) {
		if ( bytes == null ) {
			throw new NullPointerException();
		}
		
		if ( bytes.length != 20 ) {
			throw new IllegalArgumentException();
		}
		
		this.bytes = Arrays.copyOf( bytes, bytes.length );
		this.string = new String( bytes, ISO_8859_1 );
		this.hash = Arrays.hashCode( bytes );
		
		// URL Encoded String
		try {
			urlEncodedString = URLEncoder.encode( string, "ISO-8859-1" );
		} catch ( UnsupportedEncodingException e ) {
			// Should not happen, as LATIN-1 is always supported...
			throw new AssertionError();
		}
	}
	
	/**
	 * Constructs a peer ID using a string.
	 * 
	 * @param bytes Peer ID
	 * @throws NullPointerException if <tt>string</tt> is <tt>null</tt>
	 * @throws IllegalArgumentException if <tt>string</tt> has a different
	 *         length than 20
	 */
	public PeerID ( String string ) {
		if ( string == null ) {
			throw new NullPointerException();
		}
		
		if ( string.length() != 20 ) {
			throw new IllegalArgumentException();
		}
		
		this.bytes = string.getBytes( ISO_8859_1 );
		this.string = string;
		this.hash = Arrays.hashCode( bytes );
		
		// URL Encoded String
		try {
			urlEncodedString = URLEncoder.encode( string, "ISO-8859-1" );
		} catch ( UnsupportedEncodingException e ) {
			// Should not happen, as LATIN-1 is always supported...
			throw new AssertionError();
		}
	}
	
	/**
	 * Returns this peer ID as an array of bytes.
	 * 
	 * @return A 20-bytes array
	 */
	public byte[] getBytes () {
		return Arrays.copyOf( bytes, bytes.length );
	}
	
	/**
	 * Compares this peer ID to another one for equality.
	 * <p>
	 * A <tt>PeerID</tt> is equal only to another <tt>PeerID</tt> object
	 * that represents the same peer ID as this object.
	 */
	@Override
	public boolean equals ( Object obj ) {
		if ( !(obj instanceof PeerID) ) {
			return false;
		}
		
		PeerID p = (PeerID) obj;
		return Arrays.equals( p.bytes, bytes );
	}
	
	@Override
	public int hashCode () {
		return hash;
	}
	
	/**
	 * Returns the peer ID as a string object
	 */
	@Override
	public String toString () {
		return string;
	}
	
	/**
	 * Returns the same string as {@link toString}, but URL-encoded.
	 * 
	 * @return This peer ID as an URL-encoded string
	 */
	public String toUrlEncodedString () {
		return urlEncodedString;
	}
}
