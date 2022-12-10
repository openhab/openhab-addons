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
package org.openhab.binding.hyperion.internal.protocol.ng;

import org.openhab.binding.hyperion.internal.protocol.HyperionCommand;

/**
 * The {@link AdjustmentCommand} is a POJO for sending an Adjustment command
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
