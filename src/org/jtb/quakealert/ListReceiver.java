package org.jtb.quakealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ListReceiver extends BroadcastReceiver {
	private ListQuakesActivity activity;

	public ListReceiver(ListQuakesActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("updateList")) {
			activity.updateList();
		} else if (intent.getAction().equals("showRefreshDialog")) {
			activity.showDialog(ListQuakesActivity.REFRESH_DIALOG);
		} else if (intent.getAction().equals("dismissRefreshDialog")) {
			activity.dismissDialog(ListQuakesActivity.REFRESH_DIALOG);
		} else if (intent.getAction().equals("showUnknownLocationMessage")) {
			Toast.makeText(activity, "Unknown location, showing all.",
					Toast.LENGTH_LONG).show();
		} else if (intent.getAction().equals("showNetworkErrorDialog")) {
			activity.showDialog(ListQuakesActivity.NETWORK_ERROR_DIALOG);
		} else if (intent.getAction().equals("dismissNetworkErrorDialog")) {
			activity.dismissDialog(ListQuakesActivity.NETWORK_ERROR_DIALOG);
		}
	}
}
