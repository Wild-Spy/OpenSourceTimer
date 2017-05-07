package customwidgets;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcochrane on 22/11/16.
 */
public class SubGraph {
    private List<Double> scores;
    private List<Long> times;
    private String channelName;
    private GraphPanel parentGraph;
    private int plotIndex;
    private String graphLabel;
    private List<GraphIntervalMarker> intervalMarkers = new ArrayList<>();
    private List<Block> blocks = new ArrayList<>();
    private long cursorLocation;
    private boolean showCursor = false;
    private Color cursorColor;

    SubGraph(GraphPanel parentGraph, String channelName, int chanNumber, List<Double> scores, List<Long> times) {
        this(parentGraph, chanNumber,
                "Channel " + String.valueOf(chanNumber + 1),
                scores, times);
        this.channelName = channelName;
    }

    SubGraph(GraphPanel parentGraph, int plotIndex, String label, List<Double> scores, List<Long> times) {
        this.scores = scores;
        this.times = times;
        this.parentGraph = parentGraph;
        this.channelName = "";
        this.plotIndex = plotIndex;
        this.graphLabel = label;
        intervalMarkers.add(new GraphIntervalMarker(this, new Interval(
                new DateTime(2017, 1, 1, 0, 0), new DateTime(2017, 3, 1, 0, 0)
        )));
    }

    public void updateData(List<Double> scores, List<Long> times) {
        this.scores = scores;
        this.times = times;
        redrawParent();
    }

    public void changeChannelName(String name) {
        this.channelName = name;
        redrawParent();
    }

    public String getChannelName() {
        return this.channelName;
    }

    public void setPlotIndex(int plotIndex) {
        this.plotIndex = plotIndex;
    }

    public int setPlotIndex() {
        return this.plotIndex;
    }

    GraphPanel getParentGraph() {
        return this.parentGraph;
    }

    private void redrawParent() {
        parentGraph.invalidate();
        parentGraph.repaint();
    }

    public Long[] getFullWindow() {
        return new Long[]{times.get(0), times.get(times.size()-1)};
    }

    int getPlotIndex() {
        return this.plotIndex;
    }

    private class Block {
        long start;
        long end;
        double ratio;
        Point startPoint;
        Point endPoint;

        public Block(long start, long end, double ratio,
                     Point startPoint, Point endPoint) {
            this.start = start;
            this.end = end;
            this.ratio = ratio;
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }

        public long getStart() {
            return this.start;
        }

        public long getEnd() {
            return this.end;
        }

        public Point getStartPoint() {
            return this.startPoint;
        }

        public Point getEndPoint() {
            return this.endPoint;
        }

        public double getRatio() {
            return this.ratio;
        }

        public boolean isInBlock(DateTime time) {
            return (time.getMillis() >= this.start && time.getMillis() <= this.end);
        }

    }

    private class BlockParser {
        private boolean inBlock = false;
        private double blockOnTime = 0;
        private double blockOffTime = 0;
        private DoublePoint blockStart = null;
        private Long blockStartTime;
        private int lastDrawnPointIndex;
        private List<Block> blocks;

        public BlockParser(int lastDrawnPointIndex, List<Block> blocks) {
            this.lastDrawnPointIndex = lastDrawnPointIndex;
            this.blocks = blocks;
        }

        public void startBlock(DoublePoint blockStart, int p1Index) {
            inBlock = true;
            blockOnTime = 0;
            blockOffTime = 0;
            this.blockStart = blockStart;
            this.blockStartTime = times.get(p1Index);
        }

        public boolean isInBlock() {
            return inBlock;
        }

        public boolean pointsInBlock(DoublePoint p1, DoublePoint p2, int p2Index) {
            if (isInBlock()) {
                return !(pointsTooCloseToDraw(p1, p2) || p2Index == lastDrawnPointIndex);
            } else {
                return pointsTooCloseToDraw(p1, p2);
            }
        }

        private boolean pointsTooCloseToDraw(DoublePoint p1, DoublePoint p2) {
            return ((p2.x - p1.x) > 2);
        }

