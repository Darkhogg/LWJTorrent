package es.darkhogg.torrent.dht.krpc;

import java.util.Objects;

import es.darkhogg.torrent.bencode.Bencode;
import es.darkhogg.torrent.bencode.DictionaryValue;
import es.darkhogg.torrent.bencode.IntegerValue;
import es.darkhogg.torrent.bencode.ListValue;
import es.darkhogg.torrent.bencode.StringValue;
import es.darkhogg.torrent.bencode.Value;

/**
 * A class that can convert {@link Message}s to {@link Value}s and vice-versa.
 * TODO Implement this whole class.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class KrpcBencodeConverter {
	
	/**
	 * Converts the passed bencoded <tt>Value</tt> to a <tt>Message</tt> that
	 * represents it.
	 * 
	 * @param value
	 *            Value to convert
	 * @return Message converted from <tt>value</tt>
	 * @throw IllegalArgumentException if the given <tt>value</tt> is not a
	 *        valid KRPC message representation
	 */
	public static Message convertBencodeToMessage ( Value<?> value ) {
		try {
			StringValue tVal = Bencode.getChildValue( value, StringValue.class, "t" );
			StringValue yVal = Bencode.getChildValue( value, StringValue.class, "y" );
			
			byte[] trid = tVal.getValue();
			MessageType type = MessageType.fromString( yVal.getStringValue() );
			
			switch ( type ) {
			// Error message
				case ERROR: {
					ListValue eVal = Bencode.getChildValue( value, ListValue.class, "e" );
					if ( eVal.getSize() != 2 ) {
						throw new IllegalArgumentException();
					}
					int eCode = Bencode.getChildValue( eVal, IntegerValue.class, "0" ).getValue().intValue();
					String eMsg = Bencode.getChildValue( eVal, StringValue.class, "1" ).getStringValue();
					return new ErrorMessage( trid, eCode, eMsg );
				}
				
				// Query message
				case QUERY: {
					StringValue qVal = Bencode.getChildValue( value, StringValue.class, "q" );
					DictionaryValue aVal = Bencode.getChildValue( value, DictionaryValue.class, "a" );
					return new QueryMessage( trid, QueryType.fromString( qVal.getStringValue() ), aVal );
				}
				
				// Response message
				case RESPONSE: {
					DictionaryValue rVal = Bencode.getChildValue( value, DictionaryValue.class, "r" );
					return new ResponseMessage( trid, rVal );
				}
				
				// Invalid message
				default: {
					throw new IllegalArgumentException();
				}
			}
		} catch ( Exception e ) {
			// If something goes wrong, throw an IAE
			throw new IllegalArgumentException( e );
		}
	}
	
	/**
	 * Converts the passed <tt>Message</tt> to a bencoded <tt>Value</tt> that
	 * represents it.
	 * 
	 * @param msg
	 *            Message to convert
	 * @return Value converted from <tt>msg</tt>
	 */
	public static Value<?> convertMessageToBencode ( Message msg ) {
		Objects.requireNonNull( msg );
		
		if ( msg instanceof ErrorMessage ) {
			return convertErrorMessage( (ErrorMessage) msg );
			
		} else if ( msg instanceof QueryMessage ) {
			return convertQueryMessage( (QueryMessage) msg );
			
		} else if ( msg instanceof ResponseMessage ) {
			return convertResponseMessage( (ResponseMessage) msg );
			
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Returns a dictionary representing the passed message common fields
	 * 
	 * @param msg
	 *            Message to convert
	 * @return Converted message
	 */
	private static DictionaryValue convertCommonMessage ( Message msg ) {
		DictionaryValue dict = new DictionaryValue();
		dict.put( "t", new StringValue( msg.getTransaction() ) );
		dict.put( "y", new StringValue( msg.getType().getString() ) );
		return dict;
	}
	
	/**
	 * Returns a dictionary representing the passed error message
	 * 
	 * @param msg
	 *            Message to convert
	 * @return Converted message
	 */
	private static DictionaryValue convertErrorMessage ( ErrorMessage msg ) {
		ListValue errlist = new ListValue();
		errlist.add( new IntegerValue( Long.valueOf( msg.getErrorCode() ) ) );
		errlist.add( new StringValue( msg.getErrorMessage() ) );
		
		DictionaryValue dict = convertCommonMessage( msg );
		dict.put( "e", errlist );
		return dict;
	}
	
	/**
	 * Returns a dictionary representing the passed query message
	 * 
	 * @param msg
	 *            Message to convert
	 * @return Converted message
	 */
	private static DictionaryValue convertQueryMessage ( QueryMessage msg ) {
		DictionaryValue dict = convertCommonMessage( msg );
		dict.put( "q", new StringValue( msg.getQueryType().getString() ) );
		dict.put( "a", msg.getArguments() );
		return dict;
	}
	
	/**
	 * Returns a dictionary representing the passed response message
	 * 
	 * @param msg
	 *            Message to convert
	 * @return Converted message
	 */
	private static DictionaryValue convertResponseMessage ( ResponseMessage msg ) {
		DictionaryValue dict = convertCommonMessage( msg );
		dict.put( "r", msg.getReturnValues() );
		return dict;
	}
}
