package com.paperstreet.kal.tickerdroid;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
{
    /**
     * The number of pages (wizard steps) to show.
     */
    private static final int NUM_PAGES = 2;

    /**
     * Sparse array to keep track of registered fragments in memory
     */
    private SparseArray<Fragment> m_registeredFragments = new SparseArray<Fragment>();

    public ScreenSlidePagerAdapter( FragmentManager fm )
    {
        super( fm );
    }

    @Override
    public Fragment getItem( int position )
    {
        if ( position == 0 ) {
            return new DataFragment();
        }
        else if ( position == 1 ) {
            return new ChartFragment();
        }

        return new DataFragment();
    }

    @Override
    public int getCount()
    {
        return NUM_PAGES;
    }

    // Register the fragment when the item is instantiated
    @Override
    public Object instantiateItem( ViewGroup container, int position )
    {
        Fragment fragment = (Fragment)super.instantiateItem( container, position );
        m_registeredFragments.put( position, fragment );
        return fragment;
    }

    // Unregister when the item is inactive
    @Override
    public void destroyItem( ViewGroup container, int position, Object object )
    {
        m_registeredFragments.remove( position );
        super.destroyItem( container, position, object );
    }

    // Returns the fragment for the position (if instantiated)
    public Fragment getRegisteredFragment( int position )
    {
        return m_registeredFragments.get( position );
    }
}
