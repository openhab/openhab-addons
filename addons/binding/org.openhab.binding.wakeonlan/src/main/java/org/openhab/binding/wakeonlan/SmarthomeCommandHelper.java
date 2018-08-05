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

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;

/**
 * Helper service to send one or more commands to openhab items or channels
 *
 * @author Ganesh Ingle - Initial contribution
 *
 */
public interface SmarthomeCommandHelper {

    /**
     * Send one or more commands to openhab items or things.
     * Multiple targets should be separated by <b>&&</b><br/>
     * <br/>
     * Target could be in any of following formats:<br/>
     * <br/>
     * Item Name | Command<br/>
     * ChannelUID | Command<br/>
     * <br/>
     *
     * Command could be any command supported by that item or channel.<br/>
     * <br/>
     * Examples:<br/>
     * asvilabs_appliance_serial_bridge1_Appliance_3_2_status|ON<br/>
     * asvilabs:appliance:serial-bridge1:Appliance_3_2:status|ON<br/>
     * kodi:kodi:mykodi1:systemcommand|shutdown && asvilabs:appliance:serial-bridge1:Appliance_2_1:status|OFF<br/>
     *
     * @param commandsIn commands to send
     * @throws InterruptedException
     */
    void handleCommands(@NonNull String commandsIn, @NonNull Logger logger) throws InterruptedException;

    /**
     * Send a single command to openhab item or channel.
     * <br/>
     * Target could be in any of following formats:<br/>
     * <br/>
     * Item Name | Command<br/>
     * ChannelUID | Command<br/>
     * <br/>
     *
     * Command could be any command supported by that item or channel.<br/>
     * <br/>
     * Examples:<br/>
     * asvilabs_appliance_serial_bridge1_Appliance_3_2_status|ON<br/>
     * asvilabs:appliance:serial-bridge1:Appliance_3_2:status|ON<br/>
     *
     * @param commandsIn commands to send
     * @throws InterruptedException
     */
    void handleCommand(@NonNull String cmd, @NonNull Logger logger) throws InterruptedException;

}
