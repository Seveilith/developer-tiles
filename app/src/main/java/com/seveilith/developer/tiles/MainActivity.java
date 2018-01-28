package com.seveilith.developer.tiles;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = this;
        super.onCreate(savedInstanceState);

        if (Utils.isSystemApp(context)) {
            startActivity(new Intent(context, SystemAppActivity.class));
        } else {
            startActivity(new Intent(context, PrereqActivity.class));
        }
    }
}