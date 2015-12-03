package jacob.com.whereami;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import com.jacob.whereiam.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ImageActivity extends AppCompatActivity {

    private final String LOG_TAG = ImageActivity.class.getSimpleName();
    public String title;
    public String src;
    public Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        int itemPosition = intent.getIntExtra(MainActivity.EXTRA_MESSAGE, 1);

        Cursor c = MainActivity.database.rawQuery("SELECT * FROM IMAGES;", null);
        if (c.moveToFirst()) {
            for (int i = 0; i < itemPosition; i++) {
                c.moveToNext();
            }
            src = c.getString(c.getColumnIndex("SOURCE"));
            title = c.getString(c.getColumnIndex("TITLE"));
        }

        FetchBitmap task = new FetchBitmap();
        task.execute(src);

        setTitle(title);
        ImageView imageView = (ImageView)findViewById(R.id.sourceImage);
        try {
            image = task.get(1, TimeUnit.HOURS);
            imageView.setImageBitmap(image);
            }
        catch (Exception e) {
            Log.e(LOG_TAG, "FectchBitmap in ImageActivity: " + e);

        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, src);
            sendIntent.putExtra(Intent.EXTRA_TITLE, title);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share)));
            return true;
        }
        if (id == R.id.action_save) {
            Toast toast = Toast.makeText(getApplicationContext(), "Saving "+title, Toast.LENGTH_SHORT);
            toast.show();
            storeImage(image);
            toast = Toast.makeText(getApplicationContext(), "Saved " + title + ".jpg", Toast.LENGTH_SHORT);
            toast.show();
            return true;
        }
        return false;
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(LOG_TAG, "Error creating media file, check storage permissions: ");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
        }
    }

    public File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }
}