        public void endBlockAndDraw(Graphics2D g2, DoublePoint p1, DoublePoint p2, int p1Index) {
            //Draw the block
            if (blockOnTime + blockOffTime > 0) {
                double ratio = blockOnTime / (blockOnTime + blockOffTime);
                drawBlock(g2, blockStart.toPoint(), p1.toPoint(), ratio);
                blocks.add(new Block(blockStartTime, times.get(p1Index), ratio,
                        blockStart.toPoint(), p1.toPoint()));
            }
            drawSegmentPreStep(g2, p1.toPoint(), p2.toPoint());
            //Finish the block
            inBlock = false;
        }

        public void updateBlockParameters(DoublePoint p1, DoublePoint p2, int p2Index) {
            if (scores.get(p2Index).equals(1.0)) {
                blockOnTime += (p2.x - p1.x);
            } else {
                blockOffTime += (p2.x - p1.x);
            }
        }

        public void drawOrUpdateBlock(Graphics2D g2, DoublePoint p1, DoublePoint p2, int p2Index) {
            if (pointsInBlock(p1, p2, p2Index)) {
                updateBlockParameters(p1, p2, p2Index);
            } else {
                endBlockAndDraw(g2, p1, p2, p2Index-1);
            }
        }

        public void drawLineOrStartBlock(Graphics2D g2, DoublePoint p1, DoublePoint p2, int p2Index) {
            if (pointsInBlock(p1, p2, p2Index)) {
                drawSegmentPreStep(g2, p1.toPoint(), p2.toPoint());
            } else {
                startBlock(p1, p2Index-1);
            }
        }

        public void handleBlockOrDrawSomething(Graphics2D g2, DoublePoint p1, DoublePoint p2, int p2Index) {
            if (isInBlock()) {
                drawOrUpdateBlock(g2, p1, p2, p2Index);
            } else {
                drawLineOrStartBlock(g2, p1, p2, p2Index);
            }
        }
    }

    void drawChannelData(Graphics2D g2) {
        //draw background
        drawChannelBackground(g2);

        drawCursor(g2);

        int startIndex = getFirstVisiblePoint();
        if (startIndex >= scores.size()) return;
        int endIndex = getLastVisiblePoint();
        if (endIndex == 0) return;

        blocks = new ArrayList<>();
        BlockParser blockParser = new BlockParser(endIndex, blocks);

        // draw data points
        Stroke oldStroke = g2.getStroke();
        g2.setColor(GraphPanel.lineColor);
        g2.setStroke(GraphPanel.GRAPH_STROKE);

        DoublePoint p1;
        DoublePoint p2 = getPointPosition(scores.get(startIndex), times.get(startIndex));

        for (int i = startIndex+1; i < endIndex+1; i++) {
            p1 = p2;
            p2 = getPointPosition(scores.get(i), times.get(i));

            blockParser.handleBlockOrDrawSomething(g2, p1, p2, i);
        }

        g2.setStroke(oldStroke);

        for (GraphIntervalMarker im : intervalMarkers) {
            im.drawIfFullyVisible(g2);
        }

    }

    private void drawCursor(Graphics2D g2) {
        int x0 = parentGraph.getXPositionPixel(cursorLocation);
        int y0 = (int)getGraphArea().getMinY() + GraphPanel.channelMeasurementBarHeight;
        int y1 = (int)getGraphArea().getMaxY();

        Color oldColor = g2.getColor();
        g2.setColor(cursorColor);
        g2.drawLine(x0, y0, x0, y1);
        g2.setColor(oldColor);
    }

    private void drawChannelBackground(Graphics2D g2) {
        g2.setColor(GraphPanel.graphBackgroundColor);
        g2.fill(getGraphBackgroundRectangle());
    }

    private void drawBlock(Graphics2D g2, Point startPoint, Point endPoint, double onOffRatio) {
        Color oldColor = g2.getColor();
        g2.setColor(getBlockColor(onOffRatio));
        g2.fill(getBlockRect(startPoint, endPoint));
        g2.setColor(oldColor);
    }

    private Color getBlockColor(double onOffRatio) {
        double min = 100.0/255.0;
        double max = 145.0/255.0;

        float col = (float)(max - (max-min)*onOffRatio);

        return new Color(col, col, col, (float)1.0);
    }

