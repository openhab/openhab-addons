/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
