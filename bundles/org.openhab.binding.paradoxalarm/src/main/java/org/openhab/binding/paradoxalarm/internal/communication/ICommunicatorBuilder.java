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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.util.concurrent.ScheduledExecutorService;

/**
 * The {@link ICommunicatorBuilder} is representing the functionality of communicator builders.
 * The idea is to ease initialization of communicators which can have lots of parameters.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public interface ICommunicatorBuilder {
    ICommunicatorBuilder withMaxZones(Integer zones);

    ICommunicatorBuilder withMaxPartitions(Integer partitions);

    ICommunicatorBuilder withIp150Password(String ip150Password);

    ICommunicatorBuilder withPcPassword(String pcPassword);

    ICommunicatorBuilder withIpAddress(String ipAddress);

    ICommunicatorBuilder withTcpPort(Integer tcpPort);

    ICommunicatorBuilder withScheduler(ScheduledExecutorService scheduler);

    ICommunicatorBuilder withEncryption(boolean useEncryption);

    IParadoxCommunicator build();
}
