package com.iiitkottayam.sam.urbuddy;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class main extends AppCompatActivity {

    private DatabaseReference mRootRef;
    TextView menu_price;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    public Spinner mySpinner;
    java.util.Date d;
    java.util.Date f, t, day;
    String time, dy;
    Date currentTime;
    String[] user;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySpinner = (Spinner) findViewById(R.id.spinner);
        menu_price = (TextView) findViewById(R.id.menu_price);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking Status...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);


        checkInternetConnection();

        //check internet
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            firebaseAuth = FirebaseAuth.getInstance();
            currentUser = firebaseAuth.getCurrentUser();

            //setting action bar as current users details
            ActionBar ab = getSupportActionBar();
            ab.setTitle("Logged in as ");
            ab.setSubtitle(firebaseAuth.getCurrentUser().getEmail());

            //
            DatabaseReference smenuroot = FirebaseDatabase.getInstance().getReferenceFromUrl("https://ur-buddy.firebaseio.com");

            mRootRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://ur-buddy.firebaseio.com/Users");
            //

            if (isTimeAutomatic(getApplicationContext())) {
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
                                    progressDialog.dismiss();
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //
                    if (f.after(t)) {
                        // checking time constraint

                        user = currentUser.getEmail().split("@");
                        DatabaseReference childRef = mRootRef.child(user[0]);
                        final DatabaseReference secondChild = childRef.child("Flag");
                        final DatabaseReference thirdChild = childRef.child("Quantity");
                        final DatabaseReference fourthChild = childRef.child("Update Date");
                        secondChild.setValue("0");             //removing credibility and for checkin databse already created or not
                        mRootRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if (!ds.hasChild("Quantity")) {
                                        secondChild.setValue("1");
                                        thirdChild.setValue("1");
                                        fourthChild.setValue("04/Sep/2017");           //dummy date
                                    }
                                    String Udate = ds.child("Update Date").getValue().toString();
                                    try {
                                        Date Updated = formatter_to.parse(Udate);
                                        if (day.before(Updated) || day.equals(Updated)) {

                                            Intent in = new Intent(main.this, Already_done.class);
                                            startActivity(in);
                                            main.this.finish();
                                            break;
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        // variable day is to be compared with database stored day
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Out of Order Time", Toast.LENGTH_LONG).show();
                        Intent in = new Intent(main.this, Already_done.class);
                        startActivity(in);
                        main.this.finish();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                progressDialog.dismiss();
                AlertDialog.Builder dialog = new AlertDialog.Builder(main.this);
                dialog.setCancelable(false);
                dialog.setTitle("Time is set on manual");
                dialog.setMessage("Please set Time Updation Automatic Setting > Date and Time");
                dialog.show();
            }

        } else {
            // not connected to the internet
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
            // notify user you are not online

        }
    }

    public void acceptpressed(View v) {
        final String text = mySpinner.getSelectedItem().toString();


        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            //formatting current date

            if (isTimeAutomatic(getApplicationContext())) {
                currentTime = Calendar.getInstance().getTime();

                String date = currentTime + "";
                SimpleDateFormat formatter_from = new SimpleDateFormat("EEE MMM dd hh:mm:ss ZZ yyyy");
                final SimpleDateFormat formatter_to = new SimpleDateFormat("dd/MMM/yyyy");
                SimpleDateFormat formatter_time = new SimpleDateFormat("HH:mm");     // to check if formater
                try {
                    d = formatter_from.parse(date);
                    time = formatter_time.format(d);
                    dy = formatter_to.format(d);
                    day = formatter_to.parse(dy);
                    t = formatter_time.parse(time);             // to compare again converting
                    f = formatter_time.parse("11:59");             /// this we have to get from database


                    if (f.after(t)) {                         // checking time constraint                   // change to before later


                        user = currentUser.getEmail().split("@");
                        DatabaseReference childRef = mRootRef.child(user[0]);
                        final DatabaseReference secondChild = childRef.child("Flag");
                        final DatabaseReference thirdChild = childRef.child("Quantity");
                        final DatabaseReference fourthChild = childRef.child("Update Date");

                        mRootRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                    userinfo uinfo = new userinfo();
                                    uinfo.setFlag(ds.child("Flag").getValue().toString());
                                    uinfo.setQuantity(ds.child("Quantity").getValue().toString());
                                    uinfo.setUpdate(ds.child("Update Date").getValue().toString());


                                    String Udate = uinfo.getUpdate();
                                    try {
                                        Date Updated = formatter_to.parse(Udate);
                                        if (day.after(Updated)) {

                                            secondChild.setValue("1");
                                            thirdChild.setValue(text);
                                            fourthChild.setValue(dy);
                                            Toast.makeText(getApplicationContext(), "Your order have been noted, Happy fooding!", Toast.LENGTH_LONG).show();
                                            Intent in = new Intent(main.this, Already_done.class);
                                            startActivity(in);

                                            main.this.finish();
                                            break;


                                        }else{
                                            Intent in = new Intent(main.this, Already_done.class);
                                            startActivity(in);

                                            main.this.finish();
                                            break;

                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        // variable day is to be compared with database stored day
                    } else {
                        Intent in = new Intent(main.this, Already_done.class);
                        startActivity(in);
                        main.this.finish();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                progressDialog.dismiss();
                AlertDialog.Builder dialog = new AlertDialog.Builder(main.this);
                dialog.setCancelable(false);
                dialog.setTitle("Time is set on manual");
                dialog.setMessage("Please set Time Updation Automatic Setting > Date and Time");
                dialog.show();
            }


        } else {
            // not connected to the internet

            Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
            // notify user you are not online

        }

    }

    public void logoutpressed(View v) {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            firebaseAuth.signOut();
            Intent I = new Intent(getApplicationContext(), LoginActivity.class);  //loging activity
            startActivity(I);
            main.this.finish();
        } else {
            // not connected to the internet

            Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
            // notify user you are not online

        }
    }

//    public boolean checkLocationPermission() {
//        String permission = "android.permission.ACCESS_FINE_LOCATION";
//        int res = this.checkCallingOrSelfPermission(permission);
//        return (res == PackageManager.PERMISSION_GRANTED);
//    }

    private void checkInternetConnection() {

        BroadcastReceiver br = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle extras = intent.getExtras();

                NetworkInfo info = (NetworkInfo) extras
                        .getParcelable("networkInfo");

                NetworkInfo.State state = info.getState();

                Log.d("TEST Internet", info.toString() + " "
                        + state.toString());

                if (state == NetworkInfo.State.CONNECTED) {
                    Toast.makeText(getApplicationContext(), "Internet connection is on", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getApplicationContext(), "Internet connection is off", Toast.LENGTH_LONG).show();
                }

            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver((BroadcastReceiver) br, intentFilter);
    }


    public static boolean isTimeAutomatic(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(c.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0) == 1;
        }
    }

}
