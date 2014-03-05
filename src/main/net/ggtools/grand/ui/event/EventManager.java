// $Id$
/*
 * ====================================================================
 * Copyright (c) 2002-2004, Christophe Labouisse All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.ggtools.grand.ui.event;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Christophe Labouisse
 */
public class EventManager implements Runnable {

    /**
     * @author Christophe Labouisse
     */
    private final class DispatchEventAction implements Runnable {

        /**
         * Field dispatcher.
         */
        private final Dispatcher dispatcher;

        /**
         * Field event.
         */
        private final Object event;

        /**
         * Constructor for DispatchEventAction.
         * @param event Object
         * @param dispatcher Dispatcher
         */
        public DispatchEventAction(final Object event, final Dispatcher dispatcher) {
            this.event = event;
            this.dispatcher = dispatcher;
        }

        /**
         * Dispatch one event.
         *
         * @see EventManager#dispatchOneEvent(Object, Dispatcher)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            dispatchOneEvent(event, dispatcher);
        }
    }

    /**
     * @author Christophe Labouisse
     */
    private final class SubscriptionAction implements Runnable {
        /**
         * Field subscriber.
         */
        private Object subscriber;

        /**
         * Constructor for SubscriptionAction.
         * @param subscriber Object
         */
        public SubscriptionAction(final Object subscriber) {
            this.subscriber = subscriber;
        }

        /**
         * Add the subscriber to the event's dispatcher subscription list.
         *
         * @see java.lang.Runnable#run()
         */
        public void run() {
            doSubscribtion(subscriber);
        }
    }

    /**
     * @author Christophe Labouisse
     */
    private final class UnsubscriptionAction implements Runnable {
        /**
         * Field subscriber.
         */
        private Object subscriber;

        /**
         * Constructor for UnsubscriptionAction.
         * @param subscriber Object
         */
        public UnsubscriptionAction(final Object subscriber) {
            this.subscriber = subscriber;
        }

        /**
         * Add the subscriber to the event's dispatcher subscribtion list.
         *
         * @see EventManager#doUnsubscription(Object)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            doUnsubscription(subscriber);
        }
    }

    /**
     * Field log.
     */
    private static final Log LOG = LogFactory.getLog(EventManager.class);

    /**
     * Field defaultDispatchAsynchronous.
     */
    private boolean defaultDispatchAsynchronous = true;

    /**
     * Field dispatcherFactory.
     */
    private final DispatcherFactory dispatcherFactory;

    /**
     * Field dispatcherThread.
     */
    private Thread dispatcherThread;

    /**
     * Field eventQueue.
     */
    private final LinkedList<Runnable> eventQueue = new LinkedList<Runnable>();

    /**
     * Field listenerList.
     */
    private final List<WeakReference<Object>> listenerList =
            new LinkedList<WeakReference<Object>>();

    /**
     * Field name.
     */
    private final String name;

    /**
     * Creates a event dispatcher. The created dispatcher will have the
     * "Anonymous" name and will log the dispatching process.
     */
    public EventManager() {
        this("Anonymous");
    }

    /**
     * Creates an named event dispatcher. The dispatching process will be
     * logged.
     *
     * @param name String
     */
    public EventManager(final String name) {
        this.name = name;
        dispatcherThread = new Thread(this, "Dispatcher thread " + name);
        dispatcherThread.start();
        dispatcherFactory = DispatcherFactory.getInstance();
    }

    /**
     * Remove all subscribers and all pending actions from the queue.
     */
    public final void clear() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Clearing event manager");
        }
        synchronized (eventQueue) {
            eventQueue.clear();
        }

        synchronized (listenerList) {
            listenerList.clear();
        }

    }

    /**
     * Creates a new dispatcher calling a specific method when invoked.
     * @param method method to call on invocation
     * @return a new dispatcher.
     */
    public final Dispatcher createDispatcher(final Method method) {
        return dispatcherFactory.createDispatcher(this, method);
    }

    /**
     * @return String
     */
    public final String getName() {
        return name;
    }

    /**
     * @return boolean
     */
    public final boolean isDefaultDispatchAnsynchronous() {
        return defaultDispatchAsynchronous;
    }

