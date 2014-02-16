package org.streaminer.stream.quantile;

public class QuantilesException extends Exception {

    /**
     * Creates a new instance of
     * <code>QuantilesException</code> without detail message.
     */
    public QuantilesException() {
    }

    /**
     * Constructs an instance of
     * <code>QuantilesException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public QuantilesException(String msg) {
        super(msg);
    }
}
