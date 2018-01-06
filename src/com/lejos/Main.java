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
			turnRight();
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

	public List<PossibleCellLocationTuple> getPossibleCurrentLocationMap(){
		int color = getColorSensorValue();
		int numberOfWalls = 0;
		int count = 0;
		int route = 0;
		boolean openGateFound = false;
		List<PossibleCellLocationTuple> locationTuple = new LinkedList<>();
		//turn 4 times.
		String wallLocation = "";
		for (int i = 0: i<4: i++){
			if (getFrontUltrasonicSensorValue < HALF_EDGE){
				wallLocation = wallLocation + "1";
				numberOfWalls = numberOfWalls + 1;
			}
			else{
				if (!openGateFound){
					openGateFound = true;
					route = count;
				}
				wallLocation = wallLocation + "0";
			}
			count = count + 1;
			turnLeft();
		}
		locationTuple = mMap.findPossibleCellMatches(color, numberOfWalls, wallLocation);	
		if (locationTuple.size() > 1){
			if (route == 2 || route == 3){
				move(route);	
			}
			else if (route == 0){
				move(1);	
			}
			else if (route == 1){
				move(0);	
			}
			/*0 1
			1 0
			2 2
			3 3*/
		}
		return locationTuple;
	}

	public PossibleCellLocationTuple localizeRobot(){
		do {
			List<PossibleCellLocationTuple> locationTuple = getPossibleCurrentLocationMap();	
		}
		while (locationTuple.size > 1){
			//finds an empty direction and go to that direction, on the background. //change the location that is sent to
			List<PossibleCellLocationTuple> locationTuple = getPossibleCurrentLocationMap();
		}
		return locationTuple.get(0);
	}

	public List<Cell> pathPlanning(PossibleCellLocationTuple currentLocation, Location desiredCellLocation){

		// reset the direction after kidnapping. Resetted direction will be north.
		if (east.isEqual(currentLocation.getDirection()){
			turnLeft();
		}
		if (west.isEqual(currentLocation.getDirection()){
			turnRight();
		}
		if (south.isEqual(currentLocation.getDirection()){
			turnRight();
			turnRight();
		}
		direction = new Location(north.getX(),north.getY());
		// implement Dijkstra's Algorithm.
		mMap.findCell(currentLocation.getL()).setVisited(true);
		mMap.findCell(currentLocation.getL()).setDistance(0);
		while (!mMap.findCell(desiredCellLocation).isVisited()){
			for (Cell cell : mMap.getCellList){
				if (cell.isVisited()){
					for (Cell neighbor : getNeighborCells(cell)){
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

	public List<Cell> getNeighborCells(Cell cell){
		List<Cell> neighbors = new LinkedList<>();
		if (cell.getnN() == null){
			neighbors.add(mMap.findCell(new Location(cell.getL().getX(), cell.getL().getY() + 1)));
		}
		if (cell.getsN() == null){
			neighbors.add(mMap.findCell(new Location(cell.getL().getX(), cell.getL().getY() - 1)));
		}
		if (cell.geteN() == null){
			neighbors.add(mMap.findCell(new Location(cell.getL().getX() + 1, cell.getL().getY())));
		}
		if (cell.getwN() == null){
			neighbors.add(mMap.findCell(new Location(cell.getL().getX() + 1, cell.getL().getY())));
		}
		return neighbors;
	}

	public void goAccrossPath(List<Cell> path){
		for (int i = 0; i < path.size() - 1; i++){
			findRouteAndMove(path.get(i), path.get(i+1));
		}
	}

	public void findRouteAndMove(Cell source, Cell destination){
		/*
		Move method. If route:
		0 --> go left
		1 --> go front
		2 --> go reverse
		3 --> go right
		*/
		if (source.getL().getX() > destination.getL().getX()){
			//west
			if (direction.isEqual(north)){
				move(0);
			}
			else if (direction.isEqual(south)){
				move(3);
			}
			else if (direction.isEqual(east)){
				move(2);
			}
			else if (direction.isEqual(west)){
				move(1);
			}
		}
		else if(source.getL().getX() < destination.getL().getX()){
			//east
			if (direction.isEqual(north)){
				move(3);
			}
			else if (direction.isEqual(south)){
				move(0);
			}
			else if (direction.isEqual(east)){
				move(1);
			}
			else if (direction.isEqual(west)){
				move(2);
			}
		}
		else if(source.getL().getY() > destination.getL().getY()){
			//south
			if (direction.isEqual(north)){
				move(2);
			}
			else if (direction.isEqual(south)){
				move(1);
			}
			else if (direction.isEqual(east)){
				move(3);
			}
			else if (direction.isEqual(west)){
				move(0);
			}
		}
		else if(source.getL().getY() < destination.getL().getY()){
			//north
			if (direction.isEqual(north)){
				move(1);
			}
			else if (direction.isEqual(south)){
				move(2);
			}
			else if (direction.isEqual(east)){
				move(0);
			}
			else if (direction.isEqual(west)){
				move(3);
			}
		}
		
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
