package net.ggtools.grand.ui.prefs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

/**
 * @author sergeche (Sergey Chikuyonok)
 */
public class SpinnerFieldEditor extends FieldEditor {
    /**
     * Field spinner.
     */
    private Spinner spinner;

    /**
     * Validation strategy constant (value <code>0</code>) indicating that the
     * editor should perform validation after every key stroke.
     *
     * @see #setValidateStrategy
     */
    public static final int VALIDATE_ON_KEY_STROKE = 0;

    /**
     * Validation strategy constant (value <code>1</code>) indicating that the
     * editor should perform validation only when the text widget loses focus.
     *
     * @see #setValidateStrategy
     */
    public static final int VALIDATE_ON_FOCUS_LOST = 1;

    /**
     * Text limit constant (value <code>-1</code>) indicating unlimited text
     * limit and width.
     */
    public static final int UNLIMITED = -1;

    /**
     * Cached valid state.
     */
    private boolean isValid;

    /**
     * Old text value.
     *
     * @since 3.4 this field is protected.
     */
    protected int oldValue;

    /**
     * Width of text field in characters; initially unlimited.
     */
    private int widthInChars = UNLIMITED;

    /**
     * Text limit of text field in characters; initially unlimited.
     */
    private int textLimit = UNLIMITED;

    /**
     * Field increment.
     */
    private int increment = 1;

    /**
     * Field minValue.
     */
    private int minValue = 0;

    /**
     * Field maxValue.
     */
    private int maxValue = Integer.MAX_VALUE;

    /**
     * The error message, or <code>null</code> if none.
     */
    private String errorMessage;

    /**
     * The validation strategy; <code>VALIDATE_ON_KEY_STROKE</code> by default.
     */
    private int validateStrategy = VALIDATE_ON_KEY_STROKE;

    /**
     * Constructor for SpinnerFieldEditor.
     * @param name String
     * @param labelText String
     * @param parent Composite
     */
    public SpinnerFieldEditor(final String name, final String labelText, final Composite parent) {
        init(name, labelText);
        isValid = false;
        widthInChars = Integer.toString(maxValue).length() + 1;
        errorMessage = JFaceResources.getString("IntegerFieldEditor.errorMessage"); //$NON-NLS-1$
        createControl(parent);
    }

    /**
     * Method adjustForNumColumns.
     * @param numColumns int
     */
    @Override
    protected void adjustForNumColumns(final int numColumns) {
        GridData gd = (GridData) spinner.getLayoutData();
        gd.horizontalSpan = numColumns - 1;
        gd.grabExcessHorizontalSpace = false;
    }

    /**
     * Checks whether the text input field contains a valid value or not.
     *
     * @return <code>true</code> if the field value is valid, and
     *         <code>false</code> if invalid
     */
    protected boolean checkState() {
        if (spinner == null) {
            return false;
        }

        String numberString = spinner.getText();
        try {
            int number = Integer.valueOf(numberString);

            if (number >= minValue && number <= maxValue) {
                clearErrorMessage();
                return true;
            }

            showErrorMessage();
            return false;
        } catch (NumberFormatException e1) {
            showErrorMessage();
        }

        return false;
    }

    /**
     * Hook for subclasses to do specific state checks.
     * <p>
     * The default implementation of this framework method does nothing and
     * returns <code>true</code>. Subclasses should override this method to
     * specific state checks.
     * </p>
     *
     * @return <code>true</code> if the field value is valid, and
     *         <code>false</code> if invalid
     */
    protected boolean doCheckState() {
        return true;
    }

    /**
     * Method doFillIntoGrid.
     * @param parent Composite
     * @param numColumns int
     */
    @Override
    protected void doFillIntoGrid(final Composite parent, final int numColumns) {
        getLabelControl(parent);

        spinner = getSpinnerControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns - 1;
        if (widthInChars != UNLIMITED) {
            GC gc = new GC(spinner);
            try {
                Point extent = gc.textExtent("X"); //$NON-NLS-1$
                gd.widthHint = widthInChars * extent.x;
            } finally {
                gc.dispose();
            }
        } else {
            gd.horizontalAlignment = GridData.FILL;
            gd.grabExcessHorizontalSpace = true;
        }
        spinner.setLayoutData(gd);
    }

