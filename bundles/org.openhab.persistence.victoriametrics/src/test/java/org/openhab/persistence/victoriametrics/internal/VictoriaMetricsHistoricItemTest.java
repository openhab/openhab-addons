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
package org.openhab.persistence.victoriametrics.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;

/**
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VictoriaMetricsHistoricItemTest {

    @Test
    public void toStringContainsNameAndState() {
        VictoriaMetricsHistoricItem item = new VictoriaMetricsHistoricItem("MyItem", new DecimalType(5),
                Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(item.toString(), allOf(containsString("2026-01-01"), endsWith(": MyItem -> 5")));
    }
}
