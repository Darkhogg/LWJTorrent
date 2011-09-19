package es.darkhogg.torrent.dht.krpc;

import java.util.Objects;

import es.darkhogg.torrent.bencode.Bencode;
import es.darkhogg.torrent.bencode.DictionaryValue;
import es.darkhogg.torrent.bencode.Value;

public final class QueryMessage extends Message {
	
	private final QueryType type;
	
	private final DictionaryValue arguments;
	
	public QueryMessage ( byte[] transaction, QueryType type, DictionaryValue arguments ) {
		super( transaction, MessageType.QUERY );
		
		this.type = Objects.requireNonNull( type, "type" );
		this.arguments = Objects.requireNonNull( arguments, "argumnts" );
	}
	
	public QueryType getQueryType () {
		return type;
	}
	
	/**
	 * Returns a copy of the dictionary that holds all the named arguments. Use
	 * this method only if you need the full dictionary. To get individual
	 * arguments by name, use {@link #getArgument(String)} or
	 * {@link #getTypedArgument(String, Class)}.
	 * 
	 * @return A copy of the named arguments dictionary
	 */
	public DictionaryValue getArguments () {
		return Bencode.copyOf( arguments );
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
	public Value<?> getArgument ( String key ) {
		return Bencode.copyOf( arguments.get( key ) );
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
	public <T extends Value<?>> T getTypedArgument ( String key, Class<T> type ) {
		return Bencode.getChildValue( arguments, type, key );
	}
}
