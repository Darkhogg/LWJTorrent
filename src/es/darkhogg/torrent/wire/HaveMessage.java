package es.darkhogg.torrent.wire;

/**
 * Represents the <i>Have</i> message.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class HaveMessage extends BitTorrentMessage {

    /**
     * Index of the piece
     */
    private final int pieceIndex;

    /**
     * Creates a message with the given piece index
     * 
     * @param pieceIndex Piece index
     */
    public HaveMessage (int pieceIndex) {
        if (pieceIndex < 0) {
            throw new IllegalArgumentException();
        }

        this.pieceIndex = pieceIndex;
    }

    @Override
    public MessageType getMessageType () {
        return MessageType.HAVE;
    }

    /**
     * Return the piece index sent with this message
     * 
     * @return Piece index of this message
     */
    public int getPieceIndex () {
        return pieceIndex;
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder("HaveMessage");
        sb.append("{Type=").append("Have");
        sb.append("; PieceIndex=").append(pieceIndex);
        return sb.append("}").toString();
    }

}
