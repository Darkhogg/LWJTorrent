package es.darkhogg.torrent.tracker;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import es.darkhogg.torrent.data.TorrentMetaInfo;

/**
 * Represents an HTTP service capable of handling announce requests, commonly
 * known as a <i>tracker</i>.
 * 
 * @author Daniel Escoz
 * @version 1.0
 * @see Tracker#getTrackerForTorrent
 */
public abstract class Tracker {
	
	/**
	 * Default package-private constructor, to avoid external inheritance
	 */
	Tracker () {
		// Do nothing
	}
	
	/**
	 * Sends a request to this tracker and returns its response.
	 * <p>
	 * If the HTTP request fails, this method must return null, rather than
	 * throw an exception.
	 * 
	 * @param request Request to send to this tracker
	 * @return The response of this tracker, or <tt>null</tt> if an error
	 *         occurs
	 */
	public abstract TrackerResponse sendRequest ( TrackerRequest request );
	
	/**
	 * Constructs the query string of the announce request.
	 * 
	 * @param req Request to build the string from
	 * @return The query string to send to the tracker
	 */
	protected static String getUrlParams ( TrackerRequest req ) {
		try {
			StringBuilder sb = new StringBuilder( "?" );
			
			sb.append( "info_hash=" );
			sb.append( req.getInfoHash().toUrlEncodedString() );
			
			sb.append( "&peer_id=" );
			String peerstr = new String( req.getPeerId(),
				Charset.forName( "ISO-8859-1" ) );
			sb.append( URLEncoder.encode( peerstr, "UTF-8" ) );
			
			sb.append( "&port=" );
			sb.append( req.getPort() );
	
			sb.append( "&uploaded=" );
			sb.append( req.getBytesUploaded() );
			
			sb.append( "&downloaded=" );
			sb.append( req.getBytesDownloaded() );
			
			sb.append( "&left=" );
			sb.append( req.getBytesLeft() );
			
			if ( req.isCompact() != null ) {
				sb.append( "&compact=" );
				sb.append( req.isCompact() ? "1" : "0" );
			}
			
			sb.append( "&no_peer_id=" );
			sb.append( req.getWantPeerId() ? "0" : "1" );
			
			sb.append( "&event=" );
			sb.append( req.getEvent().getEventString() );
			
			if ( req.getIp() != null ) {
				sb.append( "&ip=" );
				sb.append( req.getIp().getHostAddress() );
			}
			
			sb.append( "&numwant=" );
			sb.append( req.getNumWant() );
	
			if ( req.getKey() != null ) {
				sb.append( "&key=" );
				sb.append( req.getKey() );
			}
			
			if ( req.getKey() != null ) {
				sb.append( "&trackerid=" );
				String trstr = new String( req.getTrackerId(),
					Charset.forName( "ISO-8859-1" ) );
				sb.append( URLEncoder.encode( trstr, "UTF-8" ) );
			}
			
			return sb.toString();
		} catch ( UnsupportedEncodingException e ) {
			// Should never happen!
			return null;
		}
	}
	
	/**
	 * Creates the URL used to make the announce to the tracker.
	 * 
	 * @param announce announce URL of the tracker
	 * @param req Request to build the URL from
	 * @return The full URL
	 * @throws MalformedURLException should never happen
	 */
	protected static URL getRequestUrl (
		String announce, TrackerRequest req
	) throws MalformedURLException {
		return new URL( announce + getUrlParams( req ) );
	}
	
	/**
	 * Returns a <tt>Tracker</tt> object that send its requests to the given
	 * announce URL.
	 * 
	 * @param announce The URL of the announce service
	 * @return A tracker which announces to the given URL
	 */
	private static Tracker getSingleTracker ( String announce )
	throws MalformedURLException {
		return new SingleTracker( new URL( announce ) );
	}
	
	/**
	 * Returns a <tt>Tracker</tt> object that send its request to the given
	 * announce URLs, in order, until one of them is successfull.
	 * <p>
	 * The passed object is treated as the <tt>announce-list</tt> key of a 
	 * torrent, and behaves like it.
	 * <p>
	 * <i><b>Note:</b> This mehtod is currently unimplemented and returns
	 * <tt>null</tt>. A <tt>Tacker</tt> implementation that matches the
	 * requirements of this method is needed.</i>
	 * 
	 * @param announces List of announce URLs
	 * @return A tracker which announces to the given URL list
	 */
	@SuppressWarnings( "unused" )
	private static Tracker getMultiTracker ( List<Set<String>> announces ) {
		return null;
	}
	
	/**
	 * Returns a tracker that sends its request to the specified URLs in the
	 * torrent.
	 * <p>
	 * Whether the returned object uses the <tt>announce</tt> or the
	 * <tt>announce-list</tt> values depends on the object passed and the
	 * implementation availability. In general, the best possible method is
	 * chosen for each torrent.
	 * 
	 * @param torrent Torrent meta-info used to build the tracker
	 * @return A tracker that announces to the corresponding URL
	 * @throws MalformedURLException if the URL or URLs used are not valid
	 */
	public static Tracker forTorrent ( TorrentMetaInfo torrent )
	throws MalformedURLException {
		return getSingleTracker( torrent.getAnnounce() );
	}
}
