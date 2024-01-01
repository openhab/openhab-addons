/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * Active when, the duty cycle is 0% for at least a whole period.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class AlwaysOffState extends State {
    public AlwaysOffState(StateMachine context) {
        super(context);

        controlOutput(false);
    }

    @Override
    public void dutyCycleChanged() {
        if (Math.round(context.getDutycycle()) >= 100) {
            nextState(DutycycleHundredState::new);
        } else {
            nextState(OnState::new);
        }
    }

    @Override
    protected void dutyCycleUpdated() {
        // in case we came here by the dead-man switch
        if (Math.round(context.getDutycycle()) > 0) {
            nextState(OnState::new);
        }
    }

    @Override
    public void dispose() {
        // nothing
    }
}
