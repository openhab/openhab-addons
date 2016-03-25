/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.protocol;

import org.openhab.binding.pioneeravr.internal.protocol.ParameterizedCommand.ParameterizedCommandType;
import org.openhab.binding.pioneeravr.internal.protocol.SimpleCommand.SimpleCommandType;
import org.openhab.binding.pioneeravr.internal.protocol.ip.IpAvrConnection;

/**
 * Factory that allows to build IpControl commands/responses.
 * 
 * @author Antoine Besnard
 *
 */
public final class RequestResponseFactory {

    /**
     * Return a connection to the AVR with the given host and port.
     * 
     * @param host
     * @param port
     * @return
     */
    public static IpAvrConnection getConnection(String host, Integer port) {
        return new IpAvrConnection(host, port);
    }

    /**
     * Return a ParameterizedCommand of the type given in parameter.
     * 
     * @param command
     * @return
     */
    public static SimpleCommand getIpControlCommand(SimpleCommandType command) {
        SimpleCommand result = new SimpleCommand(command);
        return result;
    }

    /**
     * Return a ParameterizedCommand of the type given in parameter. The
     * parameter of the command has to be set before send.
     * 
     * @param command
     * @return
     */
    public static ParameterizedCommand getIpControlCommand(ParameterizedCommandType command) {
        ParameterizedCommand result = new ParameterizedCommand(command);
        return result;
    }

    /**
     * Return a ParameterizedCommand of the type given in parameter. The
     * parameter of the command is set with the given paramter value.
     * 
     * @param command
     * @param parameter
     * @return
     */
    public static ParameterizedCommand getIpControlCommand(ParameterizedCommandType command, String parameter) {
        ParameterizedCommand result = getIpControlCommand(command);
        result.setParameter(parameter);
        return result;
    }

    /**
     * Return a IpControlResponse object based on the given response data.
     * 
     * @param responseData
     * @return
     */
    public static Response getIpControlResponse(String responseData) {
        return new Response(responseData);
    }

}
