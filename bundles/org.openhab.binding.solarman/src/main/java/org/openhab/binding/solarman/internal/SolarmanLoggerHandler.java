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
package org.openhab.binding.solarman.internal;

import static org.openhab.binding.solarman.internal.SolarmanBindingConstants.DYNAMIC_CHANNEL;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarman.internal.channel.BaseChannelConfig;
import org.openhab.binding.solarman.internal.channel.SolarmanChannelManager;
import org.openhab.binding.solarman.internal.defmodel.InverterDefinition;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.binding.solarman.internal.defmodel.Request;
import org.openhab.binding.solarman.internal.defmodel.Validation;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnector;
import org.openhab.binding.solarman.internal.modbus.SolarmanV5Protocol;
import org.openhab.binding.solarman.internal.state.LoggerState;
import org.openhab.binding.solarman.internal.updater.SolarmanChannelUpdater;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarmanLoggerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class SolarmanLoggerHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SolarmanLoggerHandler.class);

    private final DefinitionParser definitionParser;
    private final SolarmanChannelManager solarmanChannelManager;
    private final LoggerState loggerState;
    @Nullable
    private volatile ScheduledFuture<?> scheduledFuture;

    public SolarmanLoggerHandler(Thing thing) {
        super(thing);
        this.definitionParser = new DefinitionParser();
        this.solarmanChannelManager = new SolarmanChannelManager();
        this.loggerState = new LoggerState();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        SolarmanLoggerConfiguration config = getConfigAs(SolarmanLoggerConfiguration.class);
        SolarmanLoggerConnector solarmanLoggerConnector = new SolarmanLoggerConnector(config);

        List<Channel> staticChannels = thing.getChannels().stream()
                .filter(channel -> !channel.getProperties().containsKey(DYNAMIC_CHANNEL)).toList();

        InverterDefinition inverterDefinition = definitionParser.parseDefinition(config.inverterType);

        if (inverterDefinition == null) {
            logger.error("Unable to find a definition for the provided inverter type");
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to find a definition for the provided inverter type");
            return;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Found definition for {}", config.inverterType);
            }
        }
        SolarmanV5Protocol solarmanV5Protocol = new SolarmanV5Protocol(config);

        List<Request> mergedRequests = (StringUtils.isNotEmpty(config.getAdditionalRequests()))
                ? mergeRequests(inverterDefinition.getRequests(),
                        extractAdditionalRequests((@NonNull String) config.getAdditionalRequests()))
                : inverterDefinition.getRequests();

        Map<ParameterItem, ChannelUID> paramToChannelMapping = mergeMaps(
                extractChannelMappingFromChannels(staticChannels),
                setupChannelsForInverterDefinition(inverterDefinition));

        SolarmanChannelUpdater solarmanChannelUpdater = new SolarmanChannelUpdater(this::updateState);

        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            boolean fetchSuccessful = solarmanChannelUpdater.fetchDataFromLogger(mergedRequests,
                    solarmanLoggerConnector, solarmanV5Protocol, paramToChannelMapping, loggerState);

            if (fetchSuccessful) {
                updateStatus(ThingStatus.ONLINE);
                loggerState.setOnline();
            } else {
                updateStatus(ThingStatus.OFFLINE);
                loggerState.setPossiblyOffline();
            }

            if (loggerState.isJustBecameOffline()) {
                logger.info(
                        "Assuming logger is OFFLINE after {} failed requests. Disabling connection error logging until it becomes available again",
                        LoggerState.NO_FAILED_REQUESTS);
            }
        }, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private <K, V> Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2) {
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));
    }

    private Map<ParameterItem, ChannelUID> extractChannelMappingFromChannels(List<Channel> channels) {
        return channels.stream().map(channel -> {
            BaseChannelConfig bcc = channel.getConfiguration().as(BaseChannelConfig.class);
            return new AbstractMap.SimpleEntry<>(
                    new ParameterItem(channel.getLabel(), "N/A", "N/A", bcc.uom, bcc.scale, bcc.rule,
                            parseRegisters(bcc.registers), "N/A", new Validation(), bcc.offset, Boolean.FALSE),
                    channel.getUID());
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<Integer> parseRegisters(String registers) {
        String[] tokens = registers.split(",");
        Pattern pattern = Pattern.compile("\\s*(0x[\\da-fA-F]+|[\\d]+)\\s*");
        return Stream.of(tokens).map(pattern::matcher).filter(Matcher::matches).map(matcher -> matcher.group(1))
                .map(SolarmanLoggerHandler::parseNumber).collect(Collectors.toList());
    }

    // TODO for now just concatenate the list, in the future, merge overlapping requests
    private List<Request> mergeRequests(List<Request> requestList1, List<Request> requestList2) {
        return Stream.concat(requestList1.stream(), requestList2.stream()).collect(Collectors.toList());
    }

    private List<Request> extractAdditionalRequests(String channels) {
        String[] tokens = channels.split(",");
        Pattern pattern = Pattern.compile(
                "\\s*(0x[\\da-fA-F]+|[\\d]+)\\s*:\\s*(0x[\\da-fA-F]+|[\\d]+)\\s*-\\s*(0x[\\da-fA-F]+|[\\d]+)\\s*");

        return Stream.of(tokens).map(pattern::matcher).filter(Matcher::matches).map(matcher -> {
            try {
                int functionCode = parseNumber(matcher.group(1));
                int start = parseNumber(matcher.group(2));
                int end = parseNumber(matcher.group(3));
                return new Request(functionCode, start, end);
            } catch (NumberFormatException e) {
                logger.error("Invalid number format in token: {} , ignoring additional requests", matcher.group(), e);
                return (@NonNull Request) null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static int parseNumber(String number) {
        return number.startsWith("0x") ? Integer.parseInt(number.substring(2), 16) : Integer.parseInt(number);
    }

    private Map<ParameterItem, ChannelUID> setupChannelsForInverterDefinition(InverterDefinition inverterDefinition) {
        ThingBuilder thingBuilder = editThing();

        List<Channel> oldDynamicChannels = thing.getChannels().stream()
                .filter(channel -> channel.getProperties().containsKey(DYNAMIC_CHANNEL)).toList();

        Map<ParameterItem, Channel> newDynamicItemChannelMap = solarmanChannelManager.generateItemChannelMap(thing,
                inverterDefinition);

        // Remove old dynamic channels
        thingBuilder.withoutChannels(oldDynamicChannels);

        // Add new dynamic channels
        newDynamicItemChannelMap.values().forEach(thingBuilder::withChannel);

        updateThing(thingBuilder.build());

        logger.debug("Updated thing with id {} and {} channels", thing.getThingTypeUID(), thing.getChannels().size());

        return newDynamicItemChannelMap.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getUID()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void dispose() {
        super.dispose();

        if (scheduledFuture != null)
            Objects.requireNonNull(scheduledFuture).cancel(false);
    }
}
