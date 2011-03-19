package es.darkhogg.torrent.tracker;

import java.net.InetAddress;
import java.util.Arrays;

import es.darkhogg.torrent.data.PeerId;
import es.darkhogg.torrent.data.Sha1Hash;

/**
 * Represents a request to a {@link Tracker}. This class is immutable, so it
 * can be reused.
 * <p>
 * Note that the only public way to instantiate this class is using the
 * {@link builder} method, which returns a {@link TrackerRequest.Builder}
 * object. See its documentation for more information.
 * 
 * @author Daniel Escoz
 * @version 1.0
 * @see TrackerRequest.Builder
 * @see Tacker
 */
public final class TrackerRequest {
	
	/**
	 * Hash of the torrent this announce is for
	 */
	private final Sha1Hash infoHash;
	
	/**
	 * Peer ID of the client
	 */
	private final PeerId peerId;
	
	/**
	 * Port used to receive incoming connections
	 */
	private final int port;
	
	/**
	 * Total amount of uploaded bytes
	 */
	private final long uploaded;
	
	/**
	 * Total amount of downloaded bytes
	 */
	private final long downloaded;
	
	/**
	 * Amount of bytes left to complete this torrent
	 */
	private final long left;
	
	/**
	 * Whether the request asks for a compact peer list
	 */
	private final Boolean compact;
	
	/**
	 * Whether the peer ID should be omitted in a non-compact request
	 */
	private final boolean noPeerId;
	
	/**
	 * Type of event of this request
	 */
	private final Event event;
	
	/**
	 * True IP address of this peer
	 */
	private final InetAddress ip;
	
	/**
	 * Number of peers wanted for this request
	 */
	private final int numWant;
	
	/**
	 * Unique key used for this peer
	 */
	private final String key;
	
	/**
	 * tracker ID returned by the tracker in previous announces
	 */
	private final byte[] trackerId;
	
	/**
	 * Constructs a  TrackerRequest using all the information needed.
	 * 
	 * @param infoHash Info hash of the torrent
	 * @param peerId Peer ID for this client
	 * @param port Port of this client
	 * @param uploaded Total bytes uploaded
	 * @param downloaded Total bytes downloaded
	 * @param left Bytes left to complete the torrent
	 * @param compact Compact peer list
	 * @param noPeerId No-Peer-Id in non-compact peer list
	 * @param event Event of this request
	 * @param ip True IP address of this machine
	 * @param numWant Number of peers wanted
	 * @param key Unique, private, identification key
	 * @param trackerId Tracker ID returned in previous announces
	 * @throws NullPointerException if <tt>infoHash</tt>, <tt>peerId</tt> or
	 *         <tt>event</tt> are <tt>null</tt>
	 * @throws IllegalArgumentException if <tt>port</tt> is not in the range
	 *         0-65535 or any of <tt>uploaded</tt>, <tt>downloaded</tt> or
	 *         <tt>left</tt> is less than 0.
	 */
	private TrackerRequest (
		Sha1Hash infoHash, PeerId peerId, int port, long uploaded,
		long downloaded, long left, Boolean compact, boolean noPeerId, 
		Event event, InetAddress ip, int numWant, String key,
		byte[] trackerId
	) {
		if ( infoHash == null | peerId == null | event == null ) {
			throw new NullPointerException();
		}
		
		if (
			port < 0 | port >= 65535 |
			uploaded < 0 | downloaded < 0 | left < 0
		) {
			throw new IllegalArgumentException();
		}
		
		this.infoHash = infoHash;
		this.peerId = peerId;
		this.port = port;
		this.uploaded = uploaded;
		this.downloaded = downloaded;
		this.left = left;
		this.compact = compact;
		this.noPeerId = noPeerId;
		this.event = event;
		this.ip = ip;
		this.numWant = numWant;
		this.key = key;
		this.trackerId = ( trackerId == null )
			? null
			: Arrays.copyOf( trackerId, trackerId.length );
	}
	
	/**
	 * @return the info SHA-1 hash set for this request.
	 */
	public Sha1Hash getInfoHash () {
		return infoHash;
	}
	
