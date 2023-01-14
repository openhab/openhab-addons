/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.connection;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.common.LcnAddrGrp;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This state discovers the LCN segment couplers.
 *
 * After the authorization against the LCN-PCK gateway was successful, the LCN segment couplers are discovery, to
 * retrieve the segment ID of the local segment. When no segment couplers were found, a timeout sets the local segment
 * ID to 0.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateSegmentScan extends AbstractConnectionState {
    private final Logger logger = LoggerFactory.getLogger(ConnectionStateSegmentScan.class);
    public static final Pattern PATTERN_SK_RESPONSE = Pattern
            .compile("=M(?<segId>\\d{3})(?<modId>\\d{3})\\.SK(?<id>\\d+)");
    private final RequestStatus statusSegmentScan = new RequestStatus(-1, 3, "Segment Scan");

    public ConnectionStateSegmentScan(ConnectionStateMachine context) {
        super(context);
    }

    @Override
    public void startWorking() {
        statusSegmentScan.refresh();
        addTimer(getScheduler().scheduleWithFixedDelay(this::update, 0, 500, TimeUnit.MILLISECONDS));
    }

    private void update() {
        long currTime = System.currentTimeMillis();
        try {
            if (statusSegmentScan.shouldSendNextRequest(connection.getSettings().getTimeout(), currTime)) {
                connection.queueDirectly(new LcnAddrGrp(3, 3), false, PckGenerator.segmentCouplerScan());
                statusSegmentScan.onRequestSent(currTime);
            }
        } catch (LcnException e) {
            // Give up. Probably no segments available.
            connection.setLocalSegId(0);
            logger.debug("No segment couplers detected");
            nextState(ConnectionStateConnected::new);
        }
    }

    @Override
    public void onPckMessageReceived(String data) {
        Matcher matcher = PATTERN_SK_RESPONSE.matcher(data);

        if (matcher.matches()) {
            // any segment coupler answered
            if (Integer.parseInt(matcher.group("segId")) == 0) {
                // local segment coupler answered
                connection.setLocalSegId(Integer.parseInt(matcher.group("id")));
                logger.debug("Local segment ID is {}", connection.getLocalSegId());
                nextState(ConnectionStateConnected::new);
            }
        }
        parseLcnBusDiconnectMessage(data);
    }
}
