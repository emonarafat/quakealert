package org.jtb.quakealert;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ListQuakesActivity extends Activity {
	private static final String HELP_URL = "http://code.google.com/p/quakealert/wiki/Help";
	private static final String REPORT_URL = "http://code.google.com/p/quakealert/issues/list";

	static final int REFRESH_HIDE_WHAT = 1;
	static final int REFRESH_SHOW_WHAT = 2;
	static final int UPDATE_LIST_WHAT = 3;
	static final int NETWORK_ERROR_SHOW_WHAT = 4;
	static final int NETWORK_ERROR_HIDE_WHAT = 5;

	private static final int REFRESH_MENU = 0;
	private static final int PREFS_MENU = 1;

	private static final int PREFS_REQUEST = 0;

	static final int REFRESH_DIALOG = 0;
	static final int NETWORK_ERROR_DIALOG = 2;
	static final int WARN_DIALOG = 3;

	private AlertDialog mServiceWarnDialog;
	private ProgressDialog mRefreshDialog;
	private AlertDialog mNetworkErrorDialog;
	private AlertDialog mListClickDialog;

	private ListView mList;
	private LinearLayout mNoQuakesLayout;
	private ListReceiver listUpdateReceiver;
	private Prefs quakePrefs;
	private QuakeAdapter quakeAdapter;
	private List<Quake> quakes = new ArrayList<Quake>();

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_SHOW_WHAT:
				showDialog(REFRESH_DIALOG);
				break;
			case REFRESH_HIDE_WHAT:
				removeDialog(REFRESH_DIALOG);
				mRefreshDialog = null;

				break;
			case NETWORK_ERROR_SHOW_WHAT:
				showDialog(NETWORK_ERROR_DIALOG);
				break;
			case NETWORK_ERROR_HIDE_WHAT:
				removeDialog(NETWORK_ERROR_DIALOG);
				mRefreshDialog = null;

				break;
			case UPDATE_LIST_WHAT:
				updateList();
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		quakePrefs = new Prefs(this);
		setTheme(quakePrefs.getTheme().getId());
		setContentView(R.layout.list);

		upgrade();

		listUpdateReceiver = new ListReceiver(mHandler);
		quakeAdapter = new QuakeAdapter(this, quakes);

		mNoQuakesLayout = (LinearLayout) findViewById(R.id.no_quakes_layout);
		mList = (ListView) findViewById(R.id.list);
		mList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				AlertDialog.Builder builder = new ListClickDialogBuilder(
						ListQuakesActivity.this, position);
				mListClickDialog = builder.create();
				mListClickDialog.show();
			}
		});
		mList.setAdapter(quakeAdapter);

		if (quakePrefs.isNotificationsEnabled()) {
			sendBroadcast(new Intent("schedule", null, this,
					RefreshReceiver.class));
		}

		if (quakePrefs.isWarn("serviceWarn")) {
			showDialog(WARN_DIALOG);
		}

		Uri alertSoundUri = quakePrefs.getNotificationAlertSound();
		if (alertSoundUri == null) {
			Uri defaultUri = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			quakePrefs.setNotificationAlertSound(defaultUri);
		}

		refresh();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void refresh() {
		sendBroadcast(new Intent("refresh", null, ListQuakesActivity.this,
				RefreshReceiver.class));
	}

	@Override
	public void onPause() {
		Log.d("quakealert", "paused");

		super.onPause();

		unregisterReceiver(listUpdateReceiver);
	}

	@Override
	public void onResume() {
		Log.d("quakealert", "resumed");

		super.onResume();

		registerReceiver(listUpdateReceiver, new IntentFilter("updateList"));
		registerReceiver(listUpdateReceiver, new IntentFilter(
				"showRefreshDialog"));
		registerReceiver(listUpdateReceiver, new IntentFilter(
				"dismissRefreshDialog"));
		registerReceiver(listUpdateReceiver, new IntentFilter(
				"showUnknownLocationMessage"));
		registerReceiver(listUpdateReceiver, new IntentFilter(
				"showNetworkErrorDialog"));
	}

	public void updateList() {
		if (RefreshService.matchQuakes != null
				&& RefreshService.matchQuakes.size() > 0) {

			quakePrefs.setLastUpdate();

			quakes.clear();
			quakes.addAll(RefreshService.matchQuakes);
			quakeAdapter.setLocation(RefreshService.location);
			quakeAdapter.notifyDataSetChanged();

			mNoQuakesLayout.setVisibility(View.GONE);
			mList.setVisibility(View.VISIBLE);

			new QuakeNotifier(this).cancel();

			Log.d("quakealert", "updated list (visible)");
		} else {
			mNoQuakesLayout.setVisibility(View.VISIBLE);
			mList.setVisibility(View.GONE);
			Log.d("quakealert", "updated list (gone)");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_menu, menu);
		
		MenuItem mi;
		
		mi = menu.getItem(0);
		MenuItemCompat.setShowAsAction(mi,
				MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		mi = menu.getItem(1);
		MenuItemCompat.setShowAsAction(mi,
				MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		mi = menu.getItem(2);
		MenuItemCompat.setShowAsAction(mi,
				MenuItemCompat.SHOW_AS_ACTION_NEVER);
		mi = menu.getItem(3);
		MenuItemCompat.setShowAsAction(mi,
				MenuItemCompat.SHOW_AS_ACTION_NEVER);
	
		return true;
	}

	private void view(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refesh_item:
			refresh();
			return true;
		case R.id.preferences_item:
			Intent prefsActivity = new Intent(this, PrefsActivity.class);
			startActivityForResult(prefsActivity, PREFS_REQUEST);
			return true;
		case R.id.help_item:
			view(HELP_URL);
			return true;
		case R.id.report_item:
			view(REPORT_URL);
			return true;
		}

		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case PREFS_REQUEST:
			switch (resultCode) {
			case PrefsActivity.CHANGED_RESULT:
				refresh();
				break;
			case PrefsActivity.RESET_RESULT:
				finish();
				startActivity(new Intent(this, getClass()));
				break;
			case PrefsActivity.UPDATE_RESULT:
				updateList();
				break;
			}
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case REFRESH_DIALOG:
			if (mRefreshDialog == null) {
				mRefreshDialog = new ProgressDialog(this);
				mRefreshDialog.setMessage("Refreshing, please wait ...");
				mRefreshDialog.setIndeterminate(true);
				mRefreshDialog.setCancelable(true);
				mRefreshDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				});
			}
			return mRefreshDialog;
		case NETWORK_ERROR_DIALOG:
			if (mNetworkErrorDialog == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Network Error");
				builder.setMessage("Could not retrieve recent quakes.\n\nEnsure that you have a network connection (mobile or wifi).");
				builder.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dismissDialog(NETWORK_ERROR_DIALOG);
								finish();
							}
						});
				mNetworkErrorDialog = builder.create();
			}
			return mNetworkErrorDialog;
		case WARN_DIALOG:
			if (mServiceWarnDialog == null) {
				AlertDialog.Builder builder = new WarnDialogBuilder(
						new ContextThemeWrapper(this,
								android.R.style.Theme_Dialog), "serviceWarn",
						R.string.service_warn);
				mServiceWarnDialog = builder.create();
			}
			return mServiceWarnDialog;
		}
		return null;
	}

	private void upgrade() {
		PackageManager manager = getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e("quakealert", "could not get version", e);
			return;
		}

		if (!quakePrefs.isUpgradedTo(4)) {
			// upgrade range from km to m
			int km = quakePrefs.getRange();
			if (km > 0) {
				quakePrefs.setRange(km * 1000);
			}
		}
		if (!quakePrefs.isUpgradedTo(16)) {
			// upgrade interval
			Interval i = null;
			long interval = quakePrefs.getLong("interval", 0);
			if (interval < 60 * 60 * 1000) {
				// was set to 5 or 10 mins
				i = Interval.FIFTEEN_MINUTES;
			} else {
				// was set to 1 or 3 hours
				i = Interval.HOUR;
			}
			quakePrefs.setInterval(i);
		}
		if (!quakePrefs.isUpgradedTo(20)) {
			// if using 100km range, set to 250km
			// 100km is removed
			int km = quakePrefs.getRange();
			if (km == 100) {
				quakePrefs.setRange(250);
			}
		}
		if (!quakePrefs.isUpgradedTo(30)) {
			// set old float magnitude value to constant
			String m = quakePrefs.getString("magnitude", null);
			if (m != null && m.contains(".")) {
				float f = Float.parseFloat(m);
				Magnitude mag;

				try {
					mag = Magnitude.valueOf(f);
				} catch (IllegalArgumentException e) {
					mag = Magnitude.M3;
				}

				quakePrefs.setMagnitude(mag);
			}

			// set old string units value to constant
			String u = quakePrefs.getString("units", null);
			if (u != null && Character.isLowerCase(u.charAt(0))) {
				Units units;
				if (u.equals("metric")) {
					units = Units.METRIC;
				} else {
					units = Units.US;
				}
				quakePrefs.setUnits(units);
			}

		}

		quakePrefs.setUpgradedTo(info.versionCode);
	}
}
