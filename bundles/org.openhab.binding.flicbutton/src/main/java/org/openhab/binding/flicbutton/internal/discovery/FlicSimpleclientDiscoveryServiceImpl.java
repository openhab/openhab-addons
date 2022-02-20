/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.flicbutton.internal.discovery;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.flicbutton.internal.FlicButtonBindingConstants;
import org.openhab.binding.flicbutton.internal.util.FlicButtonUtils;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.Bdaddr;
import io.flic.fliclib.javaclient.FlicClient;
import io.flic.fliclib.javaclient.GeneralCallbacks;
import io.flic.fliclib.javaclient.GetInfoResponseCallback;
import io.flic.fliclib.javaclient.enums.BdAddrType;
import io.flic.fliclib.javaclient.enums.BluetoothControllerState;

/**
 * For each configured flicd service, there is a {@link FlicSimpleclientDiscoveryServiceImpl} which will be initialized
 * by {@link org.openhab.binding.flicbutton.internal.FlicButtonHandlerFactory}.
 *
 * It can scan for Flic Buttons already that are already added to fliclib-linux-hci ("verified" buttons), *
 * but it does not support adding and verify new buttons on it's own.
 * New buttons have to be added (verified) e.g. via simpleclient by Shortcut Labs.
 * Background discovery listens for new buttons that are getting verified.
 *
 * @author Patrick Fink - Initial contribution
 */
@NonNullByDefault
public class FlicSimpleclientDiscoveryServiceImpl extends AbstractDiscoveryService
        implements FlicButtonDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(FlicSimpleclientDiscoveryServiceImpl.class);

    private boolean activated = false;
    private ThingUID bridgeUID;
    private @Nullable FlicClient flicClient;

    public FlicSimpleclientDiscoveryServiceImpl(ThingUID bridgeUID) {
        super(FlicButtonBindingConstants.SUPPORTED_THING_TYPES_UIDS, 2, true);
        this.bridgeUID = bridgeUID;
    }

    @Override
    public void activate(FlicClient flicClient) {
        this.flicClient = flicClient;
        activated = true;
        super.activate(null);
    }

    @Override
    public void deactivate() {
        activated = false;
        super.deactivate();
    }

    @Override
    protected void startScan() {
        try {
            if (activated) {
                discoverVerifiedButtons();
            }
        } catch (IOException e) {
            logger.warn("Error occured during button discovery", e);
            if (this.scanListener != null) {
                scanListener.onErrorOccurred(e);
            }
        }
    }

    protected void discoverVerifiedButtons() throws IOException {
        flicClient.getInfo(new GetInfoResponseCallback() {
            @Override
            public void onGetInfoResponse(@Nullable BluetoothControllerState bluetoothControllerState,
                    @Nullable Bdaddr myBdAddr, @Nullable BdAddrType myBdAddrType, int maxPendingConnections,
                    int maxConcurrentlyConnectedButtons, int currentPendingConnections,
                    boolean currentlyNoSpaceForNewConnection, Bdaddr @Nullable [] verifiedButtons) throws IOException {
                for (final @Nullable Bdaddr bdaddr : verifiedButtons) {
                    if (bdaddr != null) {
                        flicButtonDiscovered(bdaddr);
                    }
                }
            }
        });
    }

    @Override
    protected void startBackgroundDiscovery() {
        super.startBackgroundDiscovery();
        flicClient.setGeneralCallbacks(new GeneralCallbacks() {
            @Override
            public void onNewVerifiedButton(@Nullable Bdaddr bdaddr) throws IOException {
                logger.debug("A new Flic button was added by an external flicd client: {}", bdaddr);
                if (bdaddr != null) {
                    flicButtonDiscovered(bdaddr);
                }
            }
        });
    }

    @Override
    protected void stopBackgroundDiscovery() {
        super.stopBackgroundDiscovery();
        if (flicClient != null) {
            flicClient.setGeneralCallbacks(null);
        }
    }

    @Override
    public ThingUID flicButtonDiscovered(Bdaddr bdaddr) {
        logger.debug("Flic Button {} discovered!", bdaddr);
        ThingUID flicButtonUID = FlicButtonUtils.getThingUIDFromBdAddr(bdaddr, bridgeUID);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(flicButtonUID).withBridge(bridgeUID)
                .withLabel("Flic Button " + bdaddr.toString().replace(":", ""))
                .withProperty(FlicButtonBindingConstants.CONFIG_ADDRESS, bdaddr.toString())
                .withRepresentationProperty(FlicButtonBindingConstants.CONFIG_ADDRESS).build();
        this.thingDiscovered(discoveryResult);
        return flicButtonUID;
    }
}
