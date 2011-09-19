package es.darkhogg.torrent.dht.krpc;

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
	 * @param benc
	 *            Value to convert
	 * @return Message converted from <tt>benc</tt>
	 */
	public Message convertBencodeToMessage ( Value<?> benc ) {
		return null;
	}
	
	/**
	 * Converts the passed <tt>Message</tt> to a bencoded <tt>Value</tt> that
	 * represents it.
	 * 
	 * @param msg
	 *            Message to convert
	 * @return Value converted from <tt>msg</tt>
	 */
	public Value<?> convertMessageToBencode ( Message msg ) {
		return null;
	}
	
}
