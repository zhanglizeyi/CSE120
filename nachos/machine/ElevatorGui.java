// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

import java.awt.Frame;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.Canvas;

import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Insets;

import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;

/**
 * A graphical visualization for the <tt>ElevatorBank</tt> class.
 */
public final class ElevatorGui extends Frame {
	private final static int w = 90, h = 75;

	private int numFloors, numElevators;

	private ElevatorShaft[] elevators;

	private Floor[] floors;

	private int totalWidth, totalHeight;

	ElevatorGui(int numFloors, int numElevators, int[] numRidersPerFloor) {
		this.numFloors = numFloors;
		this.numElevators = numElevators;

		totalWidth = w * (numElevators + 1);
		totalHeight = h * numFloors;

		setTitle("Elevator Bank");

		Panel floorPanel = new Panel(new GridLayout(numFloors, 1, 0, 0));

		floors = new Floor[numFloors];
		for (int i = numFloors - 1; i >= 0; i--) {
			floors[i] = new Floor(i, numRidersPerFloor[i]);
			floorPanel.add(floors[i]);
		}

		Panel panel = new Panel(new GridLayout(1, numElevators + 1, 0, 0));

		panel.add(floorPanel);

		elevators = new ElevatorShaft[numElevators];
		for (int i = 0; i < numElevators; i++) {
			elevators[i] = new ElevatorShaft(i);
			panel.add(elevators[i]);
		}

		add(panel);
		pack();

		setVisible(true);

		repaint();
	}

	void openDoors(int elevator) {
		elevators[elevator].openDoors();
	}

	void closeDoors(int elevator) {
		elevators[elevator].closeDoors();
	}

	void setDirectionDisplay(int elevator, int direction) {
		elevators[elevator].setDirectionDisplay(direction);
	}

	void pressUpButton(int floor) {
		floors[floor].pressUpButton();
	}

	void clearUpButton(int floor) {
		floors[floor].clearUpButton();
	}

	void pressDownButton(int floor) {
		floors[floor].pressDownButton();
	}

	void clearDownButton(int floor) {
		floors[floor].clearDownButton();
	}

	void enterElevator(int floor, int elevator) {
		floors[floor].removeRider();
		elevators[elevator].addRider();
	}

	void pressFloorButton(int floor, int elevator) {
		elevators[elevator].pressFloorButton(floor);
	}

	void exitElevator(int floor, int elevator) {
		elevators[elevator].removeRider();
		floors[floor].addRider();
	}

	void elevatorMoved(int floor, int elevator) {
		elevators[elevator].elevatorMoved(floor);
	}

	private void paintRider(Graphics g, int x, int y, int r) {
		g.setColor(Color.yellow);

		g.fillOval(x - r, y - r, 2 * r, 2 * r);

		g.setColor(Color.black);

		g.fillOval(x - r / 2, y - r / 2, r / 3, r / 3);
		g.fillOval(x + r / 4, y - r / 2, r / 3, r / 3);

		g.drawArc(x - r / 2, y - r / 2, r, r, 210, 120);
	}

	private void paintRiders(Graphics g, int x, int y, int w, int h, int n) {
		int r = 8, t = 20;

		int xn = w / t;
		int yn = h / t;

		int x0 = x + (w - xn * t) / 2 + t / 2;
		int y0 = y + h - t / 2;

		for (int j = 0; j < yn; j++) {
			for (int i = 0; i < xn; i++) {
				if (n-- > 0)
					paintRider(g, x0 + i * t, y0 - j * t, r);
			}
		}
	}

	private class Floor extends Canvas {
		int floor, numRiders;

		boolean upSet = false;

		boolean downSet = false;

		Floor(int floor, int numRiders) {
			this.floor = floor;
			this.numRiders = numRiders;

			setBackground(Color.black);
		}

		public Dimension getPreferredSize() {
			return new Dimension(w, h);
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public void repaint() {
			super.repaint();

			if (TCB.isNachosThread()) {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
				}
			}
		}

		void pressUpButton() {
			if (!upSet) {
				upSet = true;
				repaint();
			}
		}

		void pressDownButton() {
			if (!downSet) {
				downSet = true;
				repaint();
			}
		}

		void clearUpButton() {
			if (upSet) {
				upSet = false;
				repaint();
			}
		}

		void clearDownButton() {
			if (downSet) {
				downSet = false;
				repaint();
			}
		}

		void addRider() {
			numRiders++;

			repaint();
		}

		void removeRider() {
			numRiders--;

			repaint();
		}

		public void paint(Graphics g) {
			g.setColor(Color.lightGray);
			g.drawLine(0, 0, w, 0);

			paintRiders(g, 0, 5, 3 * w / 4, h - 10, numRiders);

			paintButtons(g);
		}

