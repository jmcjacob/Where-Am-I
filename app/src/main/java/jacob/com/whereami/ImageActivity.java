package jacob.com.whereami;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.jacob.whereiam.R;

import java.util.concurrent.TimeUnit;

public class ImageActivity extends AppCompatActivity {

    private final String LOG_TAG = ImageActivity.class.getSimpleName();
    public String title;
    public Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        int itemPosition = intent.getIntExtra(MainActivity.EXTRA_MESSAGE, 1);

        Log.v(LOG_TAG, " HERE ");
        String src = "";
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

        TextView text = (TextView)findViewById(R.id.title);
        text.setText(title);

        ImageView imageView = (ImageView)findViewById(R.id.sourceImage);
        try {
            image = task.get(1, TimeUnit.HOURS);
            imageView.setImageBitmap(image);
            }
        catch (Exception e) {
            Log.e(LOG_TAG, "FectchBitmap in ImageActivity: " + e);

        }
    }
}
