package android.example.com.fingerprints;

/**
 * Created by pranayponnappa on 12/27/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import fingerprint.example.com.fingerprint.R.id;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends Activity {

    Handler handlerUI = new Handler();
    protected static final String TAG = "FixIt";
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(id.camera_preview);
        preview.addView(mPreview);

        //Set Resolution
        Parameters cp = mCamera.getParameters();
        List<Size> sl = cp.getSupportedPictureSizes();

        int w = 0, h = 0 ;
        for (Size s : sl) {
            if( w < s.width && h < s.height ){
                w = s.width ;
                h = s.height ;
            }
        }
        cp.setPictureSize(w, h) ;
        mCamera.setParameters(cp) ;

        // Turn on Flash
        if( CameraActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Parameters params = mCamera.getParameters() ;
            params.setFlashMode(Parameters.FLASH_MODE_TORCH) ;
            mCamera.setParameters(params) ;
        }

        //Call focusing and metering method
        focusCamera(mCamera) ;

        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        Log.d("FixIt", "Right before the picture is taken");
                        mCamera.takePicture(null, null, mPicture);
                        Parameters params = mCamera.getParameters() ;
                        params.setFlashMode(Parameters.FLASH_MODE_OFF) ;
                        mCamera.setParameters(params) ;
                        handlerUI.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Open splash screen after capture
                                Intent intent = new Intent(CameraActivity.this, ProcessActivity.class) ;
                                startActivity(intent) ;
                            }
                        }, 1000);
                    }


                }
        );

        // Add a listener to the Focus button
        Button focusButton = (Button) findViewById(id.button_focus) ;
        focusButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View V) {
                                               Log.i(TAG, "Focusing?");
                                               //Call focusing and metering method
                                               focusCamera(mCamera) ;
                                           }
                                       }
        );

        Button flashButton = (Button) findViewById(id.button_flash) ;
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                if( CameraActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Parameters params = mCamera.getParameters() ;
                    params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(params);
                }
            }

        });


    }


    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable

    }

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {

        }
    };

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            return;

        }
    };


    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        /*File save = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(save, "Fingerprint.jpg");
        return file;*/


	    /* Being Rewritten temp for testing*/
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        //For hidden storage. Use getCacheDir() for temporary storage
        //File mediaStorageDir = new File(context.getFilesDir(), filename);

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Fingerprints");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Fingerprints", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
	    /*rewritten temporarily to test binarizer class
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else {
	        return null;
	    }*/
        File mediaFile;
        //saving picture as "Fingerprint.jpg"
        if(type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath()+File.separator+"Fingerprint.jpg");
        }
        else{
            return null;
        }

        return mediaFile;
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    //Code for Focusing on Center
    public static void focusCamera(Camera mCamera) {

        Camera.Parameters params = mCamera.getParameters();
        Rect areaRect1 = new Rect(-300, -300, 300, 300);    // specify an area in center of image

        if (params.getMaxNumMeteringAreas() > 0){ // check that metering areas are supported
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(areaRect1, 1000)); // set weight for metering
            params.setMeteringAreas(meteringAreas);
        }

        if (params.getMaxNumFocusAreas() > 0) {
            mCamera.cancelAutoFocus();
            List<Camera.Area> focusingAreas = new ArrayList<Camera.Area>() ;
            focusingAreas.add(new Camera.Area(areaRect1, 1000)) ;
            params.setFocusMode(Parameters.FOCUS_MODE_MACRO) ;
            params.setFocusAreas(focusingAreas);
        }

        mCamera.setParameters(params);
        mCamera.autoFocus(null);

    }
}