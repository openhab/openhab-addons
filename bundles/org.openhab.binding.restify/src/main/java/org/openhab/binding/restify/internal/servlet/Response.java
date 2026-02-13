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
package org.openhab.binding.restify.internal.servlet;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public sealed interface Response {
    public record StringResponse(String value) implements Response {
    }

    public record ItemResponse(String itemName, String expression) implements Response {
    }

    public record ThingResponse(String thingUid, String expression) implements Response {
    }

    public record JsonResponse(Map<String, ? extends Response> values) implements Response {
    }

    public record ArrayResponse(List<? extends Response> values) implements Response {
    }
}
