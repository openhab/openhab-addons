/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link AutomationPayloadTO} encapsulates the payload section of n automation
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AutomationPayloadTO {
    public String customerId;
    public String utterance;
    public String locale;
    public String marketplaceId;

    @Override
    public @NonNull String toString() {
        return "AutomationPayloadTO{customerId='" + customerId + "', utterance='" + utterance + "', locale='" + locale
                + "', marketplaceId='" + marketplaceId + "'}";
    }
}
