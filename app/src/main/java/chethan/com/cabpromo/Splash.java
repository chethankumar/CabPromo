package chethan.com.cabpromo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by chethan on 02/02/15.
 */
public class Splash extends Activity {

    @InjectView(R.id.SplashLogo)
    TextView splashLogo;

    @InjectView(R.id.SplashText)
    TextView splashText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        ButterKnife.inject(this);

        splashLogo.setTypeface(Utils.getAlexTypeface(getApplicationContext()));
        splashText.setTypeface(Utils.getRegularTypeface(getApplicationContext()));

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Splash.this.finish();
                Intent loginIntent = new Intent(Splash.this,LoginActivity.class);
                startActivity(loginIntent);
            }
        }, 3000);


    }
}
