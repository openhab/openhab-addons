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
package org.openhab.binding.matter.internal.bridge;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.bridge.devices.BaseDevice;
import org.openhab.binding.matter.internal.bridge.devices.DeviceRegistry;
import org.openhab.binding.matter.internal.client.MatterClientListener;
import org.openhab.binding.matter.internal.client.MatterRequestException;
import org.openhab.binding.matter.internal.client.MatterWebsocketService;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.BridgeCommissionState;
import org.openhab.binding.matter.internal.client.dto.ws.BridgeEventAttributeChanged;
import org.openhab.binding.matter.internal.client.dto.ws.BridgeEventMessage;
import org.openhab.binding.matter.internal.client.dto.ws.BridgeEventTriggered;
import org.openhab.binding.matter.internal.client.dto.ws.EventTriggeredMessage;
import org.openhab.binding.matter.internal.client.dto.ws.NodeDataMessage;
import org.openhab.binding.matter.internal.client.dto.ws.NodeStateMessage;
import org.openhab.binding.matter.internal.client.dto.ws.OtaUpdateAvailableMessage;
import org.openhab.core.OpenHAB;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemRegistryChangeListener;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link MatterBridge} is the main class for the Matter Bridge service.
 * 
 * It is responsible for exposing a "Matter Bridge" server and exposing items as endpoint on the bridge.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { MatterBridge.class,
        EventSubscriber.class }, configurationPid = MatterBridge.CONFIG_PID, property = Constants.SERVICE_PID + "="
                + MatterBridge.CONFIG_PID)
@ConfigurableService(category = "io", label = "Matter Bridge", description_uri = MatterBridge.CONFIG_URI)
public class MatterBridge implements MatterClientListener, EventSubscriber {
    private final Logger logger = LoggerFactory.getLogger(MatterBridge.class);
    protected static final String CONFIG_PID = "org.openhab.matter";
    protected static final String CONFIG_URI = "io:matter";
    private static final int START_RETRY_DELAY_SECONDS = 2;

    // Matter Bridge Device Info *Basic Information Cluster*
    private static final String VENDOR_NAME = "openHAB";
    private static final String DEVICE_NAME = "Bridge Device";
    private static final String PRODUCT_ID = "0001";
    private static final String VENDOR_ID = "65521";

    private final Map<String, BaseDevice> devices = new ConcurrentHashMap<>();
    // Items whose endpoint failed, with the structure that failed. They are left out until their structure changes.
    private final Map<String, List<Object>> rejectedDevices = new HashMap<>();
    // Counts client stops, so a registration still running when the client stops does not keep its devices
    private final AtomicInteger clientGeneration = new AtomicInteger();
    private int registeredGeneration = -1;

    private MatterBridgeClient client;
    private ItemRegistry itemRegistry;
    private MetadataRegistry metadataRegistry;
    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private MatterWebsocketService websocketService;
    private ConfigurationAdmin configAdmin;
    private MatterBridgeSettings settings;

    private final ItemRegistryChangeListener itemRegistryChangeListener;
    private final RegistryChangeListener<Metadata> metadataRegistryChangeListener;
    private final RegistryChangeListener<ItemChannelLink> itemChannelLinkRegistryChangeListener;
    // Use a dedicated pool rather than the shared common pool: registerItems() makes blocking websocket
    // calls while holding the instance lock, so it must not be able to starve the common pool.
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("matter.MatterBridge");
    private boolean resetStorage = false;
    private @Nullable ScheduledFuture<?> modifyFuture;
    private @Nullable ScheduledFuture<?> reconnectFuture;
    private @Nullable ScheduledFuture<?> reachableFuture;
    private volatile RunningState runningState = RunningState.Stopped;
    private volatile boolean commissioningWindowOpen = false;

    @Activate
    public MatterBridge(final @Reference ItemRegistry itemRegistry, final @Reference MetadataRegistry metadataRegistry,
            final @Reference MatterWebsocketService websocketService, final @Reference ConfigurationAdmin configAdmin,
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this(itemRegistry, metadataRegistry, websocketService, configAdmin, itemChannelLinkRegistry,
                new MatterBridgeClient());
    }

