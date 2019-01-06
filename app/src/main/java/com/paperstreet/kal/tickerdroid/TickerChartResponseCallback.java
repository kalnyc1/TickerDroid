package com.paperstreet.kal.tickerdroid;

import android.net.NetworkInfo;

public interface TickerChartResponseCallback
{
    void setTickerChart( TickerChartResponse objTickerChartResponse );

    /**
     * Get the device's active network status in the form of a NetworkInfo object.
     */
    NetworkInfo getActiveNetworkInfo();
}
