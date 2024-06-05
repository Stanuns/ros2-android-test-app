package com.example.ros2_android_test_app;

import org.ros2.rcljava.node.BaseComposableNode;
import org.ros2.rcljava.publisher.Publisher;

import geometry_msgs.msg.Twist;
import geometry_msgs.msg.Vector3;

public class ControlNode extends BaseComposableNode {

    private static String logtag = ControlNode.class.getName();
    private final String topic;
    public Publisher<geometry_msgs.msg.Twist> twist_pub;

    public ControlNode(final String name, final String topic){
        super(name);
        this.topic = topic;
        this.twist_pub = this.node.<Twist>createPublisher(
                geometry_msgs.msg.Twist.class, this.topic);
    }

    public void pubVelocity(Vector3 vec_lv, Vector3 vec_av){
        Twist m_twist = new Twist();
        m_twist.setLinear(vec_lv);
        m_twist.setAngular(vec_av);
        this.twist_pub.publish(m_twist);
    }

}
