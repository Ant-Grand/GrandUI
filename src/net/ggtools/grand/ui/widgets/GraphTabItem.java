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
package net.ggtools.grand.ui.widgets;

import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ggtools.grand.ant.AntTargetNode;
import net.ggtools.grand.ant.AntTargetNode.SourceElement;
import net.ggtools.grand.ui.Application;
import net.ggtools.grand.ui.graph.GraphControler;
import net.ggtools.grand.ui.graph.GraphControlerListener;
import net.ggtools.grand.ui.graph.GraphDisplayer;
import net.ggtools.grand.ui.graph.GraphListener;
import net.ggtools.grand.ui.graph.draw2d.Draw2dGraph;
import net.ggtools.grand.ui.graph.draw2d.Draw2dNode;
import net.ggtools.grand.ui.menu.GraphMenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * A CTabItem specialized in displaying graph. Although it implements
 * {@link net.ggtools.grand.ui.graph.GraphControlerProvider}this class only
 * manage a dummy notification system as there won't be any change of the
 * controler during the instances lifetime.
 * 
 * @author Christophe Labouisse
 */
public class GraphTabItem extends CTabItem implements GraphDisplayer, GraphListener {
    private final class MouseWheelZoomListener implements Listener {
        /**
         * Logger for this class
         */
        private final Log log = LogFactory.getLog(MouseWheelZoomListener.class);

        private MouseWheelZoomListener() {
        }

        public void handleEvent(Event event) {
            final float zoomBefore = getZoom();
            event.doit = false;
            if (event.count > 0) {
                zoomIn();
            }
            else {
                zoomOut();
            }
            final float zoomAfter = getZoom();
            if (zoomAfter != zoomBefore) {
                final Point location = canvas.getViewport().getViewLocation();
                final int newX = (int) (((location.x + event.x) / zoomBefore) * zoomAfter)
                        - event.x;
                final int newY = (int) (((location.y + event.y) / zoomBefore) * zoomAfter)
                        - event.y;
                canvas.scrollTo(newX, newY);
            }
        }
    }

    private final static class OutlineViewerCollator extends Collator {
        /**
         * Logger for this class
         */
        private static final Log log = LogFactory.getLog(OutlineViewerCollator.class);
        private static final int NODE_INDEX_GROUP_NUM = 4;

        private final static int NODE_NAME_GROUP_NUM = 1;

        private final static Pattern pattern = Pattern.compile(
                "\\[?(.*?)(\\s*(\\((\\d+)\\))?\\s*\\])?", Pattern.CASE_INSENSITIVE);

        private final Collator underlying;

        public OutlineViewerCollator() {
            underlying = getInstance();
        }

        public int compare(final String source, final String target) {
            final Matcher sourceMatcher = pattern.matcher(source);
            if (!sourceMatcher.matches()) {
                final String message = "Source name (" + source + ") not matched by pattern";
                log.error(message);
                throw new Error(message);
            }
            assert (sourceMatcher.groupCount() == 4);

            final Matcher targetMatcher = pattern.matcher(target);
            if (!targetMatcher.matches()) {
                final String message = "Source name (" + source + ") not matched by pattern";
                log.error(message);
                throw new Error(message);
            }
            assert (targetMatcher.groupCount() == 4);

            int result = underlying.compare(sourceMatcher.group(NODE_NAME_GROUP_NUM), targetMatcher
                    .group(NODE_NAME_GROUP_NUM));

            if (result == 0) {
                int sourceIndex;
                final String sourceIndexGroup = sourceMatcher.group(NODE_INDEX_GROUP_NUM);
                try {
                    sourceIndex = Integer.parseInt(sourceIndexGroup);
                } catch (NumberFormatException e) {
                    log.debug("Index group" + sourceIndexGroup
                            + " is not an integer treating as 0");
                    sourceIndex = 0;
                }
                
                int targetIndex;
                final String targetIndexGroup = targetMatcher.group(NODE_INDEX_GROUP_NUM);
                try {
                    targetIndex = Integer.parseInt(targetIndexGroup);
                } catch (NumberFormatException e) {
                    log.debug("Index group" + targetIndexGroup
                            + " is not an integer treating as 0");
                    targetIndex = 0;
                }
                
                result = sourceIndex < targetIndex ? -1 : (sourceIndex == targetIndex ? 0 : 1);
            }

            return result;
        }

