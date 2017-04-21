package com.byteshaft.medicosperuanos.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.byteshaft.medicosperuanos.R;

/**
 * Created by s9iper1 on 2/14/17.
 */

public class FilterDialog extends Dialog implements View.OnClickListener {

    private SeekBar seekBar;
    private Context mContext;
    private TextView seekBarText;
    private ImageButton closeDialog;

    public FilterDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_dialog);
        seekBar = (SeekBar) findViewById(R.id.filter_seek_bar);
        seekBarText = (TextView) findViewById(R.id.seek_bar_text);
        closeDialog = (ImageButton) findViewById(R.id.close_dialog);
        closeDialog.setOnClickListener(this);
        seekBarText.setText(String.valueOf(seekBar.getProgress()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBarText.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close_dialog:
                dismiss();
                break;
        }

    }
}