    private Rectangle getGraphBackgroundRectangle() {
        return new Rectangle(GraphPanel.leftPanelWidth,
                GraphPanel.timeBarHeight + GraphPanel.channelHeight * plotIndex + GraphPanel.channelMeasurementBarHeight,
                parentGraph.getWidth() - GraphPanel.leftPanelWidth,
                GraphPanel.channelHeight - GraphPanel.channelMeasurementBarHeight);
    }

    private Rectangle getGraphArea() {
        return new Rectangle(GraphPanel.leftPanelWidth,
                GraphPanel.timeBarHeight + GraphPanel.channelHeight * plotIndex,
                parentGraph.getWidth() - GraphPanel.leftPanelWidth,
                GraphPanel.channelHeight);
    }

    private List<DoublePoint> generateGraphScreenPoints() {
        int startIndex = getFirstVisiblePoint();
        int endIndex = getLastVisiblePoint();

        List<DoublePoint> graphPoints = new ArrayList<>();
        for (int i = startIndex; i < endIndex+1; i++) {
            graphPoints.add(getPointPosition(scores.get(i), times.get(i)));
        }

        return graphPoints;
    }

    private DoublePoint getPointPosition(Double score, long time) {
        long tMin = parentGraph.tWindow[0];
        //long tMax = parentGraph.tWindow[1];

        int x0 = GraphPanel.leftPanelWidth;

        double xScale = parentGraph.getXPixelsPerMs();

        double deltaX1 = (time - tMin) * xScale;

        if (deltaX1 < 0.0) deltaX1 = 0.0;
        return new DoublePoint(x0 + deltaX1, getYValue(score));
    }

    private int getYValue(Double chanScore) {
        int y0 = GraphPanel.timeBarHeight + GraphPanel.channelHeight * plotIndex + GraphPanel.channelMeasurementBarHeight;
        int activeChanHeight = GraphPanel.channelHeight - GraphPanel.channelMeasurementBarHeight;

        if (chanScore.equals(1.0)) {
            return (int) (y0 + activeChanHeight*0.1);
        } else if (chanScore.equals(0.0)) {
            return (int) (y0 + activeChanHeight*0.9);
        }
        return y0;
    }

    private Rectangle getBlockRect(Point start, Point end) {
        int x0 = start.x;
        int y0 = getYValue(1.0);
        int width = end.x - start.x;
        int height = getYValue(0.0) - y0;

        return new Rectangle(x0, y0, width, height+1);
    }

    //Actually we're getting the point before the first visible because it
    //may be attached to a line that is visible
    private int getFirstVisiblePoint() {
        for (int i = 0; i < scores.size(); i++) {
            if (times.get(i) >= parentGraph.tWindow[0]) {
                return Math.max(i-1, 0);
            }
        }
        return scores.size();
    }

    //Actually we're getting the point after the last visible because it
    //may be attached to a line that is visible
    private int getLastVisiblePoint() {
        for (int i = scores.size()-1; i >= 0; i--) {
            if (times.get(i) <= parentGraph.tWindow[1]) {
                return Math.min(i+1, scores.size()-1);
            }
        }
        return 0;
    }

