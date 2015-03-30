package net.sf.jukebox.fsm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 * The Finite State Machine implementation.
 *
 * @author <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a>
 * @version $Id: FiniteStateMachine.java,v 1.8 2006/05/14 04:19:18 vt Exp $
 */
public abstract class FiniteStateMachine<Tcontext extends FsmContext, Tstate extends Enum & FsmState, Tevent extends FsmEvent, Toutput> {

    protected final Logger log = Logger.getLogger(getClass());
    /**
     * The finite state machine context.
     */
    private final Tcontext context;
    /**
     * The current FSM state.
     *
     * If this state ever becomes {@code null}, the FSM is considered finished working and all attempts to feed input
     * events into it will result in {@link IllegalStateException}.
     */
    private Tstate currentState;
    /**
     * The state handler map.
     */
    private final Map<Tstate, FsmStateHandler<Tcontext, Tstate, Tevent, Toutput>> stateHandlerMap =
            new HashMap<Tstate, FsmStateHandler<Tcontext, Tstate, Tevent, Toutput>>();
    /**
     * The output queue.
     *
     * Whatever is produced by the state machine is placed into this queue. No size checking is done.
     * Use {@link #getOutputQueue()} to get the queue contents.
     */
    private final BlockingQueue<Toutput> outputQueue = new LinkedBlockingQueue<Toutput>();

    /**
     * Create an instance.
     *
     * @param context The context. May be {@code null}.
     */
    protected FiniteStateMachine(Tcontext context) throws Throwable {

        this.context = context;

        FsmStateHandler<Tcontext, Tstate, Tevent, Toutput> initialStateHandler = getInitialStateHandler();
        register(initialStateHandler);
        currentState = initialStateHandler.getState(context);

        getHandler(currentState).enterState(context, outputQueue);
    }

    /**
     * Get the initial state.
     *
     * @return The initial state of the Finite State Machine upon creation.

     */
    protected abstract FsmStateHandler<Tcontext, Tstate, Tevent, Toutput> getInitialStateHandler();

    /**
     * Register a state handler.
     *
     * No attempts to check the sanity of the new handler, or the presence of a handler, are made.
     *
     * @param handler Handler to use to handle the given state from now on.
     */
    protected void register(FsmStateHandler<Tcontext, Tstate, Tevent, Toutput> handler) {

        log.info("State handler registered: " + handler.getState(context) + ": " + handler);
        stateHandlerMap.put(handler.getState(context), handler);
    }

    /**
     * Process the input event.
     *
     * @param event Event to process.
     */
    public synchronized void process(Tevent event) throws Throwable {

        NDC.push("process");
        try {
            log.debug("Current state: " + currentState);

            if (currentState == null) {
                throw new IllegalStateException("FSM has reached its final state");
            }

            Tstate nextState = getHandler(currentState).process(context, event, outputQueue);

            if (nextState != null && !nextState.equals(currentState)) {

                transition(nextState);
            }
        } finally {
            NDC.pop();
        }
    }

    private void transition(Tstate nextState) {

        NDC.push("transition");
        try {
            NDC.push("leaving");
            try {
                log.debug(currentState);

                try {
                    getHandler(currentState).leaveState(context, outputQueue);
                } catch (Throwable t) {
                    log.error("Failed to leave " + currentState + ", dropping into final state", t);
                    currentState = null;
                    return;
                }
            } finally {
                NDC.pop();
            }

            currentState = nextState;

            NDC.push("entering");
            try {
                log.debug(currentState);

                try {
                    getHandler(currentState).enterState(context, outputQueue);
                } catch (Throwable t) {
                    log.error("Failed to enter " + currentState + ", dropping into final state", t);
                    currentState = null;
                }
            } finally {
                NDC.pop();
            }
        } finally {
            NDC.pop();
        }
    }

    private FsmStateHandler<Tcontext, Tstate, Tevent, Toutput> getHandler(Tstate state) {

        FsmStateHandler<Tcontext, Tstate, Tevent, Toutput> stateHandler = stateHandlerMap.get(state);

        if (stateHandler == null) {
            throw new IllegalStateException("No handler for state " + state.getName() + " (" + state.getDescription() + ')');
        }

        return stateHandler;
    }

    public final BlockingQueue<Toutput> getOutputQueue() {
        return outputQueue;
    }

    protected final Tcontext getContext() {
        return context;
    }
}
