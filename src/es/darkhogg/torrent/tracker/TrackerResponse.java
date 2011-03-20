package es.darkhogg.torrent.tracker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import es.darkhogg.torrent.bencode.Bencode;
import es.darkhogg.torrent.bencode.IntegerValue;
import es.darkhogg.torrent.bencode.ListValue;
import es.darkhogg.torrent.bencode.StringValue;
import es.darkhogg.torrent.bencode.Value;
import es.darkhogg.torrent.data.PeerId;

/**
 * Represents the response to a request to a {@link Tracker}. This class is
 * immutable.
 * <p>
 * Note that the only public way to create a <tt>TrackerRequest</tt> is using
 * a bencoded value. Objects of this class should not be created by client code,
 * but only returned by <tt>Tracker</tt>s when requested.
 * 
 * @author Daniel Escoz
 * @version 1.0
 * @see Tracker
 */
public final class TrackerResponse {
	
	/**
	 * Whether this request was failed
	 */
	private final boolean failed;
	
	/**
	 * Reason of the failing
	 */
	private final String failureReason;
	
	/**
	 * Warning issued by the tracker
	 */
	private final String warning;
	
	/**
	 * Time clients should wait between regular announces
	 */
	private final long interval;
	
	/**
	 * Minimum time clients must wait between announces
	 */
	private final long minInterval;
	
	/**
	 * Tracker ID returned by the tracker.
	 */
	private final byte[] trackerId;
	
	/**
	 * Number of seeders
	 */
	private final long complete;
	
	/**
	 * Number of leechers
	 */
	private final long incomplete;
	
	/**
	 * List of peers returned by the tracker
	 */
	private final List<PeerInfo> peers;
	
	/**
	 * Creates a new response with the given arguments.
	 * 
	 * @param failed Whether the request failed
	 * @param failureReason Failure reason
	 * @param warning Warning message
	 * @param interval Normal announce interval
	 * @param minInterval Minimum announce interval
	 * @param trackerId Tracker ID
	 * @param complete Number of seeders
	 * @param incomplete Number of leechers
	 * @param peers List of peers
	 */
	private TrackerResponse (
		boolean failed, String failureReason, String warning, long interval,
		long minInterval, byte[] trackerId, long complete, long incomplete,
		List<PeerInfo> peers
	) {
		this.failed = failed;
		
		if ( failed ) {
			if ( failureReason == null ) {
				throw new NullPointerException();
			}
			this.failureReason = failureReason;
			this.warning = ( warning == null ) ? "" : warning;
			this.interval = 0;
			this.minInterval = 0;
			this.trackerId = null;
			this.complete = 0;
			this.incomplete = 0;
			this.peers = Collections.emptyList();
		} else {
			if ( peers == null ) {
				throw new NullPointerException();
			}
			this.failureReason = "";
			this.warning = ( warning == null ) ? "" : warning;
			this.interval = interval;
			this.minInterval = minInterval;
			this.trackerId = trackerId == null
				? null : Arrays.copyOf( trackerId, trackerId.length );
			this.complete = complete;
			this.incomplete = incomplete;
			this.peers = Collections.unmodifiableList(
				new ArrayList<PeerInfo>( peers ) );
		}
	}
	
	/**
	 * Returns whther the reques failed. If this method returns <tt>true/<tt>,
	 * the {@link getFailureReason} method returns a human-readable message
	 * describing the error.
	 * 
	 * @return <tt>true</tt> if the request failed, <tt>false</tt> otherwise
	 */
	public boolean hasFailed () {
		return failed;
	}
	
	/**
	 * Returns a human-readable message describing the reason of this request
	 * failure, if any. If the tracker didn't send a failure message, this
	 * method returns <tt>""</tt>.
	 * 
	 * @return The failure message
	 */
	public String getFailureReason () {
		return failureReason;
	}
	
	/**
	 * Returns a human-readable warning message returned by the tracker. If the
	 * tracker didn't returned any warning, this method returns <tt>""</tt>.
	 * 
	 * @return The warning message
	 */
	public String getWarningMessage () {
		return warning;
	}
	
	/**
	 * Returns the number of seconds that the client should wait between regular
	 * announces. If the request failed, this method return 0.
	 * 
	 * @return The regular interval between announces
	 */
	public long getInterval () {
		return interval;
	}
	
	/**
	 * Returns the number of seconds that the client must wait between
	 * announces, regular or not. If the request failed, this method return 0.
	 * 
	 * @return The minimum interval between announces
	 */
	public long getMinInterval () {
		return minInterval;
	}
	
	/**
	 * Returns the tracker ID that must be sent in subsequent announces. If
	 * the request failed or the tracker didn't send this value, this method
	 * returns <tt>null</tt>. Otherwise, it returns a byte array.
	 * 
	 * @return The tracker ID of the request
	 */
	public byte[] getTrackerId () {
		return ( trackerId == null )
			? null : Arrays.copyOf( trackerId, trackerId.length );
	}
	
	/**
	 * Returns the number of peers available that has completed the torrent
	 * download. If the request failed, this method returns 0.
	 * 
	 * @return The number of seeders
	 */
	public long getComplete () {
		return complete;
	}
	
	/**
	 * Returns the number of peers available that has not completed the torrent
	 * download. If the request failed, this method returns 0.
	 * 
	 * @return The number of leechers
	 */
	public long getIncomplete () {
		return incomplete;
	}
	
