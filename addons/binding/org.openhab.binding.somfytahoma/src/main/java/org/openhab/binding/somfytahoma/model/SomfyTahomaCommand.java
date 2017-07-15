/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
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

    String name;
    ArrayList<String> parameters = new ArrayList<String>();

    public SomfyTahomaCommand(String command) {
        this.name = command;
    }

    public String getName() {
        return name;
    }

    public void setName(String command) {
        this.name = command;
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public void addParameters(String param) {
        this.parameters.add(param);
    }
}

