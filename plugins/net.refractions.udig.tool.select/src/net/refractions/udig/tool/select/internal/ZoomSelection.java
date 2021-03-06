/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.tool.select.internal;

import java.io.IOException;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.command.navigation.SetViewportBBoxCommand;
import net.refractions.udig.project.internal.render.impl.ScaleUtils;
import net.refractions.udig.project.ui.tool.AbstractActionTool;
import net.refractions.udig.tool.select.SelectPlugin;
import net.refractions.udig.ui.ProgressManager;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Sets the ViewportModel bounds to equal the bounds of the selected features
 * in the selected layer.
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class ZoomSelection extends AbstractActionTool {

    public static final String ID = "net.refractions.udig.tool.default.show.selection"; //$NON-NLS-1$

    public ZoomSelection() {
    }

    public void run() {
        ILayer layer = getContext().getSelectedLayer();
        if ( layer.hasResource(FeatureSource.class)){
            try {
            	FeatureSource<SimpleFeatureType, SimpleFeature> resource = featureSource(layer);
                Query query = new DefaultQuery( resource.getSchema().getTypeName(), layer.getFilter(), 
                        new String[]{resource.getSchema().getGeometryDescriptor().getLocalName()});
                ReferencedEnvelope bounds = resource.getBounds(query);
                if( bounds==null ){
                	ReferencedEnvelope envelope = resource.getFeatures(query).getBounds();
                	if (envelope != null) {
                		bounds = new ReferencedEnvelope(envelope, layer.getCRS());
                	}
                }
             // If the selection is a single point the bounds will 
                // have height == 0 and width == 0. This will break
                // in ScaleUtils:306. Adding 1 to the extent fixes the problem:
                if (bounds.getHeight() <= 0 || bounds.getWidth() <= 0) {
                    bounds.expandBy(1);
                }
                bounds = ScaleUtils.fitToMinAndMax(bounds, layer);
                
                getContext().sendASyncCommand(new SetViewportBBoxCommand(bounds,
                        layer.getCRS()));
            } catch (IOException e) {
                SelectPlugin.log("failed to obtain resource", e); //$NON-NLS-1$
            }
            
        }
    }

	@SuppressWarnings("unchecked")
	private FeatureSource<SimpleFeatureType, SimpleFeature> featureSource(
			ILayer layer) throws IOException {
		FeatureSource<SimpleFeatureType, SimpleFeature> resource = layer.getResource(FeatureSource.class, ProgressManager.instance().get());
		return resource;
	}

    public void dispose() {
    }

}
