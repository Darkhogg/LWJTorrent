package es.darkhogg.torrent.wire;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class PeerSessionPool implements Closeable {
	
	/** Internal collection containing all peers */
	private final Set<PeerSession> peers = Collections.synchronizedSet( new HashSet<PeerSession>() );
	
	/** Internal executor for events */
	private final ExecutorService eventExecutor = Executors.newSingleThreadExecutor( new PoolThreadFactory(
		"EventThread" ) );
	
	/** Internal executor for clients */
	private final ExecutorService cachedExecutor = Executors
		.newCachedThreadPool( new PoolThreadFactory( "MiscThread" ) );
	
	/** Whether this pool is closed */
	private volatile boolean closed = false;
	
	// --- Constructors ---
	
	public PeerSessionPool () {
		cachedExecutor.submit( new BackgroundThread() );
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
		PeerSession session = PeerSession.newSession( connection, eventExecutor, cachedExecutor );
		peers.add( session );
		return session;
	}
	
	// --- Closing Methods ---
	
	@Override
	public void close () {
		for ( PeerSession peer : peers ) {
			peer.close();
		}
		eventExecutor.shutdown();
		cachedExecutor.shutdown();
	}
	
	/** @return Whether this pool is closed */
	public boolean isClosed () {
		return closed;
	}
	
	// --- Background Controller Class ---
	
	private final class BackgroundThread implements Runnable {
		
		@Override
		public void run () {
			try {
				while ( !isClosed() ) {
					TimeUnit.SECONDS.sleep( 20 );
					
					synchronized ( peers ) {
						for ( Iterator<PeerSession> it = peers.iterator(); it.hasNext(); ) {
							PeerSession peer = it.next();
							if ( peer.isClosed() ) {
								it.remove();
							}
						}
					}
				}
			} catch ( InterruptedException e ) {
				close();
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
