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
package org.openhab.binding.knx.internal.monitor;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.DataUnitBuilder;
import io.calimero.FrameEvent;
import io.calimero.GroupAddress;
import io.calimero.cemi.CEMILData;
import io.calimero.link.NetworkLinkListener;

/**
 * The monitor's listener on the secure layer of one connection. The layer hands it every group telegram that
 * passed the security check, already decrypted, with the security control set for a decrypted one.
 * <p>
 * It runs on the link's notifier thread, the one that also feeds the Things, so it only appends to the monitor. It
 * catches everything itself: the library removes a listener that throws for the rest of the connection.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class IncomingTap implements NetworkLinkListener {
    private static final int GROUP_READ = 0x00;
    private static final int GROUP_RESPONSE = 0x40;
    private static final int GROUP_WRITE = 0x80;

    private final Logger logger = LoggerFactory.getLogger(IncomingTap.class);
    private final TelegramMonitor monitor;
    private final Predicate<GroupAddress> hasGroupKey;

    /**
     * @param hasGroupKey tells whether the keyring has a group key for an address; a plain telegram to such an address
     *            is marked, and still delivered
     */
    public IncomingTap(TelegramMonitor monitor, Predicate<GroupAddress> hasGroupKey) {
        this.monitor = monitor;
        this.hasGroupKey = hasGroupKey;
    }

    @Override
    public void indication(@Nullable FrameEvent e) {
        try {
            if (e != null) {
                record(e);
            }
        } catch (RuntimeException ex) {
            logger.debug("The group monitor could not record a telegram: {}", ex.toString());
        }
    }

    private void record(FrameEvent e) {
        if (!(e.getFrame() instanceof CEMILData frame) || !(frame.getDestination() instanceof GroupAddress destination)
                || destination.getRawAddress() == 0) {
            return;
        }
        byte[] apdu = frame.getPayload();
        if (apdu.length < 2) {
            return;
        }
        TelegramType type = switch (DataUnitBuilder.getAPDUService(apdu)) {
            case GROUP_READ -> TelegramType.READ;
            case GROUP_RESPONSE -> TelegramType.RESPONSE;
            case GROUP_WRITE -> TelegramType.WRITE;
            default -> null;
        };
        if (type == null) {
            return;
        }
        byte[] asdu = type == TelegramType.READ ? new byte[0] : DataUnitBuilder.extractASDU(apdu);
        boolean secured = e.security().isPresent();
        monitor.received(frame.getSource(), destination, type, asdu,
                secured ? SecurityState.SECURED : SecurityState.PLAIN, !secured && hasGroupKey.test(destination));
    }
}
