
package Team4450.Robot10;

import java.lang.Math;

import Team4450.Lib.*;
import Team4450.Lib.JoyStick.*;
import Team4450.Lib.LaunchPad.*;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.AnalogInput;

class Teleop
{
	private final Robot 		robot;
	public  JoyStick			rightStick, leftStick, utilityStick;
	public  LaunchPad			launchPad;
	private boolean				autoTarget = false;
	public boolean				invertDrive = false;
	public boolean				oneStickDriving = false;

	//Constuctor for test
	AnalogInput psiInput = new AnalogInput(0);
	
	// Constructor.

	Teleop(Robot robot)
	{
		Util.consoleLog();

		this.robot = robot;
	}

	// Free all objects that need it.

	void dispose()
	{
		Util.consoleLog();
		
		if (leftStick != null) leftStick.dispose();
		if (rightStick != null) rightStick.dispose();
		if (utilityStick != null) utilityStick.dispose();
		if (launchPad != null) launchPad.dispose();
		if (psiInput != null) psiInput.free();
	}

	void OperatorControl()
	{
		double	rightY, leftY, utilX;

		// Motor safety turned off during initialization.
		robot.robotDrive.setSafetyEnabled(false);

		Util.consoleLog();

		LCD.printLine(1, "Mode: OperatorControl");
		LCD.printLine(2, "All=%s, Start=%d, FMS=%b", robot.alliance.name(), robot.location, robot.ds.isFMSAttached());

		// Initial setting of air valves.

		GearBox.getInstance().reset();
		Gear.getInstance().reset();
		FuelManagement.getInstance().reset();

		// Configure LaunchPad and Joystick event handlers.

		launchPad = new LaunchPad(robot.launchPad, LaunchPadControlIDs.BUTTON_BLUE, this);

		LaunchPadControl lpControl = launchPad.AddControl(LaunchPadControlIDs.ROCKER_LEFT_BACK);
		lpControl.controlType = LaunchPadControlTypes.SWITCH;

		LaunchPadControl lpControl2 = launchPad.AddControl(LaunchPadControlIDs.ROCKER_LEFT_FRONT);
		lpControl2.controlType = LaunchPadControlTypes.SWITCH;

		launchPad.AddControl(LaunchPadControlIDs.BUTTON_YELLOW);
		launchPad.AddControl(LaunchPadControlIDs.BUTTON_RED_RIGHT);
		launchPad.AddControl(LaunchPadControlIDs.BUTTON_BLUE_RIGHT);
		launchPad.AddControl(LaunchPadControlIDs.BUTTON_RED);
		launchPad.addLaunchPadEventListener(new LaunchPadListener());
		launchPad.Start();

		leftStick = new JoyStick(robot.leftStick, "LeftStick", JoyStickButtonIDs.TRIGGER, this);
		leftStick.addJoyStickEventListener(new LeftStickListener());
		leftStick.Start();

		rightStick = new JoyStick(robot.rightStick, "RightStick", JoyStickButtonIDs.TOP_LEFT, this);
		rightStick.AddButton(JoyStickButtonIDs.TRIGGER);
		rightStick.AddButton(JoyStickButtonIDs.TOP_BACK);
		rightStick.addJoyStickEventListener(new RightStickListener());
		rightStick.Start();

		utilityStick = new JoyStick(robot.utilityStick, "UtilityStick", JoyStickButtonIDs.TRIGGER, this);
		utilityStick.AddButton(JoyStickButtonIDs.TOP_LEFT);
		utilityStick.AddButton(JoyStickButtonIDs.TOP_RIGHT);
		utilityStick.AddButton(JoyStickButtonIDs.TOP_BACK);
		utilityStick.AddButton(JoyStickButtonIDs.TOP_MIDDLE);
		utilityStick.addJoyStickEventListener(new UtilityStickListener());
		utilityStick.Start();

		// Tighten up dead zone for smoother climber movement.
		utilityStick.deadZone = .05;

		// Set CAN Talon brake mode by rocker switch setting.
		// We do this here so that the Utility stick thread has time to read the initial state
		// of the rocker switch.
		if (robot.isComp) robot.SetCANTalonBrakeMode(lpControl.latchedState);

		// Set gyro to heading 0.
		//robot.gyro.reset();

		robot.navx.resetYaw();
		//robot.navx.dumpValuesToNetworkTables();

		// Motor safety turned on.
		robot.robotDrive.setSafetyEnabled(true);

		// Driving loop runs until teleop is over.

		while (robot.isEnabled() && robot.isOperatorControl())
		{
			// Get joystick deflection and feed to robot drive object
			// using calls to our JoyStick class.

			if (GearBox.getInstance().isPTO())
			{
				leftY = climbLogCorrection(utilityStick.GetY());

				rightY = stickLogCorrection(rightStick.GetY());
			}
			//else if (invertDrive) {
			//	rightY = stickLogCorrection(-rightStick.GetY());	// fwd/back right
			//	leftY = stickLogCorrection(-leftStick.GetY());	// fwd/back left				
			//}
			else
			{
				rightY = stickLogCorrection(rightStick.GetY());	// fwd/back right
				leftY = stickLogCorrection(leftStick.GetY());	// fwd/back left
			}

			utilX = utilityStick.GetX();

			LCD.printLine(4, "leftY=%.4f  rightY=%.4f utilX=%.4f", leftY, rightY, utilX);
			//LCD.printLine(6, "yaw=%.0f, total=%.0f, rate=%.3f", robot.navx.getYaw(), robot.navx.getTotalYaw(), robot.navx.getYawRate());

			// Set wheel motors.
			// Do not feed JS input to robotDrive if we are controlling the motors in automatic functions.
			
			if (!autoTarget) 
				if (!oneStickDriving) 
					robot.robotDrive.tankDrive(leftY, rightY);
				else
					robot.robotDrive.drive(rightStick.GetY(), rightStick.GetX());

			// End of driving loop.
			
			//Test of psi sensor
			final double Vn = 4.7058823529411764705882352941176;
			SmartDashboard.putNumber("AirPressure", (250*(psiInput.getVoltage()/Vn)-25));
			LCD.printLine(6, "PSI: %f Volt: %f", (250*(psiInput.getVoltage()/Vn)-25),psiInput.getVoltage());
			
			Timer.delay(.020);	// wait 20ms for update from driver station.
		}

		// End of teleop mode.

		Util.consoleLog("end");
	}

