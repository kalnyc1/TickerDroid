package com.paperstreet.kal.tickerdroid;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.github.mikephil.charting.charts.LineChart;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class ChartFragment extends Fragment
{
    public LineChart m_grfLineFromDate;
    public EditText m_edtChartDate;
    final Calendar m_objCalendar = Calendar.getInstance();

    // The listener interface.
    OnFragmentInteractionListener m_objListener;

    public ChartFragment()
    {
    }

    interface OnFragmentInteractionListener
    {
        void onDateChanged( View view, String sDate );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        ViewGroup rootView = (ViewGroup)inflater.inflate(
            R.layout.fragment_chart, container, false );

        return rootView;
    }

    @Override
    public void onViewCreated( @NonNull View view, @Nullable Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState );

        // Chart date
        m_edtChartDate = (EditText)view.findViewById( R.id.edtChartDate );

        final DatePickerDialog.OnDateSetListener objDateListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet( DatePicker view, int year, int monthOfYear, int dayOfMonth )
            {
                // TODO Auto-generated method stub
                m_objCalendar.set( Calendar.YEAR, year );
                m_objCalendar.set( Calendar.MONTH, monthOfYear );
                m_objCalendar.set( Calendar.DAY_OF_MONTH, dayOfMonth );
                updateLabel();
            }
        };

        m_edtChartDate.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                // TODO Auto-generated method stub
                new DatePickerDialog( getContext(), objDateListener,
                    m_objCalendar.get( Calendar.YEAR ),
                    m_objCalendar.get( Calendar.MONTH ),
                    m_objCalendar.get( Calendar.DAY_OF_MONTH ) ).show();
            }
        } );

        // Graph
        m_grfLineFromDate = (LineChart)view.findViewById( R.id.grfLineFromDate );
    }

    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );

        if ( context instanceof OnFragmentInteractionListener ) {
            m_objListener = (OnFragmentInteractionListener)context;
        }
        else {
            throw new ClassCastException( context.toString() + " No Listener implemented" );
        }
    }

    private void updateLabel()
    {
        m_edtChartDate.setText( String.format( Locale.ENGLISH, "%02d/%02d/%04d",
            m_objCalendar.get( Calendar.MONTH ) + 1,
            m_objCalendar.get( Calendar.DAY_OF_MONTH ),
            m_objCalendar.get( Calendar.YEAR ) ) );
        m_objListener.onDateChanged( m_edtChartDate, m_edtChartDate.getText().toString() );
    }
}
