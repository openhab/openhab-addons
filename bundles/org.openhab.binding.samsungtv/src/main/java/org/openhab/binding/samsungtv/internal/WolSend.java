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
package org.openhab.binding.samsungtv.internal;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.*;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.samsungtv.internal.handler.SamsungTvHandler;
import org.openhab.binding.samsungtv.internal.service.RemoteControllerService;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WolSend} is responsible for sending WOL packets and resending of commands
 * Used by {@link SamsungTvHandler}
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
public class WolSend {

    private static final int WOL_PACKET_RETRY_COUNT = 10;
    private static final int WOL_SERVICE_CHECK_COUNT = 30;

    private final Logger logger = LoggerFactory.getLogger(WolSend.class);

    private String host = "";

    int wolCount = 0;
    String channel = POWER;
    Command command = OnOffType.ON;
    String macAddress = "";
    private Optional<ScheduledFuture<?>> wolJob = Optional.empty();
    SamsungTvHandler handler;

    public WolSend(SamsungTvHandler handler) {
        this.handler = handler;
    }

    /**
     * Send multiple WOL packets spaced with 100ms intervals and resend command
     *
     * @param channel Channel to resend command on
     * @param command Command to resend
     * @return boolean true/false if WOL job started
     */
    public boolean send(String channel, Command command) {
        this.host = handler.host;
        if (channel.equals(POWER) || channel.equals(ART_MODE)) {
            if (OnOffType.ON.equals(command)) {
                macAddress = handler.configuration.getMacAddress();
                if (macAddress.isBlank()) {
                    logger.debug("{}: Cannot send WOL packet, MAC address invalid: {}", host, macAddress);
                    return false;
                }
                this.channel = channel;
                this.command = command;
                if (channel.equals(ART_MODE) && !handler.getArtModeSupported()) {
                    logger.debug("{}: artMode is not yet detected on this TV - sending WOL anyway", host);
                }
                startWoljob();
                return true;
            } else {
                cancel();
            }
        }
        return false;
    }

    private void startWoljob() {
        wolJob.ifPresentOrElse(job -> {
            if (job.isCancelled()) {
                start();
            } else {
                logger.debug("{}: WOL job already running", host);
            }
        }, () -> {
            start();
        });
    }

    public void start() {
        wolCount = 0;
        wolJob = Optional.of(
                handler.getScheduler().scheduleWithFixedDelay(this::wolCheckPeriodic, 0, 1000, TimeUnit.MILLISECONDS));
    }

    public synchronized void cancel() {
        wolJob.ifPresent(job -> {
            logger.debug("{}: cancelling WOL Job", host);
            job.cancel(true);
        });
    }

    private void sendWOL() {
        logger.debug("{}: Send WOL packet to {}", host, macAddress);

        // send max 10 WOL packets with 100ms intervals
        for (int i = 0; i < WOL_PACKET_RETRY_COUNT; i++) {
            handler.getScheduler().schedule(() -> {
                WakeOnLanUtility.sendWOLPacket(macAddress);
            }, (i * 100), TimeUnit.MILLISECONDS);
        }
    }

    private void sendCommand(RemoteControllerService service) {
        // send command in 2 seconds to allow time for connection to re-establish
        logger.debug("{}: resend command {} to channel {} in 2 seconds...", host, command, channel);
        handler.getScheduler().schedule(() -> {
            service.handleCommand(channel, command);
        }, 2000, TimeUnit.MILLISECONDS);
    }

    private void wolCheckPeriodic() {
        if (wolCount % 10 == 0) {
            // resend WOL every 10 seconds
            sendWOL();
        }
        // after RemoteService up again to ensure state is properly set
        Optional<SamsungTvService> service = handler.findServiceInstance(RemoteControllerService.SERVICE_NAME);
        service.ifPresent(s -> {
            logger.debug("{}: RemoteControllerService found after {} attempts", host, wolCount);
            // do not resend command if artMode command as TV wakes up in artMode
            if (!channel.equals(ART_MODE)) {
                sendCommand((RemoteControllerService) s);
            }
            cancel();
        });
        // cancel job
        if (wolCount++ > WOL_SERVICE_CHECK_COUNT) {
            logger.warn("{}: Service NOT found after {} attempts: stopping WOL attempts", host, wolCount);
            cancel();
            handler.putOffline();
        }
    }
}
