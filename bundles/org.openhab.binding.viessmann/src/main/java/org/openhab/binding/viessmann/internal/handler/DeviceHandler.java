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
package org.openhab.binding.viessmann.internal.handler;

import static org.openhab.binding.viessmann.internal.ViessmannBindingConstants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.viessmann.internal.ViessmannDynamicStateDescriptionProvider;
import org.openhab.binding.viessmann.internal.api.ViessmannCommunicationException;
import org.openhab.binding.viessmann.internal.config.ThingsConfig;
import org.openhab.binding.viessmann.internal.dto.ThingMessageDTO;
import org.openhab.binding.viessmann.internal.dto.ViessmannMessage;
import org.openhab.binding.viessmann.internal.dto.device.StoredChannelValues;
import org.openhab.binding.viessmann.internal.dto.features.FeatureCommand;
import org.openhab.binding.viessmann.internal.dto.features.FeatureCommandParams;
import org.openhab.binding.viessmann.internal.dto.features.FeatureDataDTO;
import org.openhab.binding.viessmann.internal.dto.features.FeatureProperties;
import org.openhab.binding.viessmann.internal.dto.schedule.DaySchedule;
import org.openhab.binding.viessmann.internal.dto.schedule.ScheduleDTO;
import org.openhab.binding.viessmann.internal.interfaces.BridgeInterface;
import org.openhab.binding.viessmann.internal.util.ViessmannUtil;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.items.ManagedItemProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link DeviceHandler} is responsible for handling DeviceHandler
 *
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
@Component(service = DeviceHandler.class)
public class DeviceHandler extends ViessmannThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private static final Set<String> BASE_ALLOWED_SUFFIXES = Set.of("active", "enabled");
    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT);

    private ThingsConfig config = new ThingsConfig();

    private final StoredChannelValues storedChannelValues = new StoredChannelValues();

    private final ItemChannelLinkRegistry linkRegistry;
    private final ManagedItemProvider managedItemProvider;

    private final TranslationProvider i18Provider;
    private final LocaleProvider localeProvider;

    private final Bundle bundle;

    public DeviceHandler(Thing thing, ViessmannDynamicStateDescriptionProvider stateDescriptionProvider,
            ItemChannelLinkRegistry linkRegistry, ManagedItemProvider managedItemProvider,
            TranslationProvider i18Provider, LocaleProvider localeProvider) {
        super(thing, stateDescriptionProvider);
        this.linkRegistry = linkRegistry;
        this.managedItemProvider = managedItemProvider;
        this.i18Provider = i18Provider;
        this.localeProvider = localeProvider;
        bundle = FrameworkUtil.getBundle(getClass());
    }

    @Override
    public void initialize() {
        ThingsConfig config = getConfigAs(ThingsConfig.class);
        this.config = config;
        if (config.deviceId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.device-id");
            return;
        }
        updateProperty(PROPERTY_ID, config.deviceId); // set representation property used by discovery

        int thingTypeVersion = getThingTypeVersion();
        if (thingTypeVersion < DEVICE_THING_TYPE_VERSION) {
            migrateChannelIds();
        }

        initDeviceState();
    }

    private void migrateChannelIds() {
        List<Channel> oldChannels = thing.getChannels();
        List<Channel> newChannels = new ArrayList<>(oldChannels.size());

        Map<ChannelUID, ChannelUID> renameMap = new LinkedHashMap<>();

        for (Channel channel : oldChannels) {
            String oldId = channel.getUID().getId();
            String newId = ViessmannUtil.camelToHyphen(oldId);
            boolean updateChannelType = false;
            String channelLabel = channel.getLabel();
            String oldChannelType = ViessmannUtil
                    .camelToHyphen(Objects.requireNonNull(channel.getChannelTypeUID()).toString());
            oldChannelType = oldChannelType.replace(BINDING_ID + ":", "");
            String newChannelType = oldChannelType;
            if ("type-energy".equals(oldChannelType) || "energy".equals(oldChannelType)) {
                logger.trace("Migrate channelType");
                if (newId.contains("gas")) {
                    newChannelType = "gas-energy";
                }
                if (newId.contains("power")) {
                    newChannelType = "power-energy";
                }
                updateChannelType = true;
            }

            if ("type-volume".equals(oldChannelType) || "volume".equals(oldChannelType)) {
                if (newId.contains("gas")) {
                    newChannelType = "gas-volume";
                    updateChannelType = true;
                }
            }

            if ("type-minute".equals(oldChannelType) || "minute".equals(oldChannelType)
                    || "hours".equals(oldChannelType) || "type-hours".equals(oldChannelType)) {
                newChannelType = "duration";
                updateChannelType = true;
            }

            if ("type-settemperature".equals(oldChannelType) || "settemperature".equals(oldChannelType)) {
                newChannelType = "set-temperature";
                updateChannelType = true;
            }

            if (oldChannelType.contains("type-")) {
                newChannelType = newChannelType.replace("type-", "");
                updateChannelType = true;
            }

            if ("slope".equals(oldId) && oldChannelType.contains("decimal")) {
                newChannelType = "slope";
                updateChannelType = true;
            }

            if ("shift".equals(oldId) && oldChannelType.contains("number")) {
                newChannelType = "shift";
                updateChannelType = true;
            }

            if (!newId.equals(oldId) || updateChannelType) {
                logger.info("Migrating channel '{}' -> '{}' | channel-type  '{}' -> '{}'", oldId, newId, oldChannelType,
                        newChannelType);

                ChannelUID oldUid = channel.getUID();
                ChannelUID newUid = new ChannelUID(thing.getUID(), newId);
                String channelDescription = channel.getDescription();
                if (channelDescription == null) {
                    channelDescription = "";
                }

                ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, newChannelType);
                if (channelLabel != null) {
                    Channel newChannel = ChannelBuilder.create(newUid, channel.getAcceptedItemType())
                            .withLabel(channelLabel).withDescription(channelDescription).withType(channelTypeUID)
                            .withProperties(channel.getProperties()).build();

                    newChannels.add(newChannel);
                    renameMap.put(oldUid, newUid);
                }
            }
            updateProperty(THING_TYPE_VERSION, Integer.toString(DEVICE_THING_TYPE_VERSION));
        }

        if (renameMap.isEmpty()) {
            return;
        }

        updateThing(editThing().withChannels(newChannels).build());

        for (Map.Entry<ChannelUID, ChannelUID> e : renameMap.entrySet()) {
            ChannelUID oldUid = e.getKey();
            ChannelUID newUid = e.getValue();

            Collection<ItemChannelLink> links = new ArrayList<>(linkRegistry.getLinks(oldUid));

            for (ItemChannelLink link : links) {
                String item = link.getItemName();
                if (isManagedItem(item)) {
                    try {
                        linkRegistry.remove(link.getUID());
                    } catch (Exception ex) {
                        logger.warn("Could not remove old link {} -> {}: {}", item, oldUid, ex.getMessage());
                    }

                    linkRegistry.add(new ItemChannelLink(item, newUid));
                    logger.info("Re-linked item '{}' from '{}' to '{}'", item, oldUid.getId(), newUid.getId());
                }
            }
        }
    }

    private @Nullable BridgeInterface getBridgeInterface() {
        if (getBridge() instanceof Bridge bridge) {
            if (bridge.getHandler() instanceof BridgeInterface bridgeInterface) {
                return bridgeInterface;
            }
        }
        return null;
    }

    @Override
    public void initChannelState() {
        BridgeInterface bridgeInterface = getBridgeInterface();
        if (bridgeInterface == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.bridge-type");
            return;
        }
        bridgeInterface.setConfigInstallationGatewayIdToDevice(this);
        bridgeInterface.updateFeaturesOfDevice(this);
    }

    public String getDeviceId() {
        return config.deviceId;
    }

    public void setConfigInstallationGatewayId(String installationId, String gatewaySerial) {
        this.updateProperty(INSTALLATION_ID, installationId);
        this.updateProperty(GATEWAY_SERIAL, gatewaySerial);
    }

    public String getInstallationId() {
        return Objects.requireNonNull(thing.getProperties().get(INSTALLATION_ID), "Installation ID is missing!");
    }

    public String getGatewaySerial() {
        return Objects.requireNonNull(thing.getProperties().get(GATEWAY_SERIAL), "Gateway serial is missing!");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }

        Channel ch = thing.getChannel(channelUID.getId());
        if (ch == null) {
            logger.warn("Invalid or missing ChannelUID: {}", channelUID);
            return;
        }

        Map<String, String> prop = ch.getProperties();
        String commands = prop.get("command");
        if (commands == null) {
            return;
        }

        String channelId = ch.getUID().getId();
        String[] parts = channelId.split("#", 2);
        channelId = parts[0];
        String suffix = parts.length > 1 ? parts[1] : "";

        String[] params = prop.getOrDefault("params", "{}").split(",");
        String[] com = commands.split(",");

        logger.trace("ChannelUID: {} | Properties: {} | Params {}", ch.getUID(), prop, params);

        if (command instanceof OnOffType onOff) {
            handleOnOff(channelUID, channelId, suffix, prop, params, com, onOff);
        } else if (command instanceof DecimalType dec) {
            handleNumeric(channelUID, channelId, suffix, prop, params, com, dec.doubleValue());
        } else if (command instanceof QuantityType<?> qty) {
            handleNumeric(channelUID, channelId, suffix, prop, params, com, qty.doubleValue());
        } else if (command instanceof StringType str) {
            handleString(channelUID, channelId, suffix, prop, params, com, str);
        }
    }

    private void handleOnOff(ChannelUID channelUID, String channelId, String suffix, Map<String, String> prop,
            String[] params, String[] com, OnOffType onOff) {
        logger.trace("Received OnOff Command for Channel {} value={}", channelUID, onOff);
        if (!checkCommandType(prop, "boolean") && !"boolean".equals(prop.get("channelType"))) {
            logger.warn("OnOffType Command not executable for Channel: {}", channelUID);
            return;
        }

        storedChannelValues.putProperty(channelUID.getId(), onOff.toString());

        boolean isOn = onOff == OnOffType.ON;
        String feature = prop.get("feature");

        UriParam up = resolveOnOffUri(prop, channelId, params, com, isOn);
        if (up == null) {
            logger.warn("Channel {} misconfigured (no URI)", channelUID);
            return;
        }
        sendChannelCommand(up.uri(), up.param(), feature != null && feature.contains("oneTimeCharge") ? 2 : 0,
                feature != null && feature.contains("oneTimeCharge"));
    }

    private void handleNumeric(ChannelUID channelUID, String channelId, String suffix, Map<String, String> prop,
            String[] params, String[] com, double value) {
        logger.trace("Received Numeric Command for Channel {} value={}", channelUID, value);
        if (!checkCommandType(prop, "number")) {
            logger.warn("Numeric Command not executable for Channel: {}", channelUID);
            return;
        }

        storedChannelValues.putProperty(channelUID.getId(), Double.toString(value));

        UriParam up = resolveCommand(prop, channelId, suffix, params, com, value);
        if (up == null) {
            logger.trace("No matching numeric command for Channel {}", channelUID);
            return;
        }
        sendChannelCommand(up.uri(), up.param(), 0, false);
    }

    private void handleString(ChannelUID channelUID, String channelId, String suffix, Map<String, String> prop,
            String[] params, String[] com, StringType value) {
        logger.trace("Received StringType Command for Channel {} value={}", channelUID, value);
        if (!checkCommandType(prop, "string")) {
            logger.warn("StringType Command not executable for Channel: {}", channelUID);
            return;
        }

        storedChannelValues.putProperty(channelUID.getId(), value.toString());

        UriParam up = resolveCommand(prop, channelId, suffix, params, com, value);

        if (up == null) {
            logger.trace("No matching StringType command for Channel {}", channelUID);
            return;
        }

        if (channelUID.getId().contains("holiday")) {
            logger.debug("Command cached for activation via active channel");
            String url = up.uri().replace("schedule", "unschedule");
            String param = "{}";
            sendChannelCommand(url, param, 0, false);
            updateState(channelId + "#active", OnOffType.OFF);
            return;
        }

        sendChannelCommand(up.uri(), up.param(), 0, false);
    }

    private @Nullable UriParam resolveCommand(Map<String, String> prop, String channelId, String suffix,
            String[] params, String[] com, Object commandOrValue) {
        final String valueStr;
        final @Nullable Double valueNum;

        if (commandOrValue instanceof StringType st) {
            valueStr = st.toString();
            valueNum = null;
        } else if (commandOrValue instanceof Number n) {
            valueStr = Double.toString(n.doubleValue());
            valueNum = n.doubleValue();
        } else {
            // unsupported
            return null;
        }

        final String lcSuffix = Objects.requireNonNull(ViessmannUtil.hyphenToCamel(suffix, false))
                .toLowerCase(Locale.ROOT);

        String uri = null;
        String paramsDef = null;
        String cmd = null;

        final JsonObject json = new JsonObject();

        for (String c : com) {
            cmd = c;
            paramsDef = prop.get(cmd + "Params");

            final boolean matchesSuffix = (paramsDef != null && paramsDef.toLowerCase(Locale.ROOT).contains(lcSuffix))
                    || cmd.toLowerCase(Locale.ROOT).contains(lcSuffix);

            final boolean matchesQuantity = (valueNum != null) && isQuantityCommand(cmd);

            if (matchesSuffix || matchesQuantity) {
                uri = prop.get(cmd + "Uri");

                if (valueNum != null && paramsDef != null
                        && paramsDef.matches(".*(Temperature|setHysteresis|setMin|setMax|temperature).*")) {
                    if (uri == null) {
                        return null;
                    }
                    json.addProperty(paramsDef, valueNum);
                    return new UriParam(uri, json.toString());
                }
                if (channelId.contains("hygiene-trigger")) {
                    if (valueStr.contains(",")) {
                        cmd = "triggerDaily";
                        uri = prop.get(cmd + "Uri");
                    } else {
                        String v = storedChannelValues.getProperty(channelId + "#weekday");
                        if (v == null || !v.contains(",")) {
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
        }

        if (uri == null || cmd == null) {
            return null;
        }

        String[] effectiveParams = params;
        if (channelId.contains("hygiene-trigger") && "triggerDaily".equals(cmd)) {
            effectiveParams = prop.getOrDefault("triggerDailyParams", "{}").split(",");
        } else if (channelId.contains("holiday") && "changeEndDate".equals(cmd)) {
            effectiveParams = prop.getOrDefault("changeEndDateParams", "{}").split(",");
        }

        for (String p : effectiveParams) {
            final String hyphenParam = ViessmannUtil.camelToHyphen(p);
            String v = storedChannelValues.getProperty(channelId + "#" + hyphenParam);
            final @Nullable String type = prop.get(p + "Type");

            final boolean overrideWithIncoming = hyphenParam.equals(suffix) // numeric
                    || p.equals(suffix) // string
                    || v == null;

            if (overrideWithIncoming) {
                if ("number".equals(type)) {
                    if (valueNum == null) {
                        json.addProperty(p, valueStr);
                    } else {
                        json.addProperty(p, valueNum);
                    }
                } else {
                    json.addProperty(p, valueStr);
                }
            } else {
                final String nn = Objects.requireNonNull(v);

                if ("number".equals(type)) {
                    try {
                        json.addProperty(p, Double.parseDouble(nn));
                    } catch (NumberFormatException e) {
                        logger.debug("Skip param {}: invalid cached numeric value '{}'", p, nn);
                        continue;
                    }
                } else {
                    json.addProperty(p, nn);
                }
            }

            if (valueNum != null && (cmd.toLowerCase(Locale.ROOT).contains(lcSuffix) || isQuantityCommand(cmd))) {
                break;
            }
        }

        return new UriParam(uri, json.toString());
    }

    private void sendChannelCommand(String uri, String param, int delaySeconds, boolean reloadFeatures) {
        BridgeInterface bridge = getBridgeInterface();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.bridge-type");
            return;
        }

        try {
            if (!bridge.setData(uri, param) || reloadFeatures) {
                scheduler.schedule(this::initChannelState, delaySeconds, TimeUnit.SECONDS);
            }
        } catch (ViessmannCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private @Nullable UriParam resolveOnOffUri(Map<String, String> prop, String channelId, String[] params,
            String[] com, boolean isOn) {
        // setActive / setEnabled (highest priority)
        for (String base : List.of("setActive", "setEnabled")) {
            if (prop.containsKey(base + "Uri")) {
                String key = require(prop, base + "Params");
                return new UriParam(require(prop, base + "Uri"), "{\"" + key + "\":" + isOn + "}");
            }
        }

        // multi-param JSON
        if (isOn && params.length > 1) {
            JsonObject json = new JsonObject();
            for (String p : params) {
                String value = storedChannelValues.getProperty(channelId + "#" + p);
                if (value != null) {
                    if ("number".equals(prop.get(p + "Type"))) {
                        try {
                            json.addProperty(p, Double.parseDouble(value));
                        } catch (NumberFormatException e) {
                            logger.debug("Skip param {}: invalid cached numeric value '{}'", p, value);
                            continue;
                        }
                    } else {
                        json.addProperty(p, value);
                    }
                }
            }

            String uri = prop.containsKey("scheduleUri") ? prop.get("scheduleUri") : prop.get("activateUri");
            if (uri == null) {
                return null;
            }
            return new UriParam(uri, json.toString());
        }
        String uri;
        if (isOn) {
            uri = prop.get("activateUri");
        } else {
            uri = prop.get("deactivateUri");
            if (uri == null) {
                uri = prop.get("disableUri");
            }
            if (uri == null) {
                uri = prop.get("unscheduleUri");
            }
        }
        return uri != null ? new UriParam(uri, "{}") : null;
    }

    @Override
    public void handleUpdateChannel(ViessmannMessage msg) {
        logger.trace("handleUpdateChannel: {}", msg);
    }

    @Override
    public void handleUpdate(FeatureDataDTO featureDataDTO) {
        updateStatus(ThingStatus.ONLINE);
        if (featureDataDTO.properties == null) {
            return;
        }
        ThingMessageDTO msg = new ThingMessageDTO();
        msg.setDeviceId(featureDataDTO.deviceId);
        msg.setFeatureClear(featureDataDTO.feature);
        msg.setFeatureDescription(getFeatureDescription(featureDataDTO.feature));

        Map<String, FeatureCommand> commands = featureDataDTO.commands;
        if (commands != null) {
            msg.setCommands(new HashMap<>(commands));
        }

        FeatureProperties prop = featureDataDTO.properties;
        List<String> entr = prop.getUsedEntries();
        if (entr.isEmpty()) {
            return;
        }

        for (String entry : entr) {
            if ("unit".equals(entry)) {
                continue;
            }

            String valueEntry = "";
            String typeEntry = "";
            boolean bool = false;
            boolean setStateDescription = true;
            String viUnit = "";
            String unit = null;
            msg.setFeatureName(getFeatureName(featureDataDTO.feature));
            msg.setSuffix(entry);
            switch (entry) {
                case "value":
                    viUnit = prop.value.unit;
                    typeEntry = switch (viUnit) {
                        case "celsius" -> "temperature";
                        case "percent", "kelvin", "liter", "bar", "kilowattpeak", "volt", "ampere" -> viUnit;
                        case "minute" -> "duration-minute";
                        case "revolutionsPerSecond" -> "revolutions-per-second";
                        case "kilowattHour/year" -> "house-heating-load";
                        case "kiloJoule" -> "thermal-energy";
                        case "cubicMeter/hour" -> "cubic-meter-per-hour";
                        case "wattHour" -> "watt-hour";
                        case null, default -> prop.value.type;
                    };
                    if ("liter/hour".equals(viUnit)) {
                        valueEntry = String.valueOf(Double.parseDouble(prop.value.value) / 60);
                        viUnit = "liter/minute";
                        typeEntry = "liter-per-minute";
                    } else {
                        valueEntry = prop.value.value;
                    }
                    if ("kilowatt".equals(viUnit)) {
                        valueEntry = String.valueOf(Double.parseDouble(prop.value.value) * 1000);
                        viUnit = "watt";
                        typeEntry = "power";
                    }
                    break;
                case "status":
                    typeEntry = prop.status.type;
                    valueEntry = prop.status.value;
                    if ("off".equals(valueEntry)) {
                        typeEntry = "boolean";
                    } else if ("on".equals(valueEntry)) {
                        typeEntry = "boolean";
                        bool = true;
                    }
                    viUnit = "";
                    break;
                case "active":
                    typeEntry = prop.active.type;
                    valueEntry = prop.active.value ? "true" : "false";
                    bool = prop.active.value;
                    break;
                case "name":
                    typeEntry = prop.name.type;
                    valueEntry = prop.name.value;
                    break;
                case "shift":
                    typeEntry = "shift";
                    valueEntry = prop.shift.value.toString();
                    break;
                case "slope":
                    typeEntry = "slope";
                    valueEntry = prop.slope.value.toString();
                    break;
                case "entries":
                    msg.setSuffix("schedule");
                    typeEntry = prop.entries.type;
                    valueEntry = new Gson().toJson(prop.entries.value);
                    break;
                case "overlapAllowed":
                    typeEntry = prop.overlapAllowed.type;
                    valueEntry = prop.overlapAllowed.value ? "true" : "false";
                    bool = prop.overlapAllowed.value;
                    break;
                case "temperature":
                    valueEntry = prop.temperature.value.toString();
                    typeEntry = "temperature";
                    viUnit = prop.temperature.unit;
                    break;
                case "start":
                    typeEntry = prop.start.type;
                    valueEntry = prop.start.value;
                    break;
                case "begin":
                    typeEntry = prop.begin.type;
                    valueEntry = prop.begin.value;
                    break;
                case "end":
                    typeEntry = prop.end.type;
                    valueEntry = prop.end.value;
                    break;
                case "top":
                    typeEntry = prop.top.type;
                    valueEntry = prop.top.value.toString();
                    break;
                case "middle":
                    typeEntry = prop.middle.type;
                    valueEntry = prop.middle.value.toString();
                    break;
                case "bottom":
                    typeEntry = prop.bottom.type;
                    valueEntry = prop.bottom.value.toString();
                    break;
                case "day":
                    // returns array as string
                    typeEntry = prop.day.type;
                    valueEntry = prop.day.value.toString();
                    if (featureDataDTO.feature.contains("gas")) {
                        viUnit = "gas-" + prop.day.unit;
                    } else if (featureDataDTO.feature.contains("power")) {
                        viUnit = "power-" + prop.day.unit;
                    } else if (featureDataDTO.feature.contains(".heat.")) {
                        viUnit = "heat-" + prop.day.unit;
                    } else {
                        viUnit = prop.day.unit;
                    }
                    break;
                case "week":
                    // returns array as string
                    typeEntry = prop.week.type;
                    valueEntry = prop.week.value.toString();
                    if (featureDataDTO.feature.contains("gas")) {
                        viUnit = "gas-" + prop.week.unit;
                    } else if (featureDataDTO.feature.contains("power")) {
                        viUnit = "power-" + prop.week.unit;
                    } else if (featureDataDTO.feature.contains(".heat.")) {
                        viUnit = "heat-" + prop.week.unit;
                    } else {
                        viUnit = prop.week.unit;
                    }
                    break;
                case "month":
                    // returns array as string
                    typeEntry = prop.month.type;
                    valueEntry = prop.month.value.toString();
                    if (featureDataDTO.feature.contains("gas")) {
                        viUnit = "gas-" + prop.month.unit;
                    } else if (featureDataDTO.feature.contains("power")) {
                        viUnit = "power-" + prop.month.unit;
                    } else if (featureDataDTO.feature.contains(".heat.")) {
                        viUnit = "heat-" + prop.month.unit;
                    } else {
                        viUnit = prop.month.unit;
                    }
                    break;
                case "year":
                    // returns array as string
                    typeEntry = prop.year.type;
                    valueEntry = prop.year.value.toString();
                    if (featureDataDTO.feature.contains("gas")) {
                        viUnit = "gas-" + prop.year.unit;
                    } else if (featureDataDTO.feature.contains("power")) {
                        viUnit = "power-" + prop.year.unit;
                    } else if (featureDataDTO.feature.contains(".heat.")) {
                        viUnit = "heat-" + prop.year.unit;
                    } else {
                        viUnit = prop.year.unit;
                    }
                    break;
                case "starts":
                    typeEntry = prop.starts.type;
                    valueEntry = prop.starts.value.toString();
                    viUnit = prop.starts.unit;
                    break;
                case "hours":
                    typeEntry = "duration";
                    valueEntry = prop.hours.value.toString();
                    viUnit = "hour";
                    break;
                case "hoursLoadClassOne":
                    typeEntry = "duration";
                    valueEntry = prop.hoursLoadClassOne.value.toString();
                    viUnit = "hour";
                    break;
                case "hoursLoadClassTwo":
                    typeEntry = "duration";
                    valueEntry = prop.hoursLoadClassTwo.value.toString();
                    viUnit = "hour";
                    break;
                case "hoursLoadClassThree":
                    typeEntry = "duration";
                    valueEntry = prop.hoursLoadClassThree.value.toString();
                    viUnit = "hour";
                    break;
                case "hoursLoadClassFour":
                    typeEntry = "duration";
                    valueEntry = prop.hoursLoadClassFour.value.toString();
                    viUnit = "hour";
                    break;
                case "hoursLoadClassFive":
                    typeEntry = "duration";
                    valueEntry = prop.hoursLoadClassFive.value.toString();
                    viUnit = "hour";
                    break;
                case "min":
                    typeEntry = prop.min.type;
                    valueEntry = prop.min.value.toString();
                    viUnit = prop.min.unit;
                    break;
                case "max":
                    typeEntry = prop.max.type;
                    valueEntry = prop.max.value.toString();
                    viUnit = prop.max.unit;
                    break;
                case "phase":
                    typeEntry = prop.phase.type;
                    valueEntry = prop.phase.value;
                    viUnit = "";
                    break;
                case "switchOnValue":
                    typeEntry = prop.switchOnValue.type;
                    valueEntry = prop.switchOnValue.value;
                    viUnit = prop.switchOffValue.unit;
                    break;
                case "switchOffValue":
                    typeEntry = prop.switchOffValue.type;
                    valueEntry = prop.switchOffValue.value;
                    viUnit = prop.switchOffValue.unit;
                    break;
                case "enabled":
                    typeEntry = prop.enabled.type;
                    JsonElement v = prop.enabled.value;
                    if ("array".equals(typeEntry)) {
                        valueEntry = v.toString();
                    }
                    if ("boolean".equals(typeEntry)) {
                        valueEntry = v.getAsBoolean() ? "true" : "false";
                        bool = v.getAsBoolean();
                    }
                    break;
                case "currentDay":
                    typeEntry = "power-energy";
                    valueEntry = prop.currentDay.value.toString();
                    break;
                case "lastSevenDays":
                    typeEntry = "power-energy";
                    valueEntry = prop.lastSevenDays.value.toString();
                    break;
                case "currentMonth":
                    typeEntry = "power-energy";
                    valueEntry = prop.currentMonth.value.toString();
                    break;
                case "lastMonth":
                    typeEntry = "power-energy";
                    valueEntry = prop.lastMonth.value.toString();
                    break;
                case "currentYear":
                    typeEntry = "power-energy";
                    valueEntry = prop.currentYear.value.toString();
                    break;
                case "lastYear":
                    typeEntry = "power-energy";
                    valueEntry = prop.lastYear.value.toString();
                    break;
                case "target":
                    typeEntry = "percent";
                    valueEntry = prop.target.value.toString();
                    viUnit = prop.target.unit;
                    break;
                case "current":
                    typeEntry = "percent";
                    valueEntry = prop.current.value.toString();
                    viUnit = prop.current.unit;
                    break;
                case "weekdays":
                    typeEntry = prop.weekdays.type;
                    valueEntry = prop.weekdays.value.toString().replace("[", "").replace("]", "");
                    viUnit = prop.weekdays.unit;
                    msg.setSuffix("weekday");
                    break;
                case "startHour":
                    typeEntry = "start-hour";
                    valueEntry = prop.startHour.value.toString();
                    viUnit = prop.startHour.unit;
                    setStateDescription = false;
                    break;
                case "startMinute":
                    typeEntry = "start-minute";
                    valueEntry = prop.startMinute.value.toString();
                    viUnit = prop.startMinute.unit;
                    setStateDescription = false;
                    break;
                default:
                    AdditionalProperty additionalProperty = resolveAdditionalProperty(prop, entry);
                    if (additionalProperty == null) {
                        continue;
                    }
                    typeEntry = additionalProperty.type();
                    valueEntry = additionalProperty.value();
                    bool = additionalProperty.boolValue();
                    viUnit = nvl(additionalProperty.unit(), "");
                    break;
            }
            msg.setType(typeEntry);
            msg.setValue(valueEntry);
            msg.setChannelType(typeEntry);

            if ("[]".equals(valueEntry)) {
                return;
            }

            if (msg.getDeviceId().contains(config.deviceId)) {
                logger.trace("Feature: {} Type:{} Entry: {}={}", featureDataDTO.feature, typeEntry, entry, valueEntry);

                String subChannelType = "";

                if (viUnit != null) {
                    if (!viUnit.isEmpty()) {
                        msg.setUnit(viUnit);
                        unit = UNIT_MAP.get(viUnit);
                        subChannelType = SUB_CHANNEL_TYPE_MAP.getOrDefault(viUnit, "");
                        if (unit == null) {
                            logger.warn(
                                    "Unknown unit. Could not parse unit: {} of Feature: {} - Please create an issue on GitHub with this anonymized detailed JSON:\r\n{}",
                                    viUnit, featureDataDTO.feature, featureDataDTO.toPrettyJson());
                            return;
                        }
                    }
                }

                Channel channel = thing.getChannel(msg.getChannelId());
                if (channel == null) {
                    logger.trace("Channel does not exist -> Channel is being created");
                    createOrUpdateChannel(msg);
                } else {
                    updateChannelIfPropertiesChanged(channel, msg);

                    String channelDescription = channel.getDescription();
                    if (channelDescription != null) {
                        if (!channelDescription.equals(msg.getFeatureDescription())) {
                            logger.trace("Channel Description is different. The channel is now being updated.");
                            createOrUpdateChannel(msg);
                        }
                    }

                    String channelLabel = channel.getLabel();
                    if (channelLabel != null) {
                        if (!channelLabel.equals(msg.getFeatureName())) {
                            logger.trace("Channel Label is different. The channel is now being updated.");
                            createOrUpdateChannel(msg);
                        }
                    }
                }
                setStateDescriptionOptions(msg, setStateDescription);

                ThingMessageDTO subMsg = new ThingMessageDTO();
                subMsg.setDeviceId(featureDataDTO.deviceId);
                subMsg.setFeatureClear(featureDataDTO.feature);
                subMsg.setFeatureDescription(getFeatureDescription(featureDataDTO.feature));
                subMsg.setFeatureName(getFeatureName(featureDataDTO.feature));
                subMsg.setType(typeEntry);
                subMsg.setValue(valueEntry);
                switch (entry) {
                    case "entries":
                        subMsg.setIsSubChannel(true);
                        subMsg.setSuffix("produced");
                        subMsg.setChannelType("boolean-read-only");
                        checkIfSubChannelExists(subMsg);
                        break;
                    case "day":
                        subMsg.setIsSubChannel(true);
                        subMsg.setSuffix("today");
                        subMsg.setChannelType(subChannelType);
                        checkIfSubChannelExists(subMsg);
                        subMsg.setSuffix("yesterday");
                        checkIfSubChannelExists(subMsg);
                        break;
                    case "week":
                        subMsg.setIsSubChannel(true);
                        subMsg.setSuffix("this-week");
                        subMsg.setChannelType(subChannelType);
                        checkIfSubChannelExists(subMsg);
                        subMsg.setSuffix("last-week");
                        checkIfSubChannelExists(subMsg);
                        break;
                    case "month":
                        subMsg.setIsSubChannel(true);
                        subMsg.setSuffix("this-month");
                        subMsg.setChannelType(subChannelType);
                        checkIfSubChannelExists(subMsg);
                        subMsg.setSuffix("last-month");
                        checkIfSubChannelExists(subMsg);
                        break;
                    case "year":
                        subMsg.setIsSubChannel(true);
                        subMsg.setSuffix("this-year");
                        subMsg.setChannelType(subChannelType);
                        checkIfSubChannelExists(subMsg);
                        subMsg.setSuffix("last-year");
                        checkIfSubChannelExists(subMsg);
                        break;
                    case "active":
                        if (featureDataDTO.feature.contains("oneTimeCharge")) {
                            subMsg.setIsSubChannel(true);
                            subMsg.setSuffix("status");
                            subMsg.setChannelType("boolean-read-only");
                            checkIfSubChannelExists(subMsg);
                        }
                        if (featureDataDTO.feature.contains("holiday")) {
                            subMsg.setIsSubChannel(true);
                            subMsg.setSuffix("status");
                            subMsg.setChannelType("boolean-read-only");
                            checkIfSubChannelExists(subMsg);
                        }
                        break;
                    default:
                        break;
                }

                switch (typeEntry) {
                    case "decimal":
                    case "number":
                    case "temperature":
                    case "percent":
                    case "minute":
                    case "hours":
                    case "kelvin":
                    case "liter":
                    case "liter-per-minute":
                    case "bar":
                    case "power":
                    case "revolutions-per-second":
                    case "slope":
                    case "shift":
                    case "duration":
                    case "duration-minute":
                    case "thermal-energy":
                    case "cubic-meter-per-hour":
                    case "watt-hour":
                    case "volt":
                    case "ampere":
                    case "power-energy":
                    case "target":
                    case "current":
                    case "start-hour":
                    case "start-minute":
                        updateChannelState(msg.getChannelId(), msg.getValue(), unit);
                        break;
                    case "boolean":
                        OnOffType state = bool ? OnOffType.ON : OnOffType.OFF;
                        if (featureDataDTO.feature.contains("oneTimeCharge")
                                || featureDataDTO.feature.contains("holiday")) {
                            updateState(subMsg.getChannelId(), state);
                        }
                        if (!featureDataDTO.feature.contains("holiday")) {
                            updateState(msg.getChannelId(), state);
                        }
                        break;
                    case "house-heating-load":
                    case "kilowattpeak":
                        updateState(msg.getChannelId(), new DecimalType(msg.getValue()));
                        break;
                    case "string":
                    case "array":
                    case "weekdays":
                        if (featureDataDTO.feature.contains("holiday")) {
                            subMsg.setSuffix("active");

                            final String channelId = msg.getChannelId();
                            final String stored = storedChannelValues.getProperty(channelId);

                            String startDate = "start".equals(entry) ? valueEntry
                                    : storedChannelValues.getProperty(channelId.replace("#end", "#start"));
                            String endDate = "end".equals(entry) ? valueEntry
                                    : storedChannelValues.getProperty(channelId.replace("#start", "#end"));

                            boolean isActive = isTodayWithinRange(startDate, endDate);
                            if (!isActive) {
                                LocalDate today = LocalDate.now(ViessmannUtil.getOpenHABZoneId());
                                String ld = today.format(API_DATE_FORMATTER);
                                isActive = isTodayWithinRange(ld, endDate);
                            }
                            updateState(subMsg.getChannelId(), isActive ? OnOffType.ON : OnOffType.OFF);

                            if (valueEntry.isBlank()) {
                                valueEntry = (stored == null || stored.isBlank()) ? getApiDate("end".equals(entry))
                                        : stored;

                                msg.setValue(valueEntry);
                            }

                            updateState(channelId, StringType.valueOf(valueEntry));
                        }

                        updateState(msg.getChannelId(), StringType.valueOf(msg.getValue()));
                        String[] parts = msg.getValue().replace("[", "").replace("]", "").replace(" ", "").split(",");
                        if (parts.length > 1) {
                            switch (entry) {
                                case "day":
                                    subMsg.setSuffix("today");
                                    updateChannelState(subMsg.getChannelId(), parts[0], unit);
                                    subMsg.setSuffix("yesterday");
                                    updateChannelState(subMsg.getChannelId(), parts[1], unit);
                                    break;
                                case "week":
                                    subMsg.setSuffix("this-week");
                                    updateChannelState(subMsg.getChannelId(), parts[0], unit);
                                    subMsg.setSuffix("last-week");
                                    updateChannelState(subMsg.getChannelId(), parts[1], unit);
                                    break;
                                case "month":
                                    subMsg.setSuffix("this-month");
                                    updateChannelState(subMsg.getChannelId(), parts[0], unit);
                                    subMsg.setSuffix("last-month");
                                    updateChannelState(subMsg.getChannelId(), parts[1], unit);
                                    break;
                                case "year":
                                    subMsg.setSuffix("this-year");
                                    updateChannelState(subMsg.getChannelId(), parts[0], unit);
                                    subMsg.setSuffix("last-year");
                                    updateChannelState(subMsg.getChannelId(), parts[1], unit);
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    case "Schedule":
                        updateState(msg.getChannelId(), StringType.valueOf(msg.getValue()));
                        String channelId = msg.getChannelId().replace("#schedule", "#produced");
                        updateState(channelId, parseSchedule(msg.getValue()));
                        break;
                    default:
                        if (logger.isDebugEnabled()) {
                            logger.warn(
                                    "This is only shown in [DEBUG] => Unknown property {}, type: {}, value: {}, unit: {}, feature: {} JSON: \r\n{}",
                                    entry, typeEntry, valueEntry, viUnit, featureDataDTO.feature,
                                    featureDataDTO.toPrettyJson());
                        }
                        updateState(msg.getChannelId(), StringType.valueOf(msg.getValue()));
                        break;
                }
                storedChannelValues.putProperty(msg.getChannelId(), msg.getValue());
            }
        }
    }

    private @Nullable AdditionalProperty resolveAdditionalProperty(FeatureProperties properties, String entry) {
        JsonElement additional = properties.additionalProperties.get(entry);
        if (additional == null) {
            return null;
        }
        String typeEntry = "string";
        String valueEntry = "";
        String unit = null;
        boolean boolValue = false;
        if (additional.isJsonObject()) {
            JsonObject obj = additional.getAsJsonObject();
            JsonElement typeElement = obj.get("type");
            if (typeElement != null && typeElement.isJsonPrimitive()) {
                typeEntry = typeElement.getAsString();
            }
            JsonElement unitElement = obj.get("unit");
            if (unitElement != null && unitElement.isJsonPrimitive()) {
                unit = unitElement.getAsString();
            }
            JsonElement valueElement = obj.get("value");
            if (valueElement != null) {
                if (valueElement.isJsonPrimitive()) {
                    if ("boolean".equals(typeEntry) && valueElement.getAsJsonPrimitive().isBoolean()) {
                        boolValue = valueElement.getAsBoolean();
                        valueEntry = boolValue ? "true" : "false";
                    } else {
                        valueEntry = valueElement.getAsString();
                    }
                } else {
                    valueEntry = valueElement.toString();
                    if (valueElement.isJsonArray() && typeElement == null) {
                        typeEntry = "array";
                    }
                }
            } else {
                valueEntry = obj.toString();
            }
        } else if (additional.isJsonPrimitive()) {
            if (additional.getAsJsonPrimitive().isBoolean()) {
                typeEntry = "boolean";
                boolValue = additional.getAsBoolean();
                valueEntry = boolValue ? "true" : "false";
            } else {
                valueEntry = additional.getAsString();
            }
        } else {
            valueEntry = additional.toString();
            if (additional.isJsonArray()) {
                typeEntry = "array";
            }
        }
        if (valueEntry.isBlank()) {
            valueEntry = additional.toString();
        }
        return new AdditionalProperty(typeEntry, valueEntry, unit, boolValue);
    }

    private record AdditionalProperty(String type, String value, @Nullable String unit, boolean boolValue) {
    }

    /**
     * Creates new channels or updates existing channel for the thing.
     *
     * @param msg contains everything is needed of the channel to be created or updated.
     */
    private void createOrUpdateChannel(ThingMessageDTO msg) {
        getSafeCallback().ifPresent(cb -> {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), msg.getChannelId());
            ChannelTypeUID typeUID = new ChannelTypeUID(BINDING_ID, convertChannelType(msg));

            Map<String, String> properties = new HashMap<>();
            if (!msg.isSubChannel) {
                properties = buildProperties(msg);
            }

            if (msg.isSubChannel && checkSubChannelLabelAndDescription(msg)) {
                properties.put("feature", msg.getFeatureClear());
            }

            try {
                Channel channel = cb.createChannelBuilder(channelUID, typeUID)
                        .withLabel(nvl(msg.getFeatureName(), msg.getChannelId()))
                        .withDescription(nvl(msg.getFeatureDescription(), "")).withType(typeUID)
                        .withProperties(properties).build();
                Thing edited = editThing().withoutChannel(channelUID).withChannel(channel).build();
                updateThing(edited);
                logger.debug("{} {} created/updated on Thing {}", msg.isSubChannel ? "Sub-Channel" : "Channel",
                        channelUID, getThing().getUID());
            } catch (IllegalArgumentException e) {
                logger.warn("{}", e.getMessage());
            }
        });
    }

    private Optional<ThingHandlerCallback> getSafeCallback() {
        ThingHandlerCallback cb = getCallback();
        if (cb == null) {
            logger.warn("ThingHandlerCallback is null for Thing {} (Thing not initialized?)", getThing().getUID());
            return Optional.empty();
        }
        return Optional.of(cb);
    }

    private static String nvl(@Nullable String a, String fallback) {
        return a != null ? a : fallback;
    }

    private boolean checkSubChannelLabelAndDescription(ThingMessageDTO msg) {
        boolean updateChannel = false;

        Channel channel = thing.getChannel(msg.getChannelId());

        if (channel != null) {
            String channelDescription = channel.getDescription();
            String channelLabel = channel.getLabel();

            if (channelDescription != null) {
                if (!channelDescription.equals(msg.getFeatureDescription())) {
                    logger.trace("Sub-Channel Description is different. The channel is now being updated.");
                    updateChannel = true;
                }
            }

            if (channelLabel != null) {
                if (!channelLabel.equals(msg.getFeatureName())) {
                    logger.trace("Sub-Channel Label is different. The channel is now being updated.");
                    updateChannel = true;
                }
            }
        } else {
            updateChannel = true;
        }
        return updateChannel;
    }

    private String getFeatureName(String feature) {
        Locale locale = localeProvider.getLocale();
        Pattern pattern = Pattern.compile("(\\.[0-3])");
        Matcher matcher = pattern.matcher(feature);
        if (matcher.find()) {
            String circuit = matcher.group(0);
            feature = matcher.replaceAll(".N");
            String featureName = Objects
                    .requireNonNull(i18Provider.getText(bundle, "viessmann.feature.name." + feature, feature, locale));
            return featureName + " (Circuit: " + circuit.replace(".", "") + ")";
        }
        return Objects
                .requireNonNull(i18Provider.getText(bundle, "viessmann.feature.name." + feature, feature, locale));
    }

    private @Nullable String getFeatureDescription(String feature) {
        Locale locale = localeProvider.getLocale();
        feature = feature.replaceAll("\\.[0-3]", ".N");
        return Objects
                .requireNonNull(i18Provider.getText(bundle, "viessmann.feature.description." + feature, "", locale));
    }

    private OnOffType parseSchedule(String scheduleJson) {
        ZonedDateTime now = ZonedDateTime.now(ViessmannUtil.getOpenHABZoneId());

        String curr = String.format(Locale.ROOT, "%02d:%02d", now.getHour(), now.getMinute());
        Date currTime = parseTime(curr);

        @Nullable
        ScheduleDTO schedule;
        try {
            schedule = GSON.fromJson(scheduleJson, ScheduleDTO.class);
        } catch (JsonSyntaxException e) {
            logger.debug("Invalid schedule JSON: {}", scheduleJson, e);
            return OnOffType.OFF;
        }

        if (schedule == null) {
            logger.warn("Could not create schedule object and determine day.");
            return OnOffType.OFF;
        }

        @SuppressWarnings("null")
        List<DaySchedule> day = switch (now.getDayOfWeek()) {
            case MONDAY -> Objects.requireNonNullElse(schedule.getMon(), List.of());
            case TUESDAY -> Objects.requireNonNullElse(schedule.getTue(), List.of());
            case WEDNESDAY -> Objects.requireNonNullElse(schedule.getWed(), List.of());
            case THURSDAY -> Objects.requireNonNullElse(schedule.getThu(), List.of());
            case FRIDAY -> Objects.requireNonNullElse(schedule.getFri(), List.of());
            case SATURDAY -> Objects.requireNonNullElse(schedule.getSat(), List.of());
            case SUNDAY -> Objects.requireNonNullElse(schedule.getSun(), List.of());
        };
        for (DaySchedule daySchedule : day) {
            Date startTime = parseTime(daySchedule.getStart());
            Date endTime = parseTime(daySchedule.getEnd());

            boolean spansMidnight = endTime.before(startTime);
            boolean inWindow = spansMidnight ? (currTime.after(startTime) || currTime.before(endTime))
                    : (currTime.after(startTime) && currTime.before(endTime));

            if (inWindow) {
                return OnOffType.ON;
            }
        }
        return OnOffType.OFF;
    }

    private Date parseTime(String time) {
        final String inputFormat = "HH:mm";
        SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.ROOT);
        try {
            return inputParser.parse(time);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    private String getApiDate(boolean tomorrow) {
        LocalDate date = LocalDate.now(ViessmannUtil.getOpenHABZoneId());
        if (tomorrow) {
            date = date.plusDays(1);
        }
        return date.format(API_DATE_FORMATTER);
    }

    private boolean isTodayWithinRange(@Nullable String startDateAsString, @Nullable String endDateAsString) {
        LocalDate startDate = parseApiDate(startDateAsString);
        LocalDate endDate = parseApiDate(endDateAsString);
        if (startDate == null || endDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now(ViessmannUtil.getOpenHABZoneId());
        return !today.isBefore(startDate) && !today.isAfter(endDate.minusDays(1));
    }

    private @Nullable LocalDate parseApiDate(@Nullable String dateAsString) {
        if (dateAsString == null || dateAsString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateAsString, API_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.debug("Invalid date format '{}', expected yyyy-MM-dd", dateAsString);
            return null;
        }
    }

    private void updateChannelState(String channelId, String stateAsString, @Nullable String unit) {
        if (unit != null) {
            updateState(channelId, new QuantityType<>(stateAsString + " " + unit));
        } else {
            if (stateAsString.isBlank()) {
                logger.debug("Skip update for {}: empty value", channelId);
                return;
            }
            try {
                DecimalType s = DecimalType.valueOf(stateAsString);
                updateState(channelId, s);
            } catch (IllegalArgumentException e) {
                logger.debug("Skip update for {}: invalid numeric value '{}'", channelId, stateAsString);
            }
        }
    }

    private Map<String, String> buildProperties(ThingMessageDTO msg) {
        Map<String, String> prop = new HashMap<>();

        String suffix = Objects.requireNonNullElse(msg.getSuffix(), "");
        String lcSuffix = suffix.toLowerCase(Locale.ROOT);

        prop.put("suffix", suffix);
        prop.put("feature", msg.getFeatureClear());
        prop.put("channelType", convertChannelType(msg));
        prop.put("circuitId", msg.getCircuitId());

        Map<String, FeatureCommand> commands = msg.getCommands();
        if (commands == null || commands.isEmpty()) {
            return prop;
        }

        for (String command : msg.getAllCommands()) {
            String lcCommand = command.toLowerCase(Locale.ROOT);

            // empty suffix → only one command allowed
            if (lcSuffix.isBlank() && prop.containsKey("command")) {
                continue;
            }

            FeatureCommand fc = commands.get(command);
            if (fc == null || fc.isDeprecated || !fc.isExecutable) {
                continue;
            }

            List<String> allParams = fc.getAllParams();
            List<String> lcAllParams = allParams.stream().map(p -> p.toLowerCase(Locale.ROOT)).toList();
            Set<String> allowedSuffixes = new HashSet<>(BASE_ALLOWED_SUFFIXES);
            allowedSuffixes.addAll(lcAllParams);

            if (!"triggerDaily".equals(command)) {
                if (!lcSuffix.isBlank() && !lcCommand.contains(lcSuffix) && !allowedSuffixes.contains(lcSuffix)) {
                    continue;
                }
            }

            for (String param : allParams) {
                prop.put(param + "Params", addProperties(prop, param + "Params", param));
                prop.put("params", addProperties(prop, "params", param));
                prop.put(fc.name + "Params", addProperties(prop, fc.name + "Params", param));

                if (fc.params == null) {
                    continue;
                }
                FeatureCommandParams fcp = fc.params.get(param);
                if (fcp == null) {
                    continue;
                }

                Map<String, Object> constraints = fcp.getConstraints();
                String regEx = "";
                if (constraints != null) {
                    regEx = (String) constraints.getOrDefault("regEx", "");
                }
                prop.put(param + "RegEx", regEx);
                prop.put(param + "Type", fcp.getType());
            }

            prop.put("command", addProperties(prop, "command", command));
            prop.put(fc.name + "Uri", fc.uri);
        }
        return prop;
    }

    private String addProperties(Map<String, String> properties, String key, String value) {
        String p = properties.get(key);
        if (p != null) {
            if (!p.contains(value)) {
                p = p + "," + value;
            }
            return p;
        }
        return value;
    }

    private String convertChannelType(ThingMessageDTO msg) {
        String channelType = msg.getChannelType();
        if (channelType.isBlank() || "object".equals(channelType)) {
            channelType = "string";
        }
        if ("meter".equals(msg.getUnit()) || "degree".equals(msg.getUnit())) {
            channelType = msg.getUnit();
        }

        List<String> com = msg.getAllCommands();
        if (com == null) {
            return channelType.toLowerCase(Locale.ROOT);
        }
        if (!com.isEmpty() && !"boolean-read-only".equals(channelType)) {
            for (String command : com) {
                switch (command) {
                    case "setTemperature":
                    case "setTargetTemperature":
                        if (!"boolean".equals(channelType)) {
                            channelType = "set-temperature";
                        }
                        break;
                    case "setMin":
                        if (msg.getSuffix().contains("min")) {
                            channelType = "set-min";
                        }
                        break;
                    case "setMax":
                        if (msg.getSuffix().contains("max")) {
                            channelType = "set-max";
                        }
                        break;
                    case "setHysteresis", "setHysteresisSwitchOnValue":
                        channelType = "set-target-hysteresis";
                        break;
                    case "setHysteresisSwitchOffValue":
                        if (msg.getSuffix().contains("switchOffValue")) {
                            channelType = "set-hysteresis-off";
                        }
                        break;
                    case "setMode":
                        channelType = "set-mode";
                        break;
                    case "setSchedule":
                    case "setName":
                        if (msg.getSuffix().contains("active")) {
                            channelType = "boolean-read-only";
                        }
                        break;
                    case "active":
                        channelType = "boolean";
                        break;
                    default:
                        break;
                }
            }
        } else if ("boolean".equals(channelType)) {
            channelType = channelType + "-read-only";
        }
        return channelType.toLowerCase(Locale.ROOT);
    }

    private void setStateDescriptionOptions(ThingMessageDTO msg, boolean set) {
        if (!set) {
            return;
        }

        Locale locale = localeProvider.getLocale();
        List<StateOption> stateOptions = new ArrayList<>();

        Map<String, FeatureCommand> commands = msg.getCommands();
        if (commands == null || commands.isEmpty()) {
            return;
        }
        commands.forEach((name, command) -> {
            ArrayList<String> p = command.getAllParams();
            p.forEach(param -> {
                if (command.params == null) {
                    return;
                }
                FeatureCommandParams fcp = command.params.get(param);
                if (fcp == null) {
                    return;
                }
                Map<String, Object> constraints = fcp.getConstraints();
                if (constraints == null) {
                    return;
                }
                if (constraints.containsKey("enumValue")) {
                    final List<String> modes = new ArrayList<>();

                    Optional.ofNullable(command.params.get(param)).map(FeatureCommandParams::getConstraints)
                            .map(c -> c.get("enumValue")).filter(List.class::isInstance).map(v -> (List<?>) v)
                            .ifPresent(list -> list.stream().filter(String.class::isInstance).map(String.class::cast)
                                    .forEach(modes::add));

                    if (msg.getFeatureClear().contains("hygiene")) {
                        modes.add("Sun, Mon, Tue, Wed, Thu, Fri, Sat");
                    }

                    for (String cmd : modes) {
                        String commandLabel = Objects.requireNonNull(
                                i18Provider.getText(bundle, "viessmann.command.label." + cmd, cmd, locale));
                        StateOption stateOption = new StateOption(cmd, commandLabel);
                        stateOptions.add(stateOption);
                    }
                    ChannelUID channelUID = new ChannelUID(thing.getUID(), msg.getChannelId());
                    setChannelStateDescription(channelUID, stateOptions);
                }
            });
        });
    }

    private int getThingTypeVersion() {
        String versionString = thing.getProperties().get("thingTypeVersion");
        if (versionString == null) {
            logger.debug("Thing property 'thingTypeVersion' not set, assuming 0");
            return 0;
        }

        try {
            return Integer.parseInt(versionString);
        } catch (NumberFormatException e) {
            logger.debug("Invalid thingTypeVersion '{}', assuming 0", versionString);
            return 0;
        }
    }

    private boolean isManagedItem(String itemName) {
        boolean isManaged = managedItemProvider.getAll().stream().anyMatch(item -> item.getName().equals(itemName));

        if (isManaged) {
            return true; // item comes from UI / JSONDB
        }

        // fallback: treat as file-based
        logger.debug("Item '{}' not found in managed provider – treating as file-based.", itemName);
        return false;
    }

    private String require(Map<String, String> map, String key) {
        String value = map.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(key + " missing");
        }
        return value;
    }

    private void updateChannelIfPropertiesChanged(Channel channel, ThingMessageDTO msg) {
        Map<String, String> oldProperties = channel.getProperties();
        Map<String, String> newProperties = buildProperties(msg);

        if (!Objects.equals(oldProperties, newProperties)) {
            createOrUpdateChannel(msg);
            logger.debug("Channel {} properties updated: {}", channel.getUID(), newProperties);
        }
    }

    private void checkIfSubChannelExists(ThingMessageDTO msg) {
        Channel channel = thing.getChannel(msg.getChannelId());
        if (channel == null) {
            logger.trace("Sub-Channel does not exist -> Channel is being created");
            createOrUpdateChannel(msg);
        }
    }

    private boolean checkCommandType(Map<String, String> prop, String type) {
        String[] params = new String[] { "{}" };
        String paramsValue = prop.get("params");
        if (paramsValue != null) {
            params = paramsValue.split(",");
        }

        for (String p : params) {
            String propType = prop.get(p + "Type");
            if (propType == null) {
                return false;
            }
            if (propType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private record UriParam(String uri, String param) {
    }

    private boolean isQuantityCommand(String command) {
        return command.matches(".*(Temperature|temperature|setHysteresis|setMin|setMax).*");
    }
}
