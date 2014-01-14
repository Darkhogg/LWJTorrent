package es.darkhogg.torrent.dht;

/**
 * Possible status of DHT nodes, depending on its activity and replied queries.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public enum NodeStatus {
    /** Status of the node is not known */
    UNKNOWN,

    /** Node is a bad node */
    BAD,

    /** Node is not good, but also not bad */
    QUESTIONABLE,

    /** Node is a good node */
    GOOD;
}
