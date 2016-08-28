package com.kenber.doublestickyheaderslist.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.kenber.doublestickyheaderslist.R;

public class DoubleStickyHeadersListActivity extends ListActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_sticky_headers_list);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

    }

    @Override
    public void onClick(View view) {

    }
}
