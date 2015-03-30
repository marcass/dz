package net.sf.jukebox.aggregator;

import java.util.concurrent.BlockingQueue;

/**
 * Object implementing scatter/gather, or aggregation algorithm for homogenous {@link Runnable} workers.
 * 
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2007-2008
 */
public class HomogenousAggregator<Request, Response, T extends Throwable> {
    
    public void process(BlockingQueue<Request> requestQueue, BlockingQueue<Response> responseQueue, WorkerFactory<Request, Response, T> workerFactory) {
        
    }
}
