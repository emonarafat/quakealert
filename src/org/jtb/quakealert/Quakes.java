package org.jtb.quakealert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;

public class Quakes {
    //private static final String QUAKES_URL = "http://earthquake.usgs.gov/eqcenter/catalogs/eqs7day-M1.txt";
    private static final String QUAKES_URL = "http://earthquake.usgs.gov/eqcenter/catalogs/eqs1day-M1.txt";
    //private static final String QUAKES_URL = "http://gmclinet.red.iplanet.com:8080/eqs1day-M1.txt";
    
	private List<Quake> quakes = null;

    public synchronized void update() {
        BufferedReader reader = null;
        String line;

        try {
            URL u = new URL(QUAKES_URL);
            HttpURLConnection uc = (HttpURLConnection) u.openConnection();
            uc.setReadTimeout(30 * 1000); // 30 seconds

            if (uc.getResponseCode() != 200) {
                //TODO: android log
                return;
            }

            reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "ISO-8859-1"), 8192);
            reader.readLine();

            quakes = new ArrayList<Quake>();
            while ((line = reader.readLine()) != null) {
                Quake quake = new Quake(line);
                quakes.add(quake);
                //Log.d(getClass().getSimpleName(), "read quake: " + quake);
            }
            Collections.sort(quakes, Quake.DATE_COMPARATOR);
        } catch (Throwable t) {
            Log.w(getClass().getSimpleName(), t);
            quakes = null;
            return;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                // TODO: android log
            }
        }
    }

    public synchronized List<Quake> get() {
        if (quakes == null) {
            update();
        }
        return quakes;
    }
}
