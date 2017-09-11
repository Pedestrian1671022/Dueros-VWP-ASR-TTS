package com.example.pedestrian_username.dueros_vwp_asr_tts;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AddEditRobotItemDialogFragment.DialogListener, ConfirmDeleteDialogFragment.DialogListener {

    private MenuItem menuItem;
    private RecyclerView recyclerView;
    private List<RobotItem> list;
    private ViewHolderAdapter viewHolderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        list = new ArrayList<RobotItem>();
        viewHolderAdapter = new ViewHolderAdapter(this, list);
        recyclerView.setAdapter(viewHolderAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_robot_item_menu, menu);
        menuItem = menu.findItem(R.id.action_add_robot);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        AddEditRobotItemDialogFragment addRobotItemDialogFragment = new AddEditRobotItemDialogFragment();
        addRobotItemDialogFragment.show(getSupportFragmentManager(), "addrobotitemdialog");
        return true;
    }

    @Override
    public void onAddEditDialogPositiveClick(RobotItem robotItem, int position) {
        if (position == -1)
            viewHolderAdapter.addData(0, robotItem);
        else {
            list.set(position, robotItem);
            viewHolderAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onAddEditDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onConfirmDeleteDialogPositiveClick(int position) {
        viewHolderAdapter.removeData(position);
    }

    @Override
    public void onConfirmDeleteDialogNegativeClick() {

    }
}
