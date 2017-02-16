package org.openhab.binding.insteonplm.internal.device.messages;

import java.util.HashMap;

import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants.ExtendedData;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.GroupMessageStateMachine;

import com.google.common.collect.Maps;

/**
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class MessageHandlerData {
    public DeviceFeature feature;
    public HashMap<Integer, GroupMessageStateMachine> groupState = Maps.newHashMap();
    public int group = -1;
    public int button = -1;
    public int cmd1 = -1;
    public int cmd2 = -1;
    public int data1 = -1;
    public int data2 = -1;
    public int data3 = -1;
    public InsteonPLMBindingConstants.ExtendedData extended = ExtendedData.extendedNone;

    public MessageHandlerData() {
    }
}