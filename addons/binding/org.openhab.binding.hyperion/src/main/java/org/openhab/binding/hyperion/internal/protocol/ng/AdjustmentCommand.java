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

/**
 * The {@link AdjustmentCommand} is a POJO for sending a Adjustment command
 * to the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class AdjustmentCommand extends HyperionCommand {

    private static final String NAME = "adjustment";
    private Adjustment adjustment;

    public AdjustmentCommand(Adjustment adjustment) {
        super(NAME);
        setAdjustment(adjustment);
    }

    public Adjustment getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(Adjustment adjustment) {
        this.adjustment = adjustment;
    }

}
