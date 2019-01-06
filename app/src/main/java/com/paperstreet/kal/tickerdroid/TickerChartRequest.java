package com.paperstreet.kal.tickerdroid;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class TickerChartRequest
{
    private String m_sUrl;
    private TickerChartResponseCallback m_cbTickerChartResponse;

    public TickerChartRequest( TickerChartResponseCallback cbTickerChartResponse, String sUrl )
    {
        m_cbTickerChartResponse = cbTickerChartResponse;
        m_sUrl = sUrl;
    }

    public void makeRequest()
    {
        TickerChartRequest.RequestTask objRequestTask = new TickerChartRequest.RequestTask();
        objRequestTask.execute( m_sUrl );
    }

    private class RequestTask extends AsyncTask<String, Void, TickerChartResponse>
    {
        URL m_objUrl;

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute()
        {
            if ( m_cbTickerChartResponse != null ) {
                NetworkInfo networkInfo = m_cbTickerChartResponse.getActiveNetworkInfo();

                if ( networkInfo == null || !networkInfo.isConnected() ||
                    ( networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE ) ) {
                    // If no connectivity, cancel task and update Callback with null data.
                    m_cbTickerChartResponse.setTickerChart( null );
                    cancel( true );
                }
            }
        }

        /**
         * Defines work to perform on the background thread.
         */
        @Override
        protected TickerChartResponse doInBackground( String... urls )
        {
            try {
                m_objUrl = new URL( urls[0] );

                // Make the request
                return doRequest();
            }
            catch ( Exception ex ) {
                return null;
            }
        }

        /*@Override
        protected void onProgressUpdate(Integer... progress) {
            Integer nProgress = progress[0];
        }*/

        @Override
        protected void onPostExecute( TickerChartResponse objTickerChartResponse )
        {
            if ( objTickerChartResponse != null && m_cbTickerChartResponse != null ) {
                m_cbTickerChartResponse.setTickerChart( objTickerChartResponse );
            }
        }

        /**
         * Run the request.
         *
         * @return TickerChartResponse instance
         * @throws Exception Throws exception.
         */
        private TickerChartResponse doRequest() throws Exception
        {
            InputStream isInput = null;
            HttpsURLConnection objConnection = null;
            String sResult = null;

            try {
                objConnection = (HttpsURLConnection)m_objUrl.openConnection();

                // Timeout for reading InputStream arbitrarily set to 3000ms.
                objConnection.setReadTimeout( 3000 );

                // Timeout for connection.connect() arbitrarily set to 3000ms.
                objConnection.setConnectTimeout( 3000 );

                // For this use case, set HTTP method to GET.
                objConnection.setRequestMethod( "GET" );

                // Already true by default but setting just in case; needs to be true since this request
                // is carrying an input (response) body.
                objConnection.setDoInput( true );

                // Open communications link (network traffic occurs here).
                objConnection.connect();

                int nResponseCode = objConnection.getResponseCode();
                if ( nResponseCode != HttpsURLConnection.HTTP_OK ) {
                    throw new IOException( "HTTP error code: " + nResponseCode );
                }

                isInput = new BufferedInputStream( objConnection.getInputStream() );
                BufferedReader br = new BufferedReader( new InputStreamReader( isInput ) );
                StringBuilder sb = new StringBuilder();
                String inputLine;
                while ( ( inputLine = br.readLine() ) != null ) {
                    sb.append( inputLine );
                }
                sResult = sb.toString();

                //Gson objGson = new Gson();
                //return objGson.fromJson(sResult, TickerResponse.class);
                JSONArray chart = new JSONArray( sResult );
                TickerChartResponse objTickerChartResponse = new TickerChartResponse();

                // Chart data points
                if ( chart.length() > 0 ) {
                    objTickerChartResponse.m_arrLinePoints = new ArrayList<Entry>();
                    objTickerChartResponse.m_arrXAxisLabels = new ArrayList<String>();

                    double nGoodValue = 0;
                    for ( int i = 0; i < chart.length(); ++i ) {
                        JSONObject chartEntry = chart.getJSONObject( i );

                        double nValue = (float)chartEntry.optDouble( "close", 0 );
                        if ( nValue == 0 ) {
                            nValue = nGoodValue;
                        }
                        else {
                            nGoodValue = nValue;
                        }

                        objTickerChartResponse.m_arrLinePoints.add( new Entry( i, (float)nValue ) );
                        objTickerChartResponse.m_arrXAxisLabels.add(
                            chartEntry.getString( "label" ) );
                    }
                }

                return objTickerChartResponse;
            }
            catch ( Exception ex ) {
                throw ex;
            }
            finally {
                // Close Stream and disconnect HTTPS connection.
                if ( isInput != null ) {
                    isInput.close();
                }
                if ( objConnection != null ) {
                    objConnection.disconnect();
                }
            }
        }
    }
}
