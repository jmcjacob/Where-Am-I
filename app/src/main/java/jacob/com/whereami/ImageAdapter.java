package jacob.com.whereami;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jacob.whereiam.R;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

// This class adapts a src and a title to a card layout and then adds it to the RecyclerView. It features code from
// Square (2015) Picasso. [software] version 2.5.2. Available from http://square.github.io/picasso/ [Accessed 8 December 2015].
// Azzola, F. (2014) A Guide to Android RecyclerView and CardView. [online] Binpress. Available from https://www.binpress.com/tutorial/android-l-recyclerview-and-cardview-tutorial/156 [Accessed on 14 November 2015].
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final String LOG_TAG = ImageAdapter.class.getSimpleName();

    @Override
    public int getItemCount() {
        String countQuery = "SELECT * FROM IMAGES;";
        Cursor cursor = MainActivity.database.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {
        String src = "@drawable/kitten.jpg";
        String title = "Failed";
        TextView textView = (TextView)MainActivity.context.findViewById(R.id.network);
        textView.setVisibility(View.INVISIBLE);
        textView.setText("");
        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)MainActivity.context.findViewById(R.id.swipeRefresh);
        refreshLayout.setRefreshing(false);
        TextView text = (TextView)MainActivity.context.findViewById(R.id.fact);
        text.setVisibility(View.INVISIBLE);
        try {
            //
            Cursor c = MainActivity.database.rawQuery("SELECT * FROM IMAGES", null);
            if (c.moveToFirst()) {
                for (int j = 0; j < i; j++) {
                    c.moveToNext();
                }
                src = c.getString(c.getColumnIndex("THUMBNAIL"));
                title = c.getString(c.getColumnIndex("TITLE"));
            }
            c.close();
            Picasso.with(MainActivity.context).load(src).into(imageViewHolder.vImage);
            imageViewHolder.vTitle.setText(title);
        }
        catch (Exception e) {
            Log.e(LOG_TAG,"ImageAdapter: " + e);
        }
    }

    public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View item = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.card_layout, viewGroup, false);
        item.setOnClickListener(new MyOnClickListener());
        return new ImageViewHolder(item);
    }

    class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int itemPosition = MainActivity.recList.getChildPosition(v);
            Intent intent = new Intent(MainActivity.context, ImageActivity.class);
            intent.putExtra(MainActivity.EXTRA_MESSAGE, itemPosition);
            MainActivity.context.startActivity(intent);
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView vImage;
        public TextView vTitle;

        public ImageViewHolder(View v) {
            super(v);
            vImage = (ImageView) v.findViewById(R.id.image);
            vTitle = (TextView) v.findViewById(R.id.image_title);
        }
    }
}
