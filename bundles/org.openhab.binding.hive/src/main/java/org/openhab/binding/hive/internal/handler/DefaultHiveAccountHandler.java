/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hive.internal.HiveAccountConfig;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.client.HiveClient;
import org.openhab.binding.hive.internal.client.HiveClientFactory;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.NodeId;
import org.openhab.binding.hive.internal.client.exception.HiveApiAuthenticationException;
import org.openhab.binding.hive.internal.client.exception.HiveException;
import org.openhab.binding.hive.internal.discovery.HiveDiscoveryService;
import org.openhab.binding.hive.internal.discovery.HiveDiscoveryServiceFactory;
import org.openhab.binding.hive.internal.handler.strategy.ThingHandlerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DefaultHiveAccountHandler} is responsible for handling interaction with
 * the Hive API.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class DefaultHiveAccountHandler extends BaseBridgeHandler implements HiveAccountHandler {
    private static final Pattern UUID_REGEX = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private static final int POLLING_MINIMAL_INTERVAL_SECONDS = 10;
    private static final int POLLING_INITIAL_DELAY_SECONDS = 0;
    private static final double POLLING_BACKOFF_FACTOR = 2;
    private static final int POLLING_MAXIMUM_BACKOFF_INTERVAL_SECONDS = 600;
    private static final Duration POLLING_MAXIMUM_RETRY_TIME = Duration.ofHours(24);

    private final Logger logger = LoggerFactory.getLogger(DefaultHiveAccountHandler.class);

    private final Map<NodeId, HiveThingManager> hiveThingManagers = new HashMap<>();

    private final HiveClientFactory hiveClientFactory;
    private final HiveDiscoveryService discoveryService;

    private final @Nullable Phaser testingPhaser;

    private @Nullable ScheduledFuture<?> pollingJob = null;
    private int normalPollingIntervalSeconds;

    private Instant lastSuccessfulPoll;
    private boolean pollingBackingOff;
    private int backoffPollingIntervalSeconds;


    private @Nullable HiveClient hiveClient = null;

    /**
     *
     * @param bridge
     *      The {@link Bridge} that this handler is handling.
     */
    public DefaultHiveAccountHandler(
            final HiveClientFactory hiveClientFactory,
            final HiveDiscoveryServiceFactory discoveryServiceFactory,
            final Bridge bridge
    ) {
        this(hiveClientFactory, discoveryServiceFactory, bridge, null);
    }

    /**
     * Test constructor
     */
    DefaultHiveAccountHandler(
            final HiveClientFactory hiveClientFactory,
            final HiveDiscoveryServiceFactory discoveryServiceFactory,
            final Bridge bridge,
            final @Nullable Phaser testingPhaser
    ) {
        super(bridge);

        Objects.requireNonNull(hiveClientFactory);
        Objects.requireNonNull(discoveryServiceFactory);

        this.hiveClientFactory = hiveClientFactory;
        this.discoveryService = discoveryServiceFactory.create(bridge.getUID());
        this.testingPhaser = testingPhaser;

        this.lastSuccessfulPoll = Instant.EPOCH;

        if (testingPhaser != null) {
            testingPhaser.register();
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING);

        this.scheduler.execute(this::doInit);
    }

    private synchronized void doInit() {
        // The the openHAB runtime should ensure none of these are null.
        // Do a little dance to make the null checker happy.
        final HiveAccountConfig config = getConfigAs(HiveAccountConfig.class);
        final @Nullable String username = config.username;
        final @Nullable String password = config.password;
        final @Nullable Integer pollingInterval = config.pollingInterval;
        if (username == null || password == null || pollingInterval == null) {
            throw new IllegalStateException("Config is malformed");
        }

        try {
            this.hiveClient = this.hiveClientFactory.newClient(
                    username,
                    password
            );

            this.pollingBackingOff = false;
            this.normalPollingIntervalSeconds = Math.max(pollingInterval, POLLING_MINIMAL_INTERVAL_SECONDS);
            this.backoffPollingIntervalSeconds = this.normalPollingIntervalSeconds;

            this.pollingJob = this.scheduler.scheduleWithFixedDelay(
                    this::poll,
                    POLLING_INITIAL_DELAY_SECONDS,
                    this.normalPollingIntervalSeconds,
                    TimeUnit.SECONDS
            );

            updateStatus(ThingStatus.ONLINE);
        } catch (final HiveApiAuthenticationException ex) {
            updateStatus(
                    ThingStatus.OFFLINE,
                    ThingStatusDetail.CONFIGURATION_ERROR,
                    "Username and Password are not correct."
            );
        } catch (final HiveException ex) {
            updateStatus(
                    ThingStatus.OFFLINE,
                    ThingStatusDetail.COMMUNICATION_ERROR,
                    "Something went wrong communicating with the Hive Api. Details: " + ex.getMessage()
            );
            this.logger.debug("Something went wrong communicating with the Hive Api while initialising an account handler.", ex);
        } catch (final Exception ex) {
            updateStatus(
                    ThingStatus.OFFLINE,
                    ThingStatusDetail.NONE,
                    "Something went very wrong. Details: " + ex.getMessage()
            );
            this.logger.debug("Something very wrong initialising an account handler.", ex);
        } finally {
            // If we are testing synchronise with the test.
            final @Nullable Phaser testingPhaser = this.testingPhaser;
            if (testingPhaser != null) {
                testingPhaser.arriveAndAwaitAdvance();
            }
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (channelUID.getId().equals(HiveBindingConstants.CHANNEL_DUMP_NODES)) {
            final @Nullable HiveClient hiveClient = this.hiveClient;
            if (command instanceof OnOffType && hiveClient != null) {
                try {
                    final String nodesJson = hiveClient.getAllNodesJson();

                    writeNodesDumpFile(nodesJson);
                } catch (final HiveException e) {
                    this.logger.debug("Something went wrong while trying to dump nodes.", e);
                }
            }

            // Reset the switch
            this.updateState(HiveBindingConstants.CHANNEL_DUMP_NODES, OnOffType.OFF);
        }
    }

    private void writeNodesDumpFile(final String nodesJson) {
        final String dumpFilePrefix = "hive-nodes-";

        final String origDumpFileName = dumpFilePrefix + "original.json";
        final String anonDumpFileName = dumpFilePrefix + "anon.json";

        final Path origDumpFile = Paths.get(ConfigConstants.getUserDataFolder(), origDumpFileName);
        final Path anonDumpFile = Paths.get(ConfigConstants.getUserDataFolder(), anonDumpFileName);

        // Write the original file.
        try {
            Files.write(origDumpFile, nodesJson.getBytes());
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }

        // Find any UUIDs in the JSON.
        final Matcher uuidMatcher = UUID_REGEX.matcher(nodesJson);
        final Set<String> foundUuids = new HashSet<>();
        while (uuidMatcher.find()) {
            foundUuids.add(uuidMatcher.group());
        }

        // Replace all the found UUIDs with random new ones.
        String anonNodesJson = nodesJson;
        for (final String origUuid : foundUuids) {
            anonNodesJson = anonNodesJson.replace(origUuid, UUID.randomUUID().toString());
        }

        // Write the "anonymized" file.
        try {
            Files.write(anonDumpFile, anonNodesJson.getBytes());
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }

        this.logger.debug("Go get your dumped nodes here: {}", anonDumpFile.toAbsolutePath().toString());
    }

    @Override
    public void dispose() {
        final @Nullable ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }

        super.dispose();
    }

    @Override
    public DiscoveryService getDiscoveryService() {
        return this.discoveryService;
    }

    @Override
    public void bindHiveThingHandler(final HiveThingHandler hiveThingHandler) {
        Objects.requireNonNull(hiveThingHandler);

        final NodeId nodeId = getNodeIdFromHiveHandler(hiveThingHandler);

        final HiveThingManager hiveThingManager = new HiveThingManager(hiveThingHandler);
        hiveThingHandler.setCommandCallback(hiveThingManager);
        this.hiveThingManagers.put(nodeId, hiveThingManager);
    }

    @Override
    public void unbindHiveThingHandler(final HiveThingHandler hiveThingHandler) {
        Objects.requireNonNull(hiveThingHandler);

        hiveThingHandler.clearCommandCallback();

        final NodeId nodeId = getNodeIdFromHiveHandler(hiveThingHandler);
        final @Nullable HiveThingManager hiveThingManager = this.hiveThingManagers.remove(nodeId);
        if (hiveThingManager == null) {
            this.logger.debug("Didn't find the expected HiveThingManager for node {}", nodeId);
        }
    }

    private synchronized void poll() {
        // Wrap everything in a try-catch because we do not ever want the
        // polling job to stop unless we do it ourselves.
        try {
            final @Nullable HiveClient hiveClient = this.hiveClient;
            if (hiveClient == null) {
                throw new IllegalStateException("Poll called before HiveClient has been initialised.");
            }

            // Get all nodes from the Hive API.
            final Set<Node> nodes = hiveClient.getAllNodes();

            // Tell the discovery service about the nodes we found.
            this.discoveryService.updateKnownNodes(nodes);

            /* Send updated node state to handlers */
            for (final Node node : nodes) {
                // Try to find a manager responsible for this node.
                final @Nullable HiveThingManager thingManager = this.hiveThingManagers.get(node.getId());

                // Give the handler an updated version of the node.
                if (thingManager != null) {
                    thingManager.updateState(node);
                }
            }

            this.lastSuccessfulPoll = Instant.now();

            // Everything has gone ok so return to normal polling if we have
            // been backing off.
            if (this.pollingBackingOff) {
                this.logger.debug("Polling went ok! Returning to normal polling schedule.");

                // Reset backoff parameters
                this.pollingBackingOff = false;
                this.backoffPollingIntervalSeconds = this.normalPollingIntervalSeconds;

                // Make sure the old polling job is done.
                final @Nullable ScheduledFuture<?> oldPollingJob = this.pollingJob;
                if (oldPollingJob != null) {
                    oldPollingJob.cancel(false);
                }

                // Start a new repeating polling job
                this.pollingJob = this.scheduler.scheduleWithFixedDelay(
                        this::poll,
                        this.normalPollingIntervalSeconds,
                        this.normalPollingIntervalSeconds,
                        TimeUnit.SECONDS
                );

                // Make the thing online again.
                updateStatus(ThingStatus.ONLINE);
            }

            // Update the last poll timestamp channel
            updateState(
                    HiveBindingConstants.CHANNEL_LAST_POLL_TIMESTAMP,
                    new DateTimeType(this.lastSuccessfulPoll.atZone(ZoneId.systemDefault()))
            );
        } catch (final HiveApiAuthenticationException ex) {
            // Stop polling. There is no point if we are not authenticated any more.
            final @Nullable ScheduledFuture<?> pollingJob = this.pollingJob;
            if (pollingJob != null) {
                pollingJob.cancel(false);
            }

            updateStatus(
                    ThingStatus.OFFLINE,
                    ThingStatusDetail.CONFIGURATION_ERROR,
                    "Authentication failed - Have you changed your password?"
            );
        } catch (final Exception ex) {
            this.logger.debug("Polling failed with exception", ex);

            // Start backing off
            this.pollingBackingOff = true;

            // Cancel periodic polling / make sure old job is done.
            final @Nullable ScheduledFuture<?> oldPollingJob = this.pollingJob;
            if (oldPollingJob != null) {
                oldPollingJob.cancel(false);
            }

            String message = MessageFormat.format(
                    "Something went wrong polling the Hive API."
                            + " It is now {0}."
                            + " Last successful poll was at {1}."
                            + " We have not had a successful poll for {2} seconds.",
                    Instant.now().atZone(ZoneId.systemDefault()),
                    this.lastSuccessfulPoll.atZone(ZoneId.systemDefault()),
                    Duration.between(this.lastSuccessfulPoll, Instant.now()).getSeconds()
            );
            if (POLLING_MAXIMUM_RETRY_TIME.isNegative() || Duration.between(this.lastSuccessfulPoll, Instant.now()).compareTo(POLLING_MAXIMUM_RETRY_TIME) < 0) {
                // Either the retry limit is disabled or we have not exceeded
                // it yet.

                // Increase the backoff interval.
                this.backoffPollingIntervalSeconds = Math.min(
                        (int) Math.ceil(this.backoffPollingIntervalSeconds * POLLING_BACKOFF_FACTOR),
                        POLLING_MAXIMUM_BACKOFF_INTERVAL_SECONDS
                );

                // Schedule new "backed off" job.
                this.scheduler.schedule(
                        this::poll,
                        this.backoffPollingIntervalSeconds,
                        TimeUnit.SECONDS
                );

                message += MessageFormat.format(
                        " Trying again in {0} seconds (approx {1}).",
                        this.backoffPollingIntervalSeconds,
                        Instant.now().plus(this.backoffPollingIntervalSeconds, ChronoUnit.SECONDS).atZone(ZoneId.systemDefault())
                );
            } else {
                // We have exceeded the retry limit.
                // Let the polling job die.

                message += " Giving up as we have exceeded the retry limit.";
            }

            message += " Details: " + ex.getMessage();

            this.logger.debug(message);

            // Tell the user what is going on.
            updateStatus(
                    ThingStatus.OFFLINE,
                    ThingStatusDetail.COMMUNICATION_ERROR,
                    message
            );
        }
    }

    private NodeId getNodeIdFromHiveHandler(final ThingHandler handler) {
        Objects.requireNonNull(handler);

        return new NodeId(UUID.fromString((String) handler.getThing().getConfiguration().get(HiveBindingConstants.CONFIG_NODE_ID)));
    }

    private class HiveThingManager implements ThingHandlerCommandCallback {
        private final HiveThingHandler hiveThingHandler;

        private @Nullable Node hiveNode = null;

        public HiveThingManager(final HiveThingHandler hiveThingHandler) {
            Objects.requireNonNull(hiveThingHandler);

            this.hiveThingHandler = hiveThingHandler;
        }

        @Override
        public void handleCommand(final ChannelUID channelUID, final Command command) {
            final @Nullable Node hiveNode = this.hiveNode;
            if (hiveNode == null) {
                return;
            }

            // Try each strategy until we get one that can handle the command.
            @Nullable Node modifiedNode = null;
            final Iterator<ThingHandlerStrategy> strategyIterator = this.hiveThingHandler.getStrategies().iterator();
            while (strategyIterator.hasNext() && modifiedNode == null) {
                final @Nullable ThingHandlerStrategy strategy = strategyIterator.next();
                assert strategy != null;

                modifiedNode = strategy.handleCommand(channelUID, command, hiveNode);
            }

            // If the command was handled push the update to the Hive API.
            if (modifiedNode != null) {
                final @Nullable HiveClient hiveClient = DefaultHiveAccountHandler.this.hiveClient;

                if (hiveClient == null) {
                    throw new IllegalStateException("UpdateNode called before HiveClient has been initialised.");
                }

                try {
                    // Post the modified node to the Hive API and get the
                    // canonical updated version of the node in return.
                    final @Nullable Node updatedNode = hiveClient.updateNode(modifiedNode);

                    // Handle the updated node state.
                    // TODO: Create a better mechanism for updating state
                    //       immediately after posting to the Hive API.
                    //       The Hive API often provides unhelpful displayValues
                    //       in response to posting updates that makes channel
                    //       states "flicker".
                    //if (updatedNode != null) {
                    //    this.updateState(updatedNode);
                    //}
                } catch (final HiveException ex) {
                    logger.debug("Something went wrong posting an update to the Hive API", ex);
                }
            }
        }

        public final void updateState(final Node hiveNode) {
            Objects.requireNonNull(hiveNode);

            // Save the node for use by handleCommand.
            this.hiveNode = hiveNode;

            // Update channel states using the handler strategies.
            final @Nullable ThingHandlerCallback thingHandlerCallback = this.hiveThingHandler.getThingHandlerCallback();
            if (thingHandlerCallback != null) {
                for (final ThingHandlerStrategy strategy : this.hiveThingHandler.getStrategies()) {
                    strategy.handleUpdate(this.hiveThingHandler.getThing(), thingHandlerCallback, hiveNode);
                }
            }
        }

    }
}
