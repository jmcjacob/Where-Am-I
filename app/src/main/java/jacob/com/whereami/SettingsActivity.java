package jacob.com.whereami;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jacob.whereiam.R;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {
    private final String LOG_TAG = SettingsActivity.class.getSimpleName();
    EditText option1;
    EditText option2;
    Boolean edited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        option1 = (EditText) findViewById(R.id.load_images);
        option2 = (EditText) findViewById(R.id.cache);
        try {
            option1.setText(String.valueOf(MainActivity.sharedpreferences.getInt("loadImage",50)));
            option2.setText(String.valueOf(MainActivity.sharedpreferences.getInt("cacheImage", 25)));
        }
        catch (Exception e) {
            option1.setText("50");
            option2.setText("25");
        }
    }

    public void onClickSave(View view) {
        EditText option1 = (EditText)findViewById(R.id.load_images);
        int loadImage = Integer.valueOf(option1.getText().toString());
        EditText option2 = (EditText) findViewById(R.id.cache);
        int cache = Integer.valueOf(option2.getText().toString());
        if (101>loadImage && 0<loadImage && 0<cache) {

            SharedPreferences.Editor editor = MainActivity.sharedpreferences.edit();
            editor.putInt("loadImage", loadImage);
            editor.putInt("cacheImage", cache);
            editor.commit();
            edited = true;
            Toast toast = Toast.makeText(getApplicationContext(), "Saved Preferences", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(), "Cannot save make sure parameter are correct.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void onClickReset(View view) {
        MainActivity.sharedpreferences.edit().clear().commit();
        option1.setText("50");
        option2.setText("25");
        edited=true;
        Toast toast = Toast.makeText(getApplicationContext(), "Reset Preferences", Toast.LENGTH_SHORT);
        toast.show();
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            if (edited)
            {
                Intent intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
            }
            else
                super.onBackPressed();
        }
        return false;
    }
}