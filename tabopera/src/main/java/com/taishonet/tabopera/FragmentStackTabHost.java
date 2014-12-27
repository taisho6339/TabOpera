package com.taishonet.tabopera;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.Stack;

public class FragmentStackTabHost extends TabHost
        implements TabHost.OnTabChangeListener {

    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private FrameLayout mRealTabContent;
    private Context mContext;
    private FragmentManager mFragmentManager;
    private int mContainerId;
    private OnTabChangeListener mOnTabChangeListener;
    private TabInfo mLastTab;
    private boolean mAttached;

    static final class TabInfo {
        private final String tag;
        private final Class<?> clss;
        private Stack<Fragment> backstack = new Stack<Fragment>();
        private Stack<Bundle> bundleStack = new Stack<Bundle>();

        TabInfo(String _tag, Class<?> _class, Bundle _args) {
            tag = _tag;
            clss = _class;
            bundleStack.add(_args);
        }
    }


    static class DummyTabFactory implements TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    static class SavedState extends BaseSavedState {
        String curTab;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            curTab = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(curTab);
        }

        @Override
        public String toString() {
            return "FragmentTabHost.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " curTab=" + curTab + "}";
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public FragmentStackTabHost(Context context) {
        // Note that we call through to the version that takes an AttributeSet,
        // because the simple Context construct can result in a broken object!
        super(context, null);
        initFragmentTabHost(context, null);
    }

    private void initFragmentTabHost(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                new int[]{android.R.attr.inflatedId}, 0, 0);
        mContainerId = a.getResourceId(0, 0);
        a.recycle();
        super.setOnTabChangedListener(this);
    }


    public void setup(Context context, FragmentManager manager, int containerId) {
        super.setup();
        mContext = context;
        mFragmentManager = manager;
        mContainerId = containerId;
        ensureContent();
        mRealTabContent.setId(containerId);

        if (getId() == View.NO_ID) {
            setId(android.R.id.tabhost);
        }
    }

    private void ensureContent() {
        if (mRealTabContent == null) {
            mRealTabContent = (FrameLayout) findViewById(mContainerId);
            if (mRealTabContent == null) {
                throw new IllegalStateException(
                        "No tab content FrameLayout found for id " + mContainerId);
            }
        }
    }

    @Override
    public void setOnTabChangedListener(OnTabChangeListener l) {
        mOnTabChangeListener = l;
    }

    public void addTab(TabSpec tabSpec, Class<?> clss, Bundle args) {
        tabSpec.setContent(new DummyTabFactory(mContext));
        String tag = tabSpec.getTag();

        TabInfo info = new TabInfo(tag, clss, args);

        if (mAttached) {
            info.backstack.add(mFragmentManager.findFragmentByTag(tag));
            Fragment fragment = info.backstack.get(0);
            if (fragment != null && !fragment.isDetached()) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.detach(fragment);
                ft.commit();
            }
        }

        mTabs.add(info);
        addTab(tabSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        String currentTab = getCurrentTabTag();

        FragmentTransaction ft = null;
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            Fragment fragment = tab.backstack.isEmpty() ? null : tab.backstack.peek();
            if (fragment != null && !fragment.isDetached()) {
                if (tab.tag.equals(currentTab)) {
                    mLastTab = tab;
                } else {
                    if (ft == null) {
                        ft = mFragmentManager.beginTransaction();
                    }
                    ft.detach(fragment);
                }
            }
        }

        mAttached = true;
        ft = doTabChanged(currentTab, ft);
        if (ft != null) {
            ft.commit();
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttached = false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.curTab = getCurrentTabTag();
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentTabByTag(ss.curTab);
    }

    @Override
    public void onTabChanged(String tabId) {
        if (mAttached) {
            FragmentTransaction ft = doTabChanged(tabId, null);
            if (ft != null) {
                ft.commit();
            }
        }
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(tabId);
        }
    }

    public Stack<Fragment> getCurrentTabStack() {
        TabInfo newTab = null;
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            if (tab.tag.equals(getCurrentTabTag())) {
                newTab = tab;
            }
        }
        return newTab.backstack;
    }


    private FragmentTransaction doTabChanged(String tabId, FragmentTransaction ft) {
        TabInfo newTab = null;
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            if (tab.tag.equals(tabId)) {
                newTab = tab;
            }
        }
        if (newTab == null) {
            throw new IllegalStateException("No tab known for tag " + tabId);
        }

        Fragment newFragment = newTab.backstack.isEmpty() ? null : newTab.backstack.peek();
        if (mLastTab != newTab) {
            if (ft == null) {
                ft = mFragmentManager.beginTransaction();
            }
            if (mLastTab != null) {
                Fragment lastFragment = mLastTab.backstack.peek();
                if (lastFragment != null) {
                    ft.detach(lastFragment);
                }
            }
            if (newTab != null) {
                if (newFragment == null) {
                    Bundle args = newTab.bundleStack.peek();
                    newFragment = Fragment.instantiate(mContext,
                            newTab.clss.getName(), args);
                    newTab.backstack.add(newFragment);
                    ft.add(mContainerId, newFragment, newTab.tag);
                } else {
                    ft.attach(newFragment);
                }
            }

            mLastTab = newTab;
        }
        return ft;
    }
}