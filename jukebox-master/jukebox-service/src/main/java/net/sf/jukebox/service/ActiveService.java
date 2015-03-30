package net.sf.jukebox.service;

import java.util.concurrent.ThreadFactory;

import org.apache.log4j.NDC;

/**
 * Describes the concept of an active service, i.e.&nbsp;the one which starts
 * (observing some preconditions at startup), doing something, and then shuts
 * down (observing some post-conditions at shutdown).
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2008
 * @see PassiveService
 */
public abstract class ActiveService extends PassiveService {

    /**
     * The {@link #execute execute()} failure cause, if any.
     */

    /**
     * Default constructor.
     */
    protected ActiveService() {
    }

    /**
     * Create an instance.
     *
     * @param tg Thread group to use.
     * @param tf Thread factory to use.
     */
    protected ActiveService(ThreadGroup tg, ThreadFactory tf) {

        super(tg, tf);
    }

    /**
     * Create an instance.
     *
     * @param tf Thread factory to use.
     */
    protected ActiveService(ThreadFactory tf) {

        super(tf);
    }

    /**
     * Execute the main sequence.
     *
     * @exception InterruptedException if this thread was interrupted.
     * @exception Throwable propagated from the implementation. <br>
     * Important note: see the implementation of {@link PassiveService
     * PassiveService} for details on the <code>InterruptedException</code>
     * handling.
     */
    protected abstract void execute() throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startCore() {

        ActiveWrapper aw = new ActiveWrapper(this);

        core = getThreadFactory().newThread(aw);
        core.start();
    }

    /**
     * Method wrapper for {@link ActiveService#execute() execute()} call.
     */
    protected class ExecWrapper extends MethodWrapper {

        /**
         * Create an instance.
         *
         * @param target Object to control.
         */
        protected ExecWrapper(Object target) {

            super(target);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void call() throws Throwable {

            try {

                ((ActiveService) target).execute();

            } catch (Throwable t) {

                //execFailureCause = t;
                throw t;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void setFlags(boolean status) {

            // VT: FIXME: Why didn't I read the status? It obviously works, but
            // why?

            enabled = false;
            ready = false;
        }
    }

    /**
     * Wrapper for the startup, execution and shutdown threads. This class
     * serves the same goal as {@link PassiveWrapper PassiveWrapper}, but
     * supports a different lifecycle for the controlled object.
     */
    protected class ActiveWrapper implements Runnable {

        /**
         * Invocation target.
         */
        protected ActiveService target;

        /**
         * Create an instance.
         *
         * @param target Object to control.
         */
        protected ActiveWrapper(ActiveService target) {

            this.target = target;
        }

        /**
         * Run the target. This method must never be invoked directly, only by
         * {@link ActiveService ActiveService}.
         */
        public void run() {

            try {

                // complain(Log.DEBUG, "startup:");

                wrap(new StartupWrapper(target), semUp);

                if (!isReady()) {

                    // Obviously, the startup has failed.

                    return;
                }

                // complain(Log.DEBUG, "execute:");

                wrap(new ExecWrapper(target), semStopped);

                // complain(Log.DEBUG, "shutdown:");

                //wrap(new ShutdownWrapper(target, execFailureCause), semDown);
                wrap(new ShutdownWrapper(target), semDown);

            } catch (Throwable t) {

                // VT: NOTE: I'm just being paranoid. This should never happen.
                logger.fatal("THIS SHOULDN'T HAVE HAPPENED: uncaught exception sneaked through:", t);

            } finally {

                // Clean up after lazy programmers
                NDC.remove();
            }
        }
    }
}
