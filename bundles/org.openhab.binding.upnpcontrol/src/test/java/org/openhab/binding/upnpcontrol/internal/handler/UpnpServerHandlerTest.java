/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal.handler;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.upnpcontrol.internal.config.UpnpControlServerConfiguration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for {@link UpnpServerHandler}.
 *
 * @author Mark Herwege - Initial contribution
 */
@SuppressWarnings({ "null", "unchecked" })
@NonNullByDefault
public class UpnpServerHandlerTest extends UpnpHandlerTest {

    private final Logger logger = LoggerFactory.getLogger(UpnpServerHandlerTest.class);

    private static final String THING_TYPE_UID = "upnpcontrol:upnpserver";
    private static final String THING_UID = THING_TYPE_UID + ":mockserver";

    private static final String RESPONSE_HEADER = """
            <DIDL-Lite xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/" \
            xmlns:dc="http://purl.org/dc/elements/1.1/" \
            xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">\
            """;
    private static final String RESPONSE_FOOTER = "</DIDL-Lite>";

    private static final String BASE_CONTAINER = RESPONSE_HEADER
            + "<container id=\"C1\" searchable=\"0\" parentID=\"0\" restricted=\"1\" childCount=\"2\">"
            + "<dc:title>All Audio Items</dc:title><upnp:class>object.container</upnp:class>"
            + "<upnp:writeStatus>UNKNOWN</upnp:writeStatus></container>"
            + "<container id=\"C2\" searchable=\"0\" parentID=\"0\" restricted=\"1\" childCount=\"0\">"
            + "<dc:title>All Image Items</dc:title><upnp:class>object.container</upnp:class>"
            + "<upnp:writeStatus>UNKNOWN</upnp:writeStatus></container>" + RESPONSE_FOOTER;

    private static final String SINGLE_CONTAINER = RESPONSE_HEADER
            + "<container id=\"C11\" searchable=\"0\" parentID=\"C1\" restricted=\"1\" childCount=\"2\">"
            + "<dc:title>Morning Music</dc:title><upnp:class>object.container</upnp:class>"
            + "<upnp:writeStatus>UNKNOWN</upnp:writeStatus></container>" + RESPONSE_FOOTER;

    private static final String DOUBLE_CONTAINER = RESPONSE_HEADER
            + "<container id=\"C11\" searchable=\"0\" parentID=\"C1\" restricted=\"1\" childCount=\"2\">"
            + "<dc:title>Morning Music</dc:title><upnp:class>object.container</upnp:class>"
            + "<upnp:writeStatus>UNKNOWN</upnp:writeStatus></container>"
            + "<container id=\"C12\" searchable=\"0\" parentID=\"C1\" restricted=\"1\" childCount=\"1\">"
            + "<dc:title>Evening Music</dc:title><upnp:class>object.container</upnp:class>"
            + "<upnp:writeStatus>UNKNOWN</upnp:writeStatus></container>" + RESPONSE_FOOTER;

    private static final String DOUBLE_MEDIA = RESPONSE_HEADER + "<item id=\"M1\" parentID=\"C11\" restricted=\"1\">"
            + "<dc:title>Music_01</dc:title><upnp:class>object.item.audioItem</upnp:class>"
            + "<dc:creator>Creator_1</dc:creator>"
            + "<res protocolInfo=\"http-get:*:audio/mpeg:*\" size=\"8054458\" importUri=\"http://MediaServerContent_0/1/M1/\">http://MediaServerContent_0/1/M1/Test_1.mp3</res>"
            + "<upnp:writeStatus>UNKNOWN</upnp:writeStatus></item>"
            + "<item id=\"M2\" parentID=\"C11\" restricted=\"1\">"
            + "<dc:title>Music_02</dc:title><upnp:class>object.item.audioItem</upnp:class>"
            + "<dc:creator>Creator_2</dc:creator>"
            + "<res protocolInfo=\"http-get:*:audio/wav:*\" size=\"1156598\" importUri=\"http://MediaServerContent_0/3/M2/\">http://MediaServerContent_0/3/M2/Test_2.wav</res>"
            + "<upnp:writeStatus>UNKNOWN</upnp:writeStatus></item>" + RESPONSE_FOOTER;

