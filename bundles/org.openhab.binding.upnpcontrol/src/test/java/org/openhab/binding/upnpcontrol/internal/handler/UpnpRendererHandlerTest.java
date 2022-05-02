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
package org.openhab.binding.upnpcontrol.internal.handler;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.upnpcontrol.internal.audiosink.UpnpAudioSinkReg;
import org.openhab.binding.upnpcontrol.internal.config.UpnpControlRendererConfiguration;
import org.openhab.binding.upnpcontrol.internal.queue.UpnpEntry;
import org.openhab.binding.upnpcontrol.internal.queue.UpnpEntryQueue;
import org.openhab.binding.upnpcontrol.internal.queue.UpnpEntryRes;
import org.openhab.binding.upnpcontrol.internal.util.UpnpXMLParser;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for {@link UpnpRendererHandler}.
 *
 * @author Mark Herwege - Initial contribution
 */
@SuppressWarnings({ "null", "unchecked" })
@NonNullByDefault
public class UpnpRendererHandlerTest extends UpnpHandlerTest {

    private final Logger logger = LoggerFactory.getLogger(UpnpRendererHandlerTest.class);

    private static final String THING_TYPE_UID = "upnpcontrol:upnprenderer";
    private static final String THING_UID = THING_TYPE_UID + ":mockrenderer";

    private static final String LAST_CHANGE_HEADER = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">"
            + "<InstanceID val=\"0\">";
    private static final String LAST_CHANGE_FOOTER = "</InstanceID></Event>";
    private static final String AV_TRANSPORT_URI = "<AVTransportURI val=\"";
    private static final String AV_TRANSPORT_URI_METADATA = "<AVTransportURIMetaData val=\"";
    private static final String CURRENT_TRACK_URI = "<CurrentTrackURI val=\"";
    private static final String CURRENT_TRACK_METADATA = "<CurrentTrackMetaData val=\"";
    private static final String TRANSPORT_STATE = "<TransportState val=\"";
    private static final String CLOSE = "\"/>";

    protected @Nullable UpnpRendererHandler handler;

    private @Nullable UpnpEntryQueue upnpEntryQueue;

    private ChannelUID volumeChannelUID = new ChannelUID(THING_UID + ":" + VOLUME);
    private Channel volumeChannel = ChannelBuilder.create(volumeChannelUID, "Dimmer").build();

    private ChannelUID muteChannelUID = new ChannelUID(THING_UID + ":" + MUTE);
    private Channel muteChannel = ChannelBuilder.create(muteChannelUID, "Switch").build();

    private ChannelUID stopChannelUID = new ChannelUID(THING_UID + ":" + STOP);
    private Channel stopChannel = ChannelBuilder.create(stopChannelUID, "Switch").build();

    private ChannelUID controlChannelUID = new ChannelUID(THING_UID + ":" + CONTROL);
    private Channel controlChannel = ChannelBuilder.create(controlChannelUID, "Player").build();

    private ChannelUID repeatChannelUID = new ChannelUID(THING_UID + ":" + REPEAT);
    private Channel repeatChannel = ChannelBuilder.create(repeatChannelUID, "Switch").build();

    private ChannelUID shuffleChannelUID = new ChannelUID(THING_UID + ":" + SHUFFLE);
    private Channel shuffleChannel = ChannelBuilder.create(shuffleChannelUID, "Switch").build();

    private ChannelUID onlyPlayOneChannelUID = new ChannelUID(THING_UID + ":" + ONLY_PLAY_ONE);
    private Channel onlyPlayOneChannel = ChannelBuilder.create(onlyPlayOneChannelUID, "Switch").build();

    private ChannelUID uriChannelUID = new ChannelUID(THING_UID + ":" + URI);
    private Channel uriChannel = ChannelBuilder.create(uriChannelUID, "String").build();

    private ChannelUID favoriteSelectChannelUID = new ChannelUID(THING_UID + ":" + FAVORITE_SELECT);
    private Channel favoriteSelectChannel = ChannelBuilder.create(favoriteSelectChannelUID, "String").build();

    private ChannelUID favoriteChannelUID = new ChannelUID(THING_UID + ":" + FAVORITE);
    private Channel favoriteChannel = ChannelBuilder.create(favoriteChannelUID, "String").build();

    private ChannelUID favoriteActionChannelUID = new ChannelUID(THING_UID + ":" + FAVORITE_ACTION);
    private Channel favoriteActionChannel = ChannelBuilder.create(favoriteActionChannelUID, "String").build();

    private ChannelUID playlistSelectChannelUID = new ChannelUID(THING_UID + ":" + PLAYLIST_SELECT);
    private Channel playlistSelectChannel = ChannelBuilder.create(playlistSelectChannelUID, "String").build();

