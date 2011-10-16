package es.darkhogg.torrent.wire;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class PeerSessionPool implements Closeable {
	
	private static final int EVENT_EXECUTOR_SHUTDOWN_TIME = 250;
	private static final int MISC_EXECUTOR_SHUTDOWN_TIME = 50;
	
	/** Internal collection containing all peers */
	private final Set<PeerSession> peers = Collections.synchronizedSet( new HashSet<PeerSession>() );
	
	/** Internal executor for events */
	private final ExecutorService eventExecutor = Executors.newSingleThreadExecutor( new PoolThreadFactory(
		"EventThread" ) );
	
	/** Internal executor for clients */
	private final ExecutorService miscExecutor = Executors.newCachedThreadPool( new PoolThreadFactory( "MiscThread" ) );
	
	/** Listeners of this session pool */
	private final Set<PeerListener> listeners = new CopyOnWriteArraySet<>();
	
	/** Whether this pool is closed */
	private volatile boolean closed = false;
	
	// --- Constructors ---
	
	/**
	 * Constructs a new <tt>PeerSessionPool</tt> with no <tt>PeerSession</tt>s
	 * or <tt>PeerListener</tt>s attached
	 */
	public PeerSessionPool () {
		miscExecutor.submit( new BackgroundThread() );
	}
	
	// --- Listener Methods ---
	
	/**
	 * Adds a <i>peer listener</i> that will receive events when any of the
	 * <tt>PeerSession</tt>s of this pool receives a message, sends a message or
	 * is closed. The passed <tt>listener</tt> will be added to all current and
	 * future sessions managed by this pool.
	 * 
	 * @param listener
	 *            Listener to add to this pool
	 */
	public void addPeerListener ( PeerListener listener ) {
		synchronized ( peers ) {
			listeners.add( listener );
			for ( PeerSession peer : peers ) {
				peer.addPeerListener( listener );
			}
		}
	}
	
	/**
	 * Removes a <i>peer listener</i> from this pool. The passed
	 * <tt>listener</tt> will be removed from all current sessions managed by
	 * this pool, and won't be added to new sessions.
	 * 
	 * @param listener
	 *            Listener to remove to this pool
	 */
	public void removePeerListener ( PeerListener listener ) {
		synchronized ( peers ) {
			listeners.remove( listener );
			for ( PeerSession peer : peers ) {
				peer.removePeerListener( listener );
			}
		}
	}
	
	// --- Factory Methods ---
	
	/**
	 * Creates a new {@link PeerSession} using this pool's internal executors
	 * and listeners.
	 * 
	 * @param connection
	 *            Connection to use for the session
	 * @return New session using the specified <tt>connection</tt>
	 */
	public PeerSession newSession ( PeerConnection connection ) {
		PeerSession peer = PeerSession.newSession( connection, eventExecutor, miscExecutor );
		synchronized ( peers ) {
			peers.add( peer );
			for ( PeerListener listener : listeners ) {
				peer.addPeerListener( listener );
			}
		}
		return peer;
	}
	
	// --- Closing Methods ---
	
	@Override
	public void close () {
		boolean exec = false;
		synchronized ( this ) {
			if ( !closed ) {
				closed = true;
				exec = true;
			}
		}
		if ( exec ) {
			for ( PeerSession peer : peers ) {
				peer.close();
			}
			
			eventExecutor.shutdown();
			miscExecutor.shutdown();
			
			try {
				miscExecutor.awaitTermination( MISC_EXECUTOR_SHUTDOWN_TIME, TimeUnit.MILLISECONDS );
				eventExecutor.awaitTermination( EVENT_EXECUTOR_SHUTDOWN_TIME, TimeUnit.MILLISECONDS );
			} catch ( InterruptedException e ) {
				// Do nothing -> terminate method
			}
			
			if ( !eventExecutor.isTerminated() ) {
				eventExecutor.shutdownNow();
			}
			
			if ( !miscExecutor.isTerminated() ) {
				miscExecutor.shutdownNow();
			}
		}
	}
	
	/** @return Whether this pool is closed */
	public boolean isClosed () {
		return closed;
	}
	
	// --- Background Controller Class ---
	
	private final class BackgroundThread implements Runnable {
		
		@Override
		public void run () {
			while ( !isClosed() ) {
				try {
					TimeUnit.SECONDS.sleep( 30 );
				} catch ( InterruptedException e ) {
					// Interrupted!
					close();
				}
				
				synchronized ( peers ) {
					for ( Iterator<PeerSession> it = peers.iterator(); it.hasNext(); ) {
						PeerSession peer = it.next();
						if ( peer.isClosed() ) {
							it.remove();
						}
					}
				}
			}
		}
		
	}
	
	// --- Thread Factory Class ---
	
	private static final class PoolThreadFactory implements ThreadFactory {
		
		/** Number of pools counter */
		private static final AtomicInteger poolCounter = new AtomicInteger( 0 );
		
		/** Number of threads counter */
		private final AtomicInteger threadCounter = new AtomicInteger( 0 );
		
		/** Number of this pool */
		private final int poolNum = poolCounter.incrementAndGet();
		
		/** Name of this pool */
		private final String name;
		
		public PoolThreadFactory ( String name ) {
			this.name = name;
		}
		
		@Override
		public Thread newThread ( Runnable r ) {
			Thread thread = new Thread( r );
			thread.setName( name + "-P" + poolNum + "-T" + threadCounter.incrementAndGet() );
			return thread;
		}
		
	}
	
}