    private void drawSegmentStraightLine(Graphics2D g2, Point p1, Point p2) {
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private void drawSegmentPreStep(Graphics2D g2, Point p1, Point p2) {
        if (p2.x > this.parentGraph.getWidth()) p2.x = this.parentGraph.getWidth();
        g2.drawLine(p1.x, p1.y, p2.x, p1.y);
        g2.drawLine(p2.x, p1.y, p2.x, p2.y);
    }

    private void drawSegmentPostStep(Graphics2D g2, Point p1, Point p2) {
        g2.drawLine(p1.x, p1.y, p1.x, p2.y);
        g2.drawLine(p1.x, p2.y, p2.x, p2.y);
    }

    void drawLeftPanelChannelBox(Graphics2D g2) {
        Color gradientStart = new Color(75, 75, 75);
        Color gradientEnd = new Color(60, 60, 60);

        Rectangle chanBox = new Rectangle(0, GraphPanel.timeBarHeight + GraphPanel.channelHeight * plotIndex, GraphPanel.leftPanelWidth, GraphPanel.channelHeight);

        GradientPaint gp = new GradientPaint(new Point(0, chanBox.y), gradientStart,
                new Point(0, (int)chanBox.getMaxY()), gradientEnd);

        g2.setPaint(gp);
        g2.fill(chanBox);
        g2.setColor(Color.BLACK);
        g2.draw(chanBox);

        g2.setColor(new Color(145, 145, 145));
        FontMetrics metrics = g2.getFontMetrics();
        //int labelWidth = metrics.stringWidth(channelName);
        //g2.drawString(channelName, x0 - labelWidth / 2, y0 + (metrics.getHeight() + 3)*2);
        g2.drawString(graphLabel, 10, chanBox.y + metrics.getHeight() + 3);

        //draw on/off markers
        int yOn = getYValue(1.0);
        int yOff = getYValue(0.0);
        int x0 = chanBox.width-1;
        int x1 = chanBox.width - 4;
        g2.drawLine(x0, yOn, x1, yOn);
        g2.drawLine(x0, yOff, x1, yOff);

        Font f = g2.getFont();
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        metrics = g2.getFontMetrics();
        int onWidth = metrics.stringWidth("On");
        int offWidth = metrics.stringWidth("Off");
        g2.drawString("On", x1 - onWidth - 2, yOn + metrics.getHeight()/4);
        g2.drawString("Off", x1 - offWidth - 2, yOff + metrics.getHeight()/4);


        g2.setFont(f);
    }

    private DateTime getLastTransition(DateTime time) {
        for (Block b : blocks) {
            if (b.isInBlock(time)) {
                DateTime ret = new DateTime(b.getStart());
                if (ret.isEqual(parentGraph.getWindowStart()) ||
                        ret.isBefore(parentGraph.getWindowStart())) return null;
                return ret;
            }
        }

        for (int i = 1; i < times.size(); i++) {
            if (time.getMillis() < times.get(i)) return new DateTime(times.get(i-1));
        }
        return null;
    }

    private DateTime getNextTransition(DateTime time) {
        for (Block b : blocks) {
            if (b.isInBlock(time)) {
                DateTime ret = new DateTime(b.getEnd());
                if (b.getEndPoint().x >= parentGraph.getWidth()-1) return null;
                return ret;
            }
        }

        for (int i = 1; i < times.size(); i++) {
            if (time.getMillis() < times.get(i)) return new DateTime(times.get(i));
        }
        return null;
    }

    private Interval getTransitionInterval(DateTime time) {
        try {
            return new Interval(getLastTransition(time),
                    getNextTransition(time));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    void parentMouseMoved(MouseEvent mouseEvent) {
        updateCursor(mouseEvent);

        if (isMouseInBounds(mouseEvent)) {
            mouseMoved(parentGraph.getXPosition(mouseEvent.getX()));
        } else {
            intervalMarkers.get(0).setVisible(false);
        }
    }

    private void updateCursor(MouseEvent mouseEvent) {
        if (!isMouseInXBounds(mouseEvent)) {
            this.showCursor = false;
            return;
        }

        DateTime mouseTime = parentGraph.getXPosition(mouseEvent.getX());
        updateCursor(mouseTime, isMouseInYBounds(mouseEvent));
    }

    private void updateCursor(DateTime mouseTime, boolean mouseInThisGraph) {
        this.showCursor = true;
        this.cursorLocation = mouseTime.getMillis();
        if (mouseInThisGraph) {
            this.cursorColor = new Color(145, 145, 145);
        } else {
            this.cursorColor = new Color(41, 41, 41);
        }
    }

    private boolean isMouseInBounds(MouseEvent mouseEvent) {
        return getGraphArea().contains(mouseEvent.getPoint());
    }

    private boolean isMouseInXBounds(MouseEvent mouseEvent) {
        return (mouseEvent.getPoint().getX() >= getGraphArea().getMinX()) &&
                (mouseEvent.getPoint().getX() <= getGraphArea().getMaxX());
    }

    private boolean isMouseInYBounds(MouseEvent mouseEvent) {
        return (mouseEvent.getPoint().getY() >= getGraphArea().getMinY()) &&
                (mouseEvent.getPoint().getY() <= getGraphArea().getMaxY());
    }

    private void mouseMoved(DateTime mouseTime) {
        intervalMarkers.get(0).setVisible(true);
        intervalMarkers.get(0).setInterval(getTransitionInterval(mouseTime));
        //parentGraph.getMarker(0).setTime(mouseTime);
    }
}