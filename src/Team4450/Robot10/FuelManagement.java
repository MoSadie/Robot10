package Team4450.Robot10;

import Team4450.Lib.Util;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class FuelManagement {

	Talon shooterMotor, feederMotor, indexerMotor;
	Spark intakeMotor;
	static double SHOOTER_POWER = .45;
	static double FEED_POWER = 0.30;
	static double INTAKE_POWER = 0.80;
	static double INDEX_POWER = 0.50;

	public static double				SHOOTER_RPM = 3000;
	public static double				PVALUE = .0025, IVALUE = .0025, DVALUE = .003;

	// Touchless Encoder single channel on dio port 0.
	public Counter		tlEncoder = new Counter(0);

	private final PIDController		shooterPidController;

	public ShooterSpeedSource		shooterSpeedSource = new ShooterSpeedSource(tlEncoder);

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
		if (Robot.IsClone) {
			// Clone robot PID defaults.
			SHOOTER_POWER = .45;
			SHOOTER_RPM = 3000;

			PVALUE = .002; 
			IVALUE = .002;
			DVALUE = .005; 
		}

		intakeMotor = new Spark(0);
		intakeMotor.setInverted(true); //TODO Check this
		shooterMotor = new Talon(1);
		feederMotor = new Talon(2);
		indexerMotor = new Talon(3);
		indexerMotor.setInverted(true);

		tlEncoder.reset();
		tlEncoder.setDistancePerPulse(1);
		tlEncoder.setPIDSourceType(PIDSourceType.kRate);
		shooterPidController = new PIDController(0.0, 0.0, 0.0, shooterSpeedSource, shooterMotor);
	}

	public void dispose() {
		if (intakeMotor != null) intakeMotor.free();
		if (shooterMotor != null) shooterMotor.free();
		if (feederMotor != null) feederMotor.free();
		if (indexerMotor != null) indexerMotor.free();
		if (tlEncoder != null) tlEncoder.free();
	}

	public void prepareToShoot() {
		if(shooter != SHOOTER_STATES.PREPARED) {
			if (shooter != SHOOTER_STATES.SHOOTING) {
				shooter = SHOOTER_STATES.PREPARED;
				if (SmartDashboard.getBoolean("PIDEnabled",false)) {
					holdShooterRPM(SmartDashboard.getNumber("HighSetting", SHOOTER_RPM));
				}
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
			feederMotor.set(-FEED_POWER);
			SmartDashboard.putBoolean("BallPickupMotor", true);
		} else {
			Util.consoleLog("Attempted intake while already intaking!");
		}
	}

	public void stopIntake() {
		if (intaking != INTAKE_STATES.STOP) {
			intakeMotor.set(0);
			feederMotor.set(0);
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

	/**
	 * Automatically hold shooter motor speed (rpm). Starts PID controller to
	 * manage motor power to maintain rpm target.
	 * @param rpm RPM to hold.
	 */
	private void holdShooterRPM(double rpm)
	{
		double pValue = SmartDashboard.getNumber("PValue", PVALUE);
		double iValue = SmartDashboard.getNumber("IValue", IVALUE);
		double dValue = SmartDashboard.getNumber("DValue", DVALUE);

		Util.consoleLog("%.0f  p=%.4f  i=%.4f  d=%.4f", rpm, pValue, iValue, dValue);

		// p,i,d values are a guess.
		// f value is the base motor speed, which is where (power) we start.
		// setpoint is target rpm converted to rev/sec.
		// The idea is that we apply power to get rpm up to set point and then maintain.
		//shooterPidController.setPID(0.001, 0.001, 0.0, 0.0); 
		shooterPidController.setPID(pValue, iValue, dValue, 0.0); 
		shooterPidController.setSetpoint(rpm / 60);		// setpoint is revolutions per second.
		shooterPidController.setPercentTolerance(5);	// 5% error.
		shooterPidController.setToleranceBuffer(4096);	// 4 seconds of averaging.
		shooterSpeedSource.reset();
		shooterPidController.enable();
	}

	// Encapsulate an encoder or counter so we could modify the rate returned to
	// the PID controller.
	public class ShooterSpeedSource implements PIDSource
	{
		private Encoder		encoder;
		private Counter		counter;
		private int			inversion = 1;
		private double		rpmAccumulator, rpmSampleCount;

		public ShooterSpeedSource(Encoder encoder)
		{
			this.encoder = encoder;
		}

		public ShooterSpeedSource(Counter counter)
		{
			this.counter = counter;
		}

		@Override
		public void setPIDSourceType(PIDSourceType pidSource)
		{
			if (encoder != null) encoder.setPIDSourceType(pidSource);
			if (counter != null) counter.setPIDSourceType(pidSource);
		}

		@Override
		public PIDSourceType getPIDSourceType()
		{
			if (encoder != null) return encoder.getPIDSourceType();
			if (counter != null) return counter.getPIDSourceType();

			return null;
		}

		public void setInverted(boolean inverted)
		{
			if (inverted)
				inversion = -1;
			else
				inversion = 1;
		}

		public int get()
		{
			if (encoder != null ) return encoder.get() * inversion;
			if (counter != null ) return counter.get() * inversion;

			return 0;
		}

		public double getRate()
		{
			// TODO: Some sort of smoothing could be done to damp out the
			// fluctuations in encoder rate.

			//			if (rpmSampleCount > 2048) rpmAccumulator = rpmSampleCount = 0;
			//			
			//			rpmAccumulator += encoder.getRate();
			//			rpmSampleCount += 1;
			//			
			//			return rpmAccumulator / rpmSampleCount;

			if (encoder != null) return encoder.getRate() * inversion;
			if (counter != null) return counter.getRate() * inversion;

			return 0;
		}

		/**
		 * Return the current rotational rate of the encoder or current value (count) to PID controllers.
		 * @return Encoder revolutions per second or current count.
		 */
		@Override
		public double pidGet()
		{
			if (encoder != null)
			{
				if (encoder.getPIDSourceType() == PIDSourceType.kRate)
					return getRate();
				else
					return get();
			}

			if (counter != null)
			{
				if (counter.getPIDSourceType() == PIDSourceType.kRate)
					return getRate();
				else
					return get();
			}

			return 0;
		}

		public void reset()
		{
			rpmAccumulator = rpmSampleCount = 0;

			if (encoder != null) encoder.reset();
			if (counter != null) counter.reset();
		}
	}
}
