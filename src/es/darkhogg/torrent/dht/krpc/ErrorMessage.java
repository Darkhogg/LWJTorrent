package es.darkhogg.torrent.dht.krpc;


public final class ErrorMessage extends Message {
	
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
		this.message = message;
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
