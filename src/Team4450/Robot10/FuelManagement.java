package Team4450.Robot10;

import Team4450.Lib.Util;
import edu.wpi.first.wpilibj.Talon;

public class FuelManagement {
	
	Talon intakeMotor, shooterMotor;
	static final double SHOOTER_POWER = 0.75; //TODO Find correct value
	static final double INTAKE_POWER = 0.76; //TODO Find correct value
	
	private boolean shooting = false;
	private boolean intaking = false;
	
	public FuelManagement() {
		intakeMotor = new Talon(0);
		shooterMotor = new Talon(2);
	}
	
	public void dispose() {
		if (intakeMotor != null) intakeMotor.free();
		if (shooterMotor != null) shooterMotor.free();
		}
	
	public void prepareToShoot() {
		if(!shooting) {
			shooting = true;
			shooterMotor.set(SHOOTER_POWER);
		} else {
			Util.consoleLog("Attempted prepareToShoot while already shooting!");
		}
	}
	
	public void endShoot() {
		if (shooting) {
			shooterMotor.set(0);
			shooting = false;
		} else {
			Util.consoleLog("Attempted endShoot while not shooting!");
		}
	}
	
	public void intake() {
		if (!intaking) {
			intaking = true;
			intakeMotor.set(INTAKE_POWER);
		} else {
			Util.consoleLog("Attempted intake while already intaking!");
		}
	}
	
	public void stopIntake() {
		if (intaking) {
			intakeMotor.set(0);
			intaking = false;
		} else {
			Util.consoleLog("Attempted stopIntake while not intaking!");
		}
	}
}
