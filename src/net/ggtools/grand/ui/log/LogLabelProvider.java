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

/**
 * Table label provider for log events.
 * 
 * @author Christophe Labouisse
 */
public class LogLabelProvider implements ITableLabelProvider,ITableColorProvider {
    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(LogLabelProvider.class);

    public static final String[] COLUMN_NAMES = new String[]{"Level", "Date", "Class", "Message"};
    
    private static Color eventBg = new Color(null,255,255,255);
    private static Color oddBg = new Color(null,240,240,240);

    /**
     * 
     */
    public LogLabelProvider() {
        super();
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        // TODO Auto-generated method stub
        return null;
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
                rc = event.getLevel().name;
                break;

            case 1:
                rc = new Date(event.getTime()).toString();
                break;

            case 2:
                rc = event.getOriginator();
                rc = rc.substring(rc.lastIndexOf('.')+1);
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
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
        if (log.isDebugEnabled()) {
            log.debug("addListener(ILabelProviderListener listener = " + listener + ") - start");
        }
        
        if (log.isDebugEnabled()) {
            log.debug("addListener(ILabelProviderListener) - end");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
     *      java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
        if (log.isDebugEnabled()) {
            log.debug("isLabelProperty(Object element = " + element + ", String property = "
                    + property + ") - start");
        }

        // TODO Auto-generated method stub

        if (log.isDebugEnabled()) {
            log.debug("isLabelProperty(Object, String) - end - return value = " + false);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
     */
    public Color getForeground(Object element, int columnIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
     */
    public Color getBackground(Object element, int columnIndex) {
        return null;
    }
}
