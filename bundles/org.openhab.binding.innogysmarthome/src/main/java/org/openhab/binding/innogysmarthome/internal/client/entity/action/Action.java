/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client.entity.action;

import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;

/**
 * Implements the Action structure needed to send JSON actions to the innogy backend. They are used to e.g. switch the
 * state of a device.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Action {

    public static final String ACTION_TYPE_SETSTATE = "SetState";
    private static final String NAMESPACE_CORE_RWE = "core.RWE";

    /**
     * Specifies the type of the action.
     */
    private String type;

    /**
     * Link to the entity supposed to execute the action.
     */
    private String target;

    /**
     * The product (context) that should handle (execute) the action. Defaults to {@link Action#NAMESPACE_CORE_RWE}.
     */
    private String namespace = NAMESPACE_CORE_RWE;

    /**
     * Dictionary of functions required for the intended execution of the action.
     */
    private ActionParams params;

    /**
     * Default constructor, used by serialization.
     */
    public Action() {
        // used by serialization
    }

    /**
     * Sets the type of the action. Usual action type is {@link Action#ACTION_TYPE_SETSTATE}.
     *
     * @param type
     */
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
     * @return the link to the target capability
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target the link to the target capability to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Sets the link target to the given capability id.
     *
     * @param capabilityId String with the 32 character long id
     */
    public void setTargetCapabilityById(String capabilityId) {
        setTarget(Link.LINK_TYPE_CAPABILITY + capabilityId);
    }

    /**
     * @return the params
     */
    public ActionParams getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(ActionParams params) {
        this.params = params;
    }
}
