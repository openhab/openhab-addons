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
package org.openhab.binding.fox.internal.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FoxMessage} is an abstract class of Fox message (to and from system).
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
abstract class FoxMessage {

    protected String devToken = "";
    protected String appToken = "";
    protected String message = "";

    public FoxMessage() {
        reset();
    }

    void reset() {
        appToken = FoxDefinitions.appToken;
        setDeviceAll();
        message = "";
    }

    void setDeviceAll() {
        devToken = "all";
    }

    int getDevice() {
        if (devToken.startsWith("x") && devToken.length() == 3) {
            return Integer.parseInt(devToken.substring(1), 16);
        }
        return -1;
    }

    protected abstract void prepareMessage();

    protected abstract void interpretMessage();

    String prepare() {
        prepareMessage();
        return String.format("@%s:%s %s", devToken, appToken, message);
    }

    void interpret(String data) {
        reset();
        if (data.matches("@[^ ]+:[^ ]+ .+")) {
            appToken = data.substring(data.indexOf("@") + 1, data.indexOf(":"));
            devToken = data.substring(data.indexOf(":") + 1, data.indexOf(" "));
            message = data.substring(data.indexOf(" ") + 1);
        }
        interpretMessage();
    }
}
