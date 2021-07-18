package philosophers.arge.actor.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;

public class LineChart extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1429370178194424829L;
	private static final String PERFORMANCE_METRICS = "Performance Metrics";

	public LineChart(XYSeries... dataSet) {
		super(PERFORMANCE_METRICS);
		JFreeChart chart = createChart(dataSet.length, dataSet);
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(1000, 600));
		setContentPane(panel);

	}

	public final LineChart draw() {
		this.pack();
		this.setVisible(true);
		return this;
	}

	private JFreeChart createChart(int dataSetLength, XYSeries... dataset) {

		JFreeChart chart = ChartFactory.createXYLineChart("", "X", "Y", Utils.convertXYSeriesListToCollection(dataset),
				PlotOrientation.VERTICAL, true, true, false);
		return basicChartCustomization(chart, dataSetLength);
	}

	private JFreeChart basicChartCustomization(JFreeChart chart, int dataSetLength) {
		XYPlot plot = chart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

		for (int i = 0; i < dataSetLength; i++) {
			renderer.setSeriesPaint(i, Utils.getRandomColor());
			renderer.setSeriesStroke(i, new BasicStroke(3f));
		}
		// sets paint color for plot outlines
		plot.setOutlinePaint(Color.BLACK);
		plot.setOutlineStroke(new BasicStroke(2.0f));

		// sets renderer for lines
		plot.setRenderer(renderer);

		// sets plot background
		plot.setBackgroundPaint(Color.WHITE);

		// sets paint color for the grid lines
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.BLACK);

		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.BLACK);

		return chart;
	}

}