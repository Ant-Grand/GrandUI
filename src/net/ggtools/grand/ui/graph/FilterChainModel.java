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

package net.ggtools.grand.ui.graph;

import java.util.List;

import net.ggtools.grand.exceptions.GrandException;
import net.ggtools.grand.filters.FilterChain;
import net.ggtools.grand.filters.GraphFilter;
import net.ggtools.grand.graph.Graph;
import net.ggtools.grand.graph.GraphProducer;
import net.ggtools.grand.ui.event.Dispatcher;
import net.ggtools.grand.ui.event.EventManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A wrapper for a FilterChain.
 * 
 * @author Christophe Labouisse
 */
public class FilterChainModel implements GraphProducer, GraphModelListener {

    private final class FilterGraphRunnable implements Runnable {
        private final Log log = LogFactory.getLog(FilterGraphRunnable.class);

        public void run() {
            if (log.isDebugEnabled())
                    log.debug("Start filtering, filter chain size is: "
                            + filterChain.getFilterList().size());
            try {
                graph = filterChain.getGraph();
                notifyGraphAvailable();
            } catch (GrandException e) {
                // TODO Proper exception handling.
                log.error("Cannot filter graph", e);
                graph = null;
            }
        }
    }

    private static final Log log = LogFactory.getLog(FilterChainModel.class);

    private final Dispatcher eventDispatcher;

    private final EventManager eventManager;

    private final FilterChain filterChain;

    private Graph graph = null;

    private GraphModel graphModel;

    public FilterChainModel(GraphModel graphModel) {
        filterChain = new FilterChain();
        eventManager = new EventManager("FilterChainModel");
        try {
            eventDispatcher = eventManager.createDispatcher(FilterChainModelListener.class
                    .getDeclaredMethod("filteredGraphAvailable", new Class[]{Graph.class}));
        } catch (SecurityException e) {
            log.fatal("Caught exception initializing FilterChainModel", e);
            throw new RuntimeException("Cannot instanciate FilterChainModel", e);
        } catch (NoSuchMethodException e) {
            log.fatal("Caught exception initializing FilterChainModel", e);
            throw new RuntimeException("Cannot instanciate FilterChainModel", e);
        }
        this.graphModel = graphModel;
        graphModel.addListener(this);
        filterChain.setProducer(graphModel);
    }

    /**
     * @param newFilter
     */
    public void addFilterFirst(GraphFilter newFilter) {
        if (log.isDebugEnabled()) log.debug("Adding new head filter " + newFilter);
        filterChain.addFilterFirst(newFilter);
        startGraphUpdate();
    }

    /**
     * @param newFilter
     */
    public void addFilterLast(GraphFilter newFilter) {
        if (log.isDebugEnabled()) log.debug("Adding new tail filter " + newFilter);
        filterChain.addFilterLast(newFilter);
        startGraphUpdate();
    }

    /**
     * Add a new listener to this filter's events.
     * 
     * @param listener
     */
    public void addListener(FilterChainModelListener listener) {
        if (log.isDebugEnabled()) log.debug("Adding new listener " + listener);
        eventManager.subscribe(listener);
    }

    /**
     *  
     */
    public void clearFilters() {
        if (filterChain.getFilterList().size() > 0) {
            if (log.isDebugEnabled()) log.debug("Clearing filters");
            filterChain.clearFilters();
            startGraphUpdate();
        } else if (log.isDebugEnabled()) log.debug("Empty filter chain, not clearing");
    }

    /**
     * @return
     */
    public List getFilterList() {
        return filterChain.getFilterList();
    }

    /**
     * @return Returns the graph.
     */
    public final Graph getGraph() {
        return graph;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.GraphModelListener#newGraphLoaded(net.ggtools.grand.ui.graph.GraphEvent)
     */
    public void newGraphLoaded(GraphEvent event) {
        filterChain.clearFilters();
        startGraphUpdate();
    }

    /**
     * @param producer
     */
    public void setProducer(GraphProducer producer) {
        filterChain.setProducer(producer);
    }

    /**
     * Start an asynchronous update of the graph.
     */
    private final void startGraphUpdate() {
        // TODO Implements a queue.
        final Thread thread = new Thread(new FilterGraphRunnable(), "Graph filtering");
        thread.start();
    }

    protected void notifyGraphAvailable() {
        eventDispatcher.dispatch(graph);
    }

}