	// Map joystick y value of 0.0-1.0 to the motor working power range of approx 0.5-1.0

	private double stickCorrection(double joystickValue)
	{
		if (joystickValue != 0)
		{
			if (joystickValue > 0)
				joystickValue = joystickValue / 1.5 + .4;
			else
				joystickValue = joystickValue / 1.5 - .4;
		}

		return joystickValue;
	}

	// Custom base logrithim.
	// Returns logrithim base of the value.

	private double baseLog(double base, double value)
	{
		return Math.log(value) / Math.log(base);
	}

	// Map joystick y value of 0.0 to 1.0 to the motor working power range of approx 0.5 to 1.0 using
	// logrithmic curve.

	private double stickLogCorrection(double joystickValue)
	{
		double base = Math.pow(2, 1/3) + Math.pow(2, 1/3);

		if (joystickValue > 0)
			joystickValue = baseLog(base, joystickValue + 1);
		else if (joystickValue < 0)
			joystickValue = -baseLog(base, -joystickValue + 1);

		return joystickValue;
	}
	
	// Map joystick y value of 0.0 to 1.0 to the motor working power range of approx 0.5 to 1.0 using
	// logrithmic curve for climbing.

	private double climbLogCorrection(double joystickValue)
	{
		double base = 2.2239800905693155211653633767222;//Math.pow(13.5, (1/3));
		
			
		if (joystickValue > 0)
			joystickValue = baseLog(base, joystickValue + 1);
		else if (joystickValue < 0)
			joystickValue = baseLog(base, -joystickValue + 1);
		
		return joystickValue;
	}

	// Transmission control functions.
	//For legacy code, should use GearBox class directly

	//--------------------------------------

	void shifterLow()
	{
		Util.consoleLog();

		GearBox.getInstance().setGear(GearBox.STATES.LOW);
	}

	void shifterHigh()
	{
		Util.consoleLog();

		GearBox.getInstance().setGear(GearBox.STATES.HIGH);
	}

	//--------------------------------------
	void ptoDisable()
	{
		Util.consoleLog();

		GearBox.getInstance().setGear(GearBox.STATES.LOW);
	}

	void ptoEnable()
	{
		Util.consoleLog();

		GearBox.getInstance().setGear(GearBox.STATES.PTO);
	}

	// Handle LaunchPad control events.

	public class LaunchPadListener implements LaunchPadEventListener 
	{
		public void ButtonDown(LaunchPadEvent launchPadEvent) 
		{
			LaunchPadControl	control = launchPadEvent.control;

			Util.consoleLog("%s, latchedState=%b", control.id.name(),  control.latchedState);

			switch(control.id)
			{
			case BUTTON_BLUE:
				if (!GearBox.getInstance().isPTO())
					ptoEnable();
				else
					ptoDisable();

				break;

			case BUTTON_RED:
				if (Gear.getInstance().getWristState() == Gear.WRIST_STATES.RETRACT)
					Gear.getInstance().extendWrist();
				else
					Gear.getInstance().retractWrist();
				break;

			case BUTTON_BLUE_RIGHT:
				if (control.latchedState)
					Gear.getInstance().setElevator(Gear.ELEVATOR_STATES.DOWN);
				else
					Gear.getInstance().setElevator(Gear.ELEVATOR_STATES.UP);
				break;



			case BUTTON_RED_RIGHT:
				if (Gear.getInstance().getWristState() == Gear.WRIST_STATES.RETRACT)
					Gear.getInstance().extendWrist();
				else
					Gear.getInstance().retractWrist();
				break;

			case BUTTON_YELLOW:
				Util.consoleLog("isAutoGear: " + Gear.getInstance().isAutoGear);
				if (!Gear.getInstance().isAutoGear)
					Gear.getInstance().startAutoIntake();
				else
					Gear.getInstance().killAutoIntake();
				break;


			default:
				break;
			}
		}

