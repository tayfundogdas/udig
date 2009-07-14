/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004-2008, Refractions Research Inc.
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
package net.refractions.udig.catalog.internal.worldimage;

import java.io.IOException;

import net.refractions.udig.catalog.rasterings.AbstractRasterGeoResource;
import net.refractions.udig.catalog.rasterings.GridCoverageLoader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridEnvelope;

/**
 * Keeps the full coverage in memory and returns the same instance
 * 
 * @author jeichar
 * @since 1.1.0
 */
public class InMemoryCoverageLoader extends GridCoverageLoader {

    private volatile GridCoverage coverage;

    public InMemoryCoverageLoader( AbstractRasterGeoResource resource, String fileName ) throws IOException {
        super(resource);
    }
    
    @Override
    public synchronized GridCoverage load( GeneralGridGeometry geom, IProgressMonitor monitor )
            throws IOException {
        if( coverage == null ){

            AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) resource.resolve(
                    GridCoverageReader.class, monitor);
            GridEnvelope range = reader.getOriginalGridRange();
            GeneralEnvelope env = reader.getOriginalEnvelope();
            GridGeometry2D all = new GridGeometry2D(range, env);
            coverage = super.load(all, monitor);
        }
        return coverage;
    }

}
