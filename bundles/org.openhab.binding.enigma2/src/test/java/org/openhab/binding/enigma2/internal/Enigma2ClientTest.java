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
package org.openhab.binding.enigma2.internal;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The {@link Enigma2ClientTest} class is responsible for testing {@link Enigma2Client}.
 *
 * @author Guido Dolfen - Initial contribution
 */
@SuppressWarnings({ "null" })
@NonNullByDefault
public class Enigma2ClientTest {
    public static final String HOST = "http://user:password@localhost:8080";
    public static final String SOME_TEXT = "some Text";
    public static final String SOME_TEXT_ENCODED = "some+Text";
    @Nullable
    private Enigma2Client enigma2Client;
    @Nullable
    private Enigma2HttpClient enigma2HttpClient;

    @BeforeEach
    public void setUp() throws IOException {
        enigma2HttpClient = mock(Enigma2HttpClient.class);
        enigma2Client = spy(new Enigma2Client("localhost:8080", "user", "password", 5));
        when(enigma2Client.getEnigma2HttpClient()).thenReturn(requireNonNull(enigma2HttpClient));
        when(enigma2HttpClient.get(anyString())).thenReturn("<emptyResult/>");
    }

    @Test
    public void testSetPowerFalse() throws IOException {
        whenStandby("true");
        enigma2Client.setPower(false);
        verify(enigma2HttpClient).get(HOST + Enigma2Client.PATH_POWER);
        verifyNoMoreInteractions(enigma2HttpClient);
    }

    @Test
    public void testSetPower() throws IOException {
        whenStandby("true");
        enigma2Client.setPower(true);
        verify(enigma2HttpClient).get(HOST + Enigma2Client.PATH_TOGGLE_POWER);
    }

    @Test
    public void testSetVolume() throws IOException {
        enigma2Client.setVolume(20);
        verify(enigma2HttpClient).get(HOST + Enigma2Client.PATH_SET_VOLUME + 20);
    }

    @Test
    public void testSetChannel() throws IOException {
        whenStandby("false");
        whenAllServices();
        enigma2Client.refreshPower();
        enigma2Client.refreshAllServices();
        enigma2Client.setChannel("Channel 3");
        verify(enigma2HttpClient).get(HOST + Enigma2Client.PATH_ZAP + 3);
    }

    @Test
    public void testSetChannelUnknown() throws IOException {
        enigma2Client.setChannel("Channel 3");
        verifyNoInteractions(enigma2HttpClient);
    }

    @Test
    public void testSetMuteFalse() throws IOException {
        whenStandby("false");
        whenVolume("10", false);
        enigma2Client.refreshPower();
        enigma2Client.setMute(false);
        verify(enigma2HttpClient).get(HOST + Enigma2Client.PATH_POWER);
        verify(enigma2HttpClient).get(HOST + Enigma2Client.PATH_VOLUME);
        verifyNoMoreInteractions(enigma2HttpClient);
    }

    @Test
    public void testSetMute() throws IOException {
        whenStandby("false");
        whenVolume("10", false);
        enigma2Client.refreshPower();
        enigma2Client.setMute(true);
        verify(enigma2HttpClient).get(HOST + Enigma2Client.PATH_TOGGLE_MUTE);
    }

    @Test
    public void testSendRcCommand() throws IOException {
        enigma2Client.sendRcCommand(2);
        verify(enigma2HttpClient).get(HOST + Enigma2Client.PATH_REMOTE_CONTROL + 2);
    }

    @Test
    public void testSendError() throws IOException {
        enigma2Client.sendError(20, SOME_TEXT);
        verify(enigma2HttpClient).get(HOST + "/web/message?type=3&timeout=20&text=" + SOME_TEXT_ENCODED);
    }

    @Test
    public void testSendWarning() throws IOException {
        enigma2Client.sendWarning(35, SOME_TEXT);
        verify(enigma2HttpClient).get(HOST + "/web/message?type=2&timeout=35&text=" + SOME_TEXT_ENCODED);
    }

    @Test
    public void testSendInfo() throws IOException {
        enigma2Client.sendInfo(40, SOME_TEXT);
        verify(enigma2HttpClient).get(HOST + "/web/message?type=1&timeout=40&text=" + SOME_TEXT_ENCODED);
    }

