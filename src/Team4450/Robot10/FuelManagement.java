package Team4450.Robot10;

import Team4450.Lib.Util;
import edu.wpi.first.wpilibj.Talon;

public class FuelManagement {
	
	Talon intakeMotor, shooterMotor1, shooterMotor2;
	static final double SHOOTER_POWER = 0.75; //TODO Find correct value
	static final double INTAKE_POWER = 0.76; //TODO Find correct value
	
	private boolean shooting = false;
	private boolean intaking = false;
	
	public static FuelManagement getInstance() {
		if (fuelManagement == null)
			fuelManagement = new FuelManagement();
		return fuelManagement;
	}
	private static FuelManagement fuelManagement;
	
	private FuelManagement() {
		intakeMotor = new Talon(0);
		shooterMotor1 = new Talon(2);
		shooterMotor2 = new Talon(3);
		shooterMotor2.setInverted(true); //FIXME Figure out what needs reversing
	}
	
	public void dispose() {
		if (intakeMotor != null) intakeMotor.free();
		if (shooterMotor1 != null) shooterMotor1.free();
		if (shooterMotor2 != null) shooterMotor2.free();
		}
	
	public void prepareToShoot() {
		if(!shooting) {
			shooting = true;
			shooterMotor1.set(SHOOTER_POWER);
			shooterMotor2.set(SHOOTER_POWER);
		} else {
			Util.consoleLog("Attempted prepareToShoot while already shooting!");
		}
	}
	
	public void shoot() {
		//FIXME Figure out how to make fuel go from hopper to shooter wheel using another wheel.
	}
	
	public void endShoot() {
		if (shooting) {
			shooterMotor1.set(0);
			shooterMotor2.set(0);
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
