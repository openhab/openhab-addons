/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.domintell.internal.protocol.message;

/**
* The {@link BaseMessage} class is a base class for parsed messages
*
* @author Gabor Bicskei - Initial contribution
*/
public class BaseMessage {
    public enum Type {
        SESSION_OPENED, AUTH_FAILED, ACCESS_DENIED, SESSION_TIMEOUT, SESSION_CLOSED, WORLD, PONG, END_APPINFO, START_APPINFO, APPINFO, DATA, SYSTEM_TIME
    }

    private Type type;
    private String message;

    BaseMessage(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
