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
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ListQuakesActivity extends Activity {
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
	private ListQuakesActivity mThis;
	private QuakePrefs quakePrefs;
	private QuakeAdapter quakeAdapter;
	private List<Quake> quakes = new ArrayList<Quake>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		mThis = this;
		quakePrefs = new QuakePrefs(this);
		new Upgrader(this).upgrade();
		listUpdateReceiver = new ListReceiver(this);
		quakeAdapter = new QuakeAdapter(this, quakes);

		mNoQuakesLayout = (LinearLayout) findViewById(R.id.no_quakes_layout);
		mList = (ListView) findViewById(R.id.list);
		mList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				AlertDialog.Builder builder = new ListClickDialogBuilder(mThis,
						position);
				mListClickDialog = builder.create();
				mListClickDialog.show();
			}
		});
		mList.setAdapter(quakeAdapter);

		if (quakePrefs.isNotificationsEnabled()) {
			sendBroadcast(new Intent("schedule", null, this,
					QuakeRefreshReceiver.class));
		}
		sendBroadcast(new Intent("refresh", null, this,
				QuakeRefreshReceiver.class));

		if (quakePrefs.isWarn("serviceWarn")) {
			showDialog(WARN_DIALOG);
		}

		Uri alertSoundUri = quakePrefs.getNotificationAlertSound();
		if (alertSoundUri == null) {
			Uri defaultUri = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			quakePrefs.setNotificationAlertSound(defaultUri);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(listUpdateReceiver);
		Log.d("quakealert", "paused");
	}

	@Override
	public void onResume() {
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

		Log.d("quakealert", "resumed");
	}

	public void updateList() {
		if (QuakeRefreshService.matchQuakes != null
				&& QuakeRefreshService.matchQuakes.size() > 0) {

			quakePrefs.setLastUpdate();

			quakes.clear();
			quakes.addAll(QuakeRefreshService.matchQuakes);
			quakeAdapter.setLocation(QuakeRefreshService.location);
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
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, REFRESH_MENU, 0, R.string.refresh_menu).setIcon(
				android.R.drawable.ic_menu_rotate);
		menu.add(0, PREFS_MENU, 1, R.string.prefs_menu).setIcon(
				android.R.drawable.ic_menu_preferences);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case REFRESH_MENU:
			sendBroadcast(new Intent("refresh", null, this,
					QuakeRefreshReceiver.class));
			return true;
		case PREFS_MENU:
			Intent prefsActivity = new Intent(this, PrefsActivity.class);
			startActivityForResult(prefsActivity, PREFS_REQUEST);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case PREFS_REQUEST:
			if (resultCode == PrefsActivity.CHANGED_RESULT) {
				sendBroadcast(new Intent("refresh", null, this,
						QuakeRefreshReceiver.class));
			}
			break;
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case REFRESH_DIALOG:
			if (mRefreshDialog == null) {
				mRefreshDialog = new ProgressDialog(this);
				mRefreshDialog.setMessage("Refreshing, please wait.");
				mRefreshDialog.setIndeterminate(true);
				mRefreshDialog.setCancelable(false);
			}
			return mRefreshDialog;
		case NETWORK_ERROR_DIALOG:
			if (mNetworkErrorDialog == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Network Error");
				builder.setMessage("Could not retrieve recent quakes. Ensure that you have a network connection (mobile or wifi).");
				builder.setNeutralButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dismissDialog(NETWORK_ERROR_DIALOG);
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
}
