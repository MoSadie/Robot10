package Team4450.Robot10;

import com.ctre.CANTalon;

import Team4450.Lib.Util;
import Team4450.Lib.ValveDA;

public class Gear {
	
	Robot robot;
	CANTalon intakeMotor;
	ValveDA valve;
	
	enum STATES { EJECT, STOP, INTAKE };
	private STATES state;
	
	static final double INTAKE_SPEED = 0.5;
	
	public Gear(Robot robot) {
		this.robot = robot;
		robot.InitializeCANTalon(intakeMotor = new CANTalon(3)); //FIXME Get correct ID
		valve = new ValveDA(0); //FIXME Get correct IDs
	}
	
	public void dispose() {
		if (intakeMotor != null) intakeMotor.delete();
		if (valve != null) valve.dispose();
	}
	
	public void startIntake() {
		if (state != STATES.INTAKE) {
			intakeMotor.set(INTAKE_SPEED);
			state = STATES.INTAKE;
		} else {
			Util.consoleLog("Tried to startIntake while already intaking!");
		}
	}
	
	public void stopIntake() {
		if (state != STATES.STOP) {
			intakeMotor.set(0);
			state = STATES.STOP;
		} else {
			Util.consoleLog("Tried to stopIntake while stopped!");
		}
	}
	
	public void reverseIntake() {
		if (state != STATES.EJECT) {
			intakeMotor.set(-INTAKE_SPEED);
			state = STATES.EJECT;
		} else {
			Util.consoleLog("Tried to reverseIntake while already ejecting!");
		}
	}
	
	public void lowerHolder() {
		valve.SetA(); //TODO Confirm this
	}
	
	public void raiseHolder() {
		valve.SetB(); //TODO Confirm this
	}
}
