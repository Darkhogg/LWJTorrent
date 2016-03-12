import java.io.IOException;
import java.util.List;

import es.darkhogg.torrent.bencode.BencodeInputStream;
import es.darkhogg.torrent.data.Sha1Hash;
import es.darkhogg.torrent.data.TorrentFileInfo;
import es.darkhogg.torrent.data.TorrentInfoSection;
import es.darkhogg.torrent.data.TorrentMetaInfo;

/**
 * Example main class that shows the loading of a torrent file.
 * <p>
 * This class will load and print a torrent file (<tt>example.torrent</tt>) using a <tt>BencodeInputStream</tt> to read
 * a bencode value and pass it to {@link TorrentMetaInfo#fromValue}. Note that {@link TorrentMetaInfo#fromFile} allows
 * you to read a torrent file directly if you know the file path.
 * 
 * @author Daniel Escoz
 */
public final class ReadTorrentFile {

    /**
     * Loads the {@link TorrentMetaInfo} of the example torrent file.
     * 
     * @return The example torrent meta info object
     * @throws IOException If the file cannot be read
     */
    public static TorrentMetaInfo readExampleFile () throws IOException {
    	FileInputStream  torrentFile = new FileInputStream("/path/to/example.torrent");
        try (
            BencodeInputStream bis =
                new BencodeInputStream(torrentFile)){
            return TorrentMetaInfo.fromValue(bis.readValue());
        }
    }

    /**
     * @param args None
     * @throws IOException If the torrent cannot be read
     */
    public static void main (String[] args) throws IOException {
        TorrentMetaInfo tmi = readExampleFile();
        TorrentInfoSection tis = tmi.getInfoSection();

        System.out.printf("Comment: %s%n", tmi.getComment());
        System.out.printf("Created By: %s%n", tmi.getCreatedBy());
        System.out.printf("Date: %s%n", tmi.getCreationDate());
        System.out.printf("Announce: %s%n", tmi.getAnnounce());
        System.out.printf("Announce List: %s%n", tmi.getAnnounceList());
        System.out.printf("Info Hash: %s%n", tmi.getInfoHash());

        System.out.printf("Info Section:%n");
        System.out.printf("- Name: %s%n", tis.getName());
        System.out.printf("- Dir: %s%n", tis.getBaseDir());
        System.out.printf("- Pieces: %d (%d bytes each)%n", tis.getNumPieces(), tis.getPieceLength());

        System.out.printf("- File list:%n");
        for (TorrentFileInfo tfi : tis.getFiles()) {
            System.out.printf("  + %s (%d bytes)%n", tfi.getPath(), tfi.getLength());
        }

        System.out.printf("- Piece List:%n");
        List<Sha1Hash> hashes = tis.getPieceHashes();
        for (int i = 0; i < hashes.size(); i++) {
            System.out.printf("  + %s (%d bytes)%n", hashes.get(i), tis.getPieceLength(i));
        }

    }
}