    MatterBridge(ItemRegistry itemRegistry, MetadataRegistry metadataRegistry, MatterWebsocketService websocketService,
            ConfigurationAdmin configAdmin, ItemChannelLinkRegistry itemChannelLinkRegistry,
            MatterBridgeClient client) {
        this.itemRegistry = itemRegistry;
        this.metadataRegistry = metadataRegistry;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.websocketService = websocketService;
        this.configAdmin = configAdmin;
        this.client = client;
        this.settings = new MatterBridgeSettings();

        itemRegistryChangeListener = new ItemRegistryChangeListener() {
            private boolean handleMetadataChange(Item item) {
                if (metadataRegistry.get(new MetadataKey("matter", item.getUID())) != null) {
                    updateModifyFuture();
                    return true;
                }
                return false;
            }

            @Override
            public void added(Item element) {
                handleMetadataChange(element);
            }

            @Override
            public void updated(Item oldElement, Item element) {
                if (!handleMetadataChange(oldElement)) {
                    handleMetadataChange(element);
                }
            }

            @Override
            public void allItemsChanged(Collection<String> oldItemNames) {
                updateModifyFuture();
            }

            @Override
            public void removed(Item element) {
                handleMetadataChange(element);
            }
        };
        this.itemRegistry.addRegistryChangeListener(itemRegistryChangeListener);

        metadataRegistryChangeListener = new RegistryChangeListener<>() {
            private boolean handleMetadataChange(Metadata element) {
                if ("matter".equals(element.getUID().getNamespace())) {
                    updateModifyFuture();
                    return true;
                }
                return false;
            }

            public void added(Metadata element) {
                handleMetadataChange(element);
            }

            public void removed(Metadata element) {
                handleMetadataChange(element);
            }

            public void updated(Metadata oldElement, Metadata element) {
                if (!handleMetadataChange(oldElement)) {
                    handleMetadataChange(element);
                }
            }
        };
        this.metadataRegistry.addRegistryChangeListener(metadataRegistryChangeListener);

        itemChannelLinkRegistryChangeListener = new RegistryChangeListener<>() {
            public void added(ItemChannelLink element) {
                scheduleUpdateReachable();
            }

            public void removed(ItemChannelLink element) {
                scheduleUpdateReachable();
            }

            public void updated(ItemChannelLink oldElement, ItemChannelLink element) {
                scheduleUpdateReachable();
            }
        };
        this.itemChannelLinkRegistry.addRegistryChangeListener(itemChannelLinkRegistryChangeListener);
    }

    @Activate
    public void activate(Map<String, Object> properties) {
        logger.debug("Activating Matter Bridge {}", properties);
        // if this returns true, we will wait for @Modified to be called after the config is persisted
        if (!parseInitialConfig(properties)) {
            this.settings = (new Configuration(properties)).as(MatterBridgeSettings.class);
            if (this.settings.enableBridge) {
                scheduleConnect();
            }
        }
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating Matter Bridge");
        itemRegistry.removeRegistryChangeListener(itemRegistryChangeListener);
        metadataRegistry.removeRegistryChangeListener(metadataRegistryChangeListener);
        itemChannelLinkRegistry.removeRegistryChangeListener(itemChannelLinkRegistryChangeListener);
        stopClient();
    }

    @Modified
    protected void modified(Map<String, Object> properties) {
        logger.debug("Modified Matter Bridge {}", properties);
        MatterBridgeSettings settings = (new Configuration(properties)).as(MatterBridgeSettings.class);
        boolean restart = false;
        if (this.settings.enableBridge != settings.enableBridge) {
            restart = true;
        }
        if (!this.settings.bridgeName.equals(settings.bridgeName)) {
            restart = true;
        }
        if (this.settings.discriminator != settings.discriminator) {
            restart = true;
        }
        if (this.settings.passcode != settings.passcode) {
            restart = true;
        }
        if (this.settings.port != settings.port) {
            restart = true;
        }
        if (settings.resetBridge) {
            this.resetStorage = true;
            settings.resetBridge = false;
            restart = true;
        }

        this.settings = settings;

        if (!settings.enableBridge) {
            stopClient();
        } else if (!client.isConnected() || restart) {
            stopClient();
            scheduleConnect();
        } else {
            if (settings.openCommissioningWindow != commissioningWindowOpen) {
                manageCommissioningWindow(settings.openCommissioningWindow);
            }
        }
    }

