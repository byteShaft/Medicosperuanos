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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.byteshaft.medicosperuanos.utils.Helpers.getBitMap;

public class SelectedImages extends AppCompatActivity {

    private GridView androidGridView;
    private HashMap<String, String> photosList;
    private ImagesAdapter adapter;
    private ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_images);
        arrayList = new ArrayList<>();
        photosList = DoctorsAppointment.photosArrayList;
        printMap(photosList);
        androidGridView = (GridView) findViewById(R.id.selected_images);
        Log.i("TAG", "photos" + photosList);
        adapter = new ImagesAdapter(arrayList);
        androidGridView.setAdapter(adapter);
    }

    public void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            arrayList.add(String.valueOf(pair.getKey()));
        }
    }


    private class ImagesAdapter extends BaseAdapter {

        private ViewHolder viewHolder;
        private ArrayList<String> imagesList;

        private ImagesAdapter(ArrayList<String>  imagesList) {
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
            viewHolder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            getBitMap(photosList.get(imagesList.get(position)), viewHolder.imageView);
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
