package net.sf.jukebox.sem;

/**
 * The reason to exist is to note that this very exception has something to
 * do with a semaphore operation.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-1998
 */
public class SemaphoreException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create an instance.
     *
     * @param message Exception message.
     */
    public SemaphoreException(String message) {
        super(message);
    }

    /**
     * Create an instance.
     *
     * @param message Exception message.
     * @param rootCause Root cause.
     */
    public SemaphoreException(String message, Throwable rootCause) {

        super(message);

        initCause(rootCause);
    }
}