    @Override
    public void onDisconnect(String reason) {
        stopClient();
        if (this.settings.enableBridge) {
            scheduleConnect();
        }
    }

    @Override
    public void onConnect() {
    }

    @Override
    public void onReady() {
        registerItems();
    }

    @Override
    public void onEvent(NodeStateMessage message) {
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
    }

    @Override
    public void onEvent(EventTriggeredMessage message) {
    }

    @Override
    public void onEvent(NodeDataMessage message) {
    }

    @Override
    public void onEvent(OtaUpdateAvailableMessage message) {
    }

    @Override
    public void onEvent(BridgeEventMessage message) {
        if (message instanceof BridgeEventAttributeChanged attributeChanged) {
            BaseDevice d = devices.get(attributeChanged.data.endpointId);
            if (d != null) {
                d.handleMatterEvent(attributeChanged.data.clusterName, attributeChanged.data.attributeName,
                        attributeChanged.data.data);
            }
        } else if (message instanceof BridgeEventTriggered bridgeEventTriggered) {
            switch (bridgeEventTriggered.data.eventName) {
                case "commissioningWindowOpen":
                    commissioningWindowOpen = true;
                    updateConfig(Map.of("openCommissioningWindow", true));
                    break;
                case "commissioningWindowClosed":
                    commissioningWindowOpen = false;
                    updateConfig(Map.of("openCommissioningWindow", false));
                    break;
                default:
            }
        }
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Set.of(ItemStateChangedEvent.TYPE, ThingStatusInfoChangedEvent.TYPE);
    }

    @Override
    public void receive(Event event) {
        if (runningState != RunningState.Running) {
            return;
        }
        if (event instanceof ItemStateChangedEvent changed) {
            if ((changed.getItemState() instanceof UnDefType) != (changed.getOldItemState() instanceof UnDefType)) {
                String itemName = changed.getItemName();
                devices.values().stream()
                        .filter(device -> device.stateItems().stream().anyMatch(i -> i.getName().equals(itemName)))
                        .forEach(device -> device.updateReachable(this::isReachable));
            }
        } else if (event instanceof ThingStatusInfoChangedEvent changed) {
            if ((changed.getStatusInfo().getStatus() == ThingStatus.ONLINE) != (changed.getOldStatusInfo()
                    .getStatus() == ThingStatus.ONLINE)) {
                updateReachable();
            }
        }
    }

    public void restart() {
        stopClient();
        connectClient();
    }

    public void allowCommissioning() {
        manageCommissioningWindow(true);
    }

    public void resetStorage() {
        this.resetStorage = true;
        stopClient();
        connectClient();
    }

    public String listFabrics() throws InterruptedException, ExecutionException {
        return client.getFabrics().get().toString();
    }

