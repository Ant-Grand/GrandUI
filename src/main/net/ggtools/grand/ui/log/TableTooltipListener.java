package net.ggtools.grand.ui.log;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Class simulating an item dependant tooltip on a {@link Table}widget.
 * Shamelessly created from:
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
 *
 * @author Christophe Labouisse
 */
abstract class TableTooltipListener implements Listener {
    /**
     * Listener in charge of removing the tooltips and sending events to the
     * underlying table.
     *
     * @author Christophe Labouisse
     */
    private  final class ToolTipRemoverListener implements Listener {
        /**
         * Constructor for ToolTipRemoverListener.
         */
        private ToolTipRemoverListener() {
        }

        /**
         * Method handleEvent.
         * @param event Event
         * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
         */
        public void handleEvent(final Event event) {
            final Control control = (Control) event.widget;
            final Shell shell = control.getShell();
            switch (event.type) {
            case SWT.MouseDown:
                final Event e = new Event();
                e.item = (TableItem) control.getData("_TABLEITEM");
                table.setSelection(new TableItem[]{(TableItem) e.item});
                table.notifyListeners(SWT.Selection, e);
            // fall through
            case SWT.MouseExit:
                shell.dispose();
                break;
            }
        }
    }

    /**
     * Field tip.
     */
    private Shell tip = null;

    /**
     * Field table.
     */
    private final Table table;

    /**
     * Field labelListener.
     */
    private Listener labelListener = null;

    /**
     * Constructor for TableTooltipListener.
     * @param table Table
     */
    TableTooltipListener(final Table table) {
        this.table = table;
        labelListener = new ToolTipRemoverListener();
    }

    /**
     * Activates the tooltip mechanism for the table by:
     * <ol>
     * <li>turning off the table SWT tooltip,</li>
     * <li>adding <code>this</code> as listener for several events of
     * {@link #table}</li>
     * </ol>
     *
     */
    public void activateTooltips() {
        table.setToolTipText("");
        table.addListener(SWT.Dispose, this);
        table.addListener(SWT.KeyDown, this);
        table.addListener(SWT.MouseMove, this);
        table.addListener(SWT.MouseHover, this);
    }

    /**
     * Method handleEvent.
     * @param event Event
     * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
     */
    public void handleEvent(final Event event) {
        switch (event.type) {
        case SWT.Dispose:
        case SWT.KeyDown:
        case SWT.MouseMove: {
            if (tip == null) {
                break;
            }
            tip.dispose();
            tip = null;
            break;
        }
        case SWT.MouseHover: {
            final TableItem item = table.getItem(new Point(event.x, event.y));
            if (item != null) {
                if ((tip != null) && !tip.isDisposed()) {
                    tip.dispose();
                }
                tip = new Shell(table.getShell(), SWT.ON_TOP);
                tip.setLayout(new FillLayout());
                createTooltipContents(tip, item);

                final Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                // Rectangle rect = item.getBounds(0);
                final Point pt = table.toDisplay(event.x - size.x / 2, event.y - size.y);
                tip.setBounds(pt.x, pt.y, size.x, size.y);
                tip.setVisible(true);
            }
        }
        }
    }

    /**
     * Creates the tooltip contents for a specific item.
     *
     * Subclasses must override this method but may call <code>super</code>
     * as in the following example:
     *
     * <pre>
     * Composite composite = (Composite) createTooltipContents(parent, item);
     * //add controls to composite as necessary
     * return composite;
     * </pre>
     *
     * The composite returned by this method will have a {@link GridLayout}.
     *
     * @param parent Composite
     * @param item TableItem
     * @return Control
     */
    protected Control createTooltipContents(final Composite parent, final TableItem item) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final Display display = table.getShell().getDisplay();
        composite.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        composite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        composite.setData("_TABLEITEM", item);
        final GridLayout gridLayout = new GridLayout();
        composite.setLayout(gridLayout);
        composite.addListener(SWT.MouseExit, labelListener);
        composite.addListener(SWT.MouseDown, labelListener);
        return composite;
    }
}
