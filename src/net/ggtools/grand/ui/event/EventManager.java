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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Christophe Labouisse
 */
public class EventManager implements Runnable {

    private final class DispatchEventAction implements Runnable {

        private final InternalDispatcher dispatcher;

        private final Object event;

        public DispatchEventAction(final Object event, final InternalDispatcher dispatcher) {
            this.event = event;
            this.dispatcher = dispatcher;
        }

        /**
         * Dispatch one event.
         * 
         * @see EventManager#dispatchOneEvent(Object, Method)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            dispatchOneEvent(event, dispatcher);
        }
    }

    /**
     * Internal interface to be implemented by objects actually dispatching the
     * events to the subscribers.
     * 
     * @author Christophe Labouisse
     */
    interface InternalDispatcher {
        /**
         * Send one event to one subscriber.
         * 
         * @param subscriber
         * @param eventData
         */
        void sendEventToSubscriber(final Object subscriber, final Object eventData);

    }

    private final class SubscriptionAction implements Runnable {
        private Object subscriber;

        public SubscriptionAction(Object subscriber) {
            this.subscriber = subscriber;
        }

        /**
         * Add the subscriber to the event's dispatcher subscribtion list.
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            doSubscribtion(subscriber);
        }
    }

    private final class UnsubscriptionAction implements Runnable {
        private Object subscriber;

        public UnsubscriptionAction(Object subscriber) {
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

    private static final Log log = LogFactory.getLog(EventManager.class);

    private boolean defaultDispatchAsynchronous = true;

    private final DispatcherFactory dispatcherFactory;

    private Thread dispatcherThread;

    private final LinkedList eventQueue = new LinkedList();

    private final LinkedList listenerList = new LinkedList();

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
     * @param name
     */
    public EventManager(final String name) {
        this.name = name;
        this.dispatcherThread = new Thread(this, "Dispatcher thread " + name);
        dispatcherThread.start();
        dispatcherFactory = DispatcherFactory.getInstance();
    }

    public Dispatcher createDispatcher(final Method method) {
        return dispatcherFactory.createDispatcher(this, method);
    }

    /**
     * @return String
     */
    final public String getName() {
        return name;
    }

    /**
     * @return boolean
     */
    public boolean isDefaultDispatchAnsynchronous() {
        return defaultDispatchAsynchronous;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
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
                        nextEvent = (Runnable) eventQueue.removeFirst();
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
            } catch (InterruptedException e) {
                if (log.isTraceEnabled()) log.trace("Event queue watch interrupted");
            }
        }
    }

    /**
     * Sets the defaultDispatchAnsynchronous.
     * 
     * @param defaultDispatchAnsynchronous
     *            The defaultDispatchAnsynchronous to set
     */
    public void setDefaultDispatchAnsynchronous(boolean defaultDispatchAnsynchronous) {
        this.defaultDispatchAsynchronous = defaultDispatchAnsynchronous;
    }

    /**
     * Add a new listener.
     * 
     * @param listener
     */
    public void subscribe(Object listener) {
        synchronized (eventQueue) {
            eventQueue.add(new SubscriptionAction(listener));
        }
    }

    public void unSubscribe(Object listener) {
        synchronized (eventQueue) {
            eventQueue.add(new UnsubscriptionAction(listener));
        }
    }

    /**
     * Dispatch an event asynchronously. This method garanties that the events
     * will be processed in the order of reception.
     * 
     * @param event
     * @param method
     */
    private final void asynchronousDispatchEvent(final Object event,
            final InternalDispatcher dispatcher) {
        synchronized (eventQueue) {
            eventQueue.add(new DispatchEventAction(event, dispatcher));
            eventQueue.notify();
        }
    }

    /**
     * Dispatch one event to the current subscriber.
     * 
     * @param eventData
     * @param method
     */
    private void dispatchOneEvent(final Object eventData, final InternalDispatcher dispatcher) {
        log.trace("Dispatching " + eventData + " to " + dispatcher);
        synchronized (listenerList) {
            for (Iterator iterator = listenerList.iterator(); iterator.hasNext();) {
                WeakReference weakReference = (WeakReference) iterator.next();
                Object subscriber = weakReference.get();

                if (subscriber != null) {
                    dispatcher.sendEventToSubscriber(subscriber, eventData);
                } else {
                    // Remove the listener since it has been garbage collected.
                    if (log.isDebugEnabled())
                            log.debug("Removing weak reference " + weakReference);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Add a new subscriber to the dispatch list. This method will do nothing if
     * the subscriber is already in the list.
     * 
     * @param listener
     */
    private void doSubscribtion(Object listener) {
        if (log.isDebugEnabled()) log.debug(name + " subscribing " + listener);

        synchronized (listenerList) {
            listenerList.add(new WeakReference(listener));
        }
    }

    /**
     * Removes a subscriber from the list. Does nothing if the subscriber is not
     * in the list.
     * 
     * @param listener
     */
    private void doUnsubscription(Object listener) {
        if (log.isDebugEnabled()) log.debug(name + " unsubscribing " + listener);

        synchronized (listenerList) {
            for (Iterator iterator = listenerList.iterator(); iterator.hasNext();) {
                WeakReference weakRef = (WeakReference) iterator.next();

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
     * @param event
     * @param method
     */
    private final void synchronousDispatchEvent(final Object event,
            final InternalDispatcher dispatcher) {
        dispatchOneEvent(event, dispatcher);
    }

    /**
     * Dispatch an event. The dispatching will be either synchronous or
     * asynchronous depending of the <code>defaultDispatchAnsynchronous</code>
     * attributes. The default is to use synchronous event dispatching.
     * 
     * @param eventData
     * @param dispatcher
     */
    final void dispatchEvent(final Object eventData, final InternalDispatcher dispatcher) {
        if (defaultDispatchAsynchronous) {
            asynchronousDispatchEvent(eventData, dispatcher);
        } else {
            synchronousDispatchEvent(eventData, dispatcher);
        }
    }

}