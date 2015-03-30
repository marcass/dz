package net.sf.jukebox.aggregator;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2007-2008
 */
public class RunnableAggregatorTest extends TestCase {
    
    private boolean loggerInitialized = false;
    
    @Override
    public void setUp() {
        if (!loggerInitialized) {
            BasicConfigurator.configure();
            loggerInitialized = true;
        }
    }
    
    public void testEmptyQueue() {
        
        RunnableAggregator aggregator = new RunnableAggregator();

        aggregator.process(10, new LinkedBlockingQueue<Runnable>(), null);

        // We simply have to arrive at this point
    }

    public void testProducerScarce() {
        testProducer(100, 10);
    }

    public void testProducerAbundant() {
        testProducer(100, 1000);
    }

    private void testProducer(int objectLimit, int threadCount) {
        try {
            RunnableAggregator aggregator = new RunnableAggregator();
            BlockingQueue<Runnable> requestQueue = new LinkedBlockingQueue<Runnable>();

            Set<Integer> result = Collections.synchronizedSet(new TreeSet<Integer>());
            
            for (int count = 0; count < objectLimit; count++) {

                requestQueue.put(new Producer(result));
            }
            
            aggregator.process(threadCount, requestQueue, null);
            
            assertEquals("Wrong count", objectLimit, result.size());
            
        } catch (InterruptedException ex) {
            fail(ex.getMessage());
        }
    }
    
    public class Producer implements Runnable {
        
        private final Set<Integer> collector;
        
        public Producer(Set<Integer> collector) {
            this.collector = collector;
        }

        public void run() {
            collector.add(new Integer(hashCode()));
        }
    }
}