	/**
	 * @return the peer ID set for this request
	 */
	public PeerId getPeerId () {
		return peerId;
	}
	
	/**
	 * @return the advised local port set for this request
	 */
	public int getPort () {
		return port;
	}
	
	/**
	 * @return the total uploaded byte amount set for this request
	 */
	public long getBytesUploaded () {
		return uploaded;
	}
	
	/**
	 * @return the total downloaded byte amount set for this request
	 */
	public long getBytesDownloaded () {
		return downloaded;
	}
	
	/**
	 * @return the left byte amount set for this request
	 */
	public long getBytesLeft () {
		return left;
	}
	
	/**
	 * @return whether this request is compact or not, or <tt>null</tt> if it
	 *         doesn't matter
	 */
	public Boolean isCompact () {
		return compact;
	}
	
	/**
	 * @return <tt>false</tt> if this request is a no-peer-id, <tt>true</tt>
	 *         otherwise
	 */
	public boolean getWantPeerId () {
		return !noPeerId;
	}
	
	/**
	 * @return the type of event for this request
	 */
	public Event getEvent () {
		return event;
	}
	
	/**
	 * @return the true IP address of this machine, or <tt>null</tt> if not set
	 */
	public InetAddress getIp () {
		return ip;
	}
	
	/**
	 * @return the amount of peers to be returned in this request
	 */
	public int getNumWant () {
		return numWant;
	}
	
	/**
	 * @return the unique key for this client set in the request
	 */
	public String getKey () {
		return key;
	}
	
	/**
	 * @return the tracker ID set for this request
	 */
	public byte[] getTrackerId () {
		return ( trackerId==null )
			? null : Arrays.copyOf( trackerId, trackerId.length );
	}
	
	/**
	 * Creates a new {@link TrackerRequest.Builder} object with the default
	 * values and returns it.
	 * 
	 * @return A newly created <tt>TrackerRequest.Builder</tt> object
	 * @see TrackerRequest.Builder
	 */
	public static Builder builder () {
		return new Builder();
	}
	
	/**
	 * An object to build a {@link TrackerRequest} object.
	 * <p>
	 * To create an object of this class, use the {@link TrackerRequest#builder}
	 * method. After that, you can call methods in this class to set the values
	 * and then construct the <tt>TrackerRequest</tt> object using the
	 * {@link build} method.
	 * <p>
	 * This class is implemented using a fluent interface. Every method, except
	 * for the final <tt>build</tt>, returns the caller object, so you can chain
	 * statements:
	 * <pre>
	 *     // These simple lines of code...
	 *     TreackerRequest tr = TrackerRequest.builder()
	 *         .infoHash( h ).numWant( 33 ).compact( true )
	 *         .build();
	 *     
	 *     // ...are equivalent to this
	 *     TrackerRequest.Builder trb = TrackerRequest.builder();
	 *     trb.infoHash( h );
	 *     trb.numWant( 33 );
	 *     trb.compact( true );
	 *     TreackerRequest tr = trb.build();
	 * </pre>
	 * <p>
	 * This class provides no means to know the state of values and it doesn't
	 * check or copy them at all. When calling to the <tt>build</tt> method,
	 * the constructor from <tt>TrackerRequest</tt> takes care of all that.
	 * 
	 * @author Daniel Escoz
	 * @version 1.0
	 */
	public static class Builder {
		
		/**
		 * Hash of this builder
		 */
		private Sha1Hash infoHash = null;
		
		/**
		 * Peer Id of this builer
		 */
		private PeerId peerId = null;
		
		/**
		 * Port of this builder
		 */
		private int port = 6881;
		
		/**
		 * Bytes uploaded for this builder
		 */
		private long uploaded = 0;
		
		/**
		 * Bytes downloaded for this builder
		 */
		private long downloaded = 0;
		
		/**
		 * Bytes left for this builder
		 */
		private long left = 0;
		
		/**
		 * Compactness of this builder
		 */
		private Boolean compact = null;
		
		/**
		 * No-peer-id value for this builder
		 */
		private boolean noPeerId = true;
		
		/**
		 * Event of this builder
		 */
		private Event event = Event.REGULAR;
		
