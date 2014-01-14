package es.darkhogg.torrent.data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.darkhogg.torrent.bencode.Bencode;
import es.darkhogg.torrent.bencode.DictionaryValue;
import es.darkhogg.torrent.bencode.IntegerValue;
import es.darkhogg.torrent.bencode.ListValue;
import es.darkhogg.torrent.bencode.StringValue;
import es.darkhogg.torrent.bencode.Value;

/**
 * Represents the <i>info</i> bencode dictionary from a .torrent file.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class TorrentInfoSection {

    /**
     * Nominal length of every piece
     */
    private final long pieceLength;

    /**
     * List of SHA-1 hashes of all pieces
     */
    private final List<Sha1Hash> pieceHashes;

    /**
     * Whether this is a private torrent
     */
    private final boolean priv;

    /**
     * The advised base directory name
     */
    private final String baseDir;

    /**
     * List of file information
     */
    private final List<TorrentFileInfo> files;

    /**
     * Mapping from files to their infos
     */
    private final Map<Path,TorrentFileInfo> fileInfos = new HashMap<Path,TorrentFileInfo>();

    /**
     * SHA-1 hash of the info section
     */
    private final Sha1Hash hash;

    /**
     * The content of the <tt>name</tt> key
     */
    private final String name;

    /**
     * The total length of this torrent
     */
    private final long totalLength;

    /**
     * Constructs this object using every piece of information needed.
     * 
     * @param pieceLength Nominal length of every piece
     * @param pieceHashes List of SHA-1 hashes of all pieces
     * @param priv Whether this is a private torrent
     * @param baseDir The advised base directory name
     * @param files List of file information
     * @param hash SHA-1 hash of the info section
     * @throws NullPointerException if some argument is <tt>null</tt> or contains a <tt>null</tt>
     * @throws IllegalArgumentException if <tt>pieceLength</tt> is negative
     */
    private TorrentInfoSection (
        long pieceLength, List<Sha1Hash> pieceHashes, boolean priv, String baseDir, List<TorrentFileInfo> files,
        Sha1Hash hash, String name)
    {
        if ((baseDir == null | hash == null | name == null) || pieceHashes.contains(null) || files.contains(null)) {
            throw new NullPointerException();
        }

        if (pieceLength < 0) {
            throw new IllegalArgumentException();
        }

        this.pieceLength = pieceLength;
        this.priv = priv;
        this.baseDir = baseDir;
        this.hash = hash;
        this.pieceHashes = Collections.unmodifiableList(new ArrayList<Sha1Hash>(pieceHashes));
        this.files = Collections.unmodifiableList(new ArrayList<TorrentFileInfo>(files));
        this.name = name;

        long totalLength = 0;
        for (TorrentFileInfo tfi : files) {
            fileInfos.put(tfi.getPath(), tfi);
            totalLength += tfi.getLength();
        }
        this.totalLength = totalLength;
    }

    /**
     * Returns the number of pieces in this torrent
     * 
     * @return Number of pieces in this torrent
     */
    public int getNumPieces () {
        return pieceHashes.size();
    }

    /**
     * Returns the nominal piece length, that is, the length of every piece in the torrent except the last one.
     * 
     * @return The nominal length of every piece
     */
    public long getPieceLength () {
        return pieceLength;
    }

    /**
     * Returns the length of the <tt>n</tt>-th piece.
     * <p>
     * If <tt>n</tt> corresponds to the last piece ( <tt>{@link #getNumPieces numPieces}-1</tt>), this method returns
     * <tt>{@link #getTotalLength totalLength} %
     * {@link #getPieceLength() pieceLength}</tt>, unless that value is 0, in which case it returns the nominal piece
     * length.
     * <p>
     * If <tt>n</tt> is between 0 (inclusive) and <tt>numPieces-1</tt> (exclusive), it returns the nominal piece length.
     * <p>
     * On any other case, it returns <tt>0</tt>.
     * 
     * @param n Piece index
     * @return The length of the <tt>n</tt>-th piece
     */
    public long getPieceLength (int n) {
        int numPieces = getNumPieces();
        if (n == numPieces - 1) {
            long len = totalLength % pieceLength;
            return len == 0 ? pieceLength : len;
        } else if (n >= 0 && n < numPieces - 1) {
            return pieceLength;
        } else {
            return 0;
        }
    }

    /**
     * Returns the list of the SHA-1 hash of every piece in the torrent.
     * 
     * @return List of SHA-1 hashes of all pieces
     */
    public List<Sha1Hash> getPieceHashes () {
        return pieceHashes;
    }

    /**
     * Returns whether this torrent is private, that is, if the only way clients should obtain peer information is using
     * the tracker.
     * 
     * @return Whether this is a private torrent
     */
    public boolean isPrivate () {
        return priv;
    }

    /**
     * Returns the advised directory name to store this torrent.
     * <p>
     * If this torrent is a multi-file torrent, the value from the <i>name</i> value is used. If not, a string equal to
     * <tt>"."</tt> is returned.
     * 
     * @return The advised base directory name
     */
    public String getBaseDir () {
        return baseDir;
    }

    /**
     * Returns a list containing information of every file listed in this torrent.
     * 
     * @return List of file information
     */
    public List<TorrentFileInfo> getFiles () {
        return files;
    }

    /**
     * Returns the information about the specified file. The given <tt>file</tt> must be <i>equals to</i> the value
     * returned by the {@link TorrentFileInfo#getPath} method to be recognized. Otherwise, this method returns
     * <tt>null</tt>.
     * 
     * @param file File which information is to be returned
     * @return The information about the given <tt>file</tt>, or <tt>null</tt> if it don't exist.
     */
    public TorrentFileInfo getInfoForFile (Path file) {
        return fileInfos.get(file);
    }

    /**
     * Returns the SHA-1 hash of the original bencode value used to create this object.
     * 
     * @return SHA-1 hash of the info section
     */
    public Sha1Hash getHash () {
        return hash;
    }

    /**
     * Returns the value of the <tt>name</tt> entry. This can serve as a way to identify the torrent in an user
     * interface, but should never be used as an identifier in any kind of data structure.
     * 
     * @return The <tt>name</tt> of this info section
     */
    public String getName () {
        return name;
    }

    /**
     * Returns the total length of the torrent.
     * <p>
     * This value is calculated once by adding the lengths of all files in the file list.
     * 
     * @return The total length of this torrent
     */
    public long getTotalLength () {
        return totalLength;
    }

    /**
     * Creates a new <tt>TorrentInfoSection</tt> object based on the information contained on the passed <tt>value</tt>
     * argument
     * 
     * @param value A bencode value containing the info section
     * @return A new <tt>TorrentInfoSection</tt> that represents the <tt>value</tt> parameter
     * @throws NullPointerException if <tt>value</tt> is <tt>null</tt>
     * @throws IllegalArgumentException if <tt>value</tt> is not a bencode dictionary that holds the information in a
     *             torrent info section
     */
    public static TorrentInfoSection fromValue (Value<?> value) {
        if (value == null) {
            throw new NullPointerException();
        }

        try {
            long pieceLength = ((IntegerValue) Bencode.getChildValue(value, "piece length")).getValue().longValue();

            IntegerValue privv = (IntegerValue) Bencode.getChildValue(value, "private");
            boolean priv = privv == null ? false : (privv.getValue().longValue() == 1L);

            StringValue piecesv = (StringValue) Bencode.getChildValue(value, "pieces");
            byte[] piecesa = piecesv.getValue();
            if (piecesa.length % 20 != 0) {
                throw new IllegalArgumentException();
            }
            List<Sha1Hash> pieceHashes = new ArrayList<Sha1Hash>();
            for (int i = 0; i < piecesa.length; i += 20) {
                byte[] hashbytes = new byte[20];
                for (int j = 0; j < 20; j++) {
                    hashbytes[j] = piecesa[i + j];
                }

                pieceHashes.add(new Sha1Hash(hashbytes));
            }

            String name = ((StringValue) Bencode.getChildValue(value, "name")).getStringValue();
            String baseDir;
            List<TorrentFileInfo> files = new ArrayList<TorrentFileInfo>();

            ListValue filesv = (ListValue) Bencode.getChildValue(value, "files");
            if (filesv == null) {
                // SingleFile Mode
                baseDir = ".";

                long length = ((IntegerValue) Bencode.getChildValue(value, "length")).getValue().longValue();
                String fname = ((StringValue) Bencode.getChildValue(value, "name")).getStringValue();

                TorrentFileInfo file = new TorrentFileInfo(length, Paths.get(fname));

                files.add(file);
            } else {
                // MultiFile Mode
                baseDir = ((StringValue) Bencode.getChildValue(value, "name")).getStringValue();

                List<Value<?>> filesvl = filesv.getValue();
                for (Value<?> val : filesvl) {
                    DictionaryValue dval = (DictionaryValue) val;

                    long length = Bencode.getChildValue(dval, IntegerValue.class, "length").getValue().longValue();
                    ListValue pathv = (ListValue) Bencode.getChildValue(dval, "path");
                    StringBuilder path = new StringBuilder();

                    String pSep = System.getProperty("file.separator");
                    for (Value<?> pathpiece : pathv.getValue()) {
                        StringValue pps = (StringValue) pathpiece;
                        path.append(pSep);
                        path.append(pps.getStringValue());
                    }

                    files.add(new TorrentFileInfo(length, Paths.get(path.toString())));
                }
            }

            Sha1Hash hash = Sha1Hash.forValue(value);

            return new TorrentInfoSection(pieceLength, pieceHashes, priv, baseDir, files, hash, name);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw ((IllegalArgumentException) e);
            } else {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Checks if this object is equal to the given <tt>obj</tt>.
     * <p>
     * This object is only equal to a <tt>TorrentInfoSection</tt> object that holds the same information and was created
     * using a bencode value equivalent to the bencode value that created this object.
     */
    @Override
    public boolean equals (Object obj) {
        if (!(obj instanceof TorrentInfoSection)) {
            return false;
        }

        TorrentInfoSection tis = (TorrentInfoSection) obj;

        return hash.equals(tis.hash) && name.equals(tis.name) && pieceLength == tis.pieceLength && priv == tis.priv
            && pieceHashes.equals(tis.pieceHashes) && baseDir.equals(tis.baseDir) && files.equals(tis.files);
    }

    @Override
    public int hashCode () {
        return hash.hashCode();
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder("TorrentInfoSection{{ ");

        sb.append("Name(").append(name).append("), ");
        sb.append("PieceLength(").append(pieceLength).append("), ");
        sb.append("Pieces(").append(pieceHashes.size()).append("), ");
        sb.append("Private(").append(priv).append("), ");
        sb.append("Hash(").append(hash).append("), ");
        sb.append("BaseDir(").append(baseDir).append("), ");
        sb.append("Files(").append(files).append(")");

        return sb.toString();
    }

}
