package net.sf.jukebox.datastream.signal.model;

/**
 * A data sink. An entity capable of consuming a {@link DataSample data sample}.
 *
 * @param <E> Data type to handle.
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2005-2009
 */
public interface DataSink<E> {

  /**
   * Consume the data sample.
   *
   * @param signal Sample to consume.
   */
  void consume(DataSample<E> signal);
}
