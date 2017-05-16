package com.byteshaft.medicosperuanos.doctors;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.byteshaft.medicosperuanos.R;

import java.util.ArrayList;

import static com.byteshaft.medicosperuanos.utils.Helpers.getBitMap;

public class SelectedImages extends AppCompatActivity {

    private GridView androidGridView;
    private ArrayList<String> photosList;
    private ImagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_images);
        androidGridView = (GridView) findViewById(R.id.selected_images);
        photosList = DoctorsAppointment.photosArrayList;
        Log.i("TAG", "photos" + photosList);
        adapter = new ImagesAdapter(photosList);
        androidGridView.setAdapter(adapter);
    }


    private class ImagesAdapter extends BaseAdapter {

        private ViewHolder viewHolder;
        private ArrayList<String> imagesList;

        private ImagesAdapter(ArrayList<String> imagesList) {
            this.imagesList = imagesList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_images, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.photo);
                viewHolder.removeButton = (ImageButton) convertView.findViewById(R.id.remove_image);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            // set here
                getBitMap(imagesList.get(position), viewHolder.imageView);
            return convertView;
        }

        @Override
        public int getCount() {
            return imagesList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

    }

    private class ViewHolder {
        private ImageView imageView;
        private ImageButton removeButton;
    }
}
