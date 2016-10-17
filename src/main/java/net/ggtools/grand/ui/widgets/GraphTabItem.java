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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ggtools.grand.ant.AntTargetNode;
import net.ggtools.grand.ant.AntTargetNode.SourceElement;
import net.ggtools.grand.graph.Node;
import net.ggtools.grand.ui.Application;
import net.ggtools.grand.ui.graph.*;
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
import org.eclipse.jface.viewers.ViewerComparator;
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

/**
 * A CTabItem specialized in displaying graph. Although it implements
 * {@link GraphControllerProvider}this class only
 * manage a dummy notification system as there won't be any change of the
 * controller during the instances lifetime.
 *
 * @author Christophe Labouisse
 */
public class GraphTabItem extends CTabItem
        implements GraphDisplayer, GraphListener {
    /**
     * @author Christophe Labouisse
     */
    private final class MouseWheelZoomListener implements Listener {
        /**
         * Constructor for MouseWheelZoomListener.
         */
        private MouseWheelZoomListener() {
        }

        /**
         * Method handleEvent.
         * @param event Event
         * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
         */
        public void handleEvent(final Event event) {
            final float zoomBefore = getZoom();
            event.doit = false;
            if (event.count > 0) {
                zoomIn();
            } else {
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

    /**
     * @author Christophe Labouisse
     */
    private static final class OutlineViewerCollator extends Collator {
        /**
         * Logger for this class.
         */
        private static final Log OVC_LOG =
                LogFactory.getLog(OutlineViewerCollator.class);
        /**
         * Field NODE_INDEX_GROUP_NUM.
         * (value is 4)
         */
        private static final int NODE_INDEX_GROUP_NUM = 4;

        /**
         * Field NODE_NAME_GROUP_NUM.
         * (value is 1)
         */
        private static final int NODE_NAME_GROUP_NUM = 1;

        /**
         * Field pattern.
         */
        private static final Pattern PATTERN =
                Pattern.compile("\\[?(.*?)(\\s*(\\((\\d+)\\))?\\s*\\])?",
                        Pattern.CASE_INSENSITIVE);

        /**
         * Field underlying.
         */
        private final Collator underlying;

        /**
         * Constructor for OutlineViewerCollator.
         */
        public OutlineViewerCollator() {
            underlying = getInstance();
        }

        /**
         * Method compare.
         * @param source String
         * @param target String
         * @return int
         */
        @Override
        public int compare(final String source, final String target) {
            final Matcher sourceMatcher = PATTERN.matcher(source);
            if (!sourceMatcher.matches()) {
                final String message = "Source name (" + source + ") not matched by pattern";
                OVC_LOG.error(message);
                throw new Error(message);
            }
            assert (sourceMatcher.groupCount() == 4);

            final Matcher targetMatcher = PATTERN.matcher(target);
            if (!targetMatcher.matches()) {
                final String message = "Source name (" + source + ") not matched by pattern";
                OVC_LOG.error(message);
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
                } catch (final NumberFormatException e) {
                    OVC_LOG.debug("Index group" + sourceIndexGroup
                            + " is not an integer treating as 0");
                    sourceIndex = 0;
                }

                int targetIndex;
                final String targetIndexGroup = targetMatcher.group(NODE_INDEX_GROUP_NUM);
                try {
                    targetIndex = Integer.parseInt(targetIndexGroup);
                } catch (final NumberFormatException e) {
                    OVC_LOG.debug("Index group" + targetIndexGroup
                            + " is not an integer treating as 0");
                    targetIndex = 0;
                }

                result = (sourceIndex < targetIndex) ? -1 : ((sourceIndex == targetIndex) ? 0 : 1);
            }

            return result;
        }

        /**
         * Method getCollationKey.
         * @param source String
         * @return CollationKey
         */
        @Override
        public CollationKey getCollationKey(final String source) {
            throw new Error("getCollationKey is not implemented");
        }

        /**
         * Method hashCode.
         * @return int
         */
        @Override
        public int hashCode() {
            return underlying.hashCode();
        }

    }

    /**
     * Field log.
     */
    private static final Log LOG = LogFactory.getLog(GraphTabItem.class);

    /**
     * Field ZOOM_MAX.
     * (value is 3.0)
     */
    private static final float ZOOM_MAX = 3.0f;

    /**
     * Field ZOOM_MIN.
     * (value is 0.25)
     */
    private static final float ZOOM_MIN = 0.25f;

    /**
     * Field ZOOM_STEP.
     * (value is 1.1)
     */
    private static final float ZOOM_STEP = 1.1f;

    /**
     * Field canvas.
     */
    private final FigureCanvas canvas;

    /**
     * Field canvasScroller.
     */
    private final CanvasScroller canvasScroller;

    /**
     * Field contextMenu.
     */
    private final Menu contextMenu;

    /**
     * Field contextMenuManager.
     */
    private final MenuManager contextMenuManager;

    /**
     * Field controller.
     */
    private final GraphController controller;

    /**
     * Field graph.
     */
    private Draw2dGraph graph;

    /**
     * Field outlineSashForm.
     */
    private final SashForm outlineSashForm;

    /**
     * Field outlineViewer.
     *
     * TableViewer is richer than ListViewer
     */
    private final TableViewer outlineViewer;

    /**
     * Field skipJumpToNode.
     *
     * To be set to <code>true</code> when the next selection change do no
     * requires jumping to the selected node.
     */
    private boolean skipJumpToNode = false;

    /**
     * Field sourceSashForm.
     */
    private final SashForm sourceSashForm;

    /**
     * Field textComposite.
     */
    private final ScrolledComposite textComposite;

    /**
     * Field textDisplayer.
     */
    private final StyledText textDisplayer;

    /**
     * Creates a tab to display a new graph. The tab will contain two sash forms
     * a vertical one with the source panel as the bottom part and a second
     * horizontal second one containing the outline window and the graph itself.
     *
     * @param parent
     *            parent CTabFolder.
     * @param style int
     * @param controller GraphController
     */
    public GraphTabItem(final CTabFolder parent, final int style, final GraphController controller) {
        super(parent, style);
        this.controller = controller;
        sourceSashForm = new SashForm(parent, SWT.VERTICAL | SWT.BORDER);
        outlineSashForm = new SashForm(sourceSashForm, SWT.HORIZONTAL | SWT.BORDER);
        setControl(sourceSashForm);

        outlineViewer = new TableViewer(outlineSashForm, SWT.READ_ONLY
                | SWT.H_SCROLL | SWT.V_SCROLL);
        outlineViewer.setContentProvider(controller.getNodeContentProvider());
        outlineViewer.setLabelProvider(controller.getNodeLabelProvider());
        outlineViewer.setComparator(new ViewerComparator(new OutlineViewerCollator()));
        outlineViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                final ISelection selection = event.getSelection();

                if (!selection.isEmpty()) {
                    if (selection instanceof IStructuredSelection) {
                        final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                        if (structuredSelection.size() == 1) {
                            final String nodeName = structuredSelection.getFirstElement().toString();
                            if (!skipJumpToNode) {
                                jumpToNode(nodeName);
                            }
                            skipJumpToNode = false;
                            getController().selectNodeByName(nodeName, false);
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

        controller.addListener(this);
    }

    /**
     * Method addControllerListener.
     * @param listener GraphControllerListener
     * @see GraphControllerProvider#addControllerListener(GraphControllerListener)
     */
    public void addControllerListener(final GraphControllerListener listener) {
    }

    /**
     * Method getContextMenu.
     * @return Menu
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#getContextMenu()
     */
    public final Menu getContextMenu() {
        return contextMenu;
    }

    /**
     * Method getController.
     * @return GraphController
     * @see GraphControllerProvider#getController()
     */
    public final GraphController getController() {
        return controller;
    }

    /**
     * Method jumpToNode.
     * @param nodeName String
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#jumpToNode(java.lang.String)
     */
    public final void jumpToNode(final String nodeName) {
        if (graph != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("jumpToNode(nodeName = " + nodeName + ")");
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

    /**
     * Method parameterChanged.
     * @param controller GraphController
     * @see net.ggtools.grand.ui.graph.GraphListener#parameterChanged(GraphController)
     */
    public void parameterChanged(final GraphController controller) {
    }

    /**
     * Method removeControllerListener.
     * @param listener GraphControllerListener
     * @see GraphControllerProvider#removeControllerListener(GraphControllerListener)
     */
    public void removeControllerListener(final GraphControllerListener listener) {
    }

    /**
     * Method selectionChanged.
     * @param selectedNodes Collection&lt;Draw2dNode&gt;
     * @see net.ggtools.grand.ui.graph.GraphListener#selectionChanged(java.util.Collection)
     */
    public final void selectionChanged(final Collection<Draw2dNode> selectedNodes) {

        final List<Node> selection = new ArrayList<Node>(selectedNodes.size());
        for (final Draw2dNode node : selectedNodes) {
            selection.add(node.getNode());
        }

        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                skipJumpToNode = true;
                outlineViewer.setSelection(new StructuredSelection(selection), true);
            }
        });
    }

    /**
     * Method setGraph.
     * @param graph Draw2dGraph
     * @param name String
     * @param toolTip String
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#setGraph(net.ggtools.grand.ui.graph.draw2d.Draw2dGraph,
     *      java.lang.String, java.lang.String)
     */
    public final void setGraph(final Draw2dGraph graph, final String name,
            final String toolTip) {
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
    public final void setOutlinePanelVisible(final boolean outlinePanelVisible) {
        outlineViewer.getControl().setVisible(outlinePanelVisible);
        outlineSashForm.layout();
    }

    /**
     * Method setRichSource.
     * @param richSource SourceElement[]
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#setRichSource(net.ggtools.grand.ant.AntTargetNode.SourceElement[])
     */
    public final void setRichSource(final SourceElement[] richSource) {
        if (richSource == null) {
            setSourceText("");
            return;
        }

        final StringBuilder buffer = new StringBuilder();
        for (final AntTargetNode.SourceElement element : richSource) {
            buffer.append(element.getText());
        }
        setSourceText(buffer.toString());

        int start = 0;
        for (final AntTargetNode.SourceElement element : richSource) {
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
    public final void setSourcePanelVisible(final boolean sourcePanelVisible) {
        textComposite.setVisible(sourcePanelVisible);
        sourceSashForm.layout();
    }

    /**
     * Method setSourceText.
     * @param text String
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#setSourceText(java.lang.String)
     */
    public final void setSourceText(final String text) {
        textDisplayer.setText(text);
        textComposite.setMinSize(textDisplayer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Method zoomIn.
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#zoomIn()
     */
    public final void zoomIn() {
        final float zoom = getZoom();
        if (zoom < ZOOM_MAX) {
            /*
             * final int index = getZoomStep(zoom); setZoom(ZOOM_STEPS[index +
             * 1]);
             */
            setZoom(zoom * ZOOM_STEP);
        }
    }

    /**
     * Method zoomOut.
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#zoomOut()
     */
    public final void zoomOut() {
        final float zoom = getZoom();
        if (zoom > ZOOM_MIN) {
            setZoom(zoom / ZOOM_STEP);
        }
    }

    /**
     * Method zoomReset.
     * @see net.ggtools.grand.ui.graph.GraphDisplayer#zoomReset()
     */
    public final void zoomReset() {
        setZoom(1.0f);
    }

    /**
     * Method getZoom.
     * @return float
     * @see net.ggtools.grand.ui.graph.draw2d.Draw2dGraph#getZoom()
     */
    private float getZoom() {
        return (graph == null) ? 1.0f : graph.getZoom();
    }

    /**
     * Method setZoom.
     * @param zoom float
     * @see net.ggtools.grand.ui.graph.draw2d.Draw2dGraph#setZoom(float)
     */
    private void setZoom(final float zoom) {
        if (graph != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("setZoom(zoom = " + zoom + ")");
            }

            graph.setZoom(zoom);
        }
    }

}
