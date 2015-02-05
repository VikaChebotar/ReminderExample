package com.example.viktoria.reminderexample;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.Locale;

/**
 * Preference screen of application. Has only one parameter - language. Reload automatically when language changes.
 */
public class PrefFragment extends PreferenceFragment {
    private ActionBar actionBar;
    private String[] lang_array;
    private OnLanguageChangeListener mCallback;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        setHasOptionsMenu(true);
        lang_array = getActivity().getResources().getStringArray(R.array.lang_to_load_array);
        actionBar = (getActivity()).getActionBar();
        Preference pref = findPreference(getString(R.string.langPrefKey));
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //change locale
                Resources res =  getActivity().getBaseContext().getResources();
                Configuration conf =res
                        .getConfiguration();
                Locale locale = new Locale( (String) newValue);
                Locale.setDefault(locale);
                conf.locale =locale;
                res.updateConfiguration(conf, res.getDisplayMetrics());
                mCallback.onLanguageChanged();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getActivity().getString(R.string.action_settings));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.findItem(R.id.action_accept).setVisible(false);
        menu.findItem(R.id.action_sync_birthdays).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Interface to communicate with other fragment through activity, fragment shouldn't know about parent activity
     * Notifies activity that language changed and fragment need to be reload.
     */
    public interface OnLanguageChangeListener {
        public void onLanguageChanged();

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // this makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnLanguageChangeListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " " + getActivity().getString(R.string.castExc) + " " + OnLanguageChangeListener.class);
        }
    }

}