    private static final String EXTRA_MEDIA = RESPONSE_HEADER + "<item id=\"M3\" parentID=\"C12\" restricted=\"1\">"
            + "<dc:title>Extra_01</dc:title><upnp:class>object.item.audioItem</upnp:class>"
            + "<dc:creator>Creator_3</dc:creator>"
            + "<res protocolInfo=\"http-get:*:audio/mpeg:*\" size=\"8054458\" importUri=\"http://MediaServerContent_0/1/M3/\">http://MediaServerContent_0/1/M3/Test_3.mp3</res>"
            + "<upnp:writeStatus>UNKNOWN</upnp:writeStatus></item>" + RESPONSE_FOOTER;

    protected @Nullable UpnpServerHandler handler;

    private ChannelUID rendererChannelUID = new ChannelUID(THING_UID + ":" + UPNPRENDERER);
    private Channel rendererChannel = ChannelBuilder.create(rendererChannelUID, "String").build();

    private ChannelUID browseChannelUID = new ChannelUID(THING_UID + ":" + BROWSE);
    private Channel browseChannel = ChannelBuilder.create(browseChannelUID, "String").build();

    private ChannelUID currentTitleChannelUID = new ChannelUID(THING_UID + ":" + CURRENTTITLE);
    private Channel currentTitleChannel = ChannelBuilder.create(currentTitleChannelUID, "String").build();

    private ChannelUID searchChannelUID = new ChannelUID(THING_UID + ":" + SEARCH);
    private Channel searchChannel = ChannelBuilder.create(searchChannelUID, "String").build();

    private ChannelUID playlistSelectChannelUID = new ChannelUID(THING_UID + ":" + PLAYLIST_SELECT);
    private Channel playlistSelectChannel = ChannelBuilder.create(playlistSelectChannelUID, "String").build();

    private ChannelUID playlistChannelUID = new ChannelUID(THING_UID + ":" + PLAYLIST);
    private Channel playlistChannel = ChannelBuilder.create(playlistChannelUID, "String").build();

    private ChannelUID playlistActionChannelUID = new ChannelUID(THING_UID + ":" + PLAYLIST_ACTION);
    private Channel playlistActionChannel = ChannelBuilder.create(playlistActionChannelUID, "String").build();

    private ConcurrentMap<String, UpnpRendererHandler> upnpRenderers = new ConcurrentHashMap<>();

