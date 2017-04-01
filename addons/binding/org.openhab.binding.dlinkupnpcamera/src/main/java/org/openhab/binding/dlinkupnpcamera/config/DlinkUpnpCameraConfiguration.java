/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinkupnpcamera.config;

/**
 * The {@link DlinkUpnpCameraConfiguration} contains parameters for the configuration of a camera.
 *
 * @author Yacine Ndiaye
 * @author Antoine Blanc
 * @author Christopher Law
 */
public class DlinkUpnpCameraConfiguration {

    public static final String UDN = "udn";
    public static final String IP = "ip";
    public static final String NAME = "name";

    public String udn;
    public String username;
    public String password;
    public String commandRequest;
    public String imageRequest;
    public Integer connectionRefresh;

}
