/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.philipsair.internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirCipher;

/**
 * Test cases for {@link PhilipsAirHandler}. The tests provide mocks for
 * supporting entities using Mockito.
 * 
 * Scope of these integration tests involves already most of the communication process, encryption, channel updates
 *
 * @author michalboronski - Initial contribution
 */
public class PhilipsAirHandlerTest extends JavaTest {

    private PhilipsAirHandler handler;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Thing thing;

    @Mock
    private Request request;

    @Mock
    private HttpClient httpClient;

    @Mock
    private ContentResponse response;

    private static String decodedContent1 = "{\"name\":\"Philips\",\"type\":\"AC2889\",\"modelid\":\"AC2889/10\",\"swversion\":\"1.0.4\"}";//"ETThbheNTZ3X8dHXUnnPWkzoQGPiH2Fi+4U3Xto4vdD4kPQeZSRjWs+y5y2h2NhfYhKsiiJoat7EQQCLGoqwkJsMjxqAEhxCWNB3cJH/xK0=";

    private static String decodedContent2 = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":8,\"iaql\":2,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";//"74383l4x32yaptcm7NN/ULqdPxH0u/6v29nG08YKxxqFioeR63gtcNclbWEpwn3/m3eUhLx+ZPhxzAkTn4CdziU1tYiNdli/AWai/7Hal1Jvx2nR4jh80BMuCZf3VcgXSUKn9zB30qyjzbC1Nbq9GS2P8Y42RoMEMIudkIlE2P0=";

    private static String decodedContent3 = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":5,\"iaql\":2,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";//"dascgjJepjl/H6EZyNS3nNDqwL81dLNGpngmIFma3nVlLVOHyzJXhCd84E6POnHIjxuLnqtQomjT0EJI4sWLeEKsGhZYJ4tUnJ1kWHlS2icKBdnHJJrW1R3Tg9nUCk9Ju0ujuLT3CV9uKgb/Z/fRL5/GIljVaj1khn0/kCFztz4=";