    private ChannelUID titleChannelUID = new ChannelUID(THING_UID + ":" + TITLE);
    private Channel titleChannel = ChannelBuilder.create(titleChannelUID, "String").build();

    private ChannelUID albumChannelUID = new ChannelUID(THING_UID + ":" + ALBUM);
    private Channel albumChannel = ChannelBuilder.create(albumChannelUID, "String").build();

    private ChannelUID albumArtChannelUID = new ChannelUID(THING_UID + ":" + ALBUM_ART);
    private Channel albumArtChannel = ChannelBuilder.create(albumArtChannelUID, "Image").build();

    private ChannelUID creatorChannelUID = new ChannelUID(THING_UID + ":" + CREATOR);
    private Channel creatorChannel = ChannelBuilder.create(creatorChannelUID, "String").build();

    private ChannelUID artistChannelUID = new ChannelUID(THING_UID + ":" + ARTIST);
    private Channel artistChannel = ChannelBuilder.create(artistChannelUID, "String").build();

    private ChannelUID publisherChannelUID = new ChannelUID(THING_UID + ":" + PUBLISHER);
    private Channel publisherChannel = ChannelBuilder.create(publisherChannelUID, "String").build();

    private ChannelUID genreChannelUID = new ChannelUID(THING_UID + ":" + GENRE);
    private Channel genreChannel = ChannelBuilder.create(genreChannelUID, "String").build();

    private ChannelUID trackNumberChannelUID = new ChannelUID(THING_UID + ":" + TRACK_NUMBER);
    private Channel trackNumberChannel = ChannelBuilder.create(trackNumberChannelUID, "Number").build();

    private ChannelUID trackDurationChannelUID = new ChannelUID(THING_UID + ":" + TRACK_DURATION);
    private Channel trackDurationChannel = ChannelBuilder.create(trackDurationChannelUID, "Number:Time").build();

    private ChannelUID trackPositionChannelUID = new ChannelUID(THING_UID + ":" + TRACK_POSITION);
    private Channel trackPositionChannel = ChannelBuilder.create(trackPositionChannelUID, "Number:Time").build();

    private ChannelUID relTrackPositionChannelUID = new ChannelUID(THING_UID + ":" + REL_TRACK_POSITION);
    private Channel relTrackPositionChannel = ChannelBuilder.create(relTrackPositionChannelUID, "Dimmer").build();

    @Mock
    private @Nullable UpnpAudioSinkReg audioSinkReg;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        // stub thing methods
        when(thing.getUID()).thenReturn(new ThingUID("upnpcontrol", "upnprenderer", "mockrenderer"));
        when(thing.getLabel()).thenReturn("MockRenderer");
        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // stub channels
        when(thing.getChannel(VOLUME)).thenReturn(volumeChannel);
        when(thing.getChannel(MUTE)).thenReturn(muteChannel);
        when(thing.getChannel(STOP)).thenReturn(stopChannel);
        when(thing.getChannel(CONTROL)).thenReturn(controlChannel);
        when(thing.getChannel(REPEAT)).thenReturn(repeatChannel);
        when(thing.getChannel(SHUFFLE)).thenReturn(shuffleChannel);
        when(thing.getChannel(ONLY_PLAY_ONE)).thenReturn(onlyPlayOneChannel);
        when(thing.getChannel(URI)).thenReturn(uriChannel);
        when(thing.getChannel(FAVORITE_SELECT)).thenReturn(favoriteSelectChannel);
        when(thing.getChannel(FAVORITE)).thenReturn(favoriteChannel);
        when(thing.getChannel(FAVORITE_ACTION)).thenReturn(favoriteActionChannel);
        when(thing.getChannel(PLAYLIST_SELECT)).thenReturn(playlistSelectChannel);
        when(thing.getChannel(TITLE)).thenReturn(titleChannel);
        when(thing.getChannel(ALBUM)).thenReturn(albumChannel);
        when(thing.getChannel(ALBUM_ART)).thenReturn(albumArtChannel);
        when(thing.getChannel(CREATOR)).thenReturn(creatorChannel);
        when(thing.getChannel(ARTIST)).thenReturn(artistChannel);
        when(thing.getChannel(PUBLISHER)).thenReturn(publisherChannel);
        when(thing.getChannel(GENRE)).thenReturn(genreChannel);
        when(thing.getChannel(TRACK_NUMBER)).thenReturn(trackNumberChannel);
        when(thing.getChannel(TRACK_DURATION)).thenReturn(trackDurationChannel);
        when(thing.getChannel(TRACK_POSITION)).thenReturn(trackPositionChannel);
        when(thing.getChannel(REL_TRACK_POSITION)).thenReturn(relTrackPositionChannel);