		private void paintButtons(Graphics g) {
			int s = 3 * w / 4;

			int x1 = s + w / 32;
			int x2 = w - w / 32;
			int y1 = h / 8;
			int y2 = h - h / 8;

			g.setColor(Color.darkGray);
			g.drawRect(x1, y1, x2 - x1, y2 - y1);
			g.setColor(Color.lightGray);
			g.fillRect(x1 + 1, y1 + 1, x2 - x1 - 2, y2 - y1 - 2);

			int r = Math.min((x2 - x1) / 3, (y2 - y1) / 6);
			int xc = (x1 + x2) / 2;
			int yc1 = (y1 + y2) / 2 - (3 * r / 2);
			int yc2 = (y1 + y2) / 2 + (3 * r / 2);

			g.setColor(Color.red);

			if (floor < numFloors - 1) {
				if (upSet)
					g.fillOval(xc - r, yc1 - r, 2 * r, 2 * r);
				else
					g.drawOval(xc - r, yc1 - r, 2 * r, 2 * r);
			}

			if (floor > 0) {
				if (downSet)
					g.fillOval(xc - r, yc2 - r, 2 * r, 2 * r);
				else
					g.drawOval(xc - r, yc2 - r, 2 * r, 2 * r);
			}
		}
	}

	private class ElevatorShaft extends Canvas {
		ElevatorShaft(int elevator) {
			this.elevator = elevator;

			floorsSet = new boolean[numFloors];
			for (int i = 0; i < numFloors; i++)
				floorsSet[i] = false;

			setBackground(Color.black);
		}

		public Dimension getPreferredSize() {
			return new Dimension(w, h * numFloors);
		}

		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		private void repaintElevator() {
			repaint(s, h * (numFloors - 1 - Math.max(floor, prevFloor)), w - 2
					* s, h * 2);

			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
			}
		}

		void openDoors() {
			doorsOpen = true;

			repaintElevator();
		}

		void closeDoors() {
			doorsOpen = false;

			repaintElevator();
		}

		void setDirectionDisplay(int direction) {
			this.direction = direction;

			repaintElevator();
		}

		void pressFloorButton(int floor) {
			if (!floorsSet[floor]) {
				floorsSet[floor] = true;

				repaintElevator();
			}
		}

		void elevatorMoved(int floor) {
			prevFloor = this.floor;
			this.floor = floor;

			floorsSet[floor] = false;

			repaintElevator();
		}

		void addRider() {
			numRiders++;

			repaintElevator();
		}

		void removeRider() {
			numRiders--;

			repaintElevator();
		}

		public void paint(Graphics g) {
			g.setColor(Color.lightGray);

			if (g.hitClip(0, 0, s, h * numFloors)) {
				g.drawLine(0, 0, 0, h * numFloors);
				g.drawLine(s - 1, 0, s - 1, h * numFloors);
				for (int y = 0; y < h * numFloors - s; y += s)
					g.drawLine(0, y, s - 1, y + s - 1);
			}

			if (g.hitClip(w - s, 0, s, h * numFloors)) {
				g.drawLine(w - s, 0, w - s, h * numFloors);
				g.drawLine(w - 1, 0, w - 1, h * numFloors);
				for (int y = 0; y < h * numFloors - s; y += s)
					g.drawLine(w - s, y, w - 1, y + s - 1);
			}

			// rectangle containing direction display area
			Rectangle d = new Rectangle(s * 3 / 2, h * (numFloors - 1 - floor),
					w - 3 * s, w / 3 - s);

			// unit of measurement in direction rect (12ux4u)
			int u = d.width / 12;

			// draw elevator, fill riders
			Rectangle e = new Rectangle(d.x, d.y + d.height, d.width, h
					- d.height - u);
			g.drawRect(e.x, e.y, e.width, e.height);
			paintRiders(g, e.x, e.y, e.width, e.height, numRiders);

			g.setColor(Color.lightGray);

			// draw doors...
			if (doorsOpen) {
				g.drawLine(e.x + 2 * s, e.y, e.x + 2 * s, e.y + e.height);
				for (int y = 0; y < e.height - 2 * s; y += 2 * s)
					g.drawLine(e.x, e.y + y, e.x + 2 * s, e.y + y + 2 * s);

				g.drawLine(e.x + e.width - 2 * s, e.y, e.x + e.width - 2 * s,
						e.y + e.height);
				for (int y = 0; y < e.height - 2 * s; y += 2 * s)
					g.drawLine(e.x + e.width - 2 * s, e.y + y, e.x + e.width,
							e.y + y + 2 * s);
			}
			else {
				for (int x = 0; x < e.width; x += 2 * s)
					g.drawLine(e.x + x, e.y, e.x + x, e.y + e.height);
			}

			g.setColor(Color.yellow);

			int[] xUp = { d.x + u * 6, d.x + u * 8, d.x + u * 7 };
			int[] yUp = { d.y + u * 3, d.y + u * 3, d.y + u * 1 };

			int[] xDown = { d.x + u * 4, d.x + u * 6, d.x + u * 5 };
			int[] yDown = { d.y + u * 1, d.y + u * 1, d.y + u * 3 };

			// draw arrows
			if (direction == ElevatorBank.dirUp)
				g.fillPolygon(xUp, yUp, 3);
			else
				g.drawPolygon(xUp, yUp, 3);

			if (direction == ElevatorBank.dirDown)
				g.fillPolygon(xDown, yDown, 3);
			else
				g.drawPolygon(xDown, yDown, 3);
		}

		private static final int s = 5;

		private boolean doorsOpen = false;

		private int floor = 0, prevFloor = 0, numRiders = 0;

		private int direction = ElevatorBank.dirNeither;

		private int elevator;

		private boolean floorsSet[];
	}
}
