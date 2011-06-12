package org.jtb.quakealert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

public class ListReceiver extends BroadcastReceiver {
	private Handler handler;

	public ListReceiver(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("updateList")) {
			handler.sendEmptyMessage(ListQuakesActivity.UPDATE_LIST_WHAT);
		} else if (intent.getAction().equals("showRefreshDialog")) {
			handler.sendEmptyMessage(ListQuakesActivity.REFRESH_SHOW_WHAT);
		} else if (intent.getAction().equals("dismissRefreshDialog")) {
			handler.sendEmptyMessage(ListQuakesActivity.REFRESH_HIDE_WHAT);
		} else if (intent.getAction().equals("showUnknownLocationMessage")) {
			Toast.makeText(context, "Unknown location, showing all quakes.",
					Toast.LENGTH_LONG).show();
		} else if (intent.getAction().equals("showNetworkErrorDialog")) {
			handler.sendEmptyMessage(ListQuakesActivity.NETWORK_ERROR_SHOW_WHAT);
		} else if (intent.getAction().equals("dismissNetworkErrorDialog")) {
			handler.sendEmptyMessage(ListQuakesActivity.NETWORK_ERROR_HIDE_WHAT);
		}
	}
}
