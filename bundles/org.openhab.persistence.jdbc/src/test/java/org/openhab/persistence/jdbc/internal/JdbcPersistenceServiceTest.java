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
package org.openhab.persistence.jdbc.internal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.FilterCriteria;

/**
 * Tests the {@link JdbcPersistenceService}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class JdbcPersistenceServiceTest {

    private final JdbcPersistenceService jdbcPersistenceService = new JdbcPersistenceService(mock(ItemRegistry.class),
            mock(TimeZoneProvider.class)) {
        @Override
        protected boolean checkDBAccessability() {
            return true;
        }
    };
    private @NonNullByDefault({}) FilterCriteria filter;

    @BeforeEach
    public void setup() {
        filter = new FilterCriteria();
    }

    @Test
    void removeThrowsIllegalArgumentExceptionIfItemNameOfFilterIsNull() {
        assertThrows(IllegalArgumentException.class, () -> jdbcPersistenceService.remove(filter));
    }
}
