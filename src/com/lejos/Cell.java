package com.lejos;

public class Cell {
	
	//May add one more location(absolute)
	Location l;
	Cell nN,sN,wN,eN;
	int color;
	boolean visited;
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
		in numberOfWalls = 0;
		if (this.geteN.getL == null){
			numberOfWalls = numberOfWalls + 1;
		}
		if (this.getwN.getL == null){
			numberOfWalls = numberOfWalls + 1;
		}
		if (this.getsN.getL == null){
			numberOfWalls = numberOfWalls + 1;
		}
		if (this.getnN.getL == null){
			numberOfWalls = numberOfWalls + 1;
		}
		return numberOfWalls;
	}
	
	public List<String> getWallLocationList(){
		List<String> possibleWallLocations = new LinkedList<>();
		String walls = "";
		if (this.getnN.getL== null){
			walls = walls + "1";
		}
		else{
			walls = walls + "0";	
		}
		if (this.getwN.getL == null){
			walls = walls + "1";
		}
		else{
			walls = walls + "0";			
		}
		if (this.getsN.getL == null){
			walls = walls + "1";
		}
		else{
			walls = walls + "0";
		}
		if (this.geteN.getL == null){
			walls = walls + "1";
		}
		else{
			walls = walls + "0";
		}
		possibleWallLocations.add(walls);
		possibleWallLocations.add(walls[1] + walls[2] + walls[3] + walls[0]);
		possibleWallLocations.add(walls[2] + walls[3] + walls[0] + walls[1]);
		possibleWallLocations.add(walls[3] + walls[0] + walls[2] + walls[2]);
		return possibleWallLocations;
	}
	
	public List<Cell> getNeighborCells(){
		List<Cell> neighbors = new LinkedList<>();
		if (this.getnN.getL != null){
			neighbors.add(this.getnN);
		}
		if (this.getsN.getL != null){
			neighbors.add(this.getsN);
		}
		if (this.geteN.getL != null){
			neighbors.add(this.geteN);
		}
		if (this.getwN.getL != null){
			neighbors.add(this.getwN);
		}
		return neighbors;
	}
}
