package com.paperstreet.kal.tickerdroid;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.github.mikephil.charting.data.Entry;
import com.google.gson.Gson;
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

public class TickerDataRequest
{
    private String m_sUrl;
    private TickerDataResponseCallback m_cbTickerDataResponse;
    private RequestTask m_objRequestTask;

    /**
     * @param cbTickerDataResponse
     * @param sUrl
     */
    public TickerDataRequest( TickerDataResponseCallback cbTickerDataResponse, String sUrl )
    {
        m_cbTickerDataResponse = cbTickerDataResponse;
        m_sUrl = sUrl;
    }

    public void makeRequest()
    {
        m_objRequestTask = new RequestTask();
        m_objRequestTask.execute( m_sUrl );
    }

    private class RequestTask extends AsyncTask<String, Void, TickerDataResponse>
    {
        URL m_objUrl;

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute()
        {
            if ( m_cbTickerDataResponse != null ) {
                NetworkInfo networkInfo = m_cbTickerDataResponse.getActiveNetworkInfo();

                if ( networkInfo == null || !networkInfo.isConnected() ||
                        ( networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE ) ) {
                    // If no connectivity, cancel task and update Callback with null data.
                    m_cbTickerDataResponse.setTickerData( null );
                    cancel( true );
                }
            }
        }

        /**
         * Defines work to perform on the background thread.
         */
        @Override
        protected TickerDataResponse doInBackground( String... urls )
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
        protected void onPostExecute( TickerDataResponse objTickerDataResponse )
        {
            if ( objTickerDataResponse != null && m_cbTickerDataResponse != null ) {
                m_cbTickerDataResponse.setTickerData( objTickerDataResponse );
            }
        }

        /**
         * Run the request.
         * @return TickerDataResponse instance
         * @throws Exception Throws exception.
         */
        private TickerDataResponse doRequest() throws Exception
        {
            InputStream isInput = null;
            HttpsURLConnection objConnection = null;
            String sResult = null;

            try {
                objConnection = (HttpsURLConnection) m_objUrl.openConnection();

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
                JSONObject reader = new JSONObject( sResult );
                JSONObject quote = reader.getJSONObject( "quote" );
                TickerDataResponse objTickerDataResponse = new TickerDataResponse();
                objTickerDataResponse.m_sCompany = quote.getString( "companyName" );
                objTickerDataResponse.m_sExchange = quote.getString( "primaryExchange" );
                objTickerDataResponse.m_sSector = quote.getString( "sector" );
                objTickerDataResponse.m_sPrevious = quote.getString( "previousClose" );
                objTickerDataResponse.m_sOpen = quote.getString( "open" );
                objTickerDataResponse.m_sLast = quote.getString( "latestPrice" );
                objTickerDataResponse.m_sHigh = quote.getString( "high" );
                objTickerDataResponse.m_sLow = quote.getString( "low" );
                objTickerDataResponse.m_sClose = quote.getString( "close" );
                objTickerDataResponse.m_nChange = quote.getDouble( "change" );
                objTickerDataResponse.m_sExtended = quote.getString( "extendedPrice" );
                objTickerDataResponse.m_s52WeekLow = quote.getString( "week52Low" );
                objTickerDataResponse.m_s52WeekHigh = quote.getString( "week52High" );
                objTickerDataResponse.m_dtOpenTime = new Date( quote.getLong( "openTime" ) );
                objTickerDataResponse.m_dtLastTime = new Date( quote.getLong( "latestUpdate" ) );
                objTickerDataResponse.m_dtCloseTime = new Date( quote.getLong( "closeTime" ) );
                objTickerDataResponse.m_dtExtendedTime = new Date(
                    quote.getLong( "extendedPriceTime" ) );

                // Chart data points
                JSONArray chart = reader.getJSONArray( "chart" );
                if ( chart.length() > 0 ) {
                    objTickerDataResponse.m_arrLinePoints = new ArrayList<Entry>();
                    objTickerDataResponse.m_arrXAxisLabels = new ArrayList<String>();

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

                        objTickerDataResponse.m_arrLinePoints.add( new Entry( i, (float)nValue ) );
                        objTickerDataResponse.m_arrXAxisLabels.add( chartEntry.getString( "label" ) );
                    }
                }

                return objTickerDataResponse;

                // Retrieve the response body as an InputStream.
                /*isInput = objConnection.getInputStream();

                if (isInput != null) {
                    // Converts Stream to String with max length of 500.
                    sResult = readStream(isInput, 500);
                    Gson objGson = new Gson();
                    return objGson.fromJson(sResult, TickerResponse.class);
                }*/
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

        /**
         * Converts the contents of an InputStream to a String.
         */
        private String readStream( InputStream stream, int maxLength ) throws IOException
        {
            String result = null;

            // Read InputStream using the UTF-8 charset.
            InputStreamReader reader = new InputStreamReader( stream, "UTF-8" );

            // Create temporary buffer to hold Stream data with specified max length.
            char[] buffer = new char[maxLength];

            // Populate temporary buffer with Stream data.
            int numChars = 0;
            int readSize = 0;

            while ( numChars < maxLength && readSize != -1 ) {
                numChars += readSize;
                int pct = ( 100 * numChars ) / maxLength;

                readSize = reader.read( buffer, numChars, buffer.length - numChars );
            }

            if ( numChars != -1 ) {
                // The stream was not empty.
                // Create String that is actual length of response body if actual length was less than
                // max length.
                numChars = Math.min( numChars, maxLength );
                result = new String( buffer, 0, numChars );
            }

            return result;
        }
    }
}
