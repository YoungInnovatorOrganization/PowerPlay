package org.firstinspires.ftc.teamcode.comp;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;

@Autonomous
public class Right extends LinearOpMode
{
    private Servo claw, rotator;
    private DcMotorEx lift;
    OpenCvCamera camera;
    CameraDetectionPipeline aprilTagDetectionPipeline;

    static final double FEET_PER_METER = 3.28084;

    // Lens intrinsics
    // UNITS ARE PIXELS
    // NOTE: this calibration is for the C920 webcam at 800x448.
    // You will need to do your own calibration for other configurations!
    double fx = 578.272;
    double fy = 578.272;
    double cx = 402.145;
    double cy = 221.506;

    // UNITS ARE METERS
    double tagsize = 0.166;

    // Tag ID 1,2,3 from the 36h11 family
    int LEFT = 1;
    int MIDDLE = 2;
    int RIGHT = 3;

    AprilTagDetection tagOfInterest = null;

    @Override
    public void runOpMode()
    {
        claw = hardwareMap.get(Servo.class,"claw");
        rotator = hardwareMap.get(Servo.class,"rotator");
        lift = hardwareMap.get(DcMotorEx.class,"lift");
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        aprilTagDetectionPipeline = new CameraDetectionPipeline(tagsize, fx, fy, cx, cy);

        camera.setPipeline(aprilTagDetectionPipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                camera.startStreaming(800,448, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode)
            {

            }
        });
        telemetry.setMsTransmissionInterval(50);

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        Pose2d startPose = new Pose2d(36, -64.5, 270);
        drive.setPoseEstimate(startPose);

