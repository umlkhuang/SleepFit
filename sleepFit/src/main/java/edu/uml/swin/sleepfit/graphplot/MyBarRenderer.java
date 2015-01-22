package edu.uml.swin.sleepfit.graphplot;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.androidplot.exception.PlotRenderException;
import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

public class MyBarRenderer extends BarRenderer<MyBarFormatter> {
	private BarWidthStyle style = BarWidthStyle.FIXED_WIDTH;
    private float barWidth = 5;
    
    private final TreeMap<Number, XYSeries> tempSeriesMap = new TreeMap<Number, XYSeries>();
    
    public MyBarRenderer(XYPlot plot) {
    	super(plot);
    }
    
    public void setBarWidth(float barWidth) {
    	this.barWidth = barWidth;
    }

    @Override
    public void onRender(Canvas canvas, RectF plotArea) throws PlotRenderException {
        int longest = getLongestSeries();
        if(longest == 0) {
            return;  // no data, nothing to do.
        }
        tempSeriesMap.clear();

        for(int i = 0; i < longest; i++) {
            tempSeriesMap.clear();
            List<XYSeries> seriesList = getPlot().getSeriesListForRenderer(this.getClass());
            for(XYSeries series : seriesList) {
                if(i < series.size()) {
                    tempSeriesMap.put(series.getY(i), series);
                }
            }
            drawBars(canvas, plotArea, tempSeriesMap, i);
        }
    }
    
    @Override
    public void doDrawLegendIcon(Canvas canvas, RectF rect, BarFormatter formatter) {
        canvas.drawRect(rect, formatter.getFillPaint());
        canvas.drawRect(rect, formatter.getBorderPaint());
    }

    private int getLongestSeries() {
        int longest = 0;
        List<XYSeries> seriesList = getPlot().getSeriesListForRenderer(this.getClass());

        if(seriesList == null)
            return 0;

        for(XYSeries series :seriesList) {
            int seriesSize = series.size();
            if(seriesSize > longest) {
                longest = seriesSize;
            }
        }
        return longest;
    }

    private void drawBars(Canvas canvas, RectF plotArea, TreeMap<Number, XYSeries> seriesMap, int x) {
        Object[] oa = seriesMap.entrySet().toArray();
        Map.Entry<Number, XYSeries> entry;
        Number yVal = null;
        Number xVal = null;

        float halfWidth = barWidth * 0.5f;

        for(int i = oa.length-1; i >= 0; i--) {
            entry = (Map.Entry<Number, XYSeries>) oa[i];
            XYSeries tempEntry = entry.getValue();

            if(tempEntry != null) {
                yVal = tempEntry.getY(x);
                xVal = tempEntry.getX(x);

                if (yVal != null && xVal != null) {  // make sure there's a real value to draw
                    switch (style) {
                        case FIXED_WIDTH:
                            float pixX = ValPixConverter.valToPix(xVal.doubleValue(), getPlot().getCalculatedMinX().doubleValue(), getPlot().getCalculatedMaxX().doubleValue(), plotArea.width(), false) + plotArea.left;

                            float left = pixX - halfWidth;
                            float right = pixX + halfWidth;

                            boolean offScreen = left > plotArea.right || right < plotArea.left;
                            if(!offScreen){
                                float pixY = ValPixConverter.valToPix(yVal.doubleValue(), getPlot().getCalculatedMinY().doubleValue(), getPlot().getCalculatedMaxY().doubleValue(), plotArea.height(), true) + plotArea.top;

                                BarFormatter formatter = getFormatter(tempEntry);
                                if(Math.abs (left - right) > 1f) {//Don't draw as it will be hidden anyway.
                                    canvas.drawRect(left, pixY, right, plotArea.bottom, formatter.getFillPaint());
                                }
                                canvas.drawRect(left, pixY, right, plotArea.bottom, formatter.getBorderPaint());
                            }
                            break;
                        default:
                            throw new UnsupportedOperationException("Not yet implemented.");
                    }
                }
            }
        }
    }
}
