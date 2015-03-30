package net.sf.jukebox.datastream.signal.model;

/**
 * A data source. An entity capable of producing a {@link DataSample data sample}.
 *
 * @param <E> Data type to handle.
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2009
 */
public interface DataSource<E> {

    /**
     * Add a {@link DataSample data sample} consumer.
     * 
     * @param consumer Consumer to add.
     */
    void addConsumer(DataSink<E> consumer);

    /**
     * Remove a {@link DataSample data sample} consumer.
     * 
     * @param consumer Consumer to remove.
     */
    void removeConsumer(DataSink<E> consumer);
}