		/**
		 * True IP address of this builder
		 */
		private InetAddress ip = null;
		
		/**
		 * number of peers wanted of this builder
		 */
		private int numWant = 32;
		
		/**
		 * Unique key of this builder
		 */
		private String key = null;
		
		/**
		 * Tracker ID of this builder
		 */
		private byte[] trackerId = null;
		
		/**
		 * Constructs a default-initialized builder
		 */
		private Builder () {
			// Do nothing
		}
		
		/**
		 * Stores the given SHA-1 hash as the info hash of this builder.
		 * <p>
		 * If this method is never called, the info hash is <tt>null</tt> by
		 * default.
		 * 
		 * @param infoHash New info hash for this builder
		 * @return The <tt>this</tt> reference.
		 */
		public Builder infoHash ( Sha1Hash infoHash ) {
			this.infoHash = infoHash ;
			return this;
		}
		
		/**
		 * Stores the given byte array as the peer ID of this builder.
		 * <p>
		 * Note that the passed array is not copied, but stored as is, so
		 * changes in the original array will produce changes on this builder.
		 * <p>
		 * If this method is never called, the peer ID is <tt>null</tt> by
		 * default.
		 * 
		 * @param peerId New peer ID for this builder
		 * @return The <tt>this</tt> reference.
		 */
		public Builder peerId ( PeerId peerId ) {
			this.peerId = peerId;
			return this;
		}
		
		/**
		 * Stores the given integer as the port told to the tracker to use for
		 * incoming peer connections.
		 * <p>
		 * If this method is never called, the port is 6881 by default.
		 * 
		 * @param port New port for this builder
		 * @return The <tt>this</tt> reference.
		 */
		public Builder port ( int port ) {
			this.port = port;
			return this;
		}

		/**
		 * Stores the given long as the total number of bytes uploaded by
		 * this client.
		 * <p>
		 * If this method is never called, the uploaded amount is 0 by default.
		 * 
		 * @param uploaded Total amount of data uploaded
		 * @return The <tt>this</tt> reference.
		 */
		public Builder bytesUploaded ( long uploaded ) {
			this.uploaded = uploaded;
			return this;
		}
		
		/**
		 * Stores the given long as the total number of bytes downloaded by
		 * this client.
		 * <p>
		 * If this method is never called, the downloaded amount is 0 by default.
		 * 
		 * @param uploaded Total amount of data downloaded
		 * @return The <tt>this</tt> reference.
		 */
		public Builder bytesDownloaded ( long downloaded ) {
			this.downloaded = downloaded;
			return this;
		}
		
		/**
		 * Stores the given long as the total number of bytes this client needs
		 * to download to complete the torrent
		 * <p>
		 * If this method is never called, the port is 0 by default.
		 * 
		 * @param uploaded Total amount of data left to download
		 * @return The <tt>this</tt> reference.
		 */
		public Builder bytesLeft ( long left ) {
			this.left = left;
			return this;
		}
		
		/**
		 * Stores the given <tt>Boolean</tt> object as the compactness of the
		 * request. Setting this to <tt>null</tt> makes the tracker choose its
		 * preferred format.
		 * <p>
		 * If this method is never called, compact is <tt>null</tt> by default
		 * 
		 * @param compact New compactness setting for this builder
		 * @return The <tt>this</tt> reference.
		 */
		public Builder compact ( Boolean compact ) {
			this.compact = compact;
			return this;
		}
		
		/**
		 * Stores the given boolean as whether this builder wants the peer ID
		 * of the returned peers or not.
		 * <p>
		 * If this method is never called, this setting is <tt>false</tt> by
		 * default.
		 * 
		 * @param wantPeerId New value for this setting
		 * @return The <tt>this</tt> reference.
		 */
		public Builder wantPeerId ( boolean wantPeerId ) {
			this.noPeerId = !wantPeerId;
			return this;
		}
		
		/**
		 * Stores the passed {@link Event} as which event this request is for.
		 * <p>
		 * If this method is never called, the event is <tt>REGULAR</tt> by
		 * default.
		 * 
		 * @param event New event type for this builder
		 * @return The <tt>this</tt> reference.
		 */
		public Builder event ( Event event ) {
			this.event = event;
			return this;
		}

