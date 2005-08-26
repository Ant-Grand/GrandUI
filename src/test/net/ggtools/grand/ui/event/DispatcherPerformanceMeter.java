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

import sf.blacksun.util.StopWatch;

/**
 * @author Christophe Labouisse
 */
public class DispatcherPerformanceMeter {

    private static final int LOOP = 100000000;

    /**
     * @author Christophe Labouisse
     */
    public static class ManualDispatcher extends DispatcherAdapter implements Dispatcher {

        /**
         * @param manager
         */
        ManualDispatcher(EventManager manager) {
            super(manager);
        }

        /*
         * (non-Javadoc)
         * 
         * @see net.ggtools.grand.ui.event.EventManager.InternalDispatcher#sendEventToSubscriber(java.lang.Object,
         *      java.lang.Object)
         */
        public void sendEventToSubscriber(Object subscriber, Object eventData) {
            ((Listener) subscriber).listen(eventData);
        }

    }

    /**
     * A dummy listener for performance testing.
     * 
     * @author Christophe Labouisse
     */
    public static class Listener {
        public void listen(Object o) {
        }
    }

    public static void main(String[] args) throws SecurityException, NoSuchMethodException {
        final StopWatch timer = new StopWatch();
        final Listener subscriber = new Listener();
        System.out.println("Testing manual dispatcher");
        final ManualDispatcher manualDispatcher = new ManualDispatcher(null);
        timer.start();
        for (int i = 0; i < LOOP; i++) {
            manualDispatcher.sendEventToSubscriber(subscriber, "Test data");
        }
        timer.stop();
        System.out.println(" -> " + timer);
        System.out.println("Testing simple dispatcher");
        final SimpleDispatcher simpleDispatcher = new SimpleDispatcher(null, Listener.class
                .getDeclaredMethod("listen", new Class[]{Object.class}));
        timer.reset();
        timer.start();
        for (int i = 0; i < LOOP; i++) {
            simpleDispatcher.sendEventToSubscriber(subscriber, "Test data");
        }
        timer.stop();
        System.out.println(" -> " + timer);
    }
}