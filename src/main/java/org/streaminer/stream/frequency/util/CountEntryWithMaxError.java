package org.streaminer.stream.frequency.util;

/**
 * {@link CountEntry}, extended with a field maxError.
 *
 * @author Benedikt Kulmann, office@kulmann.biz
 */
public class CountEntryWithMaxError<T> extends CountEntry<T> {
    
    private static final long serialVersionUID = 1L;

    public long maxError;

    public CountEntryWithMaxError(T item, long frequency, long maxError) {
        super(item, frequency);
        this.maxError = maxError;
    }

    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public String toString() {
        return "CountEntryWithMaxError[item=" + item + ", freq=" + frequency + ", maxError=" + maxError + "]";
    }

    /**
     * {@inheritDoc}
     * @return 
     * @throws java.lang.CloneNotSupportedException
     */
    @Override
    public CountEntryWithMaxError<T> clone() throws CloneNotSupportedException {
        return (CountEntryWithMaxError<T>)super.clone();
    }
}
