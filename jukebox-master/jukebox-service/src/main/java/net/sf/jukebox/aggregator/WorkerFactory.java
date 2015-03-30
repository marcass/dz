package net.sf.jukebox.aggregator;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2007-2008
 */
public interface WorkerFactory<Request, Response, T extends Throwable> {

    public Worker<Request, Response, Runnable, T> createWorker(Request rq, BlockingQueue<Response> responseQueue);
}
