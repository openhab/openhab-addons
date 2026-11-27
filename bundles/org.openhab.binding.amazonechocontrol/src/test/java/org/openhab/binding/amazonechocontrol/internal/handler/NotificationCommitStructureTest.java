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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Guards that accepting a notification poll result stays one step: the verdict, the publication and the
 * next-poll time under {@code notificationCommit}, the network call outside it. Asserted against the source
 * because this bundle has no harness that can instantiate {@link AccountHandler}.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
class NotificationCommitStructureTest {

    private static final Path SOURCE = Path
            .of("src/main/java/org/openhab/binding/amazonechocontrol/internal/handler/AccountHandler.java");

    private static String refreshNotifications() throws IOException {
        String source = Files.readString(SOURCE);
        int start = source.indexOf("void refreshNotifications()");
        assertTrue(start > 0, "refreshNotifications() not found - this test cannot guard what it cannot read");
        return source.substring(start, source.indexOf("\n    private void refreshData()", start));
    }

    /** The {@code notificationCommit} block that encloses the given marker. */
    private static String guardedBlock(String body, String marker) {
        int at = body.indexOf(marker);
        assertTrue(at > 0, marker + " not found - this test is testing nothing");
        int start = body.lastIndexOf("synchronized (notificationCommit) {", at);
        assertTrue(start > 0, marker + " is not preceded by a notificationCommit block");
        int depth = 0;
        for (int i = body.indexOf('{', start); i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}' && --depth == 0) {
                return body.substring(start, i);
            }
        }
        throw new IllegalStateException("unbalanced braces");
    }

    @Test
    void theResultIsPublishedInsideTheGuardedBlock() throws IOException {
        String body = refreshNotifications();
        String block = guardedBlock(body, "notificationPollBackoff.onSuccess");

        assertTrue(block.contains("notificationPollBackoff.onSuccess"), "the verdict belongs in the block");
        assertTrue(block.contains("updateNotifications(notifications)"),
                "publishing outside the block lets a replaced connection publish after its result was rejected");
        assertTrue(block.contains("nextRefreshNotifications = "),
                "the next-poll time outside the block would overwrite the reset a new session depends on");
    }

    @Test
    void theFailureVerdictAndItsUndefAreOneStep() throws IOException {
        String body = refreshNotifications();
        String block = guardedBlock(body, "notificationPollBackoff.onFailure");

        assertTrue(block.contains("notificationPollBackoff.onFailure"), "the verdict belongs in the block");
        assertTrue(block.contains("updateNotifications(List.of())"),
                "UNDEF outside the block can reach the channels after the connection was replaced");
    }

    @Test
    void theNetworkCallStaysOutsideTheGuardedBlock() throws IOException {
        String body = refreshNotifications();
        int poll = body.indexOf("connection.getNotifications()");
        assertTrue(poll > 0, "the poll itself is gone - this test is testing nothing");
        assertTrue(poll < body.indexOf("synchronized (notificationCommit) {"),
                "holding the commit lock across the HTTP call would block setConnection() for the request timeout");
    }

    @Test
    void setConnectionResetsUnderTheSameLock() throws IOException {
        String source = Files.readString(SOURCE);
        int start = source.indexOf("public void setConnection(Connection newConnection)");
        assertTrue(start > 0, "setConnection() not found");
        String body = source.substring(start, source.indexOf("\n    private void storeSession()", start));
        String block = guardedBlock(body, "notificationPollBackoff.reset()");

        assertTrue(block.contains("notificationPollBackoff.reset()") && block.contains("nextRefreshNotifications = 0"),
                "the reset must be atomic against a poll committing its result");
    }
}
