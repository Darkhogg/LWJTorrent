package es.darkhogg.torrent.dht.krpc;

import java.util.Objects;

/**
 * A KRPC message indicating an error condition.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class ErrorMessage extends Message {
	
	/** Generic error code */
	public final static int ERROR_GENERIC = 201;
	
	/** Server error code */
	public final static int ERROR_SERVER = 202;
	
	/** Protocol error code */
	public final static int ERROR_PROTOCOL = 203;
	
	/** Method unknown error code */
	public final static int ERROR_METHOD_UNKNOWN = 204;
	
	/** Error code */
	private final int code;
	
	/** Error message */
	private final String message;
	
	/**
	 * Constructs an <tt>ErrorMessage</tt> with all the necessary information
	 * 
	 * @param transaction
	 *            Transaction ID
	 * @param code
	 *            Error code
	 * @param message
	 *            Error message string
	 */
	public ErrorMessage ( byte[] transaction, int code, String message ) {
		super( transaction, MessageType.ERROR );
		this.code = code;
		this.message = Objects.requireNonNull( message );
	}
	
	/** @return The error code for this message */
	public int getErrorCode () {
		return code;
	}
	
	/** @return The error message string for this message */
	public String getErrorMessage () {
		return message;
	}
}
