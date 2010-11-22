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
import android.view.ContextThemeWrapper;

public class PrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	static final int CHANGED_RESULT = 1;
	static final int UNCHANGED_RESULT = 0;

	private PrefsActivity mThis;
	private ListPreference mRangePreference;
	private ListPreference mMagnitudePreference;
	private ListPreference mUnitsPreference;
	private ListPreference mIntervalPreference;
	private CheckBoxPreference mAlertPreference;
	private RingtonePreference mAlertSoundPreference;
	private CheckBoxPreference mVibratePreference;
	private CheckBoxPreference mFlashPreference;
	private CheckBoxPreference mBootStartPreference;

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
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.prefs);

		mThis = this;
		setResult(UNCHANGED_RESULT);
		mQuakePrefs = new QuakePrefs(this);

		mRangePreference = (ListPreference) findPreference("range");
		mMagnitudePreference = (ListPreference) findPreference("magnitude");
		mUnitsPreference = (ListPreference) findPreference("units");
		mIntervalPreference = (ListPreference) findPreference("interval");
		mAlertPreference = (CheckBoxPreference) findPreference("notificationAlert");
		mAlertSoundPreference = (RingtonePreference) findPreference("notificationAlertSound");
		mVibratePreference = (CheckBoxPreference) findPreference("notificationVibrate");
		mFlashPreference = (CheckBoxPreference) findPreference("notificationFlash");
		mBootStartPreference = (CheckBoxPreference) findPreference("bootStart");

		QuakePrefs qp = new QuakePrefs(this);

		Preference.OnPreferenceChangeListener changedListener = new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				setResult(CHANGED_RESULT);
				return true;
			}
		};

		mRangePreference.setOnPreferenceChangeListener(changedListener);
		mMagnitudePreference.setOnPreferenceChangeListener(changedListener);

		Preference.OnPreferenceChangeListener unitsListener = new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				String u = (String) newValue;
				Units units = Units.valueOf(u);
				mQuakePrefs.setUnits(units);
				setRangeEntries();

				sendBroadcast(new Intent("updateList"));
				return true;
			}
		};

		mUnitsPreference.setOnPreferenceChangeListener(unitsListener);

		Preference.OnPreferenceChangeListener notificationsListener = new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				boolean checked = ((Boolean) newValue).booleanValue();
				if (checked) {
					setNotificationsEnabled(true);
					
					sendBroadcast(new Intent("schedule", null, mThis,
							QuakeRefreshReceiver.class));
				} else {
					setNotificationsEnabled(false);
					
					sendBroadcast(new Intent("cancel", null, mThis,
							QuakeRefreshReceiver.class));
				}
				return true;
			}
		};

		Preference.OnPreferenceChangeListener intervalListener = new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				String is = (String) newValue;
				Interval i = Interval.valueOf(is);
				QuakePrefs qp = new QuakePrefs(mThis);
				qp.setInterval(i);
				sendBroadcast(new Intent("schedule", null, mThis,
						QuakeRefreshReceiver.class));

				return true;
			}
		};
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

		ListPreference ip = (ListPreference) findPreference("interval");
		ip.setEnabled(qp.isNotificationsEnabled());
		ip.setOnPreferenceChangeListener(intervalListener);
		ip.setOnPreferenceClickListener(intervalWarnListener);

		CheckBoxPreference cbp = (CheckBoxPreference) findPreference("notificationsEnabled");
		cbp.setOnPreferenceChangeListener(notificationsListener);
		
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

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("range")) {
			setRangeTitle();
		} else if (key.equals("magnitude")) {
			setMagnitudeTitle();
		} else if (key.equals("interval")) {
			setIntervalTitle();
		} else if (key.equals("units")) {
			setUnitsTitle();
			setRangeEntries();
			setRangeTitle();
		}
	}

}
