package Team4450.Robot10;

import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import Team4450.Lib.CameraFeed;
import Team4450.Robot10.VisionPipelines.PegPipeline;

public class Vision {

	//Section 1: Making sure only 1 Vision2017 exists.
	
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
	
	public double getPegX() {
		pipeline.process(cameraFeed.getCurrentImage());
		if (!pipeline.filterContoursOutput().isEmpty()) {
			Rect target1;
			Rect target2;
			for (int i = 0; i < pipeline.filterContoursOutput().size(); i++) {
				target1 = Imgproc.boundingRect(pipeline.filterContoursOutput().get(i));
				for (int i2 = 0; i2 < pipeline.filterContoursOutput().size(); i2++) {
					if (i == i2) continue;
					target2 = Imgproc.boundingRect(pipeline.filterContoursOutput().get(i2));
					if (Math.abs(target1.width-target2.width) <= ERROR_RANGE && Math.abs(target1.height-target2.height) <= ERROR_RANGE) {
						double centerX1 = target1.x + (target1.width / 2);
						double centerX2 = target2.x + (target2.width / 2);
						return centerX1 + ((centerX1-centerX2)/2);
					}
				}
			}
		}
		return 9001;
	}
}
