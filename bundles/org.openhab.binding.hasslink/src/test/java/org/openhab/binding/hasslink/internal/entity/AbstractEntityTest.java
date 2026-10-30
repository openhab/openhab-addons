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
package org.openhab.binding.hasslink.internal.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.CompressedEntityState;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.HomeAssistantConfig;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Base class for entity fixture unit tests providing common JSON loading and assertion helpers.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
abstract class AbstractEntityTest {

    protected static final String COMPRESSED_FIXTURE_ROOT = "/org/openhab/binding/hasslink/internal/fixtures/compressed_fixtures/";

    /**
     * Standard list of dummy/unsupported attributes to verify default null returns.
     */
    protected static final List<String> STANDARD_UNSUPPORTED_ATTRIBUTES = List.of("options", "hvac_modes",
            "preset_modes", "fan_modes", "color_modes", "min", "max", "step", "unknown_attribute");

    protected record Fixture(String entityId, String fileName) {
        public Fixture(String entityId) {
            this(entityId, entityId + ".json");
        }

        public EntityState load(AbstractEntityTest test) {
            return test.loadFixtureState(COMPRESSED_FIXTURE_ROOT + fileName, entityId);
        }

        @Override
        public String toString() {
            return entityId;
        }
    }

    protected static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    /**
     * Loads an {@link EntityState} from a compressed JSON fixture file on the classpath.
     */
    protected EntityState loadFixtureState(String fixturePath, String entityId) {
        InputStream stream = getClass().getResourceAsStream(fixturePath);
        if (stream == null) {
            throw new IllegalArgumentException(
                    "Fixture file not found on classpath: " + fixturePath + " (entityId: " + entityId + ")");
        }

        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject event = root.getAsJsonObject("event");
            JsonObject attributesMap = event != null ? event.getAsJsonObject("a") : null;
            JsonElement entityJson = attributesMap != null ? attributesMap.get(entityId) : null;

            if (entityJson == null || !entityJson.isJsonObject()) {
                throw new IllegalArgumentException("Entity ID '" + entityId + "' not found in fixture: " + fixturePath);
            }

            CompressedEntityState compressed = Objects.requireNonNull(
                    GSON.fromJson(entityJson, CompressedEntityState.class),
                    "Missing compressed entity state in fixture");
            return new EntityState(entityId, Objects.requireNonNull(compressed.state, "Missing state in fixture"),
                    Objects.requireNonNull(compressed.attributes, "Missing attributes in fixture"));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read fixture: " + fixturePath, e);
        }
    }

    /**
     * Loads a {@link HomeAssistantConfig} from the config JSON fixture file on the classpath.
     */
    protected HomeAssistantConfig loadConfigFixture() {
        String fixturePath = "/org/openhab/binding/hasslink/internal/fixtures/ha_fixtures/get_config.json";
        InputStream stream = getClass().getResourceAsStream(fixturePath);
        if (stream == null) {
            throw new IllegalArgumentException("HA Config fixture file not found on classpath: " + fixturePath);
        }

        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject resultObj = json.has("result") ? json.getAsJsonObject("result") : json;
            return GSON.fromJson(resultObj, HomeAssistantConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read fixture: " + fixturePath, e);
        }
    }

    /**
     * Loads raw {@link JsonObject} config from the config JSON fixture file on the classpath.
     */
    protected JsonObject loadRawConfigFixture() {
        String fixturePath = "/org/openhab/binding/hasslink/internal/fixtures/ha_fixtures/get_config.json";
        InputStream stream = getClass().getResourceAsStream(fixturePath);
        if (stream == null) {
            throw new IllegalArgumentException("HA Config fixture file not found on classpath: " + fixturePath);
        }

        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.has("result") ? json.getAsJsonObject("result") : json;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read fixture: " + fixturePath, e);
        }
    }

    /**
     * Creates a mock {@link HassLinkBridgeHandler} that resolves the given entity handler and default config.
     */
    protected HassLinkBridgeHandler mockBridgeForEntity(EntityType entity) {
        HassLinkBridgeHandler bridge = mock(HassLinkBridgeHandler.class);
        when(bridge.getEntityType(any(EntityState.class))).thenReturn(entity);
        when(bridge.getHaConfig()).thenReturn(loadConfigFixture());
        when(bridge.getRawHaConfig()).thenReturn(loadRawConfigFixture());
        return bridge;
    }

    /**
     * Asserts that a {@link ServiceCall} matches expected parameters.
     */
    protected void assertService(ServiceCall call, String domain, String service, String entityId,
            @Nullable Map<String, Object> data) {
        assertThat(call.domain(), is(equalTo(domain)));
        assertThat(call.service(), is(equalTo(service)));
        assertThat(call.entityId(), is(equalTo(entityId)));
        if (data == null) {
            assertThat(call.serviceData(), is(nullValue()));
        } else {
            assertThat(call.serviceData(), is(equalTo(data)));
        }
    }

    /**
     * Asserts that getCommandOptions returns null for the specified attributes.
     */
    protected void assertNoCommandOptions(EntityType entity, EntityContext context, EntityState state,
            String... attributes) {
        for (String attribute : attributes) {
            assertThat("Expected getCommandOptions to be null for attribute: " + attribute,
                    entity.getCommandOptions(state, attribute, context), is(nullValue()));
        }
    }

    /**
     * Asserts that getStateDescriptionFragment returns null for the specified attributes.
     */
    protected void assertNoStateDescriptionFragments(EntityType entity, EntityContext context, EntityState state,
            String... attributes) {
        for (String attribute : attributes) {
            assertThat("Expected getStateDescriptionFragment to be null for attribute: " + attribute,
                    entity.getStateDescriptionFragment(state, attribute, context), is(nullValue()));
        }
    }

    /**
     * Asserts that both getStateDescriptionFragment and getCommandOptions return null for a standard suite
     * of unsupported attributes.
     */
    protected void assertRejectsUnsupportedAttributes(EntityType entity, EntityContext context, EntityState state) {
        assertNoStateDescriptionFragments(entity, context, state,
                STANDARD_UNSUPPORTED_ATTRIBUTES.toArray(String[]::new));
        assertNoCommandOptions(entity, context, state, STANDARD_UNSUPPORTED_ATTRIBUTES.toArray(String[]::new));
    }
}