		public void ButtonUp(LaunchPadEvent launchPadEvent) 
		{
			//Util.consoleLog("%s, latchedState=%b", launchPadEvent.control.name(),  launchPadEvent.control.latchedState);
		}

		public void SwitchChange(LaunchPadEvent launchPadEvent) 
		{
			LaunchPadControl	control = launchPadEvent.control;

			Util.consoleLog("%s", control.id.name());

			switch(control.id)
			{
			// Set CAN Talon brake mmode.
			case ROCKER_LEFT_BACK:
				if (control.latchedState)
					robot.SetCANTalonBrakeMode(false);	// coast
				else
					robot.SetCANTalonBrakeMode(true);	// brake

				break;

			case ROCKER_LEFT_FRONT:
				if (robot.cameraThread != null) robot.cameraThread.ChangeCamera();

			default:
				break;
			}
		}
	}

	// Handle Right JoyStick Button events.

	private class RightStickListener implements JoyStickEventListener 
	{

		public void ButtonDown(JoyStickEvent joyStickEvent) 
		{
			JoyStickButton	button = joyStickEvent.button;

			Util.consoleLog("%s, latchedState=%b", button.id.name(),  button.latchedState);

			switch(button.id)
			{
			case TOP_LEFT:
				robot.cameraThread.ChangeCamera();
				break;
				
			case TRIGGER:
				oneStickDriving = !oneStickDriving;
				break;
				
			case TOP_BACK:
				int angle = Vision.getInstance().getOutput().getPegX();
				Util.consoleLog("angle=%d", angle);
				break;

			default:
				break;
			}
		}

		public void ButtonUp(JoyStickEvent joyStickEvent) 
		{
			//Util.consoleLog("%s", joyStickEvent.button.name());
		}
	}

	// Handle Left JoyStick Button events.

	private class LeftStickListener implements JoyStickEventListener 
	{
		public void ButtonDown(JoyStickEvent joyStickEvent) 
		{
			JoyStickButton	button = joyStickEvent.button;

			Util.consoleLog("%s, latchedState=%b", button.id.name(),  button.latchedState);

			switch(button.id)
			{
			case TRIGGER:
				if (button.latchedState)
					shifterHigh();
				else
					shifterLow();

				break;

			default:
				break;
			}
		}

		public void ButtonUp(JoyStickEvent joyStickEvent) 
		{
			//Util.consoleLog("%s", joyStickEvent.button.name());
		}
	}

	// Handle Utility JoyStick Button events.

	private class UtilityStickListener implements JoyStickEventListener 
	{
		public void ButtonDown(JoyStickEvent joyStickEvent) 
		{
			JoyStickButton	button = joyStickEvent.button;

			Util.consoleLog("%s, latchedState=%b", button.id.name(),  button.latchedState);

			switch(button.id)
			{
			// Trigger starts shoot sequence.
			case TRIGGER:
				if (FuelManagement.getInstance().getShooting())
					FuelManagement.getInstance().endShoot();
				else if (FuelManagement.getInstance().getPreparedToShoot())
					FuelManagement.getInstance().shoot();
				break;

			case TOP_LEFT:
				if (!FuelManagement.getInstance().getPreparedToShoot())
					FuelManagement.getInstance().prepareToShoot();
				else
					FuelManagement.getInstance().endShoot();
				break;

			case TOP_RIGHT:
				if (button.latchedState)
					FuelManagement.getInstance().intake();
				else
					FuelManagement.getInstance().stopIntake();
				break;

			case TOP_MIDDLE:
				switch(Gear.getInstance().getIntakeState()) {
				case INTAKE:
					Gear.getInstance().stopIntake();
					break;
				case STOP:
					Gear.getInstance().startIntake();
					break;
				case EJECT:
					Gear.getInstance().stopIntake();
					break;

				}
				break;

			case TOP_BACK:
				switch(Gear.getInstance().getIntakeState()) {
				case INTAKE:
					Gear.getInstance().stopIntake();
					break;
				case STOP:
					Gear.getInstance().reverseIntake();
					break;
				case EJECT:
					Gear.getInstance().stopIntake();
					break;

				}
				break;

			default:
				break;
			}
		}

		public void ButtonUp(JoyStickEvent joyStickEvent) 
		{
			//Util.consoleLog("%s", joyStickEvent.button.id.name());
		}
	}
}
