package es.darkhogg.torrent.tracker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import es.darkhogg.torrent.bencode.BencodeInputStream;

/**
 * A tracker that sends announces to a single URL.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
/* package */final class UrlTracker extends Tracker {
	
	/**
	 * URL to send announces
	 */
	private final URL url;
	
	/**
	 * Construct a tracker with a given <tt>URL</tt>
	 * 
	 * @param url
	 *            URL to send announces
	 */
	public UrlTracker ( URL url ) {
		this.url = url;
	}
	
	@Override
	public TrackerResponse sendRequest ( TrackerRequest request ) {
		try {
			URL reqUrl = new URL( getRequestUrl( url.toString(), request ) );
			
			BencodeInputStream bis =
				new BencodeInputStream( reqUrl.openStream() );
			return TrackerResponse.fromValue( bis.readValue() );
			
		} catch ( MalformedURLException e ) {
			return null;
		} catch ( IOException e ) {
			return null;
		}
	}
	
	@Override
	public String toString () {
		return "URL-Tracker(" + url + ")";
	}
}
