package com.github.myway.render;

import java.io.IOException;

import com.github.myway.core.Earth;
import com.github.myway.osm.MapData;
import com.github.myway.osm.Node;
import com.github.myway.osm.NodeRef;
import com.github.myway.osm.Way;
import com.github.myway.voice.gpx.Point;
import com.github.myway.voice.gpx.Segment;
import com.github.myway.voice.gpx.Track;

public class MapToImage implements AutoCloseable {
	private static final int MAX_SIDE_LEN = 1800;

	private static MywayColor getWayColor(Way way) {
		if (isTrack(way))
			return MywayColor.BLACK;
		return MywayColor.GREEN;
	}

	private static boolean isTrack(Way way) {
		return way.getTags().get("highway") != null;
	}

	private Drawable drawable;
	private MapData map;
	private int h = 100;
	private int w = 100;

	private double dw = 1d;

	private double dh = 1d;

	public MapToImage(DrawableFactory factory, MapData map) {
		this.map = map;
		if (map != null) {
			double dlat = Earth.distance(map.getBounds().getLatitudeMin(), map.getBounds().getLongitudeMin(),
					map.getBounds().getLatitudeMax(), map.getBounds().getLongitudeMin());
			double dlon = Earth.distance(map.getBounds().getLatitudeMin(), map.getBounds().getLongitudeMin(),
					map.getBounds().getLatitudeMin(), map.getBounds().getLongitudeMax());

			double dmax = Math.max(dlat, dlon);
			h = (int) Math.ceil(MAX_SIDE_LEN * dlat / dmax);
			w = (int) Math.ceil(MAX_SIDE_LEN * dlon / dmax);

			dw = (map.getBounds().getLongitudeMax() - map.getBounds().getLongitudeMin()) / w;
			dh = (map.getBounds().getLatitudeMax() - map.getBounds().getLatitudeMin()) / h;
		}
		drawable = factory.create(w, h);
	}

	@Override
	public void close() {
		drawable.close();
	}

	private void drawLine(double longitudeA, double latitudeA, double longitudeB, double latitudeB) {
		int x1 = toX(longitudeA);
		int y1 = toY(latitudeA);
		int x2 = toX(longitudeB);
		int y2 = toY(latitudeB);
		drawable.drawLine(x1, y1, x2, y2);
		System.out.println("x1 " + x1 + ", y1 " + y1 + ", x2 " + x2 + ", y2 " + y2);
	}

	private void drawLine(Node nodeA, Node nodeB) {
		double longitudeA = nodeA.getLongitude();
		double latitudeA = nodeA.getLatitude();
		double longitudeB = nodeB.getLongitude();
		double latitudeB = nodeB.getLatitude();
		drawLine(longitudeA, latitudeA, longitudeB, latitudeB);
	}

	public void drawMap() throws IOException {
		drawable.setColor(MywayColor.WHITE);
		drawable.fillRect(0, 0, w, h);
		if (map == null) {
			return;
		}
		for (Way way : map.getWays()) {
			if (!isTrack(way))
				drawWay(way);
		}
		for (Way way : map.getWays()) {
			if (isTrack(way))
				drawWay(way);
		}
		drawable.setColor(MywayColor.CYAN);
		drawLine(map.getBounds().getLongitudeMin(), map.getBounds().getLatitudeMin(), map.getBounds().getLongitudeMax(),
				map.getBounds().getLatitudeMax());
	}

	private void drawSegment(Segment segment) {
		for (int i = 0; i < segment.getPoints().size() - 1; i++) {
			Point a = segment.getPoints().get(i);
			Point b = segment.getPoints().get(i + 1);
			drawable.setColor(MywayColor.RED);
			drawLine(a.getLongitude(), a.getLatitude(), b.getLongitude(), b.getLatitude());
		}
	}

	private void drawString(String name, Node nodeA) {
		double longitudeA = nodeA.getLongitude();
		double latitudeA = nodeA.getLatitude();
		int x1 = toX(longitudeA);
		int y1 = toY(latitudeA);
		drawable.drawString(name, x1, y1);
	}

	public void drawTrack(Track track) {
		if (track == null) {
			return;
		}
		for (Segment segment : track.getSegments()) {
			drawSegment(segment);
		}
	}

	private void drawWay(Way way) {
		MywayColor c = getWayColor(way);
		String name = way.getTags().get("name");
		if (name != null) {
			drawable.setColor(MywayColor.BLUE);
			drawString(name, way.center());
		}
		drawable.setColor(c);
		for (int i = 0; i < way.getNodes().size() - 1; i++) {
			NodeRef a = way.getNodes().get(i);
			NodeRef b = way.getNodes().get(i + 1);
			Node nodeA = a.getNode();
			Node nodeB = b.getNode();
			if (nodeA != null && nodeB != null) {
				drawLine(nodeA, nodeB);
			}
		}
	}

	private int toX(double longitude) {
		return map == null ? 0 : (int) ((longitude - map.getBounds().getLongitudeMin()) / dw);
	}

	private int toY(double latitude) {
		return map == null ? 0 : h - (int) ((latitude - map.getBounds().getLatitudeMin()) / dh);
	}

	public Drawable getDrawable() {
		return drawable;
	}
}
