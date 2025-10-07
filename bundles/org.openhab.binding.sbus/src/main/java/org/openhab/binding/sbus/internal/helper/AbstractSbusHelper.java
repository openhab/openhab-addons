/*
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
package org.openhab.binding.sbus.internal.helper;

import org.openhab.binding.sbus.internal.handler.AbstractSbusHandler;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ciprianpascu.sbus.msg.SbusResponse;

/**
 * The {@link AbstractSbusHelper} is a base class for SBUS sensor helpers.
 * Unlike full handlers, these helpers are lightweight processors that only handle
 * data processing without managing their own lifecycle, polling, or message listening.
 * They are managed by the main Sbus*Handler coordinator.
 *
 * @author Ciprian Pascu - Initial contribution
 */
public abstract class AbstractSbusHelper {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Thing thing;
    protected final AbstractSbusHandler coordinator;

    public AbstractSbusHelper(Thing thing, AbstractSbusHandler coordinator) {
        this.thing = thing;
        this.coordinator = coordinator;
    }

    /**
     * Initialize the helper. This should set up any necessary state but
     * should NOT register listeners or start polling jobs.
     */
    public abstract void initialize();

    /**
     * Process an asynchronous SBUS message.
     * This method should use pattern matching to handle different message types.
     *
     * @param response the SBUS response message to process
     */
    public abstract void processMessage(SbusResponse response);

    /**
     * Check if this helper handles any of the configured channels.
     *
     * @return true if this helper should be active for the current thing configuration
     */
    public abstract boolean hasRelevantChannels();

    /**
     * Dispose any resources held by this helper.
     * This should NOT dispose the thing or update thing status.
     */
    public void dispose() {
        // Default implementation does nothing
        logger.debug("Disposed SBUS helper for {}", thing.getUID());
    }
}
