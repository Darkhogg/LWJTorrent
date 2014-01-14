package es.darkhogg.torrent.dht;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * A class that represents the ID of a DHT node.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class NodeId {

    /**
     * Node ID as an array of bytes
     */
    private final byte[] bytes;

    /**
     * Cached hash-code
     */
    private final int hash;

    /**
     * Cached toString value
     */
    private final String string;

    /**
     * Creates a <tt>NodeId</tt> using a byte array of length 20.
     * 
     * @param bytes Node bytes
     * @throws NullPointerException if <tt>bytes</tt> is <tt>null</tt>
     * @throws IllegalArgumentException if <tt>bytes</tt> is not of length 20
     */
    public NodeId (byte[] bytes) {
        Objects.requireNonNull(bytes);

        if (bytes.length != 20) {
            throw new IllegalArgumentException("invalid length");
        }

        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.hash = Arrays.hashCode(bytes);

        // Hex String
        StringBuilder sb = new StringBuilder("NodeID(");
        for (byte b : bytes) {
            int ub = (b) & 0xFF;

            if (ub < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(ub).toUpperCase());
        }
        this.string = sb.append(")").toString();
    }

    /**
     * Returns this node ID as an array of bytes.
     * 
     * @return A 20-bytes array
     */
    public byte[] getBytes () {
        return Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Compares this node ID to another one for equality.
     * <p>
     * A <tt>NodeID</tt> is equal only to another <tt>NodeID</tt> object that represents the same peer ID as this
     * object.
     */
    @Override
    public boolean equals (Object obj) {
        if (!(obj instanceof NodeId)) {
            return false;
        }

        NodeId n = (NodeId) obj;
        return Arrays.equals(n.bytes, bytes);
    }

    @Override
    public int hashCode () {
        return hash;
    }

    /**
     * Returns the node ID as a string object
     */
    @Override
    public String toString () {
        return string;
    }

    /**
     * Creates a <tt>NodeId</tt> object using random bytes obtained from the passed <tt>rand</tt> argument.
     * <p>
     * If two instances of <tt>Random</tt> are on the same state, calling this method with them will produce equal
     * <tt>NodeId</tt> objects.
     * 
     * @param rand Random used to generate the random IDs
     * @return A new random node ID
     */
    public static NodeId getRandomNodeId (Random rand) {
        byte[] bytes = new byte[20];
        rand.nextBytes(bytes);
        return new NodeId(bytes);
    }

    /**
     * Creates a <tt>NodeId</tt> object using random bytes, by calling {@link #getRandomNodeId(Random)} with a newly
     * created <tt>Random</tt> object
     * 
     * @return A new random node ID
     */
    public static NodeId getRandomNodeId () {
        return getRandomNodeId(new Random());
    }
}
