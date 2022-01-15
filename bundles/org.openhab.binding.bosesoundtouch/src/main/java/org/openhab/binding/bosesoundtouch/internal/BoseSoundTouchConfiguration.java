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
package org.openhab.binding.bosesoundtouch.internal;

import org.openhab.core.thing.Thing;

/**
 * Configuration class for soundtouch
 *
 * @author Ivaylo Ivanov - Initial contribution
 */
public class BoseSoundTouchConfiguration {

    // Device configuration parameters;
    public static final String HOST = "host";
    public static final String MAC_ADDRESS = Thing.PROPERTY_MAC_ADDRESS;
    public static final String APP_KEY = "appKey";

    public String host;
    public String macAddress;
    public String appKey;

    // Not an actual configuration field, but it will contain the name of the group (in case of Stereo Pair)
    public String groupName;
}
