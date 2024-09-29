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
import org.openhab.binding.mielecloud.internal.MieleCloudBindingTestConstants;
import org.openhab.binding.mielecloud.internal.auth.OAuthException;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.webservice.exception.AuthorizationFailedException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;

/**
 * @author Björn Lange - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class AuthorizationFailedRetryStrategyTest {
    private static final String TEST_STRING = "Some Test String";

    @Mock
    @Nullable
    private Supplier<@Nullable String> operationWithReturnValue;
    @Mock
    @Nullable
    private Consumer<Exception> onException;
    @Mock
    @Nullable
    private Runnable operation;

    private final OAuthTokenRefresher refresher = mock(OAuthTokenRefresher.class);

    private Supplier<@Nullable String> getOperationWithReturnValue() {
        assertNotNull(operationWithReturnValue);
        return Objects.requireNonNull(operationWithReturnValue);
    }

    private Consumer<Exception> getOnException() {
        assertNotNull(onException);
        return Objects.requireNonNull(onException);
    }

    private Runnable getOperation() {
        assertNotNull(operation);
        return Objects.requireNonNull(operation);
    }

    @Test
    public void testPerformRetryableOperationWithReturnValueInvokesOperation() {
        // given:
        when(getOperationWithReturnValue().get()).thenReturn(TEST_STRING);

        AuthorizationFailedRetryStrategy retryStrategy = new AuthorizationFailedRetryStrategy(refresher,
                MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        String result = retryStrategy.performRetryableOperation(getOperationWithReturnValue(), getOnException());

        // then:
        assertEquals(TEST_STRING, result);
    }

    @Test
    public void testPerformRetryableOperationWithReturnValueInvokesRefreshTokenAndRetriesOperation() {
        // given:
        when(getOperationWithReturnValue().get()).thenThrow(AuthorizationFailedException.class).thenReturn(TEST_STRING);

        AuthorizationFailedRetryStrategy retryStrategy = new AuthorizationFailedRetryStrategy(refresher,
                MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        String result = retryStrategy.performRetryableOperation(getOperationWithReturnValue(), getOnException());

        // then:
        assertEquals(TEST_STRING, result);
        verify(getOnException()).accept(any());
        verify(refresher).refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);
        verifyNoMoreInteractions(getOnException(), refresher);
    }

    @Test
    public void testPerformRetryableOperationWithReturnValueThrowsMieleWebserviceExceptionWhenRetryingTheOperationFails() {
        // given:
        when(getOperationWithReturnValue().get()).thenThrow(AuthorizationFailedException.class);

        AuthorizationFailedRetryStrategy retryStrategy = new AuthorizationFailedRetryStrategy(refresher,
                MieleCloudBindingTestConstants.SERVICE_HANDLE);

        assertThrows(MieleWebserviceException.class, () -> {
            try {
                // when:
                retryStrategy.performRetryableOperation(getOperationWithReturnValue(), getOnException());
            } catch (Exception e) {
                // then:
                verify(getOnException()).accept(any());
                verify(refresher).refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);
                verifyNoMoreInteractions(getOnException(), refresher);
                throw e;
            }
        });
    }

    @Test
    public void testPerformRetryableOperationInvokesOperation() {
        // given:
        AuthorizationFailedRetryStrategy retryStrategy = new AuthorizationFailedRetryStrategy(refresher,
                MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        retryStrategy.performRetryableOperation(getOperation(), getOnException());

        // then:
        verify(getOperation()).run();
        verifyNoMoreInteractions(getOperation());
    }

    @Test
    public void testPerformRetryableOperationInvokesRefreshTokenAndRetriesOperation() {
        // given:
        doThrow(AuthorizationFailedException.class).doNothing().when(getOperation()).run();

        AuthorizationFailedRetryStrategy retryStrategy = new AuthorizationFailedRetryStrategy(refresher,
                MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        retryStrategy.performRetryableOperation(getOperation(), getOnException());

        // then:
        verify(getOnException()).accept(any());
        verify(refresher).refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);
        verify(getOperation(), times(2)).run();
        verifyNoMoreInteractions(getOnException(), refresher, getOperation());
    }

    @Test
    public void testPerformRetryableOperationThrowsMieleWebserviceExceptionWhenRetryingTheOperationFails() {
        // given:
        doThrow(AuthorizationFailedException.class).when(getOperation()).run();

        AuthorizationFailedRetryStrategy retryStrategy = new AuthorizationFailedRetryStrategy(refresher,
                MieleCloudBindingTestConstants.SERVICE_HANDLE);

        assertThrows(MieleWebserviceException.class, () -> {
            try {
                // when:
                retryStrategy.performRetryableOperation(getOperation(), getOnException());
            } catch (Exception e) {
                // then:
                verify(getOnException()).accept(any());
                verify(refresher).refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);
                verify(getOperation(), times(2)).run();
                verifyNoMoreInteractions(getOnException(), refresher, getOperation());
                throw e;
            }
        });
    }

    @Test
    public void testPerformRetryableOperationThrowsMieleWebserviceExceptionWhenTokenRefreshingFails() {
        // given:
        doThrow(AuthorizationFailedException.class).when(getOperation()).run();
        doThrow(OAuthException.class).when(refresher).refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);

        AuthorizationFailedRetryStrategy retryStrategy = new AuthorizationFailedRetryStrategy(refresher,
                MieleCloudBindingTestConstants.SERVICE_HANDLE);

        assertThrows(MieleWebserviceException.class, () -> {
            try {
                // when:
                retryStrategy.performRetryableOperation(getOperation(), getOnException());
            } catch (Exception e) {
                // then:
                verify(getOnException()).accept(any());
                verify(refresher).refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);
                verify(getOperation()).run();
                verifyNoMoreInteractions(getOnException(), refresher, getOperation());
                throw e;
            }
        });
    }
}
