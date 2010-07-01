package net.refractions.udig.catalog.geotools.process;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.process.ProcessFactory;
import org.geotools.process.Processors;

/**
 * Gathers up all ProcessFactories findable by GeoTools into the local catalog.
 * 
 * @author Jody Garnett (LISAsoft)
 * @since 1.2.0
 */
public class LocalProcessService extends IService {

    /**
     * ID used for geotools processes
     */
    public final static String ID = "process:///localhost/geotools/process"; //$NON-NLS-1$
    
    /** Service ID used for GeoTools processes */
    public final static ID SERVICE_ID = new ID(ID,"geotools");

    /**
     * Contents are presented as a series of folders; once for each process factory.
     */
    public volatile List<IResolve> folders;

    @Override
    public Map<String, Serializable> getConnectionParams() {
        HashMap<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(LocalProcessServiceExtension.KEY, SERVICE_ID.toURL() );
        return params;
    }
    
    public Status getStatus() {
        if( folders != null ){
            return Status.CONNECTED;
        }
        else {
            return Status.NOTCONNECTED;
        }
    }

    public Throwable getMessage() {
        return null;
    }

    @Override
    public net.refractions.udig.catalog.ID getID() {
        return SERVICE_ID;
    }
    
    public URL getIdentifier() {
        return SERVICE_ID.toURL();
    }

    @Override
    public synchronized List<IResolve> members( IProgressMonitor monitor ) throws IOException {
        if( folders == null ){
            folders = new ArrayList<IResolve>();
            for( ProcessFactory factory : Processors.getProcessFactories() ){
                LocalProcessFolder folder = new LocalProcessFolder(this, factory );
                folders.add( folder );
            }            
        }
        return folders;
    }
    
    /**
     * This service does not provide any spatial data.
     * @return null, as this service does not provide any spatial data
     */
    @Override
    public List<? extends IGeoResource> resources( IProgressMonitor monitor ) throws IOException {
        return null;
    }

    @Override
    protected IServiceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
