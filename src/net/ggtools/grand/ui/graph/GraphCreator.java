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
import java.io.IOException;

import net.ggtools.grand.ant.AntProject;
import net.ggtools.grand.exceptions.GrandException;
import net.ggtools.grand.graph.GraphWriter;
import net.ggtools.grand.output.DotWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
            GraphWriter writer;
            try {
                writer = new DotWriter();
                writer.setProducer(project);
                writer.setShowGraphName(true);
                File output = File.createTempFile("grand-ui-tmp", ".dot");
                output.deleteOnExit();
                log.debug("Writing output to " + output);
                writer.write(output);
                dest.worked(1);
                File imageFile = File.createTempFile("grand-ui-tmp", ".png");
                imageFile.deleteOnExit();
                String cmdString = "dot -Tpng -o " + imageFile + " " + output;
                log.debug("About to exec " + cmdString);
                Process proc = Runtime.getRuntime().exec(cmdString);
                proc.waitFor();
                log.debug("dot completed");
                dest.worked(1);
                dest.setGraph(new Graph(imageFile.getAbsolutePath()));
                dest.worked(1);
            } catch (IOException e) {
                log.error("Caught IOException", e);
            } catch (GrandException e) {
                log.error("Caught GrandException", e);
            } catch (InterruptedException e) {
                log.error("Caught InterruptedException", e);
            }
            dest.finished();
        }
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