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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A wrapper for a FilterChain.
 *
 * @author Christophe Labouisse
 */
public class FilterChainModel implements GraphProducer {

    /**
     * Field log.
     */
    private static final Log log = LogFactory.getLog(FilterChainModel.class);

    /**
     * Field filterChain.
     */
    private final FilterChain filterChain;

    /**
     * Field graph.
     */
    private Graph graph = null;

    /**
     * Field graphModel.
     */
    @SuppressWarnings("unused")
    private GraphModel graphModel;

    /**
     * Constructor for FilterChainModel.
     * @param graphModel GraphModel
     */
    public FilterChainModel(final GraphModel graphModel) {
        filterChain = new FilterChain();
        this.graphModel = graphModel;
        filterChain.setProducer(graphModel);
    }

    /**
     * @param newFilter GraphFilter
     */
    public final void addFilterFirst(final GraphFilter newFilter) {
        if (log.isDebugEnabled()) {
            log.debug("Adding new head filter " + newFilter);
        }
        filterChain.addFilterFirst(newFilter);
        filterGraph();
    }

    /**
     * @param newFilter GraphFilter
     */
    public final void addFilterLast(final GraphFilter newFilter) {
        if (log.isDebugEnabled()) {
            log.debug("Adding new tail filter " + newFilter);
        }
        filterChain.addFilterLast(newFilter);
        filterGraph();
    }

    /**
     *
     */
    public final void clearFilters() {
        if (filterChain.getFilterList().size() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Clearing filters");
            }
            filterChain.clearFilters();
            filterGraph();
        }
        else if (log.isDebugEnabled()) {
            log.debug("Empty filter chain, not clearing");
        }
    }

    /**
     * @return List<GraphFilter>
     */
    public final List<GraphFilter> getFilterList() {
        return filterChain.getFilterList();
    }

    /**
     * @return Returns the graph.
     * @see net.ggtools.grand.graph.GraphProducer#getGraph()
     */
    public final Graph getGraph() {
        return graph;
    }

    /**
     * Method filterGraph.
     */
    public final void filterGraph() {
        if (log.isDebugEnabled()) {
            log.debug("Start filtering, filter chain size is: "
                    + filterChain.getFilterList().size());
        }
        try {
            graph = filterChain.getGraph();
        } catch (final GrandException e) {
            // TODO Proper exception handling.
            log.error("Cannot filter graph", e);
            graph = null;
        }
    }

    /**
     * @param producer GraphProducer
     */
    public final void setProducer(final GraphProducer producer) {
        filterChain.setProducer(producer);
    }
}
