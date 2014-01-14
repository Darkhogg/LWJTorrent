package es.darkhogg.torrent.wire;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;

import es.darkhogg.torrent.data.PeerId;
import es.darkhogg.torrent.data.Sha1Hash;

/**
 * A class used to decode instances of {@link BitTorrentMessage} from {@link ByteBuffer}s.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class BitTorrentMessageDecoder {

    /**
     * ISO-8859-1 charset
     */
    private final static Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /**
     * Private constructor
     */
    private BitTorrentMessageDecoder () {
    }

    /**
     * Decodes a handshake start (protocol name, reserved bits and info hash) from the passed <tt>buffer</tt>.
     * 
     * @param buffer Buffer containing at least enough bytes to read a full handshake minus the last 20 bytes
     * @return A <tt>HandShakeStart</tt> message representing the handshake read
     * @throws BufferUnderflowException if there are not enough bytes to read the handshake start
     */
    public static HandShakeStart decodeHandShakeStartFromBuffer (ByteBuffer buffer) {
        // Protocol name
        int pNameLen = (buffer.get()) & 0xFF;

        byte[] pNameBytes = new byte[pNameLen];
        buffer.get(pNameBytes);

        String pName = new String(pNameBytes, ISO_8859_1);

        // Reserved bits
        byte[] resBitsArr = new byte[8];
        buffer.get(resBitsArr);
        BitSet flags = new BitSet();
        for (int i = 0; i < 64; i++) {
            int bInd = 7 - (i / 8);
            int bMask = 1 << (i % 8);
            if ((resBitsArr[bInd] & bMask) != 0) {
                flags.set(i);
            }
        }

        // Hash
        byte[] hashBytes = new byte[20];
        buffer.get(hashBytes);
        Sha1Hash hash = new Sha1Hash(hashBytes);

        // Return
        return new HandShakeStart(pName, flags, hash);
    }

    /**
     * Decodes a handshake end (peer ID) from the passed <tt>buffer</tt>.
     * 
     * @param buffer Buffer containing at least 20 bytes
     * @return A <tt>HandShakeEnd</tt> message representing the handshake read
     * @throws BufferUnderflowException if there are not enough bytes to read the handshake end
     */
    public static HandShakeEnd decodeHandShakeEndFromBuffer (ByteBuffer buffer) {
        // Peer ID
        byte[] peerId = new byte[20];
        buffer.get(peerId);

        // Return
        return new HandShakeEnd(new PeerId(peerId));
    }

    /**
     * Decodes a regular message from the given <tt>buffer</tt>. This method just read 4 bytes as an <tt>int</tt> and
     * then calls {@link #decodeMessageFromBuffer(ByteBuffer,int)} with the read integer as the <tt>length</tt>.
     * <p>
     * If there are not enough bytes in this buffer, a {@link BufferUnderflowException} is thrown. If at least four
     * bytes are available, four bytes will be read. Whether more bytes are read depends on the specification of the
     * <tt>decodeMessageFromBuffer(buffer,int)</tt> method. If there are less than four bytes, the number of bytes
     * actually read depends on the specification of the {@link ByteBuffer#getInt} method.
     * 
     * @param buffer Buffer containing a full BitTorrent message
     * @return A <tt>BitTorrentMessage</tt> representing the read message
     * @throws BufferUnderflowException if there aren't enough bytes to read a full message.
     * @throws IllegalArgumentException if the message trying to be decoded is unrecognized.
     */
    public static BitTorrentMessage decodeMessageFromBuffer (ByteBuffer buffer) {
        int length = buffer.getInt();
        return decodeMessageFromBuffer(buffer, length);
    }

    /**
     * Decodes a regular message of <tt>length</tt> bytes from the given <tt>buffer</tt>.
     * <p>
     * If there are not <tt>length</tt> bytes available, this method immediately throws a
     * {@link java.nio.BufferUnderflowException}, and no bytes from the buffer are consumed. In any other case, exactly
     * <tt>length</tt> bytes are consumed.
     * 
     * @param buffer The buffer containing the message
     * @param length The length, in bytes, of the message
     * @return A <tt>BitTorrentMessage</tt> representing the read message
     * @throws BufferUnderflowException if there aren't enough bytes to read a full message.
     * @throws IllegalArgumentException if the message trying to be decoded is unrecognized.
     */
    public static BitTorrentMessage decodeMessageFromBuffer (ByteBuffer buffer, int length) {
        // Avoid negative length
        if (length < 0) {
            throw new IllegalArgumentException();
        }

        // If the length is 0, it is a keep-alive
        if (length == 0) {
            return EmptyMessage.KEEP_ALIVE;
        }

        // If there are not enough bytes...
        if (buffer.remaining() < length) {
            throw new BufferUnderflowException();
        }

        // Get the message ID and type
        int msgId = (buffer.get()) & 0xFF;
        MessageType mt = MessageType.forId(msgId);

        // Decode the specific type
        switch (mt) {
            case CHOKE:
                return EmptyMessage.CHOKE;

            case UNCHOKE:
                return EmptyMessage.UNCHOKE;

            case INTERESTED:
                return EmptyMessage.INTERESTED;

            case UNINTERESTED:
                return EmptyMessage.UNINTERESTED;

            case HAVE:
                return decodeHave(buffer);

            case BITFIELD:
                return decodeBitField(buffer, length - 4);

            case REQUEST:
                return decodeRequest(buffer);

            case PIECE:
                return decodePiece(buffer, length - 9);

            case CANCEL:
                return decodeCancel(buffer);

            case PORT:
                return decodePort(buffer);

                // In case of an unrecognized message...
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Decodes a <i>Have</i> message from the given <tt>buffer</tt>
     * 
     * @param buffer Buffer containing the message
     * @return Decoded message
     */
    private static BitTorrentMessage decodeHave (ByteBuffer buffer) {
        return new HaveMessage(buffer.getInt());
    }

    /**
     * Decodes a <i>BitField</i> message from the given <tt>buffer</tt>
     * <p>
     * Note that the <tt>length</tt> argument is the length of the bitfield, and not the length of the message payload.
     * 
     * @param buffer Buffer containing the message
     * @param length Length, in bytes, of the bitfield
     * @return Decoded message
     */
    private static BitTorrentMessage decodeBitField (ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(length);

        int bitlen = length * 8;
        BitSet bits = new BitSet();
        for (int i = 0; i < bitlen; i++) {
            int bInd = i / 8;
            int bMask = 1 << (7 - (i % 8));
            if ((bytes[bInd] & bMask) != 0) {
                bits.set(i);
            }
        }

        return new BitFieldMessage(bits);
    }

    /**
     * Decodes a <i>Request</i> message from the given <tt>buffer</tt>
     * 
     * @param buffer Buffer containing the message
     * @return Decoded message
     */
    private static BitTorrentMessage decodeRequest (ByteBuffer buffer) {
        return new CancelMessage(buffer.getInt(), buffer.getInt(), buffer.getInt());
    }

    /**
     * Decodes a <i>Piece</i> message from the given <tt>buffer</tt>
     * <p>
     * Note that the <tt>length</tt> argument is the length of the block, and not the length of the message payload.
     * 
     * @param buffer Buffer containing the message
     * @param length Length, in bytes, of the piece
     * @return Decoded message
     */
    private static BitTorrentMessage decodePiece (ByteBuffer buffer, int length) {
        return new PieceMessage(buffer.getInt(), buffer.getInt(), buffer, length);
    }

    /**
     * Decodes a <i>Cancel</i> message from the given <tt>buffer</tt>
     * 
     * @param buffer Buffer containing the message
     * @return Decoded message
     */
    private static BitTorrentMessage decodeCancel (ByteBuffer buffer) {
        return new CancelMessage(buffer.getInt(), buffer.getInt(), buffer.getInt());
    }

    /**
     * Decodes a <i>Port</i> message from the given <tt>buffer</tt>
     * 
     * @param buffer Buffer containing the message
     * @return Decoded message
     */
    private static BitTorrentMessage decodePort (ByteBuffer buffer) {
        return new PortMessage((buffer.getShort()) & 0xFFFF);
    }
}
