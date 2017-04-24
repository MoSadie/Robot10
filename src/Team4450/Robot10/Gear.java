package Team4450.Robot10;

import com.ctre.CANTalon;

import Team4450.Lib.Util;
import Team4450.Lib.ValveDA;
import Team4450.Robot10.Gear.ELEVATOR_STATES;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Gear {

	CANTalon intakeMotor;
	ValveDA wristValve, elevatorValve;

	enum STATES { EJECT, STOP, INTAKE };
	private STATES state = STATES.STOP;

	enum ELEVATOR_STATES { UP, DOWN };
	private ELEVATOR_STATES elevator_state;

	enum WRIST_STATES {EXTEND, RETRACT};
	private WRIST_STATES wrist_state;
	
	static final double INTAKE_SPEED = 0.5;
	
	public boolean isAutoGear = false;

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
		reset();
	}

	public void reset(){
		Util.consoleLog("Gear reset");
		intakeMotor.set(0);
		state = STATES.STOP;
		elevatorValve.SetA();
		elevator_state = ELEVATOR_STATES.UP;
		wristValve.SetA();
		wrist_state = WRIST_STATES.RETRACT;
	}

	public void dispose() {
		if (intakeMotor != null) intakeMotor.delete();
		if (wristValve != null) wristValve.dispose();
		gear = null;
	}
	
	void updateNetworkTables() {
		SmartDashboard.putBoolean("GearPickupDown", (elevator_state == ELEVATOR_STATES.DOWN));
		SmartDashboard.putBoolean("GearPickupMotor", (state == STATES.INTAKE || state == STATES.EJECT));
	}
	
	public void startIntake() {
		if (state != STATES.INTAKE) {
			intakeMotor.set(INTAKE_SPEED);
			state = STATES.INTAKE;
		} else {
			Util.consoleLog("Tried to startIntake while already intaking!");
		}
	}
	
	public void startAutoIntake() {
		Util.consoleLog("Starting Auto Intake of Gear");
		GearGrab gearGrab = GearGrab.getInstance();
		gearGrab.start();
	}
	
	public void killAutoIntake() {
		Util.consoleLog("Killing Auto Intake of Gear.");
		GearGrab.getInstance().finished = true;
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
				Util.consoleLog("Changing state from Up to Down!");
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
				Util.consoleLog("Changing state from Down to Up!");
				break;
			}
			break;
		}
		elevator_state = stateToSetTo;
		updateNetworkTables();
	}

	public void extendWrist() {
		if (wrist_state != WRIST_STATES.EXTEND) {
			wristValve.SetB();
			wrist_state = WRIST_STATES.EXTEND;
			Util.consoleLog("Extended Wrist");
		} else {
			Util.consoleLog("Attempted to Extend wrist while aready Extened!");
		}
	}

	public void retractWrist() {
		if (wrist_state != WRIST_STATES.RETRACT) {
			wristValve.SetA();
			wrist_state = WRIST_STATES.RETRACT;
			Util.consoleLog("Retracted Wrist");
		} else {
			Util.consoleLog("Attempted to Retract wrist while aready Retracted!");
		}
	}
	
	public CANTalon getIntakeMotor() {
		return intakeMotor;
	}
	
	public STATES getIntakeState() {
		return state;
	}
	
	public ELEVATOR_STATES getElevatorState() {
		return elevator_state;
	}
	
	public WRIST_STATES getWristState() {
		return wrist_state;
	}
}

class GearGrab extends Thread {
	private static final double TRIGGER_CURRENT = 10.1;
	public boolean finished = false;
	
	private static GearGrab gearGrab = null;
	public static GearGrab getInstance() {
		if (gearGrab == null)
			gearGrab = new GearGrab();
		return gearGrab;
	}
	
	private GearGrab() {
		setName("Grab Gear");
	}
	
	public static void dispose() {
		if (gearGrab.isInterrupted() == false) {
			gearGrab.interrupt();
		}
		gearGrab = null;
	}

	public void run() {
		Util.consoleLog("Auto pickup start");
		Gear.getInstance().isAutoGear = true;
		try {
		finished = false;
		Gear.getInstance().setElevator(ELEVATOR_STATES.DOWN);
		Gear.getInstance().extendWrist();
		Gear.getInstance().startIntake();
		while (!isInterrupted() && !finished) {
			//Util.consoleLog("Current: " + Gear.getInstance().getIntakeMotor().getOutputCurrent() + " Finished: " + finished);
			if (Gear.getInstance().getIntakeMotor().getOutputCurrent() >= TRIGGER_CURRENT) {
				finished = true;
			}
			try {
				sleep(50);
			} catch (InterruptedException e) {

			}
		}
		sleep(500);
		Gear.getInstance().retractWrist();
		Gear.getInstance().setElevator(ELEVATOR_STATES.UP);
		sleep(1000);
		Gear.getInstance().stopIntake();
		} catch (InterruptedException e) {
			Gear.getInstance().stopIntake();
			Gear.getInstance().retractWrist();
			Gear.getInstance().setElevator(ELEVATOR_STATES.UP);
		}
		catch (Exception e) {
			e.printStackTrace(Util.logPrintStream);
			Gear.getInstance().stopIntake();
			Gear.getInstance().retractWrist();
			Gear.getInstance().setElevator(ELEVATOR_STATES.UP);
		}
		Gear.getInstance().isAutoGear = false;
		Util.consoleLog("Auto pickup end");
		GearGrab.dispose();
	}
}
