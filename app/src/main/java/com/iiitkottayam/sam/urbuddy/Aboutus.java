package com.iiitkottayam.sam.urbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class Aboutus extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);



    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Aboutus.this,
                LoginActivity.class);
        startActivity(intent);
        Aboutus.this.finish();
    }

}
