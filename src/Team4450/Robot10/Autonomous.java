
package Team4450.Robot10;

import Team4450.Lib.*;
import Team4450.Robot10.Gear.ELEVATOR_STATES;
import Team4450.Robot10.Vision.VisionOutput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Autonomous
{
	private final Robot	robot;
	private GearBox gearBox;
	private Gear gear;
	private final int	program = (int) SmartDashboard.getNumber("AutoProgramSelect",0);
	
	Autonomous(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
		
		
		gearBox = GearBox.getInstance();
		gear = Gear.getInstance();
		Vision.getInstance(); //To init it before we need it.
	}

	public void dispose()
	{
		Util.consoleLog();
	}

	public void execute()
	{
		Util.consoleLog("Alliance=%s, Location=%d, Program=%d, FMS=%b", robot.alliance.name(), robot.location, program, robot.ds.isFMSAttached());
		LCD.printLine(2, "Alliance=%s, Location=%d, FMS=%b, Program=%d", robot.alliance.name(), robot.location, robot.ds.isFMSAttached(), program);

		robot.robotDrive.setSafetyEnabled(false);

        // Set gyro/NavX to heading 0.
        //robot.gyro.reset();
		robot.navx.resetYaw();
		
        // Wait to start motors so gyro will be zero before first movement.
        //Timer.delay(.50);

		switch (program)
		{
			case 0:		// No auto program.
				break;
				
			case 1:		// Drive forward to line and stop.
				autoDrive(-.70, 9000, true);
				
				break;
				
			case 2:		// Place gear center start.
				placeGearCenter(5800, false);
				
				break;
				
			case 3:		// Place gear left start.
				placeGearFromSide(true, false);
				
				break;
				
			case 4:		// Place gear right start.
				placeGearFromSide(false, false);
				
				break;
				
			case 5:		// Place gear left start with vision.
				placeGearFromSide(true, true);
				
				break;
				
			case 6:		// Place gear right start with vision.
				placeGearFromSide(false, true);
				
				break;
				
			case 7:		// Drive then shoot, left start.
				autoShoot(true);
				
				break;

			case 8:		// Drive then shoot, right start.
				autoShoot(false);
				
				break;
				
			case 9:		// Place gear center start with vision.
				placeGearCenter(5800, true);
				
				break;
				
			case 10:	// Test.
				//autoDrive(-.50, 9000, true);
				//autoDriveVision(-.40, 3000, true);
				//autoShoot();
				
				break;
		}
		
		Util.consoleLog("end");
	}

	private void placeGearCenter(int encoderCounts, boolean useVision)
	{
		Util.consoleLog("%d", encoderCounts);
		
		// Drive forward to peg and stop.
		
		if (!useVision)
			autoDrive(-.60, encoderCounts, true);
		else
			autoDriveVision(-.50, encoderCounts, true);
		
		// Start gear pickup motor in reverse.
		
		gear.reverseIntake();
		
		Timer.delay(.500);
		
		Gear.getInstance().setElevator(ELEVATOR_STATES.DOWN);
		
		Timer.delay(.500);
		
		// Drive backward a bit.

		autoDrive(.30, 1000, true);
		
		gear.stopIntake();
	}
	
	private void placeGearFromSide(boolean leftSide, boolean useVision)
	{
		Util.consoleLog("left side=%b", leftSide);
		
		// Drive forward to be on a 55 degree angle with side peg and stop.
		
		if (leftSide)
			autoDrive(-.50, 5600, true);
		else
			autoDrive(-.50, 5600, true);
		
		// rotate as right or left 90 degrees.
		
		if (leftSide)
			// Rotate right.
			autoRotate(-.60, 55);
		else
			// Rotate left
			autoRotate(.60, 55);
		
		if (useVision) Timer.delay(.5);
		
		// Place gear.
		
		placeGearCenter(5300, useVision);
		
		//if (leftSide)
		//{
		//	autoRotate(.60, 20);
		//
		//	FuelManagement.getInstance().prepareToShoot();
		//
		//	autoDrive(.60, 6200, true);
		//
		//	FuelManagement.getInstance().shoot();
		//}
	}
	
	private void autoShoot(boolean left)
	{
		double power = -.60;
		int		encoderCountsFwd = 6200, encoderCountsBack = 3400, angle = 26;
		
		Util.consoleLog("pwr=%f  encf=%d  encb=%d  angle=%d", power, encoderCountsFwd, encoderCountsBack, angle);
	
		// Drive forward to cross base line.
		
		autoDrive(power, encoderCountsFwd, true);
		
		// Turn to point at boiler.
		
		if (left)
			autoRotate(power, angle);	
		else
			autoRotate(-power, angle);
		
		// Back up to shooting location;
		
		autoDrive(-power, encoderCountsBack, true);
		
		// Shoot balls.
		
		Gear.getInstance().setElevator(Gear.ELEVATOR_STATES.DOWN);
		
		FuelManagement.getInstance().prepareToShoot();
		
		Timer.delay(3);
		
		FuelManagement.getInstance().intake();
		
		FuelManagement.getInstance().shoot();
		
		// Run until auto is over.
		
		while (robot.isEnabled()) Timer.delay(1);
	}
	
	// Auto drive in set direction and power for specified encoder count. Stops
	// with or without brakes on CAN bus drive system. Uses gyro/NavX to go straight.
	
	private void autoDrive(double power, int encoderCounts, boolean enableBrakes)
	{
		int		angle;
		double	gain = .03;
		
		Util.consoleLog("pwr=%f, count=%d, brakes=%b", power, encoderCounts, enableBrakes);

		if (robot.isComp) robot.SetCANTalonBrakeMode(enableBrakes);

		GearBox.getInstance().getEncoder().reset();
		robot.navx.resetYaw();
		
		while (robot.isEnabled() && robot.isAutonomous() && Math.abs(GearBox.getInstance().getEncoder().get()) < encoderCounts) 
		{
			LCD.printLine(3, "encoder=%d", GearBox.getInstance().getEncoder().get());
			
			// Angle is negative if robot veering left, positive if veering right when going forward.
			// It is opposite when going backward. Note that for this robot, - power means forward and
			// + power means backward.
			
			//angle = (int) robot.gyro.getAngle();
			angle = (int) robot.navx.getYaw();

			LCD.printLine(5, "angle=%d", angle);
			
			// Invert angle for backwards.
			
			if (power > 0) angle = -angle;
			
			//Util.consoleLog("angle=%d", angle);
			
			// Note we invert sign on the angle because we want the robot to turn in the opposite
			// direction than it is currently going to correct it. So a + angle says robot is veering
			// right so we set the turn value to - because - is a turn left which corrects our right
			// drift.
			
			robot.robotDrive.drive(power, -angle * gain);
			
			Timer.delay(.020);
		}

		robot.robotDrive.tankDrive(0, 0, true);				
	}
	
	// Auto rotate left or right the specified angle. Left/right from robots forward view.
	// Turn right, power is -
	// Turn left, power is +
	// angle of rotation is always +.
	
	private void autoRotate(double power, int angle)
	{
		Util.consoleLog("pwr=%.3f  angle=%d", power, angle);
		
		robot.navx.resetYaw();
		
		robot.robotDrive.tankDrive(power, -power);

		while (robot.isEnabled() && robot.isAutonomous() && Math.abs((int) robot.navx.getYaw()) < angle) {Timer.delay(.020);} 
		
		robot.robotDrive.tankDrive(0, 0);
	}
	
	// Auto drive in set direction and power for specified encoder count. Stops
	// with or without brakes on CAN bus drive system. Uses vision to drive to spring.
	
	private void autoDriveVision(double power, int encoderCounts, boolean enableBrakes)
	{
		int		angle;
		int prevDistance = 0;
		double	gain = .002, delay = .1, power2 = power;
		boolean driving = true;
		
		Util.consoleLog("pwr=%f, count=%d, brakes=%b", power, encoderCounts, enableBrakes);

		if (robot.isComp) robot.SetCANTalonBrakeMode(enableBrakes);

		GearBox.getInstance().getEncoder().reset();
		
		//robot.monitorDistanceThread.setDelay(delay);
		
		while (driving && robot.isEnabled() && robot.isAutonomous() && Math.abs(GearBox.getInstance().getEncoder().get()) < encoderCounts) 
		{
			LCD.printLine(3, "encoder=%d", GearBox.getInstance().getEncoder().get());
			Util.consoleLog("encoder=%d", GearBox.getInstance().getEncoder().get());
			// Angle is negative if robot veering left, positive if veering right when going forward.
			// It is opposite when going backward. Note that for this robot, - power means forward and
			// + power means backward.
			VisionOutput output = Vision.getInstance().getOutput();
			Util.consoleLog(output.toString());
			int pegX = output.getPegX();
			int distance = output.getDistance();
			angle = (CameraFeed.imageWidth/2) + Vision.getInstance().getOutput().getPegX();
			SmartDashboard.putBoolean("TargetLocked", true);
			if (pegX == 9001) {
				SmartDashboard.putBoolean("TargetLocked", false);
				angle = 0;
			}
			if (distance != 9001) {
				if (distance > 150 || distance < prevDistance) {
					driving = false;
					Util.consoleLog("End Auto Drive reason Distance(" + distance + ") > 150 or < prevDistance(" + prevDistance + ")");
				} else {
					driving = distance < 200 && robot.isEnabled();
				}
				
				if (!driving) {
					Util.consoleLog("Stopped Driving. Distance: %d", distance);
					continue;
				}
			} else {
				driving = robot.isEnabled() && Math.abs(GearBox.getInstance().getEncoder().get()) < encoderCounts;
				
				if (!driving) {
					Util.consoleLog("Stopped Driving. Encoder: %d", Math.abs(GearBox.getInstance().getEncoder().get()));
				}
			}
			
			LCD.printLine(5, "angle=%d distance=%d", angle, distance);
			
			if (distance != 0 && distance < 100)
			{
				delay = .10;
				power2 = power;
			}
			else if (distance != 0)
			{
				delay = .10;
				gain = .005;
				power2 = power / 2;
			}
			
			// Invert angle for backwards.
			
			if (power < 0) angle = -angle;
			
			//Util.consoleLog("angle=%d", angle);
			
			if (distance != 9001) prevDistance = distance;
			
			double pegOffset = (pegX-(CameraFeed.imageWidth/2)) * gain;
			
			// Offset is + if robot veering right, so we invert to - because - curve is to the left.
			// Offset is - if robot veering left, so we invert to + because + curve is to the right.

			if (pegOffset < -1)
				pegOffset = -1;
			else if (pegOffset > 1)
				pegOffset = 1;
			
			robot.robotDrive.drive(power2, -pegOffset);//TODO Try unInverting pegOffset
			
			Timer.delay(delay);
		}

		robot.robotDrive.tankDrive(0, 0, true);	
		
		//robot.monitorDistanceThread.setDelay(1.0);
	}
}