package org.jtb.quakealert;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class Lock {
	private static PowerManager.WakeLock lock;

	private static PowerManager.WakeLock getLock(Context context) {
		if (lock == null) {
			PowerManager mgr = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);

			lock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"org.jtb.quakealert.lock");
			lock.setReferenceCounted(true);
		}
		return lock;
	}

	public static synchronized void acquire(Context context) {
		WakeLock wakeLock = getLock(context);
		wakeLock.acquire();
		Log.d("quakealert", "wake lock acquired");
	}

	public static synchronized void release() {
		if (lock == null) {
			Log.w("quakealert", "release attempted, but wake lock was null");
		} else {
			if (lock.isHeld()) {
				lock.release();
				Log.d("quakealert", "wake lock released");
			} else {
				Log.w("quakealert",
						"release attempted, but wake lock was not held");
			}
		}
	}
}
