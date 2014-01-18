package es.darkhogg.torrent.tracker;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import es.darkhogg.torrent.data.TorrentMetaInfo;

/**
 * Represents an service capable of handling announce requests, commonly known as a <i>tracker</i>.
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
     * This method delegates its calls to {@link #sendRequest(TrackerRequest, long, TimeUnit)}, passing it a value that
     * corresponds to a minute.
     * 
     * @param request Request to send to this tracker
     * @return The response of this tracker, or <tt>null</tt> if an error occurs
     */
    public final TrackerResponse sendRequest (TrackerRequest request) {
        return sendRequest(request, 1, TimeUnit.MINUTES);
    }

    /**
     * Sends a request to this tracker and returns its response.
     * <p>
     * This method provides a best-effort guarantee that it won't take more than the specified time. No guarantees are
     * made.
     * <p>
     * If the request fails, this method must return null, rather than throw an exception.
     * 
     * @param request Request to send to this tracker
     * @param time Maximum time for the whole request
     * @param unit Time unit in which the <tt>time</tt> argument is expressed
     * @return The response of this tracker, or <tt>null</tt> if an error occurs
     */
    public abstract TrackerResponse sendRequest (TrackerRequest request, long time, TimeUnit unit);

    /**
     * Constructs the query string of the announce request.
     * 
     * @param req Request to build the string from
     * @return The query string to send to the tracker
     */
    protected static String getUrlParams (TrackerRequest req) {
        try {
            StringBuilder sb = new StringBuilder("?");

            sb.append("info_hash=");
            sb.append(req.getInfoHash().toUrlEncodedString());

            sb.append("&peer_id=");
            sb.append(req.getPeerId().toUrlEncodedString());

            sb.append("&port=");
            sb.append(req.getPort());

            sb.append("&uploaded=");
            sb.append(req.getBytesUploaded());

            sb.append("&downloaded=");
            sb.append(req.getBytesDownloaded());

            sb.append("&left=");
            sb.append(req.getBytesLeft());

            if (req.isCompact() != null) {
                sb.append("&compact=");
                sb.append(req.isCompact() ? "1" : "0");
            }

            sb.append("&no_peer_id=");
            sb.append(req.getWantPeerId() ? "0" : "1");

            sb.append("&event=");
            sb.append(req.getEvent().getEventString());

            if (req.getIp() != null) {
                sb.append("&ip=");
                sb.append(req.getIp().getHostAddress());
            }

            sb.append("&numwant=");
            sb.append(req.getNumWant());

            sb.append("&key=");
            sb.append(req.getKey());

            if (req.getTrackerId() != null) {
                sb.append("&trackerid=");
                String trstr = new String(req.getTrackerId(), Charset.forName("ISO-8859-1"));
                sb.append(URLEncoder.encode(trstr, "ISO-8859-1"));
            }

            return sb.toString();
        } catch (UnsupportedEncodingException e) {
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
    protected static String getRequestUrl (String announce, TrackerRequest req) {
        return announce + getUrlParams(req);
    }

    /**
     * Returns a <tt>Tracker</tt> object that send its requests to the given announce URL.
     * 
     * @param announce The URL of the announce service
     * @return A tracker which announces to the given URL
     */
    protected static Tracker getSingleTracker (String announce) {
        try {
            URI uri = new URI(announce);

            if (uri.getScheme().equals("udp")) {
                return new UdpTracker(uri);
            } else {
                // Create URL tracker
                return new UrlTracker(uri.toURL());
            }
        } catch (URISyntaxException exc) {
            return null;

        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns a <tt>Tracker</tt> object that sends its request to the given announce URLs, in order, until one of them
     * is successful.
     * 
     * @param announces List of announce URLs
     * @return A tracker which announces to the given URL list
     */
    /* package */static Tracker getBackedTracker (Collection<String> announces) {
        List<Tracker> trackers = new ArrayList<Tracker>();

        for (String str : announces) {
            Tracker tracker = getSingleTracker(str);
            trackers.add(tracker);

        }

        if (trackers.isEmpty()) {
            return null;
        }

        if (trackers.size() == 1) {
            return trackers.get(0);
        }

        return new BackedTracker(trackers);
    }

    /**
     * Returns the collection of trackers a torrent is supposed to send its announce requests .
     * <p>
     * If the URL's of the passed torrent are malformed, this method will end up returning an empty collection. Caller
     * code should check for this condition before trying to make requests.
     * 
     * @param torrent Torrent meta-info used to build the tracker
     * @return A collection of trackers to use for the given torrent
     */
    public static Collection<Tracker> forTorrent (TorrentMetaInfo torrent) {
        Collection<Tracker> trackers = new ArrayList<>();
        Tracker announceTracker = getSingleTracker(torrent.getAnnounce());
        if (announceTracker != null) {
            trackers.add(announceTracker);
        }

        for (Set<String> announces : torrent.getAnnounceList()) {
            Tracker tracker = getBackedTracker(announces);
            if (tracker != null) {
                trackers.add(tracker);
            }
        }

        return Collections.unmodifiableCollection(trackers);
    }
}
