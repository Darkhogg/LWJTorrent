package es.darkhogg.torrent.data;

/**
 * Represents a range of positions within a file, a buffer, or some other
 * linear structure.
 * <p>
 * This class is not general purpose, and should not be used outside this
 * library unless required by some return or parameter type specification.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class PositionRange {
	
	/**
	 * Lower bound
	 */
	private final long lower;
	
	/**
	 * Upper bound
	 */
	private final long upper;
	
	/**
	 * Constructs a new range where the included positions are all <tt>x</tt>
	 * so that <tt>lower <= x < upper</tt>
	 * 
	 * @param lower Lower range bound, inclusive
	 * @param upper Upper range bound, exclusive
	 */
	public PositionRange ( long lower, long upper ) {
		if ( lower < 0 || upper < lower ) {
			throw new IllegalArgumentException();
		}
		
		this.lower = lower;
		this.upper = upper;
	}
	
	/**
	 * Gets the lower bound of this range. This is an <i>inclusive</i> bound.
	 * @return The lower bound of the range
	 */
	public long getLower () {
		return lower;
	}
	
	/**
	 * Gets the upper bound of this range. This is an <i>exclusive</i> bound.
	 * @return The upper bound of the range
	 */
	public long getUpper () {
		return upper;
	}
	
	/**
	 * Gets the length of this range. This value is the result of
	 * <tt>upper - lower</tt>, where <tt>upper</tt> and <tt>lower</tt> are the
	 * upper and lower bounds, respectively, returned by {@link #getUpper} and
	 * {@link #getLower}.
	 * 
	 * @return The number of elements included in the range
	 */
	public long getLength () {
		return upper-lower;
	}
	
	@Override
	public String toString () {
		return lower + "~" + upper;
	}

	@Override
	public boolean equals ( Object obj ) {
		if ( !(obj instanceof PositionRange) ) {
			return false;
		}
		
		PositionRange pr = (PositionRange) obj;
		return ( upper == pr.upper ) & ( lower == pr.lower );
	}
	
	@Override
	public int hashCode () {
		return (int)( upper * 11 + lower * 27 );
	}
}
