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
package org.openhab.voice.mactts.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openhab.core.audio.AudioException;

/**
 * Test class for {@link MacTTSAudioStream}
 *
 * @author Kai Kreuzer - Initial contribution
 */
class MacTTSAudioStreamTest {

    @Test
    void runWithTimeoutReturnsExitCodeOnSuccess() throws Exception {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        Process process = new ProcessBuilder("/bin/sh", "-c", "exit 0").start();
        assertEquals(0, MacTTSAudioStream.runWithTimeout(process, 10, TimeUnit.SECONDS));
    }

    @Test
    void runWithTimeoutReturnsNonZeroExitCode() throws Exception {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        Process process = new ProcessBuilder("/bin/sh", "-c", "exit 7").start();
        assertEquals(7, MacTTSAudioStream.runWithTimeout(process, 10, TimeUnit.SECONDS));
    }

    @Test
    void runWithTimeoutDestroysHungProcess() throws Exception {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        Process process = new ProcessBuilder("/bin/sh", "-c", "exec sleep 30").start();
        assertThrows(AudioException.class, () -> MacTTSAudioStream.runWithTimeout(process, 200, TimeUnit.MILLISECONDS));
        assertFalse(process.isAlive());
    }

    @Test
    void runWithTimeoutDestroysProcessOnInterruption() throws Exception {
        assumeTrue("Mac OS X".equals(System.getProperty("os.name")));
        Process process = new ProcessBuilder("/bin/sh", "-c", "exec sleep 30").start();
        Thread worker = new Thread(() -> {
            try {
                MacTTSAudioStream.runWithTimeout(process, 60, TimeUnit.SECONDS);
                fail("Expected InterruptedException");
            } catch (InterruptedException e) {
                // expected
            } catch (AudioException e) {
                fail("Unexpected AudioException", e);
            }
        });
        worker.start();
        // Give the worker some time to enter waitFor
        Thread.sleep(200);
        worker.interrupt();
        worker.join(5000);
        assertFalse(worker.isAlive());
        assertProcessTerminated(process);
    }

    /**
     * Asserts that the process terminates within a bounded time.
     */
    private static void assertProcessTerminated(Process process) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (process.isAlive() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertFalse(process.isAlive());
    }
}
