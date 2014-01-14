package es.darkhogg.torrent.dht.krpc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that represent the query type of a KRPC query message
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public enum QueryType {
    /** A ping query */
    PING("ping"),

    /** A query to obtain contact information for a node */
    FIND_NODE("find_node"),

    /** A query to obtain peer for an infohash */
    GET_PEER("get_peer"),

    /** A query to announce yourself to other nodes */
    ANNOUNCE_PEER("announce_peer");

    /** The map from strings to query types */
    private static final Map<String,QueryType> values;
    static {
        Map<String,QueryType> map = new HashMap<String,QueryType>();
        for (QueryType type : values()) {
            map.put(type.string, type);
        }
        values = Collections.unmodifiableMap(map);
    }

    /** Internal string used as the "q" value of the request */
    private final String string;

    /**
     * @param string Value for the "q" field
     */
    private QueryType (String string) {
        this.string = string;
    }

    /** @return The value to be used on the <tt>q</tt> field */
    public String toString () {
        return string;
    }

    /**
     * @param str Query type string obtained from the <tt>q</tt> field
     * @return The <tt>QueryType</tt> object which {@link #toString string} method would return <tt>str</tt>
     */
    public static QueryType fromString (String str) {
        return values.get(str);
    }
}
