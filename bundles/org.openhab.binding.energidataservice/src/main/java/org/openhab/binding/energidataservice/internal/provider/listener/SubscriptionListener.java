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
package org.openhab.binding.energidataservice.internal.provider.listener;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link SubscriptionListener} provides a generic interface for receiving data
 * from different providers.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public interface SubscriptionListener {
    /**
     * Properties (such as lastCall and nextCall) have been updated.
     *
     * @param properties
     */
    void onPropertiesUpdated(Map<String, String> properties);

    /**
     * A communication error has occurred when calling the service.
     *
     * @param description Error description
     */
    void onCommunicationError(@Nullable String description);
}
