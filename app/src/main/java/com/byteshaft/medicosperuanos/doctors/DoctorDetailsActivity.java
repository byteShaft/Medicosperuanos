package com.byteshaft.medicosperuanos.doctors;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.gettersetter.Review;
import com.byteshaft.medicosperuanos.messages.ConversationActivity;
import com.byteshaft.medicosperuanos.patients.DoctorBookingActivity;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorDetailsActivity extends AppCompatActivity implements View.OnClickListener,
        HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private TextView doctorName;
    private TextView doctorSpeciality;
    private RatingBar ratingBar;

    private ImageButton callButton;
    private ImageButton chatButton;
    private ImageButton heartButton;
    private Button bookingButton;
    private Button showallReviewButton;
    private TextView textClock;
    private ImageView status;
    private ReviewAdapter adapter;
    private String number;
    private CircleImageView circleImageView;
    private HttpRequest request;
    private int id;
    private boolean isBlocked;
    private String startTime;
    private ArrayList<Review> arrayList;
    private ListView reviewList;
    private ReviewAdapter reviewAdapter;
    private static DoctorDetailsActivity sInstance;
    private ProgressBar progressBar;
    private String location;
    private String name;
    private boolean isAvailable_to_chat;
    private String date;
    private String nextUrl;

    public static DoctorDetailsActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_doctor_details);
        sInstance = this;
        reviewList = (ListView) findViewById(R.id.review_list);
        startTime = getIntent().getStringExtra("start_time");
        name = getIntent().getStringExtra("name");
        final String specialist = getIntent().getStringExtra("specialist");
        final float stars = getIntent().getFloatExtra("stars", 0);
        number = getIntent().getStringExtra("number");
        location = getIntent().getStringExtra("location");
        isBlocked = getIntent().getBooleanExtra("block", false);
        final String photo = getIntent().getStringExtra("photo");
        isAvailable_to_chat = getIntent().getBooleanExtra("available_to_chat", false);
        id = getIntent().getIntExtra("user", -1);
        date = getIntent().getStringExtra("date");

        doctorName = (TextView) findViewById(R.id.doctor_name);
        doctorName.setText(name);
        doctorSpeciality = (TextView) findViewById(R.id.doctor_sp);
        doctorSpeciality.setText(specialist);
        circleImageView = (CircleImageView) findViewById(R.id.profile_image_view);
        ratingBar = (RatingBar) findViewById(R.id.user_ratings);
        ratingBar.setRating(stars);
        callButton = (ImageButton) findViewById(R.id.call_button);
        chatButton = (ImageButton) findViewById(R.id.message_button);
        heartButton = (ImageButton) findViewById(R.id.heart_button);
        bookingButton = (Button) findViewById(R.id.button_book);
        showallReviewButton = (Button) findViewById(R.id.review_all_button);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        status = (ImageView) findViewById(R.id.status);
        progressBar.setVisibility(View.GONE);

        // setting typeface
        doctorName.setTypeface(AppGlobals.typefaceNormal);
        doctorSpeciality.setTypeface(AppGlobals.typefaceNormal);

        AppGlobals.buttonEffect(chatButton);
        AppGlobals.buttonEffect(callButton);
        AppGlobals.buttonEffect(heartButton);

        callButton.setOnClickListener(this);
        chatButton.setOnClickListener(this);
        showallReviewButton.setOnClickListener(this);
        if (isBlocked) {
            chatButton.setEnabled(false);
        }
        heartButton.setOnClickListener(this);
        textClock = (TextView) findViewById(R.id.clock);
        textClock.setText(startTime);
        textClock.setTypeface(AppGlobals.typefaceNormal);
        bookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DoctorBookingActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("date", date);
                intent.putExtra("specialist", specialist);
                intent.putExtra("stars", stars);
                intent.putExtra("number", number);
                intent.putExtra("available_to_chat", isAvailable_to_chat);
                intent.putExtra("user", id);
                intent.putExtra("photo", photo);
                intent.putExtra("block", isBlocked);
                intent.putExtra("start_time", startTime);
                intent.putExtra("location", location);
                startActivity(intent);
            }
        });
        if (!isAvailable_to_chat) {
            status.setImageResource(R.mipmap.ic_offline_indicator);
        } else {
            status.setImageResource(R.mipmap.ic_online_indicator);
        }
        Helpers.getBitMap(photo, circleImageView);
        getReviews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppGlobals.isDoctorFavourite) {
            heartButton.setBackground(getResources().getDrawable(R.mipmap.ic_heart_fill));
        } else {
            heartButton.setBackground(getResources().getDrawable(R.mipmap.ic_empty_heart));

        }
    }

    private void getReviews() {
        progressBar.setVisibility(View.VISIBLE);
        request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%sdoctors/%s/review",
                AppGlobals.BASE_URL, id));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.review_all_button:
