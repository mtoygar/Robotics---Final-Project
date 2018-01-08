package com.lejos;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

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
	
	static final float EDGE = 35;
	static final float HALF_EDGE = 10;
	static final float LEFT_HALF_EDGE = 7;
	static final String FILENAME = "map.txt";
	static int previousRoute = -1;
	
	static Location north = new Location(0,1);
	static Location south = new Location(0,-1);
	static Location east = new Location(1,0);
	static Location west = new Location(-1,0);
	
	static Map mMap = new Map();
	static Location direction = new Location(north.getX(),north.getY());
	static Location location = new Location(0,0);
	
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	static GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
	
	static PilotProps pilotProps = new PilotProps();
	static MovePilot pilot;

	
	static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);

	
	static NXTUltrasonicSensor ultrasonicSensorFront = new NXTUltrasonicSensor(SensorPort.S3);
	static EV3UltrasonicSensor ultrasonicSensorLeft = new EV3UltrasonicSensor(SensorPort.S4);
	static EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S1);
	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S2);
	
	static int turn = 0;
	
	
	static ServerSocket serverSocket;
	static Socket client;
	static OutputStream outputStream;
	static DataOutputStream dataOutputStream;
	
	public static final int CELL_DATA = 1;
	public static final int LOCATION_DATA = 2;
	
	
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
		
		pilotProps.setProperty(PilotProps.KEY_WHEELDIAMETER, "5.6");
		pilotProps.setProperty(PilotProps.KEY_TRACKWIDTH, "12.0");
		pilotProps.setProperty(PilotProps.KEY_LEFTMOTOR, "A");
		pilotProps.setProperty(PilotProps.KEY_RIGHTMOTOR, "D");
		pilotProps.setProperty(PilotProps.KEY_REVERSE, "false");
		pilotProps.storePersistentValues();
		pilotProps.loadPersistentValues();
		
		pilot.setAngularSpeed(30);
		pilot.setLinearSpeed(15);
    	
		graphicsLCD.clear();
		graphicsLCD.drawString("Choose stage..", graphicsLCD.getWidth()/2,
				0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		while(true) {
			int choice = Button.waitForAnyPress();
			
			if(choice == Button.ID_UP) {
				mapping();
			} else if(choice == Button.ID_DOWN) {
				initializeTaskExec();
				populateCellFromLogs();
				localizeRobot();
				goAccrossPath(pathPlanning(getMagicWeaponLocation()));
			} else if(choice == Button.ID_ENTER) {
				//mapping();
			} else if(choice == Button.ID_ESCAPE) {
				//mapping();
			} 
		}
		
		
	}
	
	public static Location getMagicWeaponLocation() {
		for (Cell cell : mMap.getCellList()) {
			if(cell.getColor() == Color.BLUE) {
				return cell.getL();
			}
		}
		return null;
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
		return getFrontUltrasonicSensorValue()>HALF_EDGE+10;
	}
	
	public static boolean getLeft(){
		return getLeftUltrasonicSensorValue()>LEFT_HALF_EDGE+10;
	}
	
	public static void move(int route) throws IOException{
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
		}
		else if (route == 3) {
			turnRight();
			turnRight();
			
			//
			Sound.playTone(440, 75, 10);
			findValuesWithDirection();
			
			pilot.travel(EDGE);
			location.add(direction);
		} else if(route == 4) {
			turnLeft();
			
			pilot.travel(EDGE);
			location.add(direction);
		} else if(route == 5) {
			turnRight();
			turnRight();
			
			pilot.travel(EDGE);
			location.add(direction);
		}
	}
	
	public static void mapping() throws IOException {
		serverSocket = new ServerSocket(1234);
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Mapping", graphicsLCD.getWidth()/2,
				0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Waiting for PC", graphicsLCD.getWidth()/2,
				20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
		
		client = serverSocket.accept();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Mapping", graphicsLCD.getWidth()/2,
				0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Connected", graphicsLCD.getWidth()/2,
				20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
        
		outputStream = client.getOutputStream();
		
		dataOutputStream = new DataOutputStream(outputStream);
		

			
		mMap.initialize(getFront(),getLeft(),getColor());
		sendData(location,getFront(),true,true,getLeft(),getColor());
		//int counter = 0;
		PrintWriter writer = new PrintWriter(FILENAME, "UTF-8");
		while (!mMap.isDiscovered()) {	
		//while (counter < 4) {	
			
			//writer.println("L:" + location.toString() +" D:" + direction.toString() + " " + getFront() + " " + getLeft());
			
			Sound.playTone(440, 75, 10);
			int moveNumber = findNextMove();
			
			move(moveNumber);
			calibrate();
			findValuesWithDirection();

			
			//Delay.msDelay(2);
			//System.out.println("Sent!");
			//counter++;
		}
		
		
		List<Cell> mapCells = mMap.getCellList();
		for(Cell cell : mapCells) {
			writer.print(cell.getL().getX()+ " ");
			writer.print(cell.getL().getY()+ " ");
			if(cell.getnN() != null) writer.print("1 "); else writer.print("0 ");
			if(cell.getsN() != null) writer.print("1 "); else writer.print("0 ");
			if(cell.geteN() != null) writer.print("1 "); else writer.print("0 ");
			if(cell.getwN() != null) writer.print("1 "); else writer.print("0 ");
			writer.print(cell.getColor());
			writer.println();
		}
		writer.close();
		
		//
		Sound.beep();
		Sound.beep();
		Sound.playTone(440, 75, 10);
		
		//dataOutputStream.close();
		//serverSocket.close();
	}
	
	public static void initializeTaskExec() throws IOException {
		serverSocket = new ServerSocket(1234);
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Task Execution", graphicsLCD.getWidth()/2,
				0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Waiting for PC", graphicsLCD.getWidth()/2,
				20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
		
		client = serverSocket.accept();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Task Execution", graphicsLCD.getWidth()/2,
				0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Connected", graphicsLCD.getWidth()/2,
				20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
        
		outputStream = client.getOutputStream();
		
		dataOutputStream = new DataOutputStream(outputStream);
	}
	
	public static void findValuesWithDirection() throws IOException {
		if(direction.equals(north)) {
			mMap.discoverCurrentCell(location,getFront(),true,true,getLeft(),getColor());
			sendData(location,getFront(),true,true,getLeft(),getColor());
			//System.out.println("1111111111111");
		}
			
		else if(direction.equals(east)) {
			mMap.discoverCurrentCell(location,getLeft(),true,getFront(),true,getColor());
			sendData(location,getLeft(),true,getFront(),true,getColor());
			//System.out.println("222222222222");
		}
			
		else if(direction.equals(south)) {
			mMap.discoverCurrentCell(location,true,getFront(),getLeft(),true,getColor());
			sendData(location,true,getFront(),getLeft(),true,getColor());
			//System.out.println("33333333333333");
		}
			
		else if(direction.equals(west)) {
			mMap.discoverCurrentCell(location,true,getLeft(),true,getFront(),getColor());
			sendData(location,true,getLeft(),true,getFront(),getColor());
			//System.out.println("444444444444444");
		}
	}
	
	public static void sendData(Location location, boolean north, boolean south,
			boolean east, boolean west, int color) throws IOException {
		
		//This integer tells PC that next data is cell knowledge.
		dataOutputStream.writeInt(CELL_DATA);
		dataOutputStream.flush();
		
		dataOutputStream.writeInt(location.getX());
		dataOutputStream.flush();
		
		dataOutputStream.writeInt(location.getY());
		dataOutputStream.flush();
		
		dataOutputStream.writeBoolean(north);
		dataOutputStream.flush();

		dataOutputStream.writeBoolean(south);
		dataOutputStream.flush();
		
		dataOutputStream.writeBoolean(east);
		dataOutputStream.flush();
		
		dataOutputStream.writeBoolean(west);
		dataOutputStream.flush();
		
		dataOutputStream.writeInt(color);
		dataOutputStream.flush();
	}
	
	public static void sendLocation(List<PossibleCellLocationTuple> list) throws IOException {
		
		//This integer tells PC that next data is cell knowledge.
		dataOutputStream.writeInt(LOCATION_DATA);
		dataOutputStream.flush();
		
		dataOutputStream.writeInt(list.size());
		dataOutputStream.flush();
		
		for(int i=0;i<list.size();i++) {
			dataOutputStream.writeInt(list.get(i).getL().getX());
			dataOutputStream.flush();
			
			dataOutputStream.writeInt(list.get(i).getL().getY());
			dataOutputStream.flush();
			
			dataOutputStream.writeInt(list.get(i).getDirection().getX());
			dataOutputStream.flush();
			
			dataOutputStream.writeInt(list.get(i).getDirection().getY());
			dataOutputStream.flush();
		}
	}
	
	public static int getColor() {
		return getColorSensorValue();
		//return -1;
	}

	public static List<Cell> populateCellFromLogs() throws IOException{
		List<Cell> recoveredCellList = new LinkedList<>();

		

		try {
			
			File file = new File(FILENAME);
			Scanner fileScan = new Scanner(file);

			String sCurrentLine;

			while (fileScan.hasNextLine()) {
				// create new Cell instance with known locations and color.
				
				sCurrentLine = fileScan.nextLine();
				Scanner lineScan = new Scanner(sCurrentLine);
				int x = lineScan.nextInt();
				int y = lineScan.nextInt();
				int n = lineScan.nextInt();
				int s = lineScan.nextInt();
				int e = lineScan.nextInt();
				int w = lineScan.nextInt();
				int color = lineScan.nextInt();
				lineScan.close();
				Cell cell = new Cell(new Location(x,y),color); 

				// set the neighbors of the cell
				if (n == 1){
					cell.setnN(Map.getWall());
				}
				else{
					cell.setnN(null);	
				}
				if (s == 1){
					cell.setsN(Map.getWall());
				}
				else{
					cell.setsN(null);	
				}
				if (e == 1){
					cell.seteN(Map.getWall());
				}
				else{
					cell.seteN(null);	
				}
				if (w == 1){
					cell.setwN(Map.getWall());
				}
				else{
					cell.setwN(null);	
				}

				// add cell to the Cell List.
				recoveredCellList.add(cell);
				System.out.println(sCurrentLine);
			}
			
			fileScan.close();

		} catch (IOException e) {
			
			System.out.println("Map fetching error.");
			e.printStackTrace();

		} 
		mMap.setCellList(recoveredCellList);
		for(Cell cell : recoveredCellList) {
			boolean north=true,south=true,east=true,west=true;
			if(cell.getnN() != null) north = !cell.getnN().equals(Map.getWall());
			if(cell.getsN() != null) south = !cell.getsN().equals(Map.getWall());
			if(cell.geteN() != null) east = !cell.geteN().equals(Map.getWall());
			if(cell.getwN() != null) west = !cell.getwN().equals(Map.getWall());
			sendData(cell.getL(),north ,south,east, west, cell.getColor());
		}
		return recoveredCellList;
	}

	public static List<PossibleCellLocationTuple> getPossibleCurrentLocationMap(List<PossibleCellLocationTuple> previousTuple) throws IOException{
		int color = getColorSensorValue();
		int numberOfWalls = 0;
		int count = 0;
		int route = 0;
		boolean openGateFound = false;
		List<PossibleCellLocationTuple> locationTuple = new LinkedList<>();
		//turn 4 times.
		String wallLocation = "";
		for (int i = 0; i<4; i++){
			if (getFrontUltrasonicSensorValue() < HALF_EDGE+10){
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
		locationTuple = mMap.findPossibleCellMatches(color, numberOfWalls, wallLocation, previousRoute, previousTuple);	
		previousRoute = route;
		if (locationTuple.size() > 1){
			if (route == 2){
				move(2);
				move(1);	
			}
			else if(route == 3) {
				move(5);
			}
			else if (route == 0){
				move(1);	
			}
			else if (route == 1){
				move(4);	
			}
			/*0 1
			1 0
			2 2
			3 3*/
		}
		return locationTuple;
	}

	public static void localizeRobot() throws IOException{
		
		List<PossibleCellLocationTuple> locationTuples = getPossibleCurrentLocationMap(null);	
		while (locationTuples.size() != 1){
			//finds an empty direction and go to that direction, on the background. //change the location that is sent to
			locationTuples = getPossibleCurrentLocationMap(locationTuples);
			sendLocation(locationTuples);
		}
		//return locationTuple.get(0);
		location = locationTuples.get(0).getL();
		direction = locationTuples.get(0).getDirection();
		
		
	}

	public static List<Cell> pathPlanning(Location desiredCellLocation){

		// reset the direction after kidnapping. Resetted direction will be north.
		if (east.equals(direction)){
			turnLeft();
		}
		if (west.equals(direction)){
			turnRight();
		}
		if (south.equals(direction)){
			turnRight();
			turnRight();
		}
		//direction = new Location(north.getX(),north.getY());
		// implement Dijkstra's Algorithm.
		mMap.findCell(desiredCellLocation).setVisited(true);
		mMap.findCell(desiredCellLocation).setDistance(0);
		while (!mMap.findCell(location).isVisited()){
			for (Cell cell : mMap.getCellList()){
				if (cell.isVisited()){
					for (Cell neighbor : mMap.getNeighborCells(cell)){
						if (!neighbor.isVisited()){
							neighbor.setVisited(true);
							neighbor.setDistance(cell.getDistance() + 1);
						}
					}
				}
			}
		}
		int finalDistance = mMap.findCell(location).getDistance();
		int count = finalDistance - 1;
		List<Cell> path = new ArrayList<Cell>();
		path.add(mMap.findCell(desiredCellLocation));
		while (count != 0){
			for (Cell cell : mMap.getCellList()){
				if (cell.getDistance() == count && (path.size() == 0 || mMap.isNeighbor(path.get(path.size() - 1),cell))){
					count = count - 1;
					path.add(cell);
					break;
				}
			}
		}
		path.add(mMap.findCell(location));
		return path;
	}

	public static void goAccrossPath(List<Cell> path) throws IOException{
		for (int i = path.size() - 1; i > 0; i--){
			findRouteAndMove(path.get(i), path.get(i-1));
		}
	}

	public static void findRouteAndMove(Cell source, Cell destination) throws IOException{
		/*
		Move method. If route:
		0 --> go left
		1 --> go front
		2 --> go reverse
		3 --> go right
		*/
		if (source.getL().getX() > destination.getL().getX()){
			//west
			if (direction.equals(north)){
				move(4);
			}
			else if (direction.equals(south)){
				move(5);
				move(1);
			}
			else if (direction.equals(east)){
				move(2);
				move(1);
			}
			else if (direction.equals(west)){
				move(1);
			}
			ArrayList<PossibleCellLocationTuple> dataList = new ArrayList<PossibleCellLocationTuple>();
			PossibleCellLocationTuple data = new PossibleCellLocationTuple();
			data.setL(location);
			data.setDirection(direction);
			dataList.add(data);
			sendLocation(dataList);
		}
		else if(source.getL().getX() < destination.getL().getX()){
			//east
			if (direction.equals(north)){
				move(5);
				move(1);
			}
			else if (direction.equals(south)){
				move(4);
			}
			else if (direction.equals(east)){
				move(1);
			}
			else if (direction.equals(west)){
				move(2);
				move(1);
			}
		}
		else if(source.getL().getY() > destination.getL().getY()){
			//south
			if (direction.equals(north)){
				move(2);
				move(1);
			}
			else if (direction.equals(south)){
				move(1);
			}
			else if (direction.equals(east)){
				move(5);
				move(1);
			}
			else if (direction.equals(west)){
				move(4);
			}
		}
		else if(source.getL().getY() < destination.getL().getY()){
			//north
			if (direction.equals(north)){
				move(1);
			}
			else if (direction.equals(south)){
				move(2);
				move(1);
			}
			else if (direction.equals(east)){
				move(4);
			}
			else if (direction.equals(west)){
				move(5);
				move(1);
			}
		}
		
	}
	
	public static void turnLeft() {
		
		direction.rotate90Left();
		
		int reducedError = (int) (0.8 * angleError());
		int target = getGyroSensorValue() + 90 - reducedError;
		//int target2 = direction.degree();
		//System.out.println("direction degree" + target2);
		//int target = (int)((10 * target1 + 0 * target2) / 10);
		pilot.rotate(-400,true);
		//System.out.println("Turning!");
		while((getGyroSensorValue() - target) < 0) 
			System.out.println(getGyroSensorValue());
		pilot.stop();
		
		//
		System.out.println("Turning finished!");
	}
	
	public static void turnRight() {
		
		direction.rotate90Right();
		int reducedError = (int) (0.8 * angleError());
		int target = getGyroSensorValue() - 90 - reducedError;
		//int target2 = direction.degree();
		//System.out.println("direction degree" + target2);
		//int target = (int) ((10 * target1 + 0 * target2) / 10);
		pilot.rotate(400,true);
		//System.out.println("Turning!");
		while((getGyroSensorValue() - target) > 0) 
			System.out.println(getGyroSensorValue());
		pilot.stop();
		
		//
		System.out.println("Turning finished!");
	}
	
	public static int angleError() {
		int angle = getGyroSensorValue();
		int error = angle % 90;
		if(error > 45) error = error - 90;
		return error;
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
