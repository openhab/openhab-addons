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
package org.openhab.binding.homeconnectdirect.internal.handler;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ACTIVE_PROGRAM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ATTRIBUTE_ACCESS;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ATTRIBUTE_AVAILABLE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ATTRIBUTE_ENUMERATION_TYPE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ATTRIBUTE_ENUMERATION_TYPE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ATTRIBUTE_MAX;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ATTRIBUTE_MIN;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ATTRIBUTE_STEP_SIZE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.BINDING_ID;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.BINDING_PROFILES_PATH;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_ACTIVE_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_CHILD_LOCK;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COMMAND;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DOOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_OPERATION_STATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_POWER_STATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_PROGRAM_COMMAND;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_PROGRAM_PROGRESS;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_RAW_MESSAGE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_REMAINING_PROGRAM_TIME;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_REMOTE_CONTROL_OR_START_ALLOWED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_SELECTED_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_ENUM_SWITCH_VALUE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_NUMBER_DESCRIPTION;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_NUMBER_VALUE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_STRING_DESCRIPTION;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_STRING_VALUE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_SWITCH_DESCRIPTION;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_SWITCH_VALUE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_TRIGGER_VALUE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHILD_LOCK_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COMMAND_PAUSE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COMMAND_RESUME;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COMMAND_START;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CONFIGURATION_ATTRIBUTE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CONFIGURATION_DESCRIPTION_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CONFIGURATION_ON_VALUE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CONFIGURATION_UNIT_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CONFIGURATION_VALUE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CONSCRYPT_REQUIRED_GLIBC_MIN_VERSION;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CONSCRYPT_SUPPORTED_SYSTEMS;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DOOR_STATE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_PAUSE_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_RESUME_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_START_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OPERATION_STATE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.PAUSE_PROGRAM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.POWER_STATE_ENUM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.POWER_STATE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.PROGRAM_FAVORITE_KEY_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.PROGRAM_PROGRESS_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.REMAINING_PROGRAM_TIME_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.REMOTE_CONTROL_START_ALLOWED_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.RESUME_PROGRAM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SELECTED_PROGRAM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_AJAR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_FINISHED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_MAINS_OFF;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_NO_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_OFF;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_ON;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_OPEN;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_STANDBY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.UPDATE_ALL_MANDATORY_VALUES_INTERVAL;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WS_AES_URI_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WS_DEVICE_NAME;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WS_DEVICE_TYPE_APPLICATION_V1;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WS_DEVICE_TYPE_APPLICATION_V2;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WS_TLS_URI_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ZONE_ID;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.OSUtils.getOSArch;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.OSUtils.getOSName;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.OSUtils.isLinux;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.mapKeyToLabel;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action.GET;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action.NOTIFY;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action.RESPONSE;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.CI;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.CI_AUTHENTICATION;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.CI_INFO;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.CI_SERVICES;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.CI_TZ_INFO;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.EI_DEVICE_READY;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.EI_INITIAL_VALUES;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.IZ;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.IZ_INFO;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.NI;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.NI_INFO;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_ACTIVE_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_ALL_DESCRIPTION_CHANGES;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_ALL_MANDATORY_VALUES;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_DESCRIPTION_CHANGE;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_SELECTED_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_VALUES;
import static org.openhab.core.library.unit.Units.PERCENT;
import static org.openhab.core.library.unit.Units.SECOND;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.common.LimitedSizeList;
import org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils;
import org.openhab.binding.homeconnectdirect.internal.common.xml.exception.ParseException;
import org.openhab.binding.homeconnectdirect.internal.configuration.HomeConnectDirectApplianceConfiguration;
import org.openhab.binding.homeconnectdirect.internal.configuration.HomeConnectDirectConfiguration;
import org.openhab.binding.homeconnectdirect.internal.handler.model.ApplianceMessage;
import org.openhab.binding.homeconnectdirect.internal.handler.model.MessageType;
import org.openhab.binding.homeconnectdirect.internal.handler.model.SendMessageRequest;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.i18n.HomeConnectDirectTranslationProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicCommandDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicStateDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.DeviceDescriptionService;
import org.openhab.binding.homeconnectdirect.internal.service.description.DeviceDescriptionUtils;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ContentType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescriptionType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.AccessProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.AvailableProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.EnumerationTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.RangeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.feature.FeatureMappingService;
import org.openhab.binding.homeconnectdirect.internal.service.profile.ApplianceProfileService;
import org.openhab.binding.homeconnectdirect.internal.service.profile.model.AesCredentials;
import org.openhab.binding.homeconnectdirect.internal.service.profile.model.TlsCredentials;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.WebSocketAesClientService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.WebSocketClientService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.WebSocketHandler;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.WebSocketTlsConscryptClientService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.exception.WebSocketClientServiceException;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Message;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.ApplianceInfoData;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.DescriptionChangeData;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.DeviceData;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.FirstMessageIdData;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.ProgramData;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.ServiceData;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.ValueData;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.serializer.ResourceAdapter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.gson.ToNumberPolicy;

