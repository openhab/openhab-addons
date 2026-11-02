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
package org.openhab.binding.samsungtv.internal.service;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.SOURCE_NAME;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.samsungtv.internal.handler.SamsungTvHandler;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IpControlService} reads and sets the input source using Samsung IP Control. It is local and current, so
 * when it has reported an input, SmartThings no longer updates sourceName.
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
public class IpControlService implements SamsungTvService {

    public static final String SERVICE_NAME = "IpControl";
    private static final List<String> SUPPORTED_CHANNELS = List.of(SOURCE_NAME);
    // the TV takes up to 0.5 s to answer, so don't read the input on every poll
    private static final long REFRESH_INTERVAL_MS = 10000;

    private final Logger logger = LoggerFactory.getLogger(IpControlService.class);
    private final SamsungTvHandler handler;

    private volatile long nextRefresh = 0;
    private volatile boolean reportingInput = false;
    private volatile String inputSource = "";

    public IpControlService(String host, SamsungTvHandler handler) {
        this.handler = handler;
        logger.debug("{}: Creating a Samsung TV IP Control service", host);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public List<String> getSupportedChannelNames(boolean refresh) {
        return SUPPORTED_CHANNELS;
    }

    /**
     * @return true once IP Control has reported an input, so other services should not update sourceName
     */
    public boolean isReportingInput() {
        return reportingInput;
    }

    @Override
    public boolean handleCommand(String channel, Command command) {
        if (!SOURCE_NAME.equals(channel) || !checkConnection()) {
            return false;
        }
        if (command == RefreshType.REFRESH) {
            long now = System.currentTimeMillis();
            // power is off in art mode, when there is no input to report
            if (handler.getPowerState() && now >= nextRefresh) {
                nextRefresh = now + REFRESH_INTERVAL_MS;
                handler.ipControlInputSource().ifPresent(this::updateInputSource);
            }
            return true;
        }
        if (command instanceof StringType && handler.ipControlSetInputSource(command.toString())) {
            updateInputSource(command.toString());
            return true;
        }
        // autoupdate has already set the item, so post the next read even if the input did not change
        inputSource = "";
        nextRefresh = 0;
        // let SmartThings try, it knows other names for the inputs
        return false;
    }

    private void updateInputSource(String source) {
        reportingInput = true;
        if (!source.equals(inputSource)) {
            inputSource = source;
            handler.valueReceived(SOURCE_NAME, new StringType(source));
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void clearCache() {
        nextRefresh = 0;
        inputSource = "";
    }

    @Override
    public boolean isUpnp() {
        return false;
    }

    @Override
    public boolean checkConnection() {
        return handler.isIpControlReady();
    }
}
