package net.sf.jukebox.fsm;

/**
 * Describes the individual Finite State Machine state.
 *
 * @author <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a>
 * @version $Id: FsmState.java,v 1.1 2006/05/04 06:42:25 vt Exp $
 */
public interface FsmState {

    /**
     * @return A short name.
     */
    String getName();

    /**
     * @return Human readable state description.
     */
    String getDescription();
}
