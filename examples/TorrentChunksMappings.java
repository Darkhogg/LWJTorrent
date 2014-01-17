import java.io.IOException;

import es.darkhogg.torrent.data.TorrentMapping;
import es.darkhogg.torrent.data.TorrentMetaInfo;

/**
 * Example main class that shows how to obtain torrent mappings.
 * <p>
 * {@link TorrentMapping}s are utilities meant to be used by clients to easily store/load the contents of a piece inside
 * the corresponding files.
 * <p>
 * This example does not show usage of the {@link TorrentMapping#getFilesForPiece(int)} or
 * {@link TorrentMapping#getPiecesForFile(java.nio.file.Path)} methods. These two methods can be used to return only
 * mappings between one specific piece or file.
 * 
 * @author Daniel Escoz
 */
public final class TorrentChunksMappings {

    /**
     * @param args None
     * @throws IOException if he torrent cannot be read
     */
    public static void main (String[] args) throws IOException {
        TorrentMetaInfo tmi = ReadTorrentFile.readExampleFile();
        TorrentMapping tm = TorrentMapping.fromTorrent(tmi);

        for (TorrentMapping.Entry mapping : tm.getMappings()) {
            System.out.printf("Piece %d [%s] => File '%s' [%s]%n", mapping.getPiece(), mapping.getPieceRange(), mapping
                .getFile(), mapping.getFileRange());
        }
    }

}
