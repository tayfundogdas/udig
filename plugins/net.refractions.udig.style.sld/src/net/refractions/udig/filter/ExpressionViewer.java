package net.refractions.udig.filter;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.filter.expression.Expression;

/**
 * A JFace Style Expression Viewer that can be used to show an Expression
 * to a user (using whatever SWT widgets are appropriate) and allow modification.
 * <p>
 * Initially we will just use a Text control; gradually working up to PropertyName, Integer and
 * Color Expressions. In each case the Expression may be retrieved by simple get/set methods
 * and we will provide some kind of consistent change notification.
 * <p>
 * Choosing which widgets to use will be based on constants; much like MapViewer switches
 * implementations. If there is something specific we need to handle (like say restrictions
 * based on FeatureType we may need to break out different ExpressionViewers kind of like
 * how Tree and TreeTable viewers work.
 * </p>
 * <p>
 * Remember that although Viewers are a wrapper around some SWT Control or Composite you still
 * have direct access using the getControl() method so that you can do your layout data thing.
 * </p>
 * <p>
 * Future directions from Mark:
 * <ul>
 * <li>
 * @author jive
 * @since 1.1.0
 */
public class ExpressionViewer extends Viewer {
    /**
     * This is the expression we are working on here.
     * <p>
     * We are never going to be "null"; Expression.NIL is used to indicate
     * an intentionally empty expression.
     */
    protected Expression expr = Expression.NIL;
    
    /**
     * This is our internal widget we are sharing with the outside world;
     * in many cases it will be a Composite.
     */
    private Control control;

    private KeyListener keyListener = new KeyListener(){        
        public void keyReleased( KeyEvent e ) {
            // we can try and parse this puppy; and issue a selection changed
            // event when we actually have an expression that works
            if( e.widget instanceof Text ){
                Text text = (Text) e.widget;
                String cql = text.getText();
                try {
                    expr = CQL.toExpression( cql );
                } catch (CQLException e1) {
                    expr = Expression.NIL; // no valid expression right now
                    // set warning on associated feedback label
                }
            }
        }
        public void keyPressed( KeyEvent e ) {
        }
    };
    
    public ExpressionViewer( Composite parent ){
        this( parent, SWT.SINGLE );
    }
    /**
     * Creates an ExpressionViewer using the provided style.
     * <ul>
     * <li>SWT.SINGLE - A simple text field showing the expression using extended CQL notation;
     *     may be shown as a combo box if either a FeatureType or DialogSettings are provided
     * <li>SWT.MULTI - A multi line text field; may be shown as an ExpressionBuilder later on
     * <li>SWT.READ_ONLY - read only 
     * <li><li>SWT.WRAP - useful with SWT.MULTI
     * <li>SWT.LEFT - alignment
     * <li>SWT.RIGHT - alignment
     * <li>SWT.CENTER - alignment
     * </ul>
     * @param parent
     * @param none
     */
    public ExpressionViewer( Composite parent, int style ) {
        control = new Text( parent, style );
        
        control.addKeyListener(keyListener);
    }
    
    /**
     * This is the widget used to display the Expression; its parent has been provided
     * in the ExpressionViewer's constructor; but you may need direct access to it
     * in order to set layout data etc.
     *
     * @return
     */
    public Control getControl(){
        return control;
    }
    
    /**
     * Provides access to the Expression being used by this viewer.
     * <p>
     * @return Expression being viewed; may be Expression.NIL if empty (but will not be null)
     */
    @Override
    public Expression getInput() {
        return expr;
    }
    
    @Override
    public ISelection getSelection() {
        IStructuredSelection selection = new StructuredSelection(expr);
        return selection;
    }
    
    @Override
    public void refresh() {
        if( control != null ){
            control.getDisplay().asyncExec( new Runnable(){                
                public void run() {
                    if (control == null || control.isDisposed() ) return;
                    if( control instanceof Text){
                        Text text = (Text) control;
                        String cql = CQL.toCQL(expr);
                        text.setText( cql );
                    }
                }
            });
        }
    }
    
    /**
     * Set the input for this viewer.
     * <p>
     * This viewer accepts several alternative forms of input to get started:
     * <ul>
     * <li>Expression - is used directly
     * <li>String - is parsed by ECQL.toExpression; and if successful it is used
     * </ul>
     * If you have other suggestions (PropertyName could be provided by an AttributeType for example)
     * please ask on the mailing list.
     * @param input Expression or String to use as the input for this viewer
     */
    @Override    
    public void setInput( Object input ) {
        if( input instanceof Expression ){
            expr = (Expression) input;
        }
        else if (input instanceof String){
            String txt = (String) input;
            try {
                expr = ECQL.toExpression( txt );
            } catch (CQLException e) {
            }
        }
    }
    
    @Override
    public void setSelection( ISelection selection, boolean reveal ) {
        // do nothing by default
    }
}
