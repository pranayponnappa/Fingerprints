package android.example.com.fingerprints;

/**
 * Created by pranayponnappa on 12/27/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ProcessActivity extends Activity {
    Binarizer img = new Binarizer();
    boolean imgViewOpen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.process, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void scanFinger(View view) {
        Intent intent = new Intent (this, CameraActivity.class) ;
        startActivity(intent) ;

    }

    public void binarizer(View view) {
        Log.d("binarizer", "entering test things");
        double time = System.currentTimeMillis();
        img.testThings();
        TextView t = (TextView) findViewById(R.id.report_time);
        time = (System.currentTimeMillis() - time) / 1000;
        t.setText("Algorithm took " + time + " seconds");
        Log.d("binarizer", "got past test things");
    }

    public void show_image(View view) {
        //Create Binarizer x. Default constructor uses test.jpg in res.
        ImageView display = (ImageView) findViewById(R.id.imageview);
        display.setImageBitmap(img.getBitmap());
        imgViewOpen = true;
    }
    public void crop_image(View view){
        img.cropDynamically();
    }
    @Override
    public void onBackPressed() {
        if (imgViewOpen){
            Bitmap.Config config = Bitmap.Config.ARGB_8888;
            Bitmap bm = Bitmap.createBitmap(1, 1, config);
            ImageView display = (ImageView) findViewById(R.id.imageview);
            display.setImageBitmap(bm);
            imgViewOpen = false;
        }
        else{
            img.img.recycle();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void Enter_cell_size(View view){
        EditText cellsize = (EditText) findViewById(R.id.editText);
        img.setCellsize(Integer.parseInt(cellsize.getText().toString()));
    }
}
