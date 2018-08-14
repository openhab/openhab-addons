/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * utility class which contains useful helper methods. Thus, the interface can be implemented and methods are available
 * within the class.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public interface AtomicReferenceUtils {
    final Logger logger = LoggerFactory.getLogger(AtomicReferenceUtils.class);

    default void cancelJob(@Nullable Future<?> job) {
        if (job != null) {
            job.cancel(true);
        }
    }

    default void updateJobReference(AtomicReference<@Nullable Future<?>> jobReference, Future<?> newJob) {
        cancelJob(jobReference.getAndSet(newJob));
    }

    default void cancelJobReference(AtomicReference<@Nullable Future<?>> jobReference) {
        cancelJob(jobReference.getAndSet(null));
    }

}
