package Team4450.Robot10;

import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import Team4450.Lib.CameraFeed;
import Team4450.Robot10.VisionPipelines.PegPipeline;

public class Vision {

	//Section 1: Making sure only one Vision2017 exists at any point.
	
	public static Vision getInstance() {
		if (vision == null) vision = new Vision();
		return vision;
	}
	private static Vision vision;
	
	//Section 2: Static Variables
	
	private static final double ERROR_RANGE = 5.0;
	
	//Section 3: Non-Static Variables
	
	private CameraFeed cameraFeed;
	private PegPipeline pipeline;
	
	//Section 4: Constructor
	
	private Vision() {
		cameraFeed = CameraFeed.getInstance();
		pipeline = new PegPipeline();
		
	}
	
	//Section 5: Non-Static Methods
	
	public int getPegX() {
		
		//Process the latest image
		pipeline.process(cameraFeed.getCurrentImage());
		
		// If there are results...
		if (!pipeline.filterContoursOutput().isEmpty()) {
			
			//These will hold the two boxes we are tracking
			Rect target1;
			Rect target2;
			
			//For each possible first target
			for (int i = 0; i < pipeline.filterContoursOutput().size(); i++) {
				//Set Target 1 to the candidate
				target1 = Imgproc.boundingRect(pipeline.filterContoursOutput().get(i));
				
				//For each of the targets...
				for (int i2 = 0; i2 < pipeline.filterContoursOutput().size(); i2++) {
					// That are not target 1...
					if (i == i2) continue;
					
					//Set target 2
					target2 = Imgproc.boundingRect(pipeline.filterContoursOutput().get(i2));
					
					//See if the width and height are within an acceptable difference.
					if (sameWithError(target1,target2,ERROR_RANGE)) {
						//If true, calculate the center of both rectangles.
						int centerX1 = target1.x + (target1.width / 2);
						int centerX2 = target2.x + (target2.width / 2);
						
						//Calculate the center x of those points and return that value
						return (centerX1 + ((centerX1-centerX2)/2));
					}
				}
			}
		}
		return 9001;
	}
	
	public static boolean sameWithError(Rect rect1, Rect rect2, double error) {
		boolean width = Math.abs(rect1.width-rect2.width) <= error;
		boolean height = Math.abs(rect1.height-rect2.height) <= error;
		return width && height;
	}
}