    @Test
    public void testSendQuestion() throws IOException {
        enigma2Client.sendQuestion(50, SOME_TEXT);
        verify(enigma2HttpClient).get(HOST + "/web/message?type=0&timeout=50&text=" + SOME_TEXT_ENCODED);
    }

    @Test
    public void testRefreshPowerTrue() throws IOException {
        whenStandby(" FALSE ");
        enigma2Client.refreshPower();
        assertThat(enigma2Client.isPower(), is(true));
    }

    @Test
    public void testRefreshVolumeMuteTrue() throws IOException {
        whenStandby("false");
        whenVolume("30", true);
        enigma2Client.refreshPower();
        enigma2Client.refreshVolume();
        assertThat(enigma2Client.isMute(), is(true));
        assertThat(enigma2Client.getVolume(), is(30));
    }

    @Test
    public void testRefreshVolumeMuteFalse() throws IOException {
        whenStandby("false");
        whenVolume("30", false);
        enigma2Client.refreshPower();
        enigma2Client.refreshVolume();
        assertThat(enigma2Client.isMute(), is(false));
        assertThat(enigma2Client.getVolume(), is(30));
    }

    @Test
    public void testRefreshVolumePowerOff() throws IOException {
        enigma2Client.refreshVolume();
        assertThat(enigma2Client.isMute(), is(false));
        assertThat(enigma2Client.getVolume(), is(0));
    }

    @Test
    public void testRefreshPowerFalse() throws IOException {
        whenStandby(" TRUE ");
        enigma2Client.refreshPower();
        assertThat(enigma2Client.isPower(), is(false));
    }

    @Test
    public void testRefreshPowerOffline() throws IOException {
        IOException ioException = new IOException();
        when(enigma2HttpClient.get(HOST + Enigma2Client.PATH_POWER)).thenThrow(ioException);
        enigma2Client.refreshPower();
        assertThat(enigma2Client.isPower(), is(false));
    }

    @Test
    public void testRefreshAllServices() throws IOException {
        whenStandby("false");
        whenAllServices();
        enigma2Client.refreshAllServices();
        assertThat(enigma2Client.getChannels(), containsInAnyOrder("Channel 1", "Channel 2", "Channel 3"));
    }

    @Test
    public void testRefreshChannel() throws IOException {
        whenStandby("false");
        whenChannel("2", "Channel 2");
        enigma2Client.refreshPower();
        enigma2Client.refreshChannel();
        assertThat(enigma2Client.getChannel(), is("Channel 2"));
    }

    @Test
    public void testRefreshEpg() throws IOException {
        whenStandby("false");
        whenAllServices();
        whenChannel("2", "Channel 2");
        whenEpg("2", "Title", "Description");
        enigma2Client.refreshPower();
        enigma2Client.refreshAllServices();
        enigma2Client.refreshChannel();
        enigma2Client.refreshEpg();
        assertThat(enigma2Client.getTitle(), is("Title"));
        assertThat(enigma2Client.getDescription(), is("Description"));
    }

    @Test
    public void testRefreshAnswerTimeout() throws IOException {
        whenStandby("false");
        whenAnswer("False", "Timeout");
        enigma2Client.refreshPower();
        enigma2Client.refreshAnswer();
        assertThat(enigma2Client.getLastAnswerTime().isAfter(LocalDateTime.of(2020, 1, 1, 0, 0)), is(false));
        assertThat(enigma2Client.getAnswer(), is(""));
    }

    @Test
    public void testRefreshAnswerNoQuestion() throws IOException {
        whenStandby("false");
        whenAnswer("True", "Antwort lautet NEIN!");
        enigma2Client.refreshPower();
        enigma2Client.refreshAnswer();
        assertThat(enigma2Client.getLastAnswerTime().isAfter(LocalDateTime.of(2020, 1, 1, 0, 0)), is(false));
        assertThat(enigma2Client.getAnswer(), is(""));
    }

    @Test
    public void testRefreshAnswer() throws IOException {
        whenStandby("false");
        whenAnswer("True", "Antwort lautet NEIN!");
        enigma2Client.refreshPower();
        enigma2Client.sendQuestion(50, SOME_TEXT);
        enigma2Client.refreshAnswer();
        assertThat(enigma2Client.getLastAnswerTime().isAfter(LocalDateTime.of(2020, 1, 1, 0, 0)), is(true));
        assertThat(enigma2Client.getAnswer(), is("NEIN"));
    }

