package Team4450.Robot10;

import Team4450.Lib.Util;
import Team4450.Lib.ValveDA;

public class Climber {
	
	private final Robot robot;
	
	ValveDA ptoValve = new ValveDA(0); //FIXME Get correct ID number
	
	private boolean preparedToClimb = false;
	
	private static Climber climber = null;
	
	public static Climber getInstance() {
	if (climber == null) return null;
	return climber;
	}
	
	public static Climber getInstance(Robot robot) {
		if (climber == null) {
			climber = new Climber(robot);
		}
		return climber;
	}
	
	private Climber(Robot robot) {
		this.robot = robot;
	}
	
	public void dispose() {
		ptoValve.dispose();
		climber = null;
	}
	
	public void prepareClimb() {
		setPTO(true);
		preparedToClimb = true;
	}
	
	public void cancelClimb() {
		setPTO(false);
		preparedToClimb = false;
	}
	
	public void setPTO(boolean on) {
		if (on) 
			GearBox.getInstance(robot).setGear(GearBox.STATES.PTO);
		else
			GearBox.getInstance(robot).setGear(GearBox.STATES.HIGH); //TODO Determine which is faster.
	}
	
	public void climb(double value) {
		if (value >1 || value < -1) {
			Util.consoleLog("WARNING: Value is greater/less than 1/-1! Not going to do anything to prevent unexpected behavior!!");
			return;
		}
		if (!preparedToClimb) {
			Util.consoleLog("WARNING: Not prepared to climb! Please run Climber.prepareClimb() BEFORE calling this function!");
			return;
		}
		robot.LFCanTalon.set(value);
		robot.RFCanTalon.set(value);
		robot.LRCanTalon.set(value);
		robot.RRCanTalon.set(value);
	}
}
