package es.darkhogg.torrent.tracker;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
	private final List<Tracker> trackers;
	
	/**
	 * Constructs the tracker from a given list of sets of announce strings
	 * 
	 * @param announces
	 *            List of sets of announce strings
	 */
	public MultiTracker ( List<Tracker> announces ) {
		trackers =
			Collections.unmodifiableList( new LinkedList<Tracker>( announces ) );
	}
	
	@Override
	public TrackerResponse sendRequest ( TrackerRequest request ) {
		for ( Tracker tracker : trackers ) {
			TrackerResponse resp = tracker.sendRequest( request );
			
			if ( resp != null ) {
				return resp;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString () {
		return "Multi-Tracker(" + trackers + ")";
	}
}
