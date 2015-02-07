package chethan.com.cabpromo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.timroes.android.listview.EnhancedListView;
import hugo.weaving.DebugLog;
import icepick.Icepick;
import icepick.Icicle;
import me.alexrs.prefs.lib.Prefs;
import timber.log.Timber;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    @Icicle
    ArrayList<String> promoCodeList = new ArrayList<String>();

    @Icicle int navigationDrawerSelection = 0;

    @InjectView(R.id.AddPromoCode_promoListView)
    EnhancedListView promoCodeListView;

    @InjectView(R.id.progress_wheel)
    ProgressWheel loading;

    @InjectView(R.id.AddPromoCode_codeText)EditText codeEditText;
    @InjectView(R.id.AddPromoCode_addBtn)Button addCodeBtn;
    @InjectView(R.id.AddPromoCodeLayout)LinearLayout promoCodeLayout;
    @InjectView(R.id.AddPromoRetry)ImageView retryImage;
    Boolean firstRun = true;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        Icepick.restoreInstanceState(this, savedInstanceState);
        Timber.plant(new Timber.DebugTree());

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        promoCodeListView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return promoCodeList.size();
            }

            @Override
            public Object getItem(int position) {
                return promoCodeList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;

                if(convertView == null){
                    viewHolder = new ViewHolder();
                    convertView = getLayoutInflater().inflate(R.layout.cab_promo_item,parent,false);
                    viewHolder.promoCode = (TextView)convertView.findViewById(R.id.cab_promo_item_code);

                    viewHolder.promoCode.setTypeface(Utils.getRegularTypeface(getApplicationContext()));
                    convertView.setTag(viewHolder);
                }else{
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.promoCode.setText(promoCodeList.get(position));
                return convertView;
            }
        });

        promoCodeListView.setUndoStyle(EnhancedListView.UndoStyle.SINGLE_POPUP);
        promoCodeListView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView enhancedListView,final int i) {
                final String item = promoCodeList.get(i);

                promoCodeList.remove(i);
                ((BaseAdapter)promoCodeListView.getAdapter()).notifyDataSetChanged();

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                        promoCodeList.add(i,item);
                        ((BaseAdapter)promoCodeListView.getAdapter()).notifyDataSetChanged();
                    }

                    @Override
                    public void discard() {
                        super.discard();
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
                        query.whereEqualTo("email", ParseUser.getCurrentUser().getEmail());
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> parseObjects, ParseException e) {
                                ParseUser user = ParseUser.getCurrentUser();
                                for(ParseObject object :parseObjects){
                                    String usedCodes = object.getString(Constants.USED_PROMO_CODE_COLUMN);
                                    if(usedCodes.equalsIgnoreCase("null"))
                                        usedCodes="";
                                    usedCodes+=item+"|";
                                    Prefs.with(getApplicationContext()).save(Constants.USED_PROMO_CODE_COLUMN,usedCodes);
                                    user.put(Constants.USED_PROMO_CODE_COLUMN,usedCodes);
                                    user.saveEventually();
                                }
                            }
                        });
                    }
                };
            }
        });

        promoCodeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //copy to clipboard
                Utils.copyToClipboard(getApplicationContext(),promoCodeList.get(position));
                Toast.makeText(getApplicationContext(),"Promo code copied",Toast.LENGTH_SHORT).show();

            }
        });

        promoCodeListView.setUndoHideDelay(3000);
        promoCodeListView.setRequireTouchBeforeDismiss(false);
        promoCodeListView.enableSwipeToDismiss();

        codeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(codeEditText.getText().toString().equalsIgnoreCase("")){
                    addCodeBtn.setEnabled(false);
                }else{
                    addCodeBtn.setEnabled(true);
                }
            }
        });

        addCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject codeToAdd = new ParseObject(cabType(navigationDrawerSelection));
                codeToAdd.put(Constants.PROMO_CODE_COLUMN,codeEditText.getText().toString());
                codeToAdd.put(Constants.USER_EMAIL_ID,ParseUser.getCurrentUser().getEmail());
                codeToAdd.saveEventually();
                codeEditText.setText("");
                promoCodeLayout.setVisibility(View.GONE);
            }
        });


        retryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.spin();
                retryImage.setVisibility(View.GONE);
                retrievePromoCodeFromParseForCabType(cabType(navigationDrawerSelection));
            }
        });
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
//        Toast.makeText(getApplicationContext(),"Cab type "+position,Toast.LENGTH_SHORT).show();
        navigationDrawerSelection = position;
        //Retrieve Promo codes from parse for the Cab Type

        retrievePromoCodeFromParseForCabType(cabType(position));
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
        }
    }

    private String cabType(int type){
        switch (type) {
            case 0:

                return Constants.UBER_TABLE;

            case 1:

                return Constants.OLA_TABLE;

            case 2:

                return Constants.TAXI_FOR_SURE_TABLE;

            case 3:

                return Constants.MERU_TABLE;

        }
        return null;
    }

    @DebugLog
    private void retrievePromoCodeFromParseForCabType(final String cabType){
        if(firstRun)
            firstRun=false;
        else {
            loading.spin();
            retryImage.setVisibility(View.GONE);
        }
        promoCodeList.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(cabType);
        query.whereNotEqualTo(Constants.USER_EMAIL_ID, ParseUser.getCurrentUser().getEmail());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e!=null){//something wrong
                    Timber.e("Could not get Promo code for "+cabType+". Error msg and code "+e.getMessage()+e.getCode());
                    if(e.getCode() == 100) { // no internet connection
                        retryImage.setVisibility(View.VISIBLE);
                        retryImage.setImageResource(R.drawable.nonet);
                    }
                }else{
                    retryImage.setVisibility(View.GONE);

                    ArrayList<String> usedCodesList = Utils.parseDelimitedStringToList(Prefs.with(getApplicationContext()).getString(Constants.USED_PROMO_CODE_COLUMN,""));
                    for(ParseObject parseObject:parseObjects){
                        String promoCode = parseObject.getString(Constants.PROMO_CODE_COLUMN);

                        if(!usedCodesList.contains(promoCode)) {
                            Timber.d("Promo code " + promoCode);
                            promoCodeList.add(promoCode);
                        }
                    }
                    ((BaseAdapter)promoCodeListView.getAdapter()).notifyDataSetChanged();
                    if(promoCodeList.size()==0){
                        retryImage.setVisibility(View.VISIBLE);
                        retryImage.setImageResource(R.drawable.nopromo);
                    }
                }
                loading.stopSpinning();
            }
        });
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            Intent settings = new Intent(MainActivity.this,SettingsActivity.class);
//            startActivity(settings);
            ParseUser.logOut();
            Intent loginView = new Intent(MainActivity.this,LoginActivity.class);
            MainActivity.this.finish();
            startActivity(loginView);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    class ViewHolder{
        TextView promoCode;
    }
}
