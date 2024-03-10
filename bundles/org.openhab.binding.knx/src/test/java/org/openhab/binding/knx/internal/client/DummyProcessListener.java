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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListener;

/**
 * This implementation of {@link ProcessListener} caches a received frames.
 *
 * It can be registered to {@link DummyKNXNetworkLink} to receive raw frame data.
 *
 * @author Holger Friedrich - Initial contribution
 *
 */
@NonNullByDefault
public class DummyProcessListener implements ProcessListener {
    private byte[] lastFrame = new byte[0];
    public static final Logger LOGGER = LoggerFactory.getLogger(DummyProcessListener.class);

    public DummyProcessListener() {
    }

    @Override
    public void detached(@Nullable DetachEvent e) {
        LOGGER.info("The KNX network link was detached from the process communicator");
    }

    @Override
    public void groupWrite(@Nullable ProcessEvent e) {
        if (e == null) {
            lastFrame = new byte[0];
            LOGGER.warn("invalid ProcessEvent");
            return;
        }
        LOGGER.info("groupWrite({})", e.toString());
        lastFrame = e.getASDU(); // clones
    }

    @Override
    public void groupReadRequest(@Nullable ProcessEvent e) {
        if (e == null) {
            lastFrame = new byte[0];
            LOGGER.warn("invalid ProcessEvent");
            return;
        }
        LOGGER.warn("groupReadRequest({})", e.toString());
        lastFrame = e.getASDU(); // clones
    }

    @Override
    public void groupReadResponse(@Nullable ProcessEvent e) {
        if (e == null) {
            lastFrame = new byte[0];
            LOGGER.warn("invalid ProcessEvent");
            return;
        }
        LOGGER.warn("groupReadResponse({})", e.toString());
        lastFrame = e.getASDU(); // clones
    }

    public byte[] getLastFrame() {
        return lastFrame;
    }
}
