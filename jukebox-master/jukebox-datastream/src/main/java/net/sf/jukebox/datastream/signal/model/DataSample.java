package net.sf.jukebox.datastream.signal.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A data sample. An immutable object that is used to carry the data around,
 * along with its description.
 * 
 * @param <E> Data type of the sample.
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2005-2012
 */
public final class DataSample<E> implements Serializable {

    private static final long serialVersionUID = 234850129837904475L;

    /**
     * Date format used to print the timestamp in {@link #toString()}.
     */
    public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Timestamp associated with the sample. Must represent a valid time.
     */
    public final long timestamp;

    /**
     * Human readable name of the source of this data sample.
     */
    public final String sourceName;

    /**
     * Signature that can be used to uniquely identify the source of this data.
     * Due to implementation constraints, must be short and filesystem-safe.
     */
    public final String signature;

    /**
     * The sample itself.
     */
    public final E sample;

    /**
     * The error associated with the sample.
     */
    public final Throwable error;

    /**
     * Create an instance with the timestamp being current time.
     * 
     * @param sourceName Human readable name of the source of this data sample.
     * @param signature Signature that can be used to uniquely identify the
     * source of this data. Due to implementation constraints, must be short and
     * filesystem-safe. Must not be empty.
     * @param sample The sample itself. May be null only if the error is not
     * null.
     * @param error Exception associated with the error. Must not be null if the
     * sample is null.
     */
    public DataSample(String sourceName, String signature, E sample, Throwable error) {

        this(-1, sourceName, signature, sample, error);
    }

    /**
     * Create explicitly defined instance.
     * 
     * @param timestamp Timestamp associated with the sample. If it is negative,
     * current time will be used instead.
     * @param sourceName Human readable name of the source of this data sample.
     * @param signature Signature that can be used to uniquely identify the
     * source of this data. Due to implementation constraints, must be short and
     * filesystem-safe. Must not be empty.
     * @param sample The sample itself. May be null only if the error is not
     * null.
     * @param error Exception associated with the error. Must not be null if the
     * sample is null.
     */
    public DataSample(long timestamp, String sourceName, String signature, E sample, Throwable error) {

        if (timestamp < 0) {

            timestamp = System.currentTimeMillis();
        }

        this.timestamp = timestamp;
        this.sourceName = sourceName;

        if (signature == null || "".equals(signature)) {

            throw new IllegalArgumentException("Signature must not be empty");
        }

        this.signature = signature;

        if (sample == null && error == null) {

            throw new IllegalArgumentException("Sample must not be null if error is");
        }

        this.sample = sample;

        if (sample != null && error != null) {

            throw new IllegalArgumentException("Error must be null if the sample is not");
        }

        this.error = error;
    }

    /**
     * @return {@code true} if the data sample is an error.
     */
    public boolean isError() {

        return error != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
    
        StringBuffer sb = new StringBuffer("DataSample[");
        
        // Default date resolution is not good enough for some applications
        sb.append(dateFormat.format(new Date(timestamp)));
        
        sb.append("#").append(sourceName).append("#");
        sb.append("sig(").append(signature).append("), ");

        sb.append("sample(").append(sample);
        
        if (isError()) {
        
            sb.append(", error(").append(error.getMessage()).append(")");
        }
        
        sb.append(")]");
        
        return sb.toString();
    }
}
