package net.sf.jukebox.sem;

import java.util.LinkedList;

/**
 * <h3>Behaviour Description</h3>
 * This semaphore behaves somewhat differently from ones you may have
 * encountered in other operating systems, such as OS/2 or Windows NT. This is
 * so due to several reasons: firstly, I've finished programming for OS/2 more
 * than threey ears ago (to be exact, in December of 94), I've been never
 * programming for Windows NT in terms of system development, though I'm
 * familiar with its API and sure that the behaviour is much the same as one in
 * OS/2, except different system call names, and the most important, I've had a
 * time to think it over and reimplement it by myself as I see it, because of
 * Java principle - "write once, run everywhere".
 * <p>
 * Thus, the semaphore behaviour may be described as follows:
 * <dl>
 * <dt>Semaphore posting/clearing.
 * <dd>When the semaphore is being triggered, it just stores the status and
 * notifies all threads about it ({@link Object#notifyAll
 * Object.notifyAll()}).
 * <dd>There's NO posting count. I think there's absolutely NO need for it, see
 * below why do I think so.
 * <dt>Waiting for semaphore.
 * <dd>{@link #waitFor() waitFor()} method returns true if the semaphore has been
 * posted, false if it has been cleared. It returns immediately when the
 * semaphore has been posted/cleared before the call, with the following
 * exception.
 * <dd>When a thread analyzes if the semaphore has been triggered, it has only
 * one chance to catch it. It means, first request {@link #isTriggered
 * isTriggered()} will return true if so, but any subsequent request will return
 * false for that thread, until the semaphore will be triggered again. I think
 * it's pretty fair.
 * <dt>If that was a limited wait?
 * <dd>{@link SemaphoreTimeoutException SemaphoreTimeoutException} is thrown
 * upon the timeout expiration.
 * <dt>Motivation for such a strange behaviour.
 * <dd>When I decided to reimplement the semaphore mechanism myself, I didn't
 * try just to write from scratch the mechanism I've seen before. Instead, I
 * smoked over the mechanism I thought would be reasonable and useful. So, let's
 * ask some rethorical questions and give the answers.
 * <dl>
 * <dt>What is an <b>event</b>?
 * <dd>Event is something happening somewhere in the time, at least once.
 * <dt>What is an event semaphore?
 * <dd>Event semaphore is an entity that allows a different entity, described
 * below, to receive notification about an <b>event</b> without spending
 * processor time in vain attempts to check the status of abovementioned event.
 * <dt>What is the entity that requires the status of that <b>event</b>?
 * <dd>I think this can be briefly desribed as a data controlled code flow. I
 * think that put in simple words this is a thread.
 * <dt>Why do I think that a thread needs event notification just once?
 * <dd>Because the <b>event</b> happens once. One notification per event is
 * more than enough. Putting aside the thread issue, what's your opition about a
 * delivery man who's exploding through your door once in a while regardless of
 * have you received the message from him? That's not about threads, that's
 * about a common sense.
 * <dd>By the way, nobody prevents you from checking the semaphore status.
 * </dl>
 * </dl>
 *
 * @version $Id: EventSemaphore.java,v 1.5 2007-06-16 06:56:18 vtt Exp $
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-2007
 * @see SemaphoreGroup
 */
public class EventSemaphore extends Semaphore {

  /**
   * Log facility to use. Actually, almost all logger calls have been already
   * eliminated, but once in a while I have to debug the problems.
   */
  public static final String CH_SEM = "SEM";

  /**
   * Semaphore status. Initially false.
   *
   * @see #trigger
   * @see #post
   * @see #clear
   * @see #getStatus
   */
  protected boolean status;

  /**
   * Triggered status.
   * <p>
   * False until first status change, true later. All the threads which issue
   * the {@link #waitFor() waitFor()} call for a semaphore which hadn't been
   * triggered yet, will wait.
   *
   * @see #canGetStatus
   */
  protected boolean triggered;

  /**
   * Threads that have already requested status after last status change. In
   * reality, thread hash codes stores rather the thread references, to help
   * the garbage collector.
   * <p>
   * (September 10 98) With the introduction of the weak references in JDK
   * 1.2, the hash code will be probably replaced by the weak reference.
   */
  protected LinkedList<Integer> lastRequest;

  /**
   * Default constructor.
   */
  public EventSemaphore() {

    super(null);
    init();
  }

  /**
   * Creates named EventSemaphore.
   * <p>
   * Note that there may be more than one semaphore with the same name, mostly
   * because (as I mentioned before) Java environment model significantly
   * differs from one in other operating systems. Ther'll never be such a
   * thing as a global name space for every semaphore in the common networking
   * environment, like one I'm familiar with in OS/2 (and the name space for
   * the named pipes, too), so for the time being I think I don't need a
   * global name uniqueness for the semaphores.
   *
   * @param name The name assigned for the semaphore.
   */
  public EventSemaphore(String name) {

    super(name);
    init();
  }

