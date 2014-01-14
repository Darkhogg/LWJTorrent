package es.darkhogg.torrent.wire;

import java.io.Closeable;
import java.io.IOException;
import java.util.BitSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import es.darkhogg.torrent.data.PeerId;
import es.darkhogg.torrent.data.Sha1Hash;

/**
 * A further abstraction over {@link PeerConnection}. This class maintains the state of the connection and automatically
 * process it in a background thread, firing events on its listeners when a message arrives, a message is sent, or the
 * connection closes.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class PeerSession implements Closeable {

    // --- Misc Fields ---

    /** Wrapped connection */
    /* package */final PeerConnection connection;

    /** Executor used for event dispatching */
    /* package */final ExecutorService eventExecutor;

    /** Whether the event executor must be shutdown automatically */
    /* package */final boolean shutdownEventExecutor;

    /** Executor used for connection execution */
    /* package */final ExecutorService connectionExecutor;

    /** Whether the connection executor must be shutdown automatically */
    /* package */final boolean shutdownConnectionExecutor;

    /** Queue for outgoing messages */
    BlockingQueue<BitTorrentMessage> outQueue = new LinkedBlockingQueue<BitTorrentMessage>();

    /** Peer listeners associated */
    Set<PeerListener> listeners = new CopyOnWriteArraySet<PeerListener>();

    // --- State ---

    /** Peer ID sent in the initial handshake */
    private volatile PeerId localPeerId = null;

    /** Peer ID received in the initial handshake */
    private volatile PeerId remotePeerId = null;

    /** Protocol name sent in the initial handshake */
    private volatile String localProtocol = null;

    /** * Protocol name received in the initial handshake */
    private volatile String remoteProtocol = null;

    /** Info Hash sent in the initial handshake */
    private volatile Sha1Hash localHash = null;

    /** Info Hash received in the initial handshake */
    private volatile Sha1Hash remoteHash = null;

    /** Reserved bits sent in the initial handshake */
    private final BitSet localFlags = new BitSet();

    /** Reserved bits received in the initial handshake */
    private final BitSet remoteFlags = new BitSet();

    /** Parts that the local end claims to have */
    private final BitSet localClaimedPieces = new BitSet();

    /** Parts that the remote end claims to have */
    private final BitSet remoteClaimedPieces = new BitSet();

    /** Whether the local end is choking */
    private volatile boolean localChoking = true;

    /** Whether the remote end is choking */
    private volatile boolean remoteChoking = true;

    /** Whether the local end is interested */
    private volatile boolean localInterested = false;

    /** Whether the remote end is interested */
    private volatile boolean remoteInterested = false;

    /** Whether the <i>HandShake Start</i> has been sent by the local end */
    private volatile boolean localHandShakeStarted = false;

    /** Whether the <i>HandShake Start</i> has been received from the remote end */
    private volatile boolean localHandShakeFinished = false;

    /** Whether the <i>HandShake End</i> has been sent by the local end */
    private volatile boolean remoteHandShakeStarted = false;

    /** Whether the <i>HandShake End</i> has been received from the remote end */
    private volatile boolean remoteHandShakeFinished = false;

    // --- Constructor ---

    private PeerSession (PeerConnection conn, ExecutorService evExec, boolean sdEv, ExecutorService clExec, boolean sdCl)
    {
        this.connection = Objects.requireNonNull(conn);
        this.eventExecutor = Objects.requireNonNull(evExec);
        this.shutdownEventExecutor = sdEv;
        this.connectionExecutor = Objects.requireNonNull(clExec);
        this.shutdownConnectionExecutor = sdCl;

        connectionExecutor.submit(new ReceiveThread());
        connectionExecutor.submit(new SendThread());
    }

    // --- State Methods ---

    /**
     * Returns the peer ID sent with the handshake end, or <tt>null</tt> if it has not been sent yet
     * 
     * @return The local peer ID, or <tt>null</tt> if it hasn't been sent
     */
    public PeerId getLocalPeerId () {
        return localPeerId;
    }

    /**
     * Returns the peer ID received with the handshake end, or <tt>null</tt> if it has not been received yet.
     * 
     * @return The remote peer ID, or <tt>null</tt> if it hasn't been received
     */
    public PeerId getRemotePeerId () {
        return remotePeerId;
    }

    /**
     * Returns the protocol name sent with the handshake start, or <tt>null</tt> if it has not been sent yet.
     * 
     * @return The local protocol name, or <tt>null</tt> if it hasn't been sent
     */
    public String getLocalProtocol () {
        return localProtocol;
    }

    /**
     * Returns the protocol name received with the handshake start, or <tt>null</tt> if it has not been received yet.
     * 
     * @return The remote protocol name, or <tt>null</tt> if it hasn't been received
     */
    public String getRemoteProtocol () {
        return remoteProtocol;
    }

    /**
     * Returns the info hash sent with the handshake start, or <tt>null</tt> if it has not been sent yet.
     * 
     * @return The local info hash, or <tt>null</tt> if it hasn't been sent
     */
    public Sha1Hash getLocalHash () {
        return localHash;
    }

    /**
     * Returns the info hash received with the handshake start, or <tt>null</tt> if it has not been received yet.
     * 
     * @return The remote info hash, or <tt>null</tt> if it hasn't been received
     */
    public Sha1Hash getRemoteHash () {
        return remoteHash;
    }

    /**
     * Returns the reserved bits sent with the handshake start, or <tt>null</tt> if it hasn't been sent yet.
     * 
     * @return The local reserved bits, or <tt>null</tt> if it hasn't been sent
     */
    public BitSet getLocalFlags () {
        return localFlags;
    }

    /**
     * Returns the reserved bits received with the handshake start, or <tt>null</tt> if it hasn't been received yet.
     * 
     * @return The remote reserved bits, or <tt>null</tt> if it hasn't been received
     */
    public BitSet getRemoteFlags () {
        return remoteFlags;
    }

    /**
     * Returns a bit set which set bits indicate that the local end of this connection has claimed to have that part,
     * either using the <i>BitField</i> message or using subsequent <i>Have</i> messages.
     * <p>
     * The returned object is a copy and will not change if new messages are sent.
     * 
     * @return The claimed local parts
     */
    public BitSet getLocalClaimedPieces () {
        return localClaimedPieces;
    }

    /**
     * Returns a bit set which set bits indicate that the remote end of this connection has claimed to have that part,
     * either using the <i>BitField</i> message or using subsequent <i>Have</i> messages.
     * <p>
     * The returned object is a copy and will not change if new messages are received.
     * 
     * @return The claimed remote parts
     */
    public BitSet getRemoteClaimedPieces () {
        return remoteClaimedPieces;
    }

    /**
     * Returns whether the local end of this connection is choking.
     * 
     * @return Whether the local end is choking
     */
    public boolean isLocalChoking () {
        return localChoking;
    }

    /**
     * Returns whether the remote end of this connection is choking.
     * 
     * @return Whether the remote end is choking
     */
    public boolean isRemoteChoking () {
        return remoteChoking;
    }

    /**
     * Returns whether the local end of this connection is interested in the remote end.
     * 
     * @return Whether the local end is interested
     */
    public boolean isLocalInterested () {
        return localInterested;
    }

    /**
     * Returns whether the remote end of this connection is interested in the local end.
     * 
     * @return Whether the remote end is interested
     */
    public boolean isRemoteInterested () {
        return remoteInterested;
    }

    /**
     * Returns <tt>true</tt> if and only if a <i>HandShake Start</i> message has been sent using both the
     * {@link #sendMessage} and {@link #processOutputMessage} methods.
     * 
     * @return Whether the handshake start has been sent
     */
    public boolean isLocalHandShakeStarted () {
        return localHandShakeStarted;
    }

    /**
     * Returns <tt>true</tt> if and only if a <i>HandShake Start</i> message has been received using the
     * {@link #processInputMessage} method. Whether the message has been actually given to the event handlers is not
     * relevant to the returned value of this method.
     * 
     * @return Whether the handshake start has been received
     */
    public boolean isRemoteHandShakeStarted () {
        return remoteHandShakeStarted;
    }

    /**
     * Returns <tt>true</tt> if and only if a <i>HandShake End</i> message has been sent using both the
     * {@link #sendMessage} and {@link #processOutputMessage} methods.
     * 
     * @return Whether the handshake end has been sent
     */
    public boolean isLocalHandShakeFinished () {
        return localHandShakeFinished;
    }

    /**
     * Returns <tt>true</tt> if and only if a <i>HandShake End</i> message has been received using the
     * {@link #processInputMessage} method. Whether the message has been actually given to the event handlers is not
     * relevant to the returned value of this method.
     * 
     * @return Whether the handshake end has been received
     */
    public boolean isRemoteHandShakeFinished () {
        return remoteHandShakeFinished;
    }

    // --- Message Sending ---

    /**
     * Adds a message to this session internal queue so it is sent as soon as possible.
     * 
     * @param message Message to be sent.
     * @return <tt>true</tt> if the message was correctly queued, <tt>false</tt> otherwise
     * @throws NullPointerException if <tt>message</tt> is <tt>null</tt>
     */
    public boolean sendMessage (BitTorrentMessage message) {
        return outQueue.offer(message);
    }

    // --- Listener Management ---

    /**
     * Adds a <i>peer listener</i> that will receive events when messages are receive or sent, or when this session is
     * closed.
     * 
     * @param pl The listener to be added
     * @throws NullPointerException if <tt>pl</tt> is <tt>null</tt>
     */
    public void addPeerListener (PeerListener pl) {
        listeners.add(Objects.requireNonNull(pl));
    }

    /**
     * Removes a <i>peer listener</i> so it won't receive further events.
     * 
     * @param pl The listener to remove
     */
    public void removePeerListener (PeerListener pl) {
        listeners.remove(pl);
    }

    // --- Closing Methods ---

    @Override
    public void close () {
        synchronized (connection) {
            if (!connection.isClosed()) {
                connection.close();
                outQueue.offer(EmptyMessage.KEEP_ALIVE);
                fireCloseEvent();
            }
        }
    }

    // --- To String ---

    @Override
    public final String toString () {
        StringBuilder sb = new StringBuilder("PeerSession{");

        sb.append("Connection=").append(connection);

        return sb.append("}").toString();
    }

    /**
     * Tests whether this session is closed.
     * 
     * @return <tt>true</tt> if this session is closed, <tt>false</tt> otherwise
     */
    public boolean isClosed () {
        return connection.isClosed();
    }

    // --- Factory Methods ---

    /**
     * Returns a new <tt>PeerSession</tt> that used the given arguments to operate.
     * <p>
     * The connection passed as <tt>conn</tt> is used to receive and send messages in parallel threads.
     * <p>
     * The executor passed as <tt>eventExec</tt> is used to execute events. Each event is fired in all listeners as a
     * single task, guaranteeing non-concurrent execution of a single events. Multiple events are submitted as separate
     * tasks. It is recommended that a <i>single-thread executor</i> is used as the event dispatch executor, so no two
     * events are ever fired concurrently. The passed executor will <i>not</i> be auto-shutdown on session termination.
     * If <tt>null</tt> is pased, a {@link java.util.concurrent.Executors#newSingleThreadExecutor single-thread
     * executor} is used, which <i>will</i> be auto-shutdown on termination.
     * <p>
     * The executor passed as <tt>connExec</tt> is used to execute the connection controllers. At least two threads must
     * be available in this executor in order for this session to correctly execute. It is recommended that a <i>cached
     * thread pool</i> is used as the connection controller executor, so there is always room for new connections. The
     * passed executor will <i>not</i> be auto-shutdown on session termination. If <tt>null</tt> is pased, a
     * {@link java.util.concurrent.Executors#newCachedThreadPool cached thread pool} is used, which <i>will</i> be
     * auto-shutdown on termination.
     * 
     * @param conn The connection wrapped on this session
     * @param eventExec Executor that will execute events
     * @param connExec Executor that will execute the connection controllers
     * @return The newly created peer session
     */
    public static PeerSession newSession (PeerConnection conn, ExecutorService eventExec, ExecutorService connExec) {
        boolean shutdownEvent = false;
        ExecutorService ee = eventExec;
        ExecutorService ce = connExec;

        if (ee == null) {
            ee = Executors.newSingleThreadExecutor();
            shutdownEvent = true;
        }

        boolean shutdownConn = false;
        if (ce == null) {
            ce = Executors.newFixedThreadPool(2);
            shutdownConn = true;
        }

        return new PeerSession(conn, ee, shutdownEvent, ce, shutdownConn);
    }

    // --- Message Processing ---

    /**
     * Process a given message that has just been received
     * 
     * @param msg Message to process
     */
    @SuppressWarnings("incomplete-switch")
    /* package */void processInputMessage (BitTorrentMessage msg) {
        assert msg != null;

        switch (msg.getMessageType()) {
            case HANDSHAKE_START:
                HandShakeStart hss = (HandShakeStart) msg;
                remoteFlags.or(hss.getFlags());
                remoteHash = hss.getHash();
                remoteProtocol = hss.getProtocolName();
                remoteHandShakeStarted = true;
            break;

            case HANDSHAKE_END:
                HandShakeEnd hse = (HandShakeEnd) msg;
                remotePeerId = hse.getPeerId();
                remoteHandShakeFinished = true;
            break;

            case CHOKE:
                remoteChoking = true;
            break;

            case UNCHOKE:
                remoteChoking = false;
            break;

            case INTERESTED:
                remoteInterested = true;
            break;

            case UNINTERESTED:
                remoteInterested = false;
            break;

            case HAVE:
                remoteClaimedPieces.set(((HaveMessage) msg).getPieceIndex());
            break;

            case BITFIELD:
                remoteClaimedPieces.or(((BitFieldMessage) msg).getBitField());
            break;
        }
    }

    /**
     * Process a given message that has just been sent
     * 
     * @param msg Message to process
     */
    @SuppressWarnings("incomplete-switch")
    /* package */void processOutputMessage (BitTorrentMessage msg) {
        assert msg != null;

        switch (msg.getMessageType()) {
            case HANDSHAKE_START:
                HandShakeStart hss = (HandShakeStart) msg;
                localFlags.or(hss.getFlags());
                localHash = hss.getHash();
                localProtocol = hss.getProtocolName();
                localHandShakeStarted = true;
            break;

            case HANDSHAKE_END:
                HandShakeEnd hse = (HandShakeEnd) msg;
                localPeerId = hse.getPeerId();
                localHandShakeFinished = true;
            break;

            case CHOKE:
                localChoking = true;
            break;

            case UNCHOKE:
                localChoking = false;
            break;

            case INTERESTED:
                localInterested = true;
            break;

            case UNINTERESTED:
                localInterested = false;
            break;

            case HAVE:
                localClaimedPieces.set(((HaveMessage) msg).getPieceIndex());
            break;

            case BITFIELD:
                localClaimedPieces.or(((BitFieldMessage) msg).getBitField());
            break;
        }
    }

    // --- Event Firing Methods ---

    /**
     * Fires a <i>message received</i> event.
     * 
     * @param msg Message received
     */
    /* package */void fireReceiveEvent (BitTorrentMessage msg) {
        eventExecutor.submit(new ReceiveFirer(msg));
    }

    /**
     * Fires a <i>message sent</i> event.
     * 
     * @param msg Message sent
     */
    /* package */void fireSendEvent (BitTorrentMessage msg) {
        eventExecutor.submit(new SendFirer(msg));
    }

    /**
     * Fires a <i>connection closed</i> event.
     */
    private void fireCloseEvent () {
        eventExecutor.submit(new CloseFirer());
    }

    // --- Background Processing Classes ---

    /**
     * A <tt>Runnable</tt> that is in charge of processing the input channel
     * 
     * @author Daniel Escoz
     * @version 1.0
     */
    /* package */final class ReceiveThread implements Runnable {

        @Override
        public void run () {
            try {
                int messages = 0;
                while (!isClosed()) {
                    // Receive a message
                    BitTorrentMessage msg = null;
                    if (messages == 0) {
                        msg = connection.receiveHandShakeStart();
                    } else if (messages == 1) {
                        msg = connection.receiveHandShakeEnd();
                    } else {
                        msg = connection.receiveMessage();
                    }
                    // Process it
                    if (msg != null) {
                        processInputMessage(msg);
                        fireReceiveEvent(msg);
                        messages++;
                    }
                }
            } catch (IOException e) {
                // Nothing specific here
            } finally {
                close();
            }
        }

    }

    /**
     * A <tt>Runnable</tt> that is in charge of processing the output channel
     * 
     * @author Daniel Escoz
     * @version 1.0
     */
    /* package */final class SendThread implements Runnable {

        @Override
        public void run () {
            try {
                while (!isClosed()) {
                    BitTorrentMessage msg = outQueue.poll(1, TimeUnit.MINUTES);
                    if (msg != null) {
                        connection.sendMessage(msg);
                        fireSendEvent(msg);
                    }
                }
            } catch (Exception e) {
                // Nothing specific here
            } finally {
                close();
            }
        }
    }

    // --- Event Firing Classes ---

    /**
     * A <tt>Runnable</tt> that is in charge of firing <i>message received</i> events
     * 
     * @author Daniel Escoz
     * @version 1.0
     */
    /* package */final class ReceiveFirer implements Runnable {

        private final BitTorrentMessage message;

        /**
         * @param message Message that was received
         */
        /* package */ReceiveFirer (BitTorrentMessage message) {
            this.message = message;
        }

        @Override
        public void run () {
            processInputMessage(message);
            for (PeerListener pl : listeners) {
                pl.onReceive(PeerSession.this, message);
            }
        }
    }

    /**
     * A <tt>Runnable</tt> that is in charge of firing <i>message sent</i> events
     * 
     * @author Daniel Escoz
     * @version 1.0
     */
    /* package */final class SendFirer implements Runnable {

        private final BitTorrentMessage message;

        /**
         * @param message Message that was sent
         */
        /* package */SendFirer (BitTorrentMessage message) {
            this.message = message;
        }

        @Override
        public void run () {
            processOutputMessage(message);
            for (PeerListener pl : listeners) {
                pl.onSend(PeerSession.this, message);
            }
        }
    }

    /**
     * A <tt>Runnable</tt> that is in charge of firing <i>peer closed</i> events
     * 
     * @author Daniel Escoz
     * @version 1.0
     */
    /* package */final class CloseFirer implements Runnable {

        @Override
        public void run () {
            for (PeerListener pl : listeners) {
                pl.onClose(PeerSession.this);
                if (shutdownEventExecutor) {
                    eventExecutor.shutdown();
                }
                if (shutdownConnectionExecutor) {
                    connectionExecutor.shutdown();
                }
            }
        }
    }
}
