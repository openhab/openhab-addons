/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.sdm.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nest.internal.sdm.api.PubSubAPI;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubMessage;

/**
 * Interface for listeners of {@link PubSubAPI} subscription events.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public interface PubSubSubscriptionListener {

    void onError(Exception exception);

    void onMessage(PubSubMessage message);

    void onNoNewMessages();
}
