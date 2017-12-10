package com.lejos;

public class Location {

	int x,y;
	
	public Location() {
		super();
	}

	public Location(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public boolean equals (Location x) {
		System.out.println("LL:" + this.toString() +"/" + x.toString());		
		boolean result = true;
		if (this.getX() != x.getX()) result = false;
		if (this.getY() != x.getY()) result = false;
		return result;
	}
	
	public void add (Location x) {
		this.setX(this.getX() + x.getX());
		this.setY(this.getY() + x.getY());
	}
	
	public void rotate90Left () {
		if(this.getX() != 0) {
			this.setY(this.getX());
			this.setX(0);
		} else if(this.getY() != 0) {
			this.setX(this.getY() * -1);
			this.setY(0);
		}
	}
	
	public void rotate90Right () {
		if(this.getX() != 0) {
			this.setY(this.getX() * -1);
			this.setX(0);
		} else if(this.getY() != 0) {
			this.setX(this.getY());
			this.setY(0);
		}
	}
	
	public String toString() {
		String result ="" + this.getX() + "/" + this.getY();
		return result;
	}
}
