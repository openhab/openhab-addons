/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.squeezebox.internal.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Squeezebox Playlist data class.
 *
 * @author Laurent Arnal - Initial contribution
 */
public class Request {
    private int id;
    private String method;
    private List<Object> params;

    private transient List<String> subParams;

    public Request() {
        id = 1;
        method = "slim.request";
        params = new ArrayList<Object>();
        params.add("");

        subParams = new ArrayList<String>();
        params.add(subParams);
    }

    public void setPlayerId(String playerId) {
        params.remove(0);
        params.add(0, playerId);
    }

    public void addParams(String param) {
        subParams.add(param);
    }

    public static Request fromParams(String command, long start, long size, String... args) {
        Request req = new Request();
        String[] commands = command.split("\\s");
        for (String cmd : commands) {
            req.addParams(cmd);
        }
        req.addParams("" + start);
        req.addParams("" + size);
        for (String arg : args) {
            req.addParams(arg);
        }
        return req;
    }

}
