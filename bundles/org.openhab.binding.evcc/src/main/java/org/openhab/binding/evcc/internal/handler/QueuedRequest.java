/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal.handler;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;

/**
 * The {@link QueuedRequest} wraps a Jetty {@link Request} together with its success and error handlers.
 * Used by the evcc request queue to process API calls in a controlled manner.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public record QueuedRequest(Request request, Consumer<ContentResponse> onSuccess, Consumer<Exception> onError) {
}
