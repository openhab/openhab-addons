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

/**
 * The {@link ComponentState} is a POJO for a component state in the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class ComponentState {

    private String component;
    private boolean state;
    @com.google.gson.annotations.SerializedName("instance")
    private Object instance = null;

    public void setComponent(String component) {
        this.component = component;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getComponent() {
        return component;
    }

    public boolean getState() {
        return state;
    }

    @SuppressWarnings("unchecked")
    public java.util.List<Integer> getInstanceList() {
        if (instance instanceof java.util.List) {
            return (java.util.List<Integer>) instance;
        }
        return null;
    }

    public Integer getInstance() {
        if (instance instanceof Number instanceNumber) {
            return instanceNumber.intValue();
        }
        return null;
    }

    public void setInstance(java.util.List<Integer> instanceList) {
        this.instance = instanceList;
    }

    public void setInstance(Integer instanceValue) {
        this.instance = instanceValue;
    }
}
