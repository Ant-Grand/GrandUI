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

    /**
     * Field monitor.
     */
    private final IProgressMonitor monitor;

    /**
     * Field display.
     */
    private final Display display;

    /**
     *
     * @param monitor IProgressMonitor
     * @param display Display
     */
    public SafeProgressMonitor(final IProgressMonitor monitor,
            final Display display) {
        this.monitor = monitor;
        this.display = display;
    }

    /**
     * Method beginTask.
     * @param name String
     * @param totalWork int
     * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
     */
    public final void beginTask(final String name, final int totalWork) {
        display.asyncExec(() -> monitor.beginTask(name, totalWork));
    }

    /**
     * Method done.
     * @see org.eclipse.core.runtime.IProgressMonitor#done()
     */
    public final void done() {
        display.asyncExec(monitor::done);
    }

    /**
     * Method internalWorked.
     * @param work double
     * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
     */
    public final void internalWorked(final double work) {
        display.asyncExec(() -> monitor.internalWorked(work));
    }

    /**
     * Method isCanceled.
     * @return boolean
     * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
     */
    public final boolean isCanceled() {
        // As this is a getter method it probably don't need to be
        // called from the display thread.
        return monitor.isCanceled();
    }

    /**
     * Method setCanceled.
     * @param value boolean
     * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
     */
    public final void setCanceled(final boolean value) {
        display.asyncExec(() -> monitor.setCanceled(value));
    }

    /**
     * Method setTaskName.
     * @param name String
     * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
     */
    public final void setTaskName(final String name) {
        display.asyncExec(() -> monitor.setTaskName(name));
    }

    /**
     * Method subTask.
     * @param name String
     * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
     */
    public final void subTask(final String name) {
        display.asyncExec(() -> monitor.subTask(name));
    }

    /**
     * Method worked.
     * @param work int
     * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
     */
    public final void worked(final int work) {
        display.asyncExec(() -> monitor.worked(work));
    }

}
