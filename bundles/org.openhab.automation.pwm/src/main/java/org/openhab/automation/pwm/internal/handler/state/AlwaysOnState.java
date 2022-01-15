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
package org.openhab.automation.pwm.internal.handler.state;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Active when, the duty cycle is 100% for at least a whole period.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class AlwaysOnState extends State {
    public AlwaysOnState(StateMachine context) {
        super(context);

        controlOutput(true);
    }

    @Override
    public void dutyCycleChanged() {
        nextState(OffState::new);
    }

    @Override
    protected void dutyCycleUpdated() {
        // nothing
    }

    @Override
    public void dispose() {
        // nothing
    }
}
