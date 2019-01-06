package com.paperstreet.kal.tickerdroid;

import com.github.mikephil.charting.data.Entry;
import java.util.Date;
import java.util.List;

public class TickerDataResponse {
    public String m_sCompany;
    public String m_sExchange;
    public String m_sSector;
    public String m_sPrevious;
    public String m_sOpen;
    public String m_sLast;
    public String m_sHigh;
    public String m_sLow;
    public String m_sClose;
    public Double m_nChange;
    public String m_sExtended;
    public String m_s52WeekLow;
    public String m_s52WeekHigh;
    public Date m_dtOpenTime;
    public Date m_dtLastTime;
    public Date m_dtCloseTime;
    public Date m_dtExtendedTime;
    public List<Entry> m_arrLinePoints;
    public List<String> m_arrXAxisLabels;

    public TickerDataResponse()
    {
    }
}
