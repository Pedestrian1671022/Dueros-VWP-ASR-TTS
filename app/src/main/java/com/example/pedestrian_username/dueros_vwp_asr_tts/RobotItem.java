package com.example.pedestrian_username.dueros_vwp_asr_tts;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Container for information about connections to specific Robots.
 *
 * Created by Michael Brunson on 1/23/16.
 */
public class RobotItem {

    public static final String ROBOT_NAME_KEY = "ROBOT_NAME_KEY";
    /** Bundle key for master URI */
    public static final String MASTER_URI_KEY = "MASTER_URI_KEY";

    private String robot_name;

    private String master_uri;

    public String getRobot_name() {
        return robot_name;
    }

    public void setRobot_name(String robot_name) {
        this.robot_name = robot_name;
    }

    public String getMaster_uri() {
        return master_uri;
    }

    public void setMaster_uri(String master_uri) {
        this.master_uri = master_uri;
    }

    public RobotItem() {
        this.robot_name = "Robot";
        this.master_uri = "http://localhost:11311";
    }

    public RobotItem(String robot_name, String master_uri) {
        this.robot_name = robot_name;
        this.master_uri = master_uri;
    }

    public void save(@NonNull Bundle bundle) {
        bundle.putString(ROBOT_NAME_KEY, robot_name);
        bundle.putString(MASTER_URI_KEY, master_uri);
    }

    public void load(@NonNull Bundle bundle) {
        robot_name = bundle.getString(ROBOT_NAME_KEY, "");
        master_uri = bundle.getString(MASTER_URI_KEY, "http://localhost:11311");
    }
}
