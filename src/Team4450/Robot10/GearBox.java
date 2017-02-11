package Team4450.Robot10;

import Team4450.Lib.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GearBox {
	
	private final Robot robot;
	
	private static GearBox gearBox = null;
	
	private ValveSA valveCenter;
	private ValveDA valveOuter, valvePTO;
	
	private static final boolean THREE_STAGE = true;
	
	public enum STATES { HIGH , LOW , PTO, NETURAL };
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
		valveCenter = new ValveSA(0); //FIXME Get correct ID number
		valveOuter = new ValveDA(0); //FIXME Get correct ID number
		valvePTO = new ValveDA(0); //FIXME Get correct ID number
	}
	
	public void dispose() {
		if (valveCenter != null) valveCenter.dispose();
		if (valveOuter != null) valveOuter.dispose();
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
				valveCenter.Close();
				//Timer.delay(0.1); //TODO is needed?
				valveOuter.SetB();
				SmartDashboard.putBoolean("High", true);
				break;
			case PTO:
				SmartDashboard.putBoolean("PTO", false);
				valveOuter.SetB();
				valvePTO.SetB();
				SmartDashboard.putBoolean("High", true);
				break;
			case NETURAL:
				SmartDashboard.putBoolean("Netural", false);
				valveOuter.SetB();
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
				valveOuter.SetA();
				valveCenter.Open();
				SmartDashboard.putBoolean("Low", true);
				break;
			case LOW:
				Util.consoleLog("Not changing gears! Trying to change to gear already set!");
				return;
			case PTO:
				SmartDashboard.putBoolean("PTO", false);
				valveOuter.SetA();
				//Timer.delay(0.1); //TODO is needed?
				valveCenter.Open();
				valvePTO.SetB();
				SmartDashboard.putBoolean("Low", true);
				break;
			case NETURAL:
				SmartDashboard.putBoolean("Netural", false);
				valveCenter.Open();
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
				valveOuter.SetA();
				valvePTO.SetA();
				SmartDashboard.putBoolean("PTO", true);
				break;
			case LOW:
				SmartDashboard.putBoolean("Low", false);
				valveOuter.SetB();
				//Timer.delay(0.1); //TODO is needed?
				valveOuter.SetA();
				valvePTO.SetA();
				SmartDashboard.putBoolean("PTO", true);
				break;
			case PTO:
				Util.consoleLog("Not changing gears! Trying to change to gear already set!");
				return;
			case NETURAL:
				SmartDashboard.putBoolean("Netural", false);
				valvePTO.SetA();
				SmartDashboard.putBoolean("PTO", true);
				break;
			default:
				return;
			}
			break;
		case NETURAL:
			switch (currentState) {
			case HIGH:
				SmartDashboard.putBoolean("High", false);
				valveOuter.SetA();
				SmartDashboard.putBoolean("Netural", true);
				break;
			case LOW:
				SmartDashboard.putBoolean("Low", false);
				valveOuter.SetB();
				//Timer.delay(0.1); //TODO is needed?
				valveCenter.Open();
				SmartDashboard.putBoolean("Netural", true);
				break;
			case PTO:
				SmartDashboard.putBoolean("PTO", false);
				valvePTO.SetB();
				SmartDashboard.putBoolean("Netural",true);
			case NETURAL:
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
