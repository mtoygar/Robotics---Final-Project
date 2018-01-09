package com.lejos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;

public class FinalProjectPC extends JFrame {
	
	private static final long serialVersionUID = 2280874288993963333L;
	
	public static final int BLACK = 7;
	public static final int RED = 0;
	public static final int GREEN = 1;
	public static final int BLUE = 2;
	public static final int WHITE = 6;
	
	public static final int CELL_DATA = 1;
	public static final int LOCATION_DATA = 2;
	
	static InputStream inputStream;
	static DataInputStream dataInputStream;
	
	static Map mMap = new Map();
	//static Particle robot = new Particle(0,0,true);
	
	static ArrayList<PossibleCellLocationTuple> locations = new ArrayList<PossibleCellLocationTuple>();
			
	public FinalProjectPC() {
		super("Final Project PC");
		setSize( 700, 500 );
		setVisible( true );		
	}
	
	public static void main(String[] args) throws Exception	{
		
		int motion;
		int distance;
		
		FinalProjectPC monitor = new FinalProjectPC();
		
		monitor.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		String ip = "10.0.1.1";
		
		@SuppressWarnings("resource")
		Socket socket = new Socket(ip, 1234);
		System.out.println("Connected!");
		
		inputStream = socket.getInputStream();
		dataInputStream = new DataInputStream(inputStream);
		int locationX,locationY,color;
		boolean north,south,east,west;
		
		//int count = 0;
		int type;
		while( true ){
			
			type = dataInputStream.readInt();
			
			if(type == CELL_DATA) {
				locationX = dataInputStream.readInt();
				locationY = dataInputStream.readInt();
				north = dataInputStream.readBoolean();
				south = dataInputStream.readBoolean();
				east = dataInputStream.readBoolean();
				west = dataInputStream.readBoolean();
				color = dataInputStream.readInt();
				
				mMap.discoverCurrentCell(new Location(locationX,locationY),
						north, south, east, west, color);
				
				System.out.println("" + locationX + " " +
						locationY + " " +
						north + " " +
						south + " " +
						east + " " +
						west +" " + color);
			} else if (type == LOCATION_DATA) {
				int size = dataInputStream.readInt();
				locations = new ArrayList<PossibleCellLocationTuple>();
				for(int i=0;i<size;i++) {
					int locX = dataInputStream.readInt();
					int locY = dataInputStream.readInt();
					int dirX = dataInputStream.readInt();
					int dirY = dataInputStream.readInt();
					PossibleCellLocationTuple locDir = new PossibleCellLocationTuple(
							new Location(locX,locY),
							new Location(dirX,dirY));
					locations.add(locDir);
					System.out.println("PPP" + locDir.getL().toString() +"///" + locDir.getDirection().toString());
					
				}
				System.out.println("---");
			}
			
			
			monitor.repaint();
			
			//count++;
		}
	}

	
	public void paint( Graphics g ) {
		super.paint( g );
		displayCells( g );
		displayPossibleLocations( g );		
	}

	
	public void displayCells(  Graphics g ) {
		for ( int i = 0; i < mMap.getCellList().size() ; i++ ){
			displayCell( mMap.getCellList().get(i), g );
		}
	}
	
	public void displayPossibleLocations(  Graphics g ) {
		for ( int i = 0; i < locations.size() ; i++ ){
			displayPossibleLocation( locations.get(i), g );
		}
	}
	
