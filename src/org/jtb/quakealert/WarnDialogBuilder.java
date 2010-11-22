package org.jtb.quakealert;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class WarnDialogBuilder extends AlertDialog.Builder {
	private Context mContext;
	private String mWarnId;

	public WarnDialogBuilder(ContextThemeWrapper wrapper, String warnId,
			int warnMessageId) {
		super(wrapper);
		this.mContext = wrapper.getBaseContext();
		;
		this.mWarnId = warnId;

		setTitle("Warning");

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.warn_dialog, null);
		layout.setMinimumHeight(180);
		layout.setMinimumWidth(240);
		setView(layout);
		setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		CheckBox cb = (CheckBox) layout.findViewById(R.id.warn_check);
		cb.setChecked(false);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					QuakePrefs qp = new QuakePrefs(mContext);
					qp.setWarn(mWarnId, false);
				}
			}
		});

		TextView tv = (TextView) layout.findViewById(R.id.warn_text);
		tv.setText(mContext.getResources().getString(warnMessageId));
	}
}
