package net.refractions.udig.libs.internal;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.spi.ImageReaderSpi;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.datalocation.Location;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.factory.Hints.Key;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.PropertyAuthorityFactory;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.referencing.factory.epsg.ThreadedH2EpsgFactory;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The Activator for net.refractions.udig.libs provides global settings
 * to help all the open source projects get along.
 * <p>
 * Currently this activator supplied:
 * <ul>
 * <li>hints about axis order for GeoTools;
 * <li>instructs java not to use native PNG support; see UDIG-1391 for details
 * </ul>
 * <p>
 * The contents of this Activator will change over time according to the needs
 * of the libraries and tool kits we are using.
 * </p>
 * @author Jody Garnett
 * @since 1.1.0
 */
public class Activator implements BundleActivator {
	
	public void start(final BundleContext context) throws Exception {
	    if( Platform.getOS().equals(Platform.OS_WIN32) ){
		    try {
		        // PNG native support is not very good .. this turns it off
		        ImageUtilities.allowNativeCodec("png", ImageReaderSpi.class, false);  //$NON-NLS-1$
		    }
		    catch (Throwable t){
		        // we should not die if JAI is missing; we have a warning for that...
		        System.out.println("Difficulty turnning windows native PNG support (which will result in scrambled images from WMS servers)"); //$NON-NLS-1$
		        t.printStackTrace();
		    }
        }

	    // System properites work for controlling referencing behavior
	    // not so sure about the geotools global hints
	    //
	    System.setProperty("org.geotools.referencing.forceXY", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        Map<Key, Boolean> map = new HashMap<Key, Boolean> ();
	    // these commented out hints are covered by the forceXY system property
		//
		//map.put( Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, true );
	    //map.put( Hints.FORCE_STANDARD_AXIS_DIRECTIONS, true );
		//map.put( Hints.FORCE_STANDARD_AXIS_UNITS, true );
	    map.put( Hints.LENIENT_DATUM_SHIFT, true );		
		Hints global = new Hints(map);
		GeoTools.init( global );
		// WARNING - the above hints are recommended to us by GeoServer
		//           but they cause epsg-wkt not to work because the
		//           various wrapper classes trip up over a CRSAuthorityFactory
		//           that is not also a OperationAuthorityFactory (I think)
		//
		
		// prime the pump - ensure EPSG factory is found
		// (we need to do this in a separate thread if the database needs to be unpacked)
	    if( ThreadedH2EpsgFactory.isUnpacked() ){
	    	configureEPSG(context.getBundle(), new NullProgressMonitor());	
	    }
	    else {
	    	final Bundle bundle = context.getBundle();
	    	Job configure  = new Job("configure epsg"){
				protected IStatus run(IProgressMonitor monitor) {
					try {
						configureEPSG(bundle, monitor);
					}
					catch( Exception eek ){
						return new Status(IStatus.ERROR,"net.refractions.udig.libs", "Difficulty configuring epsg database:"+eek, eek );
					}
					return Status.OK_STATUS;
				}	    		
	    	};
	    	//configure.setUser(true);
	    	configure.schedule();
	    }
	}
	
	public void configureEPSG(Bundle bundle, IProgressMonitor monitor) throws Exception {
		if( monitor == null ) monitor = new NullProgressMonitor();
		monitor.beginTask("epsg setup", IProgressMonitor.UNKNOWN );
		try {
			/*
		    // To pick up H2 in plugin form
		    URL epsgInternal = context.getBundle().getEntry("epsg");
		    URL epsgUrl = FileLocator.toFileURL( epsgInternal );
		    File epsgDB = new File( epsgUrl.toURI() );
		    System.setProperty("EPSG-H2.directory", epsgDB.getPath() );
		    */        
			monitor.subTask("initialize database");
		    CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326"); 
	        if( wgs84 == null){
	        	String msg = "Unable to locate EPSG authority for EPSG:4326; consider removing temporary 'GeoTools' directory and trying again."; //$NON-NLS-1$
	        	System.out.println( msg );
	        	//throw new FactoryException(msg);
	        }
	        monitor.worked(1);
	        
	        // go through and check a couple of locations
	        // for an "epsg.properties" file full of 
	        // suplementary codes
	        //
			URL epsg = null;
			Location configLocaiton = Platform.getInstallLocation();
			Location dataLocation = Platform.getInstanceLocation();
			if( dataLocation != null ){
				try {
	        	    URL url = dataLocation.getURL();
	        	    URL proposed = new URL( url, "epsg.properties");
	        	    monitor.subTask("check "+proposed );
	        	    if( "file".equals(proposed.getProtocol())){
	        	        File file = new File( proposed.toURI() );
	        	        if( file.exists() ){
	        	            epsg = file.toURI().toURL();
	        	        }
	        	    }
	        	    monitor.worked(1);
			    }
			    catch (Throwable t ){
			    	if( Platform.inDebugMode()){
			    		System.out.println( "Could not find data directory epsg.properties");
			    		t.printStackTrace();
			    	}			        
			    }
			}
			if( epsg == null && configLocaiton != null ){
	            try {
	                URL url = configLocaiton.getURL();
	                URL proposed = new URL( url, "epsg.properties");
	                monitor.subTask("check "+proposed );
	                if( "file".equals(proposed.getProtocol())){
	                    File file = new File( proposed.toURI() );
	                    if( file.exists() ){
	                        epsg = file.toURI().toURL();
	                    }
	                }
	                monitor.worked(1);
	            }
	            catch (Throwable t ){
	            	if( Platform.inDebugMode()){
			    		System.out.println( "Could not find configuration epsg.properties");
			    		t.printStackTrace();
			    	}
	            }
			}
			if (epsg == null ){
				try {
			        URL internal = bundle.getEntry("epsg.properties");
			        URL fileUrl = FileLocator.toFileURL( internal );
			        epsg = fileUrl.toURI().toURL();
			    }
			    catch (Throwable t ){
			    	if( Platform.inDebugMode()){
			    		System.out.println( "Could not find net.refractions.udig.libs/epsg.properties");
			    		t.printStackTrace();
			    	}
	            }		    
			}
			
			if( epsg != null ){
				monitor.subTask("loading "+epsg);            
			    Hints hints = new Hints(Hints.CRS_AUTHORITY_FACTORY, PropertyAuthorityFactory.class);
			    ReferencingFactoryContainer referencingFactoryContainer = ReferencingFactoryContainer
	                .instance(hints);
	
			    PropertyAuthorityFactory factory = new PropertyAuthorityFactory(
	                referencingFactoryContainer, Citations.fromName("EPSG"), epsg );
	
			    ReferencingFactoryFinder.addAuthorityFactory(factory);
			    monitor.worked(1);
			}
			monitor.subTask("register "+epsg);
			ReferencingFactoryFinder.scanForPlugins(); // hook everything up
			monitor.worked(1);
			
			// Show EPSG authority chain if in debug mode
			//
			if( Platform.inDebugMode() ){
	            CRS.main(new String[]{"-dependencies"}); //$NON-NLS-1$
	        }
			// Verify EPSG authority configured correctly
			// if we are in development mode
			if( Platform.inDevelopmentMode() ){
				verifyReferencingEpsg();
				verifyReferencingOperation();
			}
		}
		finally {
			monitor.done();
		}
	}
    /**
     * If this method fails it's because, the epsg jar is either 
     * not available, or not set up to handle math transforms
     * in the manner udig expects.
     * 
     * @return true if referencing is working and we get the expected result
     * @throws Exception if we cannot even get that far
     */
	private void verifyReferencingEpsg() throws Exception {
        CoordinateReferenceSystem WGS84 = CRS.decode("EPSG:4326"); // latlong //$NON-NLS-1$
        CoordinateReferenceSystem BC_ALBERS = CRS.decode("EPSG:3005"); //$NON-NLS-1$
        
        MathTransform transform = CRS.findMathTransform(BC_ALBERS, WGS84 );
        DirectPosition here  = new DirectPosition2D( BC_ALBERS, 1187128, 395268 );
        DirectPosition there = new DirectPosition2D( WGS84, -123.47009173007372,48.54326498732153 );
        	
        DirectPosition check = transform.transform( here, new GeneralDirectPosition(WGS84) );
        //DirectPosition doubleCheck = transform.inverse().transform( check, new GeneralDirectPosition(BC_ALBERS) );        
//        if( !check.equals(there)){
//        	String msg = "Referencing failed to produce expected transformation; check that axis order settings are correct.";
//        	System.out.println( msg );
//        	//throw new FactoryException(msg);
//        }
        double delta = Math.abs(check.getOrdinate(0) - there.getOrdinate(0))+Math.abs(check.getOrdinate(1) - there.getOrdinate(1));
		if( delta > 0.0001){
			String msg = "Referencing failed to transformation with expected accuracy: Off by "+delta + "\n"+check+"\n"+there;   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			System.out.println( msg );
        	//throw new FactoryException(msg);
        }	
	}
    /**
     * If this method fails it's because, the epsg jar is either 
     * not available, or not set up to handle math transforms
     * in the manner udig expects.
     * 
     * @return true if referencing is working and we get the expected result
     * @throws Exception if we cannot even get that far
     */
	private void verifyReferencingOperation() throws Exception {
	       // ReferencedEnvelope[-0.24291497975705742 : 0.24291497975711265, -0.5056179775280899 : -0.0]
        // ReferencedEnvelope[-0.24291497975705742 : 0.24291497975711265, -0.5056179775280899 : -0.0]
        CoordinateReferenceSystem EPSG4326 = CRS.decode("EPSG:4326"); //$NON-NLS-1$
        ReferencedEnvelope pixelBounds = new ReferencedEnvelope( -0.24291497975705742, 0.24291497975711265, -0.5056179775280899, 0.0, EPSG4326 );
        CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
        
        ReferencedEnvelope latLong = pixelBounds.transform( WGS84, false );
        if( latLong == null){
        	String msg = "Unable to transform EPSG:4326 to DefaultGeographicCRS.WGS84"; //$NON-NLS-1$
        	System.out.println( msg );        		
        	//throw new FactoryException(msg);
        }
	}
	
	public void stop(BundleContext context) throws Exception {
	}

}
