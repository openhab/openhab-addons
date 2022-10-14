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
package org.openhab.persistence.jdbc.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests the {@link NamingStrategy} class.
 *
 * @author Jacob Laursen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class NamingStrategyTest {
    private @Mock @NonNullByDefault({}) JdbcConfiguration configurationMock;
    private NamingStrategy namingStrategy = new NamingStrategy(configurationMock);

    @BeforeEach
    public void initialize() {
        namingStrategy = new NamingStrategy(configurationMock);
    }

    @Test
    public void getTableNameWhenUseRealItemNamesNameIsLowercase() {
        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        assertThat(namingStrategy.getTableName(1, "Test"), is("test"));
    }

    @Test
    public void getTableNameWhenUseRealItemNamesSpecialCharsAreRemoved() {
        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        assertThat(namingStrategy.getTableName(1, "Te%st"), is("test"));
    }

    @Test
    public void getTableNameWhenNotUseRealItemNamesAndCount4NameHasLeavingZeros() {
        Mockito.doReturn(false).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn(4).when(configurationMock).getTableIdDigitCount();
        Mockito.doReturn("Item").when(configurationMock).getTableNamePrefix();
        assertThat(namingStrategy.getTableName(2, "Test"), is("Item0002"));
    }
}
