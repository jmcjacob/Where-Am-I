package com.jacob.whereiam;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

        private final String LOG_TAG = ImageAdapter.class.getSimpleName();

        public List<Image> ImageList;

        public ImageAdapter(List<Image> imageList)
        {
            this.ImageList = imageList;
        }


        @Override
        public int getItemCount()
        {
            return ImageList.size();
        }

        @Override
        public void onBindViewHolder(ImageViewHolder imageViewHolder, int i) {
            Image ci = ImageList.get(i);
            BitmapImage bitmapImage = new BitmapImage();
            bitmapImage.execute(ci);
            while (!bitmapImage.isReady()) {
                try {
                    wait();
                }
                catch (Exception e) {}
            }
            imageViewHolder.vImage.setImageBitmap(bitmapImage.bitmap);
            imageViewHolder.vImageTitle.setText(ci.Title);
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.card_layout, viewGroup, false);

            return new ImageViewHolder(itemView);
        }

        public static class ImageViewHolder extends RecyclerView.ViewHolder {
            protected ImageView vImage;
            protected TextView vImageTitle;

            public ImageViewHolder(View v) {
                super(v);
                vImage = (ImageView) v.findViewById(R.id.image);
                vImageTitle = (TextView)  v.findViewById(R.id.image_title);
            }
        }
    }