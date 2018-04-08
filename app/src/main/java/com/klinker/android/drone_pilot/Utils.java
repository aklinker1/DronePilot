package com.klinker.android.drone_pilot;

/**
 * Created by aklinker1 on 4/7/18.
 */

public class Utils {
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {}
    }
}
