/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.client;

import java.util.Map;

/**
 * This object represents a device connected to a wink hub. Currently abstracts away the json bits
 *
 * @author scrosby
 *
 */
public interface IWinkDevice {
    /**
     * The UUID of the device
     *
     * @return String unique device ID
     */
    public String getId();

    /**
     * The Name of the device
     *
     * @return String the friendly name for the device
     */
    public String getName();

    /**
     * The Device Type
     *
     * @return Enum that represents the type of device this is
     */
    public WinkSupportedDevice getDeviceType();

    /**
     * The current state of the device
     *
     * @return A Map of state parameters and values for the device
     */
    public Map<String, String> getCurrentState();

    /**
     * The desired state which is transitional
     *
     * @return A Map of state parameters which have been requested to be applied to a device
     */
    public Map<String, String> getDesiredState();

    /**
     * Returns the pubnub subscriber key. Pubnub is used to communicate device events to
     * the class
     *
     * @return String configured pubnub subscriber key
     */
    public String getPubNubSubscriberKey();

    /**
     * The channel in which to listen for pubnub messages
     *
     * @return String the name of the channel where device updates are published by the wink API
     */
    public String getPubNubChannel();

    /**
     * Generic top level property access
     *
     * @param property The name of the top level property required
     * @return The associated value of the property selected.
     */
    public String getProperty(String property);
}
