package net.sf.jukebox.datastream.logger.impl;

import net.sf.jukebox.datastream.logger.model.DataLogger;
import net.sf.jukebox.datastream.signal.model.DataSample;
import net.sf.jukebox.datastream.signal.model.DataSource;
import net.sf.jukebox.service.PassiveService;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.NDC;

/**
 * Common implementation base for different data loggers.
 * @param <E> Data type to log.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2005-2008
 */
public abstract class AbstractLogger<E extends Number> extends PassiveService implements DataLogger<E> {

  /**
   * Signature to name mapping.
   */
  protected final SortedMap<String, String> signature2name = new TreeMap<String, String>();

  /**
   * Create an instance listening to given data sources.
   * 
   * @param producers Data sources to listen to.
   */
  public AbstractLogger(Set<DataSource<E>> producers) {
      
      if (producers != null) {
      
          for (Iterator<DataSource<E>> i = producers.iterator(); i.hasNext(); ) {

              i.next().addConsumer(this);
          }
      }
  }

  /**
   * {@inheritDoc}
   */
  public final void consume(DataSample<E> sample) {
      
      NDC.push("consume");

      try {

          if (isKnownChannel(sample.signature)) {

              consume(sample.signature, sample);

          } else {

              // This means that we haven't heard about this signal before

              try {

                  createChannel(sample.sourceName, sample.signature, sample.timestamp);

                  logger.info("Created a channel for (" + sample.sourceName + "), sig " + sample.signature);

                  // If we were able to create a channel, we're here

                  consume(sample.signature, sample);

              } catch (Throwable t) {

                  // This most probably means we weren't able to create the
                  // channel

                  logger.warn("Unable to create a channel for '" + sample.sourceName + "' (" + sample.signature + ")", t);
              }
          }
      } finally {
          NDC.pop();
      }
  }

  /**
   * Check if this is a known channel.
   *
   * @param signature Channel signature to check.
   * @return {@code true if this channel is already known}.
   */
  protected final boolean isKnownChannel(String signature) {

    return signature2name.containsKey(signature);
  }

  /**
   * Create a new logging channel.
   * <p>
   * <strong>NOTE:</strong> Invocation of this method doesn't necessarily
   * mean that the persistent media for this channel doesn't exist. It merely
   * means that this channel wasn't yet encountered since the logger was
   * started. A check for existence <strong>must</strong> be performed.
   *
   * @param name Human readable name for this channel.
   * @param signature Signal signature that will be used to identify this
   * channel.
   * @param timestamp Timestamp of the data sample encountered.
   * @exception IOException if there was an I/O problem during channel
   * creation.
   */
  protected abstract void createChannel(String name, String signature, long timestamp) throws IOException;

  /**
   * Register a signal on a known channel.
   *
   * @param signature Signal signature.
   * @param value Signal sample value.
   */
  protected abstract void consume(String signature, DataSample<E> value);
}