	public void displayPossibleLocation( PossibleCellLocationTuple locDir, Graphics g ) {
		int[] xPoints = {0,0,0};
		int[] yPoints = {0,0,0};
		
		int centerX = 275 + 50 * locDir.getL().getX();
		int centerY = 225 - 50 * locDir.getL().getY();
		
		if(locDir.getDirection().getX() > 0) {
			xPoints[0] = centerX+10;
			xPoints[1] = centerX+20;
			xPoints[2] = centerX+10;
			
			yPoints[0] = centerY-5;
			yPoints[1] = centerY;
			yPoints[2] = centerY+5;
			
		} else if(locDir.getDirection().getX() < 0) {
			xPoints[0] = centerX-10;
			xPoints[1] = centerX-20;
			xPoints[2] = centerX-10;
			
			yPoints[0] = centerY-5;
			yPoints[1] = centerY;
			yPoints[2] = centerY+5;
			
		} else if(locDir.getDirection().getY() > 0) {
			xPoints[0] = centerX-5;
			xPoints[1] = centerX;
			xPoints[2] = centerX+5;
			
			yPoints[0] = centerY-10;
			yPoints[1] = centerY-20;
			yPoints[2] = centerY-10;
			
		} else if(locDir.getDirection().getY() < 0) {
			xPoints[0] = centerX-5;
			xPoints[1] = centerX;
			xPoints[2] = centerX+5;
			
			yPoints[0] = centerY+10;
			yPoints[1] = centerY+20;
			yPoints[2] = centerY+10;
			
		}
		
		
		
		Graphics2D g2 = ( Graphics2D ) g;
		g2.setPaint(Color.gray);
		g2.setStroke( new BasicStroke( 2.0f ));
		g2.fillPolygon(xPoints, yPoints, 3);
	}
	
	
	
	public void displayCell( Cell cell, Graphics g ){
		Graphics2D g2 = ( Graphics2D ) g;
		if(cell.getColor() == BLACK) g2.setPaint( Color.black );
		//if(cell.getColor == WHITE) g2.setPaint( Color.white);
		else if(cell.getColor() == BLUE) g2.setPaint( Color.blue);
		else if(cell.getColor() == GREEN) g2.setPaint( Color.green);
		else if(cell.getColor() == RED) g2.setPaint( Color.red);
		else g2.setPaint(Color.white);
		
		g2.setStroke( new BasicStroke( 2.0f ));
		
		g2.fillRect(250 + 50 * cell.getL().getX() , 
				200 - 50 * cell.getL().getY(), 50, 50);
		
		g2.setPaint(Color.orange);
		
		if(cell.getnN() != null) {
			if(cell.getnN().equals(Map.getWall())) {
				g2.drawLine(250 + 50 * cell.getL().getX() +2,
					250 - 50 * cell.getL().getY()-48,
					250 + 50 * cell.getL().getX()+48,
					250 - 50 * cell.getL().getY()-48);
			}
		}
		
		if(cell.getsN() != null) {
			if(cell.getsN().equals(Map.getWall())) {
				g2.drawLine(250 + 50 * cell.getL().getX()+2,
						250 - 50 * cell.getL().getY()-2,
						250 + 50 * cell.getL().getX()+48,
						250 - 50 * cell.getL().getY()-2);
			}
		}
		

		if(cell.geteN() != null) {
			if(cell.geteN().equals(Map.getWall())) {
				g2.drawLine(250 + 50 * cell.getL().getX()+48,
						250 - 50 * cell.getL().getY()-2,
						250 + 50 * cell.getL().getX()+48,
						250 - 50 * cell.getL().getY()-48);
			}
		}
		
		if(cell.getwN() != null) {
			if(cell.getwN().equals(Map.getWall())) {
				g2.drawLine(250 + 50 * cell.getL().getX()+2,
						250 - 50 * cell.getL().getY()-2,
						250 + 50 * cell.getL().getX()+2,
						250 - 50 * cell.getL().getY()-48);
			}
		}
		
	}
	
	/*public void displayPose( Particle particle, Graphics g ){
		Graphics2D g2 = ( Graphics2D ) g;
		g2.setPaint( Color.blue );
		g2.setStroke( new BasicStroke( 5.0f ));
		
		g2.fillOval(250 + 3 * ((int)particle.locationX) - 5, 
				250 + 3 * ((int) particle.locationY) - 5, 10, 10);
		
	}*/

}