        TrajectorySequence startSeq = drive.trajectorySequenceBuilder(startPose)
                .lineTo(
                        new Vector2d(38.5, 3),
                        SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .addTemporalMarker(0, () -> {
                    lift.setPower(-0.94);
                })
                .addTemporalMarker(6.6, () -> {
                    lift.setPower(0);
                })
                .build();

        Trajectory dropSeq = drive.trajectoryBuilder(new Pose2d(38.5,3,270))
                .lineToConstantHeading(new Vector2d(34.5,3))
                .addTemporalMarker(0, () -> {
                    rotator.setPosition(.335);
                })
                .addTemporalMarker(.75, () -> {
                    claw.setPosition(0.1);
                })
                .build();

        Trajectory resetSeq = drive.trajectoryBuilder(new Pose2d(34.5,3,270))
                .lineTo(
                        new Vector2d(38.5, 0),
                        SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                )
                .addTemporalMarker(0, () -> {
                    lift.setPower(1.0);
                })
                .addTemporalMarker(6.6, () -> {
                    lift.setPower(0);
                })
                .build();

        Trajectory lmaoresetSeq = drive.trajectoryBuilder(new Pose2d(38.5,0,270))
                .lineToConstantHeading(new Vector2d(38.5, -40))
                .build();

        Trajectory backSeq = drive.trajectoryBuilder(new Pose2d(38.5, -40, 270))
                .lineTo((new Vector2d(14, -36.5)))
                .build();

        Trajectory frontSeq = drive.trajectoryBuilder(new Pose2d(38.5, -40, 270))
                .lineTo((new Vector2d(65, -36.5)))
                .build();

//        TrajectorySequence coneSeq = drive.trajectorySequenceBuilder((new Pose2d(-18.5,0,270)))
//                .lineToLinearHeading(new Pose2d(-24,-15,270))
//                .lineToLinearHeading(new Pose2d(-54,-15,270))
//                .addTemporalMarker(0, () -> {
//                    lift.setPower(0.675);
//                })
//                .addTemporalMarker(4, () -> {
//                    lift.setPower(0);
//                })
//                .addTemporalMarker(0, () -> {
//                    rotator.setPosition(.335);
//                })
//                .addTemporalMarker(5, () -> {
//                    claw.setPosition(.27);
//                })
//                .build();
//
//        TrajectorySequence againSeq = drive.trajectorySequenceBuilder((new Pose2d(-54,-15,270)))
//                .lineToLinearHeading(new Pose2d(-24,-15,270))
//                .lineToLinearHeading(new Pose2d(-18.5,0,270))
//                .addTemporalMarker(0, () -> {
//                    lift.setPower(-1.0);
//                })
//                .addTemporalMarker(4, () -> {
//                    lift.setPower(0);
//                })
//                .addTemporalMarker(1, () -> {
//                    rotator.setPosition(.96);
//                })
//                .build();




        /*
         * The INIT-loop:
         * This REPLACES waitForStart!
         */
        claw.setPosition(0.24);
        while (!isStarted() && !isStopRequested())
        {
            ArrayList<AprilTagDetection> currentDetections = aprilTagDetectionPipeline.getLatestDetections();

            if(currentDetections.size() != 0)
            {
                boolean tagFound = false;

                for(AprilTagDetection tag : currentDetections)
                {
                    if(tag.id == LEFT || tag.id == MIDDLE || tag.id == RIGHT)
                    {
                        tagOfInterest = tag;
                        tagFound = true;
                        break;
                    }
                }

                if(tagFound)
                {
                    telemetry.addLine("Tag of interest is in sight!\n\nLocation data:");
                    tagToTelemetry(tagOfInterest);
                }
                else
                {
                    telemetry.addLine("Don't see tag of interest :(");

                    if(tagOfInterest == null)
                    {
                        telemetry.addLine("(The tag has never been seen)");
                    }
                    else
                    {
                        telemetry.addLine("\nBut we HAVE seen the tag before; last seen at:");
                        tagToTelemetry(tagOfInterest);
                    }
                }

            }
            else
            {
                telemetry.addLine("Don't see tag of interest :(");

                if(tagOfInterest == null)
                {
                    telemetry.addLine("(The tag has never been seen)");
                }
                else
                {
                    telemetry.addLine("\nBut we HAVE seen the tag before; last seen at:");
                    tagToTelemetry(tagOfInterest);
                }

            }

            telemetry.update();
            sleep(20);
        }

        /*
         * The START command just came in: now work off the latest snapshot acquired
         * during the init loop.
         */

        /* Update the telemetry */
        if(tagOfInterest != null)
        {
            telemetry.addLine("Tag snapshot:\n");
            tagToTelemetry(tagOfInterest);
            telemetry.update();
        }
        else
        {
            telemetry.addLine("No tag snapshot available, it was never sighted during the init loop :(");
            telemetry.update();
        }

        /* Actually do something useful */

        if(tagOfInterest == null || tagOfInterest.id == LEFT){ //left
            drive.followTrajectorySequence(startSeq);
            drive.followTrajectory(dropSeq);
            drive.followTrajectory(resetSeq);
            drive.followTrajectory(lmaoresetSeq);
            drive.followTrajectory(backSeq);
//            drive.followTrajectorySequence(coneSeq);
//            drive.followTrajectorySequence(againSeq);

        }else if(tagOfInterest.id == MIDDLE){ //middle
            drive.followTrajectorySequence(startSeq);
            drive.followTrajectory(dropSeq);
            drive.followTrajectory(resetSeq);
            drive.followTrajectory(lmaoresetSeq);
        }else{ //right
            drive.followTrajectorySequence(startSeq);
            drive.followTrajectory(dropSeq);
            drive.followTrajectory(resetSeq);
            drive.followTrajectory(lmaoresetSeq);
            drive.followTrajectory(frontSeq);
        }


        while (opModeIsActive()) {sleep(20);}
    }

    void tagToTelemetry(AprilTagDetection detection)
    {
        telemetry.addLine(String.format("\nDetected tag ID=%d", detection.id));
        telemetry.addLine(String.format("Translation X: %.2f feet", detection.pose.x*FEET_PER_METER));
        telemetry.addLine(String.format("Translation Y: %.2f feet", detection.pose.y*FEET_PER_METER));
        telemetry.addLine(String.format("Translation Z: %.2f feet", detection.pose.z*FEET_PER_METER));
        telemetry.addLine(String.format("Rotation Yaw: %.2f degrees", Math.toDegrees(detection.pose.yaw)));
        telemetry.addLine(String.format("Rotation Pitch: %.2f degrees", Math.toDegrees(detection.pose.pitch)));
        telemetry.addLine(String.format("Rotation Roll: %.2f degrees", Math.toDegrees(detection.pose.roll)));
    }
}
