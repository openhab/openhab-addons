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
package org.openhab.binding.knx.internal.client;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXAddress;
import tuwien.auto.calimero.KNXTimeoutException;
import tuwien.auto.calimero.Priority;
import tuwien.auto.calimero.cemi.CEMILData;
import tuwien.auto.calimero.link.KNXLinkClosedException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;

/**
 * This class provides a simulated KNXNetworkLink with test stubs for integration tests.
 *
 * See Calimero documentation, calimero-ng.pdf.
 *
 * Frames sent via {@link #sendRequest()} and {@link sendRequestWait()} will be looped back
 * to all registered listeners. {@link #getLastFrame()} will return the binary data provided
 * to the last send command.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class DummyKNXNetworkLink implements KNXNetworkLink {
    public static final Logger LOGGER = LoggerFactory.getLogger(DummyKNXNetworkLink.class);
    public static final int GROUP_WRITE = 0x80;

    private byte[] lastFrame = new byte[0];
    private Set<@Nullable NetworkLinkListener> listeners = new HashSet<>();

    public void setKNXMedium(@Nullable KNXMediumSettings settings) {
        LOGGER.warn(Objects.toString(settings));
    }

    public KNXMediumSettings getKNXMedium() {
        return KNXMediumSettings.create(KNXMediumSettings.MEDIUM_TP1, new IndividualAddress(1, 2, 3));
    }

    public void addLinkListener(@Nullable NetworkLinkListener l) {
        listeners.add(l);
    }

    public void removeLinkListener(@Nullable NetworkLinkListener l) {
        listeners.remove(l);
    }

    public void setHopCount(int count) {
    }

    public int getHopCount() {
        return 0;
    }

    public void sendRequest(@Nullable KNXAddress dst, @Nullable Priority p, byte @Nullable [] nsdu)
            throws KNXTimeoutException, KNXLinkClosedException {
        sendRequestWait(dst, p, nsdu);
    }

    public void sendRequestWait(@Nullable KNXAddress dst, @Nullable Priority p, byte @Nullable [] nsdu)
            throws KNXTimeoutException, KNXLinkClosedException {
        if (nsdu == null) {
            return;
        }
        LOGGER.info("sendRequestWait() {} {} {}", dst, p, HexUtils.bytesToHex(nsdu, " "));

        lastFrame = nsdu.clone();

        // not we want to mimic a received frame by looping it back to all listeners

        /*
         * relevant steps to create a CEMI frame needed for triggering a frame event:
         *
         * final CEMILData f = (CEMILData) e.getFrame();
         * final var apdu = f.getPayload();
         * final int svc = DataUnitBuilder.getAPDUService(apdu);
         * svc == GROUP_WRITE
         * fireGroupReadWrite(f, DataUnitBuilder.extractASDU(apdu), svc, apdu.length <= 2);
         * send(CEMILData.MC_LDATA_IND, dst, p, nsdu, true);
         */
        int service = GROUP_WRITE;
        byte[] apdu = new byte[nsdu.length + 2];
        apdu[0] = (byte) (service >> 8);
        apdu[1] = (byte) service;
        System.arraycopy(nsdu, 0, apdu, 2, nsdu.length);

        final IndividualAddress src = new IndividualAddress(1, 1, 1);
        final boolean repeat = false;
        final int hopCount = 1;

        FrameEvent f = new FrameEvent(this, new CEMILData(CEMILData.MC_LDATA_IND, src, dst, nsdu, p, repeat, hopCount));

        listeners.forEach(listener -> {
            if (listener != null) {
                listener.indication(f);
            }
        });
    }

    public void send(@Nullable CEMILData msg, boolean waitForCon) throws KNXTimeoutException, KNXLinkClosedException {
        LOGGER.warn("send() not implemented");
    }

    public String getName() {
        return "dummy link";
    }

    public boolean isOpen() {
        return true;
    }

    public void close() {
    }

    public byte[] getLastFrame() {
        return lastFrame;
    }
}
