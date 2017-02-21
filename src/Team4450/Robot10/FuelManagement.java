package Team4450.Robot10;

import Team4450.Lib.Util;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class FuelManagement {

	Talon shooterMotor, feederMotor, indexerMotor;
	Spark intakeMotor;
	static final double SHOOTER_POWER = 1; //TODO Find tuned value
	static final double FEED_POWER = 0.50; //TODO Find tuned value
	static final double INTAKE_POWER = 0.50; //TODO Find tuned value
	static final double INDEX_POWER = 0.50; //TODO Find tuned value

	private boolean preparedToShoot = false;
	private boolean shooting = false;
	private boolean intaking = false;

	public static FuelManagement getInstance() {
		if (fuelManagement == null)
			fuelManagement = new FuelManagement();
		return fuelManagement;
	}
	private static FuelManagement fuelManagement;

	private FuelManagement() {
		intakeMotor = new Spark(0);
		intakeMotor.setInverted(true);
		shooterMotor = new Talon(1);
		feederMotor = new Talon(2);
		indexerMotor = new Talon(3);
		indexerMotor.setInverted(true);
	}

	public void dispose() {
		if (intakeMotor != null) intakeMotor.free();
		if (shooterMotor != null) shooterMotor.free();
		if (feederMotor != null) feederMotor.free();
		if (indexerMotor != null) indexerMotor.free();
	}

	public void prepareToShoot() {
		if(!preparedToShoot) {
			if (!shooting) {
				shooting = true;
				shooterMotor.set(SHOOTER_POWER);
				SmartDashboard.putBoolean("ShooterMotor", true);
			} else {
				Util.consoleLog("Attempted prepareToShoot while already shooting!");
			}
		} else {
			Util.consoleLog("Attempted prepareToShoot while already prepared to shoot!!");
		}
	}

	public void shoot() {
		if (preparedToShoot) {
			if (!shooting) {
				shooting = true;
				feederMotor.set(FEED_POWER);
				indexerMotor.set(INDEX_POWER);
				SmartDashboard.putBoolean("DispenserMotor", true);
			} else {
				Util.consoleLog("Attempted to shoot while already shooting!");
			}
		}
		else {
			Util.consoleLog("Attempted to shoot while not prepared!");
		}
	}

	public void endShoot() {
		if (shooting) {
			shooterMotor.set(0);
			feederMotor.set(0);
			indexerMotor.set(0);
			shooting = false;
			preparedToShoot = false;
			SmartDashboard.putBoolean("ShooterMotor", false);
			SmartDashboard.putBoolean("DispenserMotor", false);
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

	public boolean getShooting() {
		return shooting;
	}

	public boolean getPreparedToShoot() {
		return preparedToShoot;
	}
}
