TabOpera
========

Simple Tab UI Operation Library for Android.
Tab Operation is very complex in nest of Fragments, so this library simplify it.
Each Tab manages Fragment Stack.

Installation
---------

Add dependency in your build settings.

```groovy

repositories {
    mavenCentral()
    maven {
        url 'https://raw.github.com/taisho6339/TabOpera/master/tabopera/repository'
    }
}

dependencies {
      ...
      compile 'taisho6339:tab-opera:0.0.3@aar'
}

```

Example
---------

### Normal Usage


```Java

public class MainActivity extends BaseTabBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button tab = new Button(this);
        tab.setText("tab1");
        addTab("tag1", tab, Fragment.class);

        tab = new Button(this);
        tab.setText("tab2");
        addTab("tag2", tab, Fragment.class);

        tab = new Button(this);
        tab.setText("tab3");
        addTab("tag3", tab, Fragment.class);

        showTabBar();
    }

    @Override
    public void onTabChanged(String tabId) {
        super.onTabChanged(tabId);
        //please write your code for tab content feedback.
        // For example,it is changing label color.
    }
}

```

### Add Fragment to Stack

```Java
        //Please call after onResume
         
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = new Fragment();

        //Set Enter and Exit Animation of Fragment.
        setAddFragmentAnimation(ft);

        ft.add(com.taishonet.tabopera.R.id.content, fragment).commit();
       
        addFragmentToTabStack(fragment);
        
```

This Behavior looks like Activity Stack.
