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
import java.util.HashMap;
import java.util.Map;

import net.ggtools.grand.ui.log.LogEvent.Level;

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
class LogLabelProvider implements ITableLabelProvider, ITableColorProvider {

    /**
     * Logger for this class.
     */
    private static final Log log = LogFactory.getLog(LogLabelProvider.class);

    /**
     * Field logLevelIcons.
     */
    private final Map<Level, Image> logLevelIcons = new HashMap<Level, Image>();

    /**
     *
     */
    public LogLabelProvider() {
        super();
    }

    /**
     * Method addListener.
     * @param listener ILabelProviderListener
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(final ILabelProviderListener listener) {
    }

    /**
     * Method dispose.
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
        for (final Map.Entry<Level, Image> entry : logLevelIcons.entrySet()) {
            final Image image = entry.getValue();
            if (image != null) {
                image.dispose();
            }
        }
    }

    /**
     * Method getBackground.
     * @param element Object
     * @param columnIndex int
     * @return Color
     * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
     */
    public Color getBackground(final Object element, final int columnIndex) {
        return null;
    }

    /**
     * Method getColumnImage.
     * @param element Object
     * @param columnIndex int
     * @return Image
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(final Object element, final int columnIndex) {
        Image rc = null;

        if (element instanceof LogEvent) {
            final LogEvent event = (LogEvent) element;
            if (columnIndex == 0) {
                final LogEvent.Level eventLevel = event.getLevel();

                // As logLevelIcons may contains a null value, with check
                // with containsKeys rather than get() == null.
                if (logLevelIcons.containsKey(eventLevel)) {
                    rc = (Image) logLevelIcons.get(eventLevel);
                }
                else {
                    final String resourceName = "resource/level_"
                            + eventLevel.name.toLowerCase() + ".gif";
                    rc = new Image(Display.getCurrent(),
                            this.getClass().getResourceAsStream(resourceName));
                }
            }
        }
        return rc;
    }

    /**
     * Method getColumnText.
     * @param element Object
     * @param columnIndex int
     * @return String
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(final Object element, final int columnIndex) {
        String rc = null;
        if (element instanceof LogEvent) {
            final LogEvent event = (LogEvent) element;
            switch (columnIndex) {
            case LogViewer.CI_LEVEL:
                // No label for this one.
                // rc = event.getLevel().name;
                break;

            case LogViewer.CI_DATE:
                rc = new Date(event.getTime()).toString();
                break;

            case LogViewer.CI_CLASS:
                rc = event.getOriginator();
                rc = rc.substring(rc.lastIndexOf('.') + 1);
                break;

            case LogViewer.CI_MESSAGE:
                rc = event.getMessage().toString();
                break;

            default:
                if (log.isWarnEnabled()) {
                    log.warn("getColumnText(columnIndex = " + columnIndex
                            + ") - Got unexpected column index ");
                }
                break;
            }
        }
        return rc;
    }

    /**
     * Method getForeground.
     * @param element Object
     * @param columnIndex int
     * @return Color
     * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
     */
    public Color getForeground(final Object element, final int columnIndex) {
        return null;
    }

    /**
     * Method isLabelProperty.
     * @param element Object
     * @param property String
     * @return boolean
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    public boolean isLabelProperty(final Object element, final String property) {
        return false;
    }

    /**
     * Method removeListener.
     * @param listener ILabelProviderListener
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(final ILabelProviderListener listener) {
    }
}
