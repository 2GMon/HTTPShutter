package jp.ddo.t2gmon.httpshutter;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements Callback, PictureCallback, AutoFocusCallback {
	private Camera camera;
	private Bitmap bmp = null;
	private boolean bmpGenerated = false;
	private List<Camera.Size> supportedPreviewSize = null;
	private List<Camera.Size> supportedPictureSize = null;
	private List<String> supportedFlashMode = null;

	public CameraView(Context context) {
		super(context);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Camera.Parameters parameters = camera.getParameters();
		boolean portrait = (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
		if (portrait) {
			camera.setDisplayOrientation(90);
		} else {
			camera.setDisplayOrientation(0);
		}
		supportedPreviewSize = parameters.getSupportedPreviewSizes();
		supportedPictureSize = camera.getParameters().getSupportedPictureSizes();
		supportedFlashMode = parameters.getSupportedFlashModes();
		Camera.Size previewSize = supportedPreviewSize.get(0);
		parameters.setPreviewSize(previewSize.width, previewSize.height);
		camera.setParameters(parameters);
		camera.startPreview();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera = Camera.open();
			camera.setPreviewDisplay(holder);
		} catch(IOException e) {
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
	}

	public void onPictureTaken(byte[] data, Camera arg1) {
		bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		bmpGenerated = true;
		camera.startPreview();
	}

	public boolean httpShutter() {
		bmpGenerated = false;
		camera.autoFocus(this);
		return true;
	}

	public Bitmap getBitmap() {
		return bmp;
	}

	public void clearBitmap() {
		bmp = null;
		bmpGenerated = false;
	}

	public boolean getBitmapGenerated() {
		return bmpGenerated;
	}

	public void setBitmapGenerated(boolean bool) {
		bmpGenerated = bool;
	}

	public List<Camera.Size> getSupportedPreviewSize() {
		return supportedPreviewSize;
	}

	public List<Camera.Size> getSupportedPictureSize() {
		return supportedPictureSize;
	}

	public Camera.Size getPreviewSize() {
		return camera.getParameters().getPreviewSize();
	}

	public Camera.Size getPictureSize() {
		return camera.getParameters().getPictureSize();
	}

	public List<String> getSupportedFlashMode() {
		return supportedFlashMode;
	}

	public void setPreviewSize(int width, int height) {
		camera.stopPreview();
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(width, height);
		camera.setParameters(parameters);
		camera.startPreview();
	}

	public void setPictureSize(int width, int height) {
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPictureSize(width, height);
		camera.setParameters(parameters);
	}

	public void setFlashMode(String flashMode) {
		camera.stopPreview();
		Camera.Parameters parameters = camera.getParameters();
		parameters.setFlashMode(flashMode);
		camera.setParameters(parameters);
		camera.startPreview();
	}

	public void doAutofocus() {
		camera.autoFocus(null);
	}

	public void onAutoFocus(boolean success, Camera camera) {
		camera.takePicture(null, null, this);
	}
}
