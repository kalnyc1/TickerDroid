package com.paperstreet.kal.tickerdroid;

import com.github.mikephil.charting.data.Entry;

import java.util.Date;
import java.util.List;

public class TickerChartResponse
{
    public List<Entry> m_arrLinePoints;
    public List<String> m_arrXAxisLabels;

    public TickerChartResponse()
    {
    }
}
