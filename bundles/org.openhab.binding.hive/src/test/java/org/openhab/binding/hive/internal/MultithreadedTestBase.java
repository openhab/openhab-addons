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
package org.openhab.binding.hive.internal;

import java.util.Objects;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public abstract class MultithreadedTestBase {
    private static final String PHASER_NULL_MESSAGE = "Phaser has not been set up yet";

    private final long awaitTimeout;
    private final TimeUnit awaitTimeUnit;

    private @Nullable Phaser testingPhaser;

    public MultithreadedTestBase(
            final long awaitTimeout,
            final TimeUnit awaitTimeUnit
    ) {
        if (awaitTimeout <= 0) {
            throw new IllegalArgumentException("Timeout must be greater than 0.");
        }
        Objects.requireNonNull(awaitTimeUnit);

        this.awaitTimeout = awaitTimeout;
        this.awaitTimeUnit = awaitTimeUnit;
    }

    @Before
    public void setUpTestingPhaser() {
        final Phaser testingPhaser = new Phaser();
        testingPhaser.register();
        this.testingPhaser = testingPhaser;
    }

    protected Phaser getTestingPhaser() {
        final @Nullable Phaser testingPhaser = this.testingPhaser;
        if (testingPhaser == null) {
            throw new IllegalStateException(PHASER_NULL_MESSAGE);
        }

        return testingPhaser;
    }

    protected void awaitTestingPhaser() throws TimeoutException, InterruptedException {
        final @Nullable Phaser testingPhaser = this.testingPhaser;
        if (testingPhaser == null) {
            throw new IllegalStateException(PHASER_NULL_MESSAGE);
        }

        testingPhaser.awaitAdvanceInterruptibly(
                this.testingPhaser.arrive(),
                this.awaitTimeout,
                this.awaitTimeUnit
        );
    }
}
