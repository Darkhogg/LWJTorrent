package es.darkhogg.torrent.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that represents the bidirectional mapping of torrent pieces to files.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class TorrentMapping {

    /**
     * Total number of pieces mapped in this object
     */
    private final int pieceCount;

    /**
     * Full Mapping list
     */
    private final List<Entry> mappings;

    /**
     * Mapping from pieces to files
     */
    private final List<List<Entry>> piecesToFiles;

    /**
     * Mapping from files to pieces
     */
    private final Map<Path,List<Entry>> filesToPieces;

    /**
     * List of files mapped in this object
     */
    private final List<Path> fileList;

    /**
     * Constructs a <tt>TorrentMapping</tt> by generating the mapping from the given list of files and piece size.
     * <p>
     * Note that this constructor is private, but a static factory exists with the same purpose that just calls this. It
     * would be a bad idea to expose this constructor, as it is not really intuitive that a <tt>TorrentMapping</tt> is
     * <i>constructed from</i> a list of files and a piece size.
     * 
     * @param files Lit of files to map
     * @param pieceSize Size of the piece
     */
    private TorrentMapping (List<TorrentFileInfo> files, long pieceSize) {
        List<Entry> allMappings = new ArrayList<Entry>(files.size());
        List<Path> allFiles = new ArrayList<Path>(files.size());
        final List<List<Entry>> pToF = new ArrayList<List<Entry>>();
        final Map<Path,List<Entry>> fToP = new HashMap<Path,List<Entry>>();

        int numPieces = 0;

        long remPieceLen = pieceSize;
        long initPiecePos = 0;

        List<Entry> thisPieceMappings = new ArrayList<Entry>();

        for (TorrentFileInfo file : files) {
            List<Entry> thisFileMappings = new ArrayList<Entry>((int) (file.getLength() / pieceSize + 1));

            long remFileLen = file.getLength();
            long initFilePos = 0;

            // While this file is not fully mapped...
            while (remFileLen > 0) {

                // If the remaining piece fits entirely in this file...
                if (remFileLen >= remPieceLen) {
                    Entry entry =
                        new Entry(numPieces, new PositionRange(initPiecePos, (initPiecePos + remPieceLen)), file
                            .getPath(), new PositionRange(initFilePos, (initFilePos + remPieceLen)));
                    allMappings.add(entry);
                    thisFileMappings.add(entry);
                    thisPieceMappings.add(entry);

                    remFileLen -= remPieceLen;
                    initFilePos += remPieceLen;

                    remPieceLen = pieceSize;
                    initPiecePos = 0;

                    numPieces++;
                    pToF.add(thisPieceMappings);
                    thisPieceMappings = new ArrayList<Entry>();
                } else {
                    Entry entry =
                        new Entry(numPieces, new PositionRange(initPiecePos, (initPiecePos + remFileLen)), file
                            .getPath(), new PositionRange(initFilePos, (initFilePos + remFileLen)));
                    allMappings.add(entry);
                    thisFileMappings.add(entry);
                    thisPieceMappings.add(entry);

                    remPieceLen -= remFileLen;
                    initPiecePos += remFileLen;

                    remFileLen = 0;
                }

            }

            allFiles.add(file.getPath());
            fToP.put(file.getPath(), thisFileMappings);
        }

        // If the last piece was not of regular size...
        if (remPieceLen < pieceSize) {
            numPieces++;
            pToF.add(thisPieceMappings);
        }

        pieceCount = numPieces;
        piecesToFiles = Collections.unmodifiableList(pToF);
        filesToPieces = Collections.unmodifiableMap(fToP);
        fileList = Collections.unmodifiableList(allFiles);
        mappings = Collections.unmodifiableList(allMappings);
    }

    /**
     * Returns the total number of pieces. Valid piece indices are integers from <tt>0</tt> (inclusive) to
     * <tt>partCount</tt> (exclusive).
     * 
     * @return The number of pieces mapped in this object
     */
    public int getPieceCount () {
        return pieceCount;
    }

    /**
     * Returns a list with all the mappings in this object.
     * 
     * @return All the mappings in this object
     */
    public List<Entry> getMappings () {
        return mappings;
    }

    /**
     * Returns a list of all the files in the mapping.
     * 
     * @return All the files in the mapping
     */
    public List<Path> getFiles () {
        return fileList;
    }

    /**
     * Returns information about which sections of which files are bound to the piece with the given index
     * 
     * @param pieceIndex Index of the piece to retrieve information from
     * @return The information about which files contains the requested piece, or <tt>null</tt> if the index is not
     *         valid.
     */
    public List<Entry> getFilesForPiece (int pieceIndex) {
        if (pieceIndex < 0 | pieceIndex >= pieceCount) {
            return null;
        }

        return piecesToFiles.get(pieceIndex);
    }

    /**
     * Returns information about which sections of which pieces are bound to the specified file.
     * 
     * @param file The file to retrieve which pieces covers it
     * @return The information about which pieces covers the requested file, or <tt>null</tt> if the file is not valid.
     */
    public List<Entry> getPiecesForFile (Path file) {
        if (file == null) {
            throw new NullPointerException();
        }

        return filesToPieces.get(file);
    }

    /**
     * Returns a <tt>String</tt> representation of this mapping.
     */
    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder("TorrentMapping{\n");

        for (Entry entry : mappings) {
            sb.append("\t").append(entry).append("\n");
        }

        return sb.append("}").toString();
    }

    /**
     * Returns a new mapping for this torrent. The returned object contains information about all the pieces and all the
     * files, fully covering them.
     * 
     * @param tmi The torrent to read the mapping from
     * @return A new mapping for the given torrent
     */
    public static TorrentMapping fromTorrent (TorrentMetaInfo tmi) {
        return fromTorrent(tmi.getInfoSection());
    }

    /**
     * Returns a new mapping for this torrent. The returned object contains information about all the pieces and all the
     * files, fully covering them.
     * 
     * @param tis The torrent to read the mapping from
     * @return A new mapping for the given torrent
     */
    public static TorrentMapping fromTorrent (TorrentInfoSection tis) {
        return new TorrentMapping(tis.getFiles(), tis.getPieceLength());
    }

    /**
     * A class that represents a single mapping between a section of a piece and a section of a file.
     * 
     * @author Daniel Escoz
     * @version 1.0
     */
    public static final class Entry {

        /**
         * Piece index mapped
         */
        private final int piece;

        /**
         * Range of positions mapped from the piece
         */
        private final PositionRange pieceRange;

        /**
         * File mapped
         */
        private final Path file;

        /**
         * Range of positions mapped from the file
         */
        private final PositionRange fileRange;

        /**
         * Creates a new bidirectional mapping from a section of a piece to an equally sized section of a file.
         * 
         * @param piece Index of the piece to be mapped
         * @param pieceRange Range of positions mapped in the piece
         * @param file File to be mapped
         * @param fileRange Range of positions mapped in the piece
         * @throws NullPointerException if any of the arguments is <tt>null</tt>
         * @throws IllegalArgumentException if <tt>piece</tt> is negative or <tt>pieceRange</tt> and <tt>fileRange</tt>
         *             have different lengths
         */
        public Entry (int piece, PositionRange pieceRange, Path file, PositionRange fileRange) {
            if (piece < 0 | pieceRange.getLength() != fileRange.getLength()) {
                throw new IllegalArgumentException();
            }
            if (file == null) {
                throw new NullPointerException();
            }

            this.piece = piece;
            this.pieceRange = pieceRange;
            this.file = file;
            this.fileRange = fileRange;
        }

        /**
         * Returns the index of the piece mapped in this object
         * 
         * @return The index of the mapped piece
         */
        public int getPiece () {
            return piece;
        }

        /**
         * Returns the range of positions mapped in the piece. This range is guaranteed to be of the same size as the
         * one returned by {@link #getFileRange}.
         * 
         * @return Range of positions mapped in the piece
         */
        public PositionRange getPieceRange () {
            return pieceRange;
        }

        /**
         * Returns the file mapped in this object
         * 
         * @return The file mapped piece
         */
        public Path getFile () {
            return file;
        }

        /**
         * Returns the range of positions mapped in the file. This range is guaranteed to be of the same size as the one
         * returned by {@link #getPieceRange}.
         * 
         * @return Range of positions mapped in the file
         */
        public PositionRange getFileRange () {
            return fileRange;
        }

        @Override
        public String toString () {
            return "(#" + getPiece() + ": " + getPieceRange() + ") <=> ('" + getFile() + "': " + getFileRange() + ")";
        }

        @Override
        public boolean equals (Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            }

            Entry e = (Entry) obj;
            return (piece == e.piece) && (pieceRange.equals(e.pieceRange)) && (file.equals(e.file))
                && (fileRange.equals(e.fileRange));
        }

        @Override
        public int hashCode () {
            int hash = 0;
            hash = (hash + piece + 1) * 11;
            hash = (hash + pieceRange.hashCode()) * 27;
            hash = (hash + file.hashCode()) * 13;
            hash = (hash + fileRange.hashCode()) * 31;

            return hash;
        }
    }
}
