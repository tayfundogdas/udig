package net.refractions.udig.tutorials.featureeditor;

import net.refractions.udig.project.command.CompositeCommand;
import net.refractions.udig.project.ui.tool.IToolContext;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;

public class CountryPanel implements KeyListener, ISelectionChangedListener {
    /** Attribute name for attribute GMI_CNTRY */
    public final static String GMI_CNTRY = "GMI_CNTRY";

    /** Attribute name for attribute REGION */
    public final static String COLOR_MAP = "COLOR_MAP";

    /** Attribute name for attribute NAME */
    public final static String NAME = "CNTRY_NAME";

    public final static Object[] COLOR_MAP_OPTS =
        new Object[] { 1, 2, 3, 4, 5, 6, 7, 8 };

    Text gmiCntry;
    Text name;
    ComboViewer colorMap;
    private Button apply;
    private Button reset;
    private SimpleFeature editedFeature;
    private SimpleFeature oldFeature;

    /** Used send commands to the edit blackboard */
    private IToolContext context;

    public void createControl(Composite parent) {
        parent.setLayout(new GridLayout(2, false));
// SWT Widgets
        Label label = new Label(parent, SWT.SHADOW_IN);
        label.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, false));
        label.setText("Country:");

        name = new Text(parent, SWT.SHADOW_IN | SWT.BORDER);
        name.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        name.addKeyListener(this);

        label = new Label(parent, SWT.SHADOW_IN);
        label.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, false));
        label.setText("Code:");

        gmiCntry = new Text(parent, SWT.SHADOW_IN | SWT.BORDER);
        gmiCntry.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        gmiCntry.addKeyListener(this);

// JFace Viewer        
        label = new Label(parent, SWT.SHADOW_IN);
        label.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, false));
        label.setText("Color Map:");

        colorMap = new ComboViewer(parent, SWT.SHADOW_IN);
        colorMap.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.NONE, true, false));
        colorMap.addSelectionChangedListener(this);

// hook up to data
colorMap.setContentProvider(new IStructuredContentProvider() {
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Object[]) {
            return (Object[]) inputElement;
        }
        return null;
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // for dynamic content we would register listeners here
    }
    public void dispose() {
    }
});
colorMap.setLabelProvider( new LabelProvider(){
    public String getText(Object element) {
        return " "+element+" color";
    }
});
colorMap.setInput(COLOR_MAP_OPTS);
// Buttons
Composite buttonBar = new Composite(parent, SWT.NONE);
GridData gridData = new GridData(SWT.RIGHT,SWT.TOP,false, false);
gridData.horizontalSpan=2;
buttonBar.setLayoutData(gridData);
buttonBar.setLayout(new FillLayout( SWT.HORIZONTAL ));

apply = new Button(buttonBar, SWT.PUSH);
apply.setText("Apply");
apply.addSelectionListener(new SelectionAdapter() {
    public void widgetSelected(SelectionEvent e) {
        applyChanges();
    }    
});
apply.setEnabled(false);

reset = new Button(buttonBar, SWT.PUSH);
reset.setText("Reset");
reset.setEnabled(false);
reset.addSelectionListener(new SelectionAdapter() {
    public void widgetSelected(SelectionEvent e) {
        resetChanges();
    }
});
    }

public void keyPressed(KeyEvent e) {
    // do nothing
}

public void keyReleased(KeyEvent e) {
    setEnabled(true);
}

private void setEnabled(boolean enabled) {
    if (oldFeature == null && enabled) {
        return;
    }
    apply.setEnabled(enabled);
    reset.setEnabled(enabled);
}

/** Listen to the viewer */
public void selectionChanged(SelectionChangedEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event
            .getSelection();

    Integer value = (Integer) selection.getFirstElement();
    setEnabled(true);
}

private void applyChanges() {
    try {
        editedFeature.setAttribute(NAME, name.getText());
        editedFeature.setAttribute(GMI_CNTRY, gmiCntry.getText());

        IStructuredSelection selection =
            (IStructuredSelection) colorMap.getSelection();
        Integer color = (Integer) selection.getFirstElement();
        editedFeature.setAttribute(COLOR_MAP, color.toString());

    } catch (IllegalAttributeException e1) {
        // shouldn't happen.
    }
    CompositeCommand compComm = new CompositeCommand();
    compComm.getCommands().add(
        context.getEditFactory().createSetEditFeatureCommand(editedFeature));
    compComm.getCommands().add(
        context.getEditFactory().createWriteEditFeatureCommand());
    context.sendASyncCommand(compComm);
    setEnabled(false);
}

    private void resetChanges() {
        setEditFeature(oldFeature, context);
        setEnabled(false);
    }

public void setEditFeature(SimpleFeature newFeature, IToolContext newcontext) {
    this.context = newcontext;
    oldFeature = newFeature;
    if (oldFeature != null) {
        try {
        	editedFeature = SimpleFeatureBuilder.copy( newFeature );
        } catch (IllegalAttributeException e) {
            // shouldn't happen
        }
    } else {
        editedFeature = null;
    }
    if (oldFeature == null) {
        gmiCntry.setText("");
        colorMap.setSelection(new StructuredSelection());
        name.setText("");
    } else {
        String nameText = (String) oldFeature.getAttribute(NAME);
        if (nameText == null)
            nameText = "";
        name.setText(nameText);

        String gmiText = (String) oldFeature.getAttribute(GMI_CNTRY);
        if (gmiText == null)
            gmiText = "";
        gmiCntry.setText(gmiText);

        String colorText = (String) oldFeature.getAttribute(COLOR_MAP);
        if (colorText != null) {
            StructuredSelection selection =
                new StructuredSelection(new Integer(colorText));
            colorMap.setSelection(selection);
        } else {
            colorMap.setSelection(new StructuredSelection());
        }
    }
    setEnabled(false);
}

    public void setFocus() {
        name.setFocus();
    }
}