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
package org.openhab.binding.revogi.internal.udp;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.net.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UdpSenderService} is responsible for sending and receiving udp packets
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class UdpSenderService {

    /**
     * Limit timeout waiting time, as we have to deal with UDP
     *
     * How it works: for every loop, we'll wait a bit longer, so the timeout counter is multiplied with the timeout base
     * value. Let max timeout count be 2 and timeout base value 800, then we'll have a maximum of loops of 3, waiting
     * 800ms in the 1st loop, 1600ms in the 2nd loop and 2400ms in the third loop.
     */
    private static final int MAX_TIMEOUT_COUNT = 2;
    private static final long TIMEOUT_BASE_VALUE_MS = 800L;
    private static final int REVOGI_PORT = 8888;

    private final Logger logger = LoggerFactory.getLogger(UdpSenderService.class);
    private final DatagramSocketWrapper datagramSocketWrapper;
    private final ScheduledExecutorService scheduler;
    private final long timeoutBaseValue;

    public UdpSenderService(DatagramSocketWrapper datagramSocketWrapper, ScheduledExecutorService scheduler) {
        this.timeoutBaseValue = TIMEOUT_BASE_VALUE_MS;
        this.datagramSocketWrapper = datagramSocketWrapper;
        this.scheduler = scheduler;
    }

    public UdpSenderService(DatagramSocketWrapper datagramSocketWrapper, long timeout) {
        this.timeoutBaseValue = timeout;
        this.datagramSocketWrapper = datagramSocketWrapper;
        this.scheduler = ThreadPoolManager.getScheduledPool("test pool");
    }

    public CompletableFuture<List<UdpResponseDTO>> broadcastUdpDatagram(String content) {
        List<String> allBroadcastAddresses = NetUtil.getAllBroadcastAddresses();
        CompletableFuture<List<UdpResponseDTO>> future = new CompletableFuture<>();
        scheduler.submit(() -> future.complete(allBroadcastAddresses.stream().map(address -> {
            try {
                return sendMessage(content, InetAddress.getByName(address));
            } catch (UnknownHostException e) {
                logger.warn("Could not find host with IP {}", address);
                return new ArrayList<UdpResponseDTO>();
            }
        }).flatMap(Collection::stream).distinct().collect(toList())));
        return future;
    }

    public CompletableFuture<List<UdpResponseDTO>> sendMessage(String content, String ipAddress) {
        try {
            CompletableFuture<List<UdpResponseDTO>> future = new CompletableFuture<>();
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            scheduler.submit(() -> future.complete(sendMessage(content, inetAddress)));
            return future;
        } catch (UnknownHostException e) {
            logger.warn("Could not find host with IP {}", ipAddress);
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }

    private List<UdpResponseDTO> sendMessage(String content, InetAddress inetAddress) {
        logger.debug("Using address {}", inetAddress);
        byte[] buf = content.getBytes(Charset.defaultCharset());
        DatagramPacket packet = new DatagramPacket(buf, buf.length, inetAddress, REVOGI_PORT);
        List<UdpResponseDTO> responses = Collections.emptyList();
        try {
            datagramSocketWrapper.initSocket();
            datagramSocketWrapper.sendPacket(packet);
            responses = getUdpResponses();
        } catch (IOException e) {
            logger.warn("Error sending message or reading anwser {}", e.getMessage());
        } finally {
            datagramSocketWrapper.closeSocket();
        }
        return responses;
    }

    private List<UdpResponseDTO> getUdpResponses() {
        int timeoutCounter = 0;
        List<UdpResponseDTO> list = new ArrayList<>();
        while (timeoutCounter < MAX_TIMEOUT_COUNT && !Thread.interrupted()) {
            byte[] receivedBuf = new byte[512];
            DatagramPacket answer = new DatagramPacket(receivedBuf, receivedBuf.length);
            try {
                datagramSocketWrapper.receiveAnswer(answer);
            } catch (SocketTimeoutException | SocketException e) {
                timeoutCounter++;
                try {
                    TimeUnit.MILLISECONDS.sleep(timeoutCounter * timeoutBaseValue);
                } catch (InterruptedException ex) {
                    logger.debug("Interrupted sleep");
                    Thread.currentThread().interrupt();
                }
                continue;
            } catch (IOException e) {
                logger.warn("Error sending message or reading anwser {}", e.getMessage());
            }

            if (answer.getAddress() != null && answer.getLength() > 0) {
                list.add(new UdpResponseDTO(new String(answer.getData(), 0, answer.getLength()),
                        answer.getAddress().getHostAddress()));
            }
        }
        return list;
    }
}
