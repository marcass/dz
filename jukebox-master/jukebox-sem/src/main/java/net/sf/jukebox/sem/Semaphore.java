package net.sf.jukebox.sem;

import net.sf.jukebox.util.PackageNameStripper;

/**
 * Basic semaphore class, which is currently a base for the {@link
 * EventSemaphore EventSemaphore}, {@link SemaphoreGroup SemaphoreGroup} and
 * {@link MutexSemaphore MutexSemaphore}.
 * <p>
 * Note that this class is a generalization of <code>EventSemaphore</code> and
 * <code>SemaphoreGroup</code> (former <code>EventSemaphoreGroup</code>)
 * classes, so it is documented more briefly - it you need clarifications, you
 * may find them there.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim
 * Tkachenko</a> 1995-1998
 * @version $Id: Semaphore.java,v 1.2 2007-06-14 04:32:18 vtt Exp $
 */
public abstract class Semaphore extends Multicaster {

    /**
     * The semaphore name.
     * <p>
     * May be empty. Note that there's no limitations on the name, as well as
     * requirement of unique name to each semaphore. <br>
     * Also note that the name plays the one and only role here so far: make
     * debugging easier. Production version of the class, should it ever exist,
     * will not have it.
     */
    protected String name;

    protected Semaphore() {

    }

    /**
     * Create the named semaphore.
     *
     * @param name The name to assign to this semaphore.
     */
    protected Semaphore(String name) {
        this.name = name;
    }

    /**
     * Create the named semaphore.
     *
     * @param owner The name to assign to this semaphore is to be deducted from
     * the owner's properties. The rule is:
     * {@code name = owner.getClass().getName() + "/" + Integer.toHexString(owner.hashCode());}
     */
    protected Semaphore(Object owner) {
        this(owner, null);
    }

    /**
     * Create the named semaphore.
     *
     * @param owner The name to assign to this semaphore is to be deducted from
     * the owner's properties. The rule is: {@code name =
     * owner.getClass().getName()+"/"+Integer.toHexString(owner.hashCode()) +
     * "/"+qualifier;}
     * @param qualifier Usually descriptive part of a semaphore name.
     */
    protected Semaphore(Object owner, String qualifier) {

        if (owner == null) {
            owner = this;
        }

        if (qualifier == null) {
            qualifier = "";
        }

        name = PackageNameStripper.stripPackage(owner.getClass().getName()) + "/"
                + Integer.toHexString(owner.hashCode()) + "/" + qualifier;
    }

    /**
     * Get the semaphore name.
     *
     * @return The semaphore name.
     */
    public String getName() {
        return name;
    }

    /**
     * Wait for the semaphore forever.
     *
     * @return Value depends on the nature of the derived class.
     * @exception InterruptedException if this thread was interrupted by another
     * thread.
     */
    abstract public boolean waitFor() throws InterruptedException;

    /**
     * Wait for the semaphore for a specified timeout.
     *
     * @param millis Milliseconds to wait.
     * @return Value depends on the nature of the derived class.
     * @exception InterruptedException if this thread was interrupted by another
     * thread.
     * @exception SemaphoreTimeoutException if the desired event hasn't happened
     * within specified timeout.
     */
    abstract public boolean waitFor(long millis) throws InterruptedException, SemaphoreTimeoutException;
}