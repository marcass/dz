package net.sf.jukebox.service;

import net.sf.jukebox.logger.LogAware;
import net.sf.jukebox.sem.EventListener;
import net.sf.jukebox.sem.EventSemaphore;
import net.sf.jukebox.sem.SemaphoreTimeoutException;

/**
 * This class allows the object which implements the {@link
 * EventListener EventListener} interface to receive
 * the asynchronous notification <b>N</b> milliseconds from the time it had
 * been armed. Once the alarm is armed, you can:
 * <ul>
 * <li>{@link #set() reset} the alarm - it will set off <b>N</b> milliseconds
 * from then.
 * <li>{@link #clear clear()} the alarm - it will not set off at all
 * </ul>
 * If the alarm has been cleared or went off, you can {@link #arm() arm()} it
 * again. <br>
 * You may also replace the notification object at any time.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-1999
 * @see EventSemaphore
 */
public class Alarm extends LogAware implements Runnable {

    /**
     * True if the alarm had been requested to go off once (default).
     */
    protected boolean once = true;

    /**
     * The object who requested the alarm.
     */
    protected EventListener client;

    /**
     * The object to notify the client with when alarm goes off.
     */
    protected Object note;

    /**
     * Time before the alarm goes off.
     */
    protected long delay;

    /**
     * Alarm watcher thread.
     */
    protected Thread core = null;

    /**
     * Semaphore used to measure the time and trigger the alarm.
     */
    protected EventSemaphore bell;

    /**
     * Create the inactive alarm for the specified client.
     *
     * @param client Alarm event consumer.
     */
    public Alarm(EventListener client) {

        this(client, null);
    }

    /**
     * Create the inactive alarm for the specified client.
     *
     * @param client Alarm event consumer.
     * @param note Object to notify the client with.
     */
    public Alarm(EventListener client, Object note) {

        this.client = client;
        this.note = note;
    }

    /**
     * Create the armed alarm for the specified client.
     *
     * @param client Alarm event consumer.
     * @param millis Delay before the alarm goes off, in milliseconds.
     * @param note Object to notify the client with.
     * @exception IllegalArgumentException if the delay value is not positive
     */
    public Alarm(EventListener client, long millis, Object note) {

        this(client, note);
        set(millis);
    }

    /**
     * Set/reset the alarm.
     * <ul>
     * <li>If alarm is inactive, it is being armed.
     * <li>If it is active, it is being reset, i.e. it will go off
     * <code>delay</code> from now, not from the time it had been armed.
     * </ul>
     *
     * @param millis Delay before the alarm goes off, in milliseconds.
     * @exception IllegalArgumentException if the delay value is not positive
     * @see #clear
     */
    public synchronized void set(long millis) {

        if (millis <= 0) {

            throw new IllegalArgumentException("Positive value expected, got " + millis);
        }

        if (core != null) {

            bell.post();
            // complain( LOG_DEBUG,LOG_KERN,"Reset: "+client.toString() );
            return;
        }

        this.delay = millis;
        this.core = new Thread(this);
        core.start();

        // complain( LOG_DEBUG,LOG_KERN,"Armed: "+client.toString() );
    }

    /**
     * Set/reset the alarm again, with the same delay. Possible only after it
     * was armed, or you're going to set it for zero delay.
     *
     * @see #clear
     */
    public synchronized void set() {

        set(delay);
    }

    /**
     * Synonym for {@code set(delay)}.
     *
     * @param delay Delay to set.
     * @see #set(long)
     */
    public synchronized void arm(long delay) {

        set(delay);
    }

    /**
     * Synonym for {@code set()}.
     *
     * @see #set()
     */
    public synchronized void arm() {

        set();
    }

    /**
     * Disable the alarm. Turn it off permanently.
     *
     * @see #set()
     */
    public synchronized void clear() {

        // complain( LOG_DEBUG,LOG_KERN,"Cleared: "+client.toString() );

        if (core != null) {

            core.interrupt();
        }
    }

    /**
     * Watch for the alarm. Use of this method is <b>strongly discouraged</b>.
     * It was made public only to comply to the <code>Runnable</code>
     * interface, the results of its direct invocation are undeterministic.
     */
    public void run() {

        bell = new EventSemaphore();

        do {

            try {

                bell.waitFor(delay);

            } catch (InterruptedException iex) {

                // complain( LOG_WARNING,LOG_KERN,iex.toString() );

                core = null;
                return;

            } catch (SemaphoreTimeoutException stoex) {

                if (core == null) {

                    // Means that it was disabled while waiting
                    return;
                }

                // To prevent the note from changing

                synchronized (this) {

                    client.eventNotification(this, note);

                    if (once) {

                        core = null;
                        return;
                    }
                }
            }

            if (core == null) {

                // Means that it was disabled while waiting
                return;
            }
        } while (core != null);

        // logger.debug(LOG_KERN, "finished");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {

        logger.debug("finalize");
        super.finalize();
    }

    /**
     * Change the notification object.
     *
     * @param note Object to notify the client with.
     */
    public synchronized void notifyWith(Object note) {

        this.note = note;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "Alarm(" + delay + " ms" + (once ? " once" : "") + ", client " + client.toString()
                + ((note == null) ? "" : ", notify with " + note.toString()) + ")";
    }

    /**
     * Enable or disable repetitious triggering. You can do it any time you
     * want. Effective immediately, unless the alarm was programmed to go off
     * once and already did it, in this case you have to <code>set()</code> it
     * again.
     *
     * @param enable Should be true if you want the alarm to oscillate with a
     * given frequency.
     */
    public void again(boolean enable) {

        once = !enable;
    }

}