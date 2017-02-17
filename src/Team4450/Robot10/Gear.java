package Team4450.Robot10;

import Team4450.Lib.Util;
import Team4450.Lib.ValveDA;
import edu.wpi.first.wpilibj.Talon;

public class Gear {
	
	Talon intakeMotor;
	ValveDA valve;
	
	enum STATES { EJECT, STOP, INTAKE };
	private STATES state = STATES.STOP;
	
	static final double INTAKE_SPEED = 0.5;
	
	private static Gear gear = null;
	public static Gear getInstance() {
		if (gear == null) {
			gear = new Gear();
		}
		
		return gear;
	}
	
	private Gear() {
		intakeMotor = new Talon(1);
		valve = new ValveDA(0); //FIXME Get correct IDs OR IS THIS EVEN STILL HOW IT WORKS?
	}
	
	public void dispose() {
		if (intakeMotor != null) intakeMotor.free();
		if (valve != null) valve.dispose();
		gear = null;
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
