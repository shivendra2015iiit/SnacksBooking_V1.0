package com.iiitkottayam.sam.urbuddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    Button login;
    ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser!=null){
            if(currentUser.isEmailVerified()) {
                Intent I = new Intent(getApplicationContext(), main.class);  //launch the main drawer activity
                startActivity(I);
                LoginActivity.this.finish();
            }
            else{
                firebaseAuth.signOut();
                  progressDialog.dismiss();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("Verify YOUR EMAIL ID");
                    dialog.setMessage("Please verify your email id by clicking on the link sent to your mail id and restart app");
                    dialog.show();

                Toast.makeText(getApplicationContext(), R.string.verifyemail, Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        //initializing login button
        login = (Button) findViewById(R.id.login);
        //setting OnClickListner to login
        login.setOnClickListener(onClickListener);

        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                Intent I = new Intent(getApplicationContext(), main.class);  //launch the main drawer activity
                startActivity(I);
                LoginActivity.this.finish();
            } else {
                firebaseAuth.signOut();
                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("Verify YOUR EMAIL ID");
                dialog.setMessage("Please verify your email id by clicking on the link sent to your mail id and restart app");
                dialog.show();

                Toast.makeText(getApplicationContext(), R.string.verifyemail, Toast.LENGTH_LONG).show();
            }


        }
    }

    // function to launch signulp activity

    public void launchsignup(View v){
        progressDialog.setMessage("Please wait");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        Intent I = new Intent(LoginActivity.this,signup.class);
        startActivity(I);
        LoginActivity.this.finish();
    }

    //function to reset email

    public void sendpassresetlink(View v){
        String E = email.getText().toString().trim();
        if(TextUtils.isEmpty(E) || !android.util.Patterns.EMAIL_ADDRESS.matcher(E).matches()){
            Snackbar.make(v, R.string.validid, Snackbar.LENGTH_LONG).show();
            return;
        }
        if(!checkdomain( E)){
            Snackbar.make(v, R.string.no_valid_domain, Snackbar.LENGTH_LONG).show();
            return;
        }
        firebaseAuth.sendPasswordResetEmail(E);
        Snackbar.make(v, R.string.resetLink, Snackbar.LENGTH_LONG).show();
    }



    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String pass = password.getText().toString().trim();
            String E = email.getText().toString().trim();

            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            final FirebaseUser[] tempuser = new FirebaseUser[1];
            if(currentUser!=null){
                if(!currentUser.isEmailVerified()) {
                    firebaseAuth.signOut();
                    Toast.makeText(getApplicationContext(), R.string.verifyemail, Toast.LENGTH_LONG).show();
                }


            }else {
                //checking if username and password are not empty
                if (TextUtils.isEmpty(pass) || TextUtils.isEmpty(E)) {
                    Snackbar.make(v, R.string.fillall, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // checking for invalid email-id formate
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(E).matches()) {
                    Toast t = Toast.makeText(getApplicationContext(), R.string.validid, Toast.LENGTH_SHORT);
                    t.show();
                    return;
                }

                // check the domain address  AT THIS TIME ONLY @IIITKOTTAYAM.AC.IN WILL BE ALLOWED(Usage 1/2 Another usage signup class)
                // WE CAN GIVE UPDATE TO APP BY ADDING MORE DOMAINS IN SELECTOR

                if (!checkdomain(E)) {
                    Snackbar.make(v, R.string.no_valid_domain, Snackbar.LENGTH_LONG).show();
                    return;
                }
                // check if password is 7 digits
                if (pass.length() < 7) {
                    Snackbar.make(v, R.string.passlen, Snackbar.LENGTH_SHORT).show();
                    return;
                }


                progressDialog.setMessage("Logging In");
                progressDialog.show();
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    // everthing is syntaxtically allright
                    firebaseAuth.signInWithEmailAndPassword(E, pass).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                tempuser[0] = firebaseAuth.getCurrentUser();
                                if(tempuser[0].isEmailVerified()) {
                                    progressDialog.dismiss();
                                    Intent I = new Intent(getApplicationContext(), main.class);  //launch the main activity
                                    startActivity(I);
                                    LoginActivity.this.finish();
                                }else{
                                    firebaseAuth.signOut();
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                                    dialog.setCancelable(false);
                                    dialog.setTitle("Verify YOUR EMAIL ID");
                                    dialog.setMessage("Please verify your email id by clicking on the link sent to your mail id and restart app");
                                    dialog.show();

                                    Toast.makeText(getApplicationContext(), R.string.verifyemail, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this,
                                        R.string.nologin
                                        , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    // not connected to the internet
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
                    // notify user you are not online

                }

            }
        }

    };
    // function to varify domains
    public boolean checkdomain(String email) {
        return email.contains("@iiitkottayam.ac.in");
    }


}








