/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.amazondashbutton.internal.config;

/**
 * The configuration of the Amazon Dash Button
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class AmazonDashButtonConfig {

    /**
     * The MAC address of the Amazon Dash Button
     */
    public String macAddress;

    /**
     * The network interface which receives the packets of the Amazon Dash Button
     */
    public String pcapNetworkInterfaceName;

    /**
     * Often a single button press is recognized multiple times. You can specify how long any further detected button
     * pressed should be ignored after one click is handled (in ms).
     */
    public Integer packetInterval;
}