/**
 * The {@link BaseHomeConnectDirectHandler} is the base handler for all Home Connect Direct appliance handlers.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class BaseHomeConnectDirectHandler extends BaseThingHandler implements WebSocketHandler {
    private static final Pattern FAVORITE_NAME_PATTERN = Pattern
            .compile("^BSH\\.Common\\.Setting\\.Favorite\\.(\\d{3})\\.Name$");

    private final ApplianceProfileService applianceProfileService;
    private final Logger logger;
    private final Gson gson;
    private final List<ServiceData> services;
    private final SecureRandom secureRandom;
    private final AtomicBoolean disposeInitialized;
    private final LimitedSizeList<ApplianceMessage> applianceMessages;
    private final List<Consumer<ApplianceMessage>> applianceMessageConsumers;
    private final HomeConnectDirectDynamicStateDescriptionProvider stateDescriptionProvider;
    private final HomeConnectDirectDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final HomeConnectDirectTranslationProvider translationProvider;
    private final String deviceId;
    private final ConcurrentHashMap<String, String> keyValueStore;
    private final ConcurrentHashMap<String, String> favoriteNames;

    private @Nullable ScheduledFuture<?> reconnectFuture;
    private @Nullable ScheduledFuture<?> updateValuesFuture;
    private @Nullable WebSocketClientService webSocketClientService;
    private @Nullable HomeConnectDirectApplianceConfiguration configuration;
    private @Nullable DeviceDescriptionService deviceDescriptionService;
    private @Nullable FeatureMappingService featureMappingService;
    private @Nullable String connectionError;
    private long outgoingMessageId;
    private long sessionId;
    private long lastRefreshExecutionTime;

    public BaseHomeConnectDirectHandler(Thing thing, ApplianceProfileService applianceProfileService,
            HomeConnectDirectDynamicCommandDescriptionProvider commandDescriptionProvider,
            HomeConnectDirectDynamicStateDescriptionProvider stateDescriptionProvider, String deviceId,
            HomeConnectDirectConfiguration configuration, HomeConnectDirectTranslationProvider translationProvider) {
        super(thing);

        this.applianceProfileService = applianceProfileService;
        this.logger = LoggerFactory.getLogger(BaseHomeConnectDirectHandler.class);
        this.gson = new GsonBuilder().registerTypeAdapter(Resource.class, new ResourceAdapter())
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
        this.secureRandom = new SecureRandom();
        this.services = new ArrayList<>();
        this.disposeInitialized = new AtomicBoolean(false);
        this.applianceMessages = new LimitedSizeList<>(configuration.messageQueueSize);
        this.applianceMessageConsumers = Collections.synchronizedList(new ArrayList<>());
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.translationProvider = translationProvider;
        this.deviceId = deviceId;
        this.lastRefreshExecutionTime = 0;
        this.keyValueStore = new ConcurrentHashMap<>();
        this.favoriteNames = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize() {
        connectionError = null;
        disposeInitialized.set(false);
        services.clear();
        var configuration = getConfigAs(HomeConnectDirectApplianceConfiguration.class);
        this.configuration = configuration;

        // check thing configuration
        if (StringUtils.isBlank(configuration.address) || StringUtils.isBlank(configuration.haId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The configuration contains an error. Please fill in all mandatory fields.");
            return;
        }

        // check and get appliance profile
        var profile = applianceProfileService.getProfile(configuration.haId);
        if (profile == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Please fetch the appliance profiles from your Home Connect account at http(s)://[YOUROPENHAB]:[YOURPORT]/homeconnectdirect (e.g. http://192.168.178.100:8080/homeconnectdirect).");
            return;
        }

        if (!ThingStatus.OFFLINE.equals(thing.getStatus())) {
            updateStatus(ThingStatus.UNKNOWN);
        }

        scheduler.execute(() -> {
            // initialize deviceDescription and featureMapping service
            try {
                var featureMappingService = new FeatureMappingService(Path.of(BINDING_PROFILES_PATH, profile.featureMappingFileName()));
                this.deviceDescriptionService = new DeviceDescriptionService(thing.getUID().getId(), Path.of(BINDING_PROFILES_PATH, profile.deviceDescriptionFileName()), featureMappingService.getFeatureMapping());
                this.featureMappingService = featureMappingService;
            } catch (ParseException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Could not parse profile XML: " + e.getMessage());
                logger.error("Could not parse profile XML! error={}", e.getMessage(), e);
                scheduleReconnect();
                return;
            }

            initializeAllStates();
            initializeStarted();

            try {
                if (profile.credentials() instanceof AesCredentials(String key, String iv)) {
                    URI uri = URI.create(WS_AES_URI_TEMPLATE.formatted(configuration.address));
                    var webSocketClientService = new WebSocketAesClientService(getThing(), uri, key, iv, this,
                            scheduler);
                    this.webSocketClientService = webSocketClientService;
                    webSocketClientService.connect();
                } else if (profile.credentials() instanceof TlsCredentials(String key)) {
                    try {
                        URI uri = URI.create(WS_TLS_URI_TEMPLATE.formatted(configuration.address));
                        var webSocketClientService = new WebSocketTlsConscryptClientService(getThing(), uri,
                                key, this, scheduler);
                        this.webSocketClientService = webSocketClientService;
                        webSocketClientService.connect();
                    } catch (Error e) {
                        if (isUnsatisfiedLinkError(e)) {
                            if (isLinux()) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED,
                                        "The running system (%s %s) does not support secure Web Socket connections. A GNU C library (glibc) of %s or higher is required. Please verify this by running 'ldd --version'. Supported supported operating systems: %s Error: %s".formatted(getOSName(), getOSArch(), CONSCRYPT_REQUIRED_GLIBC_MIN_VERSION, CONSCRYPT_SUPPORTED_SYSTEMS, e.getMessage()));
                            } else {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED,
                                        "The running system (%s %s) does not support secure Web Socket connections. Supported supported operating systems: %s Error: %s".formatted(getOSName(), getOSArch(), CONSCRYPT_SUPPORTED_SYSTEMS, e.getMessage()));
                            }
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED,
                                    "The running system (%s %s) does not support secure Web Socket connections. Supported supported operating systems: %sError: %s".formatted(getOSName(), getOSArch(), CONSCRYPT_SUPPORTED_SYSTEMS, e.getMessage()));
                        }
                        logger.error("Could not initialize {}!", WebSocketTlsConscryptClientService.class.getName(), e);
                    }
                }
            } catch (WebSocketClientServiceException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                scheduleReconnect();
                stopUpdateAllValuesFuture();
            }
            initializeFinished();
        });

    }

    protected void initializeStarted() {
        // allow child's to overwrite
    }

    protected void initializeFinished() {
        // allow child's to overwrite
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // only refresh once every 2 seconds
            synchronized (this) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRefreshExecutionTime >= 2000) {
                    lastRefreshExecutionTime = currentTime;
                    sendGet(RO_ALL_MANDATORY_VALUES);
                }
            }
        } else if (CHANNEL_COMMAND.equals(channelUID.getId()) && command instanceof StringType) {
            sendBooleanCommandIfAllowed(command.toFullString());
        } else if (CHANNEL_RAW_MESSAGE.equals(channelUID.getId()) && command instanceof StringType) {
            try {
                var request = gson.fromJson(command.toFullString(), SendMessageRequest.class);
                if (request != null) {
                    send(request);
                }
            } catch (JsonSyntaxException e) {
                logger.warn("Could not send raw message. Invalid JSON syntax: {}", e.getMessage());
            }
        } else if (CHANNEL_POWER_STATE.equals(channelUID.getId()) && command instanceof OnOffType) {
            mapSettingKey(POWER_STATE_KEY).ifPresent(settingUid -> {
                Optional<Integer> value;
                if (OnOffType.ON.equals(command)) {
                    value = mapEnumerationValueKey(POWER_STATE_ENUM_KEY, STATE_ON);
                } else {
                    value = mapEnumerationValueKey(POWER_STATE_ENUM_KEY, STATE_OFF)
                            .or(() -> mapEnumerationValueKey(POWER_STATE_ENUM_KEY, STATE_MAINS_OFF))
                            .or(() -> mapEnumerationValueKey(POWER_STATE_ENUM_KEY, STATE_STANDBY));
                }

                value.ifPresent(
                        integer -> send(Action.POST, RO_VALUES, List.of(new ValueData(settingUid, integer)), null, 1));
            });
        } else if (CHANNEL_CHILD_LOCK.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanSettingIfAllowed(command, CHILD_LOCK_KEY);
        } else if (CHANNEL_PROGRAM_COMMAND.equals(channelUID.getId()) && command instanceof StringType) {
            if (COMMAND_START.equalsIgnoreCase(command.toFullString())) {
                var selectedProgram = keyValueStore.get(SELECTED_PROGRAM_KEY);
                if (selectedProgram != null) {
                    getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                        if (deviceDescriptionService.getActiveProgram(true) != null) {
                            mapProgramKey(selectedProgram).ifPresent(programUid -> send(Action.POST, RO_ACTIVE_PROGRAM,
                                    List.of(new ProgramData(programUid, null)), null, 1));
                        } else {
                            logger.info("The '{}' control is either unavailable or in read-only mode. Cannot start program.", ACTIVE_PROGRAM_KEY);
                        }
                    });

                }
            } else if (COMMAND_PAUSE.equalsIgnoreCase(command.toFullString())) {
                sendBooleanCommandIfAllowed(PAUSE_PROGRAM_KEY);
            } else if (COMMAND_RESUME.equalsIgnoreCase(command.toFullString())) {
                sendBooleanCommandIfAllowed(RESUME_PROGRAM_KEY);
            }
        } else if (CHANNEL_SELECTED_PROGRAM.equals(channelUID.getId()) && command instanceof StringType) {
            getDeviceDescriptionServiceOptional().ifPresent(deviceDescription -> {
                if (deviceDescription.getSelectedProgram(true) != null) {
                    mapProgramKey(command.toFullString()).ifPresent(selectedProgramUid -> send(Action.POST,
                            RO_SELECTED_PROGRAM, List.of(new ProgramData(selectedProgramUid, null)), null, 1));
                } else {
                    logger.info("The '{}' control is either unavailable or in read-only mode. Cannot change selected program.", SELECTED_PROGRAM_KEY);
                }
            });
        } else {
            // custom channels
            getLinkedChannel(channelUID.getId())
                    .filter(channel -> CHANNEL_TYPE_SWITCH_VALUE.equals(channel.getChannelTypeUID())
                            || CHANNEL_TYPE_STRING_VALUE.equals(channel.getChannelTypeUID())
                            || CHANNEL_TYPE_NUMBER_VALUE.equals(channel.getChannelTypeUID()))
                    .filter(channel -> channel.getConfiguration().containsKey(CONFIGURATION_VALUE_KEY))
                    .ifPresent(channel -> {
                        var valueKey = channel.getConfiguration().get(CONFIGURATION_VALUE_KEY).toString();

                        var deviceDescriptionService = getDeviceDescriptionService();
                        if (deviceDescriptionService != null) {
                            boolean isWritable = deviceDescriptionService.isSettingAvailableAndWritable(valueKey)
                                    || deviceDescriptionService.isOptionAvailableAndWritable(valueKey)
                                    || deviceDescriptionService.isCommandAvailableAndWritable(valueKey);

                            if (!isWritable) {
                                logger.info("The custom channel '{}' with key '{}' is either unavailable or in read-only mode. Command '{}' cannot be processed.",
                                        channelUID.getId(), valueKey, command.toFullString());
                                return;
                            }
                        }

                        mapKey(valueKey).ifPresent(uid -> {
                            switch (command) {
                                case OnOffType onOffTypeCommand when CHANNEL_TYPE_SWITCH_VALUE.equals(channel.getChannelTypeUID()) ->
                                        send(Action.POST, RO_VALUES, List.of(new ValueData(uid, OnOffType.ON.equals(onOffTypeCommand))),
                                                null, 1);
                                case StringType stringTypeCommand when CHANNEL_TYPE_STRING_VALUE.equals(channel.getChannelTypeUID()) ->
                                        mapEnumerationValueKeyToValue(uid, stringTypeCommand.toFullString()).ifPresentOrElse(
                                                enumerationValue -> send(Action.POST, RO_VALUES,
                                                        List.of(new ValueData(uid, enumerationValue)), null, 1),
                                                () -> send(Action.POST, RO_VALUES,
                                                        List.of(new ValueData(uid, command.toFullString())), null, 1));
                                case Number numberCommand when CHANNEL_TYPE_NUMBER_VALUE.equals(channel.getChannelTypeUID()) -> {
                                    Unit<?> unit = null;
                                    if (channel.getConfiguration()
                                            .get(CONFIGURATION_UNIT_KEY) instanceof String unitConfiguration) {
                                        unit = UnitUtils.parseUnit(unitConfiguration);
                                    }
                                    if (PERCENT.equals(unit)) {
                                        double value = numberCommand.doubleValue();

                                        // percent value
                                        if (value > 0 && value < 1) {
                                            value *= 100;
                                        }
                                        send(Action.POST, RO_VALUES, List.of(new ValueData(uid, (int) value)), null, 1);
                                    } else {
                                        send(Action.POST, RO_VALUES, List.of(new ValueData(uid, numberCommand.intValue())), null,
                                                1);
                                    }
                                }
                                default -> {
                                    // noop
                                }
                            }
                        });
                    });
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        initializeState(channelUID.getId());
    }

    @Override
    public void dispose() {
        disposeInitialized.set(true);
        var webSocketClientService = this.webSocketClientService;
        if (webSocketClientService != null) {
            webSocketClientService.dispose();
        }
        stopReconnectSchedule();
        stopUpdateAllValuesFuture();
        applianceMessageConsumers.clear();
    }

    @Override
    public void onWebSocketConnect() {
        updateStatus(ThingStatus.ONLINE);
        logger.debug("WebSocket connection opened (thingUID={}).", thing.getUID());
    }

    @Override
    public void onWebSocketMessage(String rawMessage, WebSocketClientService websocketClientService) {
        Message message = gson.fromJson(rawMessage, Message.class);

        if (message != null) {
            switch (message.action()) {
                case POST -> {
                    if (EI_INITIAL_VALUES.equals(message.resource())) {
                        var firstMessageIdData = message.getDataAsList(FirstMessageIdData.class);
                        if (firstMessageIdData != null && firstMessageIdData.size() == 1) {
                            sessionId = message.sessionId();
                            outgoingMessageId = firstMessageIdData.getFirst().messageId();

                            // reply
                            var deviceType = message.version() == 1 ? WS_DEVICE_TYPE_APPLICATION_V1
                                    : WS_DEVICE_TYPE_APPLICATION_V2;
                            var data = new DeviceData(deviceType, WS_DEVICE_NAME, deviceId);
                            send(RESPONSE, message.resource(), List.of(data), message.messageId(), message.version());

                            // get services
                            sendGet(CI_SERVICES);
                        }
                    } else {
                        logger.warn("Unknown resource! message={} thingUID={}", message, thing.getUID());
                    }
                }
                case RESPONSE, NOTIFY -> {
                    if (CI_SERVICES.equals(message.resource())) {
                        services.clear();

                        if (message.code() == null) {
                            var serviceData = message.getDataAsList(ServiceData.class);
                            if (serviceData != null) {
                                services.addAll(Objects.requireNonNull(serviceData));
                            }
                        }

                        // authenticate (needed by washer)
                        sendGet(CI_AUTHENTICATION, List.of(Map.of("nonce", generateNonce())));

                        // needed by some services
                        sendNotify(EI_DEVICE_READY);

                        // get device info
                        services.forEach(s -> {
                            switch (s.service()) {
                                case CI -> {
                                    sendGet(CI_INFO);
                                    sendGet(CI_TZ_INFO);
                                }
                                case IZ -> sendGet(IZ_INFO);
                                case NI -> sendGet(NI_INFO);
                            }
                        });

                        // get appliance info
                        sendGet(RO_ALL_MANDATORY_VALUES);
                        sendGet(RO_ALL_DESCRIPTION_CHANGES);
                    } else if (message.code() != null) {
                        logger.trace("Received message: resource={} code={} thingUID={}", message.resource(),
                                message.code(), thing.getUID());
                    } else if (IZ_INFO.equals(message.resource()) || CI_INFO.equals(message.resource())) {
                        if (logger.isDebugEnabled()) {
                            var applianceInfoData = message.getDataAsList(ApplianceInfoData.class);
                            if (applianceInfoData != null) {
                                applianceInfoData.forEach(applianceInfo -> logger.debug(
                                        "Received appliance info: {} (thingUID={})", applianceInfo, thing.getUID()));
                            }
                        }
                    } else if (RO_ALL_MANDATORY_VALUES.equals(message.resource())
                            || RO_VALUES.equals(message.resource())) {
                        if (logger.isDebugEnabled()) {
                            var valueData = message.getDataAsList(ValueData.class);
                            if (valueData != null) {
                                logger.debug("Received appliance value update: {} (thingUID={})", valueData,
                                        thing.getUID());
                                scheduleUpdateAllValuesFuture();
                            }
                        }
                    } else if (RO_ALL_DESCRIPTION_CHANGES.equals(message.resource())
                            || RO_DESCRIPTION_CHANGE.equals(message.resource())) {
                        if (logger.isDebugEnabled()) {
                            var valueData = message.getDataAsList(DescriptionChangeData.class);
                            if (valueData != null) {
                                logger.debug("Received appliance description change: {} (thingUID={})", valueData,
                                        thing.getUID());
                                scheduleUpdateAllValuesFuture();
                            }
                        }
                    }
                }
                case GET -> logger.trace("Received message: {} ({})", message, thing.getUID());
            }

            // parse value and device description change messages
            var applianceMessage = mapApplianceMessage(message, true);
            applianceMessages.add(applianceMessage);
            applianceMessageConsumers.forEach(consumer -> consumer.accept(applianceMessage));
            var values = applianceMessage.values();
            if (values != null) {
                values.forEach(value -> onApplianceValueEvent(value, message.resource()));
            }
            var deviceDescriptionChanges = applianceMessage.descriptionChanges();
            if (deviceDescriptionChanges != null) {
                onApplianceDescriptionChangeEvent(deviceDescriptionChanges);
            }
        }
    }

    @Override
    public void onWebSocketClose() {
        logger.debug("WebSocket closed (thingUID={})!", thing.getUID());
        stopUpdateAllValuesFuture();

        var connectionError = this.connectionError;
        if (connectionError != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, connectionError);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }

        // dispose websocket
        var webSocketClientService = this.webSocketClientService;
        if (webSocketClientService != null) {
            scheduler.schedule(webSocketClientService::dispose, 2, TimeUnit.SECONDS);
            this.webSocketClientService = null;
        }

        scheduleReconnect();
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        logger.debug("WebSocket error: {} (thingUID={})", throwable.getMessage(), thing.getUID());
        this.connectionError = throwable.getMessage();
    }

    public List<ApplianceMessage> getApplianceMessages() {
        return applianceMessages.getAllElements();
    }

    public @Nullable DeviceDescriptionService getDeviceDescriptionService() {
        return deviceDescriptionService;
    }

    public Optional<DeviceDescriptionService> getDeviceDescriptionServiceOptional() {
        return Optional.ofNullable(deviceDescriptionService);
    }

    public void registerApplianceMessageListener(Consumer<ApplianceMessage> consumer) {
        applianceMessageConsumers.add(consumer);
    }

    public void removeApplianceMessageListener(Consumer<ApplianceMessage> consumer) {
        applianceMessageConsumers.remove(consumer);
    }

    protected void onApplianceDescriptionChangeEvent(List<DeviceDescriptionChange> deviceDescriptionChanges) {
        deviceDescriptionChanges.forEach(deviceDescriptionChange -> {
            // check for command changes
            if (DeviceDescriptionType.COMMAND.equals(deviceDescriptionChange.type())
                    || DeviceDescriptionType.COMMAND_LIST.equals(deviceDescriptionChange.type())) {
                updateCommandDescriptions();
            } else if (DeviceDescriptionType.SELECTED_PROGRAM.equals(deviceDescriptionChange.type())) {
                updateSelectedProgramDescription();
            } else if (DeviceDescriptionType.ACTIVE_PROGRAM.equals(deviceDescriptionChange.type())) {
                updateActiveProgramDescription();
            } else if (OPERATION_STATE_KEY.equals(deviceDescriptionChange.key())) {
                updateStatusDescriptionIfLinked(CHANNEL_OPERATION_STATE, OPERATION_STATE_KEY);
            } else if (PROGRAM_PROGRESS_KEY.equals(deviceDescriptionChange.key())) {
                getLinkedChannel(CHANNEL_PROGRAM_PROGRESS).ifPresent(
                        channel -> getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                            if (deviceDescriptionService.isOptionAvailableAndReadable(CHANNEL_PROGRAM_PROGRESS)) {
                                updateState(channel.getUID(), new QuantityType<>(0, PERCENT));
                            }
                        }));
            } else if (REMAINING_PROGRAM_TIME_KEY.equals(deviceDescriptionChange.key())) {
                getLinkedChannel(CHANNEL_REMAINING_PROGRAM_TIME).ifPresent(
                        channel -> getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                            if (!deviceDescriptionService.isOptionAvailableAndReadable(REMAINING_PROGRAM_TIME_KEY)) {
                                updateState(channel.getUID(), new QuantityType<>(0, SECOND));
                            }
                        }));
            }

            // update custom device description channels
            getThing().getChannels().stream()
                    .filter(channel -> CHANNEL_TYPE_SWITCH_DESCRIPTION.equals(channel.getChannelTypeUID())
                            || CHANNEL_TYPE_STRING_DESCRIPTION.equals(channel.getChannelTypeUID())
                            || CHANNEL_TYPE_NUMBER_DESCRIPTION.equals(channel.getChannelTypeUID()))
                    .filter(channel -> isLinked(channel.getUID())).forEach(channel -> {
                        var configuration = channel.getConfiguration();
                        var descriptionKeyConfiguration = configuration.get(CONFIGURATION_DESCRIPTION_KEY);
                        var attributeConfiguration = configuration.get(CONFIGURATION_ATTRIBUTE);

                        if (descriptionKeyConfiguration instanceof String key
                                && attributeConfiguration instanceof String attribute
                                && key.equals(deviceDescriptionChange.key())) {
                            var changes = deviceDescriptionChange.changes();
                            if (changes != null && changes.containsKey(attribute)) {
                                var change = changes.get(attribute);
                                var newValue = change == null ? null : change.to();

                                if (newValue == null) {
                                    updateState(channel.getUID(), UnDefType.UNDEF);
                                } else if (CHANNEL_TYPE_SWITCH_DESCRIPTION.equals(channel.getChannelTypeUID())) {
                                    if (newValue instanceof Boolean b) {
                                        updateState(channel.getUID(), OnOffType.from(b));
                                    }
                                } else if (CHANNEL_TYPE_STRING_DESCRIPTION.equals(channel.getChannelTypeUID())) {
                                    updateState(channel.getUID(), StringType.valueOf(newValue.toString()));
                                } else if (CHANNEL_TYPE_NUMBER_DESCRIPTION.equals(channel.getChannelTypeUID())) {
                                    updateState(channel.getUID(), new DecimalType(newValue.toString()));
                                }
                            }
                        }
                    });
        });
    }

    protected void onApplianceValueEvent(Value value, Resource resource) {
        switch (value.key()) {
            case POWER_STATE_KEY -> updateStateIfLinked(CHANNEL_POWER_STATE,
                    () -> OnOffType.from(STATE_ON.equalsIgnoreCase(value.getValueAsString())));
            case DOOR_STATE_KEY -> updateStateIfLinked(CHANNEL_DOOR,
                    () -> STATE_OPEN.equals(value.value()) || STATE_AJAR.equals(value.value()) ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            case OPERATION_STATE_KEY -> {
                var oldOperationState = keyValueStore.get(OPERATION_STATE_KEY);
                var newOperationState = value.getValueAsString();
                keyValueStore.put(OPERATION_STATE_KEY, newOperationState);

                if (STATE_FINISHED.equals(oldOperationState) && !STATE_FINISHED.equals(newOperationState)) {
                    getLinkedChannel(CHANNEL_PROGRAM_PROGRESS)
                            .ifPresent(channel -> updateState(channel.getUID(), new QuantityType<>(0, PERCENT)));
                }

                if (STATE_FINISHED.equals(value.getValueAsString())) {
                    getLinkedChannel(CHANNEL_PROGRAM_PROGRESS)
                            .ifPresent(channel -> updateState(channel.getUID(), new QuantityType<>(100, PERCENT)));
                    getLinkedChannel(CHANNEL_REMAINING_PROGRAM_TIME)
                            .ifPresent(channel -> updateState(channel.getUID(), new QuantityType<>(0, SECOND)));
                }
                getLinkedChannel(CHANNEL_OPERATION_STATE)
                        .ifPresent(channel -> updateState(channel.getUID(), new StringType(value.getValueAsString())));
            }
            case REMOTE_CONTROL_START_ALLOWED_KEY -> updateStateIfLinked(CHANNEL_REMOTE_CONTROL_OR_START_ALLOWED,
                    () -> OnOffType.from(value.getValueAsBoolean()));
            case SELECTED_PROGRAM_KEY -> {
                keyValueStore.put(SELECTED_PROGRAM_KEY, value.getValueAsString());
                updateStateIfLinked(CHANNEL_SELECTED_PROGRAM,
                        () -> STATE_NO_PROGRAM.equals(value.getValueAsString()) ? UnDefType.UNDEF
                                : new StringType(value.getValueAsString()));
            }
            case ACTIVE_PROGRAM_KEY -> {
                keyValueStore.put(ACTIVE_PROGRAM_KEY, value.getValueAsString());
                getLinkedChannel(CHANNEL_ACTIVE_PROGRAM).ifPresent(channel -> {
                    if (STATE_NO_PROGRAM.equals(value.getValueAsString())) {
                        updateState(channel.getUID(), UnDefType.UNDEF);
                    } else {
                        updateState(channel.getUID(), new StringType(value.getValueAsString()));
                    }
                });
            }
            case REMAINING_PROGRAM_TIME_KEY -> updateStateIfLinked(CHANNEL_REMAINING_PROGRAM_TIME,
                    () -> new QuantityType<>(value.getValueAsInt(), SECOND));
            case PROGRAM_PROGRESS_KEY ->
                updateStateIfLinked(CHANNEL_PROGRAM_PROGRESS, () -> new QuantityType<>(value.getValueAsInt(), PERCENT));
            case CHILD_LOCK_KEY ->
                updateStateIfLinked(CHANNEL_CHILD_LOCK, () -> OnOffType.from(value.getValueAsBoolean()));
        }

        // fetch favorite program name
        var matcher = FAVORITE_NAME_PATTERN.matcher(value.key());
        if (matcher.matches()) {
            var favoriteNumber = Integer.parseInt(matcher.group(1));
            var favoriteKey = String.format(PROGRAM_FAVORITE_KEY_TEMPLATE, favoriteNumber);
            var favoriteName = value.getValueAsString();
            if (StringUtils.isNotBlank(favoriteName)) {
                favoriteNames.put(favoriteKey, favoriteName);
            } else {
                favoriteNames.remove(favoriteKey);
            }
        }

        // update custom value channels
        getThing().getChannels().stream()
                .filter(channel -> CHANNEL_TYPE_SWITCH_VALUE.equals(channel.getChannelTypeUID())
                        || CHANNEL_TYPE_STRING_VALUE.equals(channel.getChannelTypeUID())
                        || CHANNEL_TYPE_NUMBER_VALUE.equals(channel.getChannelTypeUID())
                        || CHANNEL_TYPE_TRIGGER_VALUE.equals(channel.getChannelTypeUID())
                        || CHANNEL_TYPE_ENUM_SWITCH_VALUE.equals(channel.getChannelTypeUID()))
                .filter(channel -> channel.getConfiguration().containsKey(CONFIGURATION_VALUE_KEY))
                .filter(channel -> value.key().equals(channel.getConfiguration().get(CONFIGURATION_VALUE_KEY)))
                .filter(channel -> CHANNEL_TYPE_TRIGGER_VALUE.equals(channel.getChannelTypeUID())
                        || isLinked(channel.getUID()))
                .forEach(channel -> {
                    if (CHANNEL_TYPE_SWITCH_VALUE.equals(channel.getChannelTypeUID())) {
                        updateState(channel.getUID(), OnOffType.from(value.getValueAsBoolean()));
                    } else if (CHANNEL_TYPE_STRING_VALUE.equals(channel.getChannelTypeUID())) {
                        updateState(channel.getUID(), StringType.valueOf(value.getValueAsString()));
                    } else if (CHANNEL_TYPE_NUMBER_VALUE.equals(channel.getChannelTypeUID())) {
                        Unit<?> unit = null;
                        if (channel.getConfiguration()
                                .get(CONFIGURATION_UNIT_KEY) instanceof String unitConfiguration) {
                            unit = UnitUtils.parseUnit(unitConfiguration);
                        }
                        if (unit != null) {
                            updateState(channel.getUID(),
                                    new QuantityType<>(new BigDecimal(value.getValueAsString()), unit));
                        } else {
                            updateState(channel.getUID(), new DecimalType(value.getValueAsString()));
                        }
                    } else if (CHANNEL_TYPE_TRIGGER_VALUE.equals(channel.getChannelTypeUID())
                            && RO_VALUES.equals(resource)) {
                        triggerChannel(channel.getUID(), value.getValueAsString());
                    } else if (CHANNEL_TYPE_ENUM_SWITCH_VALUE.equals(channel.getChannelTypeUID())) {
                        var onValueConfig = channel.getConfiguration().get(CONFIGURATION_ON_VALUE_KEY);
                        if (onValueConfig != null) {
                            var onValues = Arrays.stream(onValueConfig.toString().split(",")).map(String::trim)
                                    .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
                            updateState(channel.getUID(), OnOffType.from(
                                    onValues.stream().anyMatch(v -> v.equalsIgnoreCase(value.getValueAsString()))));
                        }
                    }
                });
    }

    protected Optional<Channel> getLinkedChannel(String channelId) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null || !isLinked(channelId)) {
            return Optional.empty();
        } else {
            return Optional.of(channel);
        }
    }

    protected void updateStateIfLinked(String channelID, State state) {
        getLinkedChannel(channelID).ifPresent(channel -> updateState(channel.getUID(), state));
    }

    protected void updateStateIfLinked(String channelID, Supplier<State> stateSupplier) {
        getLinkedChannel(channelID).ifPresent(channel -> updateState(channel.getUID(), stateSupplier.get()));
    }

    public void sendGet(Resource resource) {
        sendGet(resource, null);
    }

    public void sendGet(Resource resource, @Nullable List<Object> data) {
        send(GET, resource, data, null, null);
    }

    public void sendNotify(Resource resource) {
        send(NOTIFY, resource, null, null, null);
    }

    public void send(SendMessageRequest request) {
        List<Object> data = null;
        var requestData = request.data();
        if (requestData != null) {
            if (requestData instanceof List<?> list) {
                data = new ArrayList<>(list);
            } else {
                data = new ArrayList<>();
                data.add(requestData);
            }
        }

        send(request.action(), request.resource(), data, null, request.version());
    }

    public void send(Action action, Resource resource, @Nullable List<Object> data, @Nullable Long messageId,
            @Nullable Integer versionObject) {
        int version;
        if (versionObject != null) {
            version = versionObject;
        } else {
            var latestVersion = services.stream().filter(s -> s.service().equals(resource.service())).findFirst()
                    .map(ServiceData::version).orElse(null);
            version = Objects.requireNonNullElse(latestVersion, 1);
        }

        // special case GET services
        if (GET.equals(action) && CI_SERVICES.equals(resource)) {
            version = 1;
        }

        long msgId = Objects.requireNonNullElseGet(messageId, () -> outgoingMessageId++);

        JsonArray dataList = null;
        if (data != null) {
            dataList = new JsonArray();
            data.stream().map(gson::toJsonTree).forEach(dataList::add);
        }
        var message = new Message(sessionId, msgId, resource, version, action, null, dataList);
        var rawMessage = gson.toJson(message);
        var webSocketClientService = this.webSocketClientService;
        if (webSocketClientService != null) {
            webSocketClientService.send(rawMessage);
        }

        var applianceMessage = mapApplianceMessage(message, false);
        applianceMessages.add(applianceMessage);
        applianceMessageConsumers.forEach(consumer -> consumer.accept(applianceMessage));
    }

    protected void sendBooleanCommandIfAllowed(String commandKey) {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            if (deviceDescriptionService.isCommandAvailableAndWritable(commandKey)) {
                mapCommandKey(commandKey).ifPresent(
                        commandUid -> send(Action.POST, RO_VALUES, List.of(new ValueData(commandUid, true)), null, 1));
            } else {
                logger.info(
                        "The boolean command '{}' is either unavailable or in read-only mode. Command cannot be processed.",
                        commandKey);
            }
        });
    }

    protected void sendBooleanSettingIfAllowed(Command command, String settingKey) {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            if (deviceDescriptionService.isSettingAvailableAndWritable(settingKey)) {
                mapSettingKey(settingKey).ifPresent(settingUid -> send(Action.POST, RO_VALUES,
                        List.of(new ValueData(settingUid, OnOffType.ON.equals(command))), null, 1));
            } else {
                logger.info(
                        "The boolean setting '{}' is either unavailable or in read-only mode. Command '{}' cannot be processed.",
                        settingKey, command.toFullString());
            }
        });
    }

    protected void sendBooleanOptionIfAllowed(Command command, String optionKey) {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            if (deviceDescriptionService.isOptionAvailableAndWritable(optionKey)) {
                mapOptionKey(optionKey).ifPresent(optionUid -> send(Action.POST, RO_VALUES,
                        List.of(new ValueData(optionUid, OnOffType.ON.equals(command))), null, 1));
            } else {
                logger.info(
                        "The boolean option '{}' is either unavailable or in read-only mode. Command '{}' cannot be processed.",
                        optionKey, command.toFullString());
            }
        });
    }

    protected void sendIntegerOptionIfAllowed(QuantityType<?> command, String optionKey) {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            if (deviceDescriptionService.isOptionAvailableAndWritable(optionKey)) {
                mapOptionKey(optionKey).ifPresent(optionUid -> send(Action.POST, RO_VALUES,
                        List.of(new ValueData(optionUid, command.intValue())), null, 1));
            } else {
                logger.info(
                        "The integer option '{}' is either unavailable or in read-only mode. Command '{}' cannot be processed.",
                        optionKey, command.toFullString());
            }
        });
    }

    protected void sendIntegerSettingIfAllowed(QuantityType<?> command, String settingKey) {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            if (deviceDescriptionService.isSettingAvailableAndWritable(settingKey)) {
                mapSettingKey(settingKey).ifPresent(optionUid -> send(Action.POST, RO_VALUES,
                        List.of(new ValueData(optionUid, command.intValue())), null, 1));
            } else {
                logger.info(
                        "The integer setting '{}' is either unavailable or in read-only mode. Command '{}' cannot be processed.",
                        settingKey, command.toFullString());
            }
        });
    }

    protected void sendEnumSettingIfAllowed(Command command, String settingKey) {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            if (deviceDescriptionService.isSettingAvailableAndWritable(settingKey)) {
                var setting = deviceDescriptionService.findSettingByKey(settingKey);
                if (setting != null) {
                    var enumerationTypeKey = setting.enumerationTypeKey();
                    if (enumerationTypeKey != null) {
                        mapEnumerationValueKey(enumerationTypeKey, command.toFullString())
                                .ifPresent(enumValue -> send(Action.POST, RO_VALUES,
                                        List.of(new ValueData(setting.uid(), enumValue)), null, 1));
                    }
                }
            } else {
                logger.info(
                        "The enumeration setting '{}' is either unavailable or in read-only mode. Command '{}' cannot be processed.",
                        settingKey, command.toFullString());
            }
        });
    }

    protected void sendEnumOptionIfAllowed(Command command, String optionKey) {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            if (deviceDescriptionService.isOptionAvailableAndWritable(optionKey)) {
                var option = deviceDescriptionService.findOptionByKey(optionKey);
                if (option != null) {
                    var enumerationTypeKey = option.enumerationTypeKey();
                    if (enumerationTypeKey != null) {
                        mapEnumerationValueKey(enumerationTypeKey, command.toFullString())
                                .ifPresent(enumValue -> send(Action.POST, RO_VALUES,
                                        List.of(new ValueData(option.uid(), enumValue)), null, 1));
                    }
                }
            } else {
                logger.info(
                        "The enumeration option '{}' is either unavailable or in read-only mode. Command '{}' cannot be processed.",
                        optionKey, command.toFullString());
            }
        });
    }

    protected Optional<Integer> mapStatusKey(String statusKey) {
        return getDeviceDescriptionServiceOptional()
                .map(deviceDescriptionService -> deviceDescriptionService.mapStatusKey(statusKey));
    }

    protected Optional<Integer> mapSettingKey(String settingKey) {
        return getDeviceDescriptionServiceOptional()
                .map(deviceDescriptionService -> deviceDescriptionService.mapSettingKey(settingKey));
    }

    protected Optional<Integer> mapEventKey(String eventKey) {
        return getDeviceDescriptionServiceOptional()
                .map(deviceDescriptionService -> deviceDescriptionService.mapEventKey(eventKey));
    }

    protected Optional<Integer> mapCommandKey(String commandKey) {
        return getDeviceDescriptionServiceOptional()
                .map(deviceDescriptionService -> deviceDescriptionService.mapCommandKey(commandKey));
    }

    protected Optional<Integer> mapOptionKey(String optionKey) {
        return getDeviceDescriptionServiceOptional()
                .map(deviceDescriptionService -> deviceDescriptionService.mapOptionKey(optionKey));
    }

    protected Optional<Integer> mapProgramKey(String programKey) {
        return getDeviceDescriptionServiceOptional()
                .map(deviceDescriptionService -> deviceDescriptionService.mapProgramKey(programKey));
    }

    private Optional<Integer> mapActiveProgramKey(String activeProgramKey) {
        return getDeviceDescriptionServiceOptional()
                .map(deviceDescriptionService -> deviceDescriptionService.mapActiveProgramKey(activeProgramKey));
    }

    private Optional<Integer> mapSelectedProgramKey(String selectedProgramKey) {
        return getDeviceDescriptionServiceOptional()
                .map(deviceDescriptionService -> deviceDescriptionService.mapSelectedProgramKey(selectedProgramKey));
    }

    private Optional<Integer> mapProtectionPortKey(String protectionPortKey) {
        return getDeviceDescriptionServiceOptional()
                .map(deviceDescriptionService -> deviceDescriptionService.mapProtectionPortKey(protectionPortKey));
    }

    protected Optional<Integer> mapKey(String key) {
        return mapStatusKey(key).or(() -> mapSettingKey(key)).or(() -> mapEventKey(key)).or(() -> mapCommandKey(key))
                .or(() -> mapOptionKey(key)).or(() -> mapProgramKey(key)).or(() -> mapActiveProgramKey(key))
                .or(() -> mapSelectedProgramKey(key)).or(() -> mapProtectionPortKey(key));
    }

    protected Optional<Integer> mapEnumerationValueKeyToValue(int deviceDescriptionObjectUid, String valueKey) {
        var deviceDescriptionService = this.deviceDescriptionService;
        if (deviceDescriptionService == null) {
            return Optional.empty();
        }

        var foundObject = deviceDescriptionService.getDeviceDescriptionObject(deviceDescriptionObjectUid);
        if (foundObject == null) {
            return Optional.empty();
        }

        if (foundObject.object() instanceof EnumerationTypeProvider enumerationTypeProvider) {
            var enumerationTypeKey = enumerationTypeProvider.enumerationTypeKey();
            if (enumerationTypeKey != null) {
                return Optional
                        .ofNullable(deviceDescriptionService.mapEnumerationValueKey(enumerationTypeKey, valueKey));
            }
        }
        return Optional.empty();
    }

    protected Optional<Integer> mapEnumerationValueKey(String enumerationTypeKey, String valueKey) {
        return getDeviceDescriptionServiceOptional().map(deviceDescriptionService -> deviceDescriptionService
                .mapEnumerationValueKey(enumerationTypeKey, valueKey));
    }

    private String generateNonce() {
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    private synchronized void scheduleReconnect() {
        var reconnectFuture = this.reconnectFuture;

        if ((reconnectFuture == null || reconnectFuture.isCancelled() || reconnectFuture.isDone())
                && !disposeInitialized.get()) {
            var configuration = this.configuration;
            int delay = 1;
            if (configuration != null) {
                delay = configuration.connectionRetryDelay;
            }
            logger.trace("Schedule reconnect in {} minute(s) ({}).", delay, thing.getUID());
            this.reconnectFuture = scheduler.schedule(this::initialize, delay, TimeUnit.MINUTES);
        }
    }

    private synchronized void stopReconnectSchedule() {
        ScheduledFuture<?> reconnectFuture = this.reconnectFuture;
        if (reconnectFuture != null) {
            reconnectFuture.cancel(true);
        }
    }

    private synchronized void scheduleUpdateAllValuesFuture() {
        var updateValuesFuture = this.updateValuesFuture;

        if ((updateValuesFuture == null || updateValuesFuture.isCancelled() || updateValuesFuture.isDone())
                && !disposeInitialized.get()) {
            logger.trace("Schedule update all mandatory values in {} minute(s) - {} ({}).",
                    UPDATE_ALL_MANDATORY_VALUES_INTERVAL.toMinutes(),
                    LocalDateTime.now(ZONE_ID).plus(UPDATE_ALL_MANDATORY_VALUES_INTERVAL), thing.getUID());
            this.updateValuesFuture = scheduler.schedule(() -> sendGet(RO_ALL_MANDATORY_VALUES),
                    UPDATE_ALL_MANDATORY_VALUES_INTERVAL.toSeconds(), TimeUnit.SECONDS);
        }
    }

    private synchronized void stopUpdateAllValuesFuture() {
        ScheduledFuture<?> updateValuesFuture = this.updateValuesFuture;
        if (updateValuesFuture != null) {
            logger.trace("Cancel schedule to update all mandatory values ({}).", thing.getUID());
            updateValuesFuture.cancel(true);
        }
    }

    private ApplianceMessage mapApplianceMessage(Message message, boolean incoming) {
        var deviceDescriptionService = this.deviceDescriptionService;
        var featureMappingService = this.featureMappingService;

        // handle device description change messages
        List<DeviceDescriptionChange> deviceDescriptionChanges = null;
        List<Value> specialMappedValues = null;
        if (incoming && deviceDescriptionService != null && (RO_DESCRIPTION_CHANGE.equals(message.resource())
                || RO_ALL_DESCRIPTION_CHANGES.equals(message.resource()))) {
            var descriptionChangeData = message.getDataAsList(DescriptionChangeData.class);
            deviceDescriptionChanges = deviceDescriptionService.applyDescriptionChanges(descriptionChangeData);

            // some appliances emit value changes together with description changes
            if (descriptionChangeData != null && featureMappingService != null) {
                var featureMapping = featureMappingService.getFeatureMapping();
                var deviceDescriptionChangeValues = descriptionChangeData.stream()
                        .filter(description -> description.value() != null)
                        .map(description -> new ValueData(description.uid(),
                                Objects.requireNonNull(description.value())))
                        .toList();
                specialMappedValues = DeviceDescriptionUtils.mapValues(deviceDescriptionService, featureMapping,
                        message.resource(), deviceDescriptionChangeValues, getThing().getThingTypeUID());
            }
        }

        // handle value messages
        List<Value> mappedValues = null;
        if (incoming && featureMappingService != null && deviceDescriptionService != null
                && (RO_VALUES.equals(message.resource()) || RO_ALL_MANDATORY_VALUES.equals(message.resource()))) {
            var featureMapping = featureMappingService.getFeatureMapping();
            List<ValueData> valueDataList = message.getDataAsList(ValueData.class);
            mappedValues = DeviceDescriptionUtils.mapValues(deviceDescriptionService, featureMapping,
                    message.resource(), valueDataList, getThing().getThingTypeUID());
        }

        // special case: desciption change and value messages
        List<Value> combinedValues = null;
        if (mappedValues != null || specialMappedValues != null) {
            combinedValues = new ArrayList<>();
            if (mappedValues != null) {
                combinedValues.addAll(mappedValues);
            }
            if (specialMappedValues != null) {
                combinedValues.addAll(specialMappedValues);
            }
        }

        return new ApplianceMessage(OffsetDateTime.now(ZONE_ID), message.messageId(),
                incoming ? MessageType.INCOMING : MessageType.OUTGOING, message.resource(), message.version(),
                message.sessionId(), message.messageId(), message.action(), message.code(), message.data(),
                (combinedValues == null || combinedValues.isEmpty()) ? null : combinedValues, deviceDescriptionChanges);
    }

    private void updateSelectedProgramDescription() {
        getLinkedChannel(CHANNEL_SELECTED_PROGRAM)
                .ifPresent(channel -> getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                    List<CommandOption> programOptions = new ArrayList<>();

                    // check if selectedProgram value is writable
                    if (deviceDescriptionService.getSelectedProgram(true) != null) {
                        deviceDescriptionService.getPrograms(true).stream()
                                .map(command -> new CommandOption(command.key(), getProgramLabel(command.key())))
                                .forEach(programOptions::add);
                    } else {
                        var selectedProgram = keyValueStore.get(SELECTED_PROGRAM_KEY);
                        if (selectedProgram != null) {
                            programOptions.add(new CommandOption(selectedProgram, getProgramLabel(selectedProgram)));
                        }
                    }

                    setCommandOptions(channel.getUID(), programOptions);
                }));
    }

    private void updateActiveProgramDescription() {
        getLinkedChannel(CHANNEL_ACTIVE_PROGRAM)
                .ifPresent(channel -> getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                    var stateOptions = deviceDescriptionService.getPrograms(false).stream()
                            .map(program -> new StateOption(program.key(), getProgramLabel(program.key()))).toList();

                    setStateDescriptions(channel.getUID(), stateOptions);
                }));
    }

    private String getProgramLabel(String programKey) {
        return favoriteNames.getOrDefault(programKey, mapKeyToLabel(programKey, translationProvider));
    }

    private void updateCommandDescriptions() {
        // command channel
        getLinkedChannel(CHANNEL_COMMAND)
                .ifPresent(channel -> getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                    var commands = deviceDescriptionService.getCommands(true, true);

                    // collect all commands of type boolean
                    var commandOptions = commands.stream()
                            .filter(command -> ContentType.BOOLEAN.equals(command.contentType()))
                            .map(command -> new CommandOption(command.key(),
                                    mapKeyToLabel(command.key(), translationProvider)))
                            .toList();

                    setCommandOptions(channel.getUID(), commandOptions);
                }));

        // program command
        getLinkedChannel(CHANNEL_PROGRAM_COMMAND)
                .ifPresent(channel -> getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                    var commands = deviceDescriptionService.getCommands(true, true);

                    // collect all commands of type boolean
                    var commandOptions = commands.stream()
                            .filter(command -> ContentType.BOOLEAN.equals(command.contentType()))
                            .filter(command -> Set.of(RESUME_PROGRAM_KEY, PAUSE_PROGRAM_KEY).contains(command.key()))
                            .map(command -> {
                                var commandOptionCommand = switch (command.key()) {
                                    case RESUME_PROGRAM_KEY -> COMMAND_RESUME;
                                    case PAUSE_PROGRAM_KEY -> COMMAND_PAUSE;
                                    default -> throw new IllegalStateException("Unexpected value: " + command.key());
                                };
                                var commandOptionLabel = switch (command.key()) {
                                    case RESUME_PROGRAM_KEY -> translationProvider.getText(I18N_RESUME_PROGRAM);
                                    case PAUSE_PROGRAM_KEY -> translationProvider.getText(I18N_PAUSE_PROGRAM);
                                    default -> throw new IllegalStateException("Unexpected value: " + command.key());
                                };
                                return new CommandOption(commandOptionCommand, commandOptionLabel);
                            }).toList();
                    if (commandOptions.isEmpty() && deviceDescriptionService.getActiveProgram(true) != null
                            && deviceDescriptionService.getSelectedProgram(true) != null) {
                        commandOptions = List
                                .of(new CommandOption(COMMAND_START, translationProvider.getText(I18N_START_PROGRAM)));
                    }

                    setCommandOptions(channel.getUID(), commandOptions);
                }));
    }

    protected void updateReadonlyEnumOptionDescriptionIfLinked(String channelId, String optionKey) {
        updateOptionDescriptionIfLinked(channelId, optionKey);
    }

    protected void updateEnumOptionDescriptionIfLinked(String channelId, String optionKey) {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            if (deviceDescriptionService.isOptionAvailableAndWritable(optionKey)) {
                updateOptionDescriptionIfLinked(channelId, optionKey);
            } else {
                var enumKey = keyValueStore.get(optionKey);
                if (enumKey != null) {
                    updateOptionDescriptionToFixedValueKeyIfLinked(channelId, enumKey);
                }
            }
        });
    }

    protected void updateOptionDescriptionToFixedValueKeyIfLinked(String optionChannel, String fixedOptionValueKey) {
        getLinkedChannel(optionChannel).ifPresent(channel -> setStateDescriptions(channel.getUID(), List
                .of(new StateOption(fixedOptionValueKey, mapKeyToLabel(fixedOptionValueKey, translationProvider)))));
    }

    protected void updateOptionDescriptionIfLinked(String optionChannel, String optionKey) {
        getLinkedChannel(optionChannel)
                .ifPresent(channel -> getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                    List<StateOption> stateOptions = new ArrayList<>();

                    var deviceOption = deviceDescriptionService.findOptionByKey(optionKey);
                    if (deviceOption != null) {
                        var enumerationType = deviceDescriptionService
                                .findEnumerationType(deviceOption.enumerationType());
                        if (enumerationType != null) {
                            enumerationType.enumerations().entrySet().stream().filter(entry -> {
                                var enumValue = entry.getValue().value();
                                var min = deviceOption.min();
                                var max = deviceOption.max();
                                return (min == null || enumValue >= min.intValue())
                                        && (max == null || enumValue <= max.intValue());
                            }).sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                                var valueKey = entry.getValue().valueKey();
                                var valueKeyLabel = mapKeyToLabel(valueKey, translationProvider);
                                stateOptions.add(new StateOption(valueKey, valueKeyLabel));
                            });
                        }
                    }

                    setStateDescriptions(channel.getUID(), stateOptions);
                }));
    }

    protected void updateStatusDescriptionIfLinked(String optionChannel, String statusKey) {
        getLinkedChannel(optionChannel)
                .ifPresent(channel -> getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                    List<StateOption> stateOptions = new ArrayList<>();

                    var deviceStatus = deviceDescriptionService.findStatusByKey(statusKey);
                    if (deviceStatus != null) {
                        var enumerationType = deviceDescriptionService
                                .findEnumerationType(deviceStatus.enumerationType());
                        if (enumerationType != null) {
                            enumerationType.enumerations().entrySet().stream().filter(entry -> {
                                var enumValue = entry.getValue().value();
                                var min = deviceStatus.min();
                                var max = deviceStatus.max();
                                return (min == null || enumValue >= min.intValue())
                                        && (max == null || enumValue <= max.intValue());
                            }).sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                                var valueKey = entry.getValue().valueKey();
                                var valueKeyLabel = mapKeyToLabel(valueKey, translationProvider);
                                stateOptions.add(new StateOption(valueKey, valueKeyLabel));
                            });
                        }
                    }

                    setStateDescriptions(channel.getUID(), stateOptions);
                }));
    }

    protected void updateIntegerOptionDescriptionIfLinked(String optionChannel, String optionKey) {
        getLinkedChannel(optionChannel)
                .ifPresent(channel -> getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                    var deviceOption = deviceDescriptionService.findOptionByKey(optionKey);
                    if (deviceOption != null) {
                        var builder = StateDescriptionFragmentBuilder.create();
                        var min = deviceOption.min();
                        if (min != null) {
                            builder.withMinimum(new BigDecimal(min.toString()));
                        }
                        var max = deviceOption.max();
                        if (max != null) {
                            builder.withMaximum(new BigDecimal(max.toString()));
                        }
                        var stepSize = deviceOption.stepSize();
                        if (stepSize != null) {
                            builder.withStep(new BigDecimal(stepSize.toString()));
                        }
                        stateDescriptionProvider.setStateDescriptionFragment(channel.getUID(), builder.build());
                    }
                }));
    }

    protected void setCommandOptions(ChannelUID channelUID, List<CommandOption> commandOptions) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting command options for channel {} to {}", channelUID,
                    commandOptions.stream().map(
                            commandOption -> "%s->%s".formatted(commandOption.getCommand(), commandOption.getLabel()))
                            .toList());
        }
        commandDescriptionProvider.setCommandOptions(channelUID, commandOptions);
    }

    protected void setStateDescriptions(ChannelUID channelUID, List<StateOption> stateOptions) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting state descriptions for channel {} to {}", channelUID, stateOptions.stream()
                    .map(stateOption -> "%s->%s".formatted(stateOption.getValue(), stateOption.getLabel())).toList());
        }
        stateDescriptionProvider.setStateOptions(channelUID, stateOptions);
    }

    protected boolean addChannelIfNotExist(ThingBuilder thingBuilder, String channelId, String channelTypeId,
            String itemType) {
        return addChannelIfNotExist(thingBuilder, channelId, channelTypeId, itemType, null);
    }

    protected boolean addChannelIfNotExist(ThingBuilder thingBuilder, String channelId, String channelTypeId,
            String itemType, @Nullable String label) {
        if (getThing().getChannel(channelId) == null) {
            var channelTypeUID = new ChannelTypeUID(BINDING_ID, channelTypeId);
            var channelUID = new ChannelUID(getThing().getUID(), channelId);
            var newChannelBuilder = ChannelBuilder.create(channelUID, itemType).withType(channelTypeUID);

            if (label != null) {
                newChannelBuilder.withLabel(label);
            }
            thingBuilder.withChannel(newChannelBuilder.build());
            return true;
        } else {
            return false;
        }
    }

    protected Map<String, String> getKeyValueStore() {
        return keyValueStore;
    }

    protected HomeConnectDirectTranslationProvider getTranslationProvider() {
        return translationProvider;
    }

    private void initializeAllStates() {
        Set.of(CHANNEL_COMMAND, CHANNEL_SELECTED_PROGRAM, CHANNEL_ACTIVE_PROGRAM, CHANNEL_OPERATION_STATE,
                CHANNEL_CHILD_LOCK, CHANNEL_POWER_STATE, CHANNEL_REMOTE_CONTROL_OR_START_ALLOWED, CHANNEL_DOOR,
                CHANNEL_REMAINING_PROGRAM_TIME, CHANNEL_PROGRAM_PROGRESS).forEach(this::initializeState);

        getThing().getChannels().stream()
                .filter(channel -> CHANNEL_TYPE_SWITCH_DESCRIPTION.equals(channel.getChannelTypeUID())
                        || CHANNEL_TYPE_STRING_DESCRIPTION.equals(channel.getChannelTypeUID())
                        || CHANNEL_TYPE_NUMBER_DESCRIPTION.equals(channel.getChannelTypeUID()))
                .filter(channel -> isLinked(channel.getUID()))
                .forEach(channel -> initializeState(channel.getUID().getId()));
    }

    private void initializeState(String channelId) {
        switch (channelId) {
            case CHANNEL_COMMAND, CHANNEL_PROGRAM_COMMAND -> updateCommandDescriptions();
            case CHANNEL_SELECTED_PROGRAM -> updateSelectedProgramDescription();
            case CHANNEL_ACTIVE_PROGRAM -> updateActiveProgramDescription();
            case CHANNEL_OPERATION_STATE -> updateStatusDescriptionIfLinked(channelId, OPERATION_STATE_KEY);
            case CHANNEL_CHILD_LOCK, CHANNEL_POWER_STATE, CHANNEL_REMOTE_CONTROL_OR_START_ALLOWED ->
                updateStateIfLinked(channelId, OnOffType.OFF);
            case CHANNEL_DOOR -> updateStateIfLinked(channelId, OpenClosedType.CLOSED);
            case CHANNEL_REMAINING_PROGRAM_TIME -> updateStateIfLinked(channelId, new QuantityType<>(0, SECOND));
            case CHANNEL_PROGRAM_PROGRESS -> updateStateIfLinked(channelId, new QuantityType<>(0, PERCENT));
            default -> getLinkedChannel(channelId).ifPresent(channel -> {
                if (CHANNEL_TYPE_SWITCH_DESCRIPTION.equals(channel.getChannelTypeUID())
                        || CHANNEL_TYPE_STRING_DESCRIPTION.equals(channel.getChannelTypeUID())
                        || CHANNEL_TYPE_NUMBER_DESCRIPTION.equals(channel.getChannelTypeUID())) {
                    getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                        var configuration = channel.getConfiguration();
                        var descriptionKeyConfiguration = configuration.get(CONFIGURATION_DESCRIPTION_KEY);
                        var attributeConfiguration = configuration.get(CONFIGURATION_ATTRIBUTE);

                        if (descriptionKeyConfiguration instanceof String key
                                && attributeConfiguration instanceof String attribute) {
                            mapKey(key).ifPresent(uid -> {
                                var foundObject = deviceDescriptionService.getDeviceDescriptionObject(uid);
                                if (foundObject != null) {
                                    var value = getAttributeValue(foundObject.object(), attribute);
                                    if (value == null) {
                                        updateState(channel.getUID(), UnDefType.UNDEF);
                                    } else if (CHANNEL_TYPE_SWITCH_DESCRIPTION.equals(channel.getChannelTypeUID())) {
                                        if (value instanceof Boolean b) {
                                            updateState(channel.getUID(), OnOffType.from(b));
                                        }
                                    } else if (CHANNEL_TYPE_STRING_DESCRIPTION.equals(channel.getChannelTypeUID())) {
                                        updateState(channel.getUID(), StringType.valueOf(value.toString()));
                                    } else if (CHANNEL_TYPE_NUMBER_DESCRIPTION.equals(channel.getChannelTypeUID())) {
                                        updateState(channel.getUID(), new DecimalType(value.toString()));
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private @Nullable Object getAttributeValue(Object object, String attribute) {
        if (ATTRIBUTE_ACCESS.equals(attribute) && object instanceof AccessProvider provider) {
            return provider.access().name();
        } else if (ATTRIBUTE_AVAILABLE.equals(attribute) && object instanceof AvailableProvider provider) {
            return provider.available();
        } else if (object instanceof RangeProvider provider
                && Set.of(ATTRIBUTE_MIN, ATTRIBUTE_MAX, ATTRIBUTE_STEP_SIZE).contains(attribute)) {
            switch (attribute) {
                case ATTRIBUTE_MIN -> {
                    return provider.min();
                }
                case ATTRIBUTE_MAX -> {
                    return provider.max();
                }
                case ATTRIBUTE_STEP_SIZE -> {
                    return provider.stepSize();
                }
            }
        } else if (object instanceof EnumerationTypeProvider provider) {
            if (ATTRIBUTE_ENUMERATION_TYPE.equals(attribute)) {
                return provider.enumerationType();
            } else if (ATTRIBUTE_ENUMERATION_TYPE_KEY.equals(attribute)) {
                return provider.enumerationTypeKey();
            }
        }

        return null;
    }

    private boolean isUnsatisfiedLinkError(@Nullable Throwable throwable) {
        int maxDepth = 10;
        int currentDepth = 0;
        boolean result = false;

        while (throwable != null && currentDepth < maxDepth) {
            if (throwable instanceof UnsatisfiedLinkError) {
                result = true;
            }
            throwable = throwable.getCause();
            currentDepth++;
        }

        return result;
    }
}
