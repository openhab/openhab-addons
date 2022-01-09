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
package org.openhab.binding.mielecloud.internal.webservice.sse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;

/**
 * Factory that produces configured {@link Request} instances for usage with SSE.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface SseRequestFactory {
    /**
     * Produces a {@link Request} which is decorated with all required headers.
     *
     * @param endpoint The endpoint to connect to.
     * @return The created {@link Request} or {@code null} if no request can be created due to lacking request
     *         information. If this method returns {@code null} then all connection attempts will be cancelled.
     */
    @Nullable
    Request createSseRequest(String endpoint);
}
