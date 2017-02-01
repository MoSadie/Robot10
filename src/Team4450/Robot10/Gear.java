package Team4450.Robot10;

import com.ctre.CANTalon;

import Team4450.Lib.Util;
import Team4450.Lib.ValveDA;

public class Gear {
	
	Robot robot;
	CANTalon intakeMotor;
	ValveDA valve;
	
	private boolean intaking = false;
	
	static final double INTAKE_SPEED = 0.5;
	
	public Gear(Robot robot) {
		this.robot = robot;
		robot.InitializeCANTalon(intakeMotor = new CANTalon(3)); //FIXME Get correct ID
		valve = new ValveDA(0); //FIXME Get correct IDs
	}
	
	public void startIntake() {
		if (!intaking) {
			intakeMotor.set(INTAKE_SPEED);
		} else {
			Util.consoleLog("Tried to startIntake while already intaking!");
		}
	}
	
	public void stopIntake() {
		if (intaking) {
			intakeMotor.set(0);
		} else {
			Util.consoleLog("Tried to stopIntake while not intaking!");
		}
	}
	
	public void lowerHolder() {
		valve.SetA(); //TODO Confirm this
	}
	
	public void raiseHolder() {
		valve.SetB(); //TODO Confirm this
	}
}
