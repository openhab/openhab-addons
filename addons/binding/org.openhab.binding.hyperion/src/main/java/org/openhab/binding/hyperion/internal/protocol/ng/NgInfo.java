/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.ng;

import java.util.List;

import org.openhab.binding.hyperion.internal.protocol.v1.Effect;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link NgInfo} is a POJO for information in the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class NgInfo {

    @SerializedName("components")
    private List<Component> components = null;

    @SerializedName("adjustment")
    private List<Adjustment> adjustment = null;

    @SerializedName("priorities")
    private List<Priority> priorities = null;

    @SerializedName("hyperion")
    private Hyperion hyperion = null;

    @SerializedName("effects")
    private List<Effect> effects = null;

    public List<Component> getComponents() {
        return components;
    }

    public List<Adjustment> getAdjustment() {
        return adjustment;
    }

    public List<Priority> getPriorities() {
        return priorities;
    }

    public Hyperion getHyperion() {
        return hyperion;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public void setAdjustment(List<Adjustment> adjustment) {
        this.adjustment = adjustment;
    }

    public void setPriorities(List<Priority> priorities) {
        this.priorities = priorities;
    }

    public void setHyperion(Hyperion hyperion) {
        this.hyperion = hyperion;
    }

    public List<Effect> getEffects() {
        return effects;
    }

}
