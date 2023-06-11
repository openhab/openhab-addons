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
package org.openhab.binding.hyperion.internal.protocol;

import org.openhab.binding.hyperion.internal.protocol.v1.Effect;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link EffectCommand} is a POJO for sending an effect command
 * to the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class EffectCommand extends HyperionCommand {

    private static final String NAME = "effect";

    @SerializedName("origin")
    private String origin;

    @SerializedName("effect")
    private Effect effect;

    @SerializedName("priority")
    private int priority;

    public EffectCommand(Effect effect, int priority) {
        super(NAME);
        setEffect(effect);
        setPriority(priority);
    }

    public Effect getEffect() {
        return effect;
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }
}
