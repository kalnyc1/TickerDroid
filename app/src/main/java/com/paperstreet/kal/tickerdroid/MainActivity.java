package com.paperstreet.kal.tickerdroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements TickerDataResponseCallback, TickerChartResponseCallback, ChartFragment.OnFragmentInteractionListener
{
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager m_objPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private ScreenSlidePagerAdapter m_objPagerAdapter;

    /**
     * Fragments
     */
    private DataFragment m_objDataFragment;
    private ChartFragment m_objChartFragment;

    /**
     * Views
     */
    EditText m_edtShares;
    EditText m_edtSymbol;
    Button m_btnSearch;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary( "native-lib" );
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        // Instantiate a ViewPager and a PagerAdapter.
        m_objPager = (ViewPager)findViewById( R.id.pager );
        m_objPagerAdapter = new ScreenSlidePagerAdapter( getSupportFragmentManager() );
        m_objPager.setAdapter( m_objPagerAdapter );
        m_objPager.setPageTransformer( true, new ZoomOutPageTransformer() );

        // Set resource variables
        m_edtShares = (EditText)findViewById( R.id.edtShares );
        m_edtSymbol = (EditText)findViewById( R.id.edtSymbol );
        m_btnSearch = (Button)findViewById( R.id.btnSearch );

        // Add the listener to the symbol edit view by creating a callback
        m_edtSymbol.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if ( actionId == EditorInfo.IME_ACTION_SEARCH ) {
                    //Handle search key click
                    sendMessage( m_edtSymbol );
                    return true;
                }

                if ( actionId == EditorInfo.IME_ACTION_GO ) {
                    //Handle go key click
                    sendMessage( m_edtSymbol );
                    return true;
                }

                return false;
            }
        } );
    }

    /**
     * Get the active network info
     *
     * @return NetworkInfo
     */
    @Override
    public NetworkInfo getActiveNetworkInfo()
    {
        ConnectivityManager connectivityManager =
            (ConnectivityManager)getSystemService( Context.CONNECTIVITY_SERVICE );
        return connectivityManager.getActiveNetworkInfo();
    }

    /**
     * Search button onclick handler
     *
     * @param view
     */
    public void sendMessage( View view )
    {
        String sSymbol = m_edtSymbol.getText().toString();
        String sUrl = "https://api.iextrading.com/1.0/stock/" + sSymbol +
            "/batch?types=quote,chart&range=1d&chartInterval=1";

        // Hide the soft keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(
            Context.INPUT_METHOD_SERVICE );
        imm.hideSoftInputFromWindow( view.getWindowToken(), 0 );

        try {
            // Create a ticker request passing this as the response callback object and the url.
            TickerDataRequest objTickerDataRequest = new TickerDataRequest( this, sUrl );
            objTickerDataRequest.makeRequest();
        }
        catch ( Exception ex ) {
            m_objDataFragment.m_txtCompanyValue.setText( ( "Search failed: " + ex.getMessage() ) );
        }
    }

    @Override
    public void setTickerData( TickerDataResponse objTickerDataResponse )
    {
        m_objDataFragment = (DataFragment)m_objPagerAdapter.getRegisteredFragment( 0 );

        m_objDataFragment.m_txtCompanyValue.setText( objTickerDataResponse.m_sCompany );
        m_objDataFragment.m_txtExchangeValue.setText( objTickerDataResponse.m_sExchange );
        m_objDataFragment.m_txtSectorValue.setText( objTickerDataResponse.m_sSector );
        m_objDataFragment.m_txtPreviousValue.setText( ( "$" + objTickerDataResponse.m_sPrevious ) );
        m_objDataFragment.m_txtOpenValue.setText(
            ( "$" + objTickerDataResponse.m_sOpen + "\n@ " +
                DateFormat.format( "M/dd/yyyy h:mm:ss a", objTickerDataResponse.m_dtOpenTime ) ) );
        m_objDataFragment.m_txtLastValue.setText(
            ( "$" + objTickerDataResponse.m_sLast + ( objTickerDataResponse.m_nChange > 0 ? " (+" : " (" ) +
                objTickerDataResponse.m_nChange + ")\n@ " +
                DateFormat.format( "M/dd/yyyy h:mm:ss a", objTickerDataResponse.m_dtLastTime ) ) );
        m_objDataFragment.m_txtHighValue.setText( ( "$" + objTickerDataResponse.m_sHigh ) );
        m_objDataFragment.m_txtLowValue.setText( ( "$" + objTickerDataResponse.m_sLow ) );
        m_objDataFragment.m_txtCloseValue.setText(
            ( "$" + objTickerDataResponse.m_sClose + "\n@ " +
                DateFormat.format( "M/dd/yyyy h:mm:ss a", objTickerDataResponse.m_dtCloseTime ) ) );
        m_objDataFragment.m_txtExtendedValue.setText(
            ( "$" + objTickerDataResponse.m_sExtended + "\n@ " +
                DateFormat.format( "M/dd/yyyy h:mm:ss a",
                    objTickerDataResponse.m_dtExtendedTime ) ) );
        m_objDataFragment.m_txt52WkRangeValue.setText(
            ( "$" + objTickerDataResponse.m_s52WeekLow + " - $" + objTickerDataResponse.m_s52WeekHigh ) );

        // Calculate and set shares value
        String sShares = m_edtShares.getText().toString();
        if ( sShares.length() > 0 ) {
            DecimalFormat fmtDecimal = new DecimalFormat( "0.00" );
            fmtDecimal.setMaximumFractionDigits( 2 );
            String sSharesValue = fmtDecimal.format(
                Float.parseFloat( objTickerDataResponse.m_sLast ) * Integer.parseInt( sShares ) );
            m_objDataFragment.m_txtValueValue.setText( ( "$" + sSharesValue ) );
        }

        if ( objTickerDataResponse.m_arrLinePoints != null ) {
            LineDataSet setComp = new LineDataSet( objTickerDataResponse.m_arrLinePoints, "Price" );
            setComp.setAxisDependency( YAxis.AxisDependency.LEFT );
            setComp.setColor( R.color.colorBlack );
            setComp.setDrawCircles( false );

            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add( setComp );

            if ( objTickerDataResponse.m_sPrevious.length() > 0 ) {
                float nPrevious = (float)Float.parseFloat( objTickerDataResponse.m_sPrevious );
                List<Entry> arrLinePrevious = new ArrayList<Entry>();
                for ( int p = 0; p < objTickerDataResponse.m_arrLinePoints.size(); ++p ) {
                    arrLinePrevious.add( new Entry( p, nPrevious ) );
                }
                LineDataSet setPrevious = new LineDataSet( arrLinePrevious,
                    getResources().getString( R.string.txtPrevious ) );
                setPrevious.setAxisDependency( YAxis.AxisDependency.LEFT );
                setPrevious.enableDashedLine( 100, 30, 0 );
                setPrevious.setColor( R.color.colorBlue );
                setPrevious.setDrawCircles( false );
                setPrevious.setLineWidth( 2f );
                dataSets.add( setPrevious );
            }

            LineData data = new LineData( dataSets );
            m_objDataFragment.m_grfLine.setData( data );

            // The labels that should be drawn on the XAxis
            final ArrayList<String> arrLabels = new ArrayList<String>(
                objTickerDataResponse.m_arrXAxisLabels );
            IAxisValueFormatter formatter = new IAxisValueFormatter()
            {
                @Override
                public final String getFormattedValue( float nValue, AxisBase axis )
                {
                    return arrLabels.get( (int)nValue );
                }
            };

            XAxis xAxis = m_objDataFragment.m_grfLine.getXAxis();
            xAxis.setGranularity( 1f ); // minimum axis-step (interval) is 1
            xAxis.setPosition( XAxis.XAxisPosition.BOTTOM );
            xAxis.setValueFormatter( formatter );

            m_objDataFragment.m_grfLine.getAxisRight().setEnabled( false );

            Description objDesc = new Description();
            objDesc.setText( objTickerDataResponse.m_sCompany );
            m_objDataFragment.m_grfLine.setDescription( objDesc );
            m_objDataFragment.m_grfLine.setDrawBorders( true );
            m_objDataFragment.m_grfLine.invalidate();
        }
    }

    @Override
    public void onDateChanged( View view, String sDate )
    {
        if ( sDate == null ) {
            return;
        }

        String sChartDate = sDate.substring( 6 ) + sDate.substring( 0, 2 ) + sDate.substring( 3, 5 );
        String sSymbol = m_edtSymbol.getText().toString();
        String sUrl = "https://api.iextrading.com/1.0/stock/" + sSymbol +
            "/chart/date/" + sChartDate + "?chartInterval=1";

        // Hide the soft keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(
            Context.INPUT_METHOD_SERVICE );
        imm.hideSoftInputFromWindow( view.getWindowToken(), 0 );

        try {
            // Create a ticker chart request passing this as the response callback object and the url.
            TickerChartRequest objTickerChartRequest = new TickerChartRequest( this, sUrl );
            objTickerChartRequest.makeRequest();
        }
        catch ( Exception ex ) {
            //m_objDataFragment.m_txtCompanyValue.setText( ( "Search failed: " + ex.getMessage() ) );
        }
    }

    @Override
    public void setTickerChart( TickerChartResponse objTickerChartResponse )
    {
        m_objChartFragment = (ChartFragment)m_objPagerAdapter.getRegisteredFragment( 1 );

        if ( objTickerChartResponse.m_arrLinePoints != null ) {
            LineDataSet setComp = new LineDataSet( objTickerChartResponse.m_arrLinePoints,
                "Price" );
            setComp.setAxisDependency( YAxis.AxisDependency.LEFT );
            setComp.setColor( R.color.colorBlack );
            setComp.setDrawCircles( false );

            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add( setComp );

            LineData data = new LineData( dataSets );
            m_objChartFragment.m_grfLineFromDate.setData( data );

            // The labels that should be drawn on the XAxis
            final ArrayList<String> arrLabels = new ArrayList<String>(
                objTickerChartResponse.m_arrXAxisLabels );
            IAxisValueFormatter formatter = new IAxisValueFormatter()
            {
                @Override
                public final String getFormattedValue( float nValue, AxisBase axis )
                {
                    return arrLabels.get( (int)nValue );
                }
            };

            XAxis xAxis = m_objChartFragment.m_grfLineFromDate.getXAxis();
            xAxis.setGranularity( 1f ); // minimum axis-step (interval) is 1
            xAxis.setPosition( XAxis.XAxisPosition.BOTTOM );
            xAxis.setValueFormatter( formatter );

            m_objChartFragment.m_grfLineFromDate.getAxisRight().setEnabled( false );

            Description objDesc = new Description();
            objDesc.setText( "-" );
            m_objChartFragment.m_grfLineFromDate.setDescription( objDesc );
            m_objChartFragment.m_grfLineFromDate.setDrawBorders( true );
            m_objChartFragment.m_grfLineFromDate.invalidate();
        }
    }

    @Override
    public void onBackPressed()
    {
        if ( m_objPager.getCurrentItem() == 0 ) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        }
        else {
            // Otherwise, select the previous step.
            m_objPager.setCurrentItem( m_objPager.getCurrentItem() - 1 );
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
