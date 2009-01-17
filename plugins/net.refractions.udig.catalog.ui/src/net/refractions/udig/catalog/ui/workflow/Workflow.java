package net.refractions.udig.catalog.ui.workflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

import net.refractions.udig.catalog.ui.CatalogUIPlugin;
import net.refractions.udig.catalog.ui.internal.Messages;
import net.refractions.udig.core.Pair;
import net.refractions.udig.ui.OffThreadProgressMonitor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Display;

/**
 * Basically is a state machine. It has a set of states and handles the stepping through the states
 * when next, previous or run are called.
 * <p>
 * This is an active object IE it has a thread and most method calls to the object are ran in that
 * thread. The method calls are blocking and if the call is in the display thread it is blocked in a
 * "nice" manner. IE the Display.readAndDispatch method is called.
 * 
 * @author justin
 * @since 1.0
 */
public class Workflow {

    /** set of primary states * */
    private State[] states;

    /** map of class to objects for states * */
    private Map<Class<State>, State> lookup;

    /** queue of primary states* */
    private LinkedList<State> queue;

    /** current state * */
    private State current;

    /** listeners * */
    private Set<Listener> listeners = new CopyOnWriteArraySet<Listener>();

    /** flag to indicate wither the pipe is started/finished * */
    boolean started = false;
    private boolean finished = false;

    /** context object, states use this object as a seed to perform work * */
    private Object context;

    /** A thread that drives the workflow */
    private WorkflowThread thread;

