// $Id$
/* ====================================================================
 * Copyright (c) 2002-2003, Christophe Labouisse
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.ggtools.grand.ui.graph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.ggtools.grand.ant.AntProject;
import net.ggtools.grand.exceptions.GrandException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;

/**
 * 
 * 
 * @author Christophe Labouisse
 */
public class GraphCreator {
    private static Log log = LogFactory.getLog(GraphCreator.class);

    private static class Worker implements Runnable {
        private static Log log = LogFactory.getLog(Worker.class);

        private String fileName;

        private GraphDisplayer dest;

        public Worker(String fileName, GraphDisplayer dest) {
            this.fileName = fileName;
            this.dest = dest;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            dest.beginUpdate(4);
            AntProject project = new AntProject(new File(fileName));
            dest.worked(1);
            /*GraphFilter filter = new IsolatedNodeFilter();
            filter.setProducer(project);*/
            try {
                log.debug("Creating graph");
                Draw2dGrapher grapher = new Draw2dGrapher();
                grapher.setProducer(project);
                IFigure contents = grapher.drawGraph();
                dest.worked(1);
                dest.setGraph(contents);
            } catch (GrandException e) {
                log.error("Caught GrandException", e);
            }
            dest.finished();
        }
    }
    
    /**
     * Builds a figure for the given edge and adds it to contents
     * @param contents the parent figure to add the edge to
     * @param edge the edge
     */
    static void buildEdgeFigure(Figure contents, Edge edge) {
        PolylineConnection conn = connection(edge);
        conn.setForegroundColor(ColorConstants.gray);
        PolygonDecoration dec = new PolygonDecoration();
        conn.setTargetDecoration(dec);
        Node s = edge.source;
        Node t = edge.target;
            
        conn.setSourceAnchor(new XYAnchor(edge.start));
        conn.setTargetAnchor(new XYAnchor(edge.end));
        conn.setConnectionRouter(null);
        NodeList nodes = edge.vNodes;
        if (nodes != null) {
                List bends = new ArrayList();
                conn.setConnectionRouter(new BendpointConnectionRouter());
                for (int i = 0; i < nodes.size(); i++) {
                    Node vn = nodes.getNode(i);
                    int x = vn.x;
                    int y = vn.y;
                    if (edge.isFeedback) {
                        bends.add(new AbsoluteBendpoint(x, y + vn.height));
                        bends.add(new AbsoluteBendpoint(x, y));

                    } else {
                        bends.add(new AbsoluteBendpoint(x, y));
                        bends.add(new AbsoluteBendpoint(x, y + vn.height));
                    }
                }
            conn.setRoutingConstraint(bends);
        }   
        contents.add(conn);
    }

    /**
     * Builds a Figure for the given node and adds it to contents
     * @param contents the parent Figure to add the node to
     * @param node the node to add
     */
    static void buildNodeFigure(Figure contents, Node node) {
        log.debug("Creating label for node "+node.data.toString()+" ("+node.x+","+node.y+")");
        Label label;
        label = new Label();
        label.setBackgroundColor(ColorConstants.lightGray);
        label.setOpaque(true);
        label.setBorder(new LineBorder());
        if (node.incoming.isEmpty())
            label.setBorder(new LineBorder(2));
        String text = node.data.toString();// + "(" + node.index +","+node.sortValue+ ")";
        label.setText(text);
        node.data = label;
        contents.add(label, new Rectangle(node.x, node.y, node.width, node.height));
    }

    /**
     * Builds a connection for the given edge
     * @param e the edge
     * @return the connection
     */
    static PolylineConnection connection(Edge e) {
        PolylineConnection conn = new PolylineConnection();
        conn.setConnectionRouter(new BendpointConnectionRouter());
        List bends = new ArrayList();
        NodeList nodes = e.vNodes;
        if (nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                Node n = nodes.getNode(i);
                int x = n.x;
                int y = n.y;
                bends.add(new AbsoluteBendpoint(x, y));
                bends.add(new AbsoluteBendpoint(x, y + n.height));
            }
        }
        conn.setRoutingConstraint(bends);
        return conn;
    }



    private final static ThreadGroup threadGroup = new ThreadGroup("Graph creation");

    public GraphCreator() {
    }

    public void asyncCreateGraph(String scriptName, GraphDisplayer dest) {
        Thread thread = new Thread(threadGroup, new Worker(scriptName, dest),
                "Graph creation for " + scriptName);
        thread.start();
    }
}