        // stub config for initialize
        when(config.as(UpnpControlRendererConfiguration.class)).thenReturn(new UpnpControlRendererConfiguration());

        // create a media queue for playing
        List<UpnpEntry> entries = createUpnpEntries();
        upnpEntryQueue = new UpnpEntryQueue(entries, "54321");

        handler = spy(new UpnpRendererHandler(requireNonNull(thing), requireNonNull(upnpIOService),
                requireNonNull(audioSinkReg), requireNonNull(upnpStateDescriptionProvider),
                requireNonNull(upnpCommandDescriptionProvider), configuration));

        initHandler(requireNonNull(handler));

        handler.initialize();

        expectLastChangeOnStop(true);
        expectLastChangeOnPlay(true);
        expectLastChangeOnPause(true);
    }

    private List<UpnpEntry> createUpnpEntries() {
        List<UpnpEntry> entries = new ArrayList<>();
        UpnpEntry entry;
        List<UpnpEntryRes> resList;
        UpnpEntryRes res;
        resList = new ArrayList<>();
        res = new UpnpEntryRes("http-get:*:audio/mpeg:*", 8054458L, "10", "http://MediaServerContent_0/1/M0/");
        res.setRes("http://MediaServerContent_0/1/M0/Test_0.mp3");
        resList.add(res);
        entry = new UpnpEntry("M0", "M0", "C11", "object.item.audioItem").withTitle("Music_00").withResList(resList)
                .withAlbum("My Music 0").withCreator("Creator_0").withArtist("Artist_0").withGenre("Morning")
                .withPublisher("myself 0").withAlbumArtUri("").withTrackNumber(1);
        entries.add(entry);
        resList = new ArrayList<>();
        res = new UpnpEntryRes("http-get:*:audio/wav:*", 1156598L, "6", "http://MediaServerContent_0/1/M1/");
        res.setRes("http://MediaServerContent_0/1/M1/Test_1.wav");
        resList.add(res);
        entry = new UpnpEntry("M1", "M1", "C11", "object.item.audioItem").withTitle("Music_01").withResList(resList)
                .withAlbum("My Music 0").withCreator("Creator_1").withArtist("Artist_1").withGenre("Morning")
                .withPublisher("myself 1").withAlbumArtUri("").withTrackNumber(2);
        entries.add(entry);
        resList = new ArrayList<>();
        res = new UpnpEntryRes("http-get:*:audio/mpeg:*", 1156598L, "40", "http://MediaServerContent_0/1/M2/");
        res.setRes("http://MediaServerContent_0/1/M2/Test_2.mp3");
        resList.add(res);
        entry = new UpnpEntry("M2", "M2", "C12", "object.item.audioItem").withTitle("Music_02").withResList(resList)
                .withAlbum("My Music 2").withCreator("Creator_2").withArtist("Artist_2").withGenre("Evening")
                .withPublisher("myself 2").withAlbumArtUri("").withTrackNumber(1);
        entries.add(entry);
        return entries;
    }

    @Override
    @AfterEach
    public void tearDown() {
        handler.dispose();

        super.tearDown();
    }

    @Test
    public void testRegisterQueue() {
        logger.info("testRegisterQueue");

        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        checkInternalState(0, 1, true, false, true, false);
        checkControlChannel(PlayPauseType.PAUSE);
        checkSetURI(0, 1);
        checkMetadataChannels(0);
    }

    @Test
    public void testPlayQueue() {
        logger.info("testPlayQueue");

        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        // Play media
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        checkInternalState(0, 1, false, true, false, true);
        checkControlChannel(PlayPauseType.PLAY);
        checkSetURI(0, 1);
        checkMetadataChannels(0);
    }

    @Test
    public void testStop() {
        logger.info("testStop");

        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        // Play media
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        // Stop playback
        handler.handleCommand(stopChannelUID, OnOffType.ON);

        checkInternalState(0, 1, true, false, false, false);
        checkControlChannel(PlayPauseType.PAUSE);
        checkSetURI(0, 1);
        checkMetadataChannels(0);
    }

    @Test
    public void testPause() {
        logger.info("testPause");

        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        // Play media
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        // Pause media
        handler.handleCommand(controlChannelUID, PlayPauseType.PAUSE);

        checkControlChannel(PlayPauseType.PAUSE);

        // Continue playing
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        checkControlChannel(PlayPauseType.PLAY);
    }

    @Test
    public void testPauseNotSupported() {
        logger.info("testPauseNotSupported");

        // Some players don't support pause and just continue playing.
        // Test if we properly switch back to playing state if no confirmation of pause received.

        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        // Play media
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        // Pause media
        // Do not receive a PAUSED_PLAYBACK response
        expectLastChangeOnPause(false);
        handler.handleCommand(controlChannelUID, PlayPauseType.PAUSE);

        // Wait long enough for status to turn back to PLAYING.
        // All timeouts in test are set to 1s.
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ignore) {
        }

        checkControlChannel(PlayPauseType.PLAY);
    }

    @Test
    public void testRegisterQueueWhilePlaying() {
        logger.info("testRegisterQueueWhilePlaying");

        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 2);
        List<UpnpEntry> startList = new ArrayList<UpnpEntry>();
        startList.add(requireNonNull(upnpEntryQueue.get(2)));
        UpnpEntryQueue startQueue = new UpnpEntryQueue(startList, "54321");
        handler.registerQueue(requireNonNull(startQueue));

        // Play media
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        // Register a new media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        checkInternalState(2, 0, false, true, true, true);
        checkControlChannel(PlayPauseType.PLAY);
        checkSetURI(null, 0);
        checkMetadataChannels(2);
    }

    @Test
    public void testNext() {
        logger.info("testNext");

        testNext(false, false);
    }

    @Test
    public void testNextRepeat() {
        logger.info("testNextRepeat");

        testNext(false, true);
    }

    @Test
    public void testNextWhilePlaying() {
        logger.info("testNextWhilePlaying");

        testNext(true, false);
    }

    @Test
    public void testNextWhilePlayingRepeat() {
        logger.info("testNextWhilePlayingRepeat");

        testNext(true, true);
    }

    private void testNext(boolean play, boolean repeat) {
        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        if (repeat) {
            handler.handleCommand(repeatChannelUID, OnOffType.ON);
        }

        if (play) {
            // Play media
            handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);
        }

        // Next media
        expectLastChangeOnSetAVTransportURI(true, 1);
        handler.handleCommand(controlChannelUID, NextPreviousType.NEXT);

        checkInternalState(1, 2, play ? false : true, play ? true : false, play ? false : true, play ? true : false);
        checkControlChannel(play ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        checkSetURI(1, 2);
        checkMetadataChannels(1);

        // Next media
        expectLastChangeOnSetAVTransportURI(true, 2);
        handler.handleCommand(controlChannelUID, NextPreviousType.NEXT);

        checkInternalState(2, repeat ? 0 : null, play ? false : true, play ? true : false, play ? false : true,
                play ? true : false);
        checkControlChannel(play ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        checkSetURI(2, repeat ? 0 : null);
        checkMetadataChannels(2);

        // Next media
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.handleCommand(controlChannelUID, NextPreviousType.NEXT);

        checkInternalState(0, 1, (play && repeat) ? false : true, (play && repeat) ? true : false,
                (play && repeat) ? false : true, (play && repeat) ? true : false);
        checkControlChannel((play && repeat) ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        checkSetURI(0, 1);
        checkMetadataChannels(0);
    }

    @Test
    public void testPrevious() {
        logger.info("testPrevious");

        testPrevious(false, false);
    }

    @Test
    public void testPreviousRepeat() {
        logger.info("testPreviousRepeat");

        testPrevious(false, true);
    }

    @Test
    public void testPreviousWhilePlaying() {
        logger.info("testPreviousWhilePlaying");

        testPrevious(true, false);
    }

    @Test
    public void testPreviousWhilePlayingRepeat() {
        logger.info("testPreviousWhilePlayingRepeat");

        testPrevious(true, true);
    }

    public void testPrevious(boolean play, boolean repeat) {
        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        if (repeat) {
            handler.handleCommand(repeatChannelUID, OnOffType.ON);
        }

        if (play) {
            // Play media
            handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);
        }

        // Next media
        expectLastChangeOnSetAVTransportURI(true, 1);
        handler.handleCommand(controlChannelUID, NextPreviousType.NEXT);

        // Previous media
        expectLastChangeOnSetAVTransportURI(true, 2);
        handler.handleCommand(controlChannelUID, NextPreviousType.PREVIOUS);

        checkInternalState(0, 1, play ? false : true, play ? true : false, play ? false : true, play ? true : false);
        checkControlChannel(play ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        checkSetURI(0, 1);
        checkMetadataChannels(0);

        // Previous media
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.handleCommand(controlChannelUID, NextPreviousType.PREVIOUS);

        checkInternalState(repeat ? 2 : 0, repeat ? 0 : 1, (play && repeat) ? false : true,
                (play && repeat) ? true : false, (play && repeat) ? false : true, (play && repeat) ? true : false);
        checkControlChannel((play && repeat) ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        checkSetURI(repeat ? 2 : 0, repeat ? 0 : 1);
        checkMetadataChannels(repeat ? 2 : 0);
    }

    @Test
    public void testAutoPlayNextInQueue() {
        logger.info("testAutoPlayNextInQueue");

        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        // Play media
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        // We expect GENA LastChange event with new metadata when the renderer starts to play next entry
        expectLastChangeOnSetAVTransportURI(true, 1);

        // At the end of the media, we will get GENA LastChange STOP event, renderer should move to next media and play
        // Force this STOP event for test
        String lastChange = LAST_CHANGE_HEADER + TRANSPORT_STATE + "STOPPED" + CLOSE + LAST_CHANGE_FOOTER;
        handler.onValueReceived("LastChange", lastChange, "AVTransport");

        checkInternalState(1, 2, false, true, false, true);
        checkControlChannel(PlayPauseType.PLAY);
        checkSetURI(1, 2);
        checkMetadataChannels(1);
    }

    @Test
    public void testAutoPlayNextInQueueGapless() {
        logger.info("testAutoPlayNextInQueueGapless");

        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        // Play media
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        // We expect GENA LastChange event with new metadata when the renderer starts to play next entry
        expectLastChangeOnSetAVTransportURI(true, 1);

        // At the end of the media, we will get GENA event with new URI and metadata
        String lastChange = LAST_CHANGE_HEADER + AV_TRANSPORT_URI + upnpEntryQueue.get(1).getRes() + CLOSE
                + AV_TRANSPORT_URI_METADATA + UpnpXMLParser.compileMetadataString(requireNonNull(upnpEntryQueue.get(0)))
                + CLOSE + CURRENT_TRACK_URI + upnpEntryQueue.get(1).getRes() + CLOSE + CURRENT_TRACK_METADATA
                + UpnpXMLParser.compileMetadataString(requireNonNull(upnpEntryQueue.get(1))) + CLOSE
                + LAST_CHANGE_FOOTER;
        handler.onValueReceived("LastChange", lastChange, "AVTransport");

        checkInternalState(1, 2, false, true, false, true);
        checkControlChannel(PlayPauseType.PLAY);
        checkSetURI(null, 2);
        checkMetadataChannels(1);
    }

    @Test
    public void testOnlyPlayOne() {
        logger.info("testOnlyPlayOne");

        handler.handleCommand(onlyPlayOneChannelUID, OnOffType.ON);

        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        // Play media
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        checkInternalState(0, 1, false, true, false, true);
        checkSetURI(0, null);
        checkMetadataChannels(0);

        // We expect GENA LastChange event with new metadata when the renderer has finished playing
        expectLastChangeOnSetAVTransportURI(true, 1);

        // At the end of the media, we will get GENA LastChange STOP event, renderer should stop
        // Force this STOP event for test
        String lastChange = LAST_CHANGE_HEADER + TRANSPORT_STATE + "STOPPED" + CLOSE + LAST_CHANGE_FOOTER;
        handler.onValueReceived("LastChange", lastChange, "AVTransport");

        checkInternalState(1, 2, false, false, false, true);
        checkControlChannel(PlayPauseType.PAUSE);
        checkSetURI(1, null);
        checkMetadataChannels(1);
    }

    @Test
    public void testPlayUri() {
        logger.info("testPlayUri");

        expectLastChangeOnSetAVTransportURI(true, false, 0);
        handler.handleCommand(uriChannelUID, StringType.valueOf(upnpEntryQueue.get(0).getRes()));

        // Play media
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        checkInternalState(null, null, false, true, false, false);
        checkControlChannel(PlayPauseType.PLAY);
        checkSetURI(0, null, false);
        checkMetadataChannels(0, true);
    }

    @Test
    public void testPlayAction() {
        logger.info("testPlayAction");

        expectLastChangeOnSetAVTransportURI(true, false, 0);

        // Methods called in sequence by audio sink
        handler.setCurrentURI(upnpEntryQueue.get(0).getRes(), "");
        handler.play();

        checkInternalState(null, null, false, true, false, false);
        checkControlChannel(PlayPauseType.PLAY);
        checkSetURI(0, null, false);
        checkMetadataChannels(0, true);
    }

    @Test
    public void testPlayNotification() {
        logger.info("testPlayNotification");

        // Register a media queue
        expectLastChangeOnSetAVTransportURI(true, 0);
        handler.registerQueue(requireNonNull(upnpEntryQueue));

        // Set volume
        expectLastChangeOnSetVolume(true, 50);
        handler.setVolume(new PercentType(50));

        checkInternalState(0, 1, true, false, true, false);
        checkSetURI(0, 1, true);
        checkMetadataChannels(0, false);

        // Play notification, at standard 10% volume above current volume level
        expectLastChangeOnSetAVTransportURI(true, false, 2);
        expectLastChangeOnGetPositionInfo(true, "00:00:00");
        handler.playNotification(upnpEntryQueue.get(2).getRes());

        checkInternalState(0, 1, true, false, true, false);
        checkSetURI(2, null, false);
        checkMetadataChannels(0, false);
        verify(handler).setVolume(new PercentType(55));

        // At the end of the notification, we will get GENA LastChange STOP event
        // Force this STOP event for test
        expectLastChangeOnSetAVTransportURI(true, false, 0);
        String lastChange = LAST_CHANGE_HEADER + TRANSPORT_STATE + "STOPPED" + CLOSE + LAST_CHANGE_FOOTER;
        handler.onValueReceived("LastChange", lastChange, "AVTransport");

        checkInternalState(0, 1, true, false, true, false);
        checkMetadataChannels(0, false);
        verify(handler, times(2)).setVolume(new PercentType(50));

        // Play media and move to position
        handler.handleCommand(controlChannelUID, PlayPauseType.PLAY);

        checkInternalState(0, 1, false, true, false, true); //
        checkSetURI(0, 1, true);
        checkMetadataChannels(0, false);

        // Play notification again, while simulating the current playing media is at 10s position
        // Play at volume level provided by audiSink action
        expectLastChangeOnSetAVTransportURI(true, false, 2);
        expectLastChangeOnGetPositionInfo(true, "00:00:10");
        handler.setNotificationVolume(new PercentType(70));
        handler.playNotification(upnpEntryQueue.get(2).getRes());

        checkInternalState(0, 1, false, true, false, true);
        checkSetURI(2, null, false);
        checkMetadataChannels(0, false);
        verify(handler).setVolume(new PercentType(70));

        // Wait long enough for max notification duration to be reached.
        // In the test, we have enforced 500ms delay through schedule mock.
        expectLastChangeOnSetAVTransportURI(true, false, 0);
        try {
            TimeUnit.SECONDS.sleep(1);
            logger.info("Test playing {}, stopped {}", handler.playing, handler.playerStopped);
        } catch (InterruptedException ignore) {
        }

        checkInternalState(0, 1, false, true, false, true);
        checkSetURI(0, null, false);
        checkMetadataChannels(0, false);
        verify(handler, times(3)).setVolume(new PercentType(50));
        verify(callback, times(2)).stateUpdated(trackPositionChannelUID, new QuantityType<>(10, Units.SECOND));
    }

    @Test
    public void testFavorite() {
        logger.info("testFavorite");

        // Check already called in initialize
        verify(handler).updateFavoritesList();

        // First set URI
        expectLastChangeOnSetAVTransportURI(true, false, 0);
        handler.handleCommand(uriChannelUID, StringType.valueOf(upnpEntryQueue.get(0).getRes()));

        // Save favorite
        handler.handleCommand(favoriteChannelUID, StringType.valueOf("Test_Favorite"));
        handler.handleCommand(favoriteActionChannelUID, StringType.valueOf("SAVE"));

        // Check called after saving favorite
        verify(handler, times(2)).updateFavoritesList();

        // Check that FAVORITE_SELECT channel now has the favorite as a state option
        ArgumentCaptor<List<CommandOption>> commandOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateCommandDescription(eq(thing.getChannel(FAVORITE_SELECT).getUID()),
                commandOptionListCaptor.capture());
        assertThat(commandOptionListCaptor.getValue().size(), is(1));
        assertThat(commandOptionListCaptor.getValue().get(0).getCommand(), is("Test_Favorite"));
        assertThat(commandOptionListCaptor.getValue().get(0).getLabel(), is("Test_Favorite"));

        // Clear FAVORITE channel
        handler.handleCommand(favoriteChannelUID, StringType.valueOf(""));

        // Set another URI
        expectLastChangeOnSetAVTransportURI(true, false, 2);
        handler.handleCommand(uriChannelUID, StringType.valueOf(upnpEntryQueue.get(2).getRes()));

        checkInternalState(null, null, false, true, false, false);
        checkSetURI(2, null, false);
        checkMetadataChannels(2, true);

        // Restore favorite
        expectLastChangeOnSetAVTransportURI(true, false, 0);
        handler.handleCommand(favoriteSelectChannelUID, StringType.valueOf("Test_Favorite"));

        checkInternalState(null, null, false, true, false, false);
        checkControlChannel(PlayPauseType.PLAY);
        checkSetURI(0, null, false);
        checkMetadataChannels(0, true);

        // Delete favorite
        handler.handleCommand(favoriteSelectChannelUID, StringType.valueOf("Test_Favorite"));
        handler.handleCommand(favoriteActionChannelUID, StringType.valueOf("DELETE"));

        // Check called after deleting favorite
        verify(handler, times(3)).updateFavoritesList();

        // Check that FAVORITE_SELECT channel option list is empty again
        commandOptionListCaptor = ArgumentCaptor.forClass(List.class);
        verify(handler, atLeastOnce()).updateCommandDescription(eq(thing.getChannel(FAVORITE_SELECT).getUID()),
                commandOptionListCaptor.capture());
        assertThat(commandOptionListCaptor.getValue().size(), is(0));
    }

    private void expectLastChangeOnStop(boolean respond) {
        String value = LAST_CHANGE_HEADER + TRANSPORT_STATE + "STOPPED" + CLOSE + LAST_CHANGE_FOOTER;
        doAnswer(invocation -> {
            if (respond) {
                handler.onValueReceived("LastChange", value, "AVTransport");
            }
            return Collections.emptyMap();
        }).when(upnpIOService).invokeAction(eq(handler), eq("AVTransport"), eq("Stop"), anyMap());
    }

    private void expectLastChangeOnPlay(boolean respond) {
        String value = LAST_CHANGE_HEADER + TRANSPORT_STATE + "PLAYING" + CLOSE + LAST_CHANGE_FOOTER;
        doAnswer(invocation -> {
            if (respond) {
                handler.onValueReceived("LastChange", value, "AVTransport");
            }
            return Collections.emptyMap();
        }).when(upnpIOService).invokeAction(eq(handler), eq("AVTransport"), eq("Play"), anyMap());
    }

    private void expectLastChangeOnPause(boolean respond) {
        String value = LAST_CHANGE_HEADER + TRANSPORT_STATE + "PAUSED_PLAYBACK" + CLOSE + LAST_CHANGE_FOOTER;
        doAnswer(invocation -> {
            if (respond) {
                handler.onValueReceived("LastChange", value, "AVTransport");
            }
            return Collections.emptyMap();
        }).when(upnpIOService).invokeAction(eq(handler), eq("AVTransport"), eq("Pause"), anyMap());
    }

    private void expectLastChangeOnSetVolume(boolean respond, long volume) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", "0");
        inputs.put("Channel", UPNP_MASTER);
        inputs.put("DesiredVolume", String.valueOf(volume));
        doAnswer(invocation -> {
            if (respond) {
                handler.onValueReceived(UPNP_MASTER + "Volume", String.valueOf(volume), "RenderingControl");
            }
            return Collections.emptyMap();
        }).when(upnpIOService).invokeAction(eq(handler), eq("RenderingControl"), eq("SetVolume"), eq(inputs));
    }

    private void expectLastChangeOnGetPositionInfo(boolean respond, String seekTarget) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", "0");
        doAnswer(invocation -> {
            if (respond) {
                handler.onValueReceived("RelTime", seekTarget, "AVTransport");
            }
            return Collections.emptyMap();
        }).when(upnpIOService).invokeAction(eq(handler), eq("AVTransport"), eq("GetPositionInfo"), eq(inputs));
    }

    private void expectLastChangeOnSetAVTransportURI(boolean respond, int mediaId) {
        expectLastChangeOnSetAVTransportURI(respond, true, mediaId);
    }

    private void expectLastChangeOnSetAVTransportURI(boolean respond, boolean withMetadata, int mediaId) {
        String uri = upnpEntryQueue.get(mediaId).getRes();
        String metadata = UpnpXMLParser.compileMetadataString(requireNonNull(upnpEntryQueue.get(mediaId)));
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", "0");
        inputs.put("CurrentURI", uri);
        inputs.put("CurrentURIMetaData", withMetadata ? metadata : "");
        String value = LAST_CHANGE_HEADER + AV_TRANSPORT_URI + uri + CLOSE + AV_TRANSPORT_URI_METADATA + metadata
                + CLOSE + CURRENT_TRACK_URI + uri + CLOSE + CURRENT_TRACK_METADATA + metadata + CLOSE
                + LAST_CHANGE_FOOTER;
        doAnswer(invocation -> {
            if (respond) {
                handler.onValueReceived("LastChange", value, "AVTransport");
            }
            return Collections.emptyMap();
        }).when(upnpIOService).invokeAction(eq(handler), eq("AVTransport"), eq("SetAVTransportURI"), eq(inputs));
    }

    private void checkInternalState(@Nullable Integer currentEntry, @Nullable Integer nextEntry, boolean playerStopped,
            boolean playing, boolean registeredQueue, boolean playingQueue) {
        if (currentEntry == null) {
            assertNull(handler.currentEntry);
        } else {
            assertThat(handler.currentEntry, is(upnpEntryQueue.get(currentEntry)));
        }
        if (nextEntry == null) {
            assertNull(handler.nextEntry);
        } else {
            assertThat(handler.nextEntry, is(upnpEntryQueue.get(nextEntry)));
        }
        assertThat(handler.playerStopped, is(playerStopped));
        assertThat(handler.playing, is(playing));
        assertThat(handler.registeredQueue, is(registeredQueue));
        assertThat(handler.playingQueue, is(playingQueue));
    }

    private void checkControlChannel(Command command) {
        ArgumentCaptor<PlayPauseType> captor = ArgumentCaptor.forClass(PlayPauseType.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CONTROL).getUID()), captor.capture());
        assertThat(captor.getValue(), is(command));
    }

    private void checkSetURI(@Nullable Integer current, @Nullable Integer next) {
        checkSetURI(current, next, true);
    }

    private void checkSetURI(@Nullable Integer current, @Nullable Integer next, boolean withMetadata) {
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> metadataCaptor = ArgumentCaptor.forClass(String.class);
        if (current != null) {
            verify(handler, atLeastOnce()).setCurrentURI(uriCaptor.capture(), metadataCaptor.capture());
            assertThat(uriCaptor.getValue(), is(upnpEntryQueue.get(current).getRes()));
            if (withMetadata) {
                assertThat(metadataCaptor.getValue(),
                        is(UpnpXMLParser.compileMetadataString(requireNonNull(upnpEntryQueue.get(current)))));
            }
        }
        if (next != null) {
            verify(handler, atLeastOnce()).setNextURI(uriCaptor.capture(), metadataCaptor.capture());
            assertThat(uriCaptor.getValue(), is(upnpEntryQueue.get(next).getRes()));
            if (withMetadata) {
                assertThat(metadataCaptor.getValue(),
                        is(UpnpXMLParser.compileMetadataString(requireNonNull(upnpEntryQueue.get(next)))));
            }
        }
    }

    private void checkMetadataChannels(int mediaId) {
        checkMetadataChannels(mediaId, false);
    }

    private void checkMetadataChannels(int mediaId, boolean cleared) {
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);

        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(URI).getUID()), stateCaptor.capture());
        assertThat(stateCaptor.getValue(), is(StringType.valueOf(upnpEntryQueue.get(mediaId).getRes())));

        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(TITLE).getUID()), stateCaptor.capture());
        assertThat(stateCaptor.getValue(),
                is(cleared ? UnDefType.UNDEF : StringType.valueOf(upnpEntryQueue.get(mediaId).getTitle())));
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(ALBUM).getUID()), stateCaptor.capture());
        assertThat(stateCaptor.getValue(),
                is(cleared ? UnDefType.UNDEF : StringType.valueOf(upnpEntryQueue.get(mediaId).getAlbum())));
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(CREATOR).getUID()), stateCaptor.capture());
        assertThat(stateCaptor.getValue(),
                is(cleared ? UnDefType.UNDEF : StringType.valueOf(upnpEntryQueue.get(mediaId).getCreator())));
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(ARTIST).getUID()), stateCaptor.capture());
        assertThat(stateCaptor.getValue(),
                is(cleared ? UnDefType.UNDEF : StringType.valueOf(upnpEntryQueue.get(mediaId).getArtist())));
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(PUBLISHER).getUID()), stateCaptor.capture());
        assertThat(stateCaptor.getValue(),
                is(cleared ? UnDefType.UNDEF : StringType.valueOf(upnpEntryQueue.get(mediaId).getPublisher())));
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(GENRE).getUID()), stateCaptor.capture());
        assertThat(stateCaptor.getValue(),
                is(cleared ? UnDefType.UNDEF : StringType.valueOf(upnpEntryQueue.get(mediaId).getGenre())));
        verify(callback, atLeastOnce()).stateUpdated(eq(thing.getChannel(TRACK_NUMBER).getUID()),
                stateCaptor.capture());
        Integer originalTrackNumber = upnpEntryQueue.get(mediaId).getOriginalTrackNumber();
        if (originalTrackNumber != null) {
            assertThat(stateCaptor.getValue(), is(cleared ? UnDefType.UNDEF : new DecimalType(originalTrackNumber)));
            is(new DecimalType(originalTrackNumber));
        }
    }
}
