package chethan.com.cabpromo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import icepick.Icepick;
import icepick.Icicle;
import me.alexrs.prefs.lib.Prefs;
import timber.log.Timber;


public class LoginActivity extends Activity {

    @InjectView(R.id.loginBtn)Button loginBtn;
    @InjectView(R.id.signupBtn)Button signUpBtn;
    @InjectView(R.id.emailAddress)EditText emailText;
    @InjectView(R.id.password)EditText passwordText;
    @InjectView(R.id.login_logoView)ImageView loginLogoImage;

    @Icicle String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.inject(this);
        Icepick.restoreInstanceState(this, savedInstanceState);
        Timber.plant(new Timber.DebugTree());


        //ParseUser.logOut();
        initializeLoginData();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser user = new ParseUser();
                user.setUsername(emailText.getText().toString());
                user.setEmail(emailText.getText().toString());
                user.setPassword(passwordText.getText().toString());

                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        SweetAlertDialog alertDialog;
                        if (e == null) {
                            Timber.d("Signup successfull");
                            //save the email and team name
                            Prefs.with(getApplicationContext()).save(Constants.USER_EMAIL_ID,ParseUser.getCurrentUser().getEmail());

                            alertDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                            alertDialog.setTitleText("Success").setContentText("Successfully signed up").show();

                            //Signup successful. Show main screen. Close the login activity
                            LoginActivity.this.finish();
                            Intent calIntent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(calIntent);
                        } else {
                            Timber.d("error" + e.getMessage()+e.getCode());
                            alertDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE);
                                    alertDialog.setTitleText("Oops...")
                                    .setContentText("Try again").show();

                        }
                    }
                });
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show progress spinner while logging in and cancel, once login returns
                final SweetAlertDialog pDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialog.setTitleText("Logging in ..");
                pDialog.setCancelable(false);
                pDialog.show();

                ParseUser.logInInBackground(emailText.getText().toString()
                        , passwordText.getText().toString(), new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        pDialog.cancel(); //cancelling the progress spinner
                        SweetAlertDialog alertDialog;
                        if (parseUser != null) {
                            Prefs.with(getApplicationContext()).save(Constants.USER_EMAIL_ID,ParseUser.getCurrentUser().getEmail());

                            alertDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                            alertDialog.setTitleText("Success").setContentText("Successfully logged in").show();

                            //Login successful. Show main screen. Close the login activity
                            LoginActivity.this.finish();
                            Intent calIntent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(calIntent);

                        } else {
                            Timber.d("could not log in "+e.getMessage()+e.getCode());

                            if (e.getCode() == 101){//Invalid Login credentials
                                alertDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Oops...")
                                        .setContentText("Invalid Login credentials. Try again");
                                alertDialog.show();
                            }else {//Either there is no Internet connection or something else is wrong.
                                alertDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Oops...")
                                        .setContentText("Something went wrong! Check your internet connection and try again");
                                alertDialog.show();
                            }
                        }
                    }
                });

            }
        });


    }


    private void initializeLoginData(){
        if(ParseUser.getCurrentUser() != null){
            //already user logged in. Take to main screen
            LoginActivity.this.finish();
            Intent calIntent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(calIntent);
        }

        //Initailize any more login data
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }
}
