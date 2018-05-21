/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

}
