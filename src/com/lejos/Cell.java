package com.lejos;

public class Cell {
	
	//May add one more location(absolute)
	Location l;
	Cell nN,sN,wN,eN;
	int color;
	
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
	
	

}
