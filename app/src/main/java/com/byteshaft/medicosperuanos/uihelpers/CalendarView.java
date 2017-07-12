package com.byteshaft.medicosperuanos.uihelpers;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;
import com.byteshaft.medicosperuanos.utils.AppGlobals;
import com.byteshaft.medicosperuanos.utils.Helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by a7med on 28/06/2015.
 */

public class CalendarView extends LinearLayout {

    // for logging
    private static final String LOGTAG = "Calendar View";

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 7;

    // default date format
    private static final String DATE_FORMAT = "DD MMM yyyy";

    // date format
    private String dateFormat;

    // current displayed month
    public static Calendar currentDate = Calendar.getInstance();

    //event handling
    private EventHandler eventHandler = null;

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;
    private GridView weekGrid;
    private Date selectedDate;
    private CalendarAdapter calendarAdapter;
    private Context mContext;
    private boolean canGoBack = false;

    public void setCanGoBack(boolean canGoBack) {
        this.canGoBack = canGoBack;
    }

    public CalendarView(Context context) {
        super(context);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initControl(context, attrs);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_calendar, this);
        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();
        updateCalendar();
    }

    private void loadDateFormat(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);
        try {
            // try to load provided date format, and fallback to default otherwise
            dateFormat = ta.getString(R.styleable.CalendarView_dateFormat);
            if (dateFormat == null)
                dateFormat = DATE_FORMAT;
        } finally {
            ta.recycle();
        }
    }

    private void assignUiElements() {
        // layout is inflated, assign local variables to components
        header = (LinearLayout) findViewById(R.id.calendar_header);
        btnPrev = (ImageView) findViewById(R.id.calendar_prev_button);
        btnNext = (ImageView) findViewById(R.id.calendar_next_button);
        txtDate = (TextView) findViewById(R.id.calendar_date_display);
        grid = (GridView) findViewById(R.id.calendar_grid);
        weekGrid = (GridView) findViewById(R.id.week_grid);
    }

    private void assignClickHandlers() {
        // add one month and refresh UI
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.DAY_OF_YEAR, 1);
                selectedDate = currentDate.getTime();
                eventHandler.onDayPress(selectedDate);
                updateCalendar();
            }
        });

        // subtract one month and refresh UI
        btnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = (Calendar) currentDate.clone();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
                Date dateOne = null;
                Date dateTwo = null;
                try {
                    dateOne = sdf.parse(sdf.format(calendar.getTime()));
                    dateTwo = sdf.parse(Helpers.getDateForComparison());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (dateOne.compareTo(dateTwo) < 0) {
                    if (!canGoBack)
                        Helpers.showSnackBar(getRootView().findViewById(android.R.id.content), R.string.cannot_go_back_from_current_date);
                    else
                        currentDate.add(Calendar.DAY_OF_YEAR, -1);
                        selectedDate = currentDate.getTime();
                        eventHandler.onDayPress(selectedDate);
                        updateCalendar();

                } else {
                    currentDate.add(Calendar.DAY_OF_YEAR, -1);
                    selectedDate = currentDate.getTime();
                    eventHandler.onDayPress(selectedDate);
                    updateCalendar();
                }
            }
        });

        // long-pressing a day
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                eventHandler.onDayPress((Date) adapterView.getItemAtPosition(i));
                Date date = (Date) adapterView.getItemAtPosition(i);
                DateFormat df = SimpleDateFormat.getDateInstance();
                String resultDate = df.format(date);
                currentDate.setTime(date);
                txtDate.setText(resultDate);
                selectedDate = (Date) adapterView.getItemAtPosition(i);
                calendarAdapter.notifyDataSetChanged();

            }
        });
    }

    /**
     * Display dates correctly in hori
     */
    public void updateCalendar() {
        updateCalendar(null, null);
    }

    public void updateCalendar(Date date) {
        selectedDate = date;
        updateCalendar(null, null);
    }

    /**
     * Display dates correctly in hori
     */
    public void updateCalendar(HashSet<Date> events, Date date) {
        ArrayList<Date> cells = new ArrayList<>();
//        ArrayList<String> weekDay = new ArrayList<>();
        Calendar calendar = (Calendar) currentDate.clone();
        while (cells.size() < DAYS_COUNT) {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
        }
        weekGrid.setAdapter(new WeekAdapter(getContext(), cells));
        // update calendar
        calendarAdapter = new CalendarAdapter(getContext(), cells, events);
        grid.setAdapter(calendarAdapter);
        // update title
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        txtDate.setText(sdf.format(currentDate.getTime()));
    }


    private class CalendarAdapter extends ArrayAdapter<Date> {
        // days with events
        private HashSet<Date> eventDays;
        // for view inflation
        private LayoutInflater inflater;
        private Date todayDate;

        public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays) {
            super(context, R.layout.control_calendar_day, days);
            this.eventDays = eventDays;
            inflater = LayoutInflater.from(context);
            todayDate = new Date();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Date date = getItem(position);
            int day = date.getDate();
            int month = date.getMonth();
            int year = date.getYear();

            Date today = new Date();

            if (view == null)
                view = inflater.inflate(R.layout.control_calendar_day, parent, false);
            if (eventDays != null) {
                for (Date eventDate : eventDays) {
                    if (eventDate.getDate() == day &&
                            eventDate.getMonth() == month &&
                            eventDate.getYear() == year) {
                        // mark this day for event
                        break;
                    }
                }
            }
            if (selectedDate != null && selectedDate.getDate() == todayDate.getDate()) {
                selectedDate = null;
            }
            if (selectedDate != null && selectedDate.getDate() == todayDate.getDate()) {
                int[] array = getResources().getIntArray(R.array.selected);
                final Resources resources = AppGlobals.getContext().getResources();
                final BitmapWithCharacter tileProvider = new BitmapWithCharacter
                        (resources.obtainTypedArray(R.array.selected));
                final Bitmap letterTile = tileProvider.getLetterTile(String.valueOf(day),
                        String.valueOf(array[0]), 100, 100);
                ((ImageView) view).setImageBitmap(letterTile);
            } else if (day == today.getDate() && selectedDate == null && month == today.getMonth()) {
                int[] array = getResources().getIntArray(R.array.selected);
                final Resources resources = AppGlobals.getContext().getResources();
                final BitmapWithCharacter tileProvider = new BitmapWithCharacter
                        (resources.obtainTypedArray(R.array.selected));
                final Bitmap letterTile = tileProvider.getLetterTile(String.valueOf(day),
                        String.valueOf(array[0]), 100, 100);
                ((ImageView) view).setImageBitmap(letterTile);
            } else if (selectedDate != null && selectedDate.getDate() == date.getDate() && selectedDate.getDate() != todayDate.getDate()) {
                int[] array = getResources().getIntArray(R.array.other_date);
                final Resources resources = AppGlobals.getContext().getResources();
                final BitmapWithCharacter tileProvider = new BitmapWithCharacter
                        (resources.obtainTypedArray(R.array.other_date));
                final Bitmap letterTile = tileProvider.getLetterTile(String.valueOf(day),
                        String.valueOf(array[0]), 100, 100);
                ((ImageView) view).setImageBitmap(letterTile);
            } else {
                int[] array = getResources().getIntArray(R.array.not_selected);
                final Resources resources = AppGlobals.getContext().getResources();
                final BitmapWithCharacter tileProvider = new BitmapWithCharacter
                        (resources.obtainTypedArray(R.array.not_selected));
                final Bitmap letterTile = tileProvider.getLetterTile(String.valueOf(day),
                        String.valueOf(array[0]), 100, 100);
                ((ImageView) view).setImageBitmap(letterTile);
            }

            return view;
        }
    }

    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler {
        void onDayPress(Date date);
    }

    private class WeekAdapter extends ArrayAdapter<Date> {
        // days with events

        // for view inflation
        private LayoutInflater inflater;

        public WeekAdapter(Context context, ArrayList<Date> weekDay) {
            super(context, R.layout.single_week_day, weekDay);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            // day in question
            Date date = getItem(position);
            // inflate item if it does not exist yet
            if (view == null)
                view = inflater.inflate(R.layout.single_week_day, parent, false);

            // if this day has an event, specify event image
            // set text
            SimpleDateFormat sdf = new SimpleDateFormat("EEE");
            String dayOfTheWeek = sdf.format(date.getTime());
            ((TextView) view).setText(dayOfTheWeek);
            if (dayOfTheWeek.equals("Sun")) {
                ((TextView) view).setTextColor(getResources().getColor(R.color.appointment_bg));
            }
            return view;
        }
    }
}
