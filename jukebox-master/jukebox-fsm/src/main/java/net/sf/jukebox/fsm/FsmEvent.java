package net.sf.jukebox.fsm;

import java.util.EventObject;

/**
 * Finite State Machine input event.
 *
 * This is a wrapper for {@link EventObject} to allow multiple bound type for {@link FiniteStateMachine}.
 *
 * @author <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a>
 * @version $Id: FsmEvent.java,v 1.2 2006/05/05 05:57:25 vt Exp $
 */
public class FsmEvent<T extends Enum> extends EventObject {

    private final T eventType;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     *
     * @throws IllegalArgumentException if source is null.
     */
    public FsmEvent(Object source, T eventType) {
        super(source);

        this.eventType = eventType;
    }

    /**
     * @return The event type, for easier consumption by {@link FsmStateHandler#process} .
     */
    public final T getEventType() {
        return eventType;
    }

    public String toString() {
        return super.toString() + "+type=" + eventType;
    }
}
