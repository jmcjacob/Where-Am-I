package jacob.com.whereami;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.jacob.whereiam.R;

import java.util.concurrent.TimeUnit;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            if (MainActivity.isNetworkAvailable()) {

                CatFact task = new CatFact();
                task.execute();
                TextView text = (TextView) findViewById(R.id.plea);
                String plea = text.getText() + " And now for your amusement here is a cat fact. " + task.get(1000, TimeUnit.MILLISECONDS);
                text.setText(plea);
            }
        }
        catch (Exception e) {

        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            super.onBackPressed();
        }
        return false;
    }
}
