package no_de.inf5090.visualizingsensordata.application;

import java.io.IOException;
import java.util.List;

import no_de.inf5090.visualizingsensordata.userInterface.CameraPreview;
import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

public class CameraHelper {
    private Camera mCamera;

    private MediaRecorder mediaRecorder;

    /**
     * State of recording
     */
    private boolean mIsRecording = false;

    /**
     * Profile for video recording
     */
    private CamcorderProfile mProfile;

    /**
     * The preview view
     */
    private CameraPreview mCameraPreview;

    public CameraHelper() {
       obtainCameraOrFinish();
    }

    /**
     * Obtain the camera instance or finish the application
     */
    public void obtainCameraOrFinish() {
        mCamera = getCameraInstance();
        if (mCamera == null) {
            Toast.makeText(VideoCapture.getSelf(), "Fail to get Camera", Toast.LENGTH_LONG).show();
            VideoCapture.getSelf().finish();
        }
    }

    /**
     * Set surface holder for preview
     */
    public void setCameraPreview(CameraPreview cameraPreview) {
        mCameraPreview = cameraPreview;
    }

    /**
     * Attempt to get a Camera instance
     */
    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance

            Camera.Parameters params = c.getParameters();
            List<Camera.Size> sizes = params.getSupportedPictureSizes();

            // set the second lowest resolution for taking snapshots
            params.setPictureSize(sizes.get(sizes.size() - 2).width, sizes.get(sizes.size() - 2).height);

            c.setParameters(params);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c;
    }

    /**
     * Get Camera object
     */
    public Camera getCamera() {
        return mCamera;
    }

    /**
     * Check if we have a valid camera object
     */
    public boolean hasCamera() {
        return mCamera != null;
    }

    /**
     * Get name of output file
     */
    private String getOutputFileName() {
        String filename = VideoCapture.getSelf().getOutputFileName();
        return VideoCapture.getSelf().appDir.getPath()+"/"+filename+".mp4";
    }

    /**
     * Prepare the media recorder so that it can start recording
     *
     * It will create a new MediaRecorder-object and set all needed options to it
     */
    private boolean prepareMediaRecorder() {
        if (mediaRecorder == null)
            mediaRecorder = new MediaRecorder();

        // according to API documents, this should not really be necessary, but the app crashes if not called
        mCamera.unlock();

        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mediaRecorder.setProfile(mProfile);

        mediaRecorder.setOutputFile(getOutputFileName());
        //mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
        //mediaRecorder.setMaxFileSize(5000000); // Set max file size 5M

        // set preview to previewholder
        // this is actually not needed when it is already set on the Camera object
        /*if (mCameraPreview != null) {
            mediaRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());
        }*/

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    /**
     * Release the resources used by recording
     */
    private void releaseMediaRecorder() {
        if (mediaRecorder == null) return;

        mediaRecorder.stop();
        mediaRecorder.reset(); // clear recorder configuration
    }

    /**
     * Release the camera object to other resources and applications
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

    /**
     * Start recording (saving to file)
     */
    public void startRecording() {
        // prepare the recorder
        if (!prepareMediaRecorder()) {
            Toast.makeText(VideoCapture.getSelf(), "Fail in prepareMediaRecorder()!\n - Ended -",
                    Toast.LENGTH_LONG).show();
            VideoCapture.getSelf().finish();
        }

        // start the recording
        mediaRecorder.start();
        mIsRecording = true;
    }

    /**
     * Stop recording
     */
    public void stopRecording() {
        releaseMediaRecorder();
        mIsRecording = false;
    }

    /**
     * Check if recording
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    /**
     * Get resolution in format widthxheight for the video resource
     */
    public String getVideoResolution() {
        //Camera.Size s = mCamera.getParameters().getPreviewSize();
        //return s.width + "x" + s.height;
        if (mProfile == null)
            return null;
        return mProfile.videoFrameWidth + "x" + mProfile.videoFrameHeight;
    }

    /**
     * Activity is destroyed
     */
    public void onDestroy() {
        releaseCamera();
    }

    /**
     * Activity is paused
     * Release the camera to other applications
     */
    public void onPause() {
        releaseCamera();
    }

    /**
     * Activity is resumed
     * Obtain the camera again
     */
    public void onResume() {
        if (mCamera == null) {
            obtainCameraOrFinish();
        }
    }

    /**
     * Set auto focus if available
     */
    public void setAutoFocus() {
    	Camera.Parameters params = mCamera.getParameters();
    	List<String> focusModes = params.getSupportedFocusModes();
    	if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
    		params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
    		mCamera.setParameters(params);
    	}
    }

    /**
     * This is the "base" vertical view angle. "Base" because this is just a 
     * constant, and does not change if the camera is zoomed. So the actual
     * view angle has to be calculated if zoom is implemented. 
     * 
     * @return returns the vertical view angle of the camera
     */
    public float getBaseVerticalViewAngle() {
    	return mCamera.getParameters().getVerticalViewAngle();
    }
    
    /**
     * This is the "base" horizontal view angle. "Base" because this is just a 
     * constant, and does not change if the camera is zoomed. So the actual
     * view angle has to be calculated if zoom is implemented. 
     * 
     * @return returns the horizontal view angle of the camera
     */
    public float getBaseHorizontalViewAngle() {
    	return mCamera.getParameters().getHorizontalViewAngle();
    }

    /**
     * As long as we return the values from get[Horizontal|Vertical]ViewAngle(), 
     * the angle unit will be degrees. 
     * @return string representing the unit of view angle
     */
    public String getUnitOfViewAngle() {
    	return "degrees";
    }
}
