package com.example.nogboardinset;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("NoGboardBottomInset v2 attivo.\n\n" +
                "Attiva in Vector/LSPosed con scope SOLO Gboard, poi riavvia.");
        tv.setPadding(32, 64, 32, 32);
        setContentView(tv);
    }
}