        public CollationKey getCollationKey(String source) {
            throw new NotImplementedException();
        }

        public int hashCode() {
            return underlying.hashCode();
        }

    }

    private static final Log log = LogFactory.getLog(GraphTabItem.class);

    private final static float ZOOM_MAX = 3.0f;

    private final static float ZOOM_MIN = 0.25f;

    private final static float ZOOM_STEP = 1.1f;

    private final FigureCanvas canvas;

    private final CanvasScroller canvasScroller;

    private final Menu contextMenu;

    private final MenuManager contextMenuManager;

    private final GraphControler controler;

    private Draw2dGraph graph;

    private final SashForm outlineSashForm;

    // TableViewer is richer than ListViewer
    private final TableViewer outlineViewer;

    // To be set to <code>true</code> when the next selection change do no
    // requires jumping to the selected node.
    private boolean skipJumpToNode = false;

    private final SashForm sourceSashForm;

    private final ScrolledComposite textComposite;

    private final StyledText textDisplayer;

    /**
     * Creates a tab to display a new graph. The tab will contain two sash forms
     * a vertical one with the source panel as the bottom part and a second
     * horizontal second one containing the outline window and the graph itself.
     * 
     * @param parent
     *            parent CTabFolder.
     * @param style
     */
    public GraphTabItem(final CTabFolder parent, final int style, final GraphControler controler) {
        super(parent, style);
        this.controler = controler;
        sourceSashForm = new SashForm(parent, SWT.VERTICAL | SWT.BORDER);
        outlineSashForm = new SashForm(sourceSashForm, SWT.HORIZONTAL | SWT.BORDER);
        setControl(sourceSashForm);

        outlineViewer = new TableViewer(outlineSashForm, SWT.READ_ONLY | SWT.H_SCROLL
                | SWT.V_SCROLL);
        outlineViewer.setContentProvider(controler.getNodeContentProvider());
        outlineViewer.setLabelProvider(controler.getNodeLabelProvider());
        outlineViewer.setSorter(new ViewerSorter(new OutlineViewerCollator()));
        outlineViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                final ISelection selection = event.getSelection();

                if (!selection.isEmpty()) {
                    if (selection instanceof IStructuredSelection) {
                        final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                        if (structuredSelection.size() == 1) {
                            final String nodeName = structuredSelection.getFirstElement()
                                    .toString();
                            if (!skipJumpToNode) jumpToNode(nodeName);
                            skipJumpToNode = false;
                            getControler().selectNodeByName(nodeName, false);
                        }
                    }
                }
            }
        });

        canvas = new FigureCanvas(outlineSashForm);
        canvas.getViewport().setContentsTracksHeight(true);
        canvas.getViewport().setContentsTracksWidth(true);
        canvas.setBackground(ColorConstants.white);
        canvas.setScrollBarVisibility(FigureCanvas.AUTOMATIC);
        canvas.addListener(SWT.MouseWheel, new MouseWheelZoomListener());

        canvasScroller = new CanvasScroller(canvas);
        contextMenuManager = new GraphMenu(this);
        contextMenu = contextMenuManager.createContextMenu(canvas);

        outlineSashForm.setWeights(new int[]{1, 5});

        textComposite = new ScrolledComposite(sourceSashForm, SWT.H_SCROLL | SWT.V_SCROLL);
        textDisplayer = new StyledText(textComposite, SWT.MULTI | SWT.READ_ONLY);
        textDisplayer.setFont(Application.getInstance().getFont(Application.MONOSPACE_FONT));
        textComposite.setContent(textDisplayer);
        textComposite.setExpandHorizontal(true);
        textComposite.setExpandVertical(true);
        sourceSashForm.setWeights(new int[]{5, 1});

        controler.addListener(this);
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphControlerProvider#addControlerListener(net.ggtools.grand.ui.graph.GraphControlerListener)
     */
    public void addControlerListener(GraphControlerListener listener) {
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#getContextMenu()
     */
    public Menu getContextMenu() {
        return contextMenu;
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#getControler()
     */
    public GraphControler getControler() {
        return controler;
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#jumpToNode(java.lang.String)
     */
    public void jumpToNode(final String nodeName) {
        if (graph != null) {
            if (log.isDebugEnabled()) {
                log.debug("jumpToNode(nodeName = " + nodeName + ")");
            }

            final Rectangle bounds = graph.getBoundsForNode(nodeName);
            if (bounds != null) {
                final Point center = bounds.getCenter();
                final float graphZoom = graph.getZoom();
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        final org.eclipse.swt.graphics.Point size = canvas.getSize();
                        canvas.scrollSmoothTo((int) (center.x * graphZoom - size.x / 2),
                                (int) (center.y * graphZoom - size.y / 2));
                    }
                });

            }
        }
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphListener#parameterChanged(net.ggtools.grand.ui.graph.GraphControler)
     */
    public void parameterChanged(GraphControler graphControler) {
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphControlerProvider#removeControlerListener(net.ggtools.grand.ui.graph.GraphControlerListener)
     */
    public void removeControlerListener(GraphControlerListener listener) {
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphListener#selectionChanged(java.util.Collection)
     */
    public void selectionChanged(final Collection selectedNodes) {

        final List selection = new ArrayList(selectedNodes.size());
        for (Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
            Draw2dNode node = (Draw2dNode) iter.next();
            selection.add(node.getNode());
        }

        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                skipJumpToNode = true;
                outlineViewer.setSelection(new StructuredSelection(selection), true);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#setGraph(net.ggtools.grand.ui.graph.Graph)
     */
    public void setGraph(final Draw2dGraph graph, final String name, final String toolTip) {
        this.graph = graph;

        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                // Do something for the previous graph.
                canvas.setContents(graph);
                setText(name);
                setToolTipText(toolTip);
                graph.setScroller(canvasScroller);
                outlineViewer.setInput(graph);
            }
        });
    }

    /**
     * @param outlinePanelVisible
     *            The sourcePanelVisible to set.
     */
    public final void setOutlinePanelVisible(boolean outlinePanelVisible) {
        outlineViewer.getControl().setVisible(outlinePanelVisible);
        outlineSashForm.layout();
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#setRichSource(net.ggtools.grand.ant.AntTargetNode.SourceElement[])
     */
    public void setRichSource(SourceElement[] richSource) {
        if (richSource == null) {
            setSourceText("");
            return;
        }

        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < richSource.length; i++) {
            AntTargetNode.SourceElement element = richSource[i];
            buffer.append(element.getText());
        }
        setSourceText(buffer.toString());

        int start = 0;
        for (int i = 0; i < richSource.length; i++) {
            AntTargetNode.SourceElement element = richSource[i];
            Color textColor;
            switch (element.getStyle()) {
            case AntTargetNode.SOURCE_ATTRIBUTE:
                textColor = ColorConstants.darkGreen;
                break;

            case AntTargetNode.SOURCE_MARKUP:
                textColor = ColorConstants.darkBlue;
                break;

            case AntTargetNode.SOURCE_TEXT:
                textColor = ColorConstants.black;
                break;

            default:
                textColor = ColorConstants.lightGray;

                break;
            }
            textDisplayer.setStyleRange(new StyleRange(start, element.getText().length(),
                    textColor, textDisplayer.getBackground()));
            start += element.getText().length();
        }
    }

    /**
     * @param sourcePanelVisible
     *            The sourcePanelVisible to set.
     */
    public final void setSourcePanelVisible(boolean sourcePanelVisible) {
        textComposite.setVisible(sourcePanelVisible);
        sourceSashForm.layout();
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#setSourceText(java.lang.String)
     */
    public void setSourceText(String text) {
        textDisplayer.setText(text);
        textComposite.setMinSize(textDisplayer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#zoomIn()
     */
    public void zoomIn() {
        final float zoom = getZoom();
        if (zoom < ZOOM_MAX) {
            /*
             * final int index = getZoomStep(zoom); setZoom(ZOOM_STEPS[index +
             * 1]);
             */
            setZoom(zoom * ZOOM_STEP);
        }
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#zoomOut()
     */
    public void zoomOut() {
        final float zoom = getZoom();
        if (zoom > ZOOM_MIN) {
            setZoom(zoom / ZOOM_STEP);
        }
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#zoomReset()
     */
    public void zoomReset() {
        setZoom(1.0f);
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#getZoom()
     */
    private final float getZoom() {
        return graph == null ? 1.0f : graph.getZoom();
    }

    /*
     * (non-Javadoc)
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#setZoom(float)
     */
    private final void setZoom(float zoom) {
        if (graph != null) {
            if (log.isTraceEnabled()) {
                log.trace("setZoom(zoom = " + zoom + ")");
            }

            graph.setZoom(zoom);
        }
    }

}
