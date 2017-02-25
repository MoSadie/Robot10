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
	
	void updateNetworkTables() {
		SmartDashboard.putBoolean("Low", currentState == STATES.LOW);
		SmartDashboard.putBoolean("High", currentState == STATES.HIGH);
		SmartDashboard.putBoolean("PTO", currentState == STATES.PTO);
	}
	
	public void reset() {
		Util.consoleLog("Resetting to default state!");
		valveCenter.SetA();
		valveOuter.SetA();
		valvePTO.SetB();
		currentState = STATES.LOW;
		updateNetworkTables();
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
				valveCenter.SetB();
				valveOuter.SetB();
				break;
			case PTO:
				valveOuter.SetB();
				valvePTO.SetB();
				break;
			case NETURAL:
				valveOuter.SetB();
				break;
			default:
				return;
			}
			break;
		case LOW:
			switch (currentState) {
			case HIGH:
				valveOuter.SetA();
				valveCenter.SetA();
				break;
			case LOW:
				Util.consoleLog("Not changing gears! Trying to change to gear already set!");
				return;
			case PTO:
				valveOuter.SetA();
				valveCenter.SetA();
				valvePTO.SetB();
				break;
			case NETURAL:
				valveCenter.SetA();
				break;
			default:
				return;
			}
			break;
		case PTO:
			switch (currentState) {
			case HIGH:
				valveOuter.SetA();
				valvePTO.SetA();
				break;
			case LOW:
				valveCenter.SetB();
				valveOuter.SetB();
				valveOuter.SetA();
				valvePTO.SetA();
				break;
			case PTO:
				Util.consoleLog("Not changing gears! Trying to change to gear already set!");
				return;
			case NETURAL:
				valvePTO.SetA();
				break;
			default:
				return;
			}
			break;
		case NETURAL:
			switch (currentState) {
			case HIGH:
				valveOuter.SetA();
				break;
			case LOW:
				valveOuter.SetB();
				valveCenter.SetA();
				break;
			case PTO:
				valvePTO.SetB();
				break;
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
		updateNetworkTables();
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
