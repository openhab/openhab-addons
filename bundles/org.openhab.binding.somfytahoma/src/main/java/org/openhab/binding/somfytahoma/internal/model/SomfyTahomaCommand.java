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
package org.openhab.binding.somfytahoma.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaCommand} holds information about a roller shutter
 * command and command's parameters.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaCommand {

    int type;
    String name;
    ArrayList<Object> parameters = new ArrayList<Object>();

    public SomfyTahomaCommand(String command) {
        this.name = command;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Object> getParameters() {
        return parameters;
    }

    public ArrayList<Integer> getPercentParameters() {
        ArrayList<Integer> newList = new ArrayList<>(parameters.size());
        for( Object o : parameters) {
            Double val = (Double) o;
            newList.add(val.intValue());
        }
        return newList;
    }
}

