package es.darkhogg.torrent.dht;

import java.math.BigInteger;

import es.darkhogg.torrent.data.Sha1Hash;

/**
 * A class capable of comparing <tt>NodeId</tt> objects to determine their
 * relative <i>distance</i> to another one.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public abstract class DistanceMetric {
	
	/**
	 * Returns the distance between the passed nodes as a <tt>BigInteger</tt>
	 * object.
	 * <p>
	 * The returned <tt>BigInteger</tt> object must be positive.
	 * 
	 * @param n1
	 *            First node ID
	 * @param n2
	 *            Second node ID
	 * @return The distance between the two node IDs
	 */
	public abstract BigInteger getDistance ( NodeId n1, NodeId n2 );
	
	/**
	 * Returns the distance between the passed node and hash as a
	 * <tt>Distance</tt> object.
	 * 
	 * @param hash
	 *            Hash object
	 * @param node
	 *            Node ID
	 * @return The distance between the hash and the node ID
	 */
	public final BigInteger getDistance ( Sha1Hash hash, NodeId node ) {
		return getDistance( new NodeId( hash.getBytes() ), node );
	}
	
	/**
	 * Returns a <tt>DistanceMetric</tt> that calculates distances by XORing the
	 * node IDs and interpreting the result as an unsigned integer.
	 * 
	 * @return A metric distance that uses XOR operations
	 */
	public final static DistanceMetric getXorDistanceMetric () {
		return new XorDistanceMetric();
	}
}
