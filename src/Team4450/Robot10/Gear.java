package Team4450.Robot10;

import com.ctre.CANTalon;

import Team4450.Lib.Util;
import Team4450.Lib.ValveDA;
import edu.wpi.first.wpilibj.Talon;

public class Gear {

	CANTalon intakeMotor;
	ValveDA wristValve, elevatorValve;

	enum STATES { EJECT, STOP, INTAKE };
	private STATES state = STATES.STOP;

	enum ELEVATOR_STATES { UP, DOWN };
	private ELEVATOR_STATES elevator_state;

	static final double INTAKE_SPEED = 0.5;

	private static Gear gear = null;
	public static Gear getInstance() {
		if (gear == null) {
			gear = new Gear();
		}

		return gear;
	}

	private Gear() {
		intakeMotor = new CANTalon(7);
		wristValve = new ValveDA(1,0);
		elevatorValve = new ValveDA(6);
	}

	public void reset(){
		intakeMotor.set(0);
		state = STATES.STOP;
		elevatorValve.SetA();
		elevator_state = ELEVATOR_STATES.DOWN;
	}

	public void dispose() {
		if (intakeMotor != null) intakeMotor.delete();
		if (wristValve != null) wristValve.dispose();
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

	public void setElevator(ELEVATOR_STATES stateToSetTo) {
		switch (stateToSetTo) {
		case DOWN:
			switch(elevator_state) {
			case DOWN:
				Util.consoleLog("Not changing elevator state because already at " + elevator_state.toString() + " position!");
				return;
			case UP:
				elevatorValve.SetB();
				break;
			}
			break;

		case UP:
			switch(elevator_state) {
			case UP:
				Util.consoleLog("Not changing elevator state because already at " + elevator_state.toString() + " position!");
				return;

			case DOWN:
				elevatorValve.SetA();
				break;
			}
			break;
		}
		elevator_state = stateToSetTo;
	}

	public void lowerWrist() {
		wristValve.SetB();
		startIntake();
	}

	public void raiseWrist() {
		wristValve.SetA();
		stopIntake();
	}
}

class GearGrab extends Thread {
	CANTalon intakeMotor;
	private static final double TRIGGER_CURRENT = 12.1;
	boolean finished = false;
	GearGrab(CANTalon intakeMotor) {
		setName("Grab Gear");
		this.intakeMotor = intakeMotor;
	}

	public void run() {
		finished = false;
		intakeMotor.set(Gear.INTAKE_SPEED);
		while (!isInterrupted() && !finished) {
			if (intakeMotor.getOutputCurrent() >= TRIGGER_CURRENT) {
				finished = true;
			}
		}
		intakeMotor.set(0);
	}
}
