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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;

/**
 * A wrapper to a {@link org.eclipse.core.runtime.IProgressMonitor}ensuring
 * that the actual methods are called from the display thread.
 * 
 * @author Christophe Labouisse
 */
public class SafeProgressMonitor implements IProgressMonitor {

    private final IProgressMonitor monitor;

    private final Display display;

    /**
     * 
     * @param monitor
     * @param display
     */
    public SafeProgressMonitor(IProgressMonitor monitor, Display display) {
        this.monitor = monitor;
        this.display = display;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String,
     *      int)
     */
    public void beginTask(final String name, final int totalWork) {
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.beginTask(name, totalWork);
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#done()
     */
    public void done() {
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.done();
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
     */
    public void internalWorked(final double work) {
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.internalWorked(work);
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
     */
    public boolean isCanceled() {
        // As this is a getter method it probably don't need to be
        // called from the display thread.
        return monitor.isCanceled();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
     */
    public void setCanceled(final boolean value) {
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.setCanceled(value);
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
     */
    public void setTaskName(final String name) {
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.setTaskName(name);
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
     */
    public void subTask(final String name) {
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.subTask(name);
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
     */
    public void worked(final int work) {
        display.asyncExec(new Runnable() {

            public void run() {
                monitor.worked(work);
            }
        });
    }

}
