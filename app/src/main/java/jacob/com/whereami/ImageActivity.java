package jacob.com.whereami;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import com.jacob.whereiam.R;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Class for the Image Activity that is loaded on click of an item from the MainActivity and features code from
// Square (2015) Picasso. [software] version 2.5.2. Available from http://square.github.io/picasso/ [Accessed 8 December 2015].
// Google (2015) Sending Simple Data to Other Apps. [online] California, USA: Google. Available from http://developer.android.com/training/sharing/send.html [Accessed on 3 December 2015].
// GoCrazy (2013) How to save a bitmap on internal storage. [Online] Stackoverflow. Available from http://stackoverflow.com/questions/15662258/how-to-save-a-bitmap-on-internal-storage [Accessed on 3 December 2015].
public class ImageActivity extends AppCompatActivity {

    private final String LOG_TAG = ImageActivity.class.getSimpleName();
    public String title;
    public String src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        setTitle(title);
        ImageView imageView = (ImageView)findViewById(R.id.sourceImage);
        Picasso.with(this).load(src).into(imageView);
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
            ImageView image = (ImageView)findViewById(R.id.sourceImage);
            Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
            if (!bitmap.equals(null)) {
                Toast toast = Toast.makeText(getApplicationContext(), "Saving " + title + ".jpg", Toast.LENGTH_SHORT);
                toast.show();
                storeImage(bitmap);
                toast = Toast.makeText(getApplicationContext(), "Saved " + title + ".jpg", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), "Could not save image", Toast.LENGTH_SHORT);
                toast.show();
            }
            return false;
        }
        if (id == 16908332) {
            super.onBackPressed();
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
        File mediaFile;
        String mImageName = getTitle() + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }
}
