package es.darkhogg.torrent.dht.krpc;

import java.util.Objects;

import es.darkhogg.torrent.bencode.Bencode;
import es.darkhogg.torrent.bencode.DictionaryValue;
import es.darkhogg.torrent.bencode.Value;

/**
 * A KRPC message returned after a query
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class ResponseMessage extends Message {
	
	/** Named return values */
	private final DictionaryValue returnValues;
	
	/**
	 * Constructs a <tt>ResponseMessage</tt> with the given transaction ID and
	 * return values. The <tt>returnValues</tt> argument is deeply copied to
	 * guarantee this object immutability.
	 * 
	 * @param transaction
	 *            Transaction ID for this message
	 * @param returnValues
	 *            Named return values of the response
	 * @throw NullPointerException if any of the arguments is <tt>null</tt>
	 */
	public ResponseMessage ( byte[] transaction, DictionaryValue returnValues ) {
		super( transaction, MessageType.RESPONSE );
		this.returnValues = Bencode.copyOf( Objects.requireNonNull( returnValues ) );
	}
	
	/**
	 * Returns a copy of the dictionary that holds all the named return values.
	 * Use this method only if you need the full dictionary. To get individual
	 * values by key, use {@link #getReturnValue(String)} or
	 * {@link #getTypedReturnValue(String, Class)}.
	 * 
	 * @return A copy of the named return values dictionary
	 */
	public DictionaryValue getReturnValues () {
		return Bencode.copyOf( returnValues );
	}
	
	/**
	 * Returns a copy of the value associated with the passed <tt>key</tt>, or
	 * <tt>null</tt> if it doesn't exist.
	 * 
	 * @param key
	 *            The key which value is to be returned
	 * @return The value associated with the <tt>key</tt>, or <tt>null</tt> if
	 *         the key doesn't exist
	 */
	public Value<?> getReturnValue ( String key ) {
		return Bencode.copyOf( returnValues.get( key ) );
	}
	
	/**
	 * Returns a copy of the value associated with the passed <tt>key</tt>, or
	 * <tt>null</tt> if it doesn't exist or is of the wrong type.
	 * 
	 * @param key
	 *            The key which value is to be returned
	 * @param type
	 *            The type of the returned value
	 * @return The value associated with the <tt>key</tt>, or <tt>null</tt> if
	 *         the key doesn't exist or is of the wrong type
	 */
	public <T extends Value<?>> T getTypedReturnValue ( String key, Class<T> type ) {
		return Bencode.getChildValue( returnValues, type, key );
	}
}
