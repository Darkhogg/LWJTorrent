package es.darkhogg.torrent.dht.krpc;

import java.util.Arrays;
import java.util.Objects;

public abstract class Message {

    /** Transaction ID */
    private final byte[] transaction;

    /** Type of the message */
    private final MessageType type;

    /**
     * Constructs a new <tt>Message</tt> using the given transaction and type.
     * 
     * @param transaction Transaction ID for this message
     * @param type Message type of this message
     * @throw NullPointerException if any of the arguments is <tt>null</tt>
     */
    /* package */Message (byte[] transaction, MessageType type) {
        this.transaction = Arrays.copyOf(Objects.requireNonNull(transaction, "transaction"), transaction.length);
        this.type = Objects.requireNonNull(type, "type");
    }

    /** @return The transaction ID of this message */
    public final byte[] getTransaction () {
        return Arrays.copyOf(transaction, transaction.length);
    }

    /** @return The type of this message */
    public final MessageType getType () {
        return type;
    }
}
