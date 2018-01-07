package com.lejos;

import java.util.LinkedList;
import java.util.List;

public class Cell {
	
	//May add one more location(absolute)
	Location l;
	Cell nN,sN,wN,eN;
	int color;
	boolean visited = false;
	int distance;
	
	public Cell() {
		super();
	}

	public Cell(Location l, int color) {
		super();
		this.l = l;
		this.color = color;
	}

	public Location getL() {
		return l;
	}

	public void setL(Location l) {
		this.l = l;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public Cell getnN() {
		return nN;
	}

	public void setnN(Cell nN) {
		this.nN = nN;
	}

	public Cell getsN() {
		return sN;
	}

	public void setsN(Cell sN) {
		this.sN = sN;
	}

	public Cell getwN() {
		return wN;
	}

	public void setwN(Cell wN) {
		this.wN = wN;
	}

	public Cell geteN() {
		return eN;
	}

	public void seteN(Cell eN) {
		this.eN = eN;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getNumberOfWalls(){
		int numberOfWalls = 0;
		if (this.geteN() != null){
			numberOfWalls = numberOfWalls + 1;
		}
		if (this.getwN() != null){
			numberOfWalls = numberOfWalls + 1;
		}
		if (this.getsN() != null){
			numberOfWalls = numberOfWalls + 1;
		}
		if (this.getnN() != null){
			numberOfWalls = numberOfWalls + 1;
		}
		return numberOfWalls;
	}
	
	public List<String> getWallLocationList(){
		List<String> possibleWallLocations = new LinkedList<>();
		String walls = "";
		if (this.getnN() != null){
			walls = walls + "1";
		}
		else{
			walls = walls + "0";	
		}
		if (this.getwN() != null){
			walls = walls + "1";
		}
		else{
			walls = walls + "0";			
		}
		if (this.getsN() != null){
			walls = walls + "1";
		}
		else{
			walls = walls + "0";
		}
		if (this.geteN() != null){
			walls = walls + "1";
		}
		else{
			walls = walls + "0";
		}
		possibleWallLocations.add(walls);
		possibleWallLocations.add(""+walls.charAt(1) + walls.charAt(2) + walls.charAt(3) + walls.charAt(0));
		possibleWallLocations.add(""+walls.charAt(2) + walls.charAt(3) + walls.charAt(0) + walls.charAt(1));
		possibleWallLocations.add(""+walls.charAt(3) + walls.charAt(0) + walls.charAt(1) + walls.charAt(2));
		return possibleWallLocations;
	}
}
