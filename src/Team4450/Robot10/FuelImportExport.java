package Team4450.Robot10;

import com.ctre.CANTalon;

import Team4450.Lib.Util;

public class FuelImportExport {
	
	Robot robot;
	CANTalon intakeMotor, shooterMotor1, shooterMotor2;
	static final double SHOOTER_POWER = 0.75; //TODO Find correct value
	static final double INTAKE_POWER = 0.76; //TODO Find correct value
	
	private boolean shooting = false;
	private boolean intaking = false;
	
	public FuelImportExport(Robot robot) {
		this.robot = robot;
		robot.InitializeCANTalon(intakeMotor = new CANTalon(0)); //FIXME Get correct ID number
		robot.InitializeCANTalon(shooterMotor1 = new CANTalon(1)); //FIXME Get correct ID number
		robot.InitializeCANTalon(shooterMotor2 = new CANTalon(2)); //FIXME Get correct ID number
		shooterMotor2.reverseOutput(true);
	}
	
	public void dispose() {
		if (intakeMotor != null) intakeMotor.delete();
		if (shooterMotor1 != null) shooterMotor1.delete();
		if (shooterMotor2 != null) shooterMotor2.delete();
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
