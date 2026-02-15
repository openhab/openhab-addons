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
package org.openhab.binding.restify.internal;

import static com.networknt.schema.SpecificationVersion.DRAFT_2020_12;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.schema.InputFormat;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.regex.JoniRegularExpressionFactory;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = JsonSchemaValidator.class)
public class JsonSchemaValidator implements Serializable {
    private final Logger logger = LoggerFactory.getLogger(JsonSchemaValidator.class);
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String BASE_ID = "https://www.openhab.org/addons/RESTify/";
    private static final String SCHEMA_DIR = "schema/";

    private static final String ENDPOINT_SCHEMA = "endpoint.schema.json";

    private final Map<String, String> schemaTextCache = new ConcurrentHashMap<>();

    public List<com.networknt.schema.Error> validateEndpointConfig(String config) {
        return validate(config, ENDPOINT_SCHEMA);
    }

    private List<com.networknt.schema.Error> validate(String config, String schemaFileName) {
        logger.debug("Validating config using {}", schemaFileName);

        var schemaRegistryConfig = SchemaRegistryConfig.builder()
                .regularExpressionFactory(JoniRegularExpressionFactory.getInstance()).build();

        var schemaRegistry = SchemaRegistry.withDefaultDialect(DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(schemaRegistryConfig)
                        // IMPORTANT: provide schema contents for absolute $id IRIs
                        .schemas(this::loadSchemaByAbsoluteId));

        var schema = schemaRegistry.getSchema(SchemaLocation.of(BASE_ID + schemaFileName));

        return schema.validate(config, InputFormat.JSON,
                ctx -> ctx.executionConfig(ec -> ec.formatAssertionsEnabled(true)));
    }

    /**
     * Called by json-schema-validator for absolute schema IDs (e.g. https://.../endpoint.schema.json)
     * Must return the schema JSON as a String or null if not handled.
     */
    @Nullable
    private String loadSchemaByAbsoluteId(String absoluteId) {
        if (!absoluteId.startsWith(BASE_ID)) {
            return null; // not ours
        }

        return schemaTextCache.computeIfAbsent(absoluteId, id -> {
            String fileName = id.substring(BASE_ID.length()); // e.g. "endpoint.schema.json"
            String bundlePath = SCHEMA_DIR + fileName; // e.g. "schema/endpoint.schema.json"

            URL url = FrameworkUtil.getBundle(getClass()).getEntry(bundlePath);
            if (url == null) {
                throw new IllegalStateException("Schema not found in bundle: " + bundlePath + " (for $id=" + id + ")");
            }

            try (InputStream in = url.openStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(
                        "Cannot read schema from bundle: " + bundlePath + " (for $id=" + id + ")", e);
            }
        });
    }
}
