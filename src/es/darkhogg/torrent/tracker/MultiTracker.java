package es.darkhogg.torrent.tracker;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
	private final LinkedList<Tracker> trackers;
	
	/**
	 * Constructs the tracker from a given list of sets of announce strings
	 * 
	 * @param announces
	 *            List of sets of announce strings
	 */
	public MultiTracker ( List<Tracker> announces ) {
		trackers = new LinkedList<Tracker>( announces );
	}
	
	@Override
	public TrackerResponse sendRequest ( TrackerRequest request, long time,
		TimeUnit unit )
	{
		long remaining = unit.toNanos( time );
		int left = trackers.size();
		
		for ( Iterator<Tracker> it = trackers.iterator(); it.hasNext()
			&& remaining > 0; )
		{
			long stTime = System.nanoTime();
			Tracker tracker = it.next();
			TrackerResponse resp =
				tracker.sendRequest( request, remaining / left,
					TimeUnit.NANOSECONDS );
			long ndTime = System.nanoTime();
			
			remaining -= ( ndTime - stTime );
			left--;
			
			if ( resp != null ) {
				// WARNING :: ConcurrentModificationException !
				// This will NEVER throw such an exception, as the loop ENDS
				// here. I wanted to clarify this, so no one ever, not even
				// myself, removes it for that reason.
				it.remove();
				trackers.addFirst( tracker );
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
