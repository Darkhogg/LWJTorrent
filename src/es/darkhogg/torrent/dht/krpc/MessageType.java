package es.darkhogg.torrent.dht.krpc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that represent the type of a KRPC message
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public enum MessageType {
    /** A query message */
    QUERY("q"),

    /** A response to a query */
    RESPONSE("r"),

    /** An error message */
    ERROR("e");

    /** The map from strings to message types */
    private static final Map<String,MessageType> values;
    static {
        Map<String,MessageType> map = new HashMap<String,MessageType>();
        for (MessageType type : values()) {
            map.put(type.string, type);
        }
        values = Collections.unmodifiableMap(map);
    }

    /** Value for the "y" key in the message bencode */
    private final String string;

    /**
     * @param string Value for the <tt>string</tt> field
     */
    private MessageType (String string) {
        this.string = string;
    }

    /** @return The value to use with the "y" key of the bencoded message */
    public String getString () {
        return string;
    }

    /**
     * @return The <tt>MessageType</tt> object which {@link #getString string} method would return <tt>str</tt>
     */
    public static MessageType fromString (String str) {
        return values.get(str);
    }
}
