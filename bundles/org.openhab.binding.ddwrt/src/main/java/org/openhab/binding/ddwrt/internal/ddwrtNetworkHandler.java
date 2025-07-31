/**
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
package org.openhab.binding.ddwrt.internal;

import static org.openhab.binding.ddwrt.internal.ddwrtBindingConstants.CHANNEL_TOTAL_CLIENTS;

import java.io.File;
import java.security.KeyPair;
import java.util.Collections;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ddwrtNetworkHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class ddwrtNetworkHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(ddwrtNetworkHandler.class);

    private @Nullable ddwrtConfiguration config;

    public ddwrtNetworkHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_TOTAL_CLIENTS.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(ddwrtConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        logger.debug("Initializing ddwrt handler for '{}'.", getThing().getUID());

        updateStatus(ThingStatus.UNKNOWN);

        String privateKeyDirString = OpenHAB.getUserDataFolder() + "/ddwrt/keys";
        File privateKeyDir = new File(privateKeyDirString);

        if (!privateKeyDir.exists()) {
            logger.debug("Creating directory {}", privateKeyDirString);
            privateKeyDir.mkdirs();
        }

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = false; // <background task with long running initialization here>

            SshClient client = SshClient.setUpDefaultClient();

            try {
                // client.setServerKeyVerifier((ClientSession ssh, InputStream is, int i) -> true);
                client.start();

                try (ClientSession session = client.connect(config.user, config.hostname, config.port).verify()
                        .getSession()) {
                    if (!config.password.isBlank()) {
                        session.addPasswordIdentity(config.password);
                    }
                    logger.debug("opening keys directory {}", privateKeyDir.getName());
                    File[] privateKeyFiles = privateKeyDir.listFiles();
                    if (privateKeyFiles != null) {
                        logger.debug("keys present");
                        for (File privateKeyFile : privateKeyFiles) {
                            logger.debug("loading keys from {}", privateKeyDir.getName());
                            try {
                                FileKeyPairProvider keyPairProvider = new FileKeyPairProvider(
                                        Collections.singletonList(privateKeyFile.toPath()));
                                keyPairProvider.setPasswordFinder(FilePasswordProvider.EMPTY);
                                Iterable<KeyPair> keyPairs = keyPairProvider.loadKeys(null);
                                if (keyPairs.iterator().hasNext()) {
                                    // Add private key identity
                                    session.addPublicKeyIdentity(keyPairs.iterator().next());
                                } else {
                                    logger.warn("No valid key pairs found in {}", privateKeyFile.getName());
                                }
                            } catch (Exception ex) {
                                logger.warn("Skipping file {}: not a valid key file. Reason: {}",
                                        privateKeyFile.getName(), ex.getMessage());
                            }
                        }
                    }

                    session.auth().verify();

                    logger.debug("Connected to the server!");
                    thingReachable = true;
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during refresh: {}", e.getMessage(), e);
            } finally {
                // Disconnect from the server
                if (client.isStarted()) {
                    client.stop();
                }
                logger.debug("Disconnected from the server!");
            }

            // when done do:
            if (thingReachable) {
                logger.debug("ThingStatus.ONLINE");
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("ThingStatus.OFFLINE");
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
