package net.sf.jukebox.util;

import java.util.LinkedList;

/**
 * This class is primarily intended to be used by the {@link
 * net.sf.jukebox.logger.Logger Logger} and therefore does not implement
 * any collection interfaces.
 *
 * @param <E> Type to use for the queue.
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2008
 */
public class SimpleQueue<E> {

    /**
     * The data storage.
     */
    protected LinkedList<E> theQueue = new LinkedList<E>();

    /**
     * Queue the object.
     *
     * @param target Object to queue.
     */
    public synchronized void put(E target) {

        theQueue.addLast(target);
        notify();
    }

    /**
     * Wait for the object from the queue, then remove and return it. This
     * method blocks until the object is available.
     *
     * @return The first object from the queue.
     * @exception InterruptedException if the wait was interrupted.
     */
    public synchronized E waitObject() throws InterruptedException {

        while (theQueue.isEmpty()) {

            wait();
        }

        return theQueue.removeFirst();
    }
}