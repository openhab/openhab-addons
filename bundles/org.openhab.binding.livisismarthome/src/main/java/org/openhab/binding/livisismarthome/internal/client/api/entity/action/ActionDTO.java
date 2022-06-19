/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.client.api.entity.action;

import org.openhab.binding.livisismarthome.internal.client.api.entity.link.LinkDTO;

/**
 * Implements the Action structure needed to send JSON actions to the LIVISI backend. They are used to e.g. switch the
 * state of a device.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class ActionDTO {

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
     * The product (context) that should handle (execute) the action. Defaults to {@link ActionDTO#NAMESPACE_CORE_RWE}.
     */
    private String namespace = NAMESPACE_CORE_RWE;

    /**
     * Dictionary of functions required for the intended execution of the action.
     */
    private ActionParamsDTO params;

    /**
     * Default constructor, used by serialization.
     */
    public ActionDTO() {
        // used by serialization
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
        setTarget(LinkDTO.LINK_TYPE_CAPABILITY + capabilityId);
    }

    /**
     * @return the params
     */
    public ActionParamsDTO getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(ActionParamsDTO params) {
        this.params = params;
    }
}
