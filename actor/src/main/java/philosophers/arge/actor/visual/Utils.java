package philosophers.arge.actor.visual;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public final class Utils {
	private Utils() {
	}

	private static List<Color> colorList;
	private static Random rnd = new Random();
	static {
		colorList = new ArrayList<>();
		colorList.add(Color.BLACK);
		colorList.add(Color.BLUE);
		colorList.add(Color.CYAN);
		colorList.add(Color.GRAY);
		colorList.add(Color.GREEN);
		colorList.add(Color.MAGENTA);
		colorList.add(Color.ORANGE);
		colorList.add(Color.PINK);
		colorList.add(Color.RED);
	}

	public static final XYSeries toXYDataSet(String name, Map<Double, Double> values) {
		XYSeries s1 = new XYSeries(name);
		Iterator<Double> iterator = values.keySet().iterator();
		while (iterator.hasNext()) {
			Double key = iterator.next();
			s1.add(key, values.get(key));
		}
		return s1;
	}

	public static final XYSeriesCollection convertXYSeriesListToCollection(XYSeries... dataSet) {
		XYSeriesCollection dataCollection = new XYSeriesCollection();
		for (int i = 0; i < dataSet.length; i++) {
			dataCollection.addSeries(dataSet[i]);
		}
		return dataCollection;
	}

	public static final Color getRandomColor() {
		return colorList.get(rnd.nextInt(colorList.size()));
	}

}
