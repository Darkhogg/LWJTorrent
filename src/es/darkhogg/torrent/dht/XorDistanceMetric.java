package es.darkhogg.torrent.dht;

import java.math.BigInteger;

/**
 * A {@link DistanceMetric} that uses an XOR operation to calculate distances.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
/* package */final class XorDistanceMetric extends DistanceMetric {

    @Override
    public BigInteger getDistance (NodeId n1, NodeId n2) {
        byte[] b1 = n1.getBytes();
        byte[] b2 = n2.getBytes();

        byte[] ret = new byte[20];
        for (int i = 0; i < 20; i++) {
            ret[i] = (byte) (b1[i] ^ b2[i]);
        }

        return new BigInteger(+1, ret);
    }

}
