package com.paperstreet.kal.tickerdroid;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class DataFragment extends Fragment
{
    TextView m_txtCompanyValue;
    TextView m_txtExchangeValue;
    TextView m_txtSectorValue;
    TextView m_txtPreviousValue;
    TextView m_txtOpenValue;
    TextView m_txtLastValue;
    TextView m_txtHighValue;
    TextView m_txtLowValue;
    TextView m_txtCloseValue;
    TextView m_txtExtendedValue;
    TextView m_txt52WkRangeValue;
    TextView m_txtValueValue;
    LineChart m_grfLine;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
            R.layout.fragment_data, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated( @NonNull View view, @Nullable Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState );

        // Set resource variables
        m_txtCompanyValue = (TextView)view.findViewById( R.id.txtCompanyValue );
        m_txtExchangeValue = (TextView)view.findViewById( R.id.txtExchangeValue );
        m_txtSectorValue = (TextView)view.findViewById( R.id.txtSectorValue );
        m_txtPreviousValue = (TextView)view.findViewById( R.id.txtPreviousValue );
        m_txtOpenValue = (TextView)view.findViewById( R.id.txtOpenValue );
        m_txtLastValue = (TextView)view.findViewById( R.id.txtLastValue );
        m_txtHighValue = (TextView)view.findViewById( R.id.txtHighValue );
        m_txtLowValue = (TextView)view.findViewById( R.id.txtLowValue );
        m_txtCloseValue = (TextView)view.findViewById( R.id.txtCloseValue );
        m_txtExtendedValue = (TextView)view.findViewById( R.id.txtExtendedValue );
        m_txt52WkRangeValue = (TextView)view.findViewById( R.id.txt52WkRangeValue );
        m_txtValueValue = (TextView)view.findViewById( R.id.txtValueValue );

        // Graph
        m_grfLine = (LineChart)view.findViewById( R.id.grfLine );
    }
}
