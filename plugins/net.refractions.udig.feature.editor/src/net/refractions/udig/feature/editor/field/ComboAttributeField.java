package net.refractions.udig.feature.editor.field;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.geotools.util.Converters;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

/*NOT FINISHED*/

public class ComboAttributeField extends AttributeField {

    /**
     * The <code>Combo</code> widget. Works only with strings at the moment which might be problematic if we need it to work with objects
     */
    private Combo fCombo;
    
    /**
     * The value (not the name) of the currently selected item in the Combo widget.
     */
    private String fValue;
    
    /**
     * The names (labels) and underlying values to populate the combo widget.  These should be
     * arranged as: { {name1, value1}, {name2, value2}, ...}
     */
    private String[][] fEntryNamesAndValues;

    public ComboAttributeField(String name, String labelText, List<Object> values, Composite parent) {
        this( name, labelText, toNamesAndValues( values ), parent );
    }
    
    private static String[][] toNamesAndValues( List<Object> values ) {
        String namesAndValues[][] = new String[values.size()][2];
        for( int i=0; i<values.size();i++){
            namesAndValues[i][0] = values.get(i).toString();
            namesAndValues[i][1] = values.get(i).toString();           
        }
        return namesAndValues;
    }

    /**
     * Create the combo box attribute field.
     * 
     * @param name the name of the preference this attribute field works on
     * @param labelText the label text of the attribute field
     * @param entryNamesAndValues the names (labels) and underlying values to populate the combo widget.  These should be
     * arranged as: { {name1, value1}, {name2, value2}, ...}
     * @param parent the parent composite
     */
    public ComboAttributeField(String name, String labelText, String[][] entryNamesAndValues, Composite parent) {
        init(name, labelText);
        Assert.isTrue(checkArray(entryNamesAndValues));
        fEntryNamesAndValues = entryNamesAndValues;
        createControl(parent);      
    }

    /**
     * Checks whether given <code>String[][]</code> is of "type" 
     * <code>String[][2]</code>.
     *
     * @return <code>true</code> if it is ok, and <code>false</code> otherwise
     */
    private boolean checkArray(String[][] table) {
        if (table == null) {
            return false;
        }
        for (int i = 0; i < table.length; i++) {
            String[] array = table[i];
            if (array == null || array.length != 2) {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.AttributeField#adjustForNumColumns(int)
     */
    protected void adjustForNumColumns(int numColumns) {
        if (numColumns > 1) {
            Control control = getLabelControl();
            int left = numColumns;
            if (control != null) {
                ((GridData)control.getLayoutData()).horizontalSpan = 1;
                left = left - 1;
            }
            ((GridData)fCombo.getLayoutData()).horizontalSpan = left;
        } else {
            Control control = getLabelControl();
            if (control != null) {
                ((GridData)control.getLayoutData()).horizontalSpan = 1;
            }
            ((GridData)fCombo.getLayoutData()).horizontalSpan = 1;          
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.AttributeField#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
     */
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        int comboC = 1;
        if (numColumns > 1) {
            comboC = numColumns - 1;
        }
        Control control = getLabelControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = 1;
        control.setLayoutData(gd);
        control = getComboBoxControl(parent);
        gd = new GridData();
        gd.horizontalSpan = comboC;
        gd.horizontalAlignment = GridData.FILL;
        control.setLayoutData(gd);
        control.setFont(parent.getFont());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.AttributeField#doLoad()
     */
    protected void doLoad() {
        
        Object value = getFeature().getAttribute( getAttributeName() );            
        String text = Converters.convert(value, String.class );
        updateComboForValue(text);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.AttributeField#doLoadDefault()
     */
    protected void doLoadDefault() {
        SimpleFeatureType schema = getFeature().getFeatureType();
        AttributeDescriptor descriptor = schema.getDescriptor( getAttributeName());            
        Object value = descriptor.getDefaultValue();
        String text = Converters.convert(value, String.class );
        updateComboForValue(text);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.AttributeField#doStore()
     */
    protected void doStore() {
        
    //TODO Implement this method. Not sure how this one should work

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.AttributeField#getNumberOfControls()
     */
    public int getNumberOfControls() {
        return 2;
    }

    /*
     * Lazily create and return the Combo control.
     */
    private Combo getComboBoxControl(Composite parent) {
        if (fCombo == null) {
            fCombo = new Combo(parent, SWT.READ_ONLY);
            fCombo.setFont(parent.getFont());
            for (int i = 0; i < fEntryNamesAndValues.length; i++) {
                fCombo.add(fEntryNamesAndValues[i][0], i);
            }
            
            fCombo.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent evt) {
                    String oldValue = fValue;
                    String name = fCombo.getText();
                    fValue = getValueForName(name);
                    setPresentsDefaultValue(false);
                    fireValueChanged(VALUE, oldValue, fValue);                  
                }
            });
        }
        return fCombo;
    }
    
    /*
     * Given the name (label) of an entry, return the corresponding value.
     */
    private String getValueForName(String name) {
        for (int i = 0; i < fEntryNamesAndValues.length; i++) {
            String[] entry = fEntryNamesAndValues[i];
            if (name.equals(entry[0])) {
                return entry[1];
            }
        }
        return fEntryNamesAndValues[0][0];
    }
    
    /*
     * Set the name in the combo widget to match the specified value.
     */
    private void updateComboForValue(String value) {
        fValue = value;
        for (int i = 0; i < fEntryNamesAndValues.length; i++) {
            if (value.equals(fEntryNamesAndValues[i][1])) {
                fCombo.setText(fEntryNamesAndValues[i][0]);
                return;
            }
        }
        if (fEntryNamesAndValues.length > 0) {
            fValue = fEntryNamesAndValues[0][1];
            fCombo.setText(fEntryNamesAndValues[0][0]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.AttributeField#setEnabled(boolean,
     *      org.eclipse.swt.widgets.Composite)
     */
    public void setEnabled(boolean enabled, Composite parent) {
        super.setEnabled(enabled, parent);
        getComboBoxControl(parent).setEnabled(enabled);
    }
}