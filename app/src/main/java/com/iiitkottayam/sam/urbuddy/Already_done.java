package com.iiitkottayam.sam.urbuddy;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Already_done extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    java.util.Date d;
    java.util.Date f,t,day;
    String time,dy;
    Date currentTime;
    TextView menu_price;
    DatabaseReference smenuroot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_already_done);
        Window window = this.getWindow();
        firebaseAuth = FirebaseAuth.getInstance();
        menu_price=(TextView)findViewById(R.id.menu_price);
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             //window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBlue));
        }




        ActionBar ab=getSupportActionBar();
        ab.setTitle("Logged in as ");
        ab.setSubtitle(firebaseAuth.getCurrentUser().getEmail());
            //
            smenuroot = FirebaseDatabase.getInstance().getReferenceFromUrl("https://ur-buddy.firebaseio.com");

            if(isTimeAutomatic(getApplicationContext())) {

            currentTime = Calendar.getInstance().getTime();

            String date = currentTime + "";
            SimpleDateFormat formatter_from = new SimpleDateFormat("EEE MMM dd hh:mm:ss ZZ yyyy");
            final SimpleDateFormat formatter_to = new SimpleDateFormat("dd/MMM/yyyy");
            SimpleDateFormat format_day = new SimpleDateFormat("EEE");
            SimpleDateFormat formatter_time = new SimpleDateFormat("HH:mm");     // to check if formater
            try {
                d = formatter_from.parse(date);
                final String wday = format_day.format(d);
                time = formatter_time.format(d);
                dy = formatter_to.format(d);
                day = formatter_to.parse(dy);
                t = formatter_time.parse(time);
                f = formatter_time.parse("11:59");             /// this we have to get from database

                //setting menu

                smenuroot.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            if (ds.getKey().equals("SnackMenu")) {
                                menureading menu = new menureading();
                                menu.setMenuvalue(ds.child(wday).getValue().toString());
                                String menuvalue = menu.getMenuvalue();
                                menu_price.setText(menuvalue);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //


            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else{
            Snackbar.make(getCurrentFocus(), "Please set Time Updation Automatic to Continue", Snackbar.LENGTH_INDEFINITE).show();
        }

    }
    public void logoutpressed(View v){
        firebaseAuth.signOut();
        Intent I = new Intent(getApplicationContext(),LoginActivity.class) ;  //loging activity
        startActivity(I);
        Already_done.this.finish();
    }
    public void aboutuspressed(View v){
        Intent I = new Intent(getApplicationContext(),Aboutus.class) ;  //loging activity
        startActivity(I);
        Already_done.this.finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Already_done.this.finish();
    }

    public static boolean isTimeAutomatic(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(c.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0) == 1;
        }
    }
}
