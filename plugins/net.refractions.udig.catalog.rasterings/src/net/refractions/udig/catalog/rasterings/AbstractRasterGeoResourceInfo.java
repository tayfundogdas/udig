package net.refractions.udig.catalog.rasterings;

import java.io.File;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResourceInfo;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class AbstractRasterGeoResourceInfo extends IGeoResourceInfo {
    protected final AbstractRasterGeoResource resource;
    
    
    public AbstractRasterGeoResourceInfo( AbstractRasterGeoResource resource, String... keywords ) {
        this.resource = resource;
        
        super.keywords = keywords;
    }

    /**
     * @return base name if available followed by the qualifier if supplied
     */
    @Override
    public String getTitle() {
        String name = resource.getID().toBaseFile();
        String qualifier = resource.getID().getTypeQualifier();
        if( qualifier != null ){
            return name+" ("+qualifier+")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return name;
    }
    
    @Override
    public String getDescription() {
        return resource.getIdentifier().toString();
    }

    @Override
    public synchronized ReferencedEnvelope getBounds() {
        if (this.bounds == null) {
            try {
                AbstractGridCoverage2DReader source = this.resource.service(new NullProgressMonitor()).getReader(null);
                if (source == null) {
                    return null;
                }

                GeneralEnvelope ptBounds = source.getOriginalEnvelope();
                Envelope env = new Envelope(ptBounds.getMinimum(0), ptBounds.getMaximum(0),
                        ptBounds.getMinimum(1), ptBounds.getMaximum(1));

                CoordinateReferenceSystem geomcrs = source.getCrs();
                if (geomcrs == null) {
                    geomcrs = DefaultEngineeringCRS.GENERIC_2D;
                }

                this.bounds = new ReferencedEnvelope(env, geomcrs);
            } catch (Exception e) {
                CatalogPlugin
                        .getDefault()
                        .getLog()
                        .log(
                                new org.eclipse.core.runtime.Status(
                                        IStatus.WARNING,
                                        "net.refractions.udig.catalog", 0, "Error while getting the bounds of a layer", e)); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return this.bounds;
    }
}
