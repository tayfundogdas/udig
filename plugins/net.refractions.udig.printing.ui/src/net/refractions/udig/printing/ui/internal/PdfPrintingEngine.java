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
package net.refractions.udig.printing.ui.internal;

import static net.refractions.udig.printing.ui.internal.PrintingPlugin.TRACE_PRINTING;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.refractions.udig.printing.model.Box;
import net.refractions.udig.printing.model.BoxPrinter;
import net.refractions.udig.printing.model.Page;
import net.refractions.udig.printing.ui.Template;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * The engine that prints to pdf.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PdfPrintingEngine {

    private Page page;
    private IProgressMonitor monitor;
    private final File outputPdfFile;

    /**
     * Constructs a PdfPrintingEngine using the given Page and the file to which to dump to.
     * 
     * @param page the Page to be printed.
     * @param outputPdfFile the file to which to dump to.
     */
    public PdfPrintingEngine( Page page, File outputPdfFile ) {
        this.page = page;
        this.outputPdfFile = outputPdfFile;
    }

    // public int print( Graphics graphics ) {
    //
    // Graphics2D graphics2d = (Graphics2D) graphics;
    //
    // AffineTransform at = graphics2d.getTransform();
    // double dpi = at.getScaleX() * 72;
    // // graphics2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
    //
    // Iterator<Box> iter = page.getBoxes().iterator();
    // while( iter.hasNext() ) {
    //
    // Box box = iter.next();
    // graphics2d = (Graphics2D) graphics.create(box.getLocation().x, box.getLocation().y, box
    // .getSize().width, box.getSize().height);
    //
    // box.getBoxPrinter().draw(graphics2d, monitor);
    //
    // }
    //
    // return Printable.PAGE_EXISTS;
    // }
    /**
     * @param monitor
     */
    public void setMonitor( IProgressMonitor monitor ) {
        this.monitor = monitor;
    }

    public boolean printToPdf() {

        Dimension paperSize = page.getPaperSize();
        Dimension pageSize = page.getSize();

        float xScale = (float) paperSize.width / (float) pageSize.width;
        float yScale = (float) paperSize.height / (float) pageSize.height;

        Rectangle pageRectangle = new Rectangle(paperSize.width, paperSize.height);
        Document document = new Document(pageRectangle, 0f, 0f, 0f, 0f);

        try {

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputPdfFile));
            document.open();

            PdfContentByte cb = writer.getDirectContent();
            Graphics2D graphics = cb.createGraphics(pageRectangle.width(), pageRectangle.height());

            /*
             * TODO print boxes
             */
            List<Box> boxes = page.getBoxes();
            for( Box box : boxes ) {
                String id = box.getID();
                System.out.println(id);
                Point boxLocation = box.getLocation();
                int x = boxLocation.x;
                int y = boxLocation.y;
                Dimension size = box.getSize();
                int w = size.width;
                int h = size.height;

                // float newX = xScale * (float) x;
                // float newY = yScale * (float) y;
                // float newW = xScale * (float) w;
                // float newH = yScale * (float) h;

                Graphics2D boxGraphics = (Graphics2D) graphics.create(x, y, w, h);
                // Graphics2D boxGraphics = (Graphics2D) graphics.create((int) newX, (int) newY,
                // (int) newW, (int) newH);
                AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
                graphics.transform(at);

                BoxPrinter boxPrinter = box.getBoxPrinter();
                boxPrinter.draw(boxGraphics, monitor);

            }

            graphics.dispose();
            document.newPage();
            document.close();
            writer.close();
        } catch (DocumentException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
