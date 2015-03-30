package net.sf.jukebox.sem;

import net.sf.jukebox.TimeoutException;

/**
 * Thrown if any methods that involve timed wait time out.  A primary reason
 * for existence of this kind of exception is that waitFor method should
 * return a semaphore status and just can't return any meaningful value if
 * times out.
 *
 * @see EventSemaphore#waitFor(long)
 * @see SemaphoreGroup#waitForOne(long)
 * @see SemaphoreGroup#waitForAll(long)
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2008
 */
public class SemaphoreTimeoutException extends SemaphoreException implements TimeoutException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create an instance.
     *
     * @param message Exception message.
     */
    public SemaphoreTimeoutException(String message) {
        super(message);
    }

    /**
     * Create an instance.
     *
     * @param message Exception message.
     * @param rootCause Root cause.
     */
    public SemaphoreTimeoutException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
}