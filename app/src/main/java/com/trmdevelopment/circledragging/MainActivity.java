package com.trmdevelopment.circledragging;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private CirclesDrawingView mCirclesDrawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCirclesDrawingView = (CirclesDrawingView) findViewById( R.id.circles_view );
    }
}
