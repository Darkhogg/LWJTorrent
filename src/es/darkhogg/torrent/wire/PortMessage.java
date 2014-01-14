package es.darkhogg.torrent.wire;

/**
 * Represents a <i>Port</i> message used by the BitTorrent protocol.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class PortMessage extends BitTorrentMessage {

    /**
     * Port passed with the message
     */
    private final int port;

    /**
     * Creates a message with the given port
     * 
     * @param port Port passed
     */
    public PortMessage (int port) {
        if (port < 0 | port > 65535) {
            throw new IllegalArgumentException();
        }

        this.port = port;
    }

    @Override
    public MessageType getMessageType () {
        return MessageType.PORT;
    }

    /**
     * Return the port passed with the message
     * 
     * @return Port of the message
     */
    public int getPort () {
        return port;
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder("HaveMessage");
        sb.append("{Type=").append("Have");
        sb.append("; Port=").append(port);
        return sb.append("}").toString();
    }
}
