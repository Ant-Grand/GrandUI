// $Id$
/*
 * ====================================================================
 * Copyright (c) 2002-2003, Christophe Labouisse All rights reserved.
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

package net.ggtools.grand.ui.graph;

import java.io.File;

import net.ggtools.grand.ant.AntProject;
import net.ggtools.grand.exceptions.GrandException;
import net.ggtools.grand.graph.Graph;
import net.ggtools.grand.graph.GraphProducer;
import net.ggtools.grand.ui.event.Dispatcher;
import net.ggtools.grand.ui.event.EventManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A UI friendly wrapper over the Grand core task.
 * 
 * @author Christophe Labouisse
 */
public class GraphModel implements GraphProducer {
    static {
        // disable logs in Grand core
        net.ggtools.grand.Log.setLogLevel(net.ggtools.grand.Log.MSG_ERR);
    }

    private final class LoadFileRunnable implements Runnable {
        private final Log log = LogFactory.getLog(LoadFileRunnable.class);

        private final String fileName;

        private LoadFileRunnable(final String fileName) {
            this.fileName = fileName;
        }

        public void run() {
            if (log.isDebugEnabled()) log.debug("Loading " + fileName);
            lastLoadedFile = fileName;
            final GraphProducer p = new AntProject(new File(fileName));
            synchronized (GraphModel.this) {
                producer = p;
            }
            notifyGraphLoaded();
        }
    }

    private static final Log log = LogFactory.getLog(GraphModel.class);

    private GraphProducer producer = null;

    private String lastLoadedFile;

    private final EventManager eventManager;

    private final Dispatcher eventDispatcher;

    public GraphModel() {
        eventManager = new EventManager("GraphModel");
        try {
            eventDispatcher = eventManager.createDispatcher(GraphModelListener.class
                    .getDeclaredMethod("newGraphLoaded", new Class[]{GraphEvent.class}));
        } catch (SecurityException e) {
            log.fatal("Caught exception initializing GraphModel", e);
            throw new RuntimeException("Cannot instanciate GraphModel", e);
        } catch (NoSuchMethodException e) {
            log.fatal("Caught exception initializing GraphModel", e);
            throw new RuntimeException("Cannot instanciate GraphModel", e);
        }
    }

    public void openFile(final String fileName) {
        final Thread thread = new Thread(new LoadFileRunnable(fileName), "File loading");
        thread.start();
    }
    
    public void reload() {
        if (lastLoadedFile != null) {
            if (log.isDebugEnabled()) log.debug("Reloading last file");
            openFile((lastLoadedFile));
        }
        else {
            log.info("No file previously loaded, skipping reload");
        }
    }

    public void addListener(GraphModelListener listener) {
        eventManager.subscribe(listener);
    }

    /**
     * @return Returns the currentGraph.
     */
    public final Graph getGraph() {
        Graph graph = null;
        if (producer != null) {
            try {
                graph = producer.getGraph();
            } catch (GrandException e) {
                // TODO Proper exception handling.
                log.error("Cannot build Grand graph", e);
            }
        }
        return graph;
    }

    protected void notifyGraphLoaded() {
        final GraphEvent event = new GraphEvent(this);
        eventDispatcher.dispatch(event);
    }
}