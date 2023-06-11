/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.development;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a helper class for dealing with multiple threads and synchronization.
 *
 * It provides the following methods:
 * <ul>
 * <li>{@link #findDeadlocked} to print the current locking situation.</li>
 * </ul>
 * <p>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class Threads {
    private static final Logger LOGGER = LoggerFactory.getLogger(Threads.class);

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * Suppress default constructor for creating a non-instantiable class.
     */
    private Threads() {
        throw new AssertionError();
    }

    // Class access methods

    /**
     * Print the current established locks with the associated threads.
     * <P>
     * Finds cycles of threads that are in deadlock waiting to acquire
     * object monitors or ownable synchronizers.
     *
     * Threads are <em>deadlocked</em> in a cycle waiting for a lock of
     * these two types if each thread owns one lock while
     * trying to acquire another lock already held
     * by another thread in the cycle.
     * <p>
     * This method is designed for troubleshooting use, but not for
     * synchronization control. It might be an expensive operation.
     *
     * @see ThreadMXBean#findDeadlockedThreads
     */
    public static void findDeadlocked() {
        ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
        long[] ids = tmx.findDeadlockedThreads();
        if (ids != null) {
            ThreadInfo[] infos = tmx.getThreadInfo(ids, true, true);
            LOGGER.warn("findDeadlocked() The following threads are deadlocked:");
            for (ThreadInfo ti : infos) {
                LOGGER.warn("findDeadlocked(): {}.", ti);
            }
            LOGGER.warn("findDeadlocked() done.");
        }
    }
}
