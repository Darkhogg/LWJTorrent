package es.darkhogg.wire;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the type of a message sent using the BitTorrent protocol.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public enum MessageType {
	KEEP_ALIVE		( null,	"KeepAlive" ),
	HANDSHAKE_START	( null, "Handshake-Start" ),
	HANDSHAKE_END	( null,	"Handshacke-End"),
	CHOKE			( 0,	"Choke" ),
	UNCHOKE			( 1,	"Unchoke" ),
	INTERESTED		( 2,	"Interested" ),
	UNINTERESTED	( 3,	"NotInterested" ),
	HAVE			( 5,	"Have" ),
	BITFIELD		( 4,	"BitField" ),
	REQUEST			( 6,	"Request" ),
	PIECE			( 7,	"Piece" ),
	CANCEL			( 8,	"Cancel" ),
	PORT			( 9,	"Port" );
	
	/**
	 * Contains a mapping from ID's to types, to accelerate {@link forId} and
	 * turn it to constant-time
	 */
	private static final Map<Integer,MessageType> idsToTypes;
	static {
		Map<Integer,MessageType> map = new HashMap<Integer,MessageType>();
		
		for ( MessageType mt : values() ) {
			map.put( Integer.valueOf( mt.getId() ), mt );
		}
		
		idsToTypes = Collections.unmodifiableMap( map );
	}
	
	/**
	 * ID number of this message
	 */
	private final Integer id;
	
	/**
	 * Name of the message type
	 */
	private final String name;
	
	/**
	 * Creates a new type with the given ID number
	 * 
	 * @param id ID number
	 */
	private MessageType ( Integer id, String name ) {
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Returns the ID number of this message type. If this message type doesn't
	 * have an associated ID, this method returns -1
	 * 
	 * @return ID number of this type
	 */
	public int getId () {
		return id==null ? -1 : id.intValue();
	}
	
	/**
	 * Returns the <tt>MessageType</tt> object that has the same ID number as
	 * the <tt>id</tt> passed argument. If no such object exist, this method
	 * return <tt>null</tt>.
	 * <p>
	 * An ID of -1 yields a <tt>null</tt> result, even if some of the values
	 * of this class return -1 as their ID.
	 * 
	 * @param id The ID number
	 * @return A type with the given Id, or <tt>null</tt>
	 */
	public static MessageType forId ( int id ) {
		Integer oid = Integer.valueOf( id );
		return idsToTypes.get( oid );
	}

	@Override
	public String toString () {
		return name;
	}
}
