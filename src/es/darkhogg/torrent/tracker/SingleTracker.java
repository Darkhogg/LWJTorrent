package es.darkhogg.torrent.tracker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import es.darkhogg.bencode.BencodeInputStream;

/**
 * A tracker that sends announces to a single URL.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
final class SingleTracker extends Tracker {

	/**
	 * URL to send announces
	 */
	private final URL url;
	
	/**
	 * Construct a tracker with a given <tt>URL</tt>
	 * @param url URL to send announces
	 */
	public SingleTracker ( URL url ) {
		this.url = url;
	}
	
	@Override
	public TrackerResponse sendRequest ( TrackerRequest request ) {
		try {
			URL requestUrl = getRequestUrl( url.toString(), request );
			
			BencodeInputStream bis = new BencodeInputStream( requestUrl.openStream() );
			return TrackerResponse.fromValue( bis.readValue() );
			
		} catch ( MalformedURLException e ) {
			return null;
		} catch ( IOException e ) {
			return null;
		}
	}

}
