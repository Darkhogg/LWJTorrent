package es.darkhogg.torrent.data;

import hirondelle.date4j.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import es.darkhogg.torrent.bencode.Bencode;
import es.darkhogg.torrent.bencode.BencodeInputStream;
import es.darkhogg.torrent.bencode.IntegerValue;
import es.darkhogg.torrent.bencode.ListValue;
import es.darkhogg.torrent.bencode.StringValue;
import es.darkhogg.torrent.bencode.Value;

/**
 * Represents the contents of a .torrent file.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class TorrentMetaInfo {
	
	/**
	 * Announce URL
	 */
	private final String announce;
	
	/**
	 * List of Announce URL's
	 */
	private final List<Set<String>> announceList;
	
	/**
	 * Time of creation of the torrent file
	 */
	private final DateTime creationDate;
	
	/**
	 * Comment for the torrent
	 */
	private final String comment;
	
	/**
	 * Author of the torrent file
	 */
	private final String createdBy;
	
	/**
	 * Info dictionary
	 */
	private final TorrentInfoSection info;
	
	/**
	 * Constructs a <tt>TorrentMetaInfo</tt> object specifying all its
	 * attributes.
	 * 
	 * @param info Info dictionary
	 * @param announce Announce URL
	 * @param announceList List of Announce URLs
	 * @param creationDate Time of creation
	 * @param comment Comment of this torrent
	 * @param createdBy Author of this comment
	 * @throws NullPointerException if <tt>info</tt> or <tt>announce</tt> are
	 *         <tt>null</tt>
	 * @throws IllegalArgumentException if some argument is not valid.
	 */
	protected TorrentMetaInfo (
		TorrentInfoSection info, String announce,
		List<Set<String>> announceList, DateTime creationDate, String comment,
		String createdBy
	) {
		if ( announce == null | info == null ) {
			throw new NullPointerException();
		}
		
		this.announce = announce;
		this.creationDate = creationDate;
		this.comment = comment==null ? "" : comment;
		this.createdBy = createdBy==null ? "" : createdBy;
		this.info = info;
		
		if ( announceList == null ) {
			this.announceList = new ArrayList<Set<String>>();
		} else {
			List<Set<String>> ual = new ArrayList<Set<String>>();
			for ( Set<String> ls : announceList ) {
				if ( ls != null ) {
					Set<String> ns = new HashSet<String>( ls );
					ns.remove( null );
					ual.add( Collections.unmodifiableSet( ns ) );
				}
			}
			this.announceList = Collections.unmodifiableList( ual );
		}
	}
	
	/**
	 * Returns the announce URL as defined in the original metainfo file. It is
	 * not guaranteed that the URL is valid.
	 * 
	 * @return The announce URL
	 */
	public String getAnnounce () {
		return announce;
	}
	
	/**
	 * Returns the 'announce-list' dictionary value as a {@link List} of
	 * {@link Set Sets} of {@link String Strings}. If the value was not present
	 * in the original torrent file, this method returns an empty <tt>List</tt>.
	 * <p>
	 * The elements of the returned <tt>List</tt> are in the same order that in
	 * the orginal file, and the <tt>Set</tt> elements contains each of the
	 * announce URLs.
	 * 
	 * @return The announce list
	 */
	public List<Set<String>> getAnnounceList () {
		return announceList;
	}
	
	/**
	 * Returns the creation date of the torrent file. If the creation date was
	 * not specified in the original torrent file, this method returns
	 * <tt>null</tt>.
	 * 
	 * @return The creation date of the torrent file, or <tt>null</tt> if not
	 *         specified.
	 */
	public DateTime getCreationDate () {
		return creationDate;
	}
	
	/**
	 * Returns the comment of the torrent file. If the comment was not
	 * specified in the original torrent file, this method returns an empty
	 * <tt>String</tt>.
	 * <p>
	 * This method never returns <tt>null</tt>
	 * 
	 * @return The comment of the torrent file
	 */
	public String getComment () {
		return comment;
	}
	
	/**
	 * Returns the author of the torrent file. If the author was not specified
	 * in the original torrent file, this method returns an empty
	 * <tt>String</tt>.
	 * <p>
	 * This method never returns <tt>null</tt>
	 * 
	 * @return The author of the torrent file
	 */
	public String getCreatedBy () {
		return createdBy;
	}
	
	/**
	 * Returns a <tt>TorrentInfoSection</tt> object that represents the
	 * <i>info</i> dictionary of the torrent file.
	 * 
	 * @return The info section of this torrent
	 */
	public TorrentInfoSection getInfoSection () {
		return info;
	}

	/**
	 * Returns the SHA-1 hash of this torrent's info dictionary.
	 * <p>
	 * This method is a shorcut for <code>getInfoSection().getHash()</code>
	 * 
	 * @return The hash of the info section
	 */
	public Sha1Hash getInfoHash () {
		return info.getHash();
	}
	
	/**
	 * Creates a new <tt>TorrentMetaInfo</tt> object using the information
	 * contained in the <tt>value</tt> argument.
	 * 
	 * @param value A bencode value containing the torrent metadata
	 * @return A new <tt>TorrentMetaInfo</tt> that represents the
	 *         <tt>value</tt> parameter
	 * @throws NullPointerException if <tt>value</tt> is <tt>null</tt>
	 * @throws IllegalArgumentException if <tt>value</tt> is not a bencode
	 *         dictionary that holds the information in a torrent info section
	 */
	public static TorrentMetaInfo fromValue ( Value<?> value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}
		
		try {
			// Announce
			String announce = ( (StringValue) Bencode.getChildValue(
				value, "announce" ) ).getStringValue();
			
			// Creation Date (Optional)
			IntegerValue datev = (IntegerValue) Bencode.getChildValue(
				value, "creation date" );
			DateTime creationDate = null;
			if ( datev != null ) {
				long dateInstant = datev.getValue().longValue();
				creationDate = DateTime.forInstant( dateInstant*1000,
					TimeZone.getTimeZone( "UTC" ) );
			}
			
			// Comment (Optional)
			StringValue commv = (StringValue) Bencode.getChildValue(
				value, "comment" );
			String comment = commv==null ? null : commv.getStringValue();
			
			// Created By (Optional)
			StringValue crbyv = (StringValue) Bencode.getChildValue(
				value, "created by" );
			String createdBy = crbyv==null ? null : crbyv.getStringValue();
			
			// Info Section
			TorrentInfoSection info = TorrentInfoSection.fromValue(
				Bencode.getChildValue( value, "info" ) );
			
			// Announce List (Optional)
			ListValue annlVal = (ListValue) Bencode.getChildValue(
				value, "announce-list" );
			List<Set<String>> announceList = null;
			if ( annlVal != null ) {
				announceList = new ArrayList<Set<String>>();
				if ( annlVal != null ) {
					List<Value<?>> annList = annlVal.getValue();
					for ( Value<?> annsVal : annList ) {
						List<Value<?>> annSet = ((ListValue) annsVal).getValue();
						Set<String> set = new HashSet<String>();
						for ( Value<?> anneVal :annSet ) {
							set.add( ((StringValue) anneVal).getStringValue() );
						}
						announceList.add( Collections.unmodifiableSet( set ) );
					}
				}
			}
			
			// Create the object
			return new TorrentMetaInfo( info, announce, announceList,
				creationDate, comment, createdBy );
			
		} catch ( Exception e ) {
			throw new IllegalArgumentException( e );
		}
	}
	
	/**
	 * Creates a new <tt>TorrentMetaInfo</tt> object using the contents of
	 * the given <tt>file</tt>.
	 * <p>
	 * This method first reads a value from the file and then pass it to
	 * {@link #fromValue} to create the <tt>TorrentMetaInfo</tt>. If client code
	 * is going to use the <tt>Value</tt> object returned from the file, it is
	 * recommended to do it manually using a <tt>BencodeInputStream</tt> and the
	 * call <tt>fromValue</tt> instead of use this method, which will reparse
	 * the file.
	 * 
	 * @param file File used to create the new object
	 * @return A new <tt>TorrentMetaInfo</tt> that represents the contents of
	 *         <tt>file</tt>
	 * @throws IOException If some I/O error occurs
	 * @throws IllegalArgumentException if the given file is not a valid torrent
	 *         metadata file
	 */
	public static TorrentMetaInfo fromFile ( File file )
	throws IOException {
		BencodeInputStream bin = null;
		try {
			bin = new BencodeInputStream( file );
			return fromValue( bin.readValue() );
		} finally {
			if ( bin != null ) bin.close();
		}
	}
	
	/**
	 * Checks if this object is equal to <tt>obj</tt>.
	 * <p>
	 * This object is equal to another <tt>TorrentMetaInfo</tt> object which
	 * attributes are equal to this object's attributes.
	 */
	@Override
	public boolean equals ( Object obj ) {
		if ( !(obj instanceof TorrentMetaInfo) ) {
			return false;
		}
		
		TorrentMetaInfo tmi = (TorrentMetaInfo) obj;
		
		boolean dateEq = ( creationDate == null && tmi.creationDate == null )
			|| ( creationDate != null && creationDate.equals(tmi.creationDate)
		);
		
		return ( dateEq )
			&& ( announce.equals( tmi.announce ) )
		    && ( announceList.equals( tmi.announceList ) )
		    && ( comment.equals( tmi.comment ) )
		    && ( createdBy.equals( tmi.createdBy ) )
		    && ( info.equals( tmi.info ) );
	}
	
	@Override
	public int hashCode () {
		return announce.hashCode() ^ info.hashCode();
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder( "TorrentMetaInfo{{ " );

		sb.append( "CreationDate(" ).append( creationDate ).append( "), " );
		sb.append( "Comment(" ).append( comment ).append( "), " );
		sb.append( "Announce(" ).append( announce ).append( "), " );
		sb.append( "AnnounceList(" ).append( announceList ).append( "), " );
		sb.append( "Info(" ).append( info ).append( "), " );
		
		return sb.append( " }}" ).toString();
	}

}
