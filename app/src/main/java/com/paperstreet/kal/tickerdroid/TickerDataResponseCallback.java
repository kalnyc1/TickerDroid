package com.paperstreet.kal.tickerdroid;

import android.net.NetworkInfo;

public interface TickerDataResponseCallback {
    void setTickerData( TickerDataResponse objTickerDataResponse );

    /**
     * Get the device's active network status in the form of a NetworkInfo object.
     */
    NetworkInfo getActiveNetworkInfo();
}
