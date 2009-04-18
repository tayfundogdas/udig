/**
 * 
 */
package net.refractions.udig.catalog;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.core.internal.CorePlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Allows IService connection parameters to be stored
 * into a preference object - and for restoring Service
 * again.
 * <p>
 * IResolves that are not services will be ignored; the default
 * implementation of locateService( URL, Map ) will add
 * the created services into the provided catalog.
 * <p>
 * {@link #locateService(URL, Map)} can be overridden to not create and
 * add the service to the catalog.
 * </p>
 * @author Jesse
 */
public class ServiceParameterPersister {
	private static final String PROPERTIES_KEY = "_properties"; //$NON-NLS-1$
    private static final String VALUE_ID = "value"; //$NON-NLS-1$
	private static final String TYPE_ID = "type"; //$NON-NLS-1$
    private static final String ENCODING = "UTF-8"; //$NON-NLS-1$

	protected final ICatalog localCatalog;
	protected final IServiceFactory serviceFactory;
	/** reference directory to consider when making relative files? */
	private File reference;

	public ServiceParameterPersister(final ICatalog localCatalog, final IServiceFactory serviceFactory) {
		this(localCatalog, serviceFactory, null);
	}
	
	public ServiceParameterPersister(final ICatalog localCatalog, final IServiceFactory serviceFactory, File reference) {
		super();
		this.localCatalog = localCatalog;
		this.serviceFactory = serviceFactory;
		this.reference=reference;
	}
	/**
	 * Using the connection parameter information in the preferences node
	 * restore the state of the local catalog.
	 * 
	 * @param node
	 */
	public void restore(Preferences node) {
		try {
			for (String id : node.childrenNames()) {
				try {
					URL url = toURL(id);
					Preferences servicePref = node.node(id);
					
					// BACKWARDS COMPATIBILITY
					Map<String, Serializable> connectionParams = backwardCompatibleRestore(servicePref);
					
					String[] nodes = servicePref.childrenNames();
					for (String childName : nodes) {
					    if( PROPERTIES_KEY.equals(childName)) {
					        // slip properties entry
					        continue;
					    }
						mapAsObject(servicePref, connectionParams, childName);
					}
					
					Preferences propertiesPref = servicePref.node(PROPERTIES_KEY);
					propertiesPref.flush();
                    Map<String, Serializable> properties = restoreProperties(propertiesPref);

					locateService(url, connectionParams, properties);
				} catch (Throwable t) {
					CatalogPlugin.log(null, new Exception(t));
				}
			}

		} catch (Throwable t) {
			CatalogPlugin.log(null, new Exception(t));
		}
	}
	/**
	 * Convert a persisted id string into a URL.
	 * <p>
	 * This method will decode the string based ENCODING
	 * @param id Persisted id string
	 * @return URL based on provided id string
	 * @throws MalformedURLException If the id could not be decoded into a valid URL
	 */
    private URL toURL( String id ) throws MalformedURLException {
        URL url;
        try {
        	url = new URL(null, URLDecoder.decode(id, ENCODING), CorePlugin.RELAXED_HANDLER);
        } catch (UnsupportedEncodingException e) {
        	CatalogPlugin.log("Could not code preferences URL", e); //$NON-NLS-1$
        	throw new MalformedURLException(e.toString());
        }
        return url;
    }

	/**
	 * Helper method that will unpack a servicePreference node
	 * into a map of connection parameters.
	 * @param service
	 * @param keys
	 * @return Connection parameters
	 */
	private Map<String, Serializable> backwardCompatibleRestore(Preferences preference ) {
	    Map<String, Serializable> map = new HashMap<String, Serializable>();
	    String[] keys;
        try {
            keys = preference.keys();
    		for( int j = 0; j < keys.length; j++ ) {
    			String currentKey = keys[j];
    			if( PROPERTIES_KEY.equals( currentKey )) {
    			    continue;
    			}
    			map.put(currentKey, preference.get(currentKey, null));
    		}
        } catch (BackingStoreException e) {
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }               
		return map;
	}
		
	/**
	 * Create an IService from the provided connection parameters
	 * and add them to the provided catalog.
	 * 
	 * @Param targetID In the event of a tie favour the provided targetID
	 * @param connectionParameters Used to to ask the ServiceFactory for list of candidates 
	 */
	protected void locateService(URL targetID, Map<String, Serializable> connectionParameters,  Map<String,Serializable> properties) {
		List<IService> newServices = serviceFactory.createService(connectionParameters);
		if( !newServices.isEmpty() ){
			for( IService service : newServices ) {
			    // should we check the local catalog to see if it already
			    // has an entry for this service?
			    IService found = localCatalog.getById( IService.class,service.getIdentifier(), null );
			    if( found == null ){
			        localCatalog.add(service);
			    }
			    else {
			        // Service was already available
			    }
			    try {
			        // restore persisted properties			        
                    service.getPersistentProperties().putAll( properties );
                } catch (Exception e) {
                    // could not restore propreties
                }
		    }
		} else {
			CatalogPlugin.log("Nothing was able to be loaded from saved preferences: "+connectionParameters, null); //$NON-NLS-1$
		}
	}
	/**
	 * Performs some post processing on the connection parameters to ensure
	 * they are prompted from Strings to Objects (if possible).
	 * 
	 * @param servicePreferenceNode
	 * @param connectionParams
	 * @param currentKey
	 * @throws MalformedURLException
	 */
    private void mapAsObject(Preferences servicePreferenceNode, Map<String, Serializable> connectionParams, String currentKey) {
		Preferences paramNode = servicePreferenceNode.node(currentKey);
		String value=paramNode.get(VALUE_ID, null);
		try {
			value = URLDecoder.decode(value, ENCODING);
		} catch (UnsupportedEncodingException e) {
			CatalogPlugin.log("error decoding value, using undecoded value", e); //$NON-NLS-1$
		}
		String type=paramNode.get(TYPE_ID, null);		
		Serializable obj = toObject( value, type );
		connectionParams.put(currentKey, obj);
	}
	
	private Serializable toObject( String txt, String type ){
		try{
			Class<?> clazz=Class.forName(type);
			
			// reference can be null so only decode relative path if reference is not null.
			// ie assume the URL/File is absolute if reference is null
			if( reference !=null && (URL.class.isAssignableFrom(clazz) 
					|| File.class.isAssignableFrom(clazz) )){
				URL result;
                try {
                    result = URLUtils.constructURL(this.reference, txt);
                    if( URL.class.isAssignableFrom(clazz) )
                        return (Serializable) result;
                    else
                        return new File( result.getFile());                    
                } catch (MalformedURLException e) {
                    CatalogPlugin.log(type+" was not able to use as a URL so we're putting it in to the parameters as a String", null); //$NON-NLS-1$                    
                    return txt;
                }							
			}
			
			try{
				// try finding the constructor that takes a string
				Constructor<?> constructor = clazz.getConstructor(new Class[]{String.class});
				Object object = constructor.newInstance(new Object[]{txt});
				return (Serializable) object;
			}catch(Throwable t){
				//failed lets try a setter
				try{
					Method[] methods = clazz.getMethods();
					Method bestMatch = findBestMatch(methods);
					
					if( bestMatch!=null ){
						Object obj = clazz.newInstance();
						bestMatch.invoke(obj, new Object[]{txt});
						return (Serializable) obj;
					}
				}catch (Throwable t2) {
					CatalogPlugin.log("error that occurred when trying use construction with string: "+type+" value= "+txt, t ); //$NON-NLS-1$ //$NON-NLS-2$
					CatalogPlugin.log("error that occurred when use a setter: "+type+" value= "+txt, t2 );  //$NON-NLS-1$//$NON-NLS-2$
				}
			}
			
		} catch(ClassNotFoundException cnfe){
			CatalogPlugin.log(type+" was not able find declared type so we're putting it in to the parameters as a String", null); //$NON-NLS-1$			
		}
		return txt;
	}

	private Method findBestMatch(Method[] methods) {
		Method bestMatch=null;
		for (Method method : methods) {
			Class<?>[] methodParams = method.getParameterTypes();
			if( methodParams.length==1 && methodParams[0].isAssignableFrom(String.class) ){
				// is this a setter or a parse?
				if( method.getName().startsWith("parse") ){ //$NON-NLS-1$
					if( bestMatch==null ){
						bestMatch=method;
						continue;
					}else{
						if( bestMatch.getName().startsWith("set") ){ //$NON-NLS-1$
							bestMatch=method;
							continue;
						}
					}
				}
				
				if( method.getName().startsWith("set") ){ //$NON-NLS-1$
					if( bestMatch==null ){
						bestMatch=method;
						continue;
					}
				}
			}
		}
		return bestMatch;
	}
	

	public void store(IProgressMonitor monitor, Preferences node,
			Collection<? extends IResolve> resolves ) throws BackingStoreException, IOException {
		clearPreferences(node);
        for( IResolve member : resolves ) {
            try {
                if( !CatalogPlugin.getDefault().getPreferenceStore().getBoolean("SaveTemporaryDataTypes")  //$NON-NLS-1$
                        && member.canResolve(ITransientResolve.class ) )
                    continue;
                IService service=null ;
                if( member instanceof IGeoResource ){
                	service=((IGeoResource)member).service(monitor);
                }else if( member instanceof IService ){
                	service=(IService)member;
                }
                // its not a type that we know how to get the parameters from
                if( service==null )
                	continue;
                
                String id;
				try {
                    id = URLEncoder.encode(service.getIdentifier().toExternalForm(), ENCODING);
                } catch (UnsupportedEncodingException e1) {
                    // should never happen
                    CatalogPlugin.log(null, e1);
                    throw new BackingStoreException(e1.toString());
                }

                Preferences serviceNode = node.node(id);

                for ( Map.Entry<String, Serializable> entry : service.getConnectionParams().entrySet()) {
                    String key = entry.getKey().toString();
                    
                    Serializable object = entry.getValue();
                    URL url=null;
                    if( object instanceof URL){
                    	url = (URL) object;
                    }else if( object instanceof File ){
                        File file = (File) object;
                        
                        URL old=file.toURL();
                    	url=file.toURI().toURL();
                    	if( !old.equals(url)){
                    	    CatalogPlugin.trace("old url:"+old,null); //$NON-NLS-1$
                    	    CatalogPlugin.trace("new url:"+url,null); //$NON-NLS-1$
                    	}
                    }
                    
                    String value;
                    // if reference is null then we can only encode the absolute path
                    if( reference!=null && url !=null ){
                    	URL relativeURL = URLUtils.toRelativePath(this.reference, url);
                    	value = relativeURL.toExternalForm();
                    }else{
                    	value = object == null ? null : object.toString();
                    }

                    if (value != null){
                    	value= URLEncoder.encode( value, ENCODING );
                        Preferences paramNode = serviceNode.node(key);
                        paramNode.put(VALUE_ID, value);
                        paramNode.put(TYPE_ID, object.getClass().getName());
                    }
                }
                try {
                    Map<String, Serializable> persistentProperties = service.getPersistentProperties();
                    
                    Preferences propertiesNode = serviceNode.node(PROPERTIES_KEY);                    
                    storeProperties( propertiesNode, persistentProperties );
                    propertiesNode.flush();
                } catch (Exception e) {
                    throw (RuntimeException) new RuntimeException( ).initCause( e );
                }
                
                if (serviceNode.keys().length > 0)
                    serviceNode.flush();
                monitor.worked(1);
            } catch (RuntimeException e) {
                CatalogPlugin.log("Error storing: "+member.getIdentifier(), e); //$NON-NLS-1$
            }
        }
        node.flush();
	}

    private void storeProperties( Preferences prefs,
            Map<String, Serializable> properties ) {
        
        for ( Map.Entry<String, Serializable> entry : properties.entrySet()) {
            
            final String KEY = entry.getKey().toString();            
            Serializable object = entry.getValue();
            
            String txt;
            if( object == null ){
                txt = null;
            }
            else {
                txt =  object.toString();            
            }

            if (txt != null){
                try {
                    txt= URLEncoder.encode( txt, ENCODING );
                    Preferences paramNode = prefs.node(KEY);
                    
                    paramNode.put(VALUE_ID, txt);
                    paramNode.put(TYPE_ID, object.getClass().getName());
                    paramNode.flush();
                    
                } catch (Exception e) {
                    CatalogPlugin.trace("Could not encode "+KEY+" - "+e, e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
    }
    /**
     * Helper method that will unpack a Preference node into a map of properties.
     * @param service
     * @param keys
     * @return Connection parameters
     */
    private Map<String, Serializable> restoreProperties(Preferences preference ) {
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        String[] keys;
        try {
            keys = preference.childrenNames(); //preference.keys();
            for( int j = 0; j < keys.length; j++ ) {
                final String KEY = keys[j];
                Preferences paramNode = preference.node(KEY);                
                String txt = paramNode.get(VALUE_ID,null);
                if( txt == null ) continue;
                try {
					txt= URLDecoder.decode( txt, ENCODING );
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
                String type = paramNode.get(TYPE_ID,null);
                
                Serializable value = toObject( txt, type );
                map.put(KEY, value );
            }
        } catch (BackingStoreException e) {
            throw (RuntimeException) new RuntimeException( ).initCause( e );
        }               
        return map;
    }
    
    private void clearPreferences( Preferences node ) throws BackingStoreException {
        for( String name : node.childrenNames() ) {
            Preferences child = node.node(name);
            child.removeNode();
        }
    }


}
