package no_de.inf5090.visualizingsensordata.userInterface;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import no_de.inf5090.visualizingsensordata.application.CameraHelper;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private CameraHelper mCameraHelper;

    public CameraPreview(Context context, CameraHelper cameraHelper) {
        super(context);
        mCameraHelper = cameraHelper;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        // Are we really going to support such old phones? Is GPS + Good camera even a given then?
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int weight, int height) {
        Log.d("CameraPreview", "surfaceChanged");
        // If your preview can change or rotate, take care of those events
        // here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCameraHelper.getCamera().stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // make any resize, rotate or reformatting changes here

        // start preview with new settings
        try {
            // set size
            Camera c = mCameraHelper.getCamera();
            Camera.Parameters p = c.getParameters();
            p.setPreviewSize(1920, 1080); // FIXME: check for supported and most optional sizes
            c.setParameters(p);

            c.setPreviewDisplay(holder);
            c.startPreview();

        } catch (Exception e) {
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("CameraPreview", "surfaceCreated");
        startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    /**
     * Start preview
     */
    public void startPreview() {
        Log.d("CameraPreview", "startPreview");
        // start preview with new settings
        try {
            mCameraHelper.getCamera().setPreviewDisplay(mHolder);
            mCameraHelper.getCamera().startPreview();

        } catch (Exception e) {
        }
    }
}
