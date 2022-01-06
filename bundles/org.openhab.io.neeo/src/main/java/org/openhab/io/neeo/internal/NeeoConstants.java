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
package org.openhab.io.neeo.internal;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;

/**
 * The constants class for the NEEO Integration
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoConstants {

    /**
     * The following are required by NEEO brain (as of 51.1+)
     * No backward compatibility issues however - just going
     * forward since the brain checks for these prefixes/suffixes
     * to identify things
     */
    private static final String NEEO_SDKNAME_PREFIX = "src-";
    public static final String NEEO_ADAPTER_PREFIX = "apt-";
    public static final String NEEO_SENSOR_SUFFIX = "_SENSOR";

    /** Type used for sensor notification updates */
    public static final String NEEO_SENSOR_NOTIFICATION_TYPE = "DEVICE_SENSOR_UPDATE";

    /** As of version .51.1 - new sensor notification API - old one doesn't work */
    public static final String NEEO_FIRMWARE_0_51_1 = "0.51.1";

    /** Binding ID/Thing Types for integration created things - must match app.js */
    public static final String NEEOBINDING_BINDING_ID = "neeo";
    public static final String NEEOBINDING_DEVICE_ID = NEEOBINDING_BINDING_ID + ":device";
    public static final String NEEOIO_BINDING_ID = "neeo_io";
    public static final String VIRTUAL_THING_TYPE = "virtual";

    /** Constant used to identify thread pool name */
    public static final String THREAD_POOL_NAME = "neeoio";

    /** Constants used for the Web APP */
    public static final String WEBAPP_PREFIX = "/neeo";
    public static final String WEBAPP_DASHBOARD_PREFIX = "/neeostatus";

    /** The MDNS type for neeo */
    public static final String NEEO_MDNS_TYPE = "_neeo._tcp.local.";

    /** The constants used for configuration */
    public static final String CFG_EXPOSE_ALL = "exposeAll";
    public static final String CFG_EXPOSENEEOBINDING = "exposeNeeoBinding";
    public static final String CFG_CHECKSTATUSINTERVAL = "checkStatusInterval";
    public static final String CFG_SEARCHLIMIT = "searchLimit";

    /** The name of the adapter */
    public static final String ADAPTER_NAME = NEEO_SDKNAME_PREFIX + "openHAB";

    /** The default port the brain listens on. */
    public static final int DEFAULT_BRAIN_PORT = 3000;

    /** The default protocol for the brain. */
    public static final String PROTOCOL = "http://";

    /** The device definitions file name */
    public static final String FILENAME_DEVICEDEFINITIONS = OpenHAB.getUserDataFolder() + File.separator + "neeo"
            + File.separator + "neeodefinitions.json";
    public static final String FILENAME_DISCOVEREDBRAINS = OpenHAB.getUserDataFolder() + File.separator + "neeo"
            + File.separator + "discoveredbrains.json";

    /** The search threshold value */
    public static final double SEARCH_MATCHFACTOR = 0.5;

    /** Various brain URLs */
    private static final String NEEO_VERSION = "/v1";
    public static final String REGISTER_SDK_ADAPTER = NEEO_VERSION + "/api/registerSdkDeviceAdapter";
    public static final String UNREGISTER_SDK_ADAPTER = NEEO_VERSION + "/api/unregisterSdkDeviceAdapter";
    public static final String PROJECTS_HOME = NEEO_VERSION + "/projects/home";
    public static final String RECIPES = NEEO_VERSION + "/api/recipes";
    public static final String SYSTEMINFO = NEEO_VERSION + "/systeminfo";
    public static final String IDENTBRAIN = SYSTEMINFO + "/identbrain";
    public static final String NOTIFICATION = NEEO_VERSION + "/notifications";
    public static final String CAPABILITY_PATH_PREFIX = "/device";
    public static final String GETLOG = "/curl";

    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int DEFAULT_OPENHAB_PORT = 8080;
}
