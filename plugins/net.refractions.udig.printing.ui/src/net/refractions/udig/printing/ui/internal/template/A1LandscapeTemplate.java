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
package net.refractions.udig.printing.ui.internal.template;

import net.refractions.udig.printing.model.Page;
import net.refractions.udig.project.internal.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Rectangle;

import com.lowagie.text.PageSize;

/**
 * Implementation of an A1 size Template in landscape mode. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class A1LandscapeTemplate extends LandScaleTemplate {

    /**
     * Constructs the BasicTemplate and populates its two boxes with a title and a map.
     */
    public A1LandscapeTemplate() {
        super();
    }

    /**
     * Populates the templates two boxes with a title and map
     * 
     * @param page the parent(owner) page
     * @param map the Map to be drawn
     */
    public void init( Page page, Map map ) {
        com.lowagie.text.Rectangle a1 = PageSize.A1;
        Dimension paperSize = new Dimension((int) a1.height(), (int) a1.width());
        page.setPaperSize(paperSize);

        setPageSizeFromPaperSize(page, paperSize);

        int height = page.getSize().height;
        int width = page.getSize().width;
        final int labelWidth = width;
        final int labelHeight;
        int legendWidth = 150;
        int legendHeight = 150;
        int scaleHeight = 20;
        int scaleWidth = 150;

        labelHeight = addLabelBox(map, width, labelWidth);

        Rectangle mapBounds = addMapBox(map, width, height, labelHeight, scaleHeight, legendWidth);

        addLegendBox(height, legendWidth, legendHeight, labelHeight, mapBounds);

        addScale(height, scaleHeight, scaleWidth);
    }
    
    @Override
    public String getAbbreviation() {
        return "A1L";
    }

}
