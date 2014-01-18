import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import es.darkhogg.torrent.data.PeerId;
import es.darkhogg.torrent.data.TorrentMetaInfo;
import es.darkhogg.torrent.tracker.Tracker;
import es.darkhogg.torrent.tracker.TrackerRequest;
import es.darkhogg.torrent.tracker.TrackerRequest.Event;

/**
 * Example main class that shows how to make tracker announces.
 * <p>
 * Not all fields of the {@link TrackerRequest} are mandatory, check the documentation.
 * 
 * @author Daniel Escoz
 */
public final class TrackerRequests {

    /**
     * @param args None
     * @throws IOException if he torrent cannot be read
     */
    public static void main (String[] args) throws IOException {
        TorrentMetaInfo tmi = ReadTorrentFile.readExampleFile();
        Collection<Tracker> trackers = Tracker.forTorrent(tmi);

        TrackerRequest req = TrackerRequest//
            .builder()//
            .bytesDownloaded(0)//
            .bytesUploaded(0)//
            .bytesLeft(tmi.getInfoSection().getTotalLength())//
            .event(Event.STARTED)//
            .infoHash(tmi.getInfoHash())//
            .numWant(8)//
            .wantPeerId(true)//
            .peerId(new PeerId("01234567890123456789"))//
            .port(54321)//
            .build();

        System.out.printf("Request: %s%n", req);

        for (Tracker tracker : trackers) {
            System.out.printf("%nSending request to tracker: %s%n", tracker);
            System.out.printf("Response: %s%n", tracker.sendRequest(req, 15, TimeUnit.SECONDS));
        }
    }

}