    /**
     * Method run.
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        // Main loop.
        while (true) {
            Runnable nextEvent;

            // Process the events in queue.
            do {
                nextEvent = null;

                // In order to avoid deadlocks, we only synchronize to the
                // eventQueue
                // when getting the next event.
                synchronized (eventQueue) {
                    if (!eventQueue.isEmpty()) {
                        nextEvent = eventQueue.removeFirst();
                    }
                }

                // Run should not be called from a synchronized section.
                if (nextEvent != null) {
                    nextEvent.run();
                }
            } while (nextEvent != null);

            try {
                synchronized (eventQueue) {
                    // Wait for more events to come.
                    eventQueue.wait();
                }
            } catch (final InterruptedException e) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Event queue watch interrupted");
                }
            }
        }
    }

    /**
     * Sets the defaultDispatchAnsynchronous.
     *
     * @param defaultDispatchAnsynchronous
     *            The defaultDispatchAnsynchronous to set
     */
    public final void setDefaultDispatchAnsynchronous(final boolean defaultDispatchAnsynchronous) {
        defaultDispatchAsynchronous = defaultDispatchAnsynchronous;
    }

    /**
     * Add a new listener.
     *
     * @param listener Object
     */
    public final void subscribe(final Object listener) {
        synchronized (eventQueue) {
            eventQueue.add(new SubscriptionAction(listener));
        }
    }

    /**
     * Method unSubscribe.
     * @param listener Object
     */
    public final void unSubscribe(final Object listener) {
        synchronized (eventQueue) {
            eventQueue.add(new UnsubscriptionAction(listener));
        }
    }

    /**
     * Dispatch an event asynchronously. This method guarantees that the events
     * will be processed in the order of reception.
     *
     * @param event Object
     * @param dispatcher Dispatcher
     */
    private void asynchronousDispatchEvent(final Object event,
            final Dispatcher dispatcher) {
        synchronized (eventQueue) {
            eventQueue.add(new DispatchEventAction(event, dispatcher));
            eventQueue.notify();
        }
    }

    /**
     * Dispatch one event to the current subscriber.
     *
     * @param eventData Object
     * @param dispatcher Dispatcher
     */
    private void dispatchOneEvent(final Object eventData,
            final Dispatcher dispatcher) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start dispatching to " + dispatcher);
        }
        synchronized (listenerList) {
            for (final Iterator<WeakReference<Object>> iterator = listenerList.iterator(); iterator.hasNext();) {
                final WeakReference<Object> weakReference = iterator.next();
                final Object subscriber = weakReference.get();

                if (subscriber != null) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Dispatching " + eventData + " to " + subscriber);
                    }
                    dispatcher.sendEventToSubscriber(subscriber, eventData);
                } else {
                    // Remove the listener since it has been garbage collected.
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Removing weak reference " + weakReference);
                    }
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Add a new subscriber to the dispatch list. This method will do nothing if
     * the subscriber is already in the list.
     *
     * @param listener Object
     */
    private void doSubscribtion(final Object listener) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(name + " subscribing " + listener);
        }

        synchronized (listenerList) {
            listenerList.add(new WeakReference<Object>(listener));
        }
    }

    /**
     * Removes a subscriber from the list. Does nothing if the subscriber is not
     * in the list.
     *
     * @param listener Object
     */
    private void doUnsubscription(final Object listener) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(name + " unsubscribing " + listener);
        }

        synchronized (listenerList) {
            for (final Iterator<WeakReference<Object>> iterator = listenerList.iterator(); iterator.hasNext();) {
                final WeakReference<Object> weakRef = iterator.next();

                if (weakRef.get() == listener) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    /**
     * Dispatch an event synchronously.
     *
     * @param event Object
     * @param dispatcher Dispatcher
     */
    private void synchronousDispatchEvent(final Object event,
            final Dispatcher dispatcher) {
        dispatchOneEvent(event, dispatcher);
    }

    /**
     * Dispatch an event. The dispatching will be either synchronous or
     * asynchronous depending of the <code>defaultDispatchAnsynchronous</code>
     * attributes. The default is to use synchronous event dispatching.
     *
     * @param eventData Object
     * @param dispatcher Dispatcher
     */
    final void dispatchEvent(final Object eventData,
            final Dispatcher dispatcher) {
        if (defaultDispatchAsynchronous) {
            asynchronousDispatchEvent(eventData, dispatcher);
        } else {
            synchronousDispatchEvent(eventData, dispatcher);
        }
    }

}
