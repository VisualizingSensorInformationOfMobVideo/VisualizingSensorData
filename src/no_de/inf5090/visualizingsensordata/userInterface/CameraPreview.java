package no_de.inf5090.visualizingsensordata.userInterface;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import no_de.inf5090.visualizingsensordata.application.CameraHelper;

import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private CameraHelper mCameraHelper;

    public CameraPreview(Context context, CameraHelper cameraHelper) {
        super(context, null);
        mCameraHelper = cameraHelper;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        //mHolder = getHolder();
        //mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        // Are we really going to support such old phones? Is GPS + Good camera even a given then?
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int weight, int height) {
        Log.d("CameraPreview", "surfaceChanged");
        // If your preview can change or rotate, take care of those events
        // here.
        // Make sure to stop the preview before resizing or reformatting it.

        /*if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }*/

        // stop preview before making changes
        stopPreview();

        // start preview with new settings
        startPreview();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("CameraPreview", "surfaceCreated");
        startPreview();

        //((VideoCapture)getContext()).mSession.startPreview();

        Session mSession = ((VideoCapture)getContext()).mSession;
        //mSession.setDestination("37.191.203.206"); //37.191.193.98
        //mSession.setDestination("37.191.193.98");
        mSession.setDestination("37.191.195.33");
        mSession.configure();
        //mSession.start();

        //RtspClient mClient = ((VideoCapture)getContext()).mClient;
        //mClient.setServerAddress("37.191.203.206", 5004);
        //mClient.setStreamPath("/");
        //mClient.startStream();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("CameraPreview", "surfaceDestroyed");
        stopPreview();
    }

    /**
     * Start preview
     */
    public void startPreview() {
        /*if (!mCameraHelper.hasCamera())
            return;

        // set preview size - just select the best one
        Camera c = mCameraHelper.getCamera();
        Camera.Parameters p = c.getParameters();
        List<Camera.Size> sizes = p.getSupportedPreviewSizes();
        p.setPreviewSize(sizes.get(0).width, sizes.get(0).height);
        c.setParameters(p);

        try {
            c.setPreviewDisplay(mHolder);
            c.startPreview();

            mCameraHelper.setAutoFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        ((VideoCapture)getContext()).mSession.startPreview();
    }

    /**
     * Stop preview
     */
    public void stopPreview() {
        /*Camera c = mCameraHelper.getCamera();
        if (c == null)
            return;
        try {
            c.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        ((VideoCapture)getContext()).mSession.stop();
    }
}
