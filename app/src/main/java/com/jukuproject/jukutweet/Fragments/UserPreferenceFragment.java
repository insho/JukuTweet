package com.jukuproject.jukutweet.Fragments;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.jukuproject.jukutweet.BuildConfig;
import com.jukuproject.jukutweet.R;

public class UserPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    String TAG = "TEST-UserPref";
    String sliderMultiplier = "3.0";
    SharedPreferences prefs;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final float  redthreshold = Float.parseFloat(prefs.getString("preference_redthreshold", ".3"));
        final float  yellowthreshold = Float.parseFloat(prefs.getString("preference_yellowthreshold", ".8"));


        getView().setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
        getView().setClickable(true);

        addPreferencesFromResource(R.xml.preferences);

        final MultiSelectListPreference listfavoritlists = (MultiSelectListPreference) findPreference("list_favoriteslistcount");

        final Drawable star = ContextCompat.getDrawable(getActivity(), R.drawable.ic_star_black);
        final PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
        star.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorJukuBlue), mMode);
        listfavoritlists.setIcon(star);

        final MultiSelectListPreference listfavoritlists_tweet = (MultiSelectListPreference) findPreference("list_favoriteslistcount_tweet");
        final Drawable star_tweet = ContextCompat.getDrawable(getActivity(), R.drawable.ic_star_multicolor);
        final  PorterDuff.Mode mMode2 = PorterDuff.Mode.SRC_ATOP;
        star_tweet.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorJukuGreen), mMode2);
        listfavoritlists_tweet.setIcon(star_tweet);

        final EditTextPreference redThresholdEditText = (EditTextPreference) findPreference("preference_redthreshold");
        final EditTextPreference yellowThresholdEditText = (EditTextPreference) findPreference("preference_yellowthreshold");
        final EditTextPreference greyThresholdEditText = (EditTextPreference) findPreference("preference_greythreshold");

        greyThresholdEditText.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(BuildConfig.DEBUG){Log.d(TAG,"grey newvalue: " + newValue.toString().trim());}
                String greyNewValue = newValue.toString().trim();
                if(greyNewValue.equals(".")  || greyNewValue.length()==0) {
                    greyNewValue = "0";
                }

                if(Integer.parseInt(greyNewValue) <=0){
                    Toast.makeText(getActivity(), "Grey threshold should be greater than 0", Toast.LENGTH_LONG).show();
                    greyThresholdEditText.setText("3");
                    greyThresholdEditText.setDefaultValue("3");
                } else {
                    greyThresholdEditText.setText(greyNewValue);
                    greyThresholdEditText.setDefaultValue(greyNewValue);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("preference_greythreshold", greyNewValue);
                    editor.putInt("openPopupWindow",-1);
                    editor.apply();
                }
                return false;
            }
        });


        redThresholdEditText.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(BuildConfig.DEBUG){Log.d(TAG,"red newvalue: " + newValue.toString());}
                String redNewValue = newValue.toString().trim();
                if(redNewValue.equals(".")  || redNewValue.length()==0) {
                    redNewValue = "0";
                }
                if(BuildConfig.DEBUG){Log.d(TAG,"yellow: " + yellowthreshold);}

                if(Double.parseDouble(redNewValue) > yellowthreshold) {
                    Toast.makeText(getActivity(), "Yellow threshold should be lower than the green threshold", Toast.LENGTH_LONG).show();

                    redThresholdEditText.setText(String.valueOf(redthreshold));
                    redThresholdEditText.setDefaultValue(String.valueOf(redthreshold));


                } else if(Double.parseDouble(redNewValue) <=0 || Double.parseDouble(redNewValue) >=1){
                    Toast.makeText(getActivity(), "Yellow threshold should be between 0 and .99", Toast.LENGTH_LONG).show();
                    redThresholdEditText.setText(".3");
                    redThresholdEditText.setDefaultValue(".3");
                } else {
                    redThresholdEditText.setText(redNewValue);
                    redThresholdEditText.setDefaultValue(redNewValue);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("preference_redthreshold", redNewValue);
                    editor.putInt("openPopupWindow",-1);
                    editor.apply();
                }
                return false;
            }
        });


        yellowThresholdEditText.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(BuildConfig.DEBUG){Log.d(TAG,"yellow newvalue: " + newValue.toString());}
                String yellowNewValue = newValue.toString().trim();
                if(BuildConfig.DEBUG){Log.d(TAG,"red: " + redthreshold);}
                if(yellowNewValue.equals(".") || yellowNewValue.length()==0) {
                    yellowNewValue = "0";
                }
                if(Double.parseDouble(yellowNewValue) < redthreshold) {
                    Toast.makeText(getActivity(), "Green threshold should be higher than the yellow threshold", Toast.LENGTH_LONG).show();


                    yellowThresholdEditText.setText(String.valueOf(yellowthreshold));
                    yellowThresholdEditText.setDefaultValue(String.valueOf(yellowthreshold));


                } else if(Double.parseDouble(yellowNewValue) <= 0 || Double.parseDouble(yellowNewValue) >=1){
                    Toast.makeText(getActivity(), "Green threshold should be between .01 and .99", Toast.LENGTH_LONG).show();
                    yellowThresholdEditText.setText(".8");
                    yellowThresholdEditText.setDefaultValue(".8");
                } else {
                    yellowThresholdEditText.setText(yellowNewValue);
                    yellowThresholdEditText.setDefaultValue(yellowNewValue);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("preference_yellowthreshold", yellowNewValue);
                    editor.putInt("openPopupWindow",-1);
                    editor.apply();
                }
                return false;
            }
        });


        final ListPreference listPreference = (ListPreference) findPreference("sliderMultiplier");
        sliderMultiplier = prefs.getString("sliderMultiplier", "3");

        setListPreferenceData(listPreference,sliderMultiplier);
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "OLD VALUE: " + sliderMultiplier);
                    Log.d(TAG, "NEW VALUE: " + newValue.toString());
                }
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("favorites_slider", newValue.toString());
                editor.apply();
                sliderMultiplier = newValue.toString();
                setListPreferenceData(listPreference,sliderMultiplier);
                return false;
            }
        });
        listPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(BuildConfig.DEBUG){Log.d(TAG,"slider preference clicked -- current: " + sliderMultiplier);}
                setListPreferenceData(listPreference, sliderMultiplier);
                return false;
            }
        });

    }

    /**
     * Used for the "sliderMultiplier" preference, to translate one of the values in the slider
     * to an integer value that can be set as a default value
     * @param lp the ListPreference object for the slider
     * @param defaultValue default value key
     */
    protected static void setListPreferenceData(ListPreference lp,String defaultValue) {
        CharSequence entries[] = { "1","2","3","4","5"};
        CharSequence entryvalues[] = { "1","2","3","4","5"};
        lp.setEntryValues(entryvalues);
        lp.setEntries(entries);
        int valueIndex = 1;
        switch(defaultValue) {
            case "1":
                valueIndex = 0;
                break;
            case "2":
                valueIndex = 1;
                break;
            case "3":
                valueIndex = 2;
                break;
            case "4":
                valueIndex = 3;
                break;
            case "5":
                valueIndex = 4;
                break;

        }
        lp.setValueIndex(valueIndex); //set to index of your deafult value
        lp.setDefaultValue(defaultValue);

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }



}

