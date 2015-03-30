package net.sf.jukebox.service;

import java.util.concurrent.ThreadPoolExecutor;

import net.sf.jukebox.logger.LogAware;
import net.sf.jukebox.sem.ACT;
import net.sf.jukebox.util.Interval;

import org.apache.log4j.NDC;

/**
 * Entity supposed to carry on the task and deliver the message produced as a
 * result.
 * <p/>
 * This is a simplified version of the {@link ActiveService ActiveService},
 * which does not require a complicated startup - execute - shutdown procedure.
 * <p/>
 * In the process of evolution it turned out that there are indeed services so
 * simple that the safety net is a nuisance rather than a help. However, some
 * remnants of the safety net are present - this class will not blow up and
 * disappear without leaving a trace.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2010
 */
public abstract class Messenger extends LogAware {

    /**
     * The completion token.
     */
    private ACT complete = new ACT();

    /**
     * Create the instance.
     */
    protected Messenger() {
    }

    /**
     * Start the messenger in a new thread.
     *
     * @return The asynchronous completion token associated with the completion
     *         of the given task.
     *         <p/>
     *         This object has to be treated carefully: its {@link
     *         ACT#getUserObject getUserObject()} method must
     *         be called in order to retrieve the result of the messenger's operation.
     *         It is up to the derived class implementor to outline the usage for such
     *         an object.
     *         <p/>
     *         This token will be {@link ACT#complete(boolean,
     *Object) complete()}d with a status of <code>true</code> if
     *         the execution has completed normally, and <code>false</code> if it
     *         threw the exception. In this case the completion token will contain the
     *         exception, instead of the intended result (and of course, it could be the
     *         exception you've deliberately thrown out of the
     *         {@link #execute execute()} method).
     */
    public final ACT start() {

        new Thread(new Executor()).start();
        return complete;
    }
    
    /**
     * Start the messenger in a new thread.
     * 
     * It is the responsibility of the caller to properly set up the {@code executor}.
     * 
     * @return The asynchronous completion token associated with the completion
     * of the given task.
     * 
     * @see #start()
     */
    public final ACT start(ThreadPoolExecutor executor) {
        
        executor.execute(new Executor());
        return complete;
    }

    /**
     * Do the job. Note the permissions on the method - we don't want strangers
     * to do the job for us.
     *
     * @return The result, whatever it is.
     *
     * @throws Throwable whenever it happens.
     */
    protected abstract Object execute() throws Throwable;

    /**
     * Lifecycle controller for the payload object.
     */
    private final class Executor implements Runnable {

        /**
         * Run the errand. This method must never be called directly, only by
         * the {@link Messenger Messenger}.
         */
        @Override
        public void run() {

            NDC.push("run@" + Integer.toHexString(Thread.currentThread().hashCode()));
            
            long start = System.currentTimeMillis();

            // Protect the suckers.

            // Whatever trouble happens, they will know about it - the
            // completion token will tell them.

            Object result = null;

            try {

                try {
                    result = execute();
                } catch (Throwable t) {

                    // Well, in this case I don't have anything better to do
                    // than to notify the suckers with the cause.
                    complete.complete(false, t);

                    // And bail out immediately
                    return;
                }

                complete.complete(true, result);

            } finally {

                logger.info("Completed in " + Interval.toTimeInterval(System.currentTimeMillis() - start));
                NDC.pop();
                NDC.remove();
            }
        }
    }
}
