package net.sf.jukebox.aggregator;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import junit.framework.TestCase;
import net.sf.jukebox.aggregator.RunnableAggregator.Error;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2007-2009
 */
public class HomogenousAggregatorTest extends TestCase {

    private boolean loggerInitialized = false;
    
    @Override
    public void setUp() {
        if (!loggerInitialized) {
            BasicConfigurator.configure();
            loggerInitialized = true;
        }
    }
    
    public void testX2() {
        
        HomogenousAggregator<Integer, Integer, IOException> aggregator = new HomogenousAggregator<Integer, Integer, IOException>();
        WorkerFactory<Integer, Integer, IOException> workerFactory = new X2WorkerFactory();
        
        BlockingQueue<Integer> requestQueue = new LinkedBlockingQueue<Integer>();
        BlockingQueue<Integer> responseQueue = new LinkedBlockingQueue<Integer>();
        
        aggregator.process(requestQueue, responseQueue, workerFactory);
    }
    
    public class X2WorkerFactory implements WorkerFactory<Integer, Integer, IOException> {

        public Worker<Integer, Integer, Runnable, IOException> createWorker(Integer rq, BlockingQueue<Integer> responseQueue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public class X2Worker extends Worker<Integer, Integer, Runnable, IOException> {
        
        public X2Worker(Integer rq) {
            super(rq);
        }

        @Override
        public void process(BlockingQueue<Integer> responseQueue, BlockingQueue<Error<Runnable>> errors) throws IOException {
            try {

                responseQueue.put((Integer) rq);
                responseQueue.put((Integer) rq);

            } catch (InterruptedException ex) {
                throw new IOException("Interrupted", ex);
            }
        }
        
    }
}