    public void removeFabric(String fabricId) {
        try {
            client.removeFabric(Integer.parseInt(fabricId)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Could not remove fabric", e);
        } catch (ExecutionException e) {
            logger.debug("Could not remove fabric", e);
        }
    }

    private synchronized void connectClient() {
        if (client.isConnected()) {
            logger.debug("Already Connected, returning");
            return;
        }

        String folderName = OpenHAB.getUserDataFolder() + File.separator + "matter";
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        Map<String, String> paramsMap = new HashMap<>();

        paramsMap.put("service", "bridge");
        paramsMap.put("storagePath", folder.getAbsolutePath());

        // default values the bridge exposes to clients
        paramsMap.put("deviceName", DEVICE_NAME);
        paramsMap.put("vendorName", VENDOR_NAME);
        paramsMap.put("vendorId", VENDOR_ID);
        paramsMap.put("productId", PRODUCT_ID);

        paramsMap.put("productName", settings.bridgeName);
        paramsMap.put("passcode", String.valueOf(settings.passcode));
        paramsMap.put("discriminator", String.valueOf(settings.discriminator));
        paramsMap.put("port", String.valueOf(settings.port));

        client.addListener(this);
        client.connectWhenReady(this.websocketService, paramsMap);
    }

    private void stopClient() {
        logger.debug("Stopping Matter Bridge Client");
        clientGeneration.incrementAndGet();
        cancelConnect();
        updateRunningState(RunningState.Stopped, null);
        ScheduledFuture<?> modifyFuture = this.modifyFuture;
        if (modifyFuture != null) {
            modifyFuture.cancel(true);
        }
        ScheduledFuture<?> reachableFuture = this.reachableFuture;
        if (reachableFuture != null) {
            reachableFuture.cancel(false);
        }
        client.removeListener(this);
        client.disconnect();
        devices.values().forEach(BaseDevice::deactivate);
        devices.clear();
    }

    private void scheduleConnect() {
        cancelConnect();
        this.reconnectFuture = scheduler.schedule(this::connectClient, 5, TimeUnit.SECONDS);
    }

    private void cancelConnect() {
        ScheduledFuture<?> reconnectFuture = this.reconnectFuture;
        if (reconnectFuture != null) {
            reconnectFuture.cancel(true);
        }
    }

    private boolean parseInitialConfig(Map<String, Object> properties) {
        logger.debug("Parse Config Matter Bridge");

        Dictionary<String, Object> props = null;
        org.osgi.service.cm.Configuration config = null;

        try {
            config = configAdmin.getConfiguration(MatterBridge.CONFIG_PID);
            props = config.getProperties();
        } catch (IOException e) {
            logger.warn("cannot retrieve config admin {}", e.getMessage());
        }

        if (props == null) { // if null, the configuration is new
            props = new Hashtable<>();
        }

        // A discriminator uniquely identifies a Matter device on the IPV6 network, 12-bit integer (0-4095)
        int discriminator = -1;
        @Nullable
        Object discriminatorProp = props.get("discriminator");
        if (discriminatorProp instanceof String discriminatorString) {
            try {
                discriminator = Integer.parseInt(discriminatorString);
            } catch (NumberFormatException e) {
                logger.debug("Could not parse discriminator {}", discriminatorString);
            }
        } else if (discriminatorProp instanceof Integer discriminatorInteger) {
            discriminator = discriminatorInteger;
        }

        // randomly create one if not set
        if (discriminator < 0) {
            Random random = new Random();
            discriminator = random.nextInt(4096);
        }

        props.put("discriminator", discriminator);

        // this should never be persisted true, temporary settings
        props.put("resetBridge", false);

        boolean changed = false;
        if (config != null) {
            try {
                changed = config.updateIfDifferent(props);
            } catch (IOException e) {
                logger.warn("cannot update configuration {}", e.getMessage());
            }
        }
        return changed;
    }

    void registerItems() {
        doRegisterItems(false);
    }

    private synchronized void doRegisterItems(boolean isRetry) {
        if (!client.isConnected()) {
            // on startup that scheduled run can fire before the bridge websocket is connected
            // as onReady() performs a full registration once the connection is established.
            logger.debug("Bridge not connected, deferring item registration");
            return;
        }
        int generation = clientGeneration.get();
        if (generation != registeredGeneration) {
            // a new connection retries every item
            rejectedDevices.clear();
            registeredGeneration = generation;
        }
        Map<String, BaseDevice> itemDevices = createDevices();
        if (runningState == RunningState.Running && !itemDevices.isEmpty()) {
            try {
                if (!updateEndpoints(itemDevices)) {
                    // matter.js keeps listing an endpoint that failed to add, and only a restart removes it
                    startBridge(createDevices(), false);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            startBridge(itemDevices, isRetry);
        }
    }

    /**
     * Creates a device, not yet activated, for each item with a Matter device type in its metadata. Items whose
     * endpoint failed are left out until their structure changes.
     */
    private Map<String, BaseDevice> createDevices() {
        Map<String, BaseDevice> itemDevices = new HashMap<>();
        for (Metadata metadata : metadataRegistry.getAll()) {
            final MetadataKey uid = metadata.getUID();
            if ("matter".equals(uid.getNamespace())) {
                try {
                    logger.debug("Metadata {}", metadata);
                    final GenericItem item = (GenericItem) itemRegistry.getItem(uid.getItemName());
                    for (String part : metadata.getValue().split(",")) {
                        BaseDevice device = DeviceRegistry.createDevice(part.trim(), metadataRegistry, client, item);
                        if (device != null) {
                            itemDevices.put(item.getName(), device);
                            break;
                        }
                    }
                } catch (ItemNotFoundException e) {
                    logger.debug("Could not find item {}", uid.getItemName());
                }
            }
        }
        rejectedDevices.keySet().retainAll(itemDevices.keySet());
        itemDevices.values().removeIf(device -> {
            List<Object> rejected = rejectedDevices.get(device.getName());
            if (rejected == null) {
                return false;
            }
            if (rejected.equals(device.structure())) {
                logger.debug("Leaving out item {}, its endpoint failed", device.getName());
                return true;
            }
            rejectedDevices.remove(device.getName());
            return false;
        });
        return itemDevices;
    }

    private void rejectDevice(BaseDevice device, String reason) {
        logger.warn("Could not add item {} to the bridge, it is left out until its Matter metadata changes: {}",
                device.getName(), reason);
        rejectedDevices.put(device.getName(), device.structure());
    }

    /**
     * Whether the bridge server answered with an error for this item, as opposed to the request not getting an
     * answer at all
     */
    private boolean isItemError(ExecutionException e) {
        return e.getCause() instanceof MatterRequestException requestException && requestException.isFromServer();
    }

    /**
     * Activates a device. A device whose options cannot be turned into an endpoint is left out.
     *
     * @return the endpoint, or null if the device was left out
     */
    private @Nullable BridgedEndpoint activate(BaseDevice device) {
        try {
            return device.activateBridgedEndpoint(this::isReachable);
        } catch (RuntimeException e) {
            device.dispose();
            rejectDevice(device, String.valueOf(e.getMessage()));
            return null;
        }
    }

    /**
     * Adds an activated device to the devices map, unless the client was stopped while it was being set up
     *
     * @return false if the client was stopped
     */
    private boolean keepDevice(BaseDevice device) {
        if (clientGeneration.get() != registeredGeneration) {
            device.deactivate();
            return false;
        }
        devices.put(device.getName(), device);
        return true;
    }

    private void deactivateDevices() {
        devices.values().forEach(BaseDevice::deactivate);
        devices.clear();
    }

    private void startBridge(Map<String, BaseDevice> itemDevices, boolean isRetry) {
        try {
            logger.debug("Initializing bridge, resetStorage: {}", resetStorage);
            client.initializeBridge(resetStorage).get();
            if (resetStorage) {
                resetStorage = false;
                updateConfig(Map.of("resetBridge", false));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Could not initialize endpoints", e);
            updateRunningState(RunningState.Error, e.getMessage());
            return;
        } catch (ExecutionException e) {
            logger.debug("Could not initialize endpoints", e);
            updateRunningState(RunningState.Error, e.getMessage());
            return;
        }

        updateRunningState(RunningState.Starting, null);

        // clear out any existing devices
        deactivateDevices();

        Map<String, BridgedEndpoint> bridgedEndpoints = new HashMap<>();
        for (BaseDevice device : itemDevices.values()) {
            BridgedEndpoint endpoint = activate(device);
            if (endpoint == null) {
                continue;
            }
            if (!keepDevice(device)) {
                deactivateDevices();
                updateRunningState(RunningState.Stopped, null);
                return;
            }
            bridgedEndpoints.put(device.getName(), endpoint);
            logger.debug("Registered item {} with device type {}", device.getName(), device.deviceType());
        }

        if (bridgedEndpoints.isEmpty()) {
            logger.info("No devices found to register with bridge, not starting bridge");
            updateRunningState(RunningState.Stopped, "No items found with matter metadata");
            return;
        }

        for (Map.Entry<String, BridgedEndpoint> entry : bridgedEndpoints.entrySet()) {
            try {
                logger.debug("Registering endpoint {}", entry.getKey());
                client.addEndpoint(entry.getValue()).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Could not register device with bridge", e);
                updateRunningState(RunningState.Error, e.getMessage());
                deactivateDevices();
                return;
            } catch (ExecutionException e) {
                BaseDevice device = itemDevices.get(entry.getKey());
                if (device == null || !isItemError(e)) {
                    logger.debug("Could not register device with bridge", e);
                    updateRunningState(RunningState.Error, e.getMessage());
                    deactivateDevices();
                    return;
                }
                rejectDevice(device, String.valueOf(e.getMessage()));
                deactivateDevices();
                // Start again without it. Each pass leaves out one more item, so this always ends.
                startBridge(createDevices(), isRetry);
                return;
            }
        }

        try {
            client.startBridge().get();
            updateRunningState(RunningState.Running, null);
            updatePairingCodes();
            // picks up reachability changes made while the bridge was starting
            updateReachable();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Could not start bridge", e);
            if (!isRetry) {
                logger.debug("Retrying bridge start in {} seconds", START_RETRY_DELAY_SECONDS);
                scheduler.schedule(() -> doRegisterItems(true), START_RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
                return;
            }
            updateRunningState(RunningState.Error, e.getMessage());
        } catch (ExecutionException e) {
            logger.debug("Could not start bridge", e);
            if (!isRetry) {
                logger.debug("Retrying bridge start in {} seconds", START_RETRY_DELAY_SECONDS);
                scheduler.schedule(() -> doRegisterItems(true), START_RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
                return;
            }
            updateRunningState(RunningState.Error, e.getMessage());
        }
    }

    /**
     * Brings the endpoints of a running bridge in line with the items without restarting it, so paired ecosystems
     * stay connected.
     *
     * @param itemDevices the devices for the current items, not yet activated
     * @return false if an endpoint failed to add, which leaves the bridge needing a restart
     */
    private boolean updateEndpoints(Map<String, BaseDevice> itemDevices) throws InterruptedException {
        for (String name : List.copyOf(devices.keySet())) {
            if (!itemDevices.containsKey(name)) {
                logger.debug("Removing endpoint {}", name);
                BaseDevice removed = devices.remove(name);
                if (removed != null) {
                    removed.deactivate();
                }
                awaitEndpointChange(name, client.removeEndpoint(name));
            }
        }
        for (BaseDevice device : itemDevices.values()) {
            String name = device.getName();
            BaseDevice existing = devices.get(name);
            if (existing == null) {
                logger.debug("Adding endpoint {}", name);
                if (!sendEndpoint(device, client::addEndpoint)) {
                    return false;
                }
            } else if (existing.structureChanged()) {
                logger.debug("Replacing endpoint {}", name);
                devices.remove(name);
                existing.deactivate();
                if (!sendEndpoint(device, client::updateEndpoint)) {
                    return false;
                }
            } else if (!existing.usesSameItems(device)) {
                logger.debug("Moving endpoint {} to the edited item", name);
                if (activate(device) == null) {
                    continue;
                }
                devices.remove(name);
                existing.deactivate();
                if (!keepDevice(device)) {
                    return true;
                }
                awaitEndpointChange(name, device.takeOver(existing));
            } else {
                awaitEndpointChange(name, existing.updateLabel(device.label()));
            }
        }
        updateReachable();
        return true;
    }

    /**
     * Activates a device and sends its endpoint to the bridge.
     *
     * @return false if the bridge failed to add the endpoint
     */
    private boolean sendEndpoint(BaseDevice device, Function<BridgedEndpoint, CompletableFuture<String>> send)
            throws InterruptedException {
        BridgedEndpoint endpoint = activate(device);
        if (endpoint == null) {
            return true;
        }
        try {
            send.apply(endpoint).get();
            keepDevice(device);
            return true;
        } catch (InterruptedException e) {
            device.deactivate();
            throw e;
        } catch (ExecutionException e) {
            device.deactivate();
            if (isItemError(e)) {
                rejectDevice(device, String.valueOf(e.getMessage()));
            } else {
                logger.debug("Could not update the bridge endpoint for item {}", device.getName(), e);
            }
            return false;
        }
    }

    private void awaitEndpointChange(String name, CompletableFuture<Void> change) throws InterruptedException {
        try {
            change.get();
        } catch (ExecutionException e) {
            logger.warn("Could not update the bridge endpoint for item {}: {}", name, e.getMessage());
        }
    }

    /**
     * A device is unreachable when all of its items are NULL or UNDEF, or when its items are linked to Things and
     * none of them is online. A group without members counts as unreachable.
     */
    private boolean isReachable(BaseDevice device) {
        Collection<GenericItem> items = device.stateItems();
        if (items.stream().allMatch(item -> item.getState() instanceof UnDefType)) {
            return false;
        }
        Set<Thing> things = new HashSet<>();
        items.forEach(item -> things.addAll(itemChannelLinkRegistry.getBoundThings(item.getName())));
        return things.isEmpty() || things.stream().anyMatch(thing -> thing.getStatus() == ThingStatus.ONLINE);
    }

    private void updateReachable() {
        if (runningState == RunningState.Running) {
            devices.values().forEach(device -> device.updateReachable(this::isReachable));
        }
    }

    /**
     * Updates the reachable flags once a burst of link changes, such as a reloaded things file, is over
     */
    private void scheduleUpdateReachable() {
        ScheduledFuture<?> future = reachableFuture;
        if (future != null) {
            future.cancel(false);
        }
        reachableFuture = scheduler.schedule(this::updateReachable, 1, TimeUnit.SECONDS);
    }

    private void manageCommissioningWindow(boolean open) {
        if (runningState != RunningState.Running) {
            return;
        }
        if (open && !commissioningWindowOpen) {
            try {
                client.openCommissioningWindow().get();
                commissioningWindowOpen = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Could not open commissioning window", e);
            } catch (CancellationException | ExecutionException e) {
                logger.debug("Could not open commissioning window", e);
            }
        } else if (!open && commissioningWindowOpen) {
            try {
                client.closeCommissioningWindow().get();
                commissioningWindowOpen = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Could not close commissioning window", e);
            } catch (CancellationException | ExecutionException e) {
                logger.debug("Could not close commissioning window", e);
            }
        }
        updateConfig(Map.of("openCommissioningWindow", commissioningWindowOpen));
    }

    private void updatePairingCodes() {
        try {
            BridgeCommissionState state = client.getCommissioningState().get();
            commissioningWindowOpen = state.commissioningWindowOpen;
            updateConfig(Map.of("manualPairingCode", state.pairingCodes.manualPairingCode, "qrCode",
                    state.pairingCodes.qrPairingCode, "openCommissioningWindow", state.commissioningWindowOpen));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Could not query codes", e);
        } catch (CancellationException | ExecutionException | JsonParseException e) {
            logger.debug("Could not query codes", e);
        }
    }

    private void updateConfig(Map<String, Object> entries) {
        try {
            org.osgi.service.cm.Configuration config = configAdmin.getConfiguration(MatterBridge.CONFIG_PID);
            Dictionary<String, Object> props = config.getProperties();
            if (props == null) {
                return;
            }
            entries.forEach((k, v) -> props.put(k, v));
            // if this updates, it will trigger a @Modified call
            config.updateIfDifferent(props);
        } catch (IOException e) {
            logger.debug("Could not load configuration", e);
        }
    }

    private void updateRunningState(RunningState newState, @Nullable String message) {
        runningState = newState;
        updateConfig(Map.of("runningState", runningState.toString() + (message != null ? ": " + message : "")));
        // log to INFO here as there is not many places for feedback for IO services
        logger.info("Matter Bridge State: {} {}", runningState, message != null ? ": " + message : "");
    }

    /**
     * This should be called by changes to items or metadata
     */
    private void updateModifyFuture() {
        // if the bridge is not enabled, we don't need to update the future
        if (!settings.enableBridge) {
            return;
        }
        ScheduledFuture<?> modifyFuture = this.modifyFuture;
        if (modifyFuture != null) {
            // a registration already running finishes, the next one waits for it
            modifyFuture.cancel(false);
        }
        this.modifyFuture = scheduler.schedule(this::registerItems, 5, TimeUnit.SECONDS);
    }

    enum RunningState {
        Stopped("Stopped"),
        Starting("Starting"),
        Running("Running"),
        Error("Error");

        private final String runningState;

        RunningState(String runningState) {
            this.runningState = runningState;
        }

        @Override
        public String toString() {
            return runningState;
        }
    }
}