	/**
	 * Returns the list of peers returned by the tracker. If the request
	 * failed, this method returns an empty list.
	 * 
	 * @return The list of peers
	 */
	public List<PeerInfo> getPeers () {
		return peers;
	}
	
	/**
	 * Returns a <tt>TrackerResponse</tt> constructed using the given bencode
	 * value.
	 * 
	 * @param value The bencoded dictionary the tracker sent
	 * @return An object representing the passed value
	 * @throws NullPointerException if <tt>value</tt> is <tt>null</tt>
	 * @throws IllegalArgumentException if <tt>value</tt> is not a valid
	 *         response
	 */
	public static TrackerResponse fromValue ( Value<?> value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		try {
			Value<?> failReasV = Bencode.getChildValue(
				value, "failure reason" );
			if ( failReasV == null ) {
				Value<?> warnV = Bencode.getChildValue( value,
					"warning message" );
				
				String warning = "";
				if ( warnV != null ) {
					warning = ( (StringValue) warnV ).getStringValue();
				}
				
				long interval = ( (IntegerValue) Bencode.getChildValue( value,
					"interval" ) ).getValue().longValue();
				
				Value<?> minIntV = Bencode.getChildValue( value,
					"min interval" );
				long minInterval = interval;
				if ( minIntV != null ) {
					minInterval = ( (IntegerValue) minIntV )
						.getValue().longValue();
				}
				
				Value<?> trIdV = Bencode.getChildValue( value, "tracker id" );
				byte[] trackerId = null;
				if ( trIdV != null ) {
					trackerId = ( (StringValue) trIdV ).getValue();
				}
				
				long complete = ( (IntegerValue) Bencode.getChildValue( value,
					"complete" ) ).getValue().longValue();
				
				long incomplete = ( (IntegerValue) Bencode.getChildValue( value,
					"incomplete" ) ).getValue().longValue();
				
				List<PeerInfo> peers = readPeers( Bencode.getChildValue(
					value, "peers" ) );
				
				return new TrackerResponse( false, "", warning, interval,
					minInterval, trackerId, complete, incomplete, peers );
			} else {
				String failureReason = ( (StringValue) failReasV ).getStringValue();
				return new TrackerResponse( true, failureReason,
					"", 0, 0, null, 0, 0, null );
			}
		} catch ( Exception e ) {
			throw new IllegalArgumentException( e );
		}
	}
	
	/**
	 * Returns the {@link PeerInfo} list represented by the given value.
	 * 
	 * @param value Bencoded value containing a peer list
	 * @return The peer list contained in the passed value
	 */
	private static List<PeerInfo> readPeers ( Value<?> value ) {
		if ( value instanceof StringValue ) {
			try {
				byte[] str = ( (StringValue) value ).getValue();
				
				if ( ( str.length % 6 ) != 0 ) {
					throw new IllegalArgumentException();
				}
				
				DataInputStream is = new DataInputStream(
					new ByteArrayInputStream( str ) );
				byte[] ipbuf = new byte[ 4 ];
				
				List<PeerInfo> peers = new ArrayList<PeerInfo>();
				
				while ( is.available() > 0 ) {
					is.readFully( ipbuf );
					int port = is.readChar();
					
					InetSocketAddress addr = new InetSocketAddress(
						InetAddress.getByAddress( ipbuf ), port );
					PeerInfo pi = new PeerInfo( addr, null );
					
					peers.add( pi );
				}
				
				return peers;
			} catch ( IOException e ) {
				// Should NEVER happen
				throw new IllegalArgumentException( e );
			}
		} else if ( value instanceof ListValue ) {
			ListValue list = (ListValue) value;
			
			List<PeerInfo> peers = new ArrayList<PeerInfo>();
			
			for ( Value<?> v : list.getValue() ) {
				Value<?> pIdV = Bencode.getChildValue( v, "peer id" );
				byte[] peerId = null;
				if ( pIdV != null ) {
					peerId = ( (StringValue) pIdV ).getValue();
				}
				
				String host = ( (StringValue) Bencode.getChildValue(
					v, "ip" ) ).getStringValue();
				
				int port = ( (IntegerValue) Bencode.getChildValue(
					v, "port" ) ).getValue().intValue();
				
				try {
					InetAddress addr = InetAddress.getByName( host );
					peers.add( new PeerInfo( new InetSocketAddress(
						addr, port ), new PeerId( peerId ) ) );
				} catch ( UnknownHostException e ) {
					// Don't add this peer
				}
			}
			
			return peers;
		} else {
			throw new ClassCastException();
		}
	}

	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder(
			TrackerResponse.class.getSimpleName() );
		
		sb.append( "{" );
		
		if ( failed ) {
			sb.append( "FailureReason=\"" );
			sb.append( failureReason );
			sb.append( "\"" );
		} else {
			sb.append( "Warning=\"" );
			sb.append( warning );
			
			sb.append( "\"; Interval=" );
			sb.append( interval );
			
			sb.append( "; MinInterval=" );
			sb.append( minInterval );
			
			sb.append( "; TrackerId=[" );
			sb.append( ( trackerId == null ) ? null : new String( trackerId,
				Charset.forName( "ISO-8859-1" ) ) );
			
			sb.append( "]; Complete=" );
			sb.append( complete );
			
			sb.append( "; Incomplete=" );
			sb.append( incomplete );
			
			sb.append( "; Peers=" );
			sb.append( peers );
		}
		
		return sb.append( "}" ).toString();
	}
}
