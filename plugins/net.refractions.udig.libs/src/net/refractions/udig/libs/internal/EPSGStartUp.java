package net.refractions.udig.libs.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.osgi.framework.Bundle;

/**
 * Attempts to Load the EPSG database when uDig startups. The very first time this is expected to
 * take a while as the database is unpacked into a temporary folder.
 * 
 * @author jody
 * @since 1.1.0
 */
public class EPSGStartUp implements IStartup {

    public static void initializeEPSG( Bundle bundle, IProgressMonitor monitor ) {
        if (monitor == null)
            monitor = new NullProgressMonitor();

        monitor.beginTask("EPSG Database", 100);

        Activator.configureEPSG(bundle, new SubProgressMonitor(monitor, 80));

        monitor.subTask("coordinate operation definitions");
        load(ReferencingFactoryFinder.getCoordinateOperationAuthorityFactories(null));
        monitor.worked(2);

        monitor.subTask("coordinate reference systems");
        load(ReferencingFactoryFinder.getCRSFactories(null));
        monitor.worked(8);

        monitor.subTask("coordinate systems");
        load(ReferencingFactoryFinder.getCSFactories(null));
        monitor.worked(2);

        monitor.subTask("datum definitions");
        load(ReferencingFactoryFinder.getDatumAuthorityFactories(null));
        monitor.worked(2);

        monitor.subTask("datums");
        load(ReferencingFactoryFinder.getDatumFactories(null));
        monitor.worked(2);

        monitor.subTask("math transforms");
        load(ReferencingFactoryFinder.getMathTransformFactories(null));
        monitor.worked(4);
    }

    public void earlyStartup() {
        // this is the bundle to check for an epsg.properties file
        //
        final Bundle bundle = Platform.getBundle(Activator.ID);
        System.out.println("EPSG...");
        initializeEPSG(bundle, null);
        /*
        Display display = PlatformUI.getWorkbench().getDisplay();
        display.asyncExec(new Runnable(){
            public void run() {
                try {
                    // We needed to be in the display thread or we would not be able
                    // to get a progress dialog here.
                    // The ProgressService gives us 800 milliseconds to load everything
                    // up - if we take longer then that a dialog will be shown
                    //
                    // ProgressMonitorDialog progress = new
                    // ProgressMonitorDialog(workbench.getDisplay().getActiveShell());

                    PlatformUI.getWorkbench().getProgressService().run(true, false,
                    // progress.run(true,false,
                            new IRunnableWithProgress(){
                                public void run( IProgressMonitor monitor )
                                        throws InvocationTargetException, InterruptedException {
                                    initializeEPSG(bundle, monitor);
                                }
                            });
                } catch (Throwable t) {
                    Platform.getLog(bundle).log(
                            new Status(Status.ERROR, Activator.ID, t.getLocalizedMessage(), t));
                }
            }
        });
    */
    }

    @SuppressWarnings("unchecked")
    static private void load( Set coordinateOperationAuthorityFactories ) {
        for( Iterator iter = coordinateOperationAuthorityFactories.iterator(); iter.hasNext(); ) {
            iter.next();
        }
    }

}
