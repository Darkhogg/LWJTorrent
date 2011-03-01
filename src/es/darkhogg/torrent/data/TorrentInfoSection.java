package es.darkhogg.torrent.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import es.darkhogg.bencode.*;

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
	 * SHA-1 hash of the info section
	 */
	private final Sha1Hash hash;
	
	/**
	 * The content of the <tt>name</tt> key
	 */
	private final String name;
	
	/**
	 * Constructs this object using every piece of information needed.
	 * 
	 * @param pieceLength Nominal length of every piece
	 * @param pieceHashes List of SHA-1 hashes of all pieces
	 * @param priv Whether this is a private torrent
	 * @param baseDir The advised base directory name
	 * @param files List of file information
	 * @param hash SHA-1 hash of the info section
	 * @throws NullPointerException if some argument is <tt>null</tt> or
	 *         contains a <tt>null</tt>
	 * @throws IllegalArgumentException if <tt>pieceLength</tt> is negative
	 */
	private TorrentInfoSection (
		long pieceLength, List<Sha1Hash> pieceHashes, boolean priv,
		String baseDir, List<TorrentFileInfo> files, Sha1Hash hash,
		String name
	) {
		if ( baseDir == null | hash == null | name == null
		   | pieceHashes.contains( null ) | files.contains( null ) 
		) {
			throw new NullPointerException();
		}
		
		if ( pieceLength < 0 ) {
			throw new IllegalArgumentException();
		}
		
		this.pieceLength = pieceLength;
		this.priv = priv;
		this.baseDir = baseDir;
		this.hash = hash;
		this.pieceHashes = Collections.unmodifiableList(
			new ArrayList<Sha1Hash>( pieceHashes ) );
		this.files = Collections.unmodifiableList(
			new ArrayList<TorrentFileInfo>( files ) );
		this.name = name;
	}
	
	/**
	 * Returns the nominal piece length, that is, the length of every piece in
	 * the torrent except the last one.
	 * 
	 * @return The nominal length of every piece
	 */
	public long getPieceLength () {
		return pieceLength;
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
	 * Returns whether this torrent is private, that is, if the only way
	 * clients should obtain peer information is using the tracker.
	 * 
	 * @return Whether this is a private torrent
	 */
	public boolean isPrivate () {
		return priv;
	}
	
	/**
	 * Returns the advised directory name to store this torrent.
	 * <p>
	 * If this torrent is a multi-file torrent, the value from the <i>name</i>
	 * value is used. If not, a string equal to <tt>"."</tt> is returned.
	 * 
	 * @return The advised base directory name
	 */
	public String getBaseDir () {
		return baseDir;
	}
	
	/**
	 * Returns a list containing information of every file listed in this
	 * torrent.
	 * 
	 * @return List of file information
	 */
	public List<TorrentFileInfo> getFiles () {
		return files;
	}
	
	/**
	 * Returns the SHA-1 hash of the original bencode value used to create this
	 * object.
	 * 
	 * @return SHA-1 hash of the info section
	 */
	public Sha1Hash getHash () {
		return hash;
	}
	
	/**
	 * Returns the value of the <tt>name</tt> entry. This can serve as a way
	 * to identify the torrent in an user interface, but should never be used
	 * as an identifier in any kind of data structure.
	 * 
	 * @return The <tt>name</tt> of this info section
	 */
	public String getName () {
		return name;
	}
	
	/**
	 * Creates a new <tt>TorrentInfoSection</tt> object based on the
	 * information contained on the passed <tt>value</tt> argument
	 * 
	 * @param value A bencode value containing the info section
	 * @return A new <tt>TorrentInfoSection</tt> that represents the
	 *         <tt>value</tt> parameter
	 * @throws NullPointerException if <tt>value</tt> is <tt>null</tt>
	 * @throws IllegalArgumentException if <tt>value</tt> is not a bencode
	 *         dictionary that holds the information in a torrent info section
	 */
	public static TorrentInfoSection fromValue ( Value<?> value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		try {
			long pieceLength = ( (IntegerValue) Bencode.getChildValue(
				value, "piece length" ) ).getValue().longValue();
			
			IntegerValue privv = (IntegerValue) Bencode.getChildValue(
				value, "private" );
			boolean priv = privv==null
						 ? false
						 : ( privv.getValue().longValue() == 1L );
			
			StringValue piecesv = (StringValue) Bencode.getChildValue(
				value, "pieces" );
			byte[] piecesa = piecesv.getValue();
			if ( piecesa.length % 20 != 0 ) {
				throw new IllegalArgumentException();
			}
			List<Sha1Hash> pieceHashes = new ArrayList<Sha1Hash>();
			for ( int i = 0; i < piecesa.length; i += 20 ) {
				byte[] hashbytes = new byte[ 20 ];
				for ( int j = 0; j < 20; j++ ) {
					hashbytes[ j ] = piecesa[ i+j ];
				}
				
				pieceHashes.add( new Sha1Hash( hashbytes ) );
			}
			
			String name = ( (StringValue) Bencode.getChildValue(
				value, "name" ) ).getStringValue();
			String baseDir;
			List<TorrentFileInfo> files = new ArrayList<TorrentFileInfo>();
			
			ListValue filesv = (ListValue) Bencode.getChildValue(
				value, "files" );
			if ( filesv == null ) {
				// SingleFile Mode
				baseDir = ".";

				long length = ( (IntegerValue) Bencode.getChildValue(
					value, "length" ) ).getValue().longValue();
				String fname = ( (StringValue) Bencode.getChildValue(
					value, "name" ) ).getStringValue();
				
				TorrentFileInfo file = new TorrentFileInfo(
					length, Arrays.asList( fname ) );
				
				files.add( file );
			} else {
				// MultiFile Mode
				baseDir = ( (StringValue) Bencode.getChildValue(
					value, "name" ) ).getStringValue();
				
				List<Value<?>> filesvl = filesv.getValue();
				for ( Value<?> val : filesvl ) {
					DictionaryValue dval = (DictionaryValue) val;
					
					long length = ( (IntegerValue) Bencode.getChildValue(
						dval, "length" ) ).getValue().longValue();
					ListValue pathv = (ListValue) Bencode.getChildValue(
						dval, "path" );
					List<String> path = new ArrayList<String>();
					
					for ( Value<?> pathpiece : pathv.getValue() ) {
						StringValue pps = (StringValue) pathpiece;
						path.add( pps.getStringValue() );
					}
					
					files.add( new TorrentFileInfo( length, path ) );
				}
			}
			
			Sha1Hash hash = Sha1Hash.forValue( value );
			
			return new TorrentInfoSection( pieceLength, pieceHashes,
				priv, baseDir, files, hash, name );
		} catch ( Exception e ) {
			if ( e instanceof IllegalArgumentException ) {
				throw ( (IllegalArgumentException) e );
			} else {
				throw new IllegalArgumentException( e );
			}
		}
	}
	
	/**
	 * Checks if this object is equal to the given <tt>obj</tt>.
	 * <p>
	 * This object is only equal to a <tt>TorrentInfoSection</tt> object that
	 * holds the same information and was created using a bencode value
	 * equivalent to the bencode value that created this object.
	 */
	@Override
	public boolean equals ( Object obj ) {
		if ( !(obj instanceof TorrentInfoSection) ) {
			return false;
		}
		
		TorrentInfoSection tis = (TorrentInfoSection) obj;
		
		return hash.equals( tis.hash )
		    && name.equals( tis.name )
			&& pieceLength == tis.pieceLength
			&& priv == tis.priv
			&& pieceHashes.equals( tis.pieceHashes )
			&& baseDir.equals( tis.baseDir )
			&& files.equals( tis.files );
	}
	
	@Override
	public int hashCode () {
		return hash.hashCode();
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder( "TorrentInfoSection{{ " );

		sb.append( "PieceLength(" ).append( pieceLength ).append( "), " );
		sb.append( "Pieces(" ).append( pieceHashes.size() ).append( "), " );
		sb.append( "Private(" ).append( priv ).append( "), " );
		sb.append( "Hash(" ).append( hash ).append( "), " );
		sb.append( "BaseDir(" ).append( baseDir ).append( "), " );
		sb.append( "Files(" ).append( files ).append( "), " );
		
		return sb.toString();
	}
	
}
