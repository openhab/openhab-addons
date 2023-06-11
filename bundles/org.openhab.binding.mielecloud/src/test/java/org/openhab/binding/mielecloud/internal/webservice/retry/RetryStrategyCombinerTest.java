/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.retry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.openhab.binding.mielecloud.internal.util.MockUtil;

/**
 * @author Björn Lange - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class RetryStrategyCombinerTest {
    private static final String STRING_CONSTANT = "Some String";

    private final RetryStrategy first = mock(RetryStrategy.class);
    private final RetryStrategy second = mock(RetryStrategy.class);

    @Mock
    @Nullable
    private Supplier<@Nullable String> supplier;
    @Mock
    @Nullable
    private Consumer<Exception> consumer;

    private Supplier<@Nullable String> getSupplier() {
        assertNotNull(supplier);
        return Objects.requireNonNull(supplier);
    }

    private Consumer<Exception> getConsumer() {
        assertNotNull(consumer);
        return Objects.requireNonNull(consumer);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPerformRetryableOperationInvokesRetryStrategiesInCorrectOrder() {
        // given:
        when(first.<@Nullable String> performRetryableOperation(any(Supplier.class), any()))
                .thenAnswer(new Answer<@Nullable String>() {
                    @Override
                    @Nullable
                    public String answer(@Nullable InvocationOnMock invocation) throws Throwable {
                        Supplier<String> inner = MockUtil.requireNonNull(invocation).getArgument(0);
                        return inner.get();
                    }
                });
        when(second.<@Nullable String> performRetryableOperation(any(Supplier.class), any()))
                .thenAnswer(new Answer<@Nullable String>() {
                    @Override
                    @Nullable
                    public String answer(@Nullable InvocationOnMock invocation) throws Throwable {
                        Supplier<String> inner = MockUtil.requireNonNull(invocation).getArgument(0);
                        return inner.get();
                    }
                });
        when(getSupplier().get()).thenReturn(STRING_CONSTANT);

        RetryStrategyCombiner combiner = new RetryStrategyCombiner(first, second);

        // when:
        String result = combiner.performRetryableOperation(getSupplier(), getConsumer());

        // then:
        assertEquals(STRING_CONSTANT, result);
        verify(first).performRetryableOperation(any(Supplier.class), eq(getConsumer()));
        verify(second).performRetryableOperation(any(Supplier.class), eq(getConsumer()));
        verify(getSupplier()).get();
        verifyNoMoreInteractions(first, second, getSupplier());
        verifyNoInteractions(getConsumer());
    }
}
