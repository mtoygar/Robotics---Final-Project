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
