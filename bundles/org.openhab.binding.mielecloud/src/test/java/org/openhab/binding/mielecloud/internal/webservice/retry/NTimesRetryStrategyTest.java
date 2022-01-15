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
package org.openhab.binding.mielecloud.internal.webservice.retry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceTransientException;

/**
 * @author Björn Lange - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class NTimesRetryStrategyTest {
    private static final int SUCCESSFUL_RETURN_VALUE = 42;

    @Mock
    @Nullable
    private Supplier<@Nullable Integer> operation;

    @Mock
    @Nullable
    private Consumer<Exception> onTransientException;

    private Supplier<@Nullable Integer> getOperation() {
        assertNotNull(operation);
        return Objects.requireNonNull(operation);
    }

    private Consumer<Exception> getOnTransientException() {
        assertNotNull(onTransientException);
        return Objects.requireNonNull(onTransientException);
    }

    @Test
    public void testConstructorThrowsIllegalArgumentExceptionIfNumberOfRetriesIsSmallerThanZero() {
        // when:
        assertThrows(IllegalArgumentException.class, () -> {
            new NTimesRetryStrategy(-1);
        });
    }

    @Test
    public void testSuccessfulOperationReturnsCorrectValue() {
        // given:
        when(getOperation().get()).thenReturn(SUCCESSFUL_RETURN_VALUE);

        NTimesRetryStrategy retryStrategy = new NTimesRetryStrategy(1);

        // when:
        Integer result = retryStrategy.performRetryableOperation(getOperation(), getOnTransientException());

        // then:
        assertEquals(Integer.valueOf(SUCCESSFUL_RETURN_VALUE), result);
        verifyNoMoreInteractions(onTransientException);
    }

    @Test
    public void testFailingOperationReturnsCorrectValueOnRetry() {
        // given:
        when(getOperation().get()).thenThrow(MieleWebserviceTransientException.class)
                .thenReturn(SUCCESSFUL_RETURN_VALUE);

        NTimesRetryStrategy retryStrategy = new NTimesRetryStrategy(1);

        // when:
        Integer result = retryStrategy.performRetryableOperation(getOperation(), getOnTransientException());

        // then:
        assertEquals(Integer.valueOf(SUCCESSFUL_RETURN_VALUE), result);
        verify(getOnTransientException()).accept(any());
        verifyNoMoreInteractions(onTransientException);
    }

    @Test
    public void testFailingOperationReturnsCorrectValueOnSecondRetry() {
        // given:
        when(getOperation().get()).thenThrow(MieleWebserviceTransientException.class)
                .thenThrow(MieleWebserviceTransientException.class).thenReturn(SUCCESSFUL_RETURN_VALUE);

        NTimesRetryStrategy retryStrategy = new NTimesRetryStrategy(2);

        // when:
        Integer result = retryStrategy.performRetryableOperation(getOperation(), getOnTransientException());

        // then:
        assertEquals(Integer.valueOf(SUCCESSFUL_RETURN_VALUE), result);
        verify(getOnTransientException(), times(2)).accept(any());
        verifyNoMoreInteractions(onTransientException);
    }

    @Test
    public void testAlwaysFailingOperationThrowsMieleWebserviceException() {
        // given:
        when(getOperation().get()).thenThrow(MieleWebserviceTransientException.class);

        NTimesRetryStrategy retryStrategy = new NTimesRetryStrategy(1);

        // when:
        try {
            retryStrategy.performRetryableOperation(getOperation(), getOnTransientException());
            fail();
            return;
        } catch (MieleWebserviceException e) {
        }

        // then:
        verify(getOnTransientException()).accept(any());
        verifyNoMoreInteractions(onTransientException);
    }

    @Test
    public void testNullReturnValueDoesNotCauseMultipleRetries() {
        // given:
        when(getOperation().get()).thenReturn(null);

        NTimesRetryStrategy retryStrategy = new NTimesRetryStrategy(1);

        // when:
        retryStrategy.performRetryableOperation(getOperation(), getOnTransientException());

        // then:
        verifyNoInteractions(getOnTransientException());
    }

    @Test
    public void testSuccessfulOperation() {
        // given:
        Runnable operation = mock(Runnable.class);
        doNothing().when(operation).run();

        NTimesRetryStrategy retryStrategy = new NTimesRetryStrategy(1);

        // when:
        retryStrategy.performRetryableOperation(operation, getOnTransientException());

        // then:
        verify(operation).run();
        verifyNoInteractions(getOnTransientException());
    }

    @Test
    public void testFailingOperationCausesRetry() {
        // given:
        Runnable operation = mock(Runnable.class);
        doThrow(MieleWebserviceTransientException.class).doNothing().when(operation).run();

        NTimesRetryStrategy retryStrategy = new NTimesRetryStrategy(1);

        // when:
        retryStrategy.performRetryableOperation(operation, getOnTransientException());

        // then:
        verify(getOnTransientException()).accept(any());
        verify(operation, times(2)).run();
        verifyNoMoreInteractions(getOnTransientException());
    }

    @Test
    public void testTwoTimesFailingOperationCausesTwoRetries() {
        // given:
        Runnable operation = mock(Runnable.class);
        doThrow(MieleWebserviceTransientException.class).doThrow(MieleWebserviceTransientException.class).doNothing()
                .when(operation).run();

        NTimesRetryStrategy retryStrategy = new NTimesRetryStrategy(2);

        // when:
        retryStrategy.performRetryableOperation(operation, getOnTransientException());

        // then:
        verify(getOnTransientException(), times(2)).accept(any());
        verify(operation, times(3)).run();
        verifyNoMoreInteractions(getOnTransientException());
    }

    @Test
    public void testAlwaysFailingRunnableOperationThrowsMieleWebserviceException() {
        // given:
        Runnable operation = mock(Runnable.class);
        doThrow(MieleWebserviceTransientException.class).when(operation).run();

        NTimesRetryStrategy retryStrategy = new NTimesRetryStrategy(1);

        // when:
        try {
            retryStrategy.performRetryableOperation(operation, getOnTransientException());
            fail();
            return;
        } catch (MieleWebserviceException e) {
        }

        // then:
        verify(getOnTransientException()).accept(any());
        verifyNoMoreInteractions(getOnTransientException());
    }
}
