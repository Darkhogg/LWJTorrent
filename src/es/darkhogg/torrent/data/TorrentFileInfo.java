package es.darkhogg.torrent.data;

import java.nio.file.Path;

/**
 * Represents one file from a torrent file.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class TorrentFileInfo {

    /**
     * Length of the file
     */
    private final long length;

    /**
     * Path of the file, including the filename
     */
    private final Path path;

    /**
     * Constructs an object using the given <tt>length</tt> and <tt>path</tt>
     * 
     * @param length Length of the file, in bytes
     * @param path Path of the file, including its name
     * @throws NullPointerException if <tt>path</tt> or any of its childs is <tt>null</tt>
     * @throws IllegalArgumentException if <tt>length</tt> is less than 0 or the passed path is not relative
     */
    public TorrentFileInfo (long length, Path path) {
        if (path == null) {
            throw new NullPointerException();
        }
        if (length < 0 | path.isAbsolute()) {
            throw new IllegalArgumentException();
        }

        this.length = length;
        this.path = path;
    }

    /**
     * Returns the length of the file represented by this object, in bytes.
     * 
     * @return The length, in bytes, of this file
     */
    public long getLength () {
        return length;
    }

    /**
     * Returns the path of the file represented by this object. It is not guaranteed that this path will be valid once
     * constructed.
     * <p>
     * Every element in the returned list is a path node, usually a directory, except the last one, which is the file
     * name itself.
     * 
     * @return The path of this file
     */
    public Path getPath () {
        return path;
    }

    /**
     * Checks whether this object is equal to <tt>obj</tt>.
     * <p>
     * This object is equal to another <tt>TorrentFileInfo</tt> with the same {@link #getLength length} and an equal
     * {@link #getPath path}. In particular, two objects that represent the same file but with different <tt>path</tt>s
     * are not equal.
     */
    @Override
    public boolean equals (Object obj) {
        if (!(obj instanceof TorrentFileInfo)) {
            return false;
        }

        TorrentFileInfo tfi = (TorrentFileInfo) obj;
        return tfi.length == length && path.equals(tfi.path);
    }

    @Override
    public int hashCode () {
        return (int) (length) ^ path.hashCode();
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder("TorrentFileInfo{{ ");

        sb.append("Length(").append(length).append("), ");
        sb.append("Path(").append(path).append("), ");

        return sb.toString();
    }
}
