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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author clabouisse
 */
public class EventDispatcher implements Runnable {
    private final class DispatchEventAction implements Runnable {
        private final Object event;

        private final Method method;

        public DispatchEventAction(Object event, Method method) {
            this.event = event;
            this.method = method;
        }

        /**
         * Dispatch one event.
         * 
         * @see EventDispatcher#dispatchOneEvent(Object, Method)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            dispatchOneEvent(event, method);
        }
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
         * @see EventDispatcher#doUnsubscription(Object)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            doUnsubscription(subscriber);
        }
    }

    private static final Log log = LogFactory.getLog(EventDispatcher.class);

    private boolean defaultDispatchAsynchronous = true;

    private Thread dispatcherThread;

    private final LinkedList eventQueue = new LinkedList();

    private final LinkedList listenerList = new LinkedList();

    private final String name;

    /**
     * Creates a event dispatcher. The created dispatcher will have the
     * "Anonymous" name and will log the dispatching process.
     */
    public EventDispatcher() {
        this("Anonymous");
    }

    /**
     * Creates an named event dispatcher. The dispatching process will be
     * logged.
     * 
     * @param name
     */
    public EventDispatcher(String name) {
        this.name = name;
        this.dispatcherThread = new Thread(this, "Dispatcher thread " + name);
        dispatcherThread.start();
    }

    /**
     * Dispatch an event. The dispatching will be either synchronous or
     * asynchronous depending of the <code>defaultDispatchAnsynchronous</code>
     * attributes. The default is to use synchronous event dispatching.
     * 
     * @param event
     * @param method
     */
    public void dispatchEvent(final Object event, final Method method) {
        if (defaultDispatchAsynchronous) {
            asynchronousDispatchEvent(event, method);
        } else {
            synchronousDispatchEvent(event, method);
        }
    }

    /**
     * Dispatch an event asynchronously. This method garanties that the events
     * will be processed in the order of reception.
     * 
     * @param event
     * @param method
     */
    public void asynchronousDispatchEvent(final Object event, final Method method) {
        synchronized (eventQueue) {
            eventQueue.add(new DispatchEventAction(event, method));
            eventQueue.notify();
        }
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
     * Dispatch an event synchronously.
     * 
     * @param event
     * @param method
     */
    public void synchronousDispatchEvent(final Object event, final Method method) {
        dispatchOneEvent(event, method);
    }

    /**
     * @return String
     */
    public String getName() {
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
     * Dispatch one event to the current subscriber.
     * 
     * @param event
     * @param method
     */
    private void dispatchOneEvent(Object event, Method method) {
        log.trace("Dispatching "+event+" to "+method);
        synchronized (listenerList) {
            for (Iterator iterator = listenerList.iterator(); iterator.hasNext();) {
                WeakReference weakReference = (WeakReference) iterator.next();
                Object element = weakReference.get();

                if (element != null) {
                    try {
                        method.invoke(element, new Object[]{event});
                    } catch (IllegalAccessException e) {
                        log.fatal(name + " dispatchOneEvent", e);
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        log.error(name + " dispatchOneEvent", e);
                        throw new RuntimeException(e.getCause());
                    }
                } else {
                    // Remove the listener since it has been garbage collected.
                    if (log.isDebugEnabled()) log.debug("Removing weak reference " + weakReference);
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

}
