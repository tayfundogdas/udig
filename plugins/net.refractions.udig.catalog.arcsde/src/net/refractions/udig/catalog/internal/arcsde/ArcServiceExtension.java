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
package net.refractions.udig.catalog.internal.arcsde;

import static org.geotools.arcsde.pool.ArcSDEConnectionConfig.DBTYPE_PARAM;
import static org.geotools.arcsde.pool.ArcSDEConnectionConfig.INSTANCE_NAME_PARAM;
import static org.geotools.arcsde.pool.ArcSDEConnectionConfig.PASSWORD_PARAM;
import static org.geotools.arcsde.pool.ArcSDEConnectionConfig.PORT_NUMBER_PARAM;
import static org.geotools.arcsde.pool.ArcSDEConnectionConfig.SERVER_NAME_PARAM;
import static org.geotools.arcsde.pool.ArcSDEConnectionConfig.USER_NAME_PARAM;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.AbstractDataStoreServiceExtension;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.catalog.arcsde.internal.Messages;

import org.geotools.arcsde.ArcSDEDataStoreFactory;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataAccessFactory.Param;

/**
 * Arc SDE Service Extension Implementation.
 * 
 * @author David Zwiers, Refractions Research
 * @since 0.6
 */
public class ArcServiceExtension extends AbstractDataStoreServiceExtension implements
        ServiceExtension {

    public IService createService(URL id, Map<String, Serializable> params) {
        final ArcSDEDataStoreFactory factory = getFactory();

        if (params != null && params.containsKey(PORT_NUMBER_PARAM)
                && params.get(PORT_NUMBER_PARAM) instanceof String) {
            String val = (String) params.get(PORT_NUMBER_PARAM);
            params.put(PORT_NUMBER_PARAM, new Integer(val));
        }

        if (!factory.canProcess(params))
            return null;
        if (id == null) {
            String host = (String) params.get(SERVER_NAME_PARAM);
            String port = params.get(PORT_NUMBER_PARAM).toString();
            String db = (String) params.get(INSTANCE_NAME_PARAM);
            if (null == db) {
                db = "";
            }
            try {
                URL serviceId = new URL("http://" + host + ":" + (port) + "/arcsde/" + db);
                return new ArcServiceImpl(serviceId, params); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } catch (MalformedURLException e) {
                // log this?
                e.printStackTrace();
                return null;
            }
        }
        return new ArcServiceImpl(id, params);
    }

    public Map<String, Serializable> createParams(URL url) {

        if (!isArcSDE(url))
            return null;

        ParamInfo info = parseParamInfo(url);

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(DBTYPE_PARAM, "arcsde"); // dbtype //$NON-NLS-1$
        params.put(SERVER_NAME_PARAM, info.host); // host
        params.put(PORT_NUMBER_PARAM, info.the_port); // port
        params.put(INSTANCE_NAME_PARAM, info.the_database); // database
        params.put(USER_NAME_PARAM, info.username); // user
        params.put(PASSWORD_PARAM, info.password); // pass

        if (getFactory().canProcess(params)) {
            return params;
        }
        return null;
    }

    /** A couple quick checks on the url */
    public static final boolean isArcSDE(URL url) {
        if (url == null)
            return false;
        return url.getProtocol().toLowerCase().equals("arcsde"); //$NON-NLS-1$
    }

    private static ArcSDEDataStoreFactory factory = null;

    /**
     * Factory describing ArcSDE connection parameters
     * 
     * @return factory describing ArcSDE connection parameters
     */
    protected static ArcSDEDataStoreFactory getFactory() {
        if (factory == null) {
            factory = new ArcSDEDataStoreFactory();
        }
        return factory;
    }

    @Override
    protected String doOtherChecks(Map<String, Serializable> params) {
        return null;
    }

    @Override
    protected DataStoreFactorySpi getDataStoreFactory() {
        return getFactory();
    }

    public String reasonForFailure(URL url) {
        if (url == null)
            return Messages.ArcServiceExtension_urlNull;
        if (!isArcSDE(url))
            return Messages.ArcServiceExtension_notSDEURL;
        return reasonForFailure(createParams(url));
    }
}
