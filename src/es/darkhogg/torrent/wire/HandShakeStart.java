package es.darkhogg.torrent.wire;

import java.util.BitSet;

import es.darkhogg.torrent.data.Sha1Hash;

/**
 * Represents the part of the BitTorrent protocol handshake that contains the name of the protocol, the reserved bits
 * and the info hash.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class HandShakeStart extends BitTorrentMessage {

    /**
     * Name of the protocol used
     */
    private final String protocol;

    /**
     * Reserved bytes
     */
    private final BitSet flags;

    /**
     * Info hash
     */
    private final Sha1Hash hash;

    /**
     * Constructs this object using the given <tt>flags</tt> and info <tt>hash</tt>. The protocol name is set as
     * <tt>"BitTorrent protocol"</tt>.
     * 
     * @param flags Flags of the handshake
     * @param hash Info hash of the torrent requested
     */
    public HandShakeStart (BitSet flags, Sha1Hash hash) {
        this("BitTorrent protocol", flags, hash);
    }

    /**
     * Constructs this object using the given <tt>protocol</tt>, <tt>flags</tt> and info <tt>hash</tt>.
     * <p>
     * <i><b>Note:</b> The current bitTorrent version uses the protocol name <tt>"BitTorrent protocol"</tt> as the
     * protocol name in all situations. This method is given for message decoders, but should not be used by clients.
     * See {@link HandShakeStart#HandShakeStart(BitSet,Sha1Hash) HandShakeStart(BitSet,Sha1Hash)} for a constructor that
     * uses the default protocol name by default.
     * 
     * @param protocol Protocol name of the handshake.
     * @param flags Flags of the handshake
     * @param hash Info hash of the torrent requested
     */
    public HandShakeStart (String protocol, BitSet flags, Sha1Hash hash) {
        this.protocol = protocol;
        this.flags = (BitSet) flags.clone();
        this.hash = hash;
    }

    @Override
    public MessageType getMessageType () {
        return MessageType.HANDSHAKE_START;
    }

    /**
     * Returns the protocol name specified in this handshake
     * 
     * @return Protocol name of the handshake
     */
    public String getProtocolName () {
        return protocol;
    }

    /**
     * Returns a {@link BitSet} representing the reserved bits of the handshake.
     * 
     * @return The reserved bits
     */
    public BitSet getFlags () {
        return (BitSet) flags.clone();
    }

    /**
     * Returns the info hash specified in this handshake.
     * 
     * @return The info hash
     */
    public Sha1Hash getHash () {
        return hash;
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder("HandShakeStart");
        sb.append("{Type=Handshake-Start; ");
        sb.append("Protocol=").append(protocol);
        sb.append("; Flags=").append(flags);
        sb.append("; Hash=").append(hash);
        return sb.append("}").toString();
    }
}
