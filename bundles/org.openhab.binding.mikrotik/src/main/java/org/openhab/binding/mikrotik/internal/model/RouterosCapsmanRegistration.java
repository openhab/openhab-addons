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
package org.openhab.binding.mikrotik.internal.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RouterosCapsmanRegistration} is a model class for WiFi client data retrieced from CAPsMAN controller
 * in RouterOS. Is a subclass of {@link RouterosRegistrationBase}.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosCapsmanRegistration extends RouterosRegistrationBase {
    public RouterosCapsmanRegistration(Map<String, String> props) {
        super(props);
    }

    public @Nullable String getIdentity() {
        return getProp("eap-identity");
    }

    public @Nullable Integer getRxSignal() {
        return getIntProp("rx-signal");
    }
}
