/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity.action;

import java.util.List;

import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;

import com.google.api.client.util.Key;
import com.google.gson.annotations.SerializedName;

/**
 * Implements the Action structure needed to send JSON actions to the innogy backend. They are used to e.g. switch the
 * state of a device.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Action {

    public static final String ACTION_TYPE_SETSTATE = "device/SHC.RWE/1.0/action/SetState";

    @Key("type")
    private String type;

    @Key("Link")
    @SerializedName("Link")
    private Link capabilityLink;

    @Key("Data")
    @SerializedName("Data")
    private List<ActionParameter> parameterList;

    public Action() {
        // used by serialization
    }

    public Action(String type) {
        this.type = type;
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
     * @return the capabilityLink
     */
    public Link getCapabilityLink() {
        return capabilityLink;
    }

    /**
     * @param capabilityLink the capabilityLink to set
     */
    public void setCapabilityLink(Link capabilityLink) {
        this.capabilityLink = capabilityLink;
    }

    /**
     * Sets the capability link to the given capability id.
     *
     * @param capabilityId String with the 32 character long id
     */
    public void setCapabilityLink(String capabilityId) {
        setCapabilityLink(new Link("/capability/" + capabilityId));
    }

    /**
     * @return the parameterList
     */
    public List<ActionParameter> getParameterList() {
        return parameterList;
    }

    /**
     * @param parameterList the parameterList to set
     */
    public void setParameterList(List<ActionParameter> parameterList) {
        this.parameterList = parameterList;
    }

}
