package es.darkhogg.torrent.dht;

/**
 * Possible status of DHT nodes, depending on its activity and replied queries.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public enum NodeStatus {
    UNKNOWN, BAD, QUESTIONABLE, GOOD;
}
