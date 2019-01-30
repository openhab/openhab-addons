/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.handlers;

import org.eclipse.smarthome.core.types.State;

/**
 * The {@link OpenClosedType} Used to present Opened/Closed state of zones.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public enum OpenClosedType implements State {

    OPEN,
    CLOSED;

    public static OpenClosedType from(boolean arg) {
        return arg ? OPEN : CLOSED;
    }

    @Override
    public String format(String pattern) {
        return String.format(pattern, this.toFullString());
    }

    @Override
    public String toFullString() {
        return super.toString();
    }

}
