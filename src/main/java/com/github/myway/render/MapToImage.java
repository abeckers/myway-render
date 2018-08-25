package com.github.myway.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

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

	private static Color getWayColor(Way way) {
		if (isTrack(way))
			return Color.BLACK;
		return Color.GREEN;
	}

	private static boolean isTrack(Way way) {
		return way.getTags().get("highway") != null;
	}

	private MapData map;
	private BufferedImage img;
	private Graphics g;
	private int h;
	private int w;

	private double dw;

	private double dh;

	public MapToImage(MapData map) {
		this.map = map;
		double dlat = Earth.distance(map.getBounds().getLatitudeMin(), map.getBounds().getLongitudeMin(),
				map.getBounds().getLatitudeMax(), map.getBounds().getLongitudeMin());
		double dlon = Earth.distance(map.getBounds().getLatitudeMin(), map.getBounds().getLongitudeMin(),
				map.getBounds().getLatitudeMin(), map.getBounds().getLongitudeMax());

		double dmax = Math.max(dlat, dlon);
		h = (int) (MAX_SIDE_LEN * dlat / dmax);
		w = (int) (MAX_SIDE_LEN * dlon / dmax);

		dw = (map.getBounds().getLongitudeMax() - map.getBounds().getLongitudeMin()) / w;
		dh = (map.getBounds().getLatitudeMax() - map.getBounds().getLatitudeMin()) / h;

		img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		g = img.getGraphics();
	}

	@Override
	public void close() {
		g.dispose();
	}

	private void drawLine(double longitudeA, double latitudeA, double longitudeB, double latitudeB) {
		int x1 = toX(longitudeA);
		int y1 = toY(latitudeA);
		int x2 = toX(longitudeB);
		int y2 = toY(latitudeB);
		g.drawLine(x1, y1, x2, y2);
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
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);
		for (Way way : map.getWays()) {
			if (!isTrack(way))
				drawWay(way);
		}
		for (Way way : map.getWays()) {
			if (isTrack(way))
				drawWay(way);
		}
		g.setColor(Color.CYAN);
		drawLine(map.getBounds().getLongitudeMin(), map.getBounds().getLatitudeMin(), map.getBounds().getLongitudeMax(),
				map.getBounds().getLatitudeMax());
	}

	private void drawSegment(Segment segment) {
		for (int i = 0; i < segment.getPoints().size() - 1; i++) {
			Point a = segment.getPoints().get(i);
			Point b = segment.getPoints().get(i + 1);
			g.setColor(Color.RED);
			drawLine(a.getLongitude(), a.getLatitude(), b.getLongitude(), b.getLatitude());
		}
	}

	private void drawString(String name, Node nodeA) {
		double longitudeA = nodeA.getLongitude();
		double latitudeA = nodeA.getLatitude();
		int x1 = toX(longitudeA);
		int y1 = toY(latitudeA);
		g.drawString(name, x1, y1);
	}

	public void drawTrack(Track track) {
		for (Segment segment : track.getSegments()) {
			drawSegment(segment);
		}
	}

	private void drawWay(Way way) {
		Color c = getWayColor(way);
		String name = way.getTags().get("name");
		if (name != null) {
			g.setColor(Color.BLUE);
			drawString(name, way.center());
		}
		g.setColor(c);
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

	public void saveImage() throws IOException {
		ImageIO.write(img, "jpg", new File("/tmp/map.jpg"));
	}

	private int toX(double longitude) {
		return (int) ((longitude - map.getBounds().getLongitudeMin()) / dw);
	}

	private int toY(double latitude) {
		return h - (int) ((latitude - map.getBounds().getLatitudeMin()) / dh);
	}
}