    /**
     * Creates an empty workflow. When using this constructor the states of the workflow must be set
     * before the workflow can be started.
     */
    public Workflow() {
        initThread();
    }
    private synchronized void initThread() {
        if (thread == null) {
            thread = new WorkflowThread();
            thread.setDaemon(true);
            thread.setName("Workflow Thread"); //$NON-NLS-1$
            thread.start();
        }
    }
    @Override
    protected void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }

    public void shutdown() {
        if (thread!=null && thread.running) {
            Runnable runnable = new Runnable(){

                public void run() {
                    thread.running = false;
                    thread = null;
                }

            };
            thread.requests.add(runnable);
        }
    }

    private void run( final Runnable runnable ) {
        if (Thread.currentThread() == thread) {
            runnable.run();
        } else {
            final boolean[] done = new boolean[1];
            final Throwable[] exception = new Throwable[1];
            thread.requests.add(new Runnable(){

                public void run() {
                    try {
                        runnable.run();
                    } catch (Throwable e) {
                        exception[0] = e;
                    } finally {
                        done[0] = true;
                    }
                }

            });

            Display display = Display.getCurrent();
            while( !done[0] ) {
                if (display == null || !display.readAndDispatch()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }

            if (exception[0] instanceof RuntimeException) {
                throw (RuntimeException) exception[0];
            } else if (exception[0] != null) {
                throw new RuntimeException(exception[0]);
            }
        }
    }

    /**
     * Creates a workflow from a set of workflow states.
     * 
     * @param states The states of the workflow.
     */
    public Workflow( State[] states ) {
        this();
        setStates(states);
    }

    /**
     * Adds a listener to the workflow. The listener collection is a set which prevents duplicates.
     * For this resason clients may call this method multiple times with the same object.
     * 
     * @param l The listening object.
     */
    public void addListener( Listener l ) {
        listeners.add(l);
    }

    public void removeListener( Listener l ) {
        listeners.remove(l);
    }

    /**
     * Returns an object representing a context for which the states can feed off of. The context is
     * often provided via a workbench selection.
     * 
     * @return The context object, or null if none has been set
     */
    public Object getContext() {
        return context;
    }

    /**
     * Sets the object representing a context for which states can feed off of. The context is often
     * provided via a workbench selection.
     * 
     * @param context The context object to set.
     */
    public void setContext( Object context ) {
        this.context = context;
    }

    /**
     * Sets the primary set of states of the workflow.
     * 
     * @param states An array of states.
     */
    @SuppressWarnings("unchecked")
    public void setStates( State[] states ) {
        int i2 = 0;
        if (states != null)
            i2 = states.length;
        State[] s = new State[i2];
        if (states != null)
            System.arraycopy(states, 0, s, 0, s.length);

        this.states = s;
        queue = new LinkedList<State>();
        lookup = new HashMap<Class<State>, State>();
        for( int i = 0; i < s.length; i++ ) {
            s[i].setWorkflow(this);
            queue.addLast(s[i]);
            lookup.put((Class<State>) s[i].getClass(), s[i]);
        }
    }

    /**
     * @return the primary set of states of the workflow.
     */
    public State[] getStates() {
        State[] s = new State[states.length];
        System.arraycopy(states, 0, s, 0, s.length);
        return s;
    }

    /**
     * Goes through the lookup values and find the first
     * match for the provided c.
     * 
     * @param <T> The type of the state.
     * @param c The class of the state.
     * @return The state instance, or null if none exists.
     */
    public <T> T getState( Class<T> c ) {
        
        State state = lookup.get(c);
        if( state == null ){
            // see if we have a subclass of the type
            for( State current : lookup.values() ) {
                if( c.isAssignableFrom(current.getClass())) {
                    state = current;
                    break;
                }
            }
        }
        return c.cast(state);
    }

    /**
     * Starts the workflow by moving to the first state. This method must only be called once. This
     * method executes asynchronously performing work in a seperate thread and does not block.
     */
    public void start() {
        start(new NullProgressMonitor());
    }

    /**
     * Starts the workflow by moving to the first state. This method must only be called once. This
     * method executes synchronously performing work in the current thread and blocks.
     */
    public void start( final IProgressMonitor monitor ) {
        final IProgressMonitor progressMonitor = checkMonitor(monitor);

        initThread();

        Runnable request = new Runnable(){
            public void run() {
                try {
                    // move to first state
                    current = queue.removeFirst();
                    current.setPrevious(null);
                    current.init(progressMonitor);

                    started = true;
                    dispatchStarted(current);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String toString() {
                return "Workflow.java start() task"; //$NON-NLS-1$
            }
        };

        run(request);
    }

    /**
     * This is an estimate of whether or not the Workflow can be run to completion.
     * 
     * @return true if it is likely that the workflow can be run to completion.
     */
    public boolean dryRun() {
        Queue<State> copiedQueue = new LinkedList<State>(queue);
        State state = getCurrentState();

        while( state != null ) {
            Pair<Boolean, State> dryRunResult = state.dryRun();
            if (!dryRunResult.getLeft()) {
                // the dry run predicts failure
                return false;
            }

            state = dryRunResult.getRight();
            if (state == null) {
                state = copiedQueue.poll();
            }
        }

        // we finished with no failures
        return true;
    }
    /**
     * Moves the workflow to the next state. This method executes asynchronously performing work in
     * a seperate thread and does not block.
     */
    public void next() {
        next(new NullProgressMonitor());
    }

    /**
     * Moves the workflow to the next state. This method executes synchronously performing work in
     * the current thread and blocks.
     * 
     * @return True if the state
     */
    public void next( final IProgressMonitor monitor ) {

        final IProgressMonitor progressMonitor = checkMonitor(monitor);

        Runnable request = new Runnable(){
            public void run() {
                doNextInternal(progressMonitor);
            }

            @Override
            public String toString() {
                return "Workflow.java run() task"; //$NON-NLS-1$
            }
        };

        run(request);
    }
    private IProgressMonitor checkMonitor( final IProgressMonitor monitor ) {
        if (monitor == null) {
            throw new NullPointerException("monitor is null"); //$NON-NLS-1$
        }

        final IProgressMonitor progressMonitor;
        if (monitor instanceof OffThreadProgressMonitor) {
            progressMonitor = monitor;
        } else {
            progressMonitor = new OffThreadProgressMonitor(monitor);
        }
        return progressMonitor;
    }

    @SuppressWarnings("unchecked")
    private void doNextInternal( IProgressMonitor monitor ) {

        try {
            assertStarted();
            assertNotFinished();

            String name = getCurrentState().getName();
            String string = name != null ? name : Messages.Workflow_busy;
            monitor.beginTask(string, 20);
            monitor.setTaskName(string);

            if (queue == null) {
                String msg = "No states"; //$NON-NLS-1$
                throw new IllegalStateException(msg);
            }

            // run it
            boolean ok = false;
            SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 10);
            try {
                ok = current.run(subProgressMonitor) && !monitor.isCanceled();
            } catch (Throwable t) {
                CatalogUIPlugin.log(t.getLocalizedMessage(), t);
            } finally {
                subProgressMonitor.done();
            }

            if (ok) {
                // dispatch the event
                dispatchPassed(current);

                // grab the next state, try pulling one from the current state
                State next = current.next();
                if (next == null) {
                    // try pulling from the queue
                    if (!queue.isEmpty())
                        next = queue.removeFirst();
                } else {
                    // add to lookup tables
                    lookup.put((Class<State>) next.getClass(), next);
                }

                if (next != null) {
                    // set the back pointer and call lifecyclmutexe events
                    next.setWorkflow(this);
                    next.setPrevious(current);
                    try {
                        subProgressMonitor = new SubProgressMonitor(monitor, 10);
                        next.init(subProgressMonitor);
                    } finally {
                        subProgressMonitor.done();
                    }
                    State prev = current;
                    current = next;

                    dispatchForward(current, prev);
                } else {
                    // no more states, we are finished
                    State last = current;
                    current = null;

                    finished = true;
                    shutdown();
                    dispatchFinished(last);
                }
            } else {
                // run did not succeed
                dispatchFailed(current);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Moves the workflow to the previous state. This method executes asynchronously performing work
     * in a seperate thread and does not block.
     */
    public void previous() {
        previous(new NullProgressMonitor());
    }

    /**
     * Moves the workflow to the previous state. This method executes synchronously performing work
     * in the current thread and blocks.
     */
    public void previous( final IProgressMonitor monitor ) {
        final IProgressMonitor progressMonitor = checkMonitor(monitor);

        Runnable request = new Runnable(){
            public void run() {
                try {
                    assertStarted();
                    assertNotFinished();

                    if (current.getPreviousState() != null) {
                        // if this state is a "primary" state, place back in front of queue
                        if (isPrimaryState(current))
                            queue.addFirst(current);

                        State next = current;
                        current = current.getPreviousState();

                        // renitialize the state and dispatch the started event
                        current.init(progressMonitor);
                        dispatchBackward(current, next);

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String toString() {
                return "Workflow.java run() task"; //$NON-NLS-1$
            }
        };

        run(request);
    }

    /**
     * @return True if the workflow has been started with a call to #start().
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * @return True if the workflow has been finished. The workflow is considered finished after the
     *         call to #next(), while in the final state.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @return the current state of the workflow.
     */
    public State getCurrentState() {
        return current;
    }

    /**
     * Determines if the workflow has more states. It is important to note that this method may not
     * 100% accurate depending on the behaviour of states dynamically creating new states.
     * 
     * @return True if there are more states, otherwise false.
     */
    public boolean hasMoreStates() {
        // if the queue is not empty, we definitely have more states
        if (!queue.isEmpty())
            return true;

        // ask the current state
        if (current != null)
            return current.hasNext();

        return false;
    }
    /**
     * Runs the workflow from its current state. The workflow will continue to walk through the
     * states while the state is finished.
     * 
     * @param monitor A progress monitor.
     * @return True if the pipe was able to run to completion, otherwise false.
     */
    public boolean run( IProgressMonitor monitor ) {
        if (monitor == null) {
            throw new NullPointerException("monitor is null"); //$NON-NLS-1$
        }

        WorkflowRunner runner = new WorkflowRunner(this);
        return runner.run(monitor);
    }

    /**
     * Resets the workflow. This method may only be called if the workflow is in a finished state.
     * Once reset the workflow lifecycle starts again with a call to
     * 
     * @see DataPipeline#start().
     */
    public void reset() {
        assertFinished();

        started = finished = false;
        setStates(states);
    }

    protected void assertStarted() {
        if (!started) {
            String msg = "Not started"; //$NON-NLS-1$
            throw new IllegalStateException(msg);
        }
    }

    protected void assertNotStarted() {
        if (started) {
            String msg = "Already started"; //$NON-NLS-1$
            throw new IllegalStateException(msg);
        }
    }

    protected void assertFinished() {
        if (!finished) {
            String msg = "Not finished"; //$NON-NLS-1$
            throw new IllegalStateException(msg);
        }
    }

    protected void assertNotFinished() {
        if (finished) {
            String msg = "Already finished"; //$NON-NLS-1$
            throw new IllegalStateException(msg);
        }
    }

    protected boolean isPrimaryState( State state ) {
        for( int i = 0; i < states.length; i++ ) {
            if (states[i].equals(state))
                return true;
        }

        return false;
    }

    protected void dispatchStarted( final State start ) {
        for( final Listener l : listeners ) {
            l.started(start);
        }
    }

    protected void dispatchForward( final State current, final State prev ) {
        for( final Listener l : listeners ) {
            l.forward(current, prev);
        }
    }

    protected void dispatchBackward( final State current, final State next ) {
        for( final Listener l : listeners ) {
            l.backward(current, next);
        }
    }

    protected void dispatchPassed( final State state ) {
        for( final Listener l : listeners ) {
            l.statePassed(state);
        }
    }

    protected void dispatchFailed( final State state ) {
        for( final Listener l : listeners ) {
            l.stateFailed(state);
        }
    }

    protected void dispatchFinished( final State last ) {
        for( final Listener l : listeners ) {
            l.finished(last);
        }
    }

    private final static class WorkflowThread extends Thread {
        private final BlockingQueue<Runnable> requests = new LinkedBlockingQueue<Runnable>();
        private volatile boolean running = true;
        @Override
        public void run() {
            while( running ) {
                Runnable runnable;
                try {
                    runnable = requests.take();
                } catch (InterruptedException e) {
                    continue;
                }
                runnable.run();
            }
        }
    }

    public static class WorkflowRunner implements Listener {
        Workflow pipe;
        boolean stopped;

        WorkflowRunner( Workflow pipe ) {
            this.pipe = pipe;
        }

        public boolean run( final IProgressMonitor monitor ) {
            final boolean[] result = new boolean[1];

            // run in the Workflow thread
            pipe.run(new Runnable(){
                public void run() {
                    result[0] = runInternal(monitor);
                }
            });
            return result[0];
        }

        private boolean runInternal( IProgressMonitor monitor ) {
            try {
                monitor.beginTask(Messages.Workflow_task_name, IProgressMonitor.UNKNOWN);
                stopped = false;
                pipe.addListener(this);

                // first check if the pipe is already finished
                if (pipe.isFinished())
                    return true;

                // may need to start
                if (!pipe.isStarted()) {
                    pipe.start(monitor);
                }

                while( !stopped && !pipe.isFinished() ) {
                    pipe.next(new SubProgressMonitor(monitor, 10,
                            SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
                }

                pipe.removeListener(this);
                return !stopped;
            } finally {
                monitor.done();
            }
        }

        public void forward( State current, State prev ) {
            // do nothing
        }

        public void backward( State current, State next ) {
            // do nothing
        }

        public void statePassed( State state ) {
            // do nothing
        }

        public void stateFailed( State state ) {
            stopped = true;
        }

        public void started( State first ) {
            // do nothing
        }

        public void finished( State last ) {
            // do nothing
        }
    }

}