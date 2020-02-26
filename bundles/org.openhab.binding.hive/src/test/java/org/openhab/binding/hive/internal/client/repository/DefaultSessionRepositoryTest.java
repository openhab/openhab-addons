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
package org.openhab.binding.hive.internal.client.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.hive.internal.TestUtil;
import org.openhab.binding.hive.internal.client.*;
import org.openhab.binding.hive.internal.client.dto.HeatingThermostatV1FeatureDto;
import org.openhab.binding.hive.internal.client.dto.SessionDto;
import org.openhab.binding.hive.internal.client.dto.SessionsDto;
import org.openhab.binding.hive.internal.client.exception.HiveApiAuthenticationException;
import org.openhab.binding.hive.internal.client.exception.HiveApiUnknownException;
import org.openhab.binding.hive.internal.client.exception.HiveClientRequestException;
import org.openhab.binding.hive.internal.client.exception.HiveClientResponseException;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class DefaultSessionRepositoryTest {
    private static final SessionId SESSION_ID = new SessionId("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJob25leWNvbWIiLCJleHAiOjE1ODc2MDUwMDYsInV1aWQiOiIzZTgzYzQzZS1mN2Y2LTRhNDMtYjM5Zi0yYjFkOTc2MjVlY2EiLCJ1c2VybmFtZSI6ImpvaG4uc21pdGhAZXhhbXBsZS5jb20iLCJqdGkiOiIwMGNhN2NhOC0wMTM4LTQxMTctYTAyNi0zMWYwMzg5YTk5NTkiLCJpYXQiOjE1ODc2MDE0MDZ9.vTao7fUmEBzBY1Ha1PR4di6pngnRQ9i9tDD6gFp9M-I");
    private static final UserId USER_ID = new UserId(UUID.fromString(TestUtil.UUID_DEADBEEF));
    private static final String USERNAME = "john.smith@example.com";
    private static final String PASSWORD = "super_secret_password";

    @NonNullByDefault({})
    @Mock
    private HiveApiRequestFactory requestFactory;

    @NonNullByDefault({})
    @Mock
    private HiveApiRequest request;

    @NonNullByDefault({})
    @Mock
    private HiveApiResponse response;

    @Before
    public void setUp() {
        initMocks(this);
    }

    private void setUpSuccessResponse(final SessionsDto content) throws HiveClientRequestException {
        when(this.requestFactory.newRequest(any())).thenReturn(this.request);

        when(this.request.accept(any())).thenReturn(this.request);
        when(this.request.post(any())).thenReturn(this.response);

        when(this.response.getStatusCode()).thenReturn(200);

        when(this.response.getContent(eq(SessionsDto.class))).thenReturn(content);
    }

    private static HeatingThermostatV1FeatureDto getGoodHeatingThermostatV1FeatureDto() {
        final HeatingThermostatV1FeatureDto heatingThermostatV1FeatureDto = new HeatingThermostatV1FeatureDto();
        heatingThermostatV1FeatureDto.operatingMode = TestUtil.createSimpleFeatureAttributeDto(HeatingThermostatOperatingMode.SCHEDULE);
        heatingThermostatV1FeatureDto.operatingState = TestUtil.createSimpleFeatureAttributeDto(HeatingThermostatOperatingState.OFF);
        heatingThermostatV1FeatureDto.targetHeatTemperature = TestUtil.createSimpleFeatureAttributeDto(BigDecimal.valueOf(20));
        heatingThermostatV1FeatureDto.temporaryOperatingModeOverride = TestUtil.createSimpleFeatureAttributeDto(OverrideMode.NONE);

        return heatingThermostatV1FeatureDto;
    }

    @Test
    public void testGoodCreateSession() throws HiveClientRequestException, HiveClientResponseException,
            HiveApiUnknownException, HiveApiAuthenticationException {
        /* Given */
        final SessionDto sessionDto = new SessionDto();
        sessionDto.username = USERNAME;
        sessionDto.userId = USER_ID;
        sessionDto.id = SESSION_ID;
        sessionDto.sessionId = SESSION_ID;
        sessionDto.extCustomerLevel = 1;
        sessionDto.latestSupportedApiVersion = "6";

        final SessionsDto sessionsDto = new SessionsDto();
        sessionsDto.sessions = Collections.singletonList(sessionDto);

        this.setUpSuccessResponse(sessionsDto);

        final DefaultSessionRepository sessionRepository = new DefaultSessionRepository(this.requestFactory);


        /* When */
        final Session session = sessionRepository.createSession(USERNAME, PASSWORD);


        /* Then */
        // Make sure the API was called correctly
        verify(this.requestFactory, times(1)).newRequest(HiveApiConstants.ENDPOINT_SESSIONS);

        final ArgumentCaptor<SessionsDto> sessionsDtoArgumentCaptor = ArgumentCaptor.forClass(SessionsDto.class);
        verify(this.request, times(1)).post(sessionsDtoArgumentCaptor.capture());
        final SessionsDto putSessionsDto = sessionsDtoArgumentCaptor.getValue();
        assertThat(putSessionsDto).isNotNull();
        assertThat(putSessionsDto.sessions).isNotNull();
        assertThat(putSessionsDto.sessions.size()).isEqualTo(1);
        final @Nullable SessionDto putSessionDto = putSessionsDto.sessions.get(0);
        assertThat(putSessionDto).isNotNull();
        assertThat(putSessionDto.username).isEqualTo(USERNAME);
        assertThat(putSessionDto.password).isEqualTo(PASSWORD);
        assertThat(putSessionDto.id).isNull();
        assertThat(putSessionDto.uuid).isNull();
        assertThat(putSessionDto.userId).isNull();
        assertThat(putSessionDto.sessionDuration).isNull();
        assertThat(putSessionDto.extCustomerLevel).isNull();
        assertThat(putSessionDto.href).isNull();

        // Make sure the returned session is correct
        assertThat(session.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(session.getUserId()).isEqualTo(USER_ID);
    }
}
