package jacob.com.whereami;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

// This class is for the Settings activity accessed from the menu in the MainActivity. It is where shared preferences are written and used code featured in
// Google (2015) Saving Key-Value Sets. [online] California, USA: Google. Available from http://developer.android.com/reference/android/content/SharedPreferences.html [Accessed on 3 December 2015].
// Zabri (2013) Android: create a popup that has multiple selection options. [online] Stackoverflow. Available from http://stackoverflow.com/questions/16389581/android-create-a-popup-that-has-multiple-selection-options [Accessed on 8 December 2015].
public class SettingsActivity extends AppCompatActivity {
    EditText option1;
    EditText option2;
    TextView option3;
    Boolean edited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        option1 = (EditText) findViewById(R.id.load_images);
        option2 = (EditText) findViewById(R.id.cache);
        option3 = (TextView) findViewById(R.id.option3);
        try {
            option1.setText(String.valueOf(MainActivity.sharedpreferences.getInt("loadImage",50)));
            option2.setText(String.valueOf(MainActivity.sharedpreferences.getInt("cacheImage", 25)));
            option3.setText(MainActivity.sharedpreferences.getString("searchType", "relevance"));
        }
        catch (Exception e) {
            option1.setText("50");
            option2.setText("25");
        }
    }

    public void onClickSave(View view) {
        int loadImage = Integer.valueOf(option1.getText().toString());
        int cache = Integer.valueOf(option2.getText().toString());
        String sort = (String)option3.getText();
        if (101>loadImage && 0<loadImage && 0<cache) {
            SharedPreferences.Editor editor = MainActivity.sharedpreferences.edit();
            editor.putInt("loadImage", loadImage);
            editor.putInt("cacheImage", cache);
            editor.putString("searchType", sort);
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
        option3.setText("relevance");
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

    public void onClickType(View view) {
        final TextView textView = (TextView)findViewById(R.id.option3);
        final String[]types = {"date-posted-asc", "date-posted-desc", "date-taken-asc", "date-taken-desc", "interestingness-desc", "interestingness-asc", "relevance"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick what type of images to load");
        builder.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textView.setText(types[which]);
            }
        });
        builder.show();
    }
}