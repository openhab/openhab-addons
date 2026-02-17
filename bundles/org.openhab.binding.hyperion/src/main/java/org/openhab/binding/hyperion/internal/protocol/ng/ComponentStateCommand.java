/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hyperion.internal.protocol.ng;

import org.openhab.binding.hyperion.internal.protocol.HyperionCommand;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ComponentStateCommand} is a POJO for sending a Component State command
 * to the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class ComponentStateCommand extends HyperionCommand {

    private static final String NAME = "componentstate";

    @SerializedName("componentstate")
    private ComponentState componentState;
    @SerializedName("instance")
    private Object instance;

    public ComponentStateCommand(ComponentState componentState) {
        super(NAME);
        setComponentState(componentState);
    }

    public ComponentState getComponentState() {
        return componentState;
    }

    public void setComponentState(ComponentState componentState) {
        this.componentState = componentState;
    }

    public void setInstance(Integer instanceValue) {
        this.instance = instanceValue;
    }

    public void setInstance(java.util.List<Integer> instanceList) {
        this.instance = instanceList;
    }

    public java.util.List<Integer> getInstanceList() {
        if (instance instanceof java.util.List<?> list) {
            java.util.List<Integer> result = new java.util.ArrayList<>(list.size());
            for (Object element : list) {
                if (element instanceof Number number) {
                    result.add(number.intValue());
                } else {
                    return null;
                }
            }
            return result;
        }
        return null;
    }

    public Integer getInstance() {
        if (instance instanceof Number instanceNumber) {
            return instanceNumber.intValue();
        }
        return null;
    }
}
