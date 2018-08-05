/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @author Ganesh Ingle <ganesh.ingle@asvilabs.com>
 */

package org.openhab.binding.wakeonlan;

/**
 * The {@link WakeOnLanConfiguration} is the class used to match the
 * thing configuration
 *
 * @author Ganesh Ingle - Initial contribution
 */
public class WakeOnLanConfiguration {
    public String targetIP;
    public String targetMAC;
    public Integer targetUDPPort;
    public Boolean sendOnAllInterfaces;
    public String sendOnInterface;
    public Boolean setSocketOptionBroadcast;
    public Boolean periodicPing;
    public Boolean externalPing;
    public String pingHostnameOrIp;
    public Integer pingIntervalMinutes;
    public String shutdownCommands;
    public String shutdownCommandExt;
}
