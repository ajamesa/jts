/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.ui.style;

import java.awt.Color;
import java.awt.Graphics2D;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.buffer.OffsetCurveBuilder;
import org.locationtech.jtstest.testbuilder.model.DisplayParameters;
import org.locationtech.jtstest.testbuilder.ui.ColorUtil;
import org.locationtech.jtstest.testbuilder.ui.Viewport;

public class LayerStyle implements Style  {

  private BasicStyle geomStyle;
  private StyleList decoratorStyle;
  private VertexStyle vertexStyle;
  private DataLabelStyle labelStyle;

  private StyleList.StyleFilter vertexFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! DisplayParameters.isShowingVertices();
    }
  };
  
  private StyleList.StyleFilter decorationFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! DisplayParameters.isShowingOrientation();
    }
  };
    
  private StyleList.StyleFilter structureFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! DisplayParameters.isShowingStructure();
    }
  };
    
  private StyleList.StyleFilter labelFilter = new StyleList.StyleFilter() {
    public boolean isFiltered(Style style) {
      return ! DisplayParameters.isShowingLabel();
    }
  };
  private StyleGroup orientStyle;
  private StyleGroup structureStyle;
  private ArrowSegmentStyle segArrowStyle;
  private ArrowLineEndStyle lineArrowStyle;
  private CircleLineEndStyle lineCircleStyle;
  private VertexLabelStyle vertexLabelStyle;
  private boolean isOffsetLine;

  public LayerStyle(BasicStyle geomStyle) {
    this.geomStyle = geomStyle;
    initDecorators(geomStyle);
  }
  
  public LayerStyle(BasicStyle geomStyle, StyleList decoratorStyle) {
    this.geomStyle = geomStyle;
    this.decoratorStyle = decoratorStyle;
  }
  
  public LayerStyle(LayerStyle layerStyle) {
    this.geomStyle = layerStyle.geomStyle.copy();
    initDecorators(geomStyle);
  }

  public BasicStyle getGeomStyle() {
    return geomStyle;
  }

  public StyleList getDecoratorStyle() {
    return decoratorStyle;
  }

  private void initDecorators(BasicStyle style)
  {
    vertexStyle = new VertexStyle(style.getLineColor());
    vertexLabelStyle = new VertexLabelStyle(style.getLineColor());
    labelStyle = new DataLabelStyle(ColorUtil.opaque(style.getLineColor().darker()));

    segArrowStyle = new ArrowSegmentStyle(ColorUtil.lighter(style.getLineColor(), 0.8));
    lineArrowStyle = new ArrowLineEndStyle(ColorUtil.lighter(style.getLineColor(),0.5), false, true);
    lineCircleStyle = new CircleLineEndStyle(ColorUtil.lighter(style.getLineColor(),0.5), 6, true, true);
    orientStyle = new StyleGroup(segArrowStyle, lineArrowStyle, lineCircleStyle);

    PolygonStructureStyle polyStyle = new PolygonStructureStyle(ColorUtil.opaque(style.getLineColor()));
    SegmentIndexStyle indexStyle = new SegmentIndexStyle(ColorUtil.opaque(style.getLineColor().darker()));
    structureStyle = new StyleGroup(polyStyle, indexStyle);
    
    // order is important here
    StyleList styleList = new StyleList();
    styleList.add(vertexLabelStyle, vertexFilter);
    styleList.add(vertexStyle, vertexFilter);
    styleList.add(orientStyle);
    styleList.add(structureStyle);
    styleList.add(labelStyle, labelFilter);
    styleList.setEnabled(labelStyle, false);
    styleList.setEnabled(orientStyle, false);
    styleList.setEnabled(structureStyle, false);
    styleList.setEnabled(vertexLabelStyle, false);
    
    decoratorStyle = styleList;
  }

  public void setColor(Color color) {
    segArrowStyle.setColor( ColorUtil.lighter(color,0.8) );
    lineArrowStyle.setColor( ColorUtil.lighter(color,0.5) );
    lineCircleStyle.setColor( ColorUtil.lighter(color,0.5) );
    
  }
  
  public void setVertices(boolean show) {
    decoratorStyle.setEnabled(vertexStyle, show);
  }
  
  public boolean isVertices() {
    return decoratorStyle.isEnabled(vertexStyle);
  }
  
  public int getVertexSize() {
    return vertexStyle.getSize();
  }
  public void setVertexSize(int size) {
    vertexStyle.setSize(size);
  }
  
  public Color getVertexColor() {
    return vertexStyle.getColor();
  }
  public void setVertexColor(Color color) {
    vertexStyle.setColor(color);
    vertexLabelStyle.setColor(color);
  }
  public void setVertexLabels(boolean show) {
    decoratorStyle.setEnabled(vertexLabelStyle, show);
  }
  public boolean isVertexLabels() {
    return decoratorStyle.isEnabled(vertexLabelStyle);
  }

  public void setLabel(boolean show) {
    decoratorStyle.setEnabled(labelStyle, show);
  }
  
  public boolean isLabel() {
    return decoratorStyle.isEnabled(labelStyle);
  }
  
  public int getLabelSize() {
    return labelStyle.getSize();
  }
  public void setLabelSize(int size) {
    labelStyle.setSize(size);
  }
  
  public Color getLabelColor() {
    return labelStyle.getColor();
  }
  public void setLabelColor(Color color) {
    labelStyle.setColor(color);
  }
  
  public void paint(Geometry geom, Viewport viewport, Graphics2D g) throws Exception {
    Geometry transformGeom = geom;
    if (isOffsetLine && geom instanceof LineString) {
      transformGeom = offsetCurve(geom, 10);
    }
    geomStyle.paint(transformGeom, viewport, g);
    decoratorStyle.paint(transformGeom, viewport, g);
  }

  public void setOffset(boolean show) {
    isOffsetLine = show;
    //decoratorStyle.setEnabled(offsetLineStyle, show);
  }
  
  public boolean isOffset() {
    return isOffsetLine;
    //return decoratorStyle.isEnabled(offsetLineStyle);
  }
  
  public void setOrientations(boolean show) {
    decoratorStyle.setEnabled(orientStyle, show);
  }
  
  public boolean isOrientations() {
    return decoratorStyle.isEnabled(orientStyle);
  }
  
  public void setStructure(boolean show) {
    decoratorStyle.setEnabled(structureStyle, show);
  }
  
  public boolean isStructure() {
    return decoratorStyle.isEnabled(structureStyle);
  }
  
  static Geometry offsetCurve(Geometry geom, double distance)
  {
    BufferParameters bufParams = new BufferParameters();
    OffsetCurveBuilder ocb = new OffsetCurveBuilder(
        geom.getFactory().getPrecisionModel(), bufParams
        );
    Coordinate[] pts = ocb.getOffsetCurve(geom.getCoordinates(), distance);
    Geometry curve = geom.getFactory().createLineString(pts);
    return curve;
  }



}
