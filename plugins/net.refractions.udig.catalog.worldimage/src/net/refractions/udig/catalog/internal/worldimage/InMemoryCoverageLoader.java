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

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Hashtable;

import net.refractions.udig.catalog.rasterings.AbstractRasterGeoResource;
import net.refractions.udig.catalog.rasterings.GridCoverageLoader;
import net.refractions.udig.catalog.rasterings.RasteringsPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.ViewType;
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
@SuppressWarnings("deprecation")
public class InMemoryCoverageLoader extends GridCoverageLoader {

    private volatile GridCoverage coverage;
    private String fileName;

    public InMemoryCoverageLoader( AbstractRasterGeoResource resource, String fileName ) throws IOException {
        super(resource);
        this.fileName = fileName;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public synchronized GridCoverage load( GeneralGridGeometry geom, IProgressMonitor monitor )
            throws IOException {
        if( coverage == null ){

            try {
                AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) resource
                        .resolve(GridCoverageReader.class, monitor);
                GridEnvelope range = reader.getOriginalGridRange();
                GeneralEnvelope env = reader.getOriginalEnvelope();
                GridGeometry2D all = new GridGeometry2D(range, env);
                GridCoverage2D coverage2d = (GridCoverage2D) super.load(all, monitor);
                RenderedImage image = coverage2d.view(ViewType.RENDERED).getRenderedImage();
                BufferedImage bi = new BufferedImage(image.getColorModel(), (WritableRaster) image
                        .getData(), false, new Hashtable());
                GridCoverageFactory fac = new GridCoverageFactory();

                coverage = fac.create(fileName, bi, env);
                RasteringsPlugin
                        .log(
                                "WARNING.  Loading an image fully into memory.  It is about " + size(bi) + " MB in size decompressed", null); //$NON-NLS-1$//$NON-NLS-2$
            }catch (OutOfMemoryError e) {
                input = new InputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Maximum Permitted Memory Exceeded", )
            }
        }
        return coverage;
    }

    private int size( BufferedImage bi ) {
        int colorData = 0;
        for( int elem : bi.getColorModel().getComponentSize() ) {
            colorData += elem;
        }
        
        return (bi.getWidth()*bi.getHeight()*colorData)/1024;
    }

}