//                AllReviewDialog reviewDialog = new AllReviewDialog(DoctorDetailsActivity.this);
//                reviewDialog.setTitle("All Reviews");
//                reviewDialog.show();
                reviewAdapter = new ReviewAdapter(getApplicationContext(), arrayList, arrayList.size());
                reviewList.setAdapter(reviewAdapter);
                break;
            case R.id.call_button:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                            AppGlobals.CALL_PERMISSION);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                    startActivity(intent);
                }
                break;
            case R.id.message_button:
                Intent intent = new Intent(getApplicationContext(),
                        ConversationActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                intent.putExtra("status", isAvailable_to_chat);
                startActivity(intent);
                break;
            case R.id.heart_button:
                heartButton.setEnabled(false);
                if (!AppGlobals.isDoctorFavourite) {
                    Helpers.favouriteDoctorTask(id, new HttpRequest.OnReadyStateChangeListener() {
                        @Override
                        public void onReadyStateChange(HttpRequest request, int readyState) {
                            switch (readyState) {
                                case HttpRequest.STATE_DONE:
                                    switch (request.getStatus()) {
                                        case HttpURLConnection.HTTP_OK:
                                            heartButton.setEnabled(true);
                                            Log.i("TAG", "favourite " + request.getResponseText());
                                            AppGlobals.isDoctorFavourite = true;
                                            heartButton.setBackgroundResource(R.mipmap.ic_heart_fill);
                                    }
                            }
                        }
                    }, new HttpRequest.OnErrorListener() {
                        @Override
                        public void onError(HttpRequest request, int readyState, short error, Exception exception) {
                            heartButton.setEnabled(true);
                        }
                    });
                } else {
                    Helpers.unFavouriteDoctorTask(id, new HttpRequest.OnReadyStateChangeListener() {
                        @Override
                        public void onReadyStateChange(HttpRequest request, int readyState) {
                            switch (readyState) {
                                case HttpRequest.STATE_DONE:
                                    switch (request.getStatus()) {
                                        case HttpURLConnection.HTTP_NO_CONTENT:
                                            heartButton.setEnabled(true);
                                            AppGlobals.isDoctorFavourite = false;
                                            heartButton.setBackgroundResource(R.mipmap.ic_empty_heart);

                                    }
                            }

                        }
                    }, new HttpRequest.OnErrorListener() {
                        @Override
                        public void onError(HttpRequest request, int readyState, short error, Exception exception) {
                            heartButton.setEnabled(true);
                        }
                    });
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppGlobals.CALL_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Helpers.showSnackBar(findViewById(android.R.id.content), R.string.permission_granted);
                } else {
                    Helpers.showSnackBar(findViewById(android.R.id.content), R.string.permission_denied);
                }
                break;
        }
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                progressBar.setVisibility(View.GONE);
                reviewList.setVisibility(View.VISIBLE);
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", "review " + request.getResponseText());
                        arrayList = new ArrayList<>();
                        int size = 0;
                        try {
                            JSONArray jsonArray = new JSONArray(request.getResponseText());
                            if (jsonArray.length() > 10) {
                                size = 10;
                            } else {
                                size = jsonArray.length();
                            }
                            reviewAdapter = new ReviewAdapter(getApplicationContext(), arrayList, size);
                            reviewList.setAdapter(reviewAdapter);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Review review = new Review();
                                review.setReviewId(jsonObject.getInt("id"));
                                review.setReviewText(jsonObject.getString("message"));
                                review.setReviewStars(jsonObject.getInt("stars"));
                                String currentTime = jsonObject.getString("created_at");
                                Log.i("TAG", currentTime);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                Date date = dateFormat.parse(currentTime);
                                dateFormat.setTimeZone(TimeZone.getDefault());
                                String formattedDate = dateFormat.format(date);
                                try {
                                    review.setReviewTime(getDateDiff(dateFormat.parse(formattedDate), dateFormat.parse(Helpers.getCurrentTimeAndDate()),
                                            TimeUnit.MILLISECONDS));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                arrayList.add(review);
                                reviewAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                }
        }
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        progressBar.setVisibility(View.GONE);
    }

    private class ReviewAdapter extends ArrayAdapter {

        private ViewHolder viewHolder;
        private ArrayList<Review> arrayList;
        private int size = 0;

        public ReviewAdapter(Context context, ArrayList<Review> arrayList, int size) {
            super(context, R.layout.delegate_dashboard);
            this.arrayList = arrayList;
            this.size = size;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.delegate_doctor_review, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.userName = (TextView) convertView.findViewById(R.id.by_username);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                viewHolder.userComment = (TextView) convertView.findViewById(R.id.tv_review);
                viewHolder.userRating = (RatingBar) convertView.findViewById(R.id.user_ratings);

                viewHolder.userName.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.time.setTypeface(AppGlobals.typefaceNormal);
                viewHolder.userComment.setTypeface(AppGlobals.typefaceNormal);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Review review = arrayList.get(position);
            viewHolder.userRating.setRating(review.getReviewStars());
            if (review.getReviewText().length() > 1) {
                String output = review.getReviewText().substring(0, 1).toUpperCase() +
                        review.getReviewText().substring(1);
                viewHolder.userComment.setText(output);
            }
            Log.i("TAG", "time " + review.getReviewTime());
            viewHolder.time.setText(timeConvert(review.getReviewTime()));

            return convertView;
        }

        private String timeConvert(long time) {
            long x = time / 1000;
            long seconds = x % 60;
            x /= 60;
            long minutes = x % 60;
            x /= 60;
            long hours = x % 24;
            x /= 24;
            long days = x;
            if (days > 0)
                return days + " days ago";
            else if (days == 0 && hours > 0) return hours + " hours ago";
            else if (days == 0 && hours == 0 && minutes > 0) return minutes + " minutes ago";
            else return seconds + " seconds ago";
        }

        @Override
        public int getCount() {
            return size;
        }
    }

    private class ViewHolder {
        TextView userName;
        TextView time;
        TextView userComment;
        RatingBar userRating;
    }


//    private class AllReviewDialog extends Dialog {
//        private Button closeButton;
//
//        public AllReviewDialog(Activity activity) {
//            super(activity);
//        }
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.dialog_all_review);
//            reviewAdapter = new ReviewAdapter(getApplicationContext(), arrayList);
//            reviewList = (ListView) findViewById(R.id.all_review_list);
//            closeButton = (Button) findViewById(R.id.close_dialog);
//            closeButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dismiss();
//                }
//            });
//            reviewList.setAdapter(reviewAdapter);
//        }
//    }
}
