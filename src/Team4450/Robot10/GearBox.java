package Team4450.Robot10;

import Team4450.Lib.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GearBox {
	
	private final Robot robot;
	
	private static GearBox gearBox = null;
	
	private ValveSA valve1;
	private ValveDA valve2;
	
	public enum STATES { HIGH , LOW , PTO };
	private STATES currentState;
	
	public static GearBox getInstance(Robot robot) {
		if (gearBox == null) {
			gearBox = new GearBox(robot);
		}
		return gearBox;
	}
	
	public static GearBox getInstance() {
		return gearBox;
	}
	
	private GearBox(Robot robot) {
		Util.consoleLog();
		this.robot = robot;
		valve1 = new ValveSA(0); //FIXME Get correct ID number
		valve2 = new ValveDA(0); //FIXME GEt correct ID number
	}
	
	public void dispose() {
		if (valve1 != null) valve1.dispose();
		if (valve2 != null) valve2.dispose();
		gearBox = null;
	}
	
	public void setGear(STATES gearToShiftTo) {
		switch(gearToShiftTo) {
		case HIGH:
			switch (currentState) {
			case HIGH:
				Util.consoleLog("Not changing gears! Trying to change to gear already set!");
				return;
			case LOW:
				SmartDashboard.putBoolean("Low", false);
				SmartDashboard.putBoolean("High", true);
				break;
			case PTO:
				SmartDashboard.putBoolean("PTO", false);
				SmartDashboard.putBoolean("High", true);
				break;
			default:
				return;
			
			}
			break;
		case LOW:
			switch (currentState) {
			case HIGH:
				SmartDashboard.putBoolean("High", false);
				SmartDashboard.putBoolean("Low", true);
				break;
			case LOW:
				Util.consoleLog("Not changing gears! Trying to change to gear already set!");
				return;
			case PTO:
				SmartDashboard.putBoolean("PTO", false);
				SmartDashboard.putBoolean("Low", true);
				break;
			default:
				return;
			}
			break;
		case PTO:
			switch (currentState) {
			case HIGH:
				SmartDashboard.putBoolean("High", false);
				SmartDashboard.putBoolean("PTO", true);
				break;
			case LOW:
				SmartDashboard.putBoolean("Low", false);
				SmartDashboard.putBoolean("PTO", true);
				break;
			case PTO:
				Util.consoleLog("Not changing gears! Trying to change to gear already set!");
				return;
			default:
				return;
			}
			break;
		default:
			Util.consoleLog("setGear Failed! Bad input.");
			return;
		
		}
		currentState = gearToShiftTo;
	}
	
	public boolean getPTO() {
		if (currentState == STATES.PTO) 
			return true;
		else
			return false;
	}
	
	public STATES getCurrentState() {
		return currentState;
	}
}
