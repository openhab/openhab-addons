/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity.action;

import org.openhab.binding.innogysmarthome.internal.client.entity.Constant;

import com.google.api.client.util.Key;

/**
 * Defines the {@link ActionParameter} data structure needed to pass parameters within an {@link Action} to the innogy
 * SmartHome backend.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class ActionParameter {

    @Key("name")
    private String name;

    @Key("type")
    private String type;

    @Key("Constant")
    private Constant constant;

    public ActionParameter(String name, String type, Constant constant) {
        this.name = name;
        this.type = type;
        this.constant = constant;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the constant
     */
    public Constant getConstant() {
        return constant;
    }

    /**
     * @param constant the constant to set
     */
    public void setConstant(Constant constant) {
        this.constant = constant;
    }

}
