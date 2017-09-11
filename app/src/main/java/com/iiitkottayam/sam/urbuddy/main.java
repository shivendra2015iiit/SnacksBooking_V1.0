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

            user = currentUser.getEmail().split("@");

            DatabaseReference smenuroot = FirebaseDatabase.getInstance().getReferenceFromUrl("https://ur-buddy.firebaseio.com");

            mRootRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://ur-buddy.firebaseio.com/Users/"+user[0]);
            //

            if (isTimeAutomatic(getApplicationContext())) {
                currentTime = Calendar.getInstance().getTime();


                String date = currentTime + "";
                SimpleDateFormat formatter_from = new SimpleDateFormat("EEE MMM dd kk:mm:ss ZZ yyyy");
                final SimpleDateFormat formatter_to = new SimpleDateFormat("dd/MMM/yyyy");
                SimpleDateFormat format_day = new SimpleDateFormat("EEE");
                SimpleDateFormat formatter_time = new SimpleDateFormat("kk:mm");     // to check if formater
                try {
                    d = formatter_from.parse(date);
                    final String wday = format_day.format(d);
                    time = formatter_time.format(d);
                    dy = formatter_to.format(d);
                    day = formatter_to.parse(dy);
                    t = formatter_time.parse(time);
                    f = formatter_time.parse("11:59");             /// this we have to get from database

                    // trial code//////////////////////////////////////////////////////////

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
                    if (f.after(t)) {                        //change to after


                        mRootRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getChildrenCount()==0){
                                    mRootRef.child("Flag").setValue("0");
                                    mRootRef.child("Quantity").setValue("0");
                                    mRootRef.child("Update Date").setValue("04/Sep/2017");
                                }
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                    try {
                                        if (ds.getKey().equals("Update Date")) {

                                            if (ds.getValue() != null) {
                                                String Udate = ds.getValue().toString();
                                                Date Updated = formatter_to.parse(Udate);
                                                if (day.before(Updated) || day.equals(Updated)) {
                                                    progressDialog.dismiss();
                                                    Intent in = new Intent(main.this, Already_done.class);
                                                    startActivity(in);
                                                    main.this.finish();
                                                    break;
                                                }
                                                else{
                                                    progressDialog.dismiss();
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Database error", Toast.LENGTH_SHORT).show();
                                                main.this.finish();
                                                break;
                                            }
                                        }

//
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });
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

                ///////////////////////////////////////////////////////////
                String date = currentTime + "";
                SimpleDateFormat formatter_from = new SimpleDateFormat("EEE MMM dd kk:mm:ss ZZ yyyy");
                final SimpleDateFormat formatter_to = new SimpleDateFormat("dd/MMM/yyyy");
                SimpleDateFormat format_day = new SimpleDateFormat("EEE");
                SimpleDateFormat formatter_time = new SimpleDateFormat("kk:mm");     // to check if formater
                try {
                    d = formatter_from.parse(date);
                    time = formatter_time.format(d);
                    dy = formatter_to.format(d);
                    day = formatter_to.parse(dy);
                    t = formatter_time.parse(time);
                    f = formatter_time.parse("11:59");             /// this we have to get from database



                    if (f.after(t)) {

                        mRootRef.child("Flag").setValue("1");
                        mRootRef.child("Quantity").setValue(text);
                        mRootRef.child("Update Date").setValue(dy);
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Your order have been noted, Happy fooding!", Toast.LENGTH_LONG).show();
                        Intent in = new Intent(main.this, Already_done.class);
                        startActivity(in);
                        main.this.finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Out of Order Time", Toast.LENGTH_LONG).show();
                        Intent in = new Intent(main.this, Already_done.class);
                        startActivity(in);
                        main.this.finish();
                    }
                    //////////////////////////////////////////////////////////

                }catch (ParseException e) {
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