    @Test
    public void testRefresh() throws IOException {
        whenStandby("false");
        whenAllServices();
        whenVolume("A", false);
        whenChannel("1", "Channel 1");
        whenEpg("1", "Title", "Description");
        assertThat(enigma2Client.refresh(), is(true));
        assertThat(enigma2Client.isPower(), is(true));
        assertThat(enigma2Client.isMute(), is(false));
        assertThat(enigma2Client.getVolume(), is(0));
        assertThat(enigma2Client.getChannel(), is("Channel 1"));
        assertThat(enigma2Client.getTitle(), is("Title"));
        assertThat(enigma2Client.getDescription(), is("Description"));
        assertThat(enigma2Client.getChannels(), containsInAnyOrder("Channel 1", "Channel 2", "Channel 3"));
    }

    @Test
    public void testRefreshOffline() throws IOException {
        IOException ioException = new IOException();
        when(enigma2HttpClient.get(HOST + Enigma2Client.PATH_POWER)).thenThrow(ioException);
        assertThat(enigma2Client.refresh(), is(false));
        assertThat(enigma2Client.isPower(), is(false));
        assertThat(enigma2Client.isMute(), is(false));
        assertThat(enigma2Client.getVolume(), is(0));
        assertThat(enigma2Client.getChannel(), is(""));
        assertThat(enigma2Client.getTitle(), is(""));
        assertThat(enigma2Client.getDescription(), is(""));
        assertThat(enigma2Client.getChannels().isEmpty(), is(true));
    }

    @Test
    public void testGetEnigma2HttpClient() {
        enigma2Client = new Enigma2Client("http://localhost:8080", null, null, 5);
        assertThat(enigma2Client.getEnigma2HttpClient(), is(notNullValue()));
    }

    private void whenVolume(String volume, boolean mute) throws IOException {
        when(enigma2HttpClient.get(HOST + Enigma2Client.PATH_VOLUME)).thenReturn(
                "<e2volume><e2current>" + volume + "</e2current><e2ismuted>" + mute + "</e2ismuted></e2volume>");
    }

    private void whenEpg(String id, String title, String description) throws IOException {
        when(enigma2HttpClient.get(HOST + Enigma2Client.PATH_EPG + id)).thenReturn("<e2event><e2eventtitle>" + title
                + "</e2eventtitle><e2eventdescription>" + description + "</e2eventdescription></e2event>");
    }

    private void whenAnswer(String state, String answer) throws IOException {
        when(enigma2HttpClient.get(HOST + Enigma2Client.PATH_ANSWER)).thenReturn("<e2simplexmlresult><e2state>" + state
                + "</e2state><e2statetext>" + answer + "</e2statetext></e2simplexmlresult>");
    }

    private void whenStandby(String standby) throws IOException {
        when(enigma2HttpClient.get(HOST + Enigma2Client.PATH_POWER))
                .thenReturn("<e2powerstate><e2instandby>" + standby + "</e2instandby></e2powerstate>");
    }

    private void whenChannel(String id, String name) throws IOException {
        when(enigma2HttpClient.get(HOST + Enigma2Client.PATH_CHANNEL)).thenReturn(
                "<e2servicelist><e2service><e2servicereference>" + id + "</e2servicereference><e2servicename>" + name
                        + "</e2servicename></e2service></e2servicelist>");
    }

    private void whenAllServices() throws IOException {
        when(enigma2HttpClient.get(HOST + Enigma2Client.PATH_ALL_SERVICES)).thenReturn("""
                <e2servicelistrecursive>\
                <e2bouquet>\
                <e2servicelist>\
                <e2service>\
                <e2servicereference>1</e2servicereference>\
                <e2servicename>Channel 1</e2servicename>\
                </e2service>\
                <e2service>\
                <e2servicereference>2</e2servicereference>\
                <e2servicename>Channel 2</e2servicename>\
                </e2service>\
                </e2servicelist>\
                </e2bouquet>\
                <e2bouquet>\
                <e2servicelist>\
                <e2service>\
                <e2servicereference>3</e2servicereference>\
                <e2servicename>Channel 3</e2servicename>\
                </e2service>\
                </e2servicelist>\
                </e2bouquet>\
                </e2servicelistrecursive>\
                """);
    }
}
