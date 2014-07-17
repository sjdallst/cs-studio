/**
 * Copyright (C) 2010-14 pvmanager developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.csstudio.diag.pvmanager.fxprobe;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import org.epics.graphene.Cell2DDataset;
import org.epics.graphene.GraphBuffer;
import org.epics.graphene.IntensityGraph2DRenderer;
import org.epics.graphene.InterpolationScheme;
import org.epics.graphene.LineGraph2DRenderer;
import org.epics.graphene.LineGraph2DRendererUpdate;
import org.epics.graphene.Point2DDataset;
import org.epics.graphene.Point2DDatasets;
import static org.epics.pvmanager.formula.ExpressionLanguage.formula;
import org.epics.pvmanager.graphene.DatasetConversions;
import static org.epics.pvmanager.graphene.ExpressionLanguage.*;
import org.epics.pvmanager.graphene.LineGraph2DExpression;
import org.epics.pvmanager.graphene.ScatterGraph2DExpression;
import org.epics.vtype.*;
import org.epics.graphene.IntensityGraph2DRenderer;
import org.epics.graphene.IntensityGraph2DRendererUpdate;
import org.epics.graphene.NumberColorMap;
import org.epics.graphene.NumberColorMaps;
import static org.epics.pvmanager.formula.ExpressionLanguage.formula;
import static org.epics.pvmanager.graphene.ExpressionLanguage.*;
import org.epics.pvmanager.graphene.Graph2DExpression;
import org.epics.pvmanager.graphene.IntensityGraph2DExpression;
import org.epics.vtype.VType;

/**
 *
 * @author carcassi, sjdallst
 */
public class IntensityGraphApp extends BaseGraphApp<LineGraph2DRendererUpdate> {
    
    private IntensityGraph2DRenderer renderer = new IntensityGraph2DRenderer(imagePanel.getWidth(), imagePanel.getHeight());
    
    private NumberColorMap colorMap = IntensityGraph2DRenderer.DEFAULT_COLOR_MAP;
    private boolean drawLegend = IntensityGraph2DRenderer.DEFAULT_DRAW_LEGEND;

    public IntensityGraphApp() {
        imagePanel.setImage(new BufferedImage(imagePanel.getWidth(), imagePanel.getHeight(), BufferedImage.TYPE_INT_ARGB));
    }
    
    public byte[] render(VType data, int width, int height){
        return this.render((VNumberArray)data, width, height);
    }
    
    public byte[] render(VNumberArray array, int width, int height) {
        
        GraphBuffer graphBuffer = new GraphBuffer(width, height);
        Cell2DDataset data = DatasetConversions.cell2DDatasetsFromVNumberArray(array);
        renderer.update(renderer.newUpdate().imageHeight(height).imageWidth(width));
        renderer.draw(graphBuffer, data);
        byte[] pixels = ((DataBufferByte)graphBuffer.getImage().getRaster().getDataBuffer()).getData();
        return pixels;
        
    }
    
    public NumberColorMap getColorMap() {
        return colorMap;
    }

    public void setColorMap(NumberColorMap colorMap) {
        this.colorMap = colorMap;
        updateGraph();
    }

    public boolean isDrawLegend() {
        return drawLegend;
    }

    public void setDrawLegend(boolean drawLegend) {
        this.drawLegend = drawLegend;
        updateGraph();
    }
    
    protected void updateGraph() {
        if (renderer != null) {
            update(renderer);
        }
    }
    
    protected void update(IntensityGraph2DRenderer renderer) {
        renderer.update(renderer.newUpdate().colorMap(colorMap).drawLegend(drawLegend));
    }
    
}
