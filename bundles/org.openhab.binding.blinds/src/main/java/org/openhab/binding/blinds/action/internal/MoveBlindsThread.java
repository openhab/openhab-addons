/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.blinds.action.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that is resposible to execute all blind jobs
 *
 * @author Markus Pfleger - Initial contribution
 *
 */
public class MoveBlindsThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(MoveBlindsThread.class);

    private AtomicBoolean shutdown = new AtomicBoolean(false);

    private final List<MoveBlindJob> newBlindJobs = new ArrayList<>();
    private final Map<String, List<MoveBlindJob>> blindJobs = new HashMap<>();

    final Lock lock = new ReentrantLock();
    final Condition workAvailable = lock.newCondition();

    public void scheduleJob(MoveBlindJob job) {
        lock.lock();
        try {
            newBlindJobs.add(job);
            workAvailable.signalAll();
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void run() {
        while (!shutdown.get()) {
            try {
                lock.lock();
                try {
                    while (newBlindJobs.isEmpty() && blindJobs.isEmpty()) {
                        workAvailable.await(5, TimeUnit.SECONDS);
                    }

                    // copy the newly arrived jobs
                    for (MoveBlindJob job : newBlindJobs) {
                        List<MoveBlindJob> jobs = blindJobs.computeIfAbsent(job.getRollershutterName(),
                                k -> new ArrayList<>());
                        jobs.add(job);
                    }
                    newBlindJobs.clear();
                } finally {
                    lock.unlock();
                }

                Iterator<Entry<String, List<MoveBlindJob>>> rollershutterIterator = blindJobs.entrySet().iterator();

                while (rollershutterIterator.hasNext()) {
                    Entry<String, List<MoveBlindJob>> entry = rollershutterIterator.next();
                    List<MoveBlindJob> jobsForRollershutter = entry.getValue();
                    assert jobsForRollershutter.isEmpty();
                    if (!jobsForRollershutter.isEmpty()) {
                        MoveBlindJob currentJob = jobsForRollershutter.get(0);

                        try {
                            boolean continueImmediately = false;
                            do {
                                continueImmediately = currentJob.execute();
                            } while (continueImmediately);

                            if (currentJob.isFinished()) {
                                jobsForRollershutter.remove(0);
                            }

                        } catch (Exception e) {
                            logger.warn("Exception occurred while executing job: {}. Aborting job...",
                                    currentJob.toString(), e);
                            jobsForRollershutter.remove(0);
                        }

                        if (jobsForRollershutter.isEmpty()) {
                            rollershutterIterator.remove();
                        }
                    }

                }
                Thread.sleep(100);
            } catch (Exception e) {
                logger.warn("Exception occurred in MoveBlindsThread", e);
            }
        }
    }

    public void shutdown() {
        shutdown.set(true);
    }
}