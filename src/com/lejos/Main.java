package com.lejos;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;
import lejos.utility.PilotProps;

public class Main {
	
	static final float EDGE = 33;
	static final float HALF_EDGE = 10;
	static final float LEFT_HALF_EDGE = 7;
	
	static Location north = new Location(0,1);
	static Location south = new Location(0,-1);
	static Location east = new Location(1,0);
	static Location west = new Location(-1,0);
	
	static Map mMap = new Map();
	static Location direction = new Location(north.getX(),north.getY());
	static Location location = new Location(0,0);
	
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	
	static NXTUltrasonicSensor ultrasonicSensorFront = new NXTUltrasonicSensor(SensorPort.S3);
	static EV3UltrasonicSensor ultrasonicSensorLeft = new EV3UltrasonicSensor(SensorPort.S4);
	static EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S1);
	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S2);
	
	static EV3LargeRegulatedMotor leftMotor;
	static EV3LargeRegulatedMotor rightMotor;
	
	static MovePilot pilot;
	
	static int turn = 0;
	
	static int getColorSensorValue() {
		
		SampleProvider sampleProvider = colorSensor.getColorIDMode();
		if(sampleProvider.sampleSize() > 0) {
			float [] samples = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(samples, 0);
			
			//
			//System.out.println("Front sensor:" +  (100 * samples[0]) + "\n");
			return (int) samples[0];
		}
		return -1;		
	}
	
	static int getGyroSensorValue() {
		
		SampleProvider sampleProvider = gyroSensor.getAngleMode();
		if(sampleProvider.sampleSize() > 0) {
			float [] samples = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(samples, 0);

			return (int) samples[0];
		}
		return -1;		
	}
	
	static float getFrontUltrasonicSensorValue() {
		
		SampleProvider sampleProvider = ultrasonicSensorFront.getDistanceMode();
		if(sampleProvider.sampleSize() > 0) {
			float [] samples = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(samples, 0);
			
			//
			//System.out.println("Front sensor:" +  (100 * samples[0]) + "\n");
			return 100 * samples[0];
		}
		return -1;		
	}
	
	static float getLeftUltrasonicSensorValue() {
		
		SampleProvider sampleProvider = ultrasonicSensorLeft.getDistanceMode();
		if(sampleProvider.sampleSize() > 0) {
			float [] samples = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(samples, 0);
			
			//
			//System.out.println("Left sensor:" + (100 * samples[0]) + "\n");
			return 100 * samples[0];
		}
		return -1;		
	}
	
	static int getTachoValue() {
		double xDouble = (((double) leftMotor.getTachoCount() 
				+ (double) rightMotor.getTachoCount()) / (double) 720 ) * 
				5.6 * Math.PI ;
		
		int x = (int) xDouble;
		//System.out.println("T:" + x);
		return x;
	}
	
	public static void main(String[] args) throws Exception {		
		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Mapping", graphicsLCD.getWidth()/2,
				0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		PilotProps pilotProps = new PilotProps();
		pilotProps.setProperty(PilotProps.KEY_WHEELDIAMETER, "5.6");
		pilotProps.setProperty(PilotProps.KEY_TRACKWIDTH, "12.0");
		pilotProps.setProperty(PilotProps.KEY_LEFTMOTOR, "A");
		pilotProps.setProperty(PilotProps.KEY_RIGHTMOTOR, "D");
		pilotProps.setProperty(PilotProps.KEY_REVERSE, "false");
		pilotProps.storePersistentValues();
		pilotProps.loadPersistentValues();
    	
    	leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
    	
    	float wheelDiameter = Float.parseFloat(
    			pilotProps.getProperty(PilotProps.KEY_WHEELDIAMETER, "5.6"));
    	float trackWidth = Float.parseFloat(
    			pilotProps.getProperty(PilotProps.KEY_TRACKWIDTH, "12.0"));
    	boolean reverse = Boolean.parseBoolean(
    			pilotProps.getProperty(PilotProps.KEY_REVERSE, "false"));
    	
    	Chassis chassis = new WheeledChassis(
    			new Wheel[]{WheeledChassis.modelWheel(leftMotor,wheelDiameter)
    					.offset(-trackWidth/2).invert(reverse),
    					WheeledChassis.modelWheel(rightMotor,wheelDiameter)
    					.offset(trackWidth/2).invert(reverse)}, 
    			WheeledChassis.TYPE_DIFFERENTIAL);
    	
    	pilot = new MovePilot(chassis);
    	pilot.setAngularSpeed(30);
    	pilot.setLinearSpeed(15);
    	
		
		//ServerSocket serverSocket = new ServerSocket(1234);
		
		graphicsLCD.clear();
		graphicsLCD.drawString("FProject", graphicsLCD.getWidth()/2,
				0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Waiting", graphicsLCD.getWidth()/2,
				20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
		
		//Socket client = serverSocket.accept();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("FProject", graphicsLCD.getWidth()/2,
				0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Connected", graphicsLCD.getWidth()/2,
				20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
        
		//OutputStream outputStream = client.getOutputStream();
		
		//DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		

			
		mMap.initialize(getFront(),getLeft(),getColor());
		//int counter = 0;
		PrintWriter writer = new PrintWriter("map.txt", "UTF-8");
		while (!mMap.isDiscovered()) {	
		//while (counter < 4) {	
			
			writer.println("L:" + location.toString() +" D:" + direction.toString() + " " + getFront() + " " + getLeft());
			
			Sound.playTone(440, 75, 10);
			int moveNumber = findNextMove();
			
			move(moveNumber);
			calibrate();
			findValuesWithDirection();
			
			
			//dataOutputStream.writeInt(13);
			//dataOutputStream.flush();

			
			//Delay.msDelay(2);
			//System.out.println("Sent!");
			//counter++;
		}
		
		
		List<Cell> mapCells = mMap.getCellList();
		for(Cell cell : mapCells) {
			writer.print("Cell:" + cell.getL().toString() + " ");
			if(cell.getnN() != null) writer.print("1"); else writer.print("0");
			if(cell.getsN() != null) writer.print("1"); else writer.print("0");
			if(cell.geteN() != null) writer.print("1"); else writer.print("0");
			if(cell.getwN() != null) writer.print("1"); else writer.print("0");
			writer.println();
		}
		writer.close();
		
		//dataOutputStream.close();
		//serverSocket.close();
	}
	
	public static int findNextMove (){
		if(getColor() == Color.BLACK) {
			return 3;
		} else if (getLeft()){
			return 0; //turn left and forward
		}
		else if (getFront()){
			return 1; // forward
		}
		else {
			return 2; //turn right
		}
	}
	
	public static boolean getFront(){
		return getFrontUltrasonicSensorValue()>HALF_EDGE+15;
	}
	
	public static boolean getLeft(){
		return getLeftUltrasonicSensorValue()>LEFT_HALF_EDGE+15;
	}
	
	public static void move(int route){
		if (route == 0) {
			turnLeft();
			
			//
			Sound.playTone(440, 75, 10);
			findValuesWithDirection();
			
			
			pilot.travel(EDGE);
			location.add(direction);
		}
		else if (route == 1){
			pilot.travel(EDGE);
			location.add(direction);
		}
		else if (route == 2){
			pilot.rotate(90);
			direction.rotate90Right();
		}
		else if (route == 3) {
			turnRight();
			turnRight();
			
			//
			Sound.playTone(440, 75, 10);
			findValuesWithDirection();
			
			pilot.travel(EDGE);
			location.add(direction);
		}
	}
	
	public static void findValuesWithDirection() {
		if(direction.equals(north)) {
			mMap.discoverCurrentCell(location,getFront(),true,true,getLeft(),getColor());
			//System.out.println("1111111111111");
		}
			
		else if(direction.equals(east)) {
			mMap.discoverCurrentCell(location,getLeft(),true,getFront(),true,getColor());
			//System.out.println("222222222222");
		}
			
		else if(direction.equals(south)) {
			mMap.discoverCurrentCell(location,true,getFront(),getLeft(),true,getColor());
			//System.out.println("33333333333333");
		}
			
		else if(direction.equals(west)) {
			mMap.discoverCurrentCell(location,true,getLeft(),true,getFront(),getColor());
			//System.out.println("444444444444444");
		}
	}
	
	public static int getColor() {
		return getColorSensorValue();
		//return -1;
	}
	public List<Cell> pathPlanning(PossibleCellLocationTuple currentLocation, Location desiredCellLocation){
		// implement Dijkstra's Algorithm.
		mMap.findCell(currentLocation.getL()).setVisited(true);
		mMap.findCell(currentLocation.getL()).setDistance(0);
		while (!mMap.findCell(desiredCellLocation).isVisited()){
			for (Cell cell : mMap.getCellList){
				if (cell.isVisited()){
					for (Cell neighbor : cell.getNeighborCells()){
						if (!neighbor.isVisited()){
							neighbor.setVisited(true);
							neighbor.setDistance(cell.getDistance() + 1);
						}
					}
				}
			}
		}
		int finalDistance = mMap.findCell(desiredCellLocation).getDistance();
		int count = 1;
		List path = new LinkedList<>();
		while (count < finalDistance){
			for (Cell cell : mMap.getCellList){
				if (cell.getDistance() == count){
					count = count + 1;
					path.add(cell);
				}
			}
		}
		path.add(mMap.findCell(desiredCellLocation));
		return path;
	}
	
	public static void turnLeft() {
		
		int target = getGyroSensorValue() + 90;
		pilot.rotate(-200,true);
		//
		System.out.println("Turning!");
		while((getGyroSensorValue() - target) < 0) System.out.println(getGyroSensorValue());
		pilot.stop();
		direction.rotate90Left();
		//
		System.out.println("Turning finished!");
	}
	
	public static void turnRight() {
		
		int target = getGyroSensorValue() - 90;
		pilot.rotate(200,true);
		//
		System.out.println("Turning!");
		while((getGyroSensorValue() - target) > 0) System.out.println(getGyroSensorValue());
		pilot.stop();
		direction.rotate90Right();
		//
		System.out.println("Turning finished!");
	}
	
	public static void calibrate() {
		if(getFrontUltrasonicSensorValue() < (HALF_EDGE / 2)) {
			pilot.travel(-1 * (HALF_EDGE - getFrontUltrasonicSensorValue()) ); 
		}
		
		if(getLeftUltrasonicSensorValue() < (LEFT_HALF_EDGE / 2) ) {
			float value = getLeftUltrasonicSensorValue();
			turnLeft();
			pilot.travel(-1 * (LEFT_HALF_EDGE - value));
			turnRight();
		}
	}
	
}
