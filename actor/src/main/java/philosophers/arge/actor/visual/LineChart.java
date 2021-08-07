package philosophers.arge.actor.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;

import philosophers.arge.actor.ActorPriority;

/**
 * Used for drawing performance metrics.
 * 
 * @author osmanyasal
 *
 */
@SuppressWarnings("serial")
public class LineChart extends JFrame {

	private static final String PERFORMANCE_METRICS = "Performance Metrics";

	public LineChart(String xName, String yName, XYSeries... dataSet) {
		super(PERFORMANCE_METRICS);
		JFreeChart chart = createChart(xName, yName, dataSet.length, dataSet);
		ChartPanel panel = new ChartPanel(chart);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		System.out.println(size);
		panel.setPreferredSize(
				new Dimension((int) Math.ceil(size.getWidth() / 2), (int) Math.ceil(size.getHeight() / 2)));
		setContentPane(panel);

	}

	public final LineChart draw() {
		this.pack();
		this.setVisible(true);
		return this;
	}

	private JFreeChart createChart(String xName, String yName, int dataSetLength, XYSeries... dataset) {
		JFreeChart chart = ChartFactory.createXYLineChart("", xName, yName,
				Utils.convertXYSeriesListToCollection(dataset), PlotOrientation.VERTICAL, true, true, false);
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