/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.request;

import org.openhab.binding.hyperion.internal.protocol.effect.Effect;

public class EffectCommand extends HyperionCommand {

    private final static String NAME = "effect";
    private Effect effect;
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

}
