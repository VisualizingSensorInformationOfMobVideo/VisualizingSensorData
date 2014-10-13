package no_de.inf5090.visualizingsensordata.application;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.widget.Toast;
import no_de.inf5090.visualizingsensordata.userInterface.CameraPreview;
import no_de.inf5090.visualizingsensordata.userInterface.VideoCapture;

import java.io.IOException;
import java.util.List;

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
        mCamera = getCameraInstance();
        if (mCamera == null) {
            Toast.makeText(VideoCapture.getSelf(), "Fail to get Camera", Toast.LENGTH_LONG).show();
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

            params.setPictureSize(sizes.get(sizes.size() - 1).width, sizes.get(sizes.size() - 1).height);
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
     *
     * TODO: This should be called on shutdown
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
}