    /**
     * Method doLoad.
     */
    @Override
    protected void doLoad() {
        if (spinner != null) {
            int value = getPreferenceStore().getInt(getPreferenceName());
            spinner.setSelection(value);
            oldValue = value;
        }
    }

    /**
     * Method doLoadDefault.
     */
    @Override
    protected void doLoadDefault() {
        if (spinner != null) {
            int value = getPreferenceStore().getDefaultInt(getPreferenceName());
            spinner.setSelection(value);
        }
        valueChanged();
    }

    /**
     * Method doStore.
     */
    @Override
    protected void doStore() {
        getPreferenceStore().setValue(getPreferenceName(), spinner.getSelection());
    }

    /**
     * Returns the error message that will be displayed when and if
     * an error occurs.
     *
     * @return the error message, or <code>null</code> if none
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Method getNumberOfControls.
     * @return int
     */
    @Override
    public int getNumberOfControls() {
        return 2;
    }

    /**
     * Returns the field editor's value.
     *
     * @return the current value
     */
    public int getIntValue() {
        if (spinner != null) {
            return spinner.getSelection();
        }

        return getPreferenceStore().getInt(getPreferenceName());
    }

    /**
     * Returns this field editor's text control.
     *
     * @return the text control,
     *         or <code>null</code> if no text field is created yet
     */
    protected Spinner getSpinnerControl() {
        return spinner;
    }

    /**
     * Returns this field editor's text control.
     * <p>
     * The control is created if it does not yet exist
     * </p>
     *
     * @param parent
     *            the parent
     * @return the text control
     */
    public final Spinner getSpinnerControl(final Composite parent) {
        if (spinner == null) {
            spinner = new Spinner(parent, SWT.SINGLE | SWT.BORDER);
            spinner.setFont(parent.getFont());
            switch (validateStrategy) {
            case VALIDATE_ON_KEY_STROKE:
                spinner.addKeyListener(new KeyAdapter() {
                    /*
                     * (non-Javadoc)
                     * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
                     */
                    public void keyReleased(final KeyEvent e) {
                        valueChanged();
                    }
                });
                spinner.addFocusListener(new FocusAdapter() {
                    // Ensure that the value is checked on focus loss in case we
                    // missed a keyRelease or user hasn't released key.
                    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=214716
                    public void focusLost(final FocusEvent e) {
                        valueChanged();
                    }
                });
                break;
            case VALIDATE_ON_FOCUS_LOST:
                spinner.addKeyListener(new KeyAdapter() {
                    public void keyPressed(final KeyEvent e) {
                        clearErrorMessage();
                    }
                });
                spinner.addFocusListener(new FocusAdapter() {
                    public void focusGained(final FocusEvent e) {
                        refreshValidState();
                    }
                    public void focusLost(final FocusEvent e) {
                        valueChanged();
                        clearErrorMessage();
                    }
                });
                break;
            default:
                Assert.isTrue(false, "Unknown validate strategy"); //$NON-NLS-1$
            }
            spinner.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(final DisposeEvent event) {
                    spinner = null;
                }
            });
            if (textLimit > 0) { // Only set limits above 0 - see SWT spec
                spinner.setTextLimit(textLimit);
            }

