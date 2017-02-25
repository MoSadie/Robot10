package Team4450.Robot10;

import Team4450.Lib.Util;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class FuelManagement {

	Talon shooterMotor, feederMotor, indexerMotor;
	Spark intakeMotor;
	static final double SHOOTER_POWER = .80; //TODO Find tuned value
	static final double FEED_POWER = 0.30; //TODO Find tuned value
	static final double INTAKE_POWER = 0.50; //TODO Find tuned value
	static final double INDEX_POWER = 0.50; //TODO Find tuned value

	enum SHOOTER_STATES { STOP, PREPARED, SHOOTING, REVERSE };
	private SHOOTER_STATES shooter = SHOOTER_STATES.STOP;

	enum INTAKE_STATES { IN, STOP, REVERSE };
	private INTAKE_STATES intaking = INTAKE_STATES.STOP;

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
		if(shooter != SHOOTER_STATES.PREPARED) {
			if (shooter != SHOOTER_STATES.SHOOTING) {
				shooter = SHOOTER_STATES.PREPARED;
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
		if (shooter == SHOOTER_STATES.PREPARED) {
			shooter = SHOOTER_STATES.SHOOTING;
			feederMotor.set(FEED_POWER);
			indexerMotor.set(INDEX_POWER);
			SmartDashboard.putBoolean("Feeder", true);
		}
		else {
			Util.consoleLog("Attempted to shoot while not prepared!");
		}
	}

	public void endShoot() {
		if (shooter == SHOOTER_STATES.SHOOTING) {
			shooterMotor.set(0);
			feederMotor.set(0);
			indexerMotor.set(0);
			shooter = SHOOTER_STATES.STOP;
			SmartDashboard.putBoolean("ShooterMotor", false);
			SmartDashboard.putBoolean("Feeder", false);
		} else if (shooter == SHOOTER_STATES.PREPARED) {
			shooterMotor.set(0);
			shooter = SHOOTER_STATES.STOP;
		} else {
			Util.consoleLog("Attempted endShoot while not shooting!");
		}
	}

	public void intake() {
		if (intaking != INTAKE_STATES.IN) {
			intaking = INTAKE_STATES.IN;
			intakeMotor.set(INTAKE_POWER);
			SmartDashboard.putBoolean("BallPickupMotor", true);
		} else {
			Util.consoleLog("Attempted intake while already intaking!");
		}
	}

	public void stopIntake() {
		if (intaking != INTAKE_STATES.STOP) {
			intakeMotor.set(0);
			intaking = INTAKE_STATES.STOP;
			SmartDashboard.putBoolean("BallPickupMotor", false);
		} else {
			Util.consoleLog("Attempted stopIntake while not intaking!");
		}
	}

	public void reverseFeeder() {
		if (shooter != SHOOTER_STATES.REVERSE) {
			feederMotor.set(-FEED_POWER);
			shooter = SHOOTER_STATES.REVERSE;
		} else {
			Util.consoleLog("Attempted reversing feeder while already doing that");
		}
	}

	public boolean getShooting() {
		return shooter == SHOOTER_STATES.SHOOTING;
	}

	public boolean getPreparedToShoot() {
		return shooter == SHOOTER_STATES.PREPARED || shooter == SHOOTER_STATES.SHOOTING;
	}
}
