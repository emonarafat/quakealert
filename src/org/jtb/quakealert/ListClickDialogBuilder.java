package org.jtb.quakealert;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

public class ListClickDialogBuilder extends AlertDialog.Builder {
	private static final String USGS_URL_PREFIX = "http://earthquake.usgs.gov/earthquakes/recenteqsus/Quakes/";
	
	private Context mContext;
	private int mPosition;
	
	public ListClickDialogBuilder(Context context, int position) {
		super(context);
		this.mContext = context;		
		this.mPosition = position;

		setItems(R.array.listclick_entries, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog ad = (AlertDialog) dialog;
				switch (which) {
				case 0:
					Intent i = new Intent(mContext, QuakeMapActivity.class);
					i.putExtra("org.jtb.quakealert.quake.position", mPosition);
					mContext.startActivity(i);
					break;
				case 1:
					Quake quake = QuakeRefreshService.matchQuakes.get(mPosition);
					String u = USGS_URL_PREFIX + quake.getSource() + quake.getId() + ".php";
					mContext.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse(u)));
					break;
				}
			}
		});
		setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	}

	private String[] getListItems() {
		ArrayList<String> items = new ArrayList<String>();
		items.add("Map");
		items.add("USGS");

		return items.toArray(new String[0]);
	}
}