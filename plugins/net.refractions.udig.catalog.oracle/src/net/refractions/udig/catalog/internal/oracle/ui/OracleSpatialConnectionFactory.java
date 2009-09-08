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
package net.refractions.udig.catalog.internal.oracle.ui;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.geotools.data.oracle.OracleNGDataStoreFactory;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension2;
import net.refractions.udig.catalog.internal.oracle.OracleServiceExtension;
import net.refractions.udig.catalog.ui.AbstractUDIGConnectionFactory;

/**
 * This appears to be glue code added by Jesse.
 * 
 * @since 1.2.0
 */
public class OracleSpatialConnectionFactory extends AbstractUDIGConnectionFactory {

    @Override
    protected Map<String, Serializable> doCreateConnectionParameters( Object context ) {
        // we need to check the provided object (probably a URL)
        // and ensure it is ment for us
        ID id = ID.cast( context );
        if( id.toString().indexOf("oracle") != -1){
            
        }        
        return null;
    }


    @Override
    protected URL doCreateConnectionURL( Object context ) {
        return null;
    }

    @Override
    protected boolean doOtherChecks( Object context ) {
        return false;
    }

    @Override
    protected ServiceExtension2 getServiceExtension() {
        return new OracleServiceExtension();
    }

}
