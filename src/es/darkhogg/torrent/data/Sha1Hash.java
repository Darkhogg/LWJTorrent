package es.darkhogg.torrent.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import es.darkhogg.bencode.BencodeOutputStream;
import es.darkhogg.bencode.Value;
import es.darkhogg.util.VoidOutputStream;

/**
 * A class that represents a SHA-1 hash. This class is just an immutable
 * wrapper for a 20-byte byte array with some utility methods to make hashes
 * from common sources.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class Sha1Hash {
	
	/**
	 * Stores the hash
	 */
	private final byte[] bytes;
	
	/**
	 * Hash Code of this object, used in <tt>hashCode</tt>
	 */
	private final int hash;
	
	/**
	 * Hexadecimal <tt>String</tt> representation of this object
	 */
	private final String string;
	
	/**
	 * URL-Encoded <tt>String</tt> representation of this object
	 */
	private final String urlEncodedString;
	
	/**
	 * Constructs a hash with the given byte array. The array passed must be of
	 * length 20. If not, an <tt>IllegalArgumentException</tt> is thrown.
	 * <p>
	 * No references to the passed array are maintained by this object.
	 * 
	 * @param bytes 20-byte SHA-1 hash this object represents.
	 * @throws NullPointerException if <tt>bytes/<tt> is <tt>null</tt>
	 * @throws IllegalArgumentException if <tt>bytes</tt> is not of length 20
	 */
	public Sha1Hash ( byte[] bytes ) {
		if ( bytes == null ) {
			throw new NullPointerException();
		}
		
		if ( bytes.length != 20 ) {
			throw new IllegalArgumentException();
		}
		
		this.bytes = Arrays.copyOf( bytes, bytes.length );
		
		hash = Arrays.hashCode( bytes );
		
		// Hex String
		StringBuilder sb = new StringBuilder();
		for ( byte b : bytes ) {
			int ub = ((int)b) & 0xFF;
			
			if ( ub < 16 ) {
				sb.append( '0' );
			}
			sb.append( Integer.toHexString( ub ).toUpperCase() );
		}
		string = sb.toString();
		
		// URL Encoded String
		String str = new String( bytes, Charset.forName( "ISO-8859-1" ) );
		try {
			urlEncodedString = URLEncoder.encode( str, "UTF-8" );
		} catch ( UnsupportedEncodingException e ) {
			// Should not happen, as UTF-8 is always supported...
			throw new AssertionError();
		}
	}
	
	/**
	 * Returns the hash represented by this object as a newly created byte
	 * array containing the 20 bytes of the hash.
	 * 
	 * @return The 20-byte hash represented by this object
	 */
	public byte[] getBytes () {
		return Arrays.copyOf( bytes, bytes.length );
	}
	
	/**
	 * Compares this hash to another one for equality.
	 * <p>
	 * A <tt>Sha1Hash</tt> is equal only to another <tt>Sha1Hash</tt> object
	 * that represents the same hash as this object.
	 */
	@Override
	public boolean equals ( Object obj ) {
		if ( !(obj instanceof Sha1Hash) ) {
			return false;
		}
		
		Sha1Hash h = (Sha1Hash) obj;
		return Arrays.equals( h.bytes, bytes );
	}
	
	@Override
	public int hashCode () {
		return hash;
	}
	
	/**
	 * Returns a 40-character uppercase <tt>String</tt> representing the
	 * hexadecimal value of the hash represented by this object.
	 */
	@Override
	public String toString () {
		return string;
	}
	
	public String toUrlEncodedString () {
		return urlEncodedString;
	}
	
	/**
	 * Returns a <tt>Sha1Hash</tt> object representing the SHA-1 hash of the
	 * given bencode <tt>Value</tt>. The passed value is re-encoded, but only
	 * at most 64 bytes are buffered at any given time to compute the SHA-1
	 * hash.
	 * <p>
	 * If the encoding fails for some reason or the SHA-1 algorithm is not
	 * available in this JVM, this method returns <tt>null</tt>.
	 * 
	 * @param value The value to hash
	 * @return The hash of the <tt>value</tt> argument, or <tt>null</tt> if it
	 *         cannot be calculated for some reason.
	 */
	public static Sha1Hash forValue ( Value<?> value ) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance( "SHA1" );
		} catch ( NoSuchAlgorithmException e ) {
			// If the SHA1 algorithm doesn't exist, returns null
			return null;
		}
		
		BencodeOutputStream bout = new BencodeOutputStream(
			new DigestOutputStream( new VoidOutputStream(), md ) );
		
		try {
			bout.writeValue( value );
			bout.close();
		} catch ( IOException e ) {
			// This should never happen
			// In case it does, this method returns null
			return null;
		} finally {
			try {
				bout.close();
			} catch ( IOException e ) {
				// Nothing to do
			}
		}
		
		return new Sha1Hash( md.digest() );
	}
	
	/**
	 * Returns a <tt>Sha1Hash</tt> object representing the SHA-1 hash of the
	 * given file.
	 * <p>
	 * If the SHA-1 algorithm is not available in this JVM, this method returns
	 * <tt>null</tt>.
	 * 
	 * @param file The file which hash is to be calculated
	 * @return The hash of the given file, or <tt>null</tt> if it cannot be
	 *         calculated for some reason
	 * @throws IOException if the underlying file IO operations fail
	 */
	public static Sha1Hash forFile ( File file )
	throws IOException {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance( "SHA1" );
		} catch ( NoSuchAlgorithmException e ) {
			// If the SHA1 algorithm doesn't exist, returns null
			return null;
		}
		
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream( file );
			byte[] buffer = new byte[ 65536 ];
			while ( fis.available() > 0 ) {
				int num = fis.read( buffer );
				md.update( buffer, 0, num );
			}
		} finally {
			if ( fis != null ) fis.close();
		}
		
		return new Sha1Hash( md.digest() );
	}
}
