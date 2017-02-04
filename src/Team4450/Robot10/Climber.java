package Team4450.Robot10;

import Team4450.Lib.Util;
import Team4450.Lib.ValveDA;

public class Climber {
	
	private final Robot robot;
	private final Teleop teleop;
	
	ValveDA ptoValve = new ValveDA(0); //FIXME Get correct ID number
	
	private boolean preparedToClimb = false;
	
	public Climber(Robot robot, Teleop teleop) {
		this.robot = robot;
		this.teleop = teleop;
	}
	
	public void prepareClimb() {
		
		preparedToClimb = true;
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
