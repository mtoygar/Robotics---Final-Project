package com.lejos;

import java.util.ArrayList;
import java.util.List;

import lejos.robotics.Color;

public class Map {
	
	List<Cell> cellList;
	static Cell wall = new Cell();
	
	public Map() {
		this.cellList = new ArrayList<Cell>();
	}
	
	public List<Cell> getCellList() {
		return cellList;
	}
	
	public void setCellList(List<Cell> cellList) {
		this.cellList = cellList;
	}
	
	

	public static Cell getWall() {
		return wall;
	}

	public static void setWall(Cell wall) {
		Map.wall = wall;
	}
	
	public void addCell (Cell newCell) {
		Cell existingCell = alreadyExists(newCell);
		if(existingCell == null) {
			existingCell = newCell;
			cellList.add(existingCell);
		}/*
		else{
			if(existingCell.getnN() == null) existingCell.setnN(newCell.getnN());
			if(existingCell.getsN() == null) existingCell.setsN(newCell.getsN());
			if(existingCell.geteN() == null) existingCell.seteN(newCell.geteN());
			if(existingCell.getwN() == null) existingCell.setwN(newCell.getwN());
		}
		if(existingCell.getnN() != null) existingCell.getnN().setsN(existingCell);
		if(existingCell.getsN() != null) existingCell.getsN().setnN(existingCell);
		if(existingCell.geteN() != null) existingCell.geteN().setwN(existingCell);
		if(existingCell.getwN() != null) existingCell.getwN().seteN(existingCell);*/
		
		//this.cellList.add(newCell);
	}
	
	public Cell alreadyExists(Cell cell){
		for(Cell a : cellList) {
			if(a.getL().equals(cell.getL())) {
				return a;
			}
		}
		return null;
	}
	
	public boolean isDiscovered () {
		//boolean result = true;
		for(Cell cell : cellList) {
			if(cell.getColor() != Color.BLACK) {
				if(cell.getnN() == null) if(findCell(new Location(cell.getL().getX(),cell.getL().getY() + 1)) == null) return false;
				if(cell.getsN() == null) if(findCell(new Location(cell.getL().getX(),cell.getL().getY() - 1)) == null) return false;
				if(cell.geteN() == null) if(findCell(new Location(cell.getL().getX() + 1,cell.getL().getY())) == null) return false;
				if(cell.getwN() == null) if(findCell(new Location(cell.getL().getX() - 1,cell.getL().getY())) == null) return false;
			}
		}
		return true;
	}
	


	
	public void initialize (boolean north, boolean west,int color){
		Cell initialCell = new Cell();
		initialCell.setL(new Location(0,0));
		initialCell.setColor(color);
		if (!north){
			initialCell.setnN(wall);
		}
		if (!west){
			initialCell.setwN(wall);
		}
		addCell(initialCell);
	}
	
	public void discoverCurrentCell(Location location, boolean north, boolean south, boolean east, boolean west, int color){
		
		Cell temp = findCell(location);
		if(temp == null) {
			temp = new Cell(new Location(location.getX(),location.getY()),color);
			this.addCell(temp);
		}
		if(!north) temp.setnN(Map.getWall());
		if(!south) temp.setsN(Map.getWall());
		if(!east) temp.seteN(Map.getWall());
		if(!west) temp.setwN(Map.getWall());
	}
	
	public Cell findCell(Location location){
		for(Cell cell : cellList) {
			if (cell.getL().getX() == location.getX() && cell.getL().getY() == location.getY()){
				return cell;
			}
		}
		return null;
	}

	public List<PossibleCellLocationTuple> findPossibleCellMatches(int color, int numberOfWalls, String wallLocation){
		List<PossibleCellLocationTuple> locationTuple = new LinkedList<>();
		for (Cell cell : getCellList()){
			if ((color == cell.getColor) && (numberOfWalls == cell.getNumberOfWalls)){
				int direction = 0;
				for (String wallLocations : cell.getWallLocationList()){
					if (StringUtils.isEqual(wallLocation, wallLocations)){
						if (direction == 0){
							locationTuple.add(new PossibleCellLocationTuple(cell.getL, new Location(0,1)))
						}
						else if (direction == 1){
							locationTuple.add(new PossibleCellLocationTuple(cell.getL, new Location(-1,0)))
						}
						else if (direction == 2){
							locationTuple.add(new PossibleCellLocationTuple(cell.getL, new Location(0,-1)))
						}
						else if (direction == 3){
							locationTuple.add(new PossibleCellLocationTuple(cell.getL, new Location(1,0)))
						}
					}
					direction = direction + 1;
				}
			}
		}
		return locationTuple;
	}
	

}


