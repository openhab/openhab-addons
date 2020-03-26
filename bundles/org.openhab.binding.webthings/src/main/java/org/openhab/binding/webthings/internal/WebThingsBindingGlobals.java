/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthings.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WebThingsBindingGlobals} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sven Schneider - Initial contribution
 */
@NonNullByDefault
public class WebThingsBindingGlobals {

    // List of all binding configuration parameters
    public static String token = "";
    public static String serverUrl = "";
    public static String openhabIp = "";
    public static Boolean mozilla = true;
    public static String system  = "";
    public static String userdataPath  = "";
    public static Boolean backgroundDiscovery = true;

    /**
     * Set binding configuration params
     * @param params Parameter list
     */
    public static void setParams(Map<String,Object> params){
        if(params.get("serverUrl") != null){
            serverUrl = (String) params.get("serverUrl");
        }
        if(params.get("token") != null){
            token = (String) params.get("token");
        }
        if(params.get("openhabIp") != null){
            openhabIp = (String) params.get("openhabIp");
        }
        if(params.get("mozilla") != null){
            mozilla = (Boolean) params.get("mozilla");
        }
        if(params.get("system") != null){
            system = (String) params.get("system");
        }
        if(params.get("userdataPath") != null){
            userdataPath = (String) params.get("userdataPath");
        }
        if(params.get("backgroundDiscovery") != null){
            backgroundDiscovery = (Boolean) params.get("backgroundDiscovery");
        }
    }
}
