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

import java.util.Iterator;
import java.util.LinkedList;

import net.ggtools.grand.graph.Graph;
import net.ggtools.grand.graph.Node;
import net.ggtools.grand.ui.Application;
import net.ggtools.grand.ui.GrandUiPrefStore;
import net.ggtools.grand.ui.prefs.PreferenceKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * The all singing all dancing class to provide content, label & decoration for
 * nodes.
 *
 * TODO change the data model to provide something better.
 * @author Christophe Labouisse
 */
public class GraphNodeContentProvider implements IStructuredContentProvider,
        ILabelProvider, IColorProvider {
    /**
     * Logger for this class.
     */
    @SuppressWarnings("unused")
    private static final Log LOG =
            LogFactory.getLog(GraphNodeContentProvider.class);

    /**
     * Field graph.
     */
    private Graph graph;

    /**
     *
     */
    public GraphNodeContentProvider() {
    }

    /**
     * Method addListener.
     * @param listener ILabelProviderListener
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(final ILabelProviderListener listener) {
        // TODO auto-generated method stub

    }

    /**
     * Method dispose.
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public final void dispose() {
        graph = null;
    }

    /**
     * Method getBackground.
     * @param element Object
     * @return Color
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    public final Color getBackground(final Object element) {
        if (element instanceof Node) {
            final Node node = (Node) element;
            final GrandUiPrefStore preferenceStore =
                    Application.getInstance().getPreferenceStore();
            if (node.equals(graph.getStartNode())) {
                return preferenceStore.getColor(PreferenceKeys.NODE_PREFIX + "start.fillcolor");
            }
            if (node.hasAttributes(Node.ATTR_MISSING_NODE)) {
                return preferenceStore.getColor(PreferenceKeys.NODE_PREFIX
                        + "missing.fillcolor");
            }
            if (node.hasAttributes(Node.ATTR_MAIN_NODE)) {
                return preferenceStore.getColor(PreferenceKeys.NODE_PREFIX + "main.fillcolor");
            }

            return preferenceStore.getColor(PreferenceKeys.NODE_PREFIX + "default.fillcolor");
        }

        return null;
    }

    /**
     * Method getElements.
     * @param inputElement Object
     * @return Object[]
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public final Object[] getElements(final Object inputElement) {
        if (graph == null) {
            return null;
        }

        final LinkedList<Node> list = new LinkedList<Node>();
        for (final Iterator<Node> iter = graph.getNodes(); iter.hasNext();) {
            list.add(iter.next());
        }

        return list.toArray();
    }

    /**
     * Method getForeground.
     * @param element Object
     * @return Color
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public final Color getForeground(final Object element) {
        if (element instanceof Node) {
            final Node node = (Node) element;
            final GrandUiPrefStore preferenceStore =
                    Application.getInstance().getPreferenceStore();
            if (node.equals(graph.getStartNode())) {
                return preferenceStore.getColor(PreferenceKeys.NODE_PREFIX + "start.fgcolor");
            }
            if (node.hasAttributes(Node.ATTR_MISSING_NODE)) {
                return preferenceStore.getColor(PreferenceKeys.NODE_PREFIX + "missing.fgcolor");
            }
            if (node.hasAttributes(Node.ATTR_MAIN_NODE)) {
                return preferenceStore.getColor(PreferenceKeys.NODE_PREFIX + "main.fgcolor");
            }

            return preferenceStore.getColor(PreferenceKeys.NODE_PREFIX + "default.fgcolor");
        }

        return null;
    }

    /**
     * Method getImage.
     * @param element Object
     * @return Image
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public final Image getImage(final Object element) {
        // TODO auto-generated method stub
        return null;
    }

    /**
     * Method getText.
     * @param element Object
     * @return String
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public final String getText(final Object element) {
        if (element == null) {
            return null;
        }

        if (element instanceof Node) {
            final Node node = (Node) element;
            return node.getName();
        }

        return element.toString();
    }

    /**
     * Method inputChanged.
     * @param viewer Viewer
     * @param oldInput Object
     * @param newInput Object
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public final void inputChanged(final Viewer viewer,
            final Object oldInput, final Object newInput) {
        // TODO auto-generated method stub
    }

    /**
     * Method isLabelProperty.
     * @param element Object
     * @param property String
     * @return boolean
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    public final boolean isLabelProperty(final Object element,
            final String property) {
        // TODO auto-generated method stub
        return false;
    }

    /**
     * Method removeListener.
     * @param listener ILabelProviderListener
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(final ILabelProviderListener listener) {
        // TODO auto-generated method stub
    }

    /**
     * @param graph Graph
     */
    final void setGraph(final Graph graph) {
        this.graph = graph;
    }
}
