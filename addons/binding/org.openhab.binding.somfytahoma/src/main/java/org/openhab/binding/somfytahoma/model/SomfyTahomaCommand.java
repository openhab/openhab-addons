/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.model;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaCommand} holds information about a roller shutter
 * command and command's parameters.
 *
 * @author Ondrej Pecta - Initial contribution
 */
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

