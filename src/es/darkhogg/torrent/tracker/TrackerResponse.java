package es.darkhogg.torrent.tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import es.darkhogg.bencode.Value;

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
			this.failureReason = "";
			this.warning = ( warning == null ) ? "" : warning;
			this.interval = interval;
			this.minInterval = minInterval;
			this.trackerId = Arrays.copyOf( trackerId, trackerId.length );
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
	 * returns <tt>null</tt>. Otherwise, it returns a 20-byte array.
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
			// TODO Implement this
			
			return null;
		} catch ( Exception e ) {
			throw new IllegalArgumentException( e );
		}
	}
}
