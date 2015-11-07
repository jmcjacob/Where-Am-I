package com.jacob.whereiam;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Fragment;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String jsonTest = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerFragment drawer = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.drawer_fragment);
        drawer.setup(R.id.drawer_fragment, (DrawerLayout) findViewById(R.id.drawer_layout), (Toolbar) toolbar);

        RecyclerView recList = (RecyclerView) findViewById(R.id.cardview);
        recList.setHasFixedSize(true);
        GridLayoutManager llm = new GridLayoutManager(this,2);
        llm.setOrientation(GridLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        ImageAdapter image = new ImageAdapter(createList(1));
        recList.setAdapter(image);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<Image> createList(int size)
    {

        List<Image> result = new ArrayList<Image>();
        for (int i=1; i <= size; i++)
        {
            Image ci = new Image();
            ci.Title = Image.TITLE_PREFIX + i;
            ci.SRC = Image.SRC_PREFIX + i;

            result.add(ci);

        }

        return result;
    }
    public class AsyncTaskParseJson extends AsyncTask<String, String, String> {

        String yourServiceUrl = "Put the URL here!!!!!";

        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... arg0)  {

            try {
                httpConnect jParser = new httpConnect();

                String json = jParser.getJSONFromUrl(yourServiceUrl);

                jsonTest = json.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
