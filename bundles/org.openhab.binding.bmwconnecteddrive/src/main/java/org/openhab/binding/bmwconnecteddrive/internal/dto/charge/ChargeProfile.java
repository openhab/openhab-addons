/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.charge;

/**
 * The {@link ChargeProfile} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - contributor
 */
public class ChargeProfile implements Cloneable {
    public WeeklyPlanner weeklyPlanner;

    public static ChargeProfile defaultChargeProfile() {
        final ChargeProfile cp = new ChargeProfile();
        return cp.completeChargeProfile();
    }

    public ChargeProfile completeChargeProfile() {
        if (weeklyPlanner == null) {
            weeklyPlanner = new WeeklyPlanner();
        }
        weeklyPlanner.completeWeeklyPlanner();
        return this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final ChargeProfile cp = (ChargeProfile) super.clone();
        cp.weeklyPlanner = (WeeklyPlanner) weeklyPlanner.clone();
        return cp;
    }
}
