package com.example.pedestrian_username.dueros_vwp_asr_tts;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pedestrian-username on 17-7-3.
 */

public class ViewHolderAdapter extends RecyclerView.Adapter<ViewHolderAdapter.MyViewHolder> {

    /** Bundle key for the name of the Robot being deleted */
    public static final String NAME_KEY = "DELETE_ITEM_NAME_KEY";
    /** Bundle key for the position of the RobotItem being deleted */
    public static final String POSITION_KEY = "DELETE_ITEM_POSITION_KEY";

    private List<RobotItem> list;
    private static RobotItem static_robotItem;
    private static AppCompatActivity appCompatActivity;

    public ViewHolderAdapter(AppCompatActivity appCompatActivity, List<RobotItem> list){
        this.appCompatActivity = appCompatActivity;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.robot_item, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return  myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
        myViewHolder.robot_name.setText(list.get(i).getRobot_name().toString());
        myViewHolder.master_uri.setText(list.get(i).getMaster_uri().toString());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        TextView robot_name;
        TextView master_uri;
        ImageView imageView;
        ImageButton imageButton_edit;
        ImageButton imageButton_delete;

        public MyViewHolder(View view) {
            super(view);
            robot_name = (TextView) view.findViewById(R.id.robot_name_text_view);
            master_uri = (TextView) view.findViewById(R.id.master_uri_text_view);
            imageView = (ImageView) view.findViewById(R.id.robot_wifi_image);
            imageButton_edit = (ImageButton) view.findViewById(R.id.robot_edit_button);
            imageButton_delete = (ImageButton) view.findViewById(R.id.robot_delete_button);
            imageButton_edit.setImageResource(R.drawable.ic_edit_black_24dp);
            imageButton_edit.setOnClickListener(this);
            imageButton_delete.setImageResource(R.drawable.ic_delete_black_24dp);
            imageButton_delete.setOnClickListener(this);
            imageView.setImageResource(R.drawable.wifi_0);
            imageView.setOnClickListener(this);

            Timer t = new Timer();

            t.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    try {
                        int position = getAdapterPosition();
                        final RobotItem robotItem = list.get(position);
                        URI uri = URI.create(robotItem.getMaster_uri());
                        if (isPortOpen(uri.getHost(), uri.getPort(), 10000)) {
                            appCompatActivity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    imageView.setImageResource(R.drawable.wifi_4);
                                }
                            });
                        } else {
                            appCompatActivity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    imageView.setImageResource(R.drawable.wifi_0);
                                }
                            });
                        }

                        Thread.sleep(10000);
                    } catch (Exception ignore) {

                    }
                }
            }, 1000, 15000);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Bundle bundle = new Bundle();
            RobotItem robotItem = list.get(position);

            switch (view.getId()){
                case R.id.robot_edit_button:
                    AddEditRobotItemDialogFragment addEditRobotItemDialogFragment = new AddEditRobotItemDialogFragment();
                    robotItem.save(bundle);
                    bundle.putInt(AddEditRobotItemDialogFragment.POSITION_KEY, position);
                    addEditRobotItemDialogFragment.setArguments(bundle);
                    addEditRobotItemDialogFragment.show(appCompatActivity.getSupportFragmentManager(), "editrobotialog");
                    break;
                case R.id.robot_delete_button:
                    ConfirmDeleteDialogFragment confirmDeleteDialogFragment = new ConfirmDeleteDialogFragment();
                    bundle.putInt(ConfirmDeleteDialogFragment.POSITION_KEY, position);
                    bundle.putString(ConfirmDeleteDialogFragment.NAME_KEY, robotItem.getRobot_name());
                    confirmDeleteDialogFragment.setArguments(bundle);
                    confirmDeleteDialogFragment.show(appCompatActivity.getSupportFragmentManager(), "deleterobotdialog");
                    break;
                default:
                    FragmentManager fragmentManager = appCompatActivity.getFragmentManager();
                    ConnectionProgressDialogFragment connectionProgressDialogFragment = new ConnectionProgressDialogFragment(robotItem);
                    connectionProgressDialogFragment.show(fragmentManager, "ConnectionProgressDialog");
                    break;
            }
        }
    }

    public static boolean isPortOpen(final String ip, final int port, final int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        }
        catch(ConnectException ce) {
            return false;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public void addData(int position, RobotItem robotItem) {
        list.add(position, robotItem);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, list.size());
    }

    public void removeData(int position) {
        list.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, list.size());
    }

    public static class ConnectionProgressDialogFragment extends DialogFragment {

        private static final String TAG = "ConnectionProgress";

        private final RobotItem robotItem;
        private Thread thread;

        /**
         * Default Constructor.
         */
        public ConnectionProgressDialogFragment()
        {
            this.robotItem = static_robotItem;

            if (this.robotItem == null)
                throw new IllegalArgumentException("info must be non null!");
        }

        /**
         * Creates a ConnectionProgressDialogFragment for the specified RobotInfo.
         * @param robotItem The RobotInfo with which to connect
         */
        @SuppressLint("ValidFragment")
        public ConnectionProgressDialogFragment(RobotItem robotItem)
        {
            this.robotItem = robotItem;
            static_robotItem = robotItem;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final ProgressDialog progressDialog
                    = ProgressDialog.show(appCompatActivity, "Connecting", "Connecting to "
                    + robotItem.getRobot_name() + " (" + robotItem.getMaster_uri() + ")", true, false);

            run();

            return progressDialog;
        }

        @Override
        public void onDestroy()
        {
            thread.interrupt();

            super.onDestroy();
        }

        /*
         * Starts the connection process.
         */
        private void run()
        {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(!isPortOpen(URI.create(robotItem.getMaster_uri()).getHost(), URI.create(robotItem.getMaster_uri()).getPort(), 10000)){
                            throw new Exception("Cannot connect to ROS. Please make sure ROS is running and that the Master URI is correct.");
                        }

                        final Intent intent = new Intent(appCompatActivity, RobotItemActivity.class);
                        // !!!---- EVIL USE OF STATIC VARIABLE ----!! //
                        // Should not be doing this but there is no other way that I can see -Michael
                        RobotItemActivity.static_robotItem = robotItem;

                        dismiss();

                        appCompatActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                appCompatActivity.startActivity(intent);
                            }
                        });
                    }
                    catch (final NetworkOnMainThreadException e){
                        dismiss();

                        appCompatActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(appCompatActivity, "Invalid Master URI", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    catch (InterruptedException e)
                    {
                        // Ignore
                        Log.d(TAG, "interrupted");
                    }
                    catch (final Exception e) {

                        if (ConnectionProgressDialogFragment.this.getFragmentManager() != null)
                            dismiss();

                        appCompatActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(appCompatActivity, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });

            thread.start();
        }

    }
}