    private static String decodedContent4 = "{\"om\":\"0\",\"pwr\":\"0\",\"cl\":false,\"aqil\":75,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":2,\"iaql\":1,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";//"54LDUyCS0bGhmK1fgAK2FCQsZ9/j/OunN3p9omuoPnrYZ9UnCAdOXY6Cp+HkDphFqLTi7bqwn1NCTj4pxtGFFG2v7QxwFwvf1Dp8d4G6/Xg4KePGl4dyMgM7rnY+ZBalJrjUiqbFpYswErw0wFkAcI47yjCjtp8AY1tsBToBUzQ=";
    private static String decodedContent5 = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"0\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":6,\"iaql\":2,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static String decodedContent6 = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"0\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":9,\"iaql\":3,\"aqit\":4,\"ddp\":\"0\",\"err\":0,\"wl\":0}";
    private static String decodedContent7 = "{\"om\":\"1\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"M\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":18,\"iaql\":4,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static String decodedContent8 = "{\"om\":\"1\",\"pwr\":\"1\",\"cl\":false,\"aqil\":75,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"M\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":18,\"iaql\":4,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static String decodedContent9 = "{\"om\":\"1\",\"pwr\":\"1\",\"cl\":false,\"aqil\":25,\"uil\":\"1\",\"dt\":0,\"dtrs\":0,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":13,\"iaql\":3,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static String decodedContent10 = "{\"om\":\"s\",\"pwr\":\"1\",\"cl\":false,\"aqil\":25,\"uil\":\"1\",\"dt\":1,\"dtrs\":60,\"mode\":\"P\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":11,\"iaql\":3,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    private static String decodedContent11 = "{\"om\":\"1\",\"pwr\":\"1\",\"cl\":false,\"aqil\":25,\"uil\":\"1\",\"dt\":1,\"dtrs\":57,\"mode\":\"A\",\"func\":\"PH\",\"rhset\":40,\"rh\":56,\"temp\":21,\"pm25\":7,\"iaql\":2,\"aqit\":4,\"ddp\":\"1\",\"err\":0,\"wl\":0}";
    
    private static String content1 = null;
    private static String content2 = null;
    private static String content3 = null;
    private static String content4 = null;
    private static String content5 = null;
    private static String content6 = null;
    private static String content7 = null;
    private static String content8 = null;
    private static String content9 = null;
    private static String content10 = null;
    private static String content11 = null;
    
    private static PhilipsAirCipher cipher;
    private static Configuration config = new Configuration();
    
    private static final String FAKE_KEY = "1F09722BE668AF0B8DC78051B70E4F76";
    
    @BeforeClass
    public static void initClass() throws GeneralSecurityException, UnsupportedEncodingException {
        
        config.put(PhilipsAirConfiguration.CONFIG_DEF_REFRESH_INTERVAL, 5);
        config.put(PhilipsAirConfiguration.CONFIG_HOST, "1.1.1.1");
        config.put(PhilipsAirConfiguration.CONFIG_KEY, FAKE_KEY);
        
        cipher = new PhilipsAirCipher();
        cipher.initKey(FAKE_KEY);
        /*System.out.println(cipher.decrypt("JJBpyvWxWEHfbQzXKozcg+XYqqps8i721ForFTlQlOE/lm589dW9s06iIS0/w25LP/JR1acOD184YGatYxBC6ydwHMuC6a1QvZyKoCSzcg/ZG1JH3NhxSRmLHZ+f7f6fopOQo3Sp+imPc6gY84m2Y+1wvsiFKrnIx7J5kRYaOTg="));
        System.out.println(cipher.decrypt("NSunLG88Rv4F29ePgZXESF0Cu90yrWvFoOn5uD2TwNYHDR47mEfhfzFJsIMnvsKz3yHUZewYZZ2ZCHf3hn7B/skyGWNy5j4r0JikJg4sVfrwKFNrsGX4BXi/EDX0qXbmwnVMa5mgUAo/t5c6H2UkkChssPrXkbWuLxf3wm4QU80="));
        System.out.println(cipher.decrypt("sU01Utdel/S8URooy6eW7TkZaIunR7GZgxKVkPcFbjPk1c4NAikHHhVBlrUVBaE4SdWilkf960e+GacPS+ihtwbUym8WFzozmFPK2VpYJRW0dlkQtzhBVATpkfoBrJYpjhcFvS8o+xWs3rBbV8QrKOLJqJuHBEFBQQcwbZhDoNs="));
        System.out.println(cipher.decrypt("sU01Utdel/S8URooy6eW7TkZaIunR7GZgxKVkPcFbjPk1c4NAikHHhVBlrUVBaE4SdWilkf960e+GacPS+ihtwbUym8WFzozmFPK2VpYJRW0dlkQtzhBVATpkfoBrJYpjhcFvS8o+xWs3rBbV8QrKOLJqJuHBEFBQQcwbZhDoNs="));
        System.out.println(cipher.decrypt("fEISYvNLsou4REhkig8DCj+il5L322AvZDpNqWOKOsE1OsdEVSyC7i2lLfePNYypxo4YI5wdSFQEFkU6MO48QMoDZTOzahZavO7wcOgMwZlZekCCg/m63hwWgT70Y1dX+m1CddCjv6lHBl50AWDQU2IHEkPDlx0IgBS+IlwJ65g="));
        System.out.println(cipher.decrypt("R1DwUQbvpyrLgz8GkjecmTyV6j5poKA3n3LTYywYfUNJGiz14Sw88+IkY02lpvzczxZgSDaZoEzk2rZB0LS6C/QNcvmuP2jSI7vlK+beYDA6nOxBfDZAQjIMwhscbtzEDJdibg94kjZSq+9WuVH6jdNTNGtL1aH4sryl/6FmGV0="));
        System.out.println(cipher.decrypt("W+50vW/yqXqXkRj1PvppyoojxGDTzs2uHfCXL1yyGaz8I3LxRgNYxTI2EUu3BG5WfimkqZpFIGImBm5O02oUZa1Y6T1jAedrG/VSR7z/7klavNbMI5IF4Xix0ih0ZxUf+oQEeRz+P4ipELXqfSyZy/jnbKZtA6PqzGDn5uld/qg="));
        */
        content1 = cipher.encrypt(decodedContent1);
        content2 = cipher.encrypt(decodedContent2);
        content3 = cipher.encrypt(decodedContent3);
        content4 = cipher.encrypt(decodedContent4);
        
        content5 = cipher.encrypt(decodedContent5);
        content6 = cipher.encrypt(decodedContent6);
        content7 = cipher.encrypt(decodedContent7);
        content8 = cipher.encrypt(decodedContent8);
        content9 = cipher.encrypt(decodedContent9);
        content10 = cipher.encrypt(decodedContent10);
        content11 = cipher.encrypt(decodedContent11);
        
        
    }
    
    @Before
    public void setUp() throws Exception {
        initMocks(this);
        // mock getConfiguration to prevent NPEs
        
        when(thing.getConfiguration()).thenReturn(config);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);

        ThingUID thingUID = new ThingUID("philipsair:ac2889_10:1");
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);

        handler = new PhilipsAirHandler(thing, httpClient);
        
        
        /*String encoded = cipher.decrypt(content1);
        System.out.println(encoded);
        
        encoded = cipher.decrypt(content2);
        System.out.println(encoded);
        encoded = cipher.decrypt(content3);
        System.out.println(encoded);
        encoded = cipher.decrypt(content4);
        System.out.println(encoded);*/
        when(request.send()).thenReturn(response);

        
        
    }
    

    @Test
    public void initializeShouldCallTheCallback() throws Exception {
        when(request.timeout(5, TimeUnit.SECONDS)).thenReturn(request);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(content1).thenReturn(content2).thenReturn(content1)
                .thenReturn(content3);

        
        handler.setCallback(callback);
        handler.initialize();
        // the argument captor will capture the argument of type ThingStatusInfo given
        // to the
        // callback#statusUpdated method.
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        // verify the interaction with the callback and capture the ThingStatusInfo
        // argument:

        waitForAssert(() -> {
            verify(callback, times(3)).statusUpdated(eq(thing), statusInfoCaptor.capture());
        }, 6000, 100);

        // assert that the (temporary) UNKNOWN status was given first:
        assertThat(statusInfoCaptor.getAllValues().get(0).getStatus(), is(ThingStatus.UNKNOWN));

        // assert that ONLINE status was given later:
        assertThat(statusInfoCaptor.getAllValues().get(1).getStatus(), is(ThingStatus.ONLINE));
    }

    private void initChannels(ThingHandlerCallback callback, String... ids) {
        List<Channel> channels = new ArrayList<Channel>();

        for (int i = 0; i < ids.length; i++) {
            ChannelUID channelUID = new ChannelUID("philipsair:ac2889_10:1:" + ids[i].split(":")[0]);
            Channel channel = ChannelBuilder.create(channelUID, ids[i].split(":")[1]).build();

            when(callback.isChannelLinked(channelUID)).thenReturn(true);
            channels.add(channel);
        }
        when(thing.getChannels()).thenReturn(channels);
    }

    @Test
    public void testStateUpdates() throws Exception {
        // mock getConfiguration to prevent NPEs
        
        initChannels(callback, "sensors#pm25:Number", "control#pwr:Switch", "control#om:String", "control#cl:Switch",
                "control-ui#aqil:Number", "control-ui#uil:Switch", "control#dt:Number", "control#dtrs:Number",
                "control#mode:String", "sensors#iaql:Number", "sensors#aqit:Number", "control-ui#ddp:String",
                "err:String", "func:String", "rhset:Number", "rh:Number", "temp:Number", "wl:Number");
        

        
        when(request.timeout(5, TimeUnit.SECONDS)).thenReturn(request);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(content1).thenReturn(content2).thenReturn(content1)
                .thenReturn(content3).thenReturn(content4);

        // we expect the handler#initialize method to call the callback during execution
        // and
        // pass it the thing and a ThingStatusInfo object containing the ThingStatus of
        // the thing.
        // HttpClient httpClient = new HttpClient();
        // httpClient.start();

        handler.setCallback(callback);
        handler.initialize();

        // the argument captor will capture the argument of type ThingStatusInfo given
        // to the
        // callback#statusUpdated method.
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        // verify the interaction with the callback and capture the ThingStatusInfo
        // argument:

        waitForAssert(() -> {
            verify(callback, times(3)).statusUpdated(eq(thing), statusInfoCaptor.capture());
            verify(callback, atLeast(2)).stateUpdated(any(), stateCaptor.capture());
        }, 6000, 100);

        // assert that the (temporary) UNKNOWN status was given first:
        assertThat(statusInfoCaptor.getAllValues().get(0).getStatus(), is(ThingStatus.UNKNOWN));

        // assert that ONLINE status was given later:
        assertThat(statusInfoCaptor.getAllValues().get(1).getStatus(), is(ThingStatus.ONLINE));

        int calls = stateCaptor.getAllValues().size();
        assertThat(calls, is(18));
        assertThat(stateCaptor.getAllValues().get(0), instanceOf(DecimalType.class));
        assertThat(stateCaptor.getAllValues().get(1), instanceOf(OnOffType.class));
        assertThat(stateCaptor.getAllValues().get(2), instanceOf(StringType.class));
        assertThat(stateCaptor.getAllValues().get(3), instanceOf(OnOffType.class));
        assertThat(stateCaptor.getAllValues().get(4), instanceOf(DecimalType.class));
        assertThat(stateCaptor.getAllValues().get(5), instanceOf(OnOffType.class));
        assertThat(stateCaptor.getAllValues().get(6), instanceOf(DecimalType.class)); // dt

        assertThat(stateCaptor.getAllValues().get(7), instanceOf(DecimalType.class)); // dtrs
        assertThat(stateCaptor.getAllValues().get(8), instanceOf(StringType.class)); // mode
        assertThat(stateCaptor.getAllValues().get(9), instanceOf(DecimalType.class)); // iaql
        assertThat(stateCaptor.getAllValues().get(10), instanceOf(DecimalType.class)); // aqit
        assertThat(stateCaptor.getAllValues().get(11), instanceOf(StringType.class)); // ddp
        assertThat(stateCaptor.getAllValues().get(12), instanceOf(StringType.class)); // err
        assertThat(stateCaptor.getAllValues().get(13), instanceOf(StringType.class)); // func
        assertThat(stateCaptor.getAllValues().get(14), instanceOf(DecimalType.class)); // rhset
        assertThat(stateCaptor.getAllValues().get(15), instanceOf(DecimalType.class)); // rh
        assertThat(stateCaptor.getAllValues().get(16), instanceOf(DecimalType.class)); // temp
        assertThat(stateCaptor.getAllValues().get(17), instanceOf(DecimalType.class)); // wl

        DecimalType pm25State = (DecimalType) stateCaptor.getAllValues().get(0);
        OnOffType pwrState = (OnOffType) stateCaptor.getAllValues().get(1);
        StringType omState = (StringType) stateCaptor.getAllValues().get(2);
        OnOffType clState = (OnOffType) stateCaptor.getAllValues().get(3);
        DecimalType aqilState = (DecimalType) stateCaptor.getAllValues().get(4);
        OnOffType uilState = (OnOffType) stateCaptor.getAllValues().get(5);
        DecimalType dtState = (DecimalType) stateCaptor.getAllValues().get(6);

        DecimalType dtrsState = (DecimalType) stateCaptor.getAllValues().get(7);
        StringType modeState = (StringType) stateCaptor.getAllValues().get(8);
        DecimalType iaqlState = (DecimalType) stateCaptor.getAllValues().get(9);
        DecimalType aqitState = (DecimalType) stateCaptor.getAllValues().get(10);
        StringType ddpState = (StringType) stateCaptor.getAllValues().get(11);
        StringType errState = (StringType) stateCaptor.getAllValues().get(12);
        StringType funcState = (StringType) stateCaptor.getAllValues().get(13);
        DecimalType rhsetState = (DecimalType) stateCaptor.getAllValues().get(14);
        DecimalType rhState = (DecimalType) stateCaptor.getAllValues().get(15);
        DecimalType tempState = (DecimalType) stateCaptor.getAllValues().get(16);
        DecimalType wlState = (DecimalType) stateCaptor.getAllValues().get(17);
        
        assertThat(pm25State.intValue(), is(8));
        assertThat(pwrState, is(OnOffType.ON));
        assertThat(omState, is("s"));
        assertThat(clState, is(OnOffType.OFF));
        assertThat(aqilState.intValue(), equalTo(75));
        assertThat(uilState, is(OnOffType.ON));
        assertThat(dtState.intValue(), is(0));
        assertThat(dtrsState.intValue(), is(0));
        assertThat(modeState, equalTo("P"));
        assertThat(iaqlState.intValue(), is(2));
        assertThat(aqitState.intValue(), is(4));
        assertThat(ddpState, is("1"));
        assertThat(errState, is("0"));
        assertThat(funcState, is("PH"));
        assertThat(rhsetState.intValue(), is(40));
        assertThat(rhState.intValue(), is(56));
        assertThat(tempState.intValue(), is(21));
        assertThat(wlState.intValue(), is(0));
        //assertThat(errMsgState, is("No error"));
    }

    private void sendCommandTemplate(Channel channel, Command command, State stateBefore, State stateAfter,
            String responseBefore, String commandResponse)
            throws InterruptedException, TimeoutException, ExecutionException {
        // mock getConfiguration to prevent NPEs

        when(thing.getChannels()).thenReturn(Arrays.asList(channel));
        when(callback.isChannelLinked(channel.getUID())).thenReturn(true);
        when(request.timeout(5, TimeUnit.SECONDS)).thenReturn(request);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(content1).thenReturn(responseBefore).thenReturn(commandResponse);

        // we expect the handler#initialize method to call the callback during execution
        // and
        // pass it the thing and a ThingStatusInfo object containing the ThingStatus of
        // the thing.
        // HttpClient httpClient = new HttpClient();
        // httpClient.start();
        
        handler.setCallback(callback);
        handler.initialize();
        
        // the argument captor will capture the argument of type ThingStatusInfo given
        // to the
        // callback#statusUpdated method.
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        // verify the interaction with the callback and capture the ThingStatusInfo
        // argument:

        waitForAssert(() -> {
            verify(callback, atLeast(2)).statusUpdated(eq(thing), statusInfoCaptor.capture());
            verify(callback, times(1)).stateUpdated(any(), stateCaptor.capture());
        }, 5000, 100);

        // assert that the (temporary) UNKNOWN status was given first:
        assertThat(statusInfoCaptor.getAllValues().get(0).getStatus(), is(ThingStatus.UNKNOWN));

        // assert that ONLINE status was given later:
        assertThat(statusInfoCaptor.getAllValues().get(1).getStatus(), is(ThingStatus.ONLINE));

        int calls = stateCaptor.getAllValues().size();
        assertThat(stateCaptor.getAllValues().get(calls - 1), instanceOf(State.class));

        State state = stateCaptor.getAllValues().get(calls - 1);
        assertThat(state, is(stateBefore));

        handler.handleCommand(channel.getUID(), command);
        waitForAssert(() -> {
            verify(callback, times(2)).stateUpdated(any(), stateCaptor.capture());
        }, 1000, 100);

        calls = stateCaptor.getAllValues().size();
        state = stateCaptor.getAllValues().get(calls - 1);
        if (state instanceof DecimalType) {
            assertThat(((DecimalType) state).intValue(), equalTo(((DecimalType) stateAfter).intValue()));
        } else {
            assertThat(state, is(stateAfter));
        }
    }

    @Test
    public void testSendCommandPWR() throws Exception {
        ChannelUID channelUIDpwr = new ChannelUID("philipsair:ac2889_10:1:pwr");
        Channel channel = ChannelBuilder.create(channelUIDpwr, "Switch").build();
        sendCommandTemplate(channel, OnOffType.OFF, OnOffType.ON, OnOffType.OFF, content2, content4);
    }

    @Test
    public void testSendCommandUIL() throws Exception {
        ChannelUID channelUIDpwr = new ChannelUID("philipsair:ac2889_10:1:uil");
        Channel channel = ChannelBuilder.create(channelUIDpwr, "Switch").build();
        sendCommandTemplate(channel, OnOffType.OFF, OnOffType.ON, OnOffType.OFF, content2,
                content5);
    }

    @Test
    public void testSendCommandDDP() throws Exception {
        ChannelUID channelUIDpwr = new ChannelUID("philipsair:ac2889_10:1:ddp");
        Channel channel = ChannelBuilder.create(channelUIDpwr, "String").build();
        sendCommandTemplate(channel, new StringType("0"), new StringType("1"), new StringType("0"), content2,
                content6);
    }

    @Test
    public void testSendCommandOM1() throws Exception {
        ChannelUID channelUIDpwr = new ChannelUID("philipsair:ac2889_10:1:om");
        Channel channel = ChannelBuilder.create(channelUIDpwr, "String").build();
        sendCommandTemplate(channel, new StringType("1"), new StringType("s"), new StringType("1"), content2,
                content7);
    }

    @Test
    public void testSendCommandOMs() throws Exception {
        ChannelUID channelUIDpwr = new ChannelUID("philipsair:ac2889_10:1:om");
        Channel channel = ChannelBuilder.create(channelUIDpwr, "String").build();
        sendCommandTemplate(channel, new StringType("s"), new StringType("1"), new StringType("s"),
                content8,
                content2);
    }

    @Test
    public void testSendCommandAqil() throws Exception {
        ChannelUID channelUIDpwr = new ChannelUID("philipsair:ac2889_10:1:aqil");
        Channel channel = ChannelBuilder.create(channelUIDpwr, "Number").build();
        sendCommandTemplate(channel, new DecimalType("25"), new DecimalType("75"), new DecimalType("25"), content2,
                content9);
    }

    @Test
    public void testSendCommandDt() throws Exception {
        ChannelUID channelUIDpwr = new ChannelUID("philipsair:ac2889_10:1:dt");
        Channel channel = ChannelBuilder.create(channelUIDpwr, "Number").build();
        sendCommandTemplate(channel, new DecimalType("1"), new DecimalType("0"), new DecimalType("1"), content2,
                content10);
    }

    @Test
    public void testSendCommandMode() throws Exception {
        ChannelUID channelUIDpwr = new ChannelUID("philipsair:ac2889_10:1:mode");
        Channel channel = ChannelBuilder.create(channelUIDpwr, "Number").build();
        sendCommandTemplate(channel, new StringType("A"), new StringType("P"), new StringType("A"), content2,
                content11);
    }
}
