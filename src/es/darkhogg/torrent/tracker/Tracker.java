package es.darkhogg.torrent.tracker;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import es.darkhogg.torrent.data.TorrentMetaInfo;

/**
 * Represents an HTTP service capable of handling announce requests, commonly
 * known as a <i>tracker</i>.
 * 
 * @author Daniel Escoz
 * @version 1.0
 * @see #forTorrent
 */
public abstract class Tracker {
	
	/**
	 * Default package-private constructor, to avoid external inheritance
	 */
	/* package */Tracker () {
		// Do nothing
	}
	
	/**
	 * Sends a request to this tracker and returns its response.
	 * <p>
	 * If the HTTP request fails, this method must return null, rather than
	 * throw an exception.
	 * 
	 * @param request
	 *            Request to send to this tracker
	 * @return The response of this tracker, or <tt>null</tt> if an error occurs
	 */
	public abstract TrackerResponse sendRequest ( TrackerRequest request );
	
	/**
	 * Constructs the query string of the announce request.
	 * 
	 * @param req
	 *            Request to build the string from
	 * @return The query string to send to the tracker
	 */
	protected static String getUrlParams ( TrackerRequest req ) {
		try {
			StringBuilder sb = new StringBuilder( "?" );
			
			sb.append( "info_hash=" );
			sb.append( req.getInfoHash().toUrlEncodedString() );
			
			sb.append( "&peer_id=" );
			sb.append( req.getPeerId().toUrlEncodedString() );
			
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
			
			sb.append( "&key=" );
			sb.append( req.getKey() );
			
			if ( req.getTrackerId() != null ) {
				sb.append( "&trackerid=" );
				String trstr =
					new String( req.getTrackerId(),
						Charset.forName( "ISO-8859-1" ) );
				sb.append( URLEncoder.encode( trstr, "ISO-8859-1" ) );
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
	 * @param announce
	 *            announce URL of the tracker
	 * @param req
	 *            Request to build the URL from
	 * @return The full URL
	 * @throws MalformedURLException
	 *             should never happen
	 */
	protected static String
		getRequestUrl ( String announce, TrackerRequest req )
	{
		return announce + getUrlParams( req );
	}
	
	/**
	 * Returns a <tt>Tracker</tt> object that send its requests to the given
	 * announce URL.
	 * 
	 * @param announce
	 *            The URL of the announce service
	 * @return A tracker which announces to the given URL
	 * @throws URISyntaxException
	 *             if the announce is not a valid URI
	 * @throws MalformedURLException
	 *             if the announce is not a valid URL
	 */
	protected static Tracker getSingleTracker ( String announce )
		throws URISyntaxException, MalformedURLException
	{
		URI uri = new URI( announce );
		
		if ( uri.getScheme().equals( "udp" ) ) {
			return new UdpTracker( uri );
		} else {
			// Create URL tracker
			return new UrlTracker( uri.toURL() );
		}
	}
	
	/**
	 * Returns a <tt>Tracker</tt> object that send its request to the given
	 * announce URLs, in order, until one of them is successfull.
	 * <p>
	 * The passed object is treated as the <tt>announce-list</tt> key of a
	 * torrent, and behaves like it.
	 * 
	 * @param announces
	 *            List of announce URLs
	 * @return A tracker which announces to the given URL list
	 */
	/* package */static Tracker getMultiTracker ( List<Set<String>> announces )
	{
		List<Tracker> trackers = new ArrayList<Tracker>();
		
		for ( Set<String> set : announces ) {
			for ( String str : set ) {
				try {
					Tracker tracker = getSingleTracker( str );
					trackers.add( tracker );
				} catch ( MalformedURLException e ) {
					// Just don't add it
				} catch ( URISyntaxException e ) {
					// Again, just don't add it
				}
			}
		}
		
		return new MultiTracker( trackers );
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
	 * @param torrent
	 *            Torrent meta-info used to build the tracker
	 * @return A tracker that announces to the corresponding URL
	 * @throws MalformedURLException
	 *             if the URL or URLs used are not valid
	 * @throws URISyntaxException
	 *             if the URI or URIs used are not valid
	 */
	public static Tracker forTorrent ( TorrentMetaInfo torrent )
		throws MalformedURLException, URISyntaxException
	{
		if ( torrent.getAnnounceList().isEmpty() ) {
			return getSingleTracker( torrent.getAnnounce() );
		} else {
			return getMultiTracker( torrent.getAnnounceList() );
		}
	}
}
