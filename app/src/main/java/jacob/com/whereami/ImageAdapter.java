package jacob.com.whereami;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jacob.whereiam.R;

import java.util.concurrent.TimeUnit;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final String LOG_TAG = ImageAdapter.class.getSimpleName();

    @Override
    public int getItemCount() {
        String countQuery = "SELECT  * FROM " + "IMAGES;";
        Cursor cursor = MainActivity.database.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {
        String src = "@drawable/kitten.jpg";
        String title = "Failed";
        MainActivity.disableLoading();
        try {
            Cursor c = MainActivity.database.rawQuery("SELECT THUMBNAIL FROM IMAGES", null);
            if (c.moveToFirst()) {
                for (int j = 0; j <= i; j++) {
                    c.moveToNext();
                }
                src = c.getString(c.getColumnIndex("THUMBNAIL"));
            }
            c.close();
            Cursor d = MainActivity.database.rawQuery("SELECT TITLE FROM IMAGES", null);
            if (d.moveToFirst()) {
                for (int j = 0; j <= i; j++) {
                    d.moveToNext();
                }
                title = d.getString(d.getColumnIndex("TITLE"));
            }
            d.close();
            FetchBitmap task = new FetchBitmap();
            task.execute(src);
            Bitmap image = task.get(1000, TimeUnit.MILLISECONDS);
            imageViewHolder.vImage.setImageBitmap(image);
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
        return new ImageViewHolder(item);
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        protected ImageView vImage;
        protected TextView vTitle;

        public ImageViewHolder(View v) {
            super(v);
            vImage = (ImageView) v.findViewById(R.id.image);
            vTitle = (TextView) v.findViewById(R.id.image_title);
        }
    }
}
