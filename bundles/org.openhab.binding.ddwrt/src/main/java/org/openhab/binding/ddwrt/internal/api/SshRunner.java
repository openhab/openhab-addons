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
package org.openhab.binding.ddwrt.internal.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.EnumSet;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SshRunner} executing command in a ssh session.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class SshRunner implements AutoCloseable {
    private final ClientSession session;
    private final Duration defaultTimeout;

    private final Logger logger = LoggerFactory.getLogger(SshRunner.class);

    public SshRunner(ClientSession session, Duration defaultTimeout) {
        this.session = session;
        this.defaultTimeout = defaultTimeout;
    }

    public String exec(String command, Duration timeout) throws IOException {
        logger.debug("{} executing command: {}", session.getRemoteAddress(), command);
        try (ChannelExec ch = session.createExecChannel(command)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            ch.setOut(out);
            ch.setErr(err);
            ch.open().verify();
            ch.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), timeout.toMillis());
            Integer rc = ch.getExitStatus();
            if (rc != null && rc == 0) {
                logger.debug("{} {}", rc, out.toString(StandardCharsets.UTF_8));
                return out.toString(StandardCharsets.UTF_8);
            }
            logger.debug("{} {}", rc, err.toString(StandardCharsets.UTF_8));
            throw new IOException("Command failed rc=" + rc + ", stderr=" + err.toString(StandardCharsets.UTF_8));
        }
    }

    public String exec(String command) throws IOException {
        return exec(command, defaultTimeout);
    }

    @Override
    public void close() throws Exception {
        session.close();
    }
}