  /**
   * Creates named EventSemaphore with default name.
   * <p>
   * The default name is
   * <code>owner.getClass().getName()+"/"+Integer.toHexString(owner.hashCode())</code>
   *
   * @param owner The object that gives the name to this semaphore
   */
  public EventSemaphore(Object owner) {

    super(owner);
    init();
  }

  /**
   * Creates named EventSemaphore with default name and the string name
   * appended to it, *
   * "owner.getClass().getName()+"/"+Integer.toHexString(owner.hashCode())+"/"+qualifier"
   *
   * @param owner Semaphore owner.
   * @param qualifier The additional name.
   */
  public EventSemaphore(Object owner, String qualifier) {

    super(owner, qualifier);
    init();
  }

  /**
   * Initialize. Set {@link #status status} and {@link #triggered triggered}
   * to false, and create the {@link #lastRequest lastRequest} array.
   */
  private void init() {

    status = false;
    triggered = false;
    lastRequest = new LinkedList<Integer>();
  }

  /**
   * Can the current thread receive the actual status?
   * <p>
   *
   * @return true if this method is called first time after semaphore
   * creation/posting/clearing, false otherwise.
   */
  protected synchronized boolean canGetStatus() {

    if (!triggered) {

      // No posting/clearing occured yet, nobody can get the real
      // value

      return false;
    }

    int ti = Thread.currentThread().hashCode();
    Integer t = new Integer(ti);
    int idx = lastRequest.indexOf(t);

    if (idx == -1) {

      // logger.debug(CH_SEM, "canGetStatus: first request (" +
      // Integer.toHexString(ti) + ")" );
      lastRequest.add(t);
      return true;

    }

    // logger.debug(CH_SEM, "canGetStatus: redundant request (" +
    // Integer.toHexString(ti) + ")" );
    return false;
  }

  /**
   * Trigger the semaphore with the given status.
   *
   * @param value Semaphore status to be set.
   * @see #post
   * @see #clear
   */
  public synchronized void trigger(boolean value) {

    // complain( LOG_DEBUG, CH_SEM, "triggered: " + name + ":" + value);
    lastRequest.clear();
    status = value;
    triggered = true;
    notifyListeners(status);
    notifyAll();
  }

  /**
   * {@link #trigger trigger}<code>( true )</code>
   *
   * @see #clear
   */
  public synchronized void post() {

    trigger(true);
  }

  /**
   * {@link #trigger trigger}<code>( false )</code>
   *
   * @see #post
   */
  public synchronized void clear() {

    trigger(false);
  }

  /**
   * Wait forever for the semaphore to be triggered.
   * <p>
   * Note: each thread will get "true" for a semaphore triggering just once.
   * Any subsequent call will wait again.
   *
   * @return the semaphore status
   * @see #getStatus
   * @exception InterruptedException if the thread which the semaphore waits
   * in has been interrupted.
   */
  @Override
  public synchronized boolean waitFor() throws InterruptedException {

    // complain( Log.DEBUG,"waitFor "+name );
    if (canGetStatus()) {

      // complain( Log.DEBUG,"caught behind: "+toString() );
      return status;
    }

    wait();
    // complain( Log.DEBUG,"caught: "+toString() );
    // Added 970604
    lastRequest.add(Thread.currentThread().hashCode());
    return status;
  }

  /**
   * Wait the specified amount of time for the semaphore posting.
   *
   * @param millis Time to wait, milliseconds.
   * @return the semaphore status
   * @see #getStatus
   * @exception InterruptedException if the thread which the semaphore waits
   * in is interrupted.
   * @exception SemaphoreTimeoutException if no events occured within a
   * timeout.
   */
  @Override
  public synchronized boolean waitFor(long millis) throws SemaphoreTimeoutException, InterruptedException {

    // complain( "waitFor("+millis+") "+name );
    if (canGetStatus()) {
      return status;
    }

    wait(millis);

    if (canGetStatus()) {
      return status;
    }

    SemaphoreTimeoutException timedOut = new SemaphoreTimeoutException(Long.toString(millis));

    notifyListeners(timedOut);
    throw timedOut;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized String toString() {

    String result = "(EventSem";

    if (!"".equals(name)) {

      result += "[" + name + "]";
    }

    result += "." + Integer.toHexString(hashCode()) + ":" + status + ")";

    return result;
  }

  /**
   * Had the semaphore been triggered.
   * <p>
   * Answers positive on request if the semaphore had been triggered. Note
   * that the answer is positive JUST ONCE FOR EACH REQUESTING THREAD. Any
   * subsequent answer is false, as well as if the semaphore hadn't been
   * triggered at all, until the next semaphore event. <br>
   * This method is going to become package protected.
   *
   * @return true if the semaphore has been triggered, false if not.
   * @see #waitFor()
   */
  public synchronized boolean isTriggered() {

    return canGetStatus();

    // logger.debug(CH_SEM, "isTriggered: " + result);
    //return result;
  }

  /**
   * Get the semaphore status.
   * <p>
   * Note that the real-time status may be obtained at the any moment without
   * any limitations.
   *
   * @return The semaphore status.
   */
  public synchronized boolean getStatus() {

    return status;
  }
}