    @Mock
    private @Nullable UpnpRendererHandler rendererHandler;
    @Mock
    private @Nullable Thing rendererThing;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        // stub thing methods
        when(thing.getUID()).thenReturn(new ThingUID("upnpcontrol", "upnpserver", "mockserver"));
        when(thing.getLabel()).thenReturn("MockServer");
        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // stub upnpIOService methods for initialize
        Map<String, String> result = new HashMap<>();
        result.put("Result", BASE_CONTAINER);
        when(upnpIOService.invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap())).thenReturn(result);

        // stub rendererHandler, so that only one protocol is supported and results should be filtered when filter true
        when(rendererHandler.getSink()).thenReturn(Arrays.asList("http-get:*:audio/mpeg:*"));
        when(rendererHandler.getThing()).thenReturn(requireNonNull(rendererThing));
        when(rendererThing.getUID()).thenReturn(new ThingUID("upnpcontrol", "upnprenderer", "mockrenderer"));
        when(rendererThing.getLabel()).thenReturn("MockRenderer");
        upnpRenderers.put(rendererThing.getUID().toString(), requireNonNull(rendererHandler));

        // stub channels
        when(thing.getChannel(UPNPRENDERER)).thenReturn(rendererChannel);
        when(thing.getChannel(BROWSE)).thenReturn(browseChannel);
        when(thing.getChannel(CURRENTTITLE)).thenReturn(currentTitleChannel);
        when(thing.getChannel(SEARCH)).thenReturn(searchChannel);
        when(thing.getChannel(PLAYLIST_SELECT)).thenReturn(playlistSelectChannel);
        when(thing.getChannel(PLAYLIST)).thenReturn(playlistChannel);
        when(thing.getChannel(PLAYLIST_ACTION)).thenReturn(playlistActionChannel);

        // stub config for initialize
        when(config.as(UpnpControlServerConfiguration.class)).thenReturn(new UpnpControlServerConfiguration());

        handler = spy(
                new UpnpServerHandler(requireNonNull(thing), requireNonNull(upnpIOService), requireNonNull(upnpService),
                        requireNonNull(upnpRenderers), requireNonNull(upnpStateDescriptionProvider),
                        requireNonNull(upnpCommandDescriptionProvider), configuration));

        initHandler(requireNonNull(handler));

        handler.initialize();
    }

    @Override
    @AfterEach
    public void tearDown() {
        handler.dispose();

        super.tearDown();
    }

    @Test
    public void testBase() {
        logger.info("testBase");

        handler.config.filter = false;
        handler.config.browseDown = false;
        handler.config.searchFromRoot = false;

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is(UpnpServerHandler.DIRECTORY_ROOT));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("0")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("")));

        // Check entries
        assertThat(handler.entries.size(), is(2));
        assertThat(handler.entries.get(0).getId(), is("C1"));
        assertThat(handler.entries.get(0).getTitle(), is("All Audio Items"));
        assertThat(handler.entries.get(1).getId(), is("C2"));
        assertThat(handler.entries.get(1).getTitle(), is("All Image Items"));

        // Check that BROWSE channel gets the correct command options, no UP should be added
        ArgumentCaptor<List<StateOption>> commandOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                commandOptionListCaptor.capture());
        assertThat(commandOptionListCaptor.getValue().size(), is(2));
        assertThat(commandOptionListCaptor.getValue().get(0).getValue(), is("C1"));
        assertThat(commandOptionListCaptor.getValue().get(0).getLabel(), is("All Audio Items"));
        assertThat(commandOptionListCaptor.getValue().get(1).getValue(), is("C2"));
        assertThat(commandOptionListCaptor.getValue().get(1).getLabel(), is("All Image Items"));

        // Check media queue serving
        verify(rendererHandler, times(0)).registerQueue(any());
    }

    @Test
    public void testSetBrowse() {
        logger.info("testSetBrowse");

        handler.config.filter = false;
        handler.config.browseDown = false;
        handler.config.searchFromRoot = false;

        Map<String, String> result = new HashMap<>();
        result.put("Result", DOUBLE_MEDIA);
        doReturn(result).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());

        handler.handleCommand(browseChannelUID, StringType.valueOf("C11"));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("C11"));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("C11")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("")));

        // Check entries
        assertThat(handler.entries.size(), is(2));
        assertThat(handler.entries.get(0).getId(), is("M1"));
        assertThat(handler.entries.get(0).getTitle(), is("Music_01"));
        assertThat(handler.entries.get(1).getId(), is("M2"));
        assertThat(handler.entries.get(1).getTitle(), is("Music_02"));

        // Check that BROWSE channel gets the correct state options
        ArgumentCaptor<List<StateOption>> stateOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                stateOptionListCaptor.capture());
        assertThat(stateOptionListCaptor.getValue().size(), is(3));
        assertThat(stateOptionListCaptor.getValue().get(0).getValue(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(0).getLabel(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(1).getValue(), is("M1"));
        assertThat(stateOptionListCaptor.getValue().get(1).getLabel(), is("Music_01"));
        assertThat(stateOptionListCaptor.getValue().get(2).getValue(), is("M2"));
        assertThat(stateOptionListCaptor.getValue().get(2).getLabel(), is("Music_02"));

        // Check media queue serving
        verify(rendererHandler, times(0)).registerQueue(any());
    }

    @Test
    public void testSetBrowseRendererFilter() {
        logger.info("testSetBrowseRendererFilter");

        handler.config.filter = true;
        handler.config.browseDown = false;
        handler.config.searchFromRoot = false;

        handler.handleCommand(rendererChannelUID, StringType.valueOf(rendererThing.getUID().toString()));

        Map<String, String> result = new HashMap<>();
        result.put("Result", DOUBLE_MEDIA);
        doReturn(result).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());

        handler.handleCommand(browseChannelUID, StringType.valueOf("C11"));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("C11"));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("C11")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("")));

        // Check entries
        assertThat(handler.entries.size(), is(1));
        assertThat(handler.entries.get(0).getId(), is("M1"));
        assertThat(handler.entries.get(0).getTitle(), is("Music_01"));

        // Check that BROWSE channel gets the correct state options
        ArgumentCaptor<List<StateOption>> stateOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                stateOptionListCaptor.capture());
        assertThat(stateOptionListCaptor.getValue().size(), is(2));
        assertThat(stateOptionListCaptor.getValue().get(0).getValue(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(0).getLabel(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(1).getValue(), is("M1"));
        assertThat(stateOptionListCaptor.getValue().get(1).getLabel(), is("Music_01"));

        // Check media queue serving
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(UPNPRENDERER).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf(rendererThing.getUID().toString())));

        // Check media queue serving
        verify(rendererHandler).registerQueue(any());
    }

    @Test
    public void testBrowseContainers() {
        logger.info("testBrowseContainers");

        handler.config.filter = false;
        handler.config.browseDown = false;
        handler.config.searchFromRoot = false;

        Map<String, String> result = new HashMap<>();
        result.put("Result", DOUBLE_CONTAINER);
        doReturn(result).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());

        handler.handleCommand(browseChannelUID, StringType.valueOf("C1"));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("C1"));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("C1")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("All Audio Items")));

        // Check entries
        assertThat(handler.entries.size(), is(2));
        assertThat(handler.entries.get(0).getId(), is("C11"));
        assertThat(handler.entries.get(0).getTitle(), is("Morning Music"));
        assertThat(handler.entries.get(1).getId(), is("C12"));
        assertThat(handler.entries.get(1).getTitle(), is("Evening Music"));

        // Check that BROWSE channel gets the correct state options
        ArgumentCaptor<List<StateOption>> stateOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                stateOptionListCaptor.capture());
        assertThat(stateOptionListCaptor.getValue().size(), is(3));
        assertThat(stateOptionListCaptor.getValue().get(0).getValue(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(0).getLabel(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(1).getValue(), is("C11"));
        assertThat(stateOptionListCaptor.getValue().get(1).getLabel(), is("Morning Music"));
        assertThat(stateOptionListCaptor.getValue().get(2).getValue(), is("C12"));
        assertThat(stateOptionListCaptor.getValue().get(2).getLabel(), is("Evening Music"));

        // Check media queue serving
        verify(rendererHandler, times(0)).registerQueue(any());
    }

    @Test
    public void testBrowseOneContainerNoBrowseDown() {
        logger.info("testBrowseOneContainerNoBrowseDown");

        handler.config.filter = false;
        handler.config.browseDown = false;
        handler.config.searchFromRoot = false;

        Map<String, String> resultContainer = new HashMap<>();
        resultContainer.put("Result", SINGLE_CONTAINER);
        Map<String, String> resultMedia = new HashMap<>();
        resultMedia.put("Result", DOUBLE_MEDIA);
        doReturn(resultContainer).doReturn(resultMedia).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"),
                eq("Browse"), anyMap());

        handler.handleCommand(browseChannelUID, StringType.valueOf("C1"));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("C1"));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("C1")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("All Audio Items")));

        // Check entries
        assertThat(handler.entries.size(), is(1));
        assertThat(handler.entries.get(0).getId(), is("C11"));
        assertThat(handler.entries.get(0).getTitle(), is("Morning Music"));

        // Check that BROWSE channel gets the correct state options
        ArgumentCaptor<List<StateOption>> stateOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                stateOptionListCaptor.capture());
        assertThat(stateOptionListCaptor.getValue().size(), is(2));
        assertThat(stateOptionListCaptor.getValue().get(0).getValue(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(0).getLabel(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(1).getValue(), is("C11"));
        assertThat(stateOptionListCaptor.getValue().get(1).getLabel(), is("Morning Music"));

        // Check that a no media queue is being served as there is no renderer selected
        verify(rendererHandler, times(0)).registerQueue(any());
    }

    @Test
    public void testBrowseOneContainerBrowseDown() {
        logger.info("testBrowseOneContainerBrowseDown");

        handler.config.filter = false;
        handler.config.browseDown = true;
        handler.config.searchFromRoot = false;

        Map<String, String> resultContainer = new HashMap<>();
        resultContainer.put("Result", SINGLE_CONTAINER);
        Map<String, String> resultMedia = new HashMap<>();
        resultMedia.put("Result", DOUBLE_MEDIA);
        doReturn(resultContainer).doReturn(resultMedia).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"),
                eq("Browse"), anyMap());

        handler.handleCommand(browseChannelUID, StringType.valueOf("C1"));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("C11"));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("C11")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("Morning Music")));

        // Check entries
        assertThat(handler.entries.size(), is(2));
        assertThat(handler.entries.get(0).getId(), is("M1"));
        assertThat(handler.entries.get(0).getTitle(), is("Music_01"));
        assertThat(handler.entries.get(1).getId(), is("M2"));
        assertThat(handler.entries.get(1).getTitle(), is("Music_02"));

        // Check that BROWSE channel gets the correct state options
        ArgumentCaptor<List<StateOption>> stateOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                stateOptionListCaptor.capture());
        assertThat(stateOptionListCaptor.getValue().size(), is(3));
        assertThat(stateOptionListCaptor.getValue().get(0).getValue(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(0).getLabel(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(1).getValue(), is("M1"));
        assertThat(stateOptionListCaptor.getValue().get(1).getLabel(), is("Music_01"));
        assertThat(stateOptionListCaptor.getValue().get(2).getValue(), is("M2"));
        assertThat(stateOptionListCaptor.getValue().get(2).getLabel(), is("Music_02"));

        // Check media queue serving
        verify(rendererHandler, times(0)).registerQueue(any());
    }

    @Test
    public void testSearchOneContainerNotFromRootNoBrowseDown() {
        logger.info("testSearchOneContainerNotFromRootNoBrowseDown");

        handler.config.filter = false;
        handler.config.browseDown = false;
        handler.config.searchFromRoot = false;

        // First navigate away from root
        Map<String, String> result = new HashMap<>();
        result.put("Result", DOUBLE_CONTAINER);
        doReturn(result).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());
        handler.handleCommand(browseChannelUID, StringType.valueOf("C1"));

        Map<String, String> resultContainer = new HashMap<>();
        resultContainer.put("Result", SINGLE_CONTAINER);
        Map<String, String> resultMedia = new HashMap<>();
        resultMedia.put("Result", DOUBLE_MEDIA);
        doReturn(resultContainer).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Search"),
                anyMap());
        doReturn(resultMedia).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());

        String searchString = "dc:title contains \"Morning\" and upnp:class derivedfrom \"object.container\"";
        handler.handleCommand(searchChannelUID, StringType.valueOf(searchString));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("C1"));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("C1")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("All Audio Items")));

        // Check entries
        assertThat(handler.entries.size(), is(1));
        assertThat(handler.entries.get(0).getId(), is("C11"));
        assertThat(handler.entries.get(0).getTitle(), is("Morning Music"));

        // Check that BROWSE channel gets the correct state options
        ArgumentCaptor<List<StateOption>> stateOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                stateOptionListCaptor.capture());
        assertThat(stateOptionListCaptor.getValue().size(), is(2));
        assertThat(stateOptionListCaptor.getValue().get(0).getValue(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(0).getLabel(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(1).getValue(), is("C11"));
        assertThat(stateOptionListCaptor.getValue().get(1).getLabel(), is("Morning Music"));

        // Check that a no media queue is being served as there is no renderer selected
        verify(rendererHandler, times(0)).registerQueue(any());
    }

    @Test
    public void testSearchOneContainerNotFromRootBrowseDown() {
        logger.info("testSearchOneContainerNotFromRootBrowseDown");

        handler.config.filter = false;
        handler.config.browseDown = true;
        handler.config.searchFromRoot = false;

        // First navigate away from root
        Map<String, String> result = new HashMap<>();
        result.put("Result", DOUBLE_CONTAINER);
        doReturn(result).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());
        handler.handleCommand(browseChannelUID, StringType.valueOf("C1"));

        Map<String, String> resultContainer = new HashMap<>();
        resultContainer.put("Result", SINGLE_CONTAINER);
        Map<String, String> resultMedia = new HashMap<>();
        resultMedia.put("Result", DOUBLE_MEDIA);
        doReturn(resultContainer).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Search"),
                anyMap());
        doReturn(resultMedia).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());

        String searchString = "dc:title contains \"Morning\" and upnp:class derivedfrom \"object.container\"";
        handler.handleCommand(searchChannelUID, StringType.valueOf(searchString));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("C11"));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("C11")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("Morning Music")));

        // Check entries
        assertThat(handler.entries.size(), is(2));
        assertThat(handler.entries.get(0).getId(), is("M1"));
        assertThat(handler.entries.get(0).getTitle(), is("Music_01"));
        assertThat(handler.entries.get(1).getId(), is("M2"));
        assertThat(handler.entries.get(1).getTitle(), is("Music_02"));

        // Check that BROWSE channel gets the correct state options
        ArgumentCaptor<List<StateOption>> stateOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                stateOptionListCaptor.capture());
        assertThat(stateOptionListCaptor.getValue().size(), is(3));
        assertThat(stateOptionListCaptor.getValue().get(0).getValue(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(0).getLabel(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(1).getValue(), is("M1"));
        assertThat(stateOptionListCaptor.getValue().get(1).getLabel(), is("Music_01"));
        assertThat(stateOptionListCaptor.getValue().get(2).getValue(), is("M2"));
        assertThat(stateOptionListCaptor.getValue().get(2).getLabel(), is("Music_02"));

        // Check that a no media queue is being served as there is no renderer selected
        verify(rendererHandler, times(0)).registerQueue(any());
    }

    @Test
    public void testSearchOneContainerFromRootNoBrowseDown() {
        logger.info("testSearchOneContainerFromRootNoBrowseDown");

        handler.config.filter = false;
        handler.config.browseDown = false;
        handler.config.searchFromRoot = true;

        // First navigate away from root
        Map<String, String> result = new HashMap<>();
        result.put("Result", DOUBLE_CONTAINER);
        doReturn(result).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());
        handler.handleCommand(browseChannelUID, StringType.valueOf("C1"));

        Map<String, String> resultContainer = new HashMap<>();
        resultContainer.put("Result", SINGLE_CONTAINER);
        Map<String, String> resultMedia = new HashMap<>();
        resultMedia.put("Result", DOUBLE_MEDIA);
        doReturn(resultContainer).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Search"),
                anyMap());
        doReturn(resultMedia).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());

        String searchString = "dc:title contains \"Morning\" and upnp:class derivedfrom \"object.container\"";
        handler.handleCommand(searchChannelUID, StringType.valueOf(searchString));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("0"));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("0")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("")));

        // Check entries
        assertThat(handler.entries.size(), is(1));
        assertThat(handler.entries.get(0).getId(), is("C11"));
        assertThat(handler.entries.get(0).getTitle(), is("Morning Music"));

        // Check that BROWSE channel gets the correct state options
        ArgumentCaptor<List<StateOption>> stateOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                stateOptionListCaptor.capture());
        assertThat(stateOptionListCaptor.getValue().size(), is(2));
        assertThat(stateOptionListCaptor.getValue().get(0).getValue(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(0).getLabel(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(1).getValue(), is("C11"));
        assertThat(stateOptionListCaptor.getValue().get(1).getLabel(), is("Morning Music"));

        // Check that a no media queue is being served as there is no renderer selected
        verify(rendererHandler, times(0)).registerQueue(any());
    }

    @Test
    public void testSearchOneContainerFromRootBrowseDown() {
        logger.info("testSearchOneContainerFromRootBrowseDown");

        handler.config.filter = false;
        handler.config.browseDown = true;
        handler.config.searchFromRoot = true;

        // First navigate away from root
        Map<String, String> result = new HashMap<>();
        result.put("Result", DOUBLE_CONTAINER);
        doReturn(result).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());
        handler.handleCommand(browseChannelUID, StringType.valueOf("C1"));

        Map<String, String> resultContainer = new HashMap<>();
        resultContainer.put("Result", SINGLE_CONTAINER);
        Map<String, String> resultMedia = new HashMap<>();
        resultMedia.put("Result", DOUBLE_MEDIA);
        doReturn(resultContainer).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Search"),
                anyMap());
        doReturn(resultMedia).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());

        String searchString = "dc:title contains \"Morning\" and upnp:class derivedfrom \"object.container\"";
        handler.handleCommand(searchChannelUID, StringType.valueOf(searchString));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("C11"));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("C11")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("Morning Music")));

        // Check entries
        assertThat(handler.entries.size(), is(2));
        assertThat(handler.entries.get(0).getId(), is("M1"));
        assertThat(handler.entries.get(0).getTitle(), is("Music_01"));
        assertThat(handler.entries.get(1).getId(), is("M2"));
        assertThat(handler.entries.get(1).getTitle(), is("Music_02"));

        // Check that BROWSE channel gets the correct state options
        ArgumentCaptor<List<StateOption>> stateOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                stateOptionListCaptor.capture());
        assertThat(stateOptionListCaptor.getValue().size(), is(3));
        assertThat(stateOptionListCaptor.getValue().get(0).getValue(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(0).getLabel(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(1).getValue(), is("M1"));
        assertThat(stateOptionListCaptor.getValue().get(1).getLabel(), is("Music_01"));
        assertThat(stateOptionListCaptor.getValue().get(2).getValue(), is("M2"));
        assertThat(stateOptionListCaptor.getValue().get(2).getLabel(), is("Music_02"));

        // Check that a no media queue is being served as there is no renderer selected
        verify(rendererHandler, times(0)).registerQueue(any());
    }

    @Test
    public void testSearchMediaFromRootBrowseDownFilter() {
        logger.info("testSearchMediaFromRootBrowseDownFilter");

        handler.config.filter = true;
        handler.config.browseDown = true;
        handler.config.searchFromRoot = true;

        // First navigate away from root
        Map<String, String> result = new HashMap<>();
        result.put("Result", DOUBLE_CONTAINER);
        doReturn(result).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Browse"), anyMap());
        handler.handleCommand(browseChannelUID, StringType.valueOf("C1"));

        Map<String, String> resultMedia = new HashMap<>();
        resultMedia.put("Result", DOUBLE_MEDIA);
        doReturn(resultMedia).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Search"), anyMap());

        String searchString = "dc:title contains \"Music\"";
        handler.handleCommand(searchChannelUID, StringType.valueOf(searchString));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("0"));

        // Check BROWSE
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(BROWSE).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("0")));

        // Check CURRENTTITLE
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CURRENTTITLE).getUID()),
                stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("")));

        // Check entries
        assertThat(handler.entries.size(), is(2));
        assertThat(handler.entries.get(0).getId(), is("M1"));
        assertThat(handler.entries.get(0).getTitle(), is("Music_01"));
        assertThat(handler.entries.get(1).getId(), is("M2"));
        assertThat(handler.entries.get(1).getTitle(), is("Music_02"));

        // Check that BROWSE channel gets the correct state options
        ArgumentCaptor<List<StateOption>> stateOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateStateDescription(eq(thing.getChannel(BROWSE).getUID()),
                stateOptionListCaptor.capture());
        assertThat(stateOptionListCaptor.getValue().size(), is(3));
        assertThat(stateOptionListCaptor.getValue().get(0).getValue(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(0).getLabel(), is(".."));
        assertThat(stateOptionListCaptor.getValue().get(1).getValue(), is("M1"));
        assertThat(stateOptionListCaptor.getValue().get(1).getLabel(), is("Music_01"));
        assertThat(stateOptionListCaptor.getValue().get(2).getValue(), is("M2"));
        assertThat(stateOptionListCaptor.getValue().get(2).getLabel(), is("Music_02"));

        // Check that a no media queue is being served as there is no renderer selected
        verify(rendererHandler, times(0)).registerQueue(any());
    }

    @Test
    public void testPlaylist() {
        logger.info("testPlaylist");

        handler.config.filter = false;
        handler.config.browseDown = false;
        handler.config.searchFromRoot = true;

        // Check already called in initialize
        verify(handler).playlistsListChanged();

        // First search for media
        Map<String, String> resultMedia = new HashMap<>();
        resultMedia.put("Result", DOUBLE_MEDIA);
        doReturn(resultMedia).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Search"), anyMap());
        String searchString = "dc:title contains \"Music\"";
        handler.handleCommand(searchChannelUID, StringType.valueOf(searchString));

        // Save playlist
        handler.handleCommand(playlistChannelUID, StringType.valueOf("Test_Playlist"));
        handler.handleCommand(playlistActionChannelUID, StringType.valueOf("SAVE"));

        // Check called after saving playlist
        verify(handler, times(2)).playlistsListChanged();

        // Check that PLAYLIST_SELECT channel now has the playlist as a state option
        ArgumentCaptor<List<CommandOption>> commandOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateCommandDescription(eq(thing.getChannel(PLAYLIST_SELECT).getUID()),
                commandOptionListCaptor.capture());
        assertThat(commandOptionListCaptor.getValue().size(), is(1));
        assertThat(commandOptionListCaptor.getValue().get(0).getCommand(), is("Test_Playlist"));
        assertThat(commandOptionListCaptor.getValue().get(0).getLabel(), is("Test_Playlist"));

        // Clear PLAYLIST channel
        handler.handleCommand(playlistChannelUID, StringType.valueOf(""));

        // Search for some extra media
        resultMedia = new HashMap<>();
        resultMedia.put("Result", EXTRA_MEDIA);
        doReturn(resultMedia).when(upnpIOService).invokeAction(any(), eq("ContentDirectory"), eq("Search"), anyMap());
        searchString = "dc:title contains \"Extra\"";
        handler.handleCommand(searchChannelUID, StringType.valueOf(searchString));

        // Append to playlist
        handler.handleCommand(playlistSelectChannelUID, StringType.valueOf("Test_Playlist"));
        handler.handleCommand(playlistActionChannelUID, StringType.valueOf("APPEND"));

        // Check called after appending to playlist
        verify(handler, times(3)).playlistsListChanged();

        // Check that PLAYLIST channel received "Test_Playlist"
        ArgumentCaptor<StringType> stringCaptor = ArgumentCaptor.forClass(StringType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(PLAYLIST).getUID()), stringCaptor.capture());
        assertThat(stringCaptor.getValue(), is(StringType.valueOf("Test_Playlist")));

        // Clear PLAYLIST channel
        handler.handleCommand(playlistChannelUID, StringType.valueOf(""));

        // Restore playlist
        handler.handleCommand(playlistSelectChannelUID, StringType.valueOf("Test_Playlist"));
        handler.handleCommand(playlistActionChannelUID, StringType.valueOf("RESTORE"));

        // Check currentEntry
        assertThat(handler.currentEntry.getId(), is("C11"));

        // Check entries
        assertThat(handler.entries.size(), is(3));
        assertThat(handler.entries.get(0).getId(), is("M1"));
        assertThat(handler.entries.get(0).getTitle(), is("Music_01"));
        assertThat(handler.entries.get(1).getId(), is("M2"));
        assertThat(handler.entries.get(1).getTitle(), is("Music_02"));
        assertThat(handler.entries.get(2).getId(), is("M3"));
        assertThat(handler.entries.get(2).getTitle(), is("Extra_01"));

        // Delete playlist
        handler.handleCommand(playlistSelectChannelUID, StringType.valueOf("Test_Playlist"));
        handler.handleCommand(playlistActionChannelUID, StringType.valueOf("DELETE"));

        // Check called after deleting playlist
        verify(handler, times(4)).playlistsListChanged();

        // Check that PLAYLIST_SELECT channel is empty again
        commandOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateCommandDescription(eq(thing.getChannel(PLAYLIST_SELECT).getUID()),
                commandOptionListCaptor.capture());
        assertThat(commandOptionListCaptor.getValue().size(), is(0));

        // select a renderer, so we expect the "current" playlist to be created
        handler.handleCommand(rendererChannelUID, StringType.valueOf(rendererThing.getUID().toString()));

        // Check called after selecting renderer
        verify(handler, times(5)).playlistsListChanged();

        // Check that PLAYLIST_SELECT channel received "current" playlist
        verify(handler, atLeastOnce()).updateCommandDescription(eq(thing.getChannel(PLAYLIST_SELECT).getUID()),
                commandOptionListCaptor.capture());
        assertThat(commandOptionListCaptor.getValue().size(), is(1));
        assertThat(commandOptionListCaptor.getValue().get(0).getCommand(), is("current"));
        assertThat(commandOptionListCaptor.getValue().get(0).getLabel(), is("current"));
    }
}
