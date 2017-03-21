//package com.jukuproject.jukutweet.Models;
//
//import android.support.v4.app.FragmentStatePagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.support.v7.app.ActionBar;
//
///**
// * Created by JClassic on 3/21/2017.
// */
//
//public class TabsAdapter extends FragmentStatePagerAdapter implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
//
//    /** The sherlock fragment activity. */
//    private final SherlockFragmentActivity mActivity;
//
//    /** The action bar. */
//    private final ActionBar mActionBar;
//
//    /** The pager. */
//    private final ViewPager mPager;
//
//    /** The tabs. */
//    private List<TabInfo> mTabs = new LinkedList<TabInfo>();
//
//    /** The total number of tabs. */
//    private int TOTAL_TABS;
//
//    private Map<Integer, Stack<TabInfo>> history = new HashMap<Integer, Stack<TabInfo>>();
//
//    /**
//     * Creates a new instance.
//     *
//     * @param activity the activity
//     * @param pager    the pager
//     */
//    public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
//        super(activity.getSupportFragmentManager());
//        activity.getSupportFragmentManager();
//        this.mActivity = activity;
//        this.mActionBar = activity.getSupportActionBar();
//        this.mPager = pager;
//        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//    }
//
//    /**
//     * Adds the tab.
//     *
//     * @param image         the image
//     * @param fragmentClass the class
//     * @param args          the arguments
//     */
//    public void addTab(final Drawable image, final Class fragmentClass, final Bundle args) {
//        final TabInfo tabInfo = new TabInfo(fragmentClass, args);
//        final ActionBar.Tab tab = mActionBar.newTab();
//        tab.setTabListener(this);
//        tab.setTag(tabInfo);
//        tab.setIcon(image);
//
//        mTabs.add(tabInfo);
//        mActionBar.addTab(tab);
//
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public Fragment getItem(final int position) {
//        final TabInfo tabInfo = mTabs.get(position);
//        return Fragment.instantiate(mActivity, tabInfo.fragmentClass.getName(), tabInfo.args);
//    }
//
//    @Override
//    public int getItemPosition(final Object object) {
//    /* Get the current position. */
//        int position = mActionBar.getSelectedTab().getPosition();
//
//    /* The default value. */
//        int pos = POSITION_NONE;
//        if (history.get(position).isEmpty()) {
//            return POSITION_NONE;
//        }
//
//    /* Checks if the object exists in current history. */
//        for (Stack<TabInfo> stack : history.values()) {
//            TabInfo c = stack.peek();
//            if (c.fragmentClass.getName().equals(object.getClass().getName())) {
//                pos = POSITION_UNCHANGED;
//                break;
//            }
//        }
//        return pos;
//    }
//
//    @Override
//    public int getCount() {
//        return mTabs.size();
//    }
//
//    @Override
//    public void onPageScrollStateChanged(int arg0) {
//    }
//
//    @Override
//    public void onPageScrolled(int arg0, float arg1, int arg2) {
//    }
//
//    @Override
//    public void onPageSelected(int position) {
//        mActionBar.setSelectedNavigationItem(position);
//    }
//
//    @Override
//    public void onTabSelected(final ActionBar.Tab tab, final FragmentTransaction ft) {
//        TabInfo tabInfo = (TabInfo) tab.getTag();
//        for (int i = 0; i < mTabs.size(); i++) {
//            if (mTabs.get(i).equals(tabInfo)) {
//                mPager.setCurrentItem(i);
//            }
//        }
//    }
//
//    @Override
//    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
//    }
//
//    @Override
//    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
//    }
//
//    public void replace(final int position, final Class fragmentClass, final Bundle args) {
//    /* Save the fragment to the history. */
//        mActivity.getSupportFragmentManager().beginTransaction().addToBackStack(null).commit();
//
//    /* Update the tabs. */
//        updateTabs(new TabInfo(fragmentClass, args), position);
//
//    /* Updates the history. */
//        history.get(position).push(new TabInfo(mTabs.get(position).fragmentClass, mTabs.get(position).args));
//
//        notifyDataSetChanged();
//    }
//
//    /**
//     * Updates the tabs.
//     *
//     * @param tabInfo
//     *          the new tab info
//     * @param position
//     *          the position
//     */
//    private void updateTabs(final TabInfo tabInfo, final int position) {
//        mTabs.remove(position);
//        mTabs.add(position, tabInfo);
//        mActionBar.getTabAt(position).setTag(tabInfo);
//    }
//
//    /**
//     * Creates the history using the current state.
//     */
//    public void createHistory() {
//        int position = 0;
//        TOTAL_TABS = mTabs.size();
//        for (TabInfo mTab : mTabs) {
//            if (history.get(position) == null) {
//                history.put(position, new Stack<TabInfo>());
//            }
//            history.get(position).push(new TabInfo(mTab.fragmentClass, mTab.args));
//            position++;
//        }
//    }