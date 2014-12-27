package com.taishonet.tabopera;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by taisho6339 on 2014/07/16.
 */
public class BaseTabBarActivity extends FragmentActivity implements TabHost.OnTabChangeListener {

    private ArrayList<String> mTabTagList = new ArrayList<>();
    private ArrayList<Integer> mTabButtonResourceList = new ArrayList<>();
    private ArrayList<Integer> mTabLabelResourceList = new ArrayList<>();
    private ArrayList<Class> mFragmentList = new ArrayList<>();

    private FragmentManager mFragmentManager;
    private FragmentStackTabHost mTabHost;

    //The Tab which selected in last.
    private View mlastTabView = null;
    private int mTabMenuLayoutId = R.layout.menu_tab_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();
    }

    @Override
    public void onTabChanged(String tabId) {
        //タブメニューラベルの色を変更
        View view = mTabHost.getCurrentTabView();
        setTabLabelColor(mlastTabView, getResources().getColor(R.color.white));
        setTabLabelColor(view, getResources().getColor(R.color.blue));
        mlastTabView = view;
    }

    @Override
    public void onBackPressed() {

        if (backControl())
            return;
        try {
            super.onBackPressed();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private boolean backControl() {

        Stack<Fragment> stack = mTabHost.getCurrentTabStack();

        if (!stack.isEmpty()) {

            if (stack.size() == 1) {
                return true;
            } else {
                Fragment lastFragment = stack.pop();
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.setCustomAnimations(R.anim.fragment_push_animation, R.anim.fragment_pop_animation);
                ft.remove(lastFragment).commit();
            }
            Fragment newFragment = stack.peek();
            mFragmentManager.beginTransaction().attach(newFragment).commit();
            return true;
        }
        return false;
    }

    public void showTabBar() {
        initTabHost();
    }

    public void addTab(String tag, int buttonImageId, int labelId, Class<Fragment> fragment) {
        mTabTagList.add(tag);
        mTabButtonResourceList.add(buttonImageId);
        mTabLabelResourceList.add(labelId);
        mFragmentList.add(fragment);
    }

    //Set Each Tab Content Layout Id
    public void setMenuTabLayout(int layoutId) {
        mTabMenuLayoutId = layoutId;
    }

    //Set Animation which is going to play when Fragment will start and end.
    public void setAddFragmentAnimation(FragmentTransaction ft) {
        ft.setCustomAnimations(R.anim.fragment_push_animation, R.anim.fragment_pop_animation);
    }

    public void changeTab(int index) {
        mTabHost.setCurrentTab(index);
    }


    protected void addFragmentToTabStack(Fragment fragment) {
        Stack<Fragment> stack = mTabHost.getCurrentTabStack();
        mFragmentManager.beginTransaction().detach(stack.peek()).commit();
        stack.add(fragment);
    }

    //Get Fragment Stack binding with current tab.
    protected Stack<Fragment> getCurrentTabStack() {
        return mTabHost.getCurrentTabStack();
    }

    private void setTabLabelColor(View content, int color) {
        TextView label = (TextView) content.findViewById(R.id.tab_label);
        label.setTextColor(color);
    }

    private void initTabHost() {
        mTabHost = new FragmentStackTabHost(this);
        View layoutw = getLayoutInflater().inflate(R.layout.activity_main_menu, null);
        mTabHost.addView(layoutw);
        mTabHost.setup(this, mFragmentManager, R.id.content);

        for (int i = 0; i < mFragmentList.size(); i++) {
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTabTagList.get(i));
            View layout = getLayoutInflater().inflate(mTabMenuLayoutId, null);
            ImageView icon = (ImageView) layout.findViewById(R.id.tab_icon);
            TextView label = (TextView) layout.findViewById(R.id.tab_label);
            icon.setImageResource(mTabButtonResourceList.get(i));
            label.setText(mTabLabelResourceList.get(i));
            tabSpec.setIndicator(layout);
            mTabHost.addTab(tabSpec, mFragmentList.get(i), new Bundle());
        }
        mlastTabView = mTabHost.getCurrentTabView();
        setTabLabelColor(mlastTabView, getResources().getColor(R.color.blue));
        mTabHost.setOnTabChangedListener(this);
        setContentView(mTabHost);
    }

}
