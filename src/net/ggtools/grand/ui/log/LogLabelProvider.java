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
package net.ggtools.grand.ui.log;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Table label provider for log events.
 * 
 * @author Christophe Labouisse
 */
public class LogLabelProvider implements ITableLabelProvider, ITableColorProvider {

    public static final String[] COLUMN_NAMES = new String[]{"Level", "Date", "Class", "Message"};

    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(LogLabelProvider.class);

    private Image errorImage;

    private Image fatalErrorImage;

    private Image infoImage;

    private Image warningImage;

    /**
     * 
     */
    public LogLabelProvider() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
        if (infoImage != null) infoImage.dispose();
        if (warningImage != null) warningImage.dispose();
        if (errorImage != null) errorImage.dispose();
        if (fatalErrorImage != null) fatalErrorImage.dispose();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object,
     *      int)
     */
    public Color getBackground(Object element, int columnIndex) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        Image rc = null;

        if (element instanceof LogEvent) {
            final LogEvent event = (LogEvent) element;
            if (columnIndex == 0) {
                final int level = event.getLevel().value;
                if (level == LogEvent.INFO.value) {
                    if (infoImage == null) {
                        infoImage = new Image(Display.getCurrent(), this.getClass()
                                .getResourceAsStream("resource/info_obj.gif"));
                    }
                    rc = infoImage;
                }
                else if (level == LogEvent.WARNING.value) {
                    if (warningImage == null) {
                        warningImage = new Image(Display.getCurrent(), this.getClass()
                                .getResourceAsStream("resource/warning_obj.gif"));
                    }
                    rc = warningImage;
                }
                else if (level == LogEvent.ERROR.value) {
                    if (errorImage == null) {
                        errorImage = new Image(Display.getCurrent(), this.getClass()
                                .getResourceAsStream("resource/error_obj.gif"));
                    }
                    rc = errorImage;
                }
                else if (level == LogEvent.FATAL.value) {
                    if (fatalErrorImage == null) {
                        fatalErrorImage = new Image(Display.getCurrent(), this.getClass()
                                .getResourceAsStream("resource/fatalerror_obj.gif"));
                    }
                    rc = fatalErrorImage;
                }
            }
        }
        return rc;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    public String getColumnText(Object element, int columnIndex) {
        String rc = null;
        if (element instanceof LogEvent) {
            final LogEvent event = (LogEvent) element;
            switch (columnIndex) {
            case 0:
                // No label for this one.
                //rc = event.getLevel().name;
                break;

            case 1:
                rc = new Date(event.getTime()).toString();
                break;

            case 2:
                rc = event.getOriginator();
                rc = rc.substring(rc.lastIndexOf('.') + 1);
                break;

            case 3:
                rc = event.getMessage().toString();
                break;

            default:

                break;
            }
        }
        return rc;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object,
     *      int)
     */
    public Color getForeground(Object element, int columnIndex) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
     *      java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
    }
}
