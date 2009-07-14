package net.refractions.udig.catalog.internal.worldimage;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResourceInfo;

import org.eclipse.core.runtime.IStatus;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Describes this Resource.
 *
 * @author mleslie
 * @since 0.6.0
 */
public class IGeoResourceWorldImageInfo extends IGeoResourceInfo {
	/** IGeoResourceWorldImageInfo worldImageGeoResourceImpl field */
    private final WorldImageGeoResourceImpl worldImageGeoResourceImpl;

    IGeoResourceWorldImageInfo(WorldImageGeoResourceImpl worldImageGeoResourceImpl) {
        this.worldImageGeoResourceImpl = worldImageGeoResourceImpl;
        this.keywords = new String[]{"WorldImage", "world image", ".gif", ".jpg", ".jpeg", //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$ //$NON-NLS-5$
                ".tif", ".tiff", ".png"}; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        this.name = this.worldImageGeoResourceImpl.getIdentifier().getFile();
        int indexOf = name.lastIndexOf('/');
        if (indexOf > -1 && indexOf < name.length()) {
            name = name.substring(indexOf + 1);
        }
        this.title = name.replace('_', ' ');
        this.description = this.worldImageGeoResourceImpl.getIdentifier().toString();
        this.bounds = getBounds();
    }

	/*
	 * @see net.refractions.udig.catalog.IGeoResourceInfo#getBounds()
	 */
	public ReferencedEnvelope getBounds() {
		if (this.bounds == null) {
			Envelope env = null;
			try {
				GridCoverage source = (GridCoverage) this.worldImageGeoResourceImpl.findResource();
				org.opengis.geometry.Envelope ptBounds = source
						.getEnvelope();
				env = new Envelope(ptBounds.getMinimum(0), ptBounds
						.getMaximum(0), ptBounds.getMinimum(1), ptBounds
						.getMaximum(1));

				CoordinateReferenceSystem geomcrs = source
						.getCoordinateReferenceSystem();

				this.bounds = new ReferencedEnvelope(env, geomcrs);
				/*
				 * if(geomcrs != null) {
				 * if(!geomcrs.equals(CRS.decode("EPSG:4269"))) {
				 * //$NON-NLS-1$ bounds = JTS.transform(bounds,
				 * CRS.decode("EPSG:4269")); //$NON-NLS-1$ } } else {
				 * System.err.println("CRS unknown for WorldImage");
				 * //$NON-NLS-1$ }
				 */
			} catch (Exception e) {
				CatalogPlugin
						.getDefault()
						.getLog()
						.log(
								new org.eclipse.core.runtime.Status(
										IStatus.WARNING,
										"net.refractions.udig.catalog", 0, //$NON-NLS-1$
										"Error while getting the bounds of a layer", e)); //$NON-NLS-1$

				this.bounds = new ReferencedEnvelope(new Envelope(-180,
						180, -90, 90), DefaultGeographicCRS.WGS84);
			}
		}
		return this.bounds;
	}
}