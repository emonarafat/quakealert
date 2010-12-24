package org.jtb.quakealert;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;
import android.util.Log;
import android.view.ContextThemeWrapper;

public class PrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	static final int CHANGED_RESULT = 1;
	static final int UNCHANGED_RESULT = 0;
	static final int RESET_RESULT = 2;

	private PrefsActivity mThis;
	private ListPreference mRangePreference;
	private ListPreference mMagnitudePreference;
	private ListPreference mUnitsPreference;
	private ListPreference mIntervalPreference;
	private ListPreference mThemePreference;
	private ListPreference mMaxAgePreference;
	private CheckBoxPreference mAlertPreference;
	private RingtonePreference mAlertSoundPreference;
	private CheckBoxPreference mVibratePreference;
	private CheckBoxPreference mFlashPreference;
	private CheckBoxPreference mBootStartPreference;
	private CheckBoxPreference mNotificationsPreference;

	private AlertDialog mIntervalWarnDialog;
	private QuakePrefs mQuakePrefs;

	private void setRangeEntries() {
		if (mQuakePrefs.getUnits() == Units.METRIC) {
			mRangePreference.setEntries(R.array.range_metric_entries);
		} else {
			mRangePreference.setEntries(R.array.range_us_entries);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mQuakePrefs = new QuakePrefs(this);
		setTheme(mQuakePrefs.getTheme().getId());
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.prefs);

		mThis = this;
		setResult(UNCHANGED_RESULT);

		mRangePreference = (ListPreference) findPreference("range");
		mMagnitudePreference = (ListPreference) findPreference("magnitude");
		mThemePreference = (ListPreference) findPreference("theme");
		mMaxAgePreference = (ListPreference) findPreference("maxAge");
		mIntervalPreference = (ListPreference) findPreference("interval");
		Preference.OnPreferenceClickListener intervalWarnListener = new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder builder = new WarnDialogBuilder(
						new ContextThemeWrapper(preference.getContext(),
								android.R.style.Theme_Dialog), "intervalWarn",
						R.string.interval_warn);
				if (mQuakePrefs.isWarn("intervalWarn")) {
					mIntervalWarnDialog = builder.create();
					mIntervalWarnDialog.show();
				}

				return true;
			}
		};
		mIntervalPreference.setEnabled(mQuakePrefs.isNotificationsEnabled());
		mIntervalPreference.setOnPreferenceClickListener(intervalWarnListener);
		
		mAlertPreference = (CheckBoxPreference) findPreference("notificationAlert");
		mAlertSoundPreference = (RingtonePreference) findPreference("notificationAlertSound");
		mVibratePreference = (CheckBoxPreference) findPreference("notificationVibrate");
		mFlashPreference = (CheckBoxPreference) findPreference("notificationFlash");
		mBootStartPreference = (CheckBoxPreference) findPreference("bootStart");
		mUnitsPreference = (ListPreference) findPreference("units");
		mNotificationsPreference = (CheckBoxPreference) findPreference("notificationsEnabled");		
		
		setNotificationsEnabled(mQuakePrefs.isNotificationsEnabled());
	}

	private void setNotificationsEnabled(boolean enabled) {
		mIntervalPreference.setEnabled(enabled);
		mFlashPreference.setEnabled(enabled);
		mAlertPreference.setEnabled(enabled);
		mAlertSoundPreference.setEnabled(enabled);
		mVibratePreference.setEnabled(enabled);
		mBootStartPreference.setEnabled(enabled);		
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		setRangeEntries();
		
		setRangeTitle();
		setMagnitudeTitle();
		setUnitsTitle();
		setIntervalTitle();
		setThemeTitle();
		setMaxAgeTitle();
		
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}
	
	private void setRangeTitle() {
		int range = mQuakePrefs.getRange();
		mRangePreference.setTitle("Range? (" + new Distance(range).toString(mQuakePrefs) + ")");
	}

	private void setMagnitudeTitle() {
		Magnitude magnitude = mQuakePrefs.getMagnitude();
		mMagnitudePreference.setTitle("Magnitude? (" + magnitude.getTitle(this) + ")");
	}

	private void setUnitsTitle() {
		Units units = mQuakePrefs.getUnits();
		mUnitsPreference.setTitle("Units? (" + units.getTitle(this) + ")");
		setRangeEntries();
	}

	private void setIntervalTitle() {
		Interval i = mQuakePrefs.getInterval();
		mIntervalPreference.setTitle("Interval? (" + i.getTitle(this) + ")");
	}

	private void setThemeTitle() {
		Theme t = mQuakePrefs.getTheme();
		mThemePreference.setTitle("Theme? (" + t.getTitle(this) + ")");
	}

	private void setMaxAgeTitle() {
		Age a = mQuakePrefs.getMaxAge();
		mMaxAgePreference.setTitle("Maximum Age? (" + a.getTitle(this) + ")");
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d("quakealert", "preference changed: " + key);
		
		if (key.equals("range")) {
			setRangeTitle();
			setResult(CHANGED_RESULT);		
		} else if (key.equals("magnitude")) {
			setMagnitudeTitle();
			setResult(CHANGED_RESULT);
		} else if (key.equals("theme")) {
			setThemeTitle();
			setResult(RESET_RESULT);
		} else if (key.equals("maxAge")) {
			setMaxAgeTitle();
			setResult(CHANGED_RESULT);
		} else if (key.equals("interval")) {
			setIntervalTitle();
			sendBroadcast(new Intent("schedule", null, mThis,
					RefreshReceiver.class));
		} else if (key.equals("units")) {
			setUnitsTitle();
			setRangeEntries();
			setRangeTitle();
			sendBroadcast(new Intent("updateList"));
		} else if (key.equals("notificationsEnabled")) {
			if (mQuakePrefs.isNotificationsEnabled()) {
				setNotificationsEnabled(true);
				sendBroadcast(new Intent("schedule", null, mThis,
						RefreshReceiver.class));				
			} else {
				setNotificationsEnabled(false);
				sendBroadcast(new Intent("cancel", null, mThis,
						RefreshReceiver.class));				
			}
		}
		
		
	}

}
