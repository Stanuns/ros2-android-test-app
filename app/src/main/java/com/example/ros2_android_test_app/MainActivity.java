package com.example.ros2_android_test_app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.ros2.rcljava.RCLJava;

import geometry_msgs.msg.Vector3;

public class MainActivity extends ROSActivity {

    private static final String IS_WORKING_TALKER = "isWorkingTalker";
    private static final String IS_WROKING_LISTENER = "isWorkingListener";

    private ListenerNode listenerNode;
    private TalkerNode talkerNode;

    private TextView listenerView;

    private static String logtag = MainActivity.class.getName();

    private boolean isWorkingListener;
    private boolean isWorkingTalker;

    private JoystickView joystickRight;
    private ControlNode control_node;
    private TextView mTextViewLinearVRight;
    private TextView mTextViewRotationalSRight;

    /** Called when the activity is first created. */
    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            isWorkingListener = savedInstanceState.getBoolean(IS_WROKING_LISTENER);
            isWorkingTalker = savedInstanceState.getBoolean(IS_WORKING_TALKER);
        }

        /**
        Button listenerStartBtn = (Button)findViewById(R.id.listenerStartBtn);
        listenerStartBtn.setOnClickListener(startListenerListener);
        Button listenerStopBtn = (Button)findViewById(R.id.listenerStopBtn);
        listenerStopBtn.setOnClickListener(stopListenerListener);

        Button talkerStartBtn = (Button)findViewById(R.id.talkerStartBtn);
        talkerStartBtn.setOnClickListener(startTalkerListener);
        Button talkerStopBtn = (Button)findViewById(R.id.talkerStopBtn);
        talkerStopBtn.setOnClickListener(stopTalkerListener);

        listenerView = (TextView)findViewById(R.id.listenerTv);
        listenerView.setMovementMethod(new ScrollingMovementMethod());
         **/

        joystickRight = findViewById(R.id.joystickView_right);

        RCLJava.rclJavaInit();

        listenerNode =
                new ListenerNode("ros2humblenode_listener", "/chatter", listenerView);//ros2galacticnode_listener

        talkerNode = new TalkerNode("ros2humblenode_talker", "/chatter");//ros2galacticnode_talker

        changeListenerState(false);
        changeTalkerState(false);

        control_node = new ControlNode("HomeRobot_control", "/cmd_vel");
        getExecutor().addNode(control_node);
        joyControl();
    }

    // Create an anonymous implementation of OnClickListener
    private OnClickListener startListenerListener = new OnClickListener() {
        public void onClick(final View view) {
            changeListenerState(true);
        }
    };

    // Create an anonymous implementation of OnClickListener
    private OnClickListener stopListenerListener = new OnClickListener() {
        public void onClick(final View view) {
            Log.d(logtag, "onClick() called - stop listener button");
            changeListenerState(false);
        }
    };

    private OnClickListener startTalkerListener = new OnClickListener() {
        public void onClick(final View view) {
            Log.d(logtag, "onClick() called - start talker button");
            changeTalkerState(true);
        }
    };

    // Create an anonymous implementation of OnClickListener
    private OnClickListener stopTalkerListener = new OnClickListener() {
        public void onClick(final View view) {
            Log.d(logtag, "onClick() called - stop talker button");
            changeTalkerState(false);
        }
    };

    private void changeListenerState(boolean isWorking) {
        this.isWorkingListener = isWorking;
        /**
        Button buttonStart = (Button)findViewById(R.id.listenerStartBtn);
        Button buttonStop = (Button)findViewById(R.id.listenerStopBtn);
        buttonStart.setEnabled(!isWorking);
        buttonStop.setEnabled(isWorking);
         **/
        if (isWorking){
            getExecutor().addNode(listenerNode);
        } else {
            getExecutor().removeNode(listenerNode);
        }
    }

    private void changeTalkerState(boolean isWorking) {
        this.isWorkingTalker = isWorking;
        /**
        Button buttonStart = (Button)findViewById(R.id.talkerStartBtn);
        Button buttonStop = (Button)findViewById(R.id.talkerStopBtn);
        buttonStart.setEnabled(!isWorking);
        buttonStop.setEnabled(isWorking);
         **/
        if (isWorking){
            getExecutor().addNode(talkerNode);
            talkerNode.start();

            std_msgs.msg.String msg = new std_msgs.msg.String();
            talkerNode.publisher.publish(msg);
        } else {
            talkerNode.stop();
            getExecutor().removeNode(talkerNode);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.putBoolean(IS_WROKING_LISTENER, isWorkingListener);
            outState.putBoolean(IS_WORKING_TALKER, isWorkingTalker);
        }
        super.onSaveInstanceState(outState);
    }



    @SuppressLint("DefaultLocale")
    private void joyControl(){
        mTextViewLinearVRight = findViewById(R.id.textView2);
        mTextViewRotationalSRight = findViewById(R.id.textView3);
        joystickRight = findViewById(R.id.joystickView_right);
        joystickRight.setOnMoveListener((angle, strength) -> {
            //手动设置最大线速度与角速度
            double max_v = 0.8;//m/s
            double max_a = 0.6;//rad/s
            double now_v = max_v * Math.sin(angle*Math.PI/180) * strength/100; //当前摇杆所推力度对应速度值
            double now_a = max_a * Math.cos(angle*Math.PI/180) * strength/100;

            Vector3 vec_lv = new Vector3();
            Vector3 vec_av = new Vector3();
//            vec_lv.setX(-now_v);
//            vec_av.setZ(-now_a);
            vec_lv.setX(now_v);
            vec_av.setZ(now_a);

            control_node.pubVelocity(vec_lv, vec_av);
            mTextViewLinearVRight.setText(String.format("Current linear velocity:%1$.2f m/s ", now_v));
            mTextViewRotationalSRight.setText(String.format("Current rotational speed:%1$.2f rad/s ", now_a));
        });
    }
}