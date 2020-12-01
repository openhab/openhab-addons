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
package org.openhab.binding.sony.internal.transports;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class will filter all content-type headers to remove any double-quotes or quotes from the content-type. Sony
 * devices weren't exactly written to spec and cannot handle either.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyContentTypeFilter implements ClientResponseFilter {
    @Override
    public void filter(final @Nullable ClientRequestContext requestCtx,
            final @Nullable ClientResponseContext responseCtx) throws IOException {
        Objects.requireNonNull(responseCtx, "responseCtx cannot be null");

        final List<String> contentValues = responseCtx.getHeaders().remove("content-type");
        if (contentValues != null) {
            final List<String> newValues = contentValues.stream().map(e -> e.replaceAll("[\"\']", ""))
                    .collect(Collectors.toList());
            responseCtx.getHeaders().put("content", newValues);
        }
    }
}
