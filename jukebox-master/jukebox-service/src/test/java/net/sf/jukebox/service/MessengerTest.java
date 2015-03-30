package net.sf.jukebox.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.jukebox.sem.SemaphoreGroup;

import junit.framework.TestCase;

public class MessengerTest extends TestCase {
    
    private int current = 0;
    private int max = 0;

    public void testPool() throws InterruptedException {

        final int poolSize = 10;
        final BlockingQueue<Runnable> messengerQueue = new LinkedBlockingQueue<Runnable>();
        final ThreadPoolExecutor tpe = new ThreadPoolExecutor(poolSize, poolSize, 60L, TimeUnit.SECONDS, messengerQueue);
        SemaphoreGroup done = new SemaphoreGroup();
        
        for (int count = 0; count < poolSize * 10; count++) {
            
            done.add(new Worker().start(tpe));
        }
        
        done.waitForAll();
        
        assertEquals("Wrong max", poolSize, max);
        assertEquals("Wrong current", 0, current);
    }
    
    private synchronized void in() {
        
        current++;
        
        if (current > max) {
            max = current;
        }
    }
    
    private synchronized void out() {
        
        current--;
    }

    protected class Worker extends Messenger {

        @Override
        protected Object execute() throws Throwable {

            in();
            Thread.sleep(100);
            out();
            return null;
        }
        
    }
}
