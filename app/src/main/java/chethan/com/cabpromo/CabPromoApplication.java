package chethan.com.cabpromo;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by chethan on 30/01/15.
 */
public class CabPromoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "drrYQFrYvB1V6gFkQo7cIzmduSWiIuKacAAecT2E", "z1UCBq9sDbKJVuowhtVaa6eIsdlK7BRZ3OFBbXEu");
    }
}
