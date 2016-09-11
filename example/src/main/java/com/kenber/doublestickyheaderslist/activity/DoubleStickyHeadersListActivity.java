package com.kenber.doublestickyheaderslist.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.kenber.doublestickyheaderslist.R;
import com.kenber.doublestickyheaderslist.data.ListItem;
import com.kenber.doublestickyheaderslist.ui.ListAdapter;

/**
 * @author Kenber
 */
public class DoubleStickyHeadersListActivity extends ListActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_sticky_headers_list);
        setListAdapter(new ListAdapter(this, R.layout.view_list_item, R.id.text1));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ListItem item  = (ListItem) getListView().getAdapter().getItem(position);
        if (item != null) {
            Toast.makeText(getApplicationContext(), "Item " + position + ": level " + item.level + ", text: " + item.text, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Item " + position + ": not exist", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this, "Item: " + view.getTag(), Toast.LENGTH_SHORT).show();
    }

}
