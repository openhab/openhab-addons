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
package org.openhab.binding.tr064.internal;

import static org.openhab.binding.tr064.internal.Tr064BindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.openhab.binding.tr064.internal.config.Tr064ChannelConfig;
import org.openhab.binding.tr064.internal.config.Tr064RootConfiguration;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDDeviceType;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDServiceType;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDActionType;
import org.openhab.binding.tr064.internal.phonebook.Phonebook;
import org.openhab.binding.tr064.internal.phonebook.PhonebookProvider;
import org.openhab.binding.tr064.internal.phonebook.Tr064PhonebookImpl;
import org.openhab.binding.tr064.internal.soap.SOAPConnector;
import org.openhab.binding.tr064.internal.soap.SOAPRequest;
import org.openhab.binding.tr064.internal.soap.SOAPValueConverter;
import org.openhab.binding.tr064.internal.util.SCPDUtil;
import org.openhab.binding.tr064.internal.util.Util;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Tr064RootHandler} is responsible for handling commands, which are
 * sent to one of the channels and update channel values
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064RootHandler extends BaseBridgeHandler implements PhonebookProvider {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_GENERIC, THING_TYPE_FRITZBOX);
    private static final int RETRY_INTERVAL = 60;
    private static final Set<String> PROPERTY_ARGUMENTS = Set.of("NewSerialNumber", "NewSoftwareVersion",
            "NewModelName");

    private final Logger logger = LoggerFactory.getLogger(Tr064RootHandler.class);
    private final HttpClient httpClient;

    private @Nullable SCPDUtil scpdUtil;
    private SOAPConnector soapConnector;

    // these are set when the config is available
    private Tr064RootConfiguration config = new Tr064RootConfiguration();
    private String endpointBaseURL = "";
    private int timeout = Tr064RootConfiguration.DEFAULT_HTTP_TIMEOUT;

    private String deviceType = "";

    private final Map<ChannelUID, Tr064ChannelConfig> channels = new HashMap<>();
    // caching is used to prevent excessive calls to the same action
    private final ExpiringCacheMap<ChannelUID, State> stateCache = new ExpiringCacheMap<>(Duration.ofMillis(2000));
    private Collection<Phonebook> phonebooks = List.of();

    private @Nullable ScheduledFuture<?> connectFuture;
    private @Nullable ScheduledFuture<?> pollFuture;
    private @Nullable ScheduledFuture<?> phonebookFuture;

    private boolean communicationEstablished = false;

    Tr064RootHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.soapConnector = new SOAPConnector(httpClient, endpointBaseURL, timeout);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!communicationEstablished) {
            logger.debug("Tried to process command, but thing is not yet ready: {} to {}", channelUID, command);
        }
        Tr064ChannelConfig channelConfig = channels.get(channelUID);
        if (channelConfig == null) {
            logger.trace("Channel {} not supported.", channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            SOAPConnector soapConnector = this.soapConnector;
            State state = stateCache.putIfAbsentAndGet(channelUID,
                    () -> soapConnector.getChannelStateFromDevice(channelConfig, channels, stateCache));
            if (state != null) {
                updateState(channelUID, state);
            }
            return;
        }

        if (channelConfig.getChannelTypeDescription().getSetAction() == null) {
            logger.debug("Discarding command {} to {}, read-only channel", command, channelUID);
            return;
        }
        scheduler.execute(() -> soapConnector.sendChannelCommandToDevice(channelConfig, command));
    }

    @Override
    public void initialize() {
        config = getConfigAs(Tr064RootConfiguration.class);
        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "At least one mandatory configuration field is empty");
            return;
        }

        endpointBaseURL = "http://" + config.host + ":49000";
        soapConnector = new SOAPConnector(httpClient, endpointBaseURL, timeout);
        timeout = config.timeout;
        updateStatus(ThingStatus.UNKNOWN);

        connectFuture = scheduler.scheduleWithFixedDelay(this::internalInitialize, 0, RETRY_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * internal thing initializer (sets SCPDUtil and connects to remote device)
     */
    private void internalInitialize() {
        try {
            scpdUtil = new SCPDUtil(httpClient, endpointBaseURL, timeout);
        } catch (SCPDException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "could not get device definitions from " + config.host);
            return;
        }

        if (establishSecureConnectionAndUpdateProperties()) {
            removeConnectScheduler();

            // connection successful, check channels
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withoutChannels(thing.getChannels());
            final SCPDUtil scpdUtil = this.scpdUtil;
            final ThingHandlerCallback callback = getCallback();
            if (scpdUtil != null && callback != null) {
                Util.checkAvailableChannels(thing, callback, thingBuilder, scpdUtil, "", deviceType, channels);
                updateThing(thingBuilder.build());
            }

            communicationEstablished = true;
            installPolling();
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        }
    }

    private void removeConnectScheduler() {
        final ScheduledFuture<?> connectFuture = this.connectFuture;
        if (connectFuture != null) {
            connectFuture.cancel(true);
            this.connectFuture = null;
        }
    }

    @Override
    public void dispose() {
        communicationEstablished = false;
        removeConnectScheduler();
        uninstallPolling();
        stateCache.clear();
        scpdUtil = null;
    }

    /**
     * poll remote device for channel values
     */
    private void poll() {
        try {
            channels.forEach((channelUID, channelConfig) -> {
                if (isLinked(channelUID)) {
                    State state = stateCache.putIfAbsentAndGet(channelUID,
                            () -> soapConnector.getChannelStateFromDevice(channelConfig, channels, stateCache));
                    if (state != null) {
                        updateState(channelUID, state);
                    }
                }
            });
        } catch (RuntimeException e) {
            logger.warn("Exception while refreshing remote data for thing '{}':", thing.getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Refresh exception: " + e.getMessage());
        }
    }

    /**
     * establish the connection - get secure port (if available), install authentication, get device properties
     *
     * @return true if successful
     */
    private boolean establishSecureConnectionAndUpdateProperties() {
        final SCPDUtil scpdUtil = this.scpdUtil;
        if (scpdUtil != null) {
            try {
                SCPDDeviceType device = scpdUtil.getDevice("")
                        .orElseThrow(() -> new SCPDException("Root device not found"));
                SCPDServiceType deviceService = device.getServiceList().stream()
                        .filter(service -> "urn:DeviceInfo-com:serviceId:DeviceInfo1".equals(service.getServiceId()))
                        .findFirst().orElseThrow(() -> new SCPDException(
                                "service 'urn:DeviceInfo-com:serviceId:DeviceInfo1' not found"));

                this.deviceType = device.getDeviceType();

                // try to get security (https) port
                SOAPMessage soapResponse = soapConnector
                        .doSOAPRequest(new SOAPRequest(deviceService, "GetSecurityPort"));
                if (!soapResponse.getSOAPBody().hasFault()) {
                    SOAPValueConverter soapValueConverter = new SOAPValueConverter(httpClient, timeout);
                    soapValueConverter.getStateFromSOAPValue(soapResponse, "NewSecurityPort", null)
                            .ifPresentOrElse(port -> {
                                endpointBaseURL = "https://" + config.host + ":" + port;
                                soapConnector = new SOAPConnector(httpClient, endpointBaseURL, timeout);
                                logger.debug("endpointBaseURL is now '{}'", endpointBaseURL);
                            }, () -> logger.warn("Could not determine secure port, disabling https"));
                } else {
                    logger.warn("Could not determine secure port, disabling https");
                }

                // clear auth cache and force re-auth
                AuthenticationStore authStore = httpClient.getAuthenticationStore();
                URI endpointUri = URI.create(endpointBaseURL);
                Authentication authentication = authStore.findAuthentication("Digest", endpointUri,
                        Authentication.ANY_REALM);
                if (authentication != null) {
                    authStore.removeAuthentication(authentication);
                }
                Authentication.Result authResult = authStore.findAuthenticationResult(endpointUri);
                if (authResult != null) {
                    authStore.removeAuthenticationResult(authResult);
                }
                authStore.addAuthentication(new DigestAuthentication(new URI(endpointBaseURL), Authentication.ANY_REALM,
                        config.user, config.password));

                // check & update properties
                SCPDActionType getInfoAction = scpdUtil.getService(deviceService.getServiceId())
                        .orElseThrow(() -> new SCPDException(
                                "Could not get service definition for 'urn:DeviceInfo-com:serviceId:DeviceInfo1'"))
                        .getActionList().stream().filter(action -> "GetInfo".equals(action.getName())).findFirst()
                        .orElseThrow(() -> new SCPDException("Action 'GetInfo' not found"));
                SOAPMessage soapResponse1 = soapConnector
                        .doSOAPRequest(new SOAPRequest(deviceService, getInfoAction.getName()));
                SOAPValueConverter soapValueConverter = new SOAPValueConverter(httpClient, timeout);
                Map<String, String> properties = editProperties();
                PROPERTY_ARGUMENTS.forEach(argumentName -> getInfoAction.getArgumentList().stream()
                        .filter(argument -> argument.getName().equals(argumentName)).findFirst()
                        .ifPresent(argument -> soapValueConverter
                                .getStateFromSOAPValue(soapResponse1, argumentName, null).ifPresent(value -> properties
                                        .put(argument.getRelatedStateVariable(), value.toString()))));
                properties.put("deviceType", device.getDeviceType());
                updateProperties(properties);

                return true;
            } catch (SCPDException | SOAPException | Tr064CommunicationException | URISyntaxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * get all sub devices of this root device (used for discovery)
     *
     * @return the list
     */
    public List<SCPDDeviceType> getAllSubDevices() {
        final SCPDUtil scpdUtil = this.scpdUtil;
        return (scpdUtil == null) ? List.of() : scpdUtil.getAllSubDevices();
    }

    /**
     * get the SOAP connector (used by sub devices for communication with the remote device)
     *
     * @return the SOAP connector
     */
    public SOAPConnector getSOAPConnector() {
        return soapConnector;
    }

    /**
     * return the result of an (authenticated) GET request
     *
     * @param url the requested URL
     *
     * @return a {@link ContentResponse} with the result of the request
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public ContentResponse getUrl(String url) throws ExecutionException, InterruptedException, TimeoutException {
        httpClient.getAuthenticationStore().addAuthentication(
                new DigestAuthentication(URI.create(url), Authentication.ANY_REALM, config.user, config.password));
        return httpClient.GET(URI.create(url));
    }

    /**
     * get the SCPD processing utility
     *
     * @return the SCPD utility (or null if not available)
     */
    public @Nullable SCPDUtil getSCPDUtil() {
        return scpdUtil;
    }

    /**
     * uninstall the polling
     */
    private void uninstallPolling() {
        final ScheduledFuture<?> pollFuture = this.pollFuture;
        if (pollFuture != null) {
            pollFuture.cancel(true);
            this.pollFuture = null;
        }
        final ScheduledFuture<?> phonebookFuture = this.phonebookFuture;
        if (phonebookFuture != null) {
            phonebookFuture.cancel(true);
            this.phonebookFuture = null;
        }
    }

    /**
     * install the polling
     */
    private void installPolling() {
        uninstallPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, config.refresh, TimeUnit.SECONDS);
        if (config.phonebookInterval > 0) {
            phonebookFuture = scheduler.scheduleWithFixedDelay(this::retrievePhonebooks, 0, config.phonebookInterval,
                    TimeUnit.SECONDS);
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Phonebook> processPhonebookList(SOAPMessage soapMessagePhonebookList,
            SCPDServiceType scpdService) {
        SOAPValueConverter soapValueConverter = new SOAPValueConverter(httpClient, timeout);
        Optional<Stream<String>> phonebookStream = soapValueConverter
                .getStateFromSOAPValue(soapMessagePhonebookList, "NewPhonebookList", null)
                .map(phonebookList -> Arrays.stream(phonebookList.toString().split(",")));
        return phonebookStream.map(stringStream -> (Collection<Phonebook>) stringStream.map(index -> {
            try {
                SOAPMessage soapMessageURL = soapConnector
                        .doSOAPRequest(new SOAPRequest(scpdService, "GetPhonebook", Map.of("NewPhonebookID", index)));
                return soapValueConverter.getStateFromSOAPValue(soapMessageURL, "NewPhonebookURL", null)
                        .map(url -> (Phonebook) new Tr064PhonebookImpl(httpClient, url.toString(), timeout));
            } catch (Tr064CommunicationException e) {
                logger.warn("Failed to get phonebook with index {}:", index, e);
            }
            return Optional.empty();
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList())).orElseGet(Set::of);
    }

    private void retrievePhonebooks() {
        String serviceId = "urn:X_AVM-DE_OnTel-com:serviceId:X_AVM-DE_OnTel1";
        SCPDUtil scpdUtil = this.scpdUtil;
        if (scpdUtil == null) {
            logger.warn("Cannot find SCPDUtil. This is most likely a programming error.");
            return;
        }
        Optional<SCPDServiceType> scpdService = scpdUtil.getDevice("").flatMap(deviceType -> deviceType.getServiceList()
                .stream().filter(service -> service.getServiceId().equals(serviceId)).findFirst());

        phonebooks = Objects.requireNonNull(scpdService.map(service -> {
            try {
                return processPhonebookList(soapConnector.doSOAPRequest(new SOAPRequest(service, "GetPhonebookList")),
                        service);
            } catch (Tr064CommunicationException e) {
                return Collections.<Phonebook> emptyList();
            }
        }).orElse(List.of()));

        if (phonebooks.isEmpty()) {
            logger.warn("Could not get phonebooks for thing {}", thing.getUID());
        }
    }

    @Override
    public Optional<Phonebook> getPhonebookByName(String name) {
        return phonebooks.stream().filter(p -> name.equals(p.getName())).findAny();
    }

    @Override
    public Collection<Phonebook> getPhonebooks() {
        return phonebooks;
    }

    @Override
    public ThingUID getUID() {
        return thing.getUID();
    }

    @Override
    public String getFriendlyName() {
        String friendlyName = thing.getLabel();
        return friendlyName != null ? friendlyName : getUID().getId();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        if (THING_TYPE_FRITZBOX.equals(thing.getThingTypeUID())) {
            return Set.of(Tr064DiscoveryService.class, FritzboxActions.class);
        } else {
            return Set.of(Tr064DiscoveryService.class);
        }
    }

    /**
     * get the backup configuration for this thing (only applies to FritzBox devices
     *
     * @return the configuration
     */
    public FritzboxActions.BackupConfiguration getBackupConfiguration() {
        return new FritzboxActions.BackupConfiguration(config.backupDirectory,
                Objects.requireNonNullElse(config.backupPassword, config.password));
    }
}
