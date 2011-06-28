package es.darkhogg.torrent.tracker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import es.darkhogg.torrent.bencode.BencodeInputStream;

/**
 * A tracker that sends announces to different URLs until one of them responds.
 * In theory, this class should behave just like is specified for the
 * <tt>announce-list</tt> torrent field. In practice, there are most important
 * this to program right now, so it just send requests until one is answered.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
/* package */final class MultiTracker extends Tracker {
	
	/**
	 * List of URL's of the tracker
	 */
	private final List<String> urls;
	
	/**
	 * Constructs the tracker from a given list of sets of announce strings
	 * 
	 * @param announces
	 *            List of sets of announce strings
	 */
	public MultiTracker ( List<Set<String>> announces ) {
		urls = new LinkedList<String>();
		
		for ( Set<String> set : announces ) {
			urls.addAll( set );
		}
	}
	
	@Override
	public TrackerResponse sendRequest ( TrackerRequest request ) {
		
		for ( String str : urls ) {
			try {
				URL requestUrl = getRequestUrl( str, request );
				
				BencodeInputStream bis =
					new BencodeInputStream( requestUrl.openStream() );
				return TrackerResponse.fromValue( bis.readValue() );
				
			} catch ( MalformedURLException e ) {
				// Nothing
			} catch ( IOException e ) {
				// Nothing
			}
		}
		
		return null;
	}
	
}
