package com.byteshaft.medicosperuanos.doctors;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.byteshaft.medicosperuanos.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static com.byteshaft.medicosperuanos.utils.Helpers.getBitMapForSelectedImage;

public class SelectedImages extends AppCompatActivity {

    private GridView androidGridView;
    private ImagesAdapter adapter;
    private ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_selected_images);
        arrayList = new ArrayList<>();
        printMap(DoctorsAppointment.photosHashMap);
        androidGridView = (GridView) findViewById(R.id.selected_images);
        adapter = new ImagesAdapter(arrayList);
        androidGridView.setAdapter(adapter);
    }

    public void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            if (pair.getValue() != null)
            arrayList.add(String.valueOf(pair.getKey()));
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (adapter != null) {
//            adapter.notifyDataSetChanged();
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default: return false;
        }
    }

    private class ImagesAdapter extends BaseAdapter {

        private ViewHolder viewHolder;
        private ArrayList<String> imagesList;

        private ImagesAdapter(ArrayList<String> imagesList) {
            this.imagesList = imagesList;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
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
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), FullscreenImageView.class);
                    intent.putExtra("url", DoctorsAppointment.photosHashMap.get(imagesList.get(position)));
                    startActivity(intent);
                }
            });
            viewHolder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DoctorsAppointment.photosHashMap.containsKey(imagesList.get(position))) {
                        DoctorsAppointment.photosHashMap.remove(imagesList.get(position));
                        DoctorsAppointment.removedImages.add(imagesList.get(position));
                        imagesList.remove(position);
                        notifyDataSetChanged();
                    }
                }
            });
            getBitMapForSelectedImage(DoctorsAppointment.photosHashMap.get(imagesList.get(position)), viewHolder.imageView);
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
