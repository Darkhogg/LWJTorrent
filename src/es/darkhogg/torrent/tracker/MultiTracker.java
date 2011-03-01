package es.darkhogg.torrent.tracker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import es.darkhogg.bencode.BencodeInputStream;

final class MultiTracker extends Tracker {

	private final List<String> urls;
	
	public MultiTracker ( List<Set<String>> announces ) {
		urls = new LinkedList<String>();
		
		for ( Set<String> set : announces ) {
			urls.addAll( set );
		}
	}

	@Override
	public TrackerResponse sendRequest ( TrackerRequest request ) {
		for ( String str : urls ) {
			try {
				URL requestUrl = getRequestUrl( str, request );
				
				BencodeInputStream bis = new BencodeInputStream( requestUrl.openStream() );
				return TrackerResponse.fromValue( bis.readValue() );
				
			} catch ( MalformedURLException e ) {
				// Nothing
			} catch ( IOException e ) {
				// Nothing
			}
		}
		
		return null;
	}

}
