/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.catalog.internal;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceFactory;
import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.core.internal.ExtensionPointProcessor;
import net.refractions.udig.core.internal.ExtensionPointUtil;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

/**
 * Default implementation of IServiceFactory used by the local catalog.
 * <p>
 * This is an internal class and defines no additional API of interest.
 * 
 * @author David Zwiers, Refractions Research
 * @since 0.6
 */
public class ServiceFactoryImpl implements IServiceFactory {
    
    /** Lock used to protect map of available services; for the last call? */
    private Lock lock = new ReentrantLock();

    /** @deprecated use createService */
    public List<IService> aquire( final URL id, final Map<String, Serializable> params ) {
        return acquire(id, params);
    }
    /** @deprecated use createService */
    public List<IService> aquire( Map<String, Serializable> params ) {
        return acquire(params);
    }
    /** @deprecated use createService */
    public List<IService> acquire( Map<String, Serializable> params ) {
        return acquire(null, params);
    }
    /** @deprecated use createService */
    public List<IService> aquire( final URL target ) {
        return acquire(target);
    }
    /** @deprecated use createService */
    public List<IService> acquire( URL target ){
        return createService(target);
    }

    /** @deprecated use createService */    
    public List<IService> acquire( final URL id, final Map<String, Serializable> params ) {
    	return createService(params);
    }
    
    /**
     * List candidate IService handles generated by all ServiceExtentions that think they can handle the
     * provided target drag and drop url.
     * <p>
     * Note: Just because a target is created does *NOT* mean it will actually work. You can check
     * the handles in the usual manner (ask for their info) after you get back this list.
     * </p>
     * 
     * @see net.refractions.udig.catalog.IServiceFactory#acquire(java.net.URL)
     * @param target Target url usually provided by drag and drop code
     * @return List of candidate services
     */
    public List<IService> createService( final URL target ) {
        final Map<String,Map<String, Serializable>> available = new HashMap<String, Map<String, Serializable>>();

        lock.lock();
        try {
           // load available
           ExtensionPointUtil.process(CatalogPlugin.getDefault(),
                    "net.refractions.udig.catalog.ServiceExtension", new ExtensionPointProcessor(){ //$NON-NLS-1$
                        public void process( IExtension extension, IConfigurationElement element )
                                throws Exception {
                            // extentionIdentifier used to report any problems;
                            // in the event of failure we want to be able to report
                            // who had the problem
                            String extentionIdentifier = extension.getUniqueIdentifier();
                            ServiceExtension se = (ServiceExtension) element
                                    .createExecutableExtension("class"); //$NON-NLS-1$
                            Map<String, Serializable> defaultParameters = se.createParams(target);
                            if (defaultParameters != null) {
                                available.put( extentionIdentifier, defaultParameters );
                            }
                        }
                    });
        } finally {
            lock.unlock();
        }
        
        List<IService> candidates = new LinkedList<IService>();
        for( Map.Entry<String,Map<String, Serializable>> candidateEntry : available.entrySet()){
            String extentionIdentifier = candidateEntry.getKey();
            Map<String,Serializable> connectionParameters = candidateEntry.getValue();
            try {
                List<IService> service = createService(connectionParameters);
                if (service != null && !service.isEmpty()){
                    candidates.addAll( service );
                }
            }
            catch( Throwable deadService ){
                CatalogPlugin.log(  extentionIdentifier+" could not create service", deadService); //$NON-NLS-1$
            }
        }
        return candidates;
    }

    public List<IService> createService( final Map<String, Serializable> connectionParameters ) {
        final List<IService> services = new LinkedList<IService>();
        lock.lock();
        try {
			// load services
			ExtensionPointUtil.process(CatalogPlugin.getDefault(),
					ServiceExtension.EXTENSION_ID,
					new ExtensionPointProcessor() {
						/**
						 * Attempt to construct a service, and add to the list if available.
						 * <p>
						 * Note: ExtentionPointUtil will log exceptions against provided plugin.
						 * </p>
						 * 
						 * @see net.refractions.udig.core.internal.ExtensionPointProcessor#process(org.eclipse.core.runtime.IExtension,
						 *      org.eclipse.core.runtime.IConfigurationElement)
						 * @param extension
						 * @param element
						 */
						public void process(IExtension extension,
								IConfigurationElement element) throws Exception {
							ServiceExtension se = (ServiceExtension) element
									.createExecutableExtension("class"); //$NON-NLS-1$                   
							IService service = se.createService(null,
									connectionParameters);
							if (service != null) {
								services.add(service);
							}
						}
					});
		} finally {
            lock.unlock();
        }
        return services;
    }
}
