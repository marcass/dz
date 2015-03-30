package net.sf.jukebox.fsm;

import java.util.concurrent.BlockingQueue;

/**
 * The Finite State Machine state handler.
 *
 * @author <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a>
 * @version $Id: FsmStateHandler.java,v 1.5 2006/05/14 04:19:18 vt Exp $
 */
public interface FsmStateHandler<Tcontext extends FsmContext, Tstate extends FsmState, Tevent extends FsmEvent, Toutput> {

    /**
     * Process the event.
     *
     * @param context Finite state machine context.
     * @param event Event to process.
     * @param outputQueue Queue to put output objects into, if any.
     * @return State FSM must transition itself to. {@code null} means no state change is required.
     */
    Tstate process(Tcontext context, Tevent event, BlockingQueue<Toutput> outputQueue) throws Throwable;

    /**
     * Enter the state corresponding to this handler.
     *
     * @param context Finite state machine context.
     * @param outputQueue Queue to put output objects into, if any.
     */
    void enterState(Tcontext context, BlockingQueue<Toutput> outputQueue) throws InterruptedException, Throwable;

    /**
     * Leave the state corresponding to this handler.
     *
     * @param context Finite state machine context.
     * @param outputQueue Queue to put output objects into, if any.
     */
    void leaveState(Tcontext context, BlockingQueue<Toutput> outputQueue) throws Throwable;

    /**
     * @return The state this handler is associated with.
     * @param context
     */
    Tstate getState(Tcontext context);
}
