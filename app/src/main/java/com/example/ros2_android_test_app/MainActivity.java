package com.example.ros2_android_test_app;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.RawResourceDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.ui.PlayerView;


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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ros2.rcljava.RCLJava;

import geometry_msgs.msg.Vector3;

@UnstableApi public class MainActivity extends ROSActivity {

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

    private PlayerView exoPlayerView;
    private MediaSource mediaSource;
    private ExoPlayer player;
    private DefaultLoadControl customLoadControl;

    private EditText rtsp_url;

    /** Called when the activity is first created. */
    @OptIn(markerClass = UnstableApi.class) @Override
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

        String rtspUri = "rtsp://192.168.251.121:8554/front";
        exoPlayerView = findViewById(R.id.exo_player);
        // Create an RTSP media source pointing to an RTSP uri.
        mediaSource =
                new RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(rtspUri));
        // reduce rtsp time latency
        customLoadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(200, 300, 100, 100)
                .build();
        // Create a player instance.
        player = new ExoPlayer.Builder(this).setLoadControl(customLoadControl).build();
//        player = new ExoPlayer.Builder(this).build();

        // Set the media source to be played.
        player.setMediaSource(mediaSource);
        // Prepare the player.
        player.prepare();
        exoPlayerView.setPlayer(player);
        player.play();

        Button setRtspUrlBtn = (Button)findViewById(R.id.rtsp_url_btn);
        rtsp_url = findViewById(R.id.rtsp_url);
        setRtspUrlBtn.setOnClickListener(setRtspUrlListener);
//        setRtspUrlBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String newRtspUri = rtsp_url.getText().toString();
//                //Log.e("cs","new rtsp url:"+newRtspUri);
//            }
//        });

    }

    private OnClickListener setRtspUrlListener = new OnClickListener() {
        @OptIn(markerClass = UnstableApi.class) public void onClick(final View view) {
            String newRtspUri = rtsp_url.getText().toString();
            player.stop();
            mediaSource =
                    new RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(newRtspUri));
            // Set the media source to be played.
            player.setMediaSource(mediaSource);
            player.prepare();
            exoPlayerView.setPlayer(player);
            player.play();
        }
    };

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