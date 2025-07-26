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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.homeassistant.internal.AbstractHomeAssistantTests;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractComponentConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.Device;

/**
 * @author Jochen Klein - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class HAConfigurationTests extends AbstractHomeAssistantTests {

    private static String readTestJson(final String name) {
        StringBuilder result = new StringBuilder();

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(HAConfigurationTests.class.getResourceAsStream(name), "UTF-8"))) {
            String line;

            while ((line = in.readLine()) != null) {
                result.append(line).append('\n');
            }
            return result.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void testAbbreviations() {
        String json = readTestJson("configA.json");

        Switch.Configuration config = AbstractComponentConfiguration.create(PYTHON, "switch", json,
                Switch.Configuration.class);

        assertThat(config.getName(), is("A"));
        assertThat(config.getQos(), is(1));
        assertThat(config.isRetain(), is(true));
        assertThat(config.getValueTemplate().toString(), is("Template<template=(B) renders=0>"));
        assertThat(config.getUniqueId(), is("C"));
        assertThat(config.getAvailabilityTopic(), is("D/E"));
        assertThat(config.getPayloadAvailable(), is("F"));
        assertThat(config.getPayloadNotAvailable(), is("G"));

        assertThat(config.getDevice(), is(notNullValue()));

        Device device = config.getDevice();
        if (device != null) {
            assertThat(device.getIdentifiers(), contains("H"));
            assertThat(device.getName(), is("J"));
            assertThat(device.getModel(), is("K"));
            assertThat(device.getSwVersion(), is("L"));
            assertThat(device.getManufacturer(), is("M"));
        }
    }

    @Test
    public void testTildeSubstritution() {
        String json = readTestJson("configB.json");

        Switch.Configuration config = AbstractComponentConfiguration.create(PYTHON, "switch", json,
                Switch.Configuration.class);

        assertThat(config.getAvailabilityTopic(), is("D/E"));
        assertThat(config.getStateTopic(), is("O/D/"));
        assertThat(config.getCommandTopic(), is("P~Q"));
        assertThat(config.getDevice(), is(notNullValue()));

        Device device = config.getDevice();
        if (device != null) {
            assertThat(device.getIdentifiers(), contains("H"));
        }
    }

    @Test
    public void testSampleFanConfig() {
        String json = readTestJson("configFan.json");

        Fan.Configuration config = AbstractComponentConfiguration.create(PYTHON, "fan", json, Fan.Configuration.class);
        assertThat(config.getName(), is("Bedroom Fan"));
    }

    @Test
    public void testDeviceListConfig() {
        String json = readTestJson("configDeviceList.json");

        Fan.Configuration config = AbstractComponentConfiguration.create(PYTHON, "fan", json, Fan.Configuration.class);
        assertThat(config.getDevice(), is(notNullValue()));

        Device device = config.getDevice();
        if (device != null) {
            assertThat(device.getIdentifiers(), is(Arrays.asList("A", "B", "C")));
        }
    }

    @Test
    public void testDeviceSingleStringConfig() {
        String json = readTestJson("configDeviceSingleString.json");

        Fan.Configuration config = AbstractComponentConfiguration.create(PYTHON, "fan", json, Fan.Configuration.class);
        assertThat(config.getDevice(), is(notNullValue()));

        Device device = config.getDevice();
        if (device != null) {
            assertThat(device.getIdentifiers(), is(Arrays.asList("A")));
        }
    }

    @Test
    public void testTS0601ClimateConfig() {
        String json = readTestJson("configTS0601ClimateThermostat.json");
        Climate.Configuration config = AbstractComponentConfiguration.create(PYTHON, "climate", json,
                Climate.Configuration.class);
        assertThat(config.getDevice(), is(notNullValue()));
        assertThat(config.getDevice().getIdentifiers(), is(notNullValue()));
        assertThat(config.getDevice().getIdentifiers().get(0), is("zigbee2mqtt_0x847127fffe11dd6a"));
        assertThat(config.getDevice().getManufacturer(), is("TuYa"));
        assertThat(config.getDevice().getModel(), is("Radiator valve with thermostat (TS0601_thermostat)"));
        assertThat(config.getDevice().getName(), is("th1"));
        assertThat(config.getDevice().getSwVersion(), is("Zigbee2MQTT 1.18.2"));

        assertThat(config.getActionTemplate().toString(), is(
                "Template<template=({% set values = {'idle':'off','heat':'heating','cool':'cooling','fan only':'fan'} %}{{ values[value_json.running_state] }}) renders=0>"));
        assertThat(config.getActionTopic(), is("zigbee2mqtt/th1"));
        assertThat(config.getCurrentTemperatureTemplate().toString(),
                is("Template<template=({{ value_json.local_temperature }}) renders=0>"));
        assertThat(config.getCurrentTemperatureTopic(), is("zigbee2mqtt/th1"));
        assertThat(config.getJsonAttributesTopic(), is("zigbee2mqtt/th1"));
        assertThat(config.getMaxTemp(), is(35d));
        assertThat(config.getMinTemp(), is(5d));
        assertThat(config.getModeCommandTopic(), is("zigbee2mqtt/th1/set/system_mode"));
        assertThat(config.getModeStateTemplate().toString(),
                is("Template<template=({{ value_json.system_mode }}) renders=0>"));
        assertThat(config.getModeStateTopic(), is("zigbee2mqtt/th1"));
        assertThat(config.getModes(), is(List.of("heat", "auto", "off")));
        assertThat(config.getName(), is("th1"));
        assertThat(config.getTempStep(), is(0.5d));
        assertThat(config.getTemperatureCommandTopic(), is("zigbee2mqtt/th1/set/current_heating_setpoint"));
        assertThat(config.getTemperatureStateTemplate().toString(),
                is("Template<template=({{ value_json.current_heating_setpoint }}) renders=0>"));
        assertThat(config.getTemperatureStateTopic(), is("zigbee2mqtt/th1"));
        assertThat(AbstractComponent.TemperatureUnit.fromString(Objects.requireNonNull(config.getTemperatureUnit())),
                is(AbstractComponent.TemperatureUnit.CELSIUS));
        assertThat(config.getUniqueId(), is("0x847127fffe11dd6a_climate_zigbee2mqtt"));
    }

    @Test
    public void testClimateConfig() {
        String json = readTestJson("configClimate.json");
        Climate.Configuration config = AbstractComponentConfiguration.create(PYTHON, "climate", json,
                Climate.Configuration.class);
        assertThat(config.getActionTemplate().toString(), is("Template<template=(a) renders=0>"));
        assertThat(config.getActionTopic(), is("b"));
        assertThat(config.getCurrentTemperatureTemplate().toString(), is("Template<template=(i) renders=0>"));
        assertThat(config.getCurrentTemperatureTopic(), is("j"));
        assertThat(config.getFanModeCommandTemplate().toString(), is("Template<template=(k) renders=0>"));
        assertThat(config.getFanModeCommandTopic(), is("l"));
        assertThat(config.getFanModeStateTemplate().toString(), is("Template<template=(m) renders=0>"));
        assertThat(config.getFanModeStateTopic(), is("n"));
        assertThat(config.getFanModes(), is(List.of("p1", "p2")));
        assertThat(config.getJsonAttributesTemplate().toString(), is("Template<template=(v) renders=0>"));
        assertThat(config.getJsonAttributesTopic(), is("w"));
        assertThat(config.getModeCommandTemplate().toString(), is("Template<template=(x) renders=0>"));
        assertThat(config.getModeCommandTopic(), is("y"));
        assertThat(config.getModeStateTemplate().toString(), is("Template<template=(z) renders=0>"));
        assertThat(config.getModeStateTopic(), is("A"));
        assertThat(config.getModes(), is(List.of("B1", "B2")));
        assertThat(config.getSwingModeCommandTemplate().toString(), is("Template<template=(C) renders=0>"));
        assertThat(config.getSwingModeCommandTopic(), is("D"));
        assertThat(config.getSwingModeStateTemplate().toString(), is("Template<template=(E) renders=0>"));
        assertThat(config.getSwingModeStateTopic(), is("F"));
        assertThat(config.getSwingModes(), is(List.of("G1")));
        assertThat(config.getTemperatureCommandTemplate().toString(), is("Template<template=(H) renders=0>"));
        assertThat(config.getTemperatureCommandTopic(), is("I"));
        assertThat(config.getTemperatureStateTemplate().toString(), is("Template<template=(J) renders=0>"));
        assertThat(config.getTemperatureStateTopic(), is("K"));
        assertThat(config.getTemperatureHighCommandTemplate().toString(), is("Template<template=(L) renders=0>"));
        assertThat(config.getTemperatureHighCommandTopic(), is("N"));
        assertThat(config.getTemperatureHighStateTemplate().toString(), is("Template<template=(O) renders=0>"));
        assertThat(config.getTemperatureHighStateTopic(), is("P"));
        assertThat(config.getTemperatureLowCommandTemplate().toString(), is("Template<template=(Q) renders=0>"));
        assertThat(config.getTemperatureLowCommandTopic(), is("R"));
        assertThat(config.getTemperatureLowStateTemplate().toString(), is("Template<template=(S) renders=0>"));
        assertThat(config.getTemperatureLowStateTopic(), is("T"));
        assertThat(config.getPowerCommandTopic(), is("U"));
        assertThat(config.getTempInitial(), is(10d));
        assertThat(config.getMaxTemp(), is(40d));
        assertThat(config.getMinTemp(), is(0d));
        assertThat(AbstractComponent.TemperatureUnit.fromString(Objects.requireNonNull(config.getTemperatureUnit())),
                is(AbstractComponent.TemperatureUnit.FAHRENHEIT));
        assertThat(config.getTempStep(), is(1d));
        assertThat(config.getPrecision(), is(0.5d));
    }
}