            spinner.setIncrement(getIncrement());
            spinner.setMinimum(getMinValue());
            spinner.setMaximum(getMaxValue());
        } else {
            checkParent(spinner, parent);
        }
        return spinner;
    }

    /**
     * Method isValid.
     * @return boolean
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Method refreshValidState.
     */
    protected void refreshValidState() {
        isValid = checkState();
    }

    /**
     * Sets the error message that will be displayed when and if an error
     * occurs.
     *
     * @param message
     *            the error message
     */
    public void setErrorMessage(final String message) {
        errorMessage = message;
    }

    /**
     * Method setFocus.
     */
    public void setFocus() {
        if (spinner != null) {
            spinner.setFocus();
        }
    }

    /**
     * Sets this field editor's value.
     *
     * @param value the new value, or <code>null</code> meaning the empty string
     */
    public void setIntValue(final int value) {
        if (spinner != null) {
            oldValue = spinner.getSelection();
            if (oldValue != value) {
                spinner.setSelection(value);
                valueChanged();
            }
        }
    }

    /**
     * Sets this text field's text limit.
     *
     * @param limit the limit on the number of character in the text
     *  input field, or <code>UNLIMITED</code> for no limit
     */
    public void setTextLimit(final int limit) {
        textLimit = limit;
        if (spinner != null) {
            spinner.setTextLimit(limit);
        }
    }

    /**
     * Sets the strategy for validating the text.
     * <p>
     * Calling this method has no effect after <code>createPartControl</code>
     * is called. Thus this method is really only useful for subclasses to call
     * in their constructor. However, it has public visibility for backward
     * compatibility.
     * </p>
     *
     * @param value either <code>VALIDATE_ON_KEY_STROKE</code> to perform
     *  on the fly checking (the default), or <code>VALIDATE_ON_FOCUS_LOST</code> to
     *  perform validation only after the text has been typed in
     */
    public void setValidateStrategy(final int value) {
        Assert.isTrue(value == VALIDATE_ON_FOCUS_LOST
                      || value == VALIDATE_ON_KEY_STROKE);
        validateStrategy = value;
    }

    /**
     * Shows the error message set via <code>setErrorMessage</code>.
     */
    public void showErrorMessage() {
        showErrorMessage(errorMessage);
    }

    /**
     * Informs this field editor's listener, if it has one, about a change to
     * the value (<code>VALUE</code> property) provided that the old and new
     * values are different.
     * <p>
     * This hook is <em>not</em> called when the text is initialized (or reset
     * to the default value) from the preference store.
     * </p>
     */
    protected void valueChanged() {
        setPresentsDefaultValue(false);
        boolean oldState = isValid;
        refreshValidState();

        if (isValid != oldState) {
            fireStateChanged(IS_VALID, oldState, isValid);
        }

        int newValue = spinner.getSelection();
        if (newValue != oldValue) {
            fireValueChanged(VALUE, oldValue, newValue);
            oldValue = newValue;
        }
    }

    /**
      * Method setEnabled.
      * @param enabled boolean
      * @param parent Composite
      */
    public void setEnabled(final boolean enabled, final Composite parent) {
        super.setEnabled(enabled, parent);
        getSpinnerControl(parent).setEnabled(enabled);
    }

    /**
     * Method setIncrement.
     * @param increment int
     */
    public void setIncrement(final int increment) {
        this.increment = increment;
    }

    /**
     * Method getIncrement.
     * @return int
     */
    public int getIncrement() {
        return increment;
    }

    /**
     * Method getMinValue.
     * @return int
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * Method getMaxValue.
     * @return int
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the range of valid values for this field.
     *
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     */
    public void setValidRange(final int min, final int max) {
        minValue = min;
        maxValue = max;
        spinner.setMinimum(getMinValue());
        spinner.setMaximum(getMaxValue());

        widthInChars = Integer.toString(maxValue).length() + 1;
        GridData gd = (GridData) spinner.getLayoutData();
        GC gc = new GC(spinner);
        try {
            Point extent = gc.textExtent("X"); //$NON-NLS-1$
            gd.widthHint = widthInChars * extent.x;
        } finally {
            gc.dispose();
        }
        spinner.setLayoutData(gd);

        setErrorMessage(JFaceResources.format(
                                              "IntegerFieldEditor.errorMessageRange", //$NON-NLS-1$
                                              new Object[] { new Integer(min), new Integer(max) }));
    }
}
