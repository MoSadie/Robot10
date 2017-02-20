package Team4450.Robot10;

import Team4450.Lib.*;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GearBox {
	
	private static GearBox gearBox = null;
	
	private ValveDA valveOuter, valvePTO, valveCenter;
	
	private Encoder encoder = new Encoder(0,1,true, EncodingType.k4X);
	
	public enum STATES { HIGH , LOW , PTO, NETURAL };
	private STATES currentState;
	
	public static GearBox getInstance() {
		if (gearBox == null) {
			gearBox = new GearBox();
		}
		return gearBox;
	}
	
	private GearBox() {
		Util.consoleLog();
		valveCenter = new ValveDA(4);
		valveOuter = new ValveDA(0);
		valvePTO = new ValveDA(2);
	}
	
	public void dispose() {
		if (valveCenter != null) valveCenter.dispose();
		if (valveOuter != null) valveOuter.dispose();
		if (valvePTO != null) valvePTO.dispose();
		if (encoder != null) encoder.free();
		gearBox = null;
	}
	
	public void reset() {
		Util.consoleLog("Resetting to default state!");
		valveCenter.SetA();
		valveOuter.SetA();
		valvePTO.SetB();
		SmartDashboard.putBoolean("Low", true);
		SmartDashboard.putBoolean("High", false);
		SmartDashboard.putBoolean("PTO", false);
		currentState = STATES.LOW;
	}
	
	public void setGear(STATES gearToShiftTo) {
		Util.consoleLog("Current Gear: " + currentState.toString() + " Target Gear: " + gearToShiftTo.toString());
		switch(gearToShiftTo) {
		case HIGH:
			switch (currentState) {
			case HIGH:
				Util.consoleLog("Not changing gears! Trying to change to gear already set!");
				return;
			case LOW:
				SmartDashboard.putBoolean("Low", false);
				valveCenter.SetB();
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
				valveCenter.SetA();
				SmartDashboard.putBoolean("Low", true);
				break;
			case LOW:
				Util.consoleLog("Not changing gears! Trying to change to gear already set!");
				return;
			case PTO:
				SmartDashboard.putBoolean("PTO", false);
				valveOuter.SetA();
				valveCenter.SetA();
				valvePTO.SetB();
				SmartDashboard.putBoolean("Low", true);
				break;
			case NETURAL:
				SmartDashboard.putBoolean("Netural", false);
				valveCenter.SetA();
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
				valveCenter.SetB();
				valveOuter.SetB();
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
				valveCenter.SetA();
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
	
	public boolean isPTO() {
		if (currentState == STATES.PTO) 
			return true;
		else
			return false;
	}
	
	public STATES getCurrentState() {
		return currentState;
	}
	
	public Encoder getEncoder() {
		return encoder;
	}
}
