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
package org.openhab.binding.jellyfin.internal.thirdparty.api;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.Module.SetupContext;
import com.fasterxml.jackson.databind.module.SimpleModule;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RFC3339JavaTimeModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    public RFC3339JavaTimeModule() {
        super("RFC3339JavaTimeModule");
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);

        addDeserializer(Instant.class, RFC3339InstantDeserializer.INSTANT);
        addDeserializer(OffsetDateTime.class, RFC3339InstantDeserializer.OFFSET_DATE_TIME);
        addDeserializer(ZonedDateTime.class, RFC3339InstantDeserializer.ZONED_DATE_TIME);
    }
}
