package net.sf.jukebox.datastream.logger.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.NDC;

import net.sf.jukebox.datastream.signal.model.DataSample;
import net.sf.jukebox.datastream.signal.model.DataSink;
import net.sf.jukebox.datastream.signal.model.DataSource;
import net.sf.jukebox.logger.LogAware;
import net.sf.jukebox.service.Messenger;
import net.sf.jukebox.util.CollectionSynchronizer;

/**
 * A data source. An entity capable of producing a {@link DataSample data sample}.
 *
 * @param <E> Data type to handle.
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2009-2012
 */
public class DataBroadcaster<E> extends LogAware implements DataSource<E> {

    private final Set<DataSink<E>> consumerSet = new HashSet<DataSink<E>>();
    private final boolean async;
    
    public DataBroadcaster() {
        
        // Hunting down the deadlock
        String property = System.getProperty(getClass().getName()+ ".async");
        
        logger.info("Async: '" + property + "'");
        async = property != null;    
    }
    
    public synchronized void addConsumer(DataSink<E> consumer) {
        
        NDC.push("addConsumer");
        
        try {
            
            // Reason for the synchronized scope (and not method) is the collection synchronizer
            synchronized (consumerSet) {
            
                consumerSet.add(consumer);
                logger.debug("Added: " + consumer);
            }
            
        } finally {
            NDC.pop();
        }
    }

    public synchronized void removeConsumer(DataSink<E> consumer) {
        
        NDC.push("removeConsumer");
        
        try {
            
            // Reason for the synchronized scope (and not method) is the collection synchronizer
            synchronized (consumerSet) {
            
                consumerSet.remove(consumer);
                logger.debug("Removed: " + consumer);
            }
            
        } finally {
            NDC.pop();
        }
    }
    
    public void broadcast(final DataSample<E> signal) {
        
        // Need the hash code to uniquely identify the broadcast invocation in the log
        NDC.push("broadcast#" + Integer.toHexString(signal.hashCode()));

        try {
            
            logger.debug("Signal: " + signal);
            
            Set<DataSink<E>> copy = new CollectionSynchronizer<DataSink<E>>().copy(consumerSet);

            for (Iterator<DataSink<E>> i = copy.iterator(); i.hasNext();) {
                
                final DataSink<E> dataSink = i.next();
                
                if (async) { 

                    Messenger m = new Messenger() {

                        @Override
                        protected Object execute() throws Throwable {
                            
                            NDC.push("execute");
                            
                            try {
                            
                                logger.debug("Feeding: " + dataSink);
                                dataSink.consume(signal);
                                return null;

                            } finally {
                                NDC.pop();
                            }
                        }
                    };

                    // There's nothing we can do about the return value, forget it - but don't forget to check the logs
                    m.start();

                } else {
                    
                    try {
                        
                        logger.debug("Feeding: " + dataSink);
                        dataSink.consume(signal);
                        
                    } catch (Throwable t) {
                        logger.warn("Consumer invocation resulted in exception, there's nothing we can do to fix it", t);
                    }
                }
            }
            
        } finally {
            
            NDC.pop();
        }
    }
}
