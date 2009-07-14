package net.refractions.udig.style.sld.editor;

import java.util.Map;

import net.refractions.udig.style.sld.SLDPlugin;
import net.refractions.udig.style.sld.internal.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.part.PageBook;

public class PointEditorPage extends StyleEditorPage {
    private static int standardPadding = 2;

    public PointEditorPage() {
    }
    
 
    

    @Override
    public void createPageContent( Composite parent ) {
    	parent.setLayout(new GridLayout(2, false));
    	createListContent(parent);
    	createGraphicBook(parent);
    	createGraphicContent(parent);
    }
    
    /*
     * These belong to the list composite responsible for managing External
     * Graphics and Marks.  Multiple options are provided, with the first that
     * the system can render being used.
     */
    Composite listComposite;
    List representationList;
    Button listAddButton;
    Button listUpButton;
    Button listDownButton;
    Button listRemoveButton;
    
    private void createListContent(Composite parent) {
        listComposite = new Composite(parent, SWT.NONE);
        listComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        listComposite.setLayout(new GridLayout(2, false));
        
        representationList = new List(listComposite, SWT.SINGLE | SWT.DEFAULT);
        representationList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        representationList.setItems(getGraphicRepresentations());
        representationList.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected( SelectionEvent e ) {
                widgetSelected(e);
            }

            public void widgetSelected( SelectionEvent e ) {
                listRemoveButton.setEnabled(true);
                if(representationList.getSelectionIndex() == 0)
                    listUpButton.setEnabled(false);
                else
                    listUpButton.setEnabled(true);
                if(representationList.getSelectionIndex() == representationList.getItemCount() -1)
                    listDownButton.setEnabled(false);
                else
                    listDownButton.setEnabled(true);
            }
            
        });
        
        Composite buttonComposite = new Composite(listComposite, SWT.NONE);
        buttonComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        buttonComposite.setLayout(new FormLayout());
        
        listAddButton = new Button(buttonComposite, SWT.PUSH);
        listAddButton.setText("Add");
        FormData data = new FormData();
        data.left = new FormAttachment(0, standardPadding);
        data.top = new FormAttachment(0, standardPadding);
        listAddButton.setLayoutData(data);
        
        listUpButton = new Button(buttonComposite, SWT.PUSH);
        listUpButton.setText("Move Up");
        data = new FormData();
        data.left = new FormAttachment(listAddButton, 0, SWT.LEFT);
        data.top = new FormAttachment(listAddButton, standardPadding * 3);
        listUpButton.setLayoutData(data);
        listUpButton.setEnabled(false);
        
        listDownButton = new Button(buttonComposite, SWT.PUSH);
        listDownButton.setText("Move Down");
        data = new FormData();
        data.left = new FormAttachment(listAddButton, 0, SWT.LEFT);
        data.top = new FormAttachment(listUpButton, standardPadding);
        listDownButton.setLayoutData(data);
        listDownButton.setEnabled(false);
        
        listRemoveButton = new Button(buttonComposite, SWT.PUSH);
        listRemoveButton.setText("Remove");
        data = new FormData();
        data.top = new FormAttachment(listDownButton, standardPadding * 3);
        data.left = new FormAttachment(listAddButton, 0, SWT.LEFT);
        data.bottom = new FormAttachment(100, -1 * standardPadding);
        listRemoveButton.setLayoutData(data);
        listRemoveButton.setEnabled(false);
        
    }
    
 
    
    /*
     * These widgets belong to the graphic composite and provide attributes 
     * common to all graphic options listed in the list composite above.
     */
    Composite graphicComposite;
    Combo opacityCombo;
    Combo sizeCombo;
    Combo rotationCombo;
    
    private void createGraphicContent(Composite parent) {
        graphicComposite = new Composite(parent, SWT.NONE);
        graphicComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        graphicComposite.setLayout(new GridLayout(2, false));
        
        Label label = new Label(graphicComposite, SWT.NONE);
        label.setText(Messages.StylingConstants_label_opacity);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
        label.setToolTipText(Messages.StylingConstants_tooltip_opacity);
        opacityCombo = generateFancyCombo(graphicComposite, getOpacityList(), true, -1, SWT.DROP_DOWN);
        opacityCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        opacityCombo.setToolTipText(Messages.StylingConstants_label_opacity);
        
        label = new Label(graphicComposite, SWT.NONE);
        label.setText(Messages.StylingConstants_label_size);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
        label.setToolTipText(Messages.StylingConstants_tooltip_size);
        sizeCombo = generateFancyCombo(graphicComposite, getSizeList(), false, -1, SWT.DROP_DOWN);
        sizeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        sizeCombo.setToolTipText(Messages.StylingConstants_tooltip_size);
        
        label = new Label(graphicComposite, SWT.NONE);
        label.setText(Messages.StylingConstants_label_rotation);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
        label.setToolTipText(Messages.StylingConstants_tooltip_rotation);
        rotationCombo = generateFancyCombo(graphicComposite, getRotationList(), true, 7, SWT.DROP_DOWN);
        
        
        
    }
    
    /*
     * These widgets belong to the various components of the graphic book.
     * They provide the appropriate details when a graphic is selected from the
     * list composite.
     */
    PageBook graphicBook;
    
    Composite externalGraphicPage;
    Composite markPage;
    
    private void createGraphicBook(Composite parent) {
        graphicBook = new PageBook(parent, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.verticalSpan = 2;
        graphicBook.setLayoutData(gd);
    }
    @Override
    public String getErrorMessage() {
        // check over content and return
        // non null if in error
        return null;
    }

    @Override
    public String getLabel() {
        return null; // not sure what this is for we need to update the javadocs
    }

    @Override
    public void gotFocus() {
        //refresh();
        //dirty = false;
    }

    @Override
    public boolean performCancel() {
        return true;
    }

    @Override
    public void styleChanged( Object source ) {
        
    }

    public boolean okToLeave() {
        return true;
    }

    public boolean performApply() {
        return true;
    }

    public boolean performOk() {
        return true;
    }

    public void refresh() {
    }

    /*
     * TODO: implement stuff
     */
    private String[] getGraphicRepresentations() {
        return new String[] {"External Graphic (PNG)",
                "External Graphic (BMP)",
                "Well Known Mark (Star)"};
    }
    
    /*
     * TODO: implement stuff
     */
    private String[] getOpacityList() {
        return new String[] {"Clear", "25%", "50%", "75%", "Opaque"};
    }
    
    private String[] getSizeList() {
        return new String[] {"1", "2", "3", "5", "10", "12", "15", "20"};
    }
    
    private String[] getRotationList() {
        return new String[] {"-150°", "-135°", "-120°", "-90°", "-60°", "-45°", "-30°", "0°", 
                "30°", "45°", "60°", "90°", "120°", "135°", "150°", "180°"};
    }
    
    /**
     * Creates a fancy-ass combo box that may or may not have an included default indicator, and 
     * has an unselected message initially, if no selected value or default are available.
     *
     * @param parent Parent Composite
     * @param items array of display names
     * @param hasDefault indicates if a default option should be inserted
     * @param selection The index of the item to select by default.  If selection is outside the 
     *                  array limits, the default or unselected message will be selected.
     * @param style Style bits for the combo.  Can include SWT.READONLY and/or one of 
     *              SWT.DROPDOWN or SWT.SIMPLE.
     * @return
     */
    private Combo generateFancyCombo(Composite parent, String[] items, boolean hasDefault, int selection, int style) {
        final Combo fancyCombo = new Combo(parent, style);
        fancyCombo.setItems(items);
        if(selection >= 0 && selection < items.length)
            fancyCombo.select(selection);
        if(hasDefault) {
            fancyCombo.add(Messages.PointEditorPage_fancyCombo_default, 0);
            if(selection < 0 || selection >= items.length)
                fancyCombo.select(0);
        } else if(selection < 0 || selection >= items.length) {
            fancyCombo.add(Messages.PointEditorPage_fancyCombo_unselected, 0);
            fancyCombo.select(0);
            final SelectionListener listener = new SelectionListener() {
                public void widgetDefaultSelected( SelectionEvent e ) {
                    widgetSelected(e);
                }

                public void widgetSelected( SelectionEvent e ) {
                    fancyCombo.remove(0);
                    fancyCombo.removeSelectionListener(this);
                    SLDPlugin.log("Removing selection listener", null);
                }
            };
            fancyCombo.addSelectionListener(listener);
        }
        
        return fancyCombo;
    }
}
