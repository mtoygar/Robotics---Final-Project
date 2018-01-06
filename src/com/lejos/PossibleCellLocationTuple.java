public class PossibleCellLocationTuple {
	
	//May add one more location(absolute)
	Location l, direction;
	
	public PossibleCellLocationTuple() {
		super();
	}

	public PossibleCellLocationTuple(Location l, Location direction){
		super();
		this.l = l;
		this.direction = direction;
	}

	public void setL(Location l) {
		this.l = l;
	}
	
	public Location getL() {
		return l;
	}

	public void setDirection(Location direction) {
		this.direction = direction;
	}
	
	public Location getDirection() {
		return direction;
	}

}