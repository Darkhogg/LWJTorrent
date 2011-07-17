package es.darkhogg.torrent.tracker;

import java.net.URI;

/**
 * A tracker that sends its requests using the UDP Tracker protocol.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
/* package */final class UdpTracker extends Tracker {
	
	/**
	 * URI which specifies where to find the tracker
	 */
	private final URI uri;
	
	/**
	 * Constructs a tracker using a given URI
	 * 
	 * @param uri
	 *            The URI for this tracker
	 */
	public UdpTracker ( URI uri ) {
		this.uri = uri;
	}
	
	@Override
	public TrackerResponse sendRequest ( TrackerRequest request ) {
		// TODO Implement this thing
		return null;
	}
	
}
