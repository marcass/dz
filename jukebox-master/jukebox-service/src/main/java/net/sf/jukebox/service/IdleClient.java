package net.sf.jukebox.service;

import net.sf.jukebox.sem.EventListener;

/**
 * Every service which wants to watch for its idle time should implement this
 * interface.
 * <p>
 * When the client idles out, it receives the notification with null producer
 * and the status equal to <code>Idle.OUT</code>.
 * <h3>BugTrack</h3>
 * <dl>
 * <dt>February 23 98
 * <dd>{@code Idle} implementation has been rewritten from scratch, so this
 * interface definition has been changed in such a way that there's no point to
 * preserve the former documentation for it.
 * </dl>
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim
 * Tkachenko</a> 1996-1999
 * @version $Id: IdleClient.java,v 1.2 2007-06-14 04:32:19 vtt Exp $
 * @see Idle
 * @see Idle#OUT
 * @see Alarm
 * @see net.sf.jukebox.sem.EventListener#eventNotification
 */
public interface IdleClient extends EventListener {

    /**
     * Idle time limit for this service. Milliseconds is a measurement unit.
     *
     * @return Idle time limit, in milliseconds.
     */
    public long idleLimit();
}