		/**
		 * Stores the passed {@link InetAddress} as the real IP of this client
		 * <p>
		 * If this method is never called, the real IP is <tt>null</tt>, which
		 * means it is not specified.
		 * 
		 * @param ip New real IP for this builder
		 * @return The <tt>this</tt> reference.
		 */
		public Builder ip ( InetAddress ip ) {
			this.ip = ip;
			return this;
		}
		
		/**
		 * Stores the passed integer as the number of peers the tracker is
		 * supposed to return.
		 * <p>
		 * If this method is never called, the wanted number of peers is 32 by
		 * default.
		 * 
		 * @param numWant New number of wanted peers for this builder
		 * @return The <tt>this</tt> reference
		 */
		public Builder numWant ( int numWant ) {
			this.numWant = numWant;
			return this;
		}
		
		/**
		 * Stores the passed <tt>String</tt> as the unique key for this client.
		 * <p>
		 * If this method is never called, the client key is unspecified
		 * 
		 * @param key New client key for this builder
		 * @return The <tt>this</tt> reference
		 */
		public Builder key ( String key ) {
			this.key = key;
			return this;
		}
		
		/**
		 * Stores the given byte array as the tracker ID of this builder.
		 * <p>
		 * Note that the passed array is not copied, but stored as is, so
		 * changes in the original array will produce changes on this builder.
		 * <p>
		 * If this method is never called, the tracker ID is <tt>null</tt> by
		 * default, meaning it's not specified.
		 * 
		 * @param trackerId New tracker ID for this builder
		 * @return The <tt>this</tt> reference
		 */
		public Builder trackerId ( byte[] trackerId ) {
			this.trackerId = trackerId;
			return this;
		}
		
		/**
		 * Builds a new {@link TrackerRequest} object using the values stored
		 * within this builder as its fields.
		 * <p>
		 * When this method is called, some checks and conversions happen:
		 * <ul>
		 * <li>If any of <tt>infoHash</tt>, <tt>peerId</tt> or <tt>event</tt>
		 * are <tt>null</tt>, a <tt>NullPointerException</tt> is thrown.
		 * <li>If the <tt>port</tt> is not in the range 0-65535, an
		 * <tt>IllegalArgumentException</tt> is thrown.
		 * <li>If <tt>bytesUploaded</tt>, <tt>bytesDownloaded</tt> or
		 * <tt>bytesLeft</tt> is less than 0, an
		 * <tt>IllegalArgumentException</tt> is thrown.
		 * <li>The stored <tt>peerId</tt> and <tt>trackerId</tt> fields are
		 * copied, so the returned object doesn't share any references.
		 * </ul>
		 * 
		 * @return A new <tt>TrackerRequest</tt> with the parameters stored
		 *         within this builder
		 */
		public TrackerRequest build () {
			return new TrackerRequest( infoHash, peerId, port, uploaded,
				downloaded, left, compact, noPeerId, event, ip, numWant,
				key, trackerId );
		}
	}
	
	/**
	 * Represents the type of an announce request.
	 * 
	 * @author Daniel Escoz
	 * @version 1.0
	 */
	public static enum Event {
		/**
		 * Event for the first announce
		 */
		STARTED( "started" ),
		
		/**
		 * Event to send when a torrent stops gracefully
		 */
		STOPPED( "stopped" ),
		
		/**
		 * Event for a completed torrent. Should only be sent once per torrent
		 */
		COMPLETED( "completed" ),
		
		/**
		 * Event for regular announces
		 */
		REGULAR( "" );
		
		/**
		 * String that must be sent to the tracker
		 */
		private final String string;
		
		/**
		 * Constructs an event with the given string
		 * 
		 * @param str The string of the event
		 */
		private Event ( String str ) {
			this.string = str;
		}
		
		/**
		 * Returns the string that must be sent to the tracker. This method
		 * returns <tt>""</tt> if no <i>event</i> parameter should be present.
		 * 
		 * @return The string assocciated with this event
		 */
		public String getEventString () {
			return string;
		}
	}

}
