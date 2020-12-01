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
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.net.NetUtil;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebContext;
import org.openhab.binding.sony.internal.scalarweb.VersionUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebError;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebEvent;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.api.ActiveTerminal;
import org.openhab.binding.sony.internal.scalarweb.models.api.AudioInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.BivlInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.BroadcastFreq;
import org.openhab.binding.sony.internal.scalarweb.models.api.ContentCount_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.ContentCount_1_3;
import org.openhab.binding.sony.internal.scalarweb.models.api.ContentListRequest_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.ContentListRequest_1_4;
import org.openhab.binding.sony.internal.scalarweb.models.api.ContentListResult_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.ContentListResult_1_2;
import org.openhab.binding.sony.internal.scalarweb.models.api.ContentListResult_1_4;
import org.openhab.binding.sony.internal.scalarweb.models.api.ContentListResult_1_5;
import org.openhab.binding.sony.internal.scalarweb.models.api.Count;
import org.openhab.binding.sony.internal.scalarweb.models.api.CurrentExternalInputsStatus_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.CurrentExternalInputsStatus_1_1;
import org.openhab.binding.sony.internal.scalarweb.models.api.CurrentExternalTerminalsStatus_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.DabInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.DeleteContent;
import org.openhab.binding.sony.internal.scalarweb.models.api.DeleteProtection;
import org.openhab.binding.sony.internal.scalarweb.models.api.Duration;
import org.openhab.binding.sony.internal.scalarweb.models.api.Output;
import org.openhab.binding.sony.internal.scalarweb.models.api.ParentalInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.ParentalRatingSetting_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.PlayContent_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.PlayContent_1_2;
import org.openhab.binding.sony.internal.scalarweb.models.api.PlayingContentInfoRequest_1_2;
import org.openhab.binding.sony.internal.scalarweb.models.api.PlayingContentInfoResult_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.PlayingContentInfoResult_1_2;
import org.openhab.binding.sony.internal.scalarweb.models.api.PresetBroadcastStation;
import org.openhab.binding.sony.internal.scalarweb.models.api.ScanPlayingContent_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.Scheme;
import org.openhab.binding.sony.internal.scalarweb.models.api.SeekBroadcastStation_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.Source;
import org.openhab.binding.sony.internal.scalarweb.models.api.StateInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.SubtitleInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.TvContentVisibility;
import org.openhab.binding.sony.internal.scalarweb.models.api.VideoInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.Visibility;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the protocol handles the Av Content service
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
class ScalarWebAvContentProtocol<T extends ThingCallback<String>> extends AbstractScalarWebProtocol<T> {

    // Default values for devices that have only a single output
    private static final String MAINOUTPUT = "main"; // cannot be empty as it's used as a channel id
    private static final String MAINTITLE = "Main";

    // Constants used by this protocol
    private static final String BLUETOOTHSETTINGS = "bluetoothsetting";
    private static final String PLAYBACKSETTINGS = "playbackmode";
    private static final String SCHEMES = "schemes";
    private static final String SOURCES = "sources";

    // parental rating channel constants
    private static final String PARENTRATING = "pr_";
    private static final String PR_RATINGTYPEAGE = PARENTRATING + "ratingtypeage";
    private static final String PR_RATINGTYPESONY = PARENTRATING + "ratingtypesony";
    private static final String PR_RATINGCOUNTRY = PARENTRATING + "ratingcountry";
    private static final String PR_RATINGCUSTOMTYPETV = PARENTRATING + "ratingcustomtypetv";
    private static final String PR_RATINGCUSTOMTYPEMPAA = PARENTRATING + "ratingcustomtypempaa";
    private static final String PR_RATINGCUSTOMTYPECAENGLISH = PARENTRATING + "ratingcustomtypecaenglish";
    private static final String PR_RATINGCUSTOMTYPECAFRENCH = PARENTRATING + "ratingcustomtypecafrench";
    private static final String PR_UNRATEDLOCK = PARENTRATING + "unratedlock";

    // now playing channel constants
    private static final String PLAYING = "pl_";

    private static final String PL_ALBUMNAME = PLAYING + "albumname";
    private static final String PL_APPLICATIONNAME = PLAYING + "applicationname";
    private static final String PL_ARTIST = PLAYING + "artist";
    private static final String PL_AUDIOCHANNEL = PLAYING + "audiochannel";
    private static final String PL_AUDIOCODEC = PLAYING + "audiocodec";
    private static final String PL_AUDIOFREQUENCY = PLAYING + "audiofrequency";
    private static final String PL_BIVLASSETID = PLAYING + "bivlassetid";
    private static final String PL_BIVLPROVIDER = PLAYING + "bivlprovider";
    private static final String PL_BIVLSERVICEID = PLAYING + "bivlserviceid";
    private static final String PL_BROADCASTFREQ = PLAYING + "broadcastfreq";
    private static final String PL_BROADCASTFREQBAND = PLAYING + "broadcastfreqband";
    private static final String PL_CHANNELNAME = PLAYING + "channelname";
    private static final String PL_CHAPTERCOUNT = PLAYING + "chaptercount";
    private static final String PL_CHAPTERINDEX = PLAYING + "chapterindex";
    private static final String PL_CMD = PLAYING + "cmd";
    private static final String PL_CONTENTKIND = PLAYING + "contentkind";
    private static final String PL_DABCOMPONENTLABEL = PLAYING + "dabcomponentlabel";
    private static final String PL_DABDYNAMICLABEL = PLAYING + "dabdynamiclabel";
    private static final String PL_DABENSEMBLELABEL = PLAYING + "dabensemblelabel";
    private static final String PL_DABSERVICELABEL = PLAYING + "dabservicelabel";
    private static final String PL_DISPNUM = PLAYING + "dispnum";
    private static final String PL_DURATIONMSEC = PLAYING + "durationmsec";
    private static final String PL_DURATIONSEC = PLAYING + "durationsec";
    private static final String PL_FILENO = PLAYING + "fileno";
    private static final String PL_GENRE = PLAYING + "genre";
    private static final String PL_INDEX = PLAYING + "index";
    private static final String PL_IS3D = PLAYING + "is3d";
    private static final String PL_MEDIATYPE = PLAYING + "mediatype";
    private static final String PL_ORIGINALDISPNUM = PLAYING + "originaldispnum";
    private static final String PL_OUTPUT = PLAYING + "output";
    private static final String PL_PARENTINDEX = PLAYING + "parentindex";
    private static final String PL_PARENTURI = PLAYING + "parenturi";
    private static final String PL_PATH = PLAYING + "path";
    private static final String PL_PLAYLISTNAME = PLAYING + "playlistname";
    private static final String PL_PLAYSPEED = PLAYING + "playspeed";
    private static final String PL_PLAYSTEPSPEED = PLAYING + "playstepspeed";
    private static final String PL_PODCASTNAME = PLAYING + "podcastname";
    private static final String PL_POSITIONMSEC = PLAYING + "positionmsec";
    private static final String PL_POSITIONSEC = PLAYING + "positionsec";
    private static final String PL_PRESET = PLAYING + "presetid";
    private static final String PL_PROGRAMNUM = PLAYING + "programnum";
    private static final String PL_PROGRAMTITLE = PLAYING + "programtitle";
    private static final String PL_REPEATTYPE = PLAYING + "repeattype";
    private static final String PL_SERVICE = PLAYING + "service";
    private static final String PL_SOURCE = PLAYING + "source";
    private static final String PL_SOURCELABEL = PLAYING + "sourcelabel";
    private static final String PL_STARTDATETIME = PLAYING + "startdatetime";
    private static final String PL_STATE = PLAYING + "state";
    private static final String PL_STATESUPPLEMENT = PLAYING + "statesupplement";
    private static final String PL_SUBTITLEINDEX = PLAYING + "subtitleindex";
    private static final String PL_TITLE = PLAYING + "title";
    private static final String PL_TOTALCOUNT = PLAYING + "totalcount";
    private static final String PL_TRIPLETSTR = PLAYING + "tripletstr";
    private static final String PL_URI = PLAYING + "uri";
    private static final String PL_VIDEOCODEC = PLAYING + "videocodec";

    // virtual presets channel constants
    private static final String PRESETS = "ps_";
    private static final String PS_CHANNEL = PRESETS + "channel";

    // input channel constants
    private static final String INPUT = "in_";
    private static final String IN_URI = INPUT + "uri";
    private static final String IN_TITLE = INPUT + "title";
    private static final String IN_CONNECTION = INPUT + "connection";
    private static final String IN_LABEL = INPUT + "label";
    private static final String IN_ICON = INPUT + "icon";
    private static final String IN_STATUS = INPUT + "status";

    // terminal channel constants
    private static final String TERM = "tm_";
    private static final String TERM_SOURCE = TERM + "source";
    private static final String TERM_URI = TERM + "uri";
    private static final String TERM_TITLE = TERM + "title";
    private static final String TERM_CONNECTION = TERM + "connection";
    private static final String TERM_LABEL = TERM + "label";
    private static final String TERM_ICON = TERM + "icon";
    private static final String TERM_ACTIVE = TERM + "active";

    // content channel constants
    private static final String CONTENT = "cn_";
    private static final String CN_ALBUMNAME = CONTENT + "albumname";
    private static final String CN_APPLICATIONNAME = CONTENT + "applicationname";
    private static final String CN_ARTIST = CONTENT + "artist";
    private static final String CN_AUDIOCHANNEL = CONTENT + "audiochannel";
    private static final String CN_AUDIOCODEC = CONTENT + "audiocodec";
    private static final String CN_AUDIOFREQUENCY = CONTENT + "audiofrequency";
    private static final String CN_BROADCASTFREQ = CONTENT + "broadcastfreq";
    private static final String CN_BIVLSERVICEID = CONTENT + "bivlserviceid";
    private static final String CN_BIVLASSETID = CONTENT + "bivleassetid";
    private static final String CN_BIVLPROVIDER = CONTENT + "bivlprovider";
    private static final String CN_BROADCASTFREQBAND = CONTENT + "broadcastfreqband";
    private static final String CN_CHANNELNAME = CONTENT + "channelname";
    private static final String CN_CHANNELSURFINGVISIBILITY = CONTENT + "channelsurfingvisibility";
    private static final String CN_CHAPTERCOUNT = CONTENT + "chaptercount";
    private static final String CN_CHAPTERINDEX = CONTENT + "chapterindex";
    private static final String CN_CHILDCOUNT = CONTENT + "childcount";
    private static final String CN_CLIPCOUNT = CONTENT + "clipcount";
    private static final String CN_CMD = CONTENT + "cmd";
    private static final String CN_CONTENTKIND = CONTENT + "contentkind";
    private static final String CN_CONTENTTYPE = CONTENT + "contenttype";
    private static final String CN_CREATEDTIME = CONTENT + "createdtime";
    private static final String CN_DABCOMPONENTLABEL = CONTENT + "dabcomponentlabel";
    private static final String CN_DABDYNAMICLABEL = CONTENT + "dabdynamiclabel";
    private static final String CN_DABENSEMBLELABEL = CONTENT + "dabensemblelabel";
    private static final String CN_DABSERVICELABEL = CONTENT + "dabservicelabel";
    private static final String CN_DESCRIPTION = CONTENT + "description";
    private static final String CN_DIRECTREMOTENUM = CONTENT + "directremotenum";
    private static final String CN_DISPNUM = CONTENT + "dispnum";
    private static final String CN_DURATIONMSEC = CONTENT + "durationmsec";
    private static final String CN_DURATIONSEC = CONTENT + "durationsec";
    private static final String CN_EPGVISIBILITY = CONTENT + "epgvisibility";
    private static final String CN_EVENTID = CONTENT + "eventid";
    private static final String CN_FILENO = CONTENT + "fileno";
    private static final String CN_FILESIZEBYTE = CONTENT + "filesizebyte";
    private static final String CN_FOLDERNO = CONTENT + "folderno";
    private static final String CN_GENRE = CONTENT + "genre";
    private static final String CN_GLOBALPLAYBACKCOUNT = CONTENT + "globalplaybackcount";
    private static final String CN_HASRESUME = CONTENT + "hasresume";
    private static final String CN_INDEX = CONTENT + "idx";
    private static final String CN_IS3D = CONTENT + "is3d";
    private static final String CN_IS4K = CONTENT + "is4k";
    private static final String CN_ISALREADYPLAYED = CONTENT + "isalreadyplayed";
    private static final String CN_ISAUTODELETE = CONTENT + "isautodelete";
    private static final String CN_ISBROWSABLE = CONTENT + "isbrowsable";
    private static final String CN_ISNEW = CONTENT + "isnew";
    private static final String CN_ISPLAYABLE = CONTENT + "isplayable";
    private static final String CN_ISPLAYLIST = CONTENT + "isplaylist";
    private static final String CN_ISPROTECTED = CONTENT + "isprotected";
    private static final String CN_ISSOUNDPHOTO = CONTENT + "issoundphoto";
    private static final String CN_MEDIATYPE = CONTENT + "mediatype";
    private static final String CN_ORIGINALDISPNUM = CONTENT + "originaldispnum";
    private static final String CN_OUTPUT = CONTENT + "output";
    private static final String CN_PARENTALCOUNTRY = CONTENT + "parentalcountry";
    private static final String CN_PARENTALRATING = CONTENT + "parentalrating";
    private static final String CN_PARENTALSYSTEM = CONTENT + "parentalsystem";
    private static final String CN_PARENTINDEX = CONTENT + "parentindex";
    private static final String CN_PARENTURI = CONTENT + "parenturi";
    private static final String CN_PATH = CONTENT + "path";
    private static final String CN_PLAYLISTNAME = CONTENT + "playlistname";
    private static final String CN_PODCASTNAME = CONTENT + "podcastname";
    private static final String CN_PRODUCTID = CONTENT + "productid";
    private static final String CN_PROGRAMMEDIATYPE = CONTENT + "programmediatype";
    private static final String CN_PROGRAMNUM = CONTENT + "programnum";
    private static final String CN_PROGRAMSERVICETYPE = CONTENT + "programservicetype";
    private static final String CN_PROGRAMTITLE = CONTENT + "programtitle";
    private static final String CN_REMOTEPLAYTYPE = CONTENT + "remoteplaytype";
    private static final String CN_REPEATTYPE = CONTENT + "repeattype";
    private static final String CN_SERVICE = CONTENT + "service";
    private static final String CN_SIZEMB = CONTENT + "sizemb";
    private static final String CN_SOURCE = CONTENT + "source";
    private static final String CN_SOURCELABEL = CONTENT + "sourcelabel";
    private static final String CN_STARTDATETIME = CONTENT + "startdatetime";
    private static final String CN_STATE = CONTENT + "state";
    private static final String CN_STATESUPPLEMENT = CONTENT + "statesupplement";
    private static final String CN_STORAGEURI = CONTENT + "storageuri";
    private static final String CN_SUBTITLELANGUAGE = CONTENT + "subtitlelanguage";
    private static final String CN_SUBTITLETITLE = CONTENT + "subtitletitle";
    private static final String CN_SYNCCONTENTPRIORITY = CONTENT + "synccontentpriority";
    private static final String CN_TITLE = CONTENT + "title";
    private static final String CN_TOTALCOUNT = CONTENT + "totalcount";
    private static final String CN_TRIPLETSTR = CONTENT + "tripletstr";
    private static final String CN_URI = CONTENT + "uri";
    private static final String CN_USERCONTENTFLAG = CONTENT + "usercontentflag";
    private static final String CN_VIDEOCODEC = CONTENT + "videocodec";
    private static final String CN_VISIBILITY = CONTENT + "visibility";

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebAvContentProtocol.class);

    /** The cached schemes */
    private final AtomicReference<Set<Scheme>> stateSchemes = new AtomicReference<>(new HashSet<>());

    /** The cached sources by scheme */
    private final ConcurrentMap<String, Set<Source>> stateSources = new ConcurrentHashMap<>();

    /** The cached terminals */
    private final AtomicReference<List<CurrentExternalTerminalsStatus_1_0>> stateTerminals = new AtomicReference<>(
            new ArrayList<>());

    /** The cached inputs */
    private final AtomicReference<@Nullable ScalarWebResult> stateInputs = new AtomicReference<>(null);

    /** The cached content state (ie what our current url, index is) */
    private final AtomicReference<ContentState> stateContent = new AtomicReference<>(new ContentState());

    /** The cached now playing state */
    private final ConcurrentMap<String, PlayingState> statePlaying = new ConcurrentHashMap<>();

    /** Maximum amount of content to pull in one request */
    private static final int MAX_CT = 150;

    /** The notifications that are enabled */
    private final NotificationHelper notificationHelper;

    /**
     * Function interface to process a content list result
     */
    @NonNullByDefault
    private interface ContentListCallback {
        /**
         * Called to process a content list result
         * 
         * @return true if processed, false otherwise
         */
        boolean processContentListResult(ContentListResult_1_0 result);
    }

    /**
     * Instantiates a new scalar web av content protocol.
     *
     * @param factory the non-null factory
     * @param context the non-null context
     * @param service the non-null service
     * @param callback the non-null callback
     */
    ScalarWebAvContentProtocol(final ScalarWebProtocolFactory<T> factory, final ScalarWebContext context,
            final ScalarWebService service, final T callback) {
        super(factory, context, service, callback);
        notificationHelper = new NotificationHelper(enableNotifications(ScalarWebEvent.NOTIFYPLAYINGCONTENTINFO,
                /** ScalarWebEvent.NOTIFYAVAILABLEPLAYBACKFUNCTION, */
                ScalarWebEvent.NOTIFYEXTERNALTERMINALSTATUS));
    }

    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors(final boolean dynamicOnly) {
        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        // Add the predefined channels (dynamic)
        if (service.hasMethod(ScalarWebMethod.GETCONTENTLIST)) {
            addPresetChannelDescriptors(descriptors);
        }

        if (!dynamicOnly) {
            if (service.hasMethod(ScalarWebMethod.GETCONTENTLIST)) {
                addContentListDescriptors(descriptors);
            }

            if (service.hasMethod(ScalarWebMethod.GETSCHEMELIST)) {
                descriptors.add(createDescriptor(createChannel(SCHEMES), "String", "scalarwebavcontrolschemes"));
            }

            if (service.hasMethod(ScalarWebMethod.GETSOURCELIST)) {
                descriptors.add(createDescriptor(createChannel(SOURCES), "String", "scalarwebavcontrolsources"));
            }

            // don't check has here since we create a dummy terminal for single output devices
            addTerminalStatusDescriptors(descriptors);

            if (service.hasMethod(ScalarWebMethod.GETCURRENTEXTERNALINPUTSSTATUS)) {
                addInputStatusDescriptors(descriptors);
            }

            if (service.hasMethod(ScalarWebMethod.GETPARENTALRATINGSETTINGS)) {
                addParentalRatingDescriptors(descriptors);
            }

            // Note: must come AFTER terminal descriptors since we use the IDs generated from it
            if (service.hasMethod(ScalarWebMethod.GETPLAYINGCONTENTINFO)) {
                addPlayingContentDescriptors(descriptors);
            }

            if (service.hasMethod(ScalarWebMethod.GETBLUETOOTHSETTINGS)) {
                addGeneralSettingsDescriptor(descriptors, ScalarWebMethod.GETBLUETOOTHSETTINGS, BLUETOOTHSETTINGS,
                        "Bluetooth Setting");
            }

            if (service.hasMethod(ScalarWebMethod.GETPLAYBACKMODESETTINGS)) {
                addGeneralSettingsDescriptor(descriptors, ScalarWebMethod.GETPLAYBACKMODESETTINGS, PLAYBACKSETTINGS,
                        "Playback Setting");
            }
        }

        // update the terminal sources
        updateTermSource();

        return descriptors;
    }

    /**
     * Adds the content list descriptors
     * 
     * @param descriptors the non-null, possibly empty list of descriptors
     */
    private void addContentListDescriptors(final List<ScalarWebChannelDescriptor> descriptors) {
        Objects.requireNonNull(descriptors, "descriptors cannot be null");

        // The control (parent uri/uri/index) and virtual channels (childcount/selected)
        descriptors.add(createDescriptor(createChannel(CN_PARENTURI), "String", "scalarwebavcontrolcontentparenturi"));
        descriptors.add(createDescriptor(createChannel(CN_URI), "String", "scalarwebavcontrolcontenturi"));
        descriptors.add(createDescriptor(createChannel(CN_INDEX), "Number", "scalarwebavcontrolcontentidx"));
        descriptors
                .add(createDescriptor(createChannel(CN_CHILDCOUNT), "Number", "scalarwebavcontrolcontentchildcount"));

        // final Map<String, String> outputs = getTerminalOutputs(getTerminalStatuses());
        //
        // for (Entry<String, String> entry : outputs.entrySet()) {
        // descriptors.add(createDescriptor(createChannel(CN_SELECTED, entry.getKey()), "Switch",
        // "scalarwebavcontroltermstatusselected", "Content Play on Output " + entry.getValue(), null));
        // }
        descriptors.add(createDescriptor(createChannel(CN_CMD), "String", "scalarwebavcontrolcontentcmd"));

        final String version = getVersion(ScalarWebMethod.GETCONTENTLIST);
        if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1)) {
            descriptors.add(
                    createDescriptor(createChannel(CN_CHANNELNAME), "String", "scalarwebavcontrolcontentchannelname"));
            descriptors.add(createDescriptor(createChannel(CN_DIRECTREMOTENUM), "Number",
                    "scalarwebavcontrolcontentdirectremotenum"));
            descriptors.add(createDescriptor(createChannel(CN_DISPNUM), "String", "scalarwebavcontrolcontentdispnum"));
            descriptors.add(createDescriptor(createChannel(CN_DURATIONSEC), "Number:Time",
                    "scalarwebavcontrolcontentdurationsec"));
            descriptors.add(createDescriptor(createChannel(CN_FILESIZEBYTE), "Number:DataAmount",
                    "scalarwebavcontrolcontentfilesizebyte"));
            descriptors.add(createDescriptor(createChannel(CN_ISALREADYPLAYED), "String",
                    "scalarwebavcontrolcontentisalreadyplayed"));
            descriptors.add(
                    createDescriptor(createChannel(CN_ISPROTECTED), "String", "scalarwebavcontrolcontentisprotected"));
            descriptors.add(createDescriptor(createChannel(CN_ORIGINALDISPNUM), "String",
                    "scalarwebavcontrolcontentoriginaldispnum"));
            descriptors.add(createDescriptor(createChannel(CN_PROGRAMMEDIATYPE), "String",
                    "scalarwebavcontrolcontentprogrammediatype"));
            descriptors.add(
                    createDescriptor(createChannel(CN_PROGRAMNUM), "Number", "scalarwebavcontrolcontentprogramnum"));
            descriptors.add(createDescriptor(createChannel(CN_STARTDATETIME), "String",
                    "scalarwebavcontrolcontentstartdatetime"));
            descriptors.add(createDescriptor(createChannel(CN_TITLE), "String", "scalarwebavcontrolcontenttitle"));
            descriptors.add(
                    createDescriptor(createChannel(CN_TRIPLETSTR), "String", "scalarwebavcontrolcontenttripletstr"));
        } else if (VersionUtilities.equals(version, ScalarWebMethod.V1_2, ScalarWebMethod.V1_3)) {
            descriptors.add(createDescriptor(createChannel(CN_AUDIOCHANNEL), "String",
                    "scalarwebavcontrolcontentaudiochannel"));
            descriptors.add(
                    createDescriptor(createChannel(CN_AUDIOCODEC), "String", "scalarwebavcontrolcontentaudiocodec"));
            descriptors.add(createDescriptor(createChannel(CN_AUDIOFREQUENCY), "String",
                    "scalarwebavcontrolcontentaudiofrequency"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CHANNELNAME), "String", "scalarwebavcontrolcontentchannelname"));
            descriptors.add(createDescriptor(createChannel(CN_CHANNELSURFINGVISIBILITY), "String",
                    "scalarwebavcontrolcontentchannelsurfingvisibility"));
            descriptors.add(createDescriptor(createChannel(CN_CHAPTERCOUNT), "Number",
                    "scalarwebavcontrolcontentchaptercount"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CONTENTTYPE), "String", "scalarwebavcontrolcontentcontenttype"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CREATEDTIME), "String", "scalarwebavcontrolcontentcreatedtime"));
            descriptors.add(createDescriptor(createChannel(CN_DIRECTREMOTENUM), "Number",
                    "scalarwebavcontrolcontentdirectremotenum"));
            descriptors.add(createDescriptor(createChannel(CN_DISPNUM), "String", "scalarwebavcontrolcontentdispnum"));
            descriptors.add(createDescriptor(createChannel(CN_DURATIONSEC), "Number:Time",
                    "scalarwebavcontrolcontentdurationsec"));
            descriptors.add(createDescriptor(createChannel(CN_EPGVISIBILITY), "String",
                    "scalarwebavcontrolcontentepgvisibility"));
            descriptors.add(createDescriptor(createChannel(CN_FILESIZEBYTE), "Number:DataAmount",
                    "scalarwebavcontrolcontentfilesizebyte"));
            descriptors.add(createDescriptor(createChannel(CN_ISALREADYPLAYED), "String",
                    "scalarwebavcontrolcontentisalreadyplayed"));
            descriptors.add(
                    createDescriptor(createChannel(CN_ISPROTECTED), "String", "scalarwebavcontrolcontentisprotected"));
            descriptors.add(createDescriptor(createChannel(CN_ORIGINALDISPNUM), "String",
                    "scalarwebavcontrolcontentoriginaldispnum"));
            descriptors.add(createDescriptor(createChannel(CN_PARENTALCOUNTRY), "String",
                    "scalarwebavcontrolcontentparentalcountry"));
            descriptors.add(createDescriptor(createChannel(CN_PARENTALRATING), "String",
                    "scalarwebavcontrolcontentparentalrating"));
            descriptors.add(createDescriptor(createChannel(CN_PARENTALSYSTEM), "String",
                    "scalarwebavcontrolcontentparentalsystem"));
            descriptors
                    .add(createDescriptor(createChannel(CN_PRODUCTID), "String", "scalarwebavcontrolcontentproductid"));
            descriptors.add(createDescriptor(createChannel(CN_PROGRAMMEDIATYPE), "String",
                    "scalarwebavcontrolcontentprogrammediatype"));
            descriptors.add(
                    createDescriptor(createChannel(CN_PROGRAMNUM), "Number", "scalarwebavcontrolcontentprogramnum"));
            descriptors.add(
                    createDescriptor(createChannel(CN_SIZEMB), "Number:DataAmount", "scalarwebavcontrolcontentsizemb"));
            descriptors.add(createDescriptor(createChannel(CN_STARTDATETIME), "String",
                    "scalarwebavcontrolcontentstartdatetime"));
            descriptors.add(
                    createDescriptor(createChannel(CN_STORAGEURI), "String", "scalarwebavcontrolcontentstorageuri"));
            descriptors.add(createDescriptor(createChannel(CN_SUBTITLELANGUAGE), "String",
                    "scalarwebavcontrolcontentsubtitlelanguage"));
            descriptors.add(createDescriptor(createChannel(CN_SUBTITLETITLE), "String",
                    "scalarwebavcontrolcontentsubtitletitle"));
            descriptors.add(createDescriptor(createChannel(CN_TITLE), "String", "scalarwebavcontrolcontenttitle"));
            descriptors.add(
                    createDescriptor(createChannel(CN_TRIPLETSTR), "String", "scalarwebavcontrolcontenttripletstr"));
            descriptors.add(createDescriptor(createChannel(CN_USERCONTENTFLAG), "Switch",
                    "scalarwebavcontrolcontentusercontentflag"));
            descriptors.add(
                    createDescriptor(createChannel(CN_VIDEOCODEC), "String", "scalarwebavcontrolcontentvideocodec"));
            descriptors.add(
                    createDescriptor(createChannel(CN_VISIBILITY), "String", "scalarwebavcontrolcontentvisibility"));

        } else if (VersionUtilities.equals(version, ScalarWebMethod.V1_4)) {
            descriptors
                    .add(createDescriptor(createChannel(CN_ALBUMNAME), "String", "scalarwebavcontrolcontentalbumname"));
            descriptors.add(createDescriptor(createChannel(CN_ARTIST), "String", "scalarwebavcontrolcontentartist"));
            descriptors.add(createDescriptor(createChannel(CN_AUDIOCHANNEL), "String",
                    "scalarwebavcontrolcontentaudiochannel"));
            descriptors.add(
                    createDescriptor(createChannel(CN_AUDIOCODEC), "String", "scalarwebavcontrolcontentaudiocodec"));
            descriptors.add(createDescriptor(createChannel(CN_AUDIOFREQUENCY), "String",
                    "scalarwebavcontrolcontentaudiofrequency"));
            descriptors.add(createDescriptor(createChannel(CN_BROADCASTFREQ), "Number:Frequency",
                    "scalarwebavcontrolcontentbroadcastfreq"));
            descriptors.add(createDescriptor(createChannel(CN_BROADCASTFREQBAND), "String",
                    "scalarwebavcontrolcontentbroadcastband"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CHANNELNAME), "String", "scalarwebavcontrolcontentchannelname"));
            descriptors.add(createDescriptor(createChannel(CN_CHANNELSURFINGVISIBILITY), "String",
                    "scalarwebavcontrolcontentchannelsurfingvisibility"));
            descriptors.add(createDescriptor(createChannel(CN_CHAPTERCOUNT), "Number",
                    "scalarwebavcontrolcontentchaptercount"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CONTENTKIND), "String", "scalarwebavcontrolcontentcontentkind"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CONTENTTYPE), "String", "scalarwebavcontrolcontentcontenttype"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CREATEDTIME), "String", "scalarwebavcontrolcontentcreatedtime"));
            descriptors.add(createDescriptor(createChannel(CN_DIRECTREMOTENUM), "Number",
                    "scalarwebavcontrolcontentdirectremotenum"));
            descriptors.add(createDescriptor(createChannel(CN_DISPNUM), "String", "scalarwebavcontrolcontentdispnum"));
            descriptors.add(createDescriptor(createChannel(CN_DURATIONMSEC), "Number:Time",
                    "scalarwebavcontrolcontentdurationmsec"));
            descriptors.add(createDescriptor(createChannel(CN_EPGVISIBILITY), "String",
                    "scalarwebavcontrolcontentepgvisibility"));
            descriptors.add(createDescriptor(createChannel(CN_FILENO), "String", "scalarwebavcontrolcontentfileno"));
            descriptors.add(createDescriptor(createChannel(CN_FILESIZEBYTE), "Number:DataAmount",
                    "scalarwebavcontrolcontentfilesizebyte"));
            descriptors
                    .add(createDescriptor(createChannel(CN_FOLDERNO), "String", "scalarwebavcontrolcontentfolderno"));
            descriptors.add(createDescriptor(createChannel(CN_GENRE), "String", "scalarwebavcontrolcontentgenre"));
            descriptors.add(createDescriptor(createChannel(CN_IS3D), "String", "scalarwebavcontrolcontentis3d"));
            descriptors.add(createDescriptor(createChannel(CN_ISALREADYPLAYED), "String",
                    "scalarwebavcontrolcontentisalreadyplayed"));
            descriptors.add(
                    createDescriptor(createChannel(CN_ISBROWSABLE), "String", "scalarwebavcontrolcontentisbrowsable"));
            descriptors.add(
                    createDescriptor(createChannel(CN_ISPLAYABLE), "String", "scalarwebavcontrolcontentisplayable"));
            descriptors.add(
                    createDescriptor(createChannel(CN_ISPROTECTED), "String", "scalarwebavcontrolcontentisprotected"));
            descriptors.add(createDescriptor(createChannel(CN_ORIGINALDISPNUM), "String",
                    "scalarwebavcontrolcontentoriginaldispnum"));
            descriptors.add(createDescriptor(createChannel(CN_PARENTALCOUNTRY), "String",
                    "scalarwebavcontrolcontentparentalcountry"));
            descriptors.add(createDescriptor(createChannel(CN_PARENTALRATING), "String",
                    "scalarwebavcontrolcontentparentalrating"));
            descriptors.add(createDescriptor(createChannel(CN_PARENTALSYSTEM), "String",
                    "scalarwebavcontrolcontentparentalsystem"));
            descriptors.add(
                    createDescriptor(createChannel(CN_PARENTINDEX), "Number", "scalarwebavcontrolcontentparentindex"));
            descriptors.add(createDescriptor(createChannel(CN_PATH), "String", "scalarwebavcontrolcontentpath"));
            descriptors.add(createDescriptor(createChannel(CN_PLAYLISTNAME), "String",
                    "scalarwebavcontrolcontentplaylistname"));
            descriptors.add(
                    createDescriptor(createChannel(CN_PODCASTNAME), "String", "scalarwebavcontrolcontentpodcastname"));
            descriptors
                    .add(createDescriptor(createChannel(CN_PRODUCTID), "String", "scalarwebavcontrolcontentproductid"));
            descriptors.add(createDescriptor(createChannel(CN_PROGRAMMEDIATYPE), "String",
                    "scalarwebavcontrolcontentprogrammediatype"));
            descriptors.add(
                    createDescriptor(createChannel(CN_PROGRAMNUM), "Number", "scalarwebavcontrolcontentprogramnum"));
            descriptors.add(createDescriptor(createChannel(CN_REMOTEPLAYTYPE), "String",
                    "scalarwebavcontrolcontentremoteplaytype"));
            descriptors.add(
                    createDescriptor(createChannel(CN_SIZEMB), "Number:DataAmount", "scalarwebavcontrolcontentsizemb"));
            descriptors.add(createDescriptor(createChannel(CN_STARTDATETIME), "String",
                    "scalarwebavcontrolcontentstartdatetime"));
            descriptors.add(
                    createDescriptor(createChannel(CN_STORAGEURI), "String", "scalarwebavcontrolcontentstorageuri"));
            descriptors.add(createDescriptor(createChannel(CN_SUBTITLELANGUAGE), "String",
                    "scalarwebavcontrolcontentsubtitlelanguage"));
            descriptors.add(createDescriptor(createChannel(CN_SUBTITLETITLE), "String",
                    "scalarwebavcontrolcontentsubtitletitle"));
            descriptors.add(createDescriptor(createChannel(CN_TITLE), "String", "scalarwebavcontrolcontenttitle"));
            descriptors.add(
                    createDescriptor(createChannel(CN_TRIPLETSTR), "String", "scalarwebavcontrolcontenttripletstr"));
            descriptors.add(createDescriptor(createChannel(CN_USERCONTENTFLAG), "Switch",
                    "scalarwebavcontrolcontentusercontentflag"));
            descriptors.add(
                    createDescriptor(createChannel(CN_VIDEOCODEC), "String", "scalarwebavcontrolcontentvideocodec"));
            descriptors.add(
                    createDescriptor(createChannel(CN_VISIBILITY), "String", "scalarwebavcontrolcontentvisibility"));
        } else if (VersionUtilities.equals(version, ScalarWebMethod.V1_5)) {
            descriptors
                    .add(createDescriptor(createChannel(CN_ALBUMNAME), "String", "scalarwebavcontrolcontentalbumname"));
            descriptors.add(createDescriptor(createChannel(CN_APPLICATIONNAME), "String",
                    "scalarwebavcontrolplapplicationname"));
            descriptors.add(createDescriptor(createChannel(CN_ARTIST), "String", "scalarwebavcontrolcontentartist"));
            descriptors.add(createDescriptor(createChannel(CN_AUDIOCHANNEL), "String",
                    "scalarwebavcontrolcontentaudiochannel"));
            descriptors.add(
                    createDescriptor(createChannel(CN_AUDIOCODEC), "String", "scalarwebavcontrolcontentaudiocodec"));
            descriptors.add(createDescriptor(createChannel(CN_AUDIOFREQUENCY), "String",
                    "scalarwebavcontrolcontentaudiofrequency"));
            descriptors.add(createDescriptor(createChannel(CN_BIVLSERVICEID), "String",
                    "scalarwebavcontrolcontentbivlserviceid"));
            descriptors.add(
                    createDescriptor(createChannel(CN_BIVLASSETID), "String", "scalarwebavcontrolcontentbivlassetid"));
            descriptors.add(createDescriptor(createChannel(CN_BIVLPROVIDER), "String",
                    "scalarwebavcontrolcontentbivlprovider"));
            descriptors.add(createDescriptor(createChannel(CN_BROADCASTFREQ), "Number:Frequency",
                    "scalarwebavcontrolcontentbroadcastfreq"));
            descriptors.add(createDescriptor(createChannel(CN_BROADCASTFREQBAND), "String",
                    "scalarwebavcontrolcontentbroadcastband"));
            // descriptors.add(createDescriptor(createChannel(CN_BROADCASTGENREINFO), "String",
            // "scalarwebavcontrolcontextbroadcastGenreInfo"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CHANNELNAME), "String", "scalarwebavcontrolcontentchannelname"));
            descriptors.add(createDescriptor(createChannel(CN_CHANNELSURFINGVISIBILITY), "String",
                    "scalarwebavcontrolcontentchannelsurfingvisibility"));
            descriptors.add(createDescriptor(createChannel(CN_CHAPTERCOUNT), "Number",
                    "scalarwebavcontrolcontentchaptercount"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CHAPTERINDEX), "Number", "scalarwebavcontrolplchapterindex"));
            descriptors
                    .add(createDescriptor(createChannel(CN_CLIPCOUNT), "Number", "scalarwebavcontrolcontentclipcount"));
            // descriptors.add(createDescriptor(createChannel(CN_CONTENT), "String",
            // "scalarwebavcontrolcontextcontent"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CONTENTKIND), "String", "scalarwebavcontrolcontentcontentkind"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CONTENTTYPE), "String", "scalarwebavcontrolcontentcontenttype"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CREATEDTIME), "String", "scalarwebavcontrolcontentcreatedtime"));
            descriptors.add(
                    createDescriptor(createChannel(CN_CREATEDTIME), "String", "scalarwebavcontrolcontentcreatedtime"));
            descriptors.add(createDescriptor(createChannel(CN_DABCOMPONENTLABEL), "String",
                    "scalarwebavcontrolcontentdabcomponentlabel"));
            descriptors.add(createDescriptor(createChannel(CN_DABDYNAMICLABEL), "String",
                    "scalarwebavcontrolcontentdabdynamiclabel"));
            descriptors.add(createDescriptor(createChannel(CN_DABENSEMBLELABEL), "String",
                    "scalarwebavcontrolcontentdabensemblelabel"));
            descriptors.add(createDescriptor(createChannel(CN_DABSERVICELABEL), "String",
                    "scalarwebavcontrolcontentdabservicelabel"));
            // descriptors.add(createDescriptor(createChannel(CN_DATAINFO), "String",
            // "scalarwebavcontrolcontextdataInfo"));
            // // todo
            descriptors.add(
                    createDescriptor(createChannel(CN_DESCRIPTION), "String", "scalarwebavcontrolcontentdescription"));
            descriptors.add(createDescriptor(createChannel(CN_DIRECTREMOTENUM), "Number",
                    "scalarwebavcontrolcontentdirectremotenum"));
            descriptors.add(createDescriptor(createChannel(CN_DISPNUM), "String", "scalarwebavcontrolcontentdispnum"));
            // descriptors.add(createDescriptor(createChannel(CN_DUBBINGINFO), "String",
            // "scalarwebavcontrolcontextdubbingInfo")); // todo
            descriptors.add(createDescriptor(createChannel(CN_DURATIONMSEC), "Number:Time",
                    "scalarwebavcontrolcontentdurationmsec"));
            descriptors.add(createDescriptor(createChannel(CN_DURATIONSEC), "Number:Time",
                    "scalarwebavcontrolcontentdurationsec"));
            descriptors.add(createDescriptor(createChannel(CN_EPGVISIBILITY), "String",
                    "scalarwebavcontrolcontentepgvisibility"));
            descriptors.add(createDescriptor(createChannel(CN_EVENTID), "String", "scalarwebavcontrolcontenteventid"));
            descriptors.add(createDescriptor(createChannel(CN_FILENO), "String", "scalarwebavcontrolcontentfileno"));
            descriptors.add(createDescriptor(createChannel(CN_FILESIZEBYTE), "Number:DataAmount",
                    "scalarwebavcontrolcontentfilesizebyte"));
            descriptors
                    .add(createDescriptor(createChannel(CN_FOLDERNO), "String", "scalarwebavcontrolcontentfolderno"));
            descriptors.add(createDescriptor(createChannel(CN_GENRE), "String", "scalarwebavcontrolcontentgenre"));
            descriptors.add(createDescriptor(createChannel(CN_GLOBALPLAYBACKCOUNT), "Number",
                    "scalarwebavcontrolcontentglobalplaybackcount"));
            // descriptors.add(createDescriptor(createChannel(CN_GROUPINFO), "String",
            // "scalarwebavcontrolcontextgroupInfo")); // TODO
            descriptors
                    .add(createDescriptor(createChannel(CN_HASRESUME), "String", "scalarwebavcontrolcontenthasresume"));
            descriptors.add(createDescriptor(createChannel(CN_IS3D), "String", "scalarwebavcontrolcontentis3d"));
            descriptors.add(createDescriptor(createChannel(CN_IS4K), "String", "scalarwebavcontrolcontentis4k"));
            descriptors.add(createDescriptor(createChannel(CN_ISALREADYPLAYED), "String",
                    "scalarwebavcontrolcontentisalreadyplayed"));
            descriptors.add(createDescriptor(createChannel(CN_ISAUTODELETE), "String",
                    "scalarwebavcontrolcontentisautodelete"));
            descriptors.add(
                    createDescriptor(createChannel(CN_ISBROWSABLE), "String", "scalarwebavcontrolcontentisbrowsable"));
            descriptors.add(createDescriptor(createChannel(CN_ISNEW), "String", "scalarwebavcontrolcontentisnew"));
            descriptors.add(
                    createDescriptor(createChannel(CN_ISPLAYABLE), "String", "scalarwebavcontrolcontentisplayable"));
            descriptors.add(
                    createDescriptor(createChannel(CN_ISPLAYLIST), "String", "scalarwebavcontrolcontentisplaylist"));
            descriptors.add(
                    createDescriptor(createChannel(CN_ISPROTECTED), "String", "scalarwebavcontrolcontentisprotected"));
            descriptors.add(createDescriptor(createChannel(CN_ISSOUNDPHOTO), "String",
                    "scalarwebavcontrolcontentissoundphoto"));
            descriptors
                    .add(createDescriptor(createChannel(CN_MEDIATYPE), "String", "scalarwebavcontrolcontentmediatype"));
            descriptors.add(createDescriptor(createChannel(CN_ORIGINALDISPNUM), "String",
                    "scalarwebavcontrolcontentoriginaldispnum"));
            descriptors.add(createDescriptor(createChannel(CN_OUTPUT), "String", "scalarwebavcontrolcontentoutput"));
            descriptors.add(createDescriptor(createChannel(CN_PARENTALCOUNTRY), "String",
                    "scalarwebavcontrolcontentparentalcountry"));
            descriptors.add(createDescriptor(createChannel(CN_PARENTALRATING), "String",
                    "scalarwebavcontrolcontentparentalrating"));
            descriptors.add(createDescriptor(createChannel(CN_PARENTALSYSTEM), "String",
                    "scalarwebavcontrolcontentparentalsystem"));
            descriptors.add(
                    createDescriptor(createChannel(CN_PARENTINDEX), "Number", "scalarwebavcontrolcontentparentindex"));
            // descriptors.add(createDescriptor(createChannel(CN_PLAYLISTINFO), "String",
            // "scalarwebavcontrolcontextplaylistInfo")); //todo
            descriptors.add(createDescriptor(createChannel(CN_PLAYLISTNAME), "String",
                    "scalarwebavcontrolcontentplaylistname"));
            // descriptors.add(createDescriptor(createChannel(CN_PLAYSPEED), "String",
            // "scalarwebavcontrolcontextplaySpeed")); // todo
            descriptors.add(
                    createDescriptor(createChannel(CN_PODCASTNAME), "String", "scalarwebavcontrolcontentpodcastname"));
            // descriptors.add(createDescriptor(createChannel(CN_POSITION), "String",
            // "scalarwebavcontrolcontextposition"));
            // //todo
            descriptors
                    .add(createDescriptor(createChannel(CN_PRODUCTID), "String", "scalarwebavcontrolcontentproductid"));
            descriptors.add(createDescriptor(createChannel(CN_PROGRAMMEDIATYPE), "String",
                    "scalarwebavcontrolcontentprogrammediatype"));
            descriptors.add(
                    createDescriptor(createChannel(CN_PROGRAMNUM), "Number", "scalarwebavcontrolcontentprogramnum"));
            descriptors.add(createDescriptor(createChannel(CN_PROGRAMSERVICETYPE), "String",
                    "scalarwebavcontrolcontentprogramservicetype"));
            descriptors.add(createDescriptor(createChannel(CN_PROGRAMTITLE), "String",
                    "scalarwebavcontrolcontentprogramtitle"));
            // descriptors.add(createDescriptor(createChannel(CN_RECORDINGINFO), "String",
            // "scalarwebavcontrolcontextrecordingInfo")); // todo
            descriptors.add(createDescriptor(createChannel(CN_REMOTEPLAYTYPE), "String",
                    "scalarwebavcontrolcontentremoteplaytype"));
            descriptors.add(
                    createDescriptor(createChannel(CN_REPEATTYPE), "String", "scalarwebavcontrolcontentrepeattype"));
            descriptors.add(createDescriptor(createChannel(CN_SERVICE), "String", "scalarwebavcontrolcontentservice"));
            descriptors.add(
                    createDescriptor(createChannel(CN_SIZEMB), "Number:DataAmount", "scalarwebavcontrolcontentsizemb"));
            descriptors.add(createDescriptor(createChannel(CN_SOURCE), "String", "scalarwebavcontrolcontentsource"));
            descriptors.add(
                    createDescriptor(createChannel(CN_SOURCELABEL), "String", "scalarwebavcontrolcontentsourcelabel"));
            descriptors.add(createDescriptor(createChannel(CN_STARTDATETIME), "String",
                    "scalarwebavcontrolcontentstartdatetime"));
            descriptors.add(createDescriptor(createChannel(CN_STATE), "String", "scalarwebavcontrolcontentstate"));
            descriptors.add(createDescriptor(createChannel(CN_STATESUPPLEMENT), "String",
                    "scalarwebavcontrolcontentstatesupplement"));
            descriptors.add(
                    createDescriptor(createChannel(CN_STORAGEURI), "String", "scalarwebavcontrolcontentstorageuri"));
            descriptors.add(createDescriptor(createChannel(CN_SUBTITLELANGUAGE), "String",
                    "scalarwebavcontrolcontentsubtitlelanguage"));
            descriptors.add(createDescriptor(createChannel(CN_SUBTITLETITLE), "String",
                    "scalarwebavcontrolcontentsubtitletitle"));
            descriptors.add(createDescriptor(createChannel(CN_SYNCCONTENTPRIORITY), "String",
                    "scalarwebavcontrolcontentsynccontentpriority"));
            descriptors.add(createDescriptor(createChannel(CN_TITLE), "String", "scalarwebavcontrolcontenttitle"));
            descriptors.add(
                    createDescriptor(createChannel(CN_TOTALCOUNT), "Number", "scalarwebavcontrolcontenttotalcount"));
            descriptors.add(
                    createDescriptor(createChannel(CN_TRIPLETSTR), "String", "scalarwebavcontrolcontenttripletstr"));
            descriptors.add(createDescriptor(createChannel(CN_USERCONTENTFLAG), "Switch",
                    "scalarwebavcontrolcontentusercontentflag"));
            descriptors.add(
                    createDescriptor(createChannel(CN_VIDEOCODEC), "String", "scalarwebavcontrolcontentvideocodec"));
            descriptors.add(
                    createDescriptor(createChannel(CN_VISIBILITY), "String", "scalarwebavcontrolcontentvisibility"));
        }
    }

    /**
     * Adds input status descriptors for a specific input
     * 
     * @param descriptors a non-null, possibly empty list of descriptors
     * @param id a non-null, non-empty channel id
     * @param uri a non-null, non-empty input uri
     * @param title a non-null, non-empty input title
     * @param apiVersion a non-null, non-empty API version
     */
    private void addInputStatusDescriptor(final List<ScalarWebChannelDescriptor> descriptors, final String id,
            final String uri, final String title, final String apiVersion) {
        Objects.requireNonNull(descriptors, "descriptors cannot be null");
        Validate.notEmpty(id, "id cannot be empty");
        Validate.notEmpty(uri, "uri cannot be empty");
        Validate.notEmpty(title, "title cannot be empty");
        Validate.notEmpty(apiVersion, "apiVersion cannot be empty");

        descriptors.add(createDescriptor(createChannel(IN_URI, id, uri), "String", "scalarwebavcontrolinpstatusuri",
                "Input " + title + " URI", null));

        descriptors.add(createDescriptor(createChannel(IN_TITLE, id, uri), "String", "scalarwebavcontrolinpstatustitle",
                "Input " + title + " Title", null));

        descriptors.add(createDescriptor(createChannel(IN_CONNECTION, id, uri), "Switch",
                "scalarwebavcontrolinpstatusconnection", "Input " + title + " Connected", uri));

        descriptors.add(createDescriptor(createChannel(IN_LABEL, id, uri), "String", "scalarwebavcontrolinpstatuslabel",
                "Input " + title + " Label", null));

        descriptors.add(createDescriptor(createChannel(IN_ICON, id, uri), "String", "scalarwebavcontrolinpstatusicon",
                "Input " + title + " Icon Type", null));

        if (StringUtils.equalsIgnoreCase(apiVersion, ScalarWebMethod.V1_1)) {
            descriptors.add(createDescriptor(createChannel(IN_STATUS, id, uri), "String",
                    "scalarwebavcontrolinpstatusstatus", "Input " + title + " Status", null));
        }
    }

    /**
     * Adds all input status descriptors
     * 
     * @param descriptors a non-null, possibly empty list of descriptors
     */
    private void addInputStatusDescriptors(final List<ScalarWebChannelDescriptor> descriptors) {
        Objects.requireNonNull(descriptors, "descriptors cannot be null");

        try {
            final ScalarWebResult result = getInputStatus();
            final String version = getService().getVersion(ScalarWebMethod.GETCURRENTEXTERNALINPUTSSTATUS);
            if (VersionUtilities.equals(version, ScalarWebMethod.V1_0)) {
                for (final CurrentExternalInputsStatus_1_0 status : result
                        .asArray(CurrentExternalInputsStatus_1_0.class)) {
                    final String uri = status.getUri();
                    if (uri == null || StringUtils.isEmpty(uri)) {
                        logger.debug("External Input status had no URI (which is required): {}", status);
                        continue;
                    }

                    final String id = createChannelId(uri, true);
                    addInputStatusDescriptor(descriptors, id, uri, status.getTitle(MAINTITLE), ScalarWebMethod.V1_0);
                }
            } else if (VersionUtilities.equals(version, ScalarWebMethod.V1_1)) {
                for (final CurrentExternalInputsStatus_1_1 status : result
                        .asArray(CurrentExternalInputsStatus_1_1.class)) {
                    final String uri = status.getUri();
                    if (uri == null || StringUtils.isEmpty(uri)) {
                        logger.debug("External Input status had no URI (which is required): {}", status);
                        continue;
                    }

                    final String id = createChannelId(uri, true);
                    addInputStatusDescriptor(descriptors, id, uri, status.getTitle(MAINTITLE), ScalarWebMethod.V1_1);
                }
            }
        } catch (final IOException e) {
            logger.debug("Error add input status description {}", e.getMessage());
        }
    }

    /**
     * Adds the parental rating descriptors
     * 
     * @param descriptors a non-null, possibly empty list of descriptors
     */
    private void addParentalRatingDescriptors(final List<ScalarWebChannelDescriptor> descriptors) {
        Objects.requireNonNull(descriptors, "descriptors cannot be null");

        try {
            // execute to verify if it exists
            getParentalRating();

            descriptors.add(
                    createDescriptor(createChannel(PR_RATINGTYPEAGE), "Number", "scalarwebavcontrolprratingtypeage"));
            descriptors.add(
                    createDescriptor(createChannel(PR_RATINGTYPESONY), "String", "scalarwebavcontrolprratingtypesony"));
            descriptors.add(
                    createDescriptor(createChannel(PR_RATINGCOUNTRY), "String", "scalarwebavcontrolprratingcountry"));
            descriptors.add(createDescriptor(createChannel(PR_RATINGCUSTOMTYPETV), "String",
                    "scalarwebavcontrolprratingcustomtypetv"));
            descriptors.add(createDescriptor(createChannel(PR_RATINGCUSTOMTYPEMPAA), "String",
                    "scalarwebavcontrolprratingcustomtypempaa"));
            descriptors.add(createDescriptor(createChannel(PR_RATINGCUSTOMTYPECAENGLISH), "String",
                    "scalarwebavcontrolprratingcustomtypecaenglish"));
            descriptors.add(createDescriptor(createChannel(PR_RATINGCUSTOMTYPECAFRENCH), "String",
                    "scalarwebavcontrolprratingcustomtypecafrench"));
            descriptors
                    .add(createDescriptor(createChannel(PR_UNRATEDLOCK), "Switch", "scalarwebavcontrolprunratedlock"));
        } catch (final IOException e) {
            logger.debug("Exception occurring getting the parental ratings: {}", e.getMessage());
        }
    }

    /**
     * Adds the playing content descriptors
     * 
     * @param descriptors a non-null, possibly empty list of descriptors
     */
    private void addPlayingContentDescriptors(final List<ScalarWebChannelDescriptor> descriptors) {
        Objects.requireNonNull(descriptors, "descriptors cannot be null");

        final Map<String, String> outputs = getTerminalOutputs(getTerminalStatuses());

        final String version = getService().getVersion(ScalarWebMethod.GETPLAYINGCONTENTINFO);

        for (final Entry<String, String> entry : outputs.entrySet()) {
            final String translatedOutput = getTranslatedOutput(entry.getKey());
            final String prefix = "Playing"
                    + (StringUtils.isEmpty(translatedOutput) ? " " : (" (" + entry.getValue() + ") "));

            final String uri = entry.getKey();
            final String id = getIdForOutput(uri); // use the same id as the related terminal

            descriptors.add(createDescriptor(createChannel(PL_CMD, id, uri), "String", "scalarwebavcontrolplcommand",
                    prefix + "Command", null));

            descriptors.add(createDescriptor(createChannel(PL_PRESET, id, uri), "Number", "scalarwebavcontrolplpreset",
                    prefix + "Preset", null));

            if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1, ScalarWebMethod.V1_2)) {
                descriptors.add(createDescriptor(createChannel(PL_BIVLASSETID, id, uri), "String",
                        "scalarwebavcontrolplbivlassetid", prefix + "BIVL AssetID", null));

                descriptors.add(createDescriptor(createChannel(PL_BIVLPROVIDER, id, uri), "String",
                        "scalarwebavcontrolplbivlprovider", prefix + "BIVL Provider", null));

                descriptors.add(createDescriptor(createChannel(PL_BIVLSERVICEID, id, uri), "String",
                        "scalarwebavcontrolplbivlserviceid", prefix + "BIVL ServiceID", null));

                descriptors.add(createDescriptor(createChannel(PL_DISPNUM, id, uri), "String",
                        "scalarwebavcontrolpldispnum", prefix + "Display Number", null));

                descriptors.add(createDescriptor(createChannel(PL_DURATIONSEC, id, uri), "Number:Time",
                        "scalarwebavcontrolpldurationsec", prefix + "Duraction (in seconds)", null));

                descriptors.add(createDescriptor(createChannel(PL_MEDIATYPE, id, uri), "String",
                        "scalarwebavcontrolplmediatype", prefix + "Media Type", null));

                descriptors.add(createDescriptor(createChannel(PL_ORIGINALDISPNUM, id, uri), "String",
                        "scalarwebavcontrolploriginaldispnum", prefix + "Original Display Number", null));

                descriptors.add(createDescriptor(createChannel(PL_PLAYSPEED, id, uri), "String",
                        "scalarwebavcontrolplplayspeed", prefix + "Play Speed", null));

                descriptors.add(createDescriptor(createChannel(PL_PROGRAMNUM, id, uri), "Number",
                        "scalarwebavcontrolplprogramnum", prefix + "Program Number", null));

                descriptors.add(createDescriptor(createChannel(PL_PROGRAMTITLE, id, uri), "String",
                        "scalarwebavcontrolplprogramtitle", prefix + "Program Title", null));

                descriptors.add(createDescriptor(createChannel(PL_SOURCE, id, uri), "String",
                        "scalarwebavcontrolplsource", prefix + "Source", null));

                descriptors.add(createDescriptor(createChannel(PL_STARTDATETIME, id, uri), "String",
                        "scalarwebavcontrolplstartdatetime", prefix + "Start Date/Time", null));

                descriptors.add(createDescriptor(createChannel(PL_TITLE, id, uri), "String",
                        "scalarwebavcontrolpltitle", prefix + "Title", null));

                descriptors.add(createDescriptor(createChannel(PL_TRIPLETSTR, id, uri), "String",
                        "scalarwebavcontrolpltripletstr", prefix + "Triplet", null));

                descriptors.add(createDescriptor(createChannel(PL_URI, id, uri), "String", "scalarwebavcontrolpluri",
                        prefix + "URI", null));

            }

            if (VersionUtilities.equals(version, ScalarWebMethod.V1_2)) {
                descriptors.add(createDescriptor(createChannel(PL_ALBUMNAME, id, uri), "String",
                        "scalarwebavcontrolplalbumname", prefix + "Album Name", null));
                descriptors.add(createDescriptor(createChannel(PL_APPLICATIONNAME, id, uri), "String",
                        "scalarwebavcontrolplapplicationname", prefix + "Application Name", null));
                descriptors.add(createDescriptor(createChannel(PL_ARTIST, id, uri), "String",
                        "scalarwebavcontrolplartist", prefix + "Artist", null));
                descriptors.add(createDescriptor(createChannel(PL_AUDIOCHANNEL, id, uri), "String",
                        "scalarwebavcontrolplaudiochannel", prefix + "Audio Channel", null));
                descriptors.add(createDescriptor(createChannel(PL_AUDIOCODEC, id, uri), "String",
                        "scalarwebavcontrolplaudiocodec", prefix + "Audio Codec", null));
                descriptors.add(createDescriptor(createChannel(PL_AUDIOFREQUENCY, id, uri), "String",
                        "scalarwebavcontrolplaudiofrequency", prefix + "Audio Frequency", null));
                descriptors.add(createDescriptor(createChannel(PL_BROADCASTFREQ, id, uri), "Number:Frequency",
                        "scalarwebavcontrolplbroadcastfreq", prefix + "Broadcast Frequency", null));
                descriptors.add(createDescriptor(createChannel(PL_BROADCASTFREQBAND, id, uri), "String",
                        "scalarwebavcontrolplbroadcastfreqband", prefix + "Broadcast Frequency Band", null));
                descriptors.add(createDescriptor(createChannel(PL_CHANNELNAME, id, uri), "String",
                        "scalarwebavcontrolplchannelname", prefix + "Channel Name", null));
                descriptors.add(createDescriptor(createChannel(PL_CHAPTERCOUNT, id, uri), "Number",
                        "scalarwebavcontrolplchaptercount", prefix + "Chapter Count", null));
                descriptors.add(createDescriptor(createChannel(PL_CHAPTERINDEX, id, uri), "Number",
                        "scalarwebavcontrolplchapterindex", prefix + "Chapter Index", null));
                descriptors.add(createDescriptor(createChannel(PL_CONTENTKIND, id, uri), "String",
                        "scalarwebavcontrolplcontentkind", prefix + "Content Kind", null));
                descriptors.add(createDescriptor(createChannel(PL_DABCOMPONENTLABEL, id, uri), "String",
                        "scalarwebavcontrolpldabcomponentlabel", prefix + "DAB Component Label", null));
                descriptors.add(createDescriptor(createChannel(PL_DABDYNAMICLABEL, id, uri), "String",
                        "scalarwebavcontrolpldabdynamiclabel", prefix + "DAB Dynamic Label", null));
                descriptors.add(createDescriptor(createChannel(PL_DABENSEMBLELABEL, id, uri), "String",
                        "scalarwebavcontrolpldabensemblelabel", prefix + "DAB Ensemble Label", null));
                descriptors.add(createDescriptor(createChannel(PL_DABSERVICELABEL, id, uri), "String",
                        "scalarwebavcontrolpldabservicelabel", prefix + "DAB Service Label", null));
                descriptors.add(createDescriptor(createChannel(PL_DURATIONMSEC, id, uri), "Number:Time",
                        "scalarwebavcontrolpldurationmsec", prefix + "Duration (in milliseconds)", null));
                descriptors.add(createDescriptor(createChannel(PL_FILENO, id, uri), "String",
                        "scalarwebavcontrolplfileno", prefix + "File Number", null));
                descriptors.add(createDescriptor(createChannel(PL_GENRE, id, uri), "String",
                        "scalarwebavcontrolplgenre", prefix + "Genre", null));
                descriptors.add(createDescriptor(createChannel(PL_INDEX, id, uri), "Number",
                        "scalarwebavcontrolplindex", prefix + "Index", null));
                descriptors.add(createDescriptor(createChannel(PL_IS3D, id, uri), "String", "scalarwebavcontrolplis3d",
                        prefix + "is 3D", null));
                descriptors.add(createDescriptor(createChannel(PL_OUTPUT, id, uri), "String",
                        "scalarwebavcontrolploutput", prefix + "Output", null));
                descriptors.add(createDescriptor(createChannel(PL_PARENTINDEX, id, uri), "Number",
                        "scalarwebavcontrolplparentindex", prefix + "Parent Index", null));
                descriptors.add(createDescriptor(createChannel(PL_PARENTURI, id, uri), "String",
                        "scalarwebavcontrolplparenturi", prefix + "Parent URI", null));
                descriptors.add(createDescriptor(createChannel(PL_PATH, id, uri), "String", "scalarwebavcontrolplpath",
                        prefix + "Path", null));
                descriptors.add(createDescriptor(createChannel(PL_PLAYLISTNAME, id, uri), "String",
                        "scalarwebavcontrolplplaylistname", prefix + "Play List Name", null));
                descriptors.add(createDescriptor(createChannel(PL_PLAYSTEPSPEED, id, uri), "Number",
                        "scalarwebavcontrolplplaystepspeed", prefix + "Play Step Speed", null));
                descriptors.add(createDescriptor(createChannel(PL_PODCASTNAME, id, uri), "String",
                        "scalarwebavcontrolplpodcastname", prefix + "Podcast Name", null));
                descriptors.add(createDescriptor(createChannel(PL_POSITIONMSEC, id, uri), "Number:Time",
                        "scalarwebavcontrolplpositionmsec", prefix + "Position (in milliseconds)", null));
                descriptors.add(createDescriptor(createChannel(PL_POSITIONSEC, id, uri), "Number:Time",
                        "scalarwebavcontrolplpositionsec", prefix + "Position (in seconds)", null));
                descriptors.add(createDescriptor(createChannel(PL_REPEATTYPE, id, uri), "String",
                        "scalarwebavcontrolplrepeattype", prefix + "Repeat Type", null));
                descriptors.add(createDescriptor(createChannel(PL_SERVICE, id, uri), "String",
                        "scalarwebavcontrolplservice", prefix + "Service", null));
                descriptors.add(createDescriptor(createChannel(PL_SOURCELABEL, id, uri), "String",
                        "scalarwebavcontrolplsourcelabel", prefix + "Source Label", null));
                descriptors.add(createDescriptor(createChannel(PL_STATE, id, uri), "String",
                        "scalarwebavcontrolplstate", prefix + "State", null));
                descriptors.add(createDescriptor(createChannel(PL_STATESUPPLEMENT, id, uri), "String",
                        "scalarwebavcontrolplstatesupplement", prefix + "State Supplement", null));
                descriptors.add(createDescriptor(createChannel(PL_SUBTITLEINDEX, id, uri), "Number",
                        "scalarwebavcontrolplsubtitleindex", prefix + "Subtitle Index", null));
                descriptors.add(createDescriptor(createChannel(PL_TOTALCOUNT, id, uri), "Number",
                        "scalarwebavcontrolpltotalcount", prefix + "Total Count", null));
                descriptors.add(createDescriptor(createChannel(PL_VIDEOCODEC, id, uri), "String",
                        "scalarwebavcontrolplvideocodec", prefix + "Video Codec", null));
            }
        }
    }

    /**
     * Adds the terminal status descriptors
     * 
     * @param descriptors a non-null, possibly empty list of descriptors
     */
    private void addTerminalStatusDescriptors(final List<ScalarWebChannelDescriptor> descriptors) {
        Objects.requireNonNull(descriptors, "descriptors cannot be null");

        for (final CurrentExternalTerminalsStatus_1_0 term : getTerminalStatuses()) {
            final String uri = term.getUri();
            if (uri == null) {
                logger.debug("External Terminal status had no URI (which is required): {}", term);
                continue;
            }

            final String title = term.getTitle(MAINTITLE);
            final String id = createChannelId(uri, false);

            if (term.isOutput()) {
                descriptors.add(createDescriptor(createChannel(TERM_SOURCE, id, uri), "String",
                        "scalarwebavcontroltermstatussource", "Terminal " + title + " Source", null));

                // if not our dummy 'main', create an active switch
                if (!StringUtils.equalsIgnoreCase(title, MAINTITLE)) {
                    descriptors.add(createDescriptor(createChannel(TERM_ACTIVE, id, uri), "Switch",
                            "scalarwebavcontroltermstatusactive", "Terminal " + title + " Active", null));
                }
            }

            descriptors.add(createDescriptor(createChannel(TERM_URI, id, uri), "String",
                    "scalarwebavcontroltermstatusuri", "Terminal " + title + " URI", null));

            descriptors.add(createDescriptor(createChannel(TERM_TITLE, id, uri), "String",
                    "scalarwebavcontroltermstatustitle", "Terminal " + title + " Title", null));

            descriptors.add(createDescriptor(createChannel(TERM_CONNECTION, id, uri), "String",
                    "scalarwebavcontroltermstatusconnection", "Terminal " + title + " Connection", null));

            descriptors.add(createDescriptor(createChannel(TERM_LABEL, id, uri), "String",
                    "scalarwebavcontroltermstatuslabel", "Terminal " + title + " Label", null));

            descriptors.add(createDescriptor(createChannel(TERM_ICON, id, uri), "Image",
                    "scalarwebavcontroltermstatusicon", "Terminal " + title + " Icon", null));

        }
    }

    /**
     * Adds the preset channel descriptors
     * 
     * @param descriptors a non-null, possibly empty list of descriptors
     */
    private void addPresetChannelDescriptors(final List<ScalarWebChannelDescriptor> descriptors) {
        Objects.requireNonNull(descriptors, "descriptors cannot be null");

        for (final Scheme scheme : getSchemes()) {
            if (StringUtils.equalsIgnoreCase(Scheme.TV, scheme.getScheme())
                    || StringUtils.equalsIgnoreCase(Scheme.RADIO, scheme.getScheme())) {
                for (final Source src : getSources(scheme)) {
                    addPresetChannelDescriptor(descriptors, src);
                }
            }
        }
    }

    /**
     * Adds the preset channel descriptors for a specific source
     * 
     * @param descriptors a non-null, possibly empty list of descriptors
     * @param src a non-null source
     */
    private void addPresetChannelDescriptor(final List<ScalarWebChannelDescriptor> descriptors, final Source src) {
        Objects.requireNonNull(descriptors, "descriptors cannot be null");
        Objects.requireNonNull(src, "src cannot be null");

        final String source = src.getSource();
        if (source == null || StringUtils.isEmpty(source)) {
            logger.debug("Source did not have a source assigned: {}", src);
            return;
        }

        final String sourcePart = src.getSourcePart();
        if (sourcePart == null || StringUtils.isEmpty(sourcePart)) {
            logger.debug("Source had a malformed source (no source part or no scheme): {}", src);
            return;
        }

        final ScalarWebChannel chl = createChannel(PS_CHANNEL, sourcePart, source);

        final String upperSrc = sourcePart.toUpperCase();
        descriptors.add(createDescriptor(chl, "String", "scalarwebavcontrolpresetchannel", "Presets for " + upperSrc,
                "Set preset for " + upperSrc));

        refreshPresetChannelStateDescription(Collections.singletonList(chl));
    }

    @Override
    protected void eventReceived(final ScalarWebEvent event) throws IOException {
        Objects.requireNonNull(event, "event cannot be null");
        final @Nullable String mtd = event.getMethod();
        if (mtd == null || StringUtils.isEmpty(mtd)) {
            logger.debug("Unhandled event received (no method): {}", event);
        } else {
            switch (mtd) {
                case ScalarWebEvent.NOTIFYPLAYINGCONTENTINFO:
                    final String version = getVersion(ScalarWebMethod.GETPLAYINGCONTENTINFO);
                    if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1)) {
                        notifyPlayingContentInfo(event.as(PlayingContentInfoResult_1_0.class),
                                getIdForOutput(MAINOUTPUT));
                    } else {
                        final PlayingContentInfoResult_1_2 res = event.as(PlayingContentInfoResult_1_2.class);
                        final String output = res.getOutput(MAINOUTPUT);
                        notifyPlayingContentInfo(res, getIdForOutput(output));
                    }

                    break;

                case ScalarWebEvent.NOTIFYAVAILABLEPLAYBACKFUNCTION:
                    // TODO
                    break;

                case ScalarWebEvent.NOTIFYEXTERNALTERMINALSTATUS:
                    notifyCurrentTerminalStatus(event.as(CurrentExternalTerminalsStatus_1_0.class));
                    break;

                default:
                    logger.debug("Unhandled event received: {}", event);
                    break;
            }
        }
    }

    /**
     * Get's the channel id for the given output
     * 
     * @param output a non-null, non-empty output identifier
     * @return a channel identifier representing the output
     */
    private String getIdForOutput(final String output) {
        Validate.notEmpty(output, "output cannot be empty");

        for (final Channel chl : getContext().getThing().getChannels()) {
            final ScalarWebChannel swc = new ScalarWebChannel(chl);
            if (StringUtils.equalsIgnoreCase(swc.getCategory(), TERM_URI)) {
                final String uri = swc.getPathPart(0);
                if (StringUtils.equals(uri, output)) {
                    return swc.getId();
                }
            }
        }

        return SonyUtil.createValidChannelUId(output);
    }

    /**
     * Get the source identifier for the given URL
     * 
     * @param uid a non-null, non-empty source
     * @return the source identifier (or uid if none found)
     */
    private String getSourceFromUri(final String uid) {
        Validate.notEmpty(uid, "uid cannot be empty");
        // Following finds the source from the uri (radio:fm&content=x to radio:fm)
        final String src = getSources().stream().filter(s -> StringUtils.startsWith(uid, s.getSource())).findFirst()
                .map(s -> s.getSource()).orElse(null);
        return StringUtils.defaultIfEmpty(src, uid);
    }

    /**
     * Returns the current input statuses. This method simply calls getInputStatus(false) to get the cached result if it
     * exists
     * 
     * @return the ScalarWebResult containing the status information for all the inputs
     * @throws IOException if an IOException occurrs getting the input status
     */
    private ScalarWebResult getInputStatus() throws IOException {
        return getInputStatus(false);
    }

    /**
     * Returns the current input status. If refresh is false, the cached version is used (if it exists) - otherwise we
     * query the device for the statuses
     * 
     * @param refresh true to refresh from the device, false to potentially use a cached version (if it exists)
     * @return the ScalarWebResult containing the status information for all the inputs
     * @throws IOException if an IOException occurrs getting the input status
     */
    private ScalarWebResult getInputStatus(final boolean refresh) throws IOException {
        if (!refresh) {
            final ScalarWebResult rs = stateInputs.get();
            if (rs != null) {
                return rs;
            }
        }
        final ScalarWebResult res = execute(ScalarWebMethod.GETCURRENTEXTERNALINPUTSSTATUS);
        stateInputs.set(res);
        return res;
    }

    /**
     * Get's the parental rating from the device
     * 
     * @return the ScalarWebResult containing the status information for all the inputs
     * @throws IOException if an IOException occurrs getting the input status
     */
    private ScalarWebResult getParentalRating() throws IOException {
        return execute(ScalarWebMethod.GETPARENTALRATINGSETTINGS);
    }

    /**
     * Gets the playing content info
     *
     * @return the ScalarWebResult containing the status information for all the inputs
     * @throws IOException if an IOException occurrs getting the input status
     */
    private ScalarWebResult getPlayingContentInfo() throws IOException {
        return execute(ScalarWebMethod.GETPLAYINGCONTENTINFO, version -> {
            if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1)) {
                return null;
            }
            return new PlayingContentInfoRequest_1_2("");
        });
    }

    /**
     * Returns the current set of schemes. This method simply calls getSchemes(false) to get the cached result if it
     * exists
     * 
     * @return a non-null, possibly empty set of schemes
     */
    private Set<Scheme> getSchemes() {
        return getSchemes(false);
    }

    /**
     * Returns the current set of schemes. If refresh is false, the cached version is used (if it exists) - otherwise we
     * query the device for the schemes
     * 
     * @param refresh true to refresh from the device, false to potentially use a cached version (if it exists)
     * @return a non-null, possibly empty set of schemes
     */
    private Set<Scheme> getSchemes(final boolean refresh) {
        final Set<Scheme> cacheSchemes = stateSchemes.get();
        if (!cacheSchemes.isEmpty() && !refresh) {
            return cacheSchemes;
        }

        final Set<Scheme> schemes = new HashSet<>();
        try {
            for (final Scheme scheme : execute(ScalarWebMethod.GETSCHEMELIST).asArray(Scheme.class)) {
                final String schemeName = scheme.getScheme();
                if (schemeName != null && StringUtils.isNotEmpty(schemeName)) {
                    schemes.add(scheme);
                }
            }
        } catch (final IOException e) {
            logger.debug("Exception occurred retrieving the scheme list: {}", e.getMessage());
        }

        stateSchemes.set(schemes);
        return schemes;
    }

    /**
     * Returns the current set of sources. This method simply calls getSources(false) to get the cached result if it
     * exists
     * 
     * @return a non-null, possibly empty set of sources
     */
    private Set<Source> getSources() {
        return getSources(false);
    }

    /**
     * Gets list of sources for a scheme. Some schemes are not valid for specific versions of the source (like DLNA is
     * only valid 1.2+) - so query all sources by scheme and return a consolidated list
     *
     * @param refresh true to refresh from the device, false to potentially use a cached version (if it exists)
     * @return the non-null, possibly empty list of sources
     */
    private Set<Source> getSources(final boolean refresh) {
        final Set<Source> sources = getSchemes().stream().flatMap(s -> getSources(s, refresh).stream())
                .collect(Collectors.toSet());

        if (refresh) {
            updateTermSource();
        }

        return sources;
    }

    /**
     * Gets a list of sources for a scheme
     * 
     * @param scheme a non-null scheme
     * @return a non-null, possibly empty list of sources
     */
    private Set<Source> getSources(final Scheme scheme) {
        Objects.requireNonNull(scheme, "scheme cannot be null");
        return getSources(scheme, false);
    }

    /**
     * Gets a list of sources for a scheme, possibly refreshing them first
     * 
     * @param scheme a non-null scheme
     * @param refresh true to refresh first, false to use cached
     * @return a non-null, possibly empty list of sources
     */
    private Set<Source> getSources(final Scheme scheme, final boolean refresh) {
        Objects.requireNonNull(scheme, "scheme cannot be null");

        final String schemeName = scheme.getScheme();
        if (schemeName == null || StringUtils.isEmpty(schemeName)) {
            return Collections.emptySet();
        }

        return stateSources.compute(schemeName, (k, v) -> {
            if (v != null && !v.isEmpty() && !refresh) {
                return v;
            }

            final Set<Source> srcs = new HashSet<>();
            try {
                for (final String version : getService().getVersions(ScalarWebMethod.GETSOURCELIST)) {
                    final ScalarWebResult result = getService().executeSpecific(ScalarWebMethod.GETSOURCELIST, version,
                            scheme);

                    // This can happen if the specific version source doesn't support the scheme for
                    if (result.getDeviceErrorCode() == ScalarWebError.NOTIMPLEMENTED
                            || result.getDeviceErrorCode() == ScalarWebError.UNSUPPORTEDOPERATION
                            || result.getDeviceErrorCode() == ScalarWebError.ILLEGALARGUMENT) {
                        logger.trace("Source version {} for scheme {} is not implemented", version, scheme);
                    } else {
                        for (final Source src : result.asArray(Source.class)) {
                            final String sourceName = src.getSource();
                            if (sourceName != null && StringUtils.isNotEmpty(sourceName)) {
                                srcs.add(src);
                            }
                        }
                    }
                }
            } catch (final IOException e) {
                logger.debug("Exception occurred retrieving the source list for scheme {}: {}", scheme, e.getMessage());
            }
            return srcs;
        });
    }

    /**
     * Updates the terminal sources
     */
    private void updateTermSource() {
        final List<InputSource> sources = new ArrayList<>();
        if (getService().hasMethod(ScalarWebMethod.GETCURRENTEXTERNALINPUTSSTATUS)
                && !getService().hasMethod(ScalarWebMethod.GETCURRENTEXTERNALTERMINALSSTATUS)) {
            // no need to do versioning since everything we want is in v1_0 (and v1_1 inherites from it)
            try {
                for (final CurrentExternalInputsStatus_1_0 inp : getInputStatus()
                        .asArray(CurrentExternalInputsStatus_1_0.class)) {
                    final String uri = inp.getUri();
                    if (uri == null || StringUtils.isEmpty(uri)) {
                        continue;
                    }

                    final String title = inp.getTitle(uri);
                    sources.add(new InputSource(uri, title, null));
                }
            } catch (final IOException e) {
                logger.debug("Error updating terminal source {}", e.getMessage());
            }
        }

        stateSources.values().stream().flatMap(e -> e.stream()).forEach(src -> {
            final String uri = src.getSource();
            if (uri != null && StringUtils.isNotEmpty(uri)) {
                final String[] outputs = src.getOutputs();

                // Add the source if not duplicating an input/terminal
                // (need to use starts with because hdmi source has a port number in it and
                // the source is just hdmi [no ports])
                if (!sources.stream().anyMatch(s -> StringUtils.startsWithIgnoreCase(s.getUri(), uri))) {
                    sources.add(new InputSource(uri, src.getTitle(),
                            outputs == null ? new ArrayList<>() : Arrays.asList(outputs)));
                }
            }
        });

        for (final CurrentExternalTerminalsStatus_1_0 term : getTerminalStatuses()) {
            if (term.isOutput()) {
                final String uri = term.getUri();
                if (uri != null && StringUtils.isNotEmpty(uri)) {
                    final List<StateOption> options = sources.stream()
                            .filter(s -> s.getOutputs().isEmpty() || s.getOutputs().contains(uri))
                            .map(s -> new StateOption(s.getUri(), s.getTitle()))
                            .sorted((a, b) -> ObjectUtils.compare(a.getLabel(), b.getLabel()))
                            .collect(Collectors.toList());

                    final String id = getIdForOutput(uri);
                    final ScalarWebChannel cnl = createChannel(TERM_SOURCE, id, uri);
                    final StateDescription sd = StateDescriptionFragmentBuilder.create().withOptions(options).build()
                            .toStateDescription();
                    if (sd != null) {
                        getContext().getStateProvider().addStateOverride(getContext().getThingUID(), cnl.getChannelId(),
                                sd);
                    }
                }
            }
        }
    }

    /**
     * Gets a terminal status
     * 
     * @return a non-null {@link ScalarWebResult}
     * @throws IOException if an IO exception occurs
     */
    private ScalarWebResult getTerminalStatus() throws IOException {
        return execute(ScalarWebMethod.GETCURRENTEXTERNALTERMINALSSTATUS);
    }

    /**
     * Gets the list of terminal status
     * 
     * @return a non-null, possibly empty list of terminal status
     */
    private List<CurrentExternalTerminalsStatus_1_0> getTerminalStatuses() {
        return getTerminalStatuses(false);
    }

    /**
     * Gets the list of terminal status, possibly refreshing before returning
     * 
     * @param refresh true to refresh statuses, false to use cached statuses
     * @return a non-null, possibly empty list of terminal statuses
     */
    private List<CurrentExternalTerminalsStatus_1_0> getTerminalStatuses(final boolean refresh) {
        final List<CurrentExternalTerminalsStatus_1_0> cachedTerms = stateTerminals.get();
        if (!cachedTerms.isEmpty() && !refresh) {
            return cachedTerms;
        }

        final List<CurrentExternalTerminalsStatus_1_0> terms = new ArrayList<>();
        try {
            terms.addAll(getTerminalStatus().asArray(CurrentExternalTerminalsStatus_1_0.class));
        } catch (final IOException e) {
            logger.debug("Error getting terminal statuses {}", e.getMessage());
        }

        // If no outputs, create our dummy 'main' output
        if (!terms.stream().anyMatch(t -> t.isOutput())) {
            terms.add(new CurrentExternalTerminalsStatus_1_0(MAINOUTPUT, MAINTITLE));
        }

        stateTerminals.set(terms);
        return terms;
    }

    /**
     * Updates all content channels to undefined
     */
    private void notifyContentListResult() {
        // Set everything to undefined except for uri, index, childcount and selected
        stateChanged(CN_ALBUMNAME, UnDefType.UNDEF);
        stateChanged(CN_APPLICATIONNAME, UnDefType.UNDEF);
        stateChanged(CN_ARTIST, UnDefType.UNDEF);
        stateChanged(CN_AUDIOCHANNEL, UnDefType.UNDEF);
        stateChanged(CN_AUDIOCODEC, UnDefType.UNDEF);
        stateChanged(CN_AUDIOFREQUENCY, UnDefType.UNDEF);
        stateChanged(CN_BIVLSERVICEID, UnDefType.UNDEF);
        stateChanged(CN_BIVLASSETID, UnDefType.UNDEF);
        stateChanged(CN_BIVLPROVIDER, UnDefType.UNDEF);
        stateChanged(CN_BROADCASTFREQ, UnDefType.UNDEF);
        stateChanged(CN_BROADCASTFREQBAND, UnDefType.UNDEF);
        stateChanged(CN_CHANNELNAME, UnDefType.UNDEF);
        stateChanged(CN_CHANNELSURFINGVISIBILITY, UnDefType.UNDEF);
        stateChanged(CN_CHAPTERCOUNT, UnDefType.UNDEF);
        stateChanged(CN_CHAPTERINDEX, UnDefType.UNDEF);
        stateChanged(CN_CLIPCOUNT, UnDefType.UNDEF);
        stateChanged(CN_CONTENTKIND, UnDefType.UNDEF);
        stateChanged(CN_CONTENTTYPE, UnDefType.UNDEF);
        stateChanged(CN_CREATEDTIME, UnDefType.UNDEF);
        stateChanged(CN_DABCOMPONENTLABEL, UnDefType.UNDEF);
        stateChanged(CN_DABDYNAMICLABEL, UnDefType.UNDEF);
        stateChanged(CN_DABENSEMBLELABEL, UnDefType.UNDEF);
        stateChanged(CN_DABSERVICELABEL, UnDefType.UNDEF);
        stateChanged(CN_DESCRIPTION, UnDefType.UNDEF);
        stateChanged(CN_DIRECTREMOTENUM, UnDefType.UNDEF);
        stateChanged(CN_DISPNUM, UnDefType.UNDEF);
        stateChanged(CN_DURATIONMSEC, UnDefType.UNDEF);
        stateChanged(CN_DURATIONSEC, UnDefType.UNDEF);
        stateChanged(CN_EPGVISIBILITY, UnDefType.UNDEF);
        stateChanged(CN_EVENTID, UnDefType.UNDEF);
        stateChanged(CN_FILENO, UnDefType.UNDEF);
        stateChanged(CN_FILESIZEBYTE, UnDefType.UNDEF);
        stateChanged(CN_FOLDERNO, UnDefType.UNDEF);
        stateChanged(CN_GENRE, UnDefType.UNDEF);
        stateChanged(CN_GLOBALPLAYBACKCOUNT, UnDefType.UNDEF);
        stateChanged(CN_HASRESUME, UnDefType.UNDEF);
        stateChanged(CN_IS3D, UnDefType.UNDEF);
        stateChanged(CN_IS4K, UnDefType.UNDEF);
        stateChanged(CN_ISALREADYPLAYED, UnDefType.UNDEF);
        stateChanged(CN_ISAUTODELETE, UnDefType.UNDEF);
        stateChanged(CN_ISBROWSABLE, UnDefType.UNDEF);
        stateChanged(CN_ISNEW, UnDefType.UNDEF);
        stateChanged(CN_ISPLAYABLE, UnDefType.UNDEF);
        stateChanged(CN_ISPLAYLIST, UnDefType.UNDEF);
        stateChanged(CN_ISPROTECTED, UnDefType.UNDEF);
        stateChanged(CN_ISSOUNDPHOTO, UnDefType.UNDEF);
        stateChanged(CN_MEDIATYPE, UnDefType.UNDEF);
        stateChanged(CN_ORIGINALDISPNUM, UnDefType.UNDEF);
        stateChanged(CN_OUTPUT, UnDefType.UNDEF);
        stateChanged(CN_PARENTALCOUNTRY, UnDefType.UNDEF);
        stateChanged(CN_PARENTALRATING, UnDefType.UNDEF);
        stateChanged(CN_PARENTALSYSTEM, UnDefType.UNDEF);
        stateChanged(CN_PARENTINDEX, UnDefType.UNDEF);
        stateChanged(CN_PATH, UnDefType.UNDEF);
        stateChanged(CN_PLAYLISTNAME, UnDefType.UNDEF);
        stateChanged(CN_PODCASTNAME, UnDefType.UNDEF);
        stateChanged(CN_PRODUCTID, UnDefType.UNDEF);
        stateChanged(CN_PROGRAMMEDIATYPE, UnDefType.UNDEF);
        stateChanged(CN_PROGRAMNUM, UnDefType.UNDEF);
        stateChanged(CN_PROGRAMSERVICETYPE, UnDefType.UNDEF);
        stateChanged(CN_PROGRAMTITLE, UnDefType.UNDEF);
        stateChanged(CN_REMOTEPLAYTYPE, UnDefType.UNDEF);
        stateChanged(CN_REPEATTYPE, UnDefType.UNDEF);
        stateChanged(CN_SERVICE, UnDefType.UNDEF);
        stateChanged(CN_SIZEMB, UnDefType.UNDEF);
        stateChanged(CN_SOURCE, UnDefType.UNDEF);
        stateChanged(CN_SOURCELABEL, UnDefType.UNDEF);
        stateChanged(CN_STATE, UnDefType.UNDEF);
        stateChanged(CN_STATESUPPLEMENT, UnDefType.UNDEF);
        stateChanged(CN_STARTDATETIME, UnDefType.UNDEF);
        stateChanged(CN_STORAGEURI, UnDefType.UNDEF);
        stateChanged(CN_SUBTITLELANGUAGE, UnDefType.UNDEF);
        stateChanged(CN_SUBTITLETITLE, UnDefType.UNDEF);
        stateChanged(CN_SYNCCONTENTPRIORITY, UnDefType.UNDEF);
        stateChanged(CN_TITLE, UnDefType.UNDEF);
        stateChanged(CN_TOTALCOUNT, UnDefType.UNDEF);
        stateChanged(CN_TRIPLETSTR, UnDefType.UNDEF);
        stateChanged(CN_USERCONTENTFLAG, UnDefType.UNDEF);
        stateChanged(CN_VIDEOCODEC, UnDefType.UNDEF);
        stateChanged(CN_VISIBILITY, UnDefType.UNDEF);
    }

    /**
     * Updates the content channels from the 1.0 result
     * 
     * @param clr the non-null content list result
     */
    private void notifyContentListResult(final ContentListResult_1_0 clr) {
        Objects.requireNonNull(clr, "clr cannot be null");

        stateChanged(CN_CHANNELNAME, SonyUtil.newStringType(clr.getChannelName()));

        stateChanged(CN_DIRECTREMOTENUM, SonyUtil.newDecimalType(clr.getDirectRemoteNum()));
        stateChanged(CN_DISPNUM, SonyUtil.newStringType(clr.getDispNum()));
        stateChanged(CN_DURATIONSEC, SonyUtil.newQuantityType(clr.getDurationSec(), SmartHomeUnits.SECOND));
        stateChanged(CN_FILESIZEBYTE, SonyUtil.newQuantityType(clr.getFileSizeByte(), SmartHomeUnits.BYTE));
        stateChanged(CN_ISALREADYPLAYED,
                SonyUtil.newStringType(Boolean.toString(BooleanUtils.toBoolean(clr.isAlreadyPlayed()))));
        stateChanged(CN_ISPROTECTED,
                SonyUtil.newStringType(Boolean.toString(BooleanUtils.toBoolean(clr.isProtected()))));
        stateChanged(CN_ORIGINALDISPNUM, SonyUtil.newStringType(clr.getOriginalDispNum()));

        stateChanged(CN_PROGRAMMEDIATYPE, SonyUtil.newStringType(clr.getProgramMediaType()));
        stateChanged(CN_PROGRAMNUM, SonyUtil.newDecimalType(clr.getProgramNum()));

        stateChanged(CN_STARTDATETIME, SonyUtil.newStringType(clr.getStartDateTime()));

        stateChanged(CN_TITLE, SonyUtil.newStringType(clr.getTitle()));
        stateChanged(CN_TRIPLETSTR, SonyUtil.newStringType(clr.getTripletStr()));
    }

    /**
     * Updates the content channels from the 1.2 result
     * 
     * @param clr the non-null content list result
     */
    private void notifyContentListResult(final ContentListResult_1_2 clr) {
        Objects.requireNonNull(clr, "clr cannot be null");

        stateChanged(CN_AUDIOCHANNEL, SonyUtil.newStringType(StringUtils.join(clr.getAudioChannel(), ',')));
        stateChanged(CN_AUDIOCODEC, SonyUtil.newStringType(StringUtils.join(clr.getAudioCodec(), ',')));
        stateChanged(CN_AUDIOFREQUENCY, SonyUtil.newStringType(StringUtils.join(clr.getAudioFrequency(), ',')));

        stateChanged(CN_CHANNELNAME, SonyUtil.newStringType(clr.getChannelName()));

        stateChanged(CN_CHANNELSURFINGVISIBILITY, SonyUtil.newStringType(clr.getChannelSurfingVisibility()));
        stateChanged(CN_EPGVISIBILITY, SonyUtil.newStringType(clr.getEpgVisibility()));
        stateChanged(CN_VISIBILITY, SonyUtil.newStringType(clr.getVisibility()));

        stateChanged(CN_CHAPTERCOUNT, SonyUtil.newDecimalType(clr.getChapterCount()));
        stateChanged(CN_CONTENTTYPE, SonyUtil.newStringType(clr.getContentType()));
        stateChanged(CN_CREATEDTIME, SonyUtil.newStringType(clr.getCreatedTime()));

        stateChanged(CN_DIRECTREMOTENUM, SonyUtil.newDecimalType(clr.getDirectRemoteNum()));
        stateChanged(CN_DISPNUM, SonyUtil.newStringType(clr.getDispNum()));
        stateChanged(CN_DURATIONSEC, SonyUtil.newQuantityType(clr.getDurationSec(), SmartHomeUnits.SECOND));
        stateChanged(CN_FILESIZEBYTE, SonyUtil.newQuantityType(clr.getFileSizeByte(), SmartHomeUnits.BYTE));
        stateChanged(CN_ISALREADYPLAYED,
                SonyUtil.newStringType(Boolean.toString(BooleanUtils.toBoolean(clr.isAlreadyPlayed()))));
        stateChanged(CN_ISPROTECTED,
                SonyUtil.newStringType(Boolean.toString(BooleanUtils.toBoolean(clr.isProtected()))));
        stateChanged(CN_ORIGINALDISPNUM, SonyUtil.newStringType(clr.getOriginalDispNum()));

        stateChanged(CN_PARENTALCOUNTRY, SonyUtil.newStringType(StringUtils.join(clr.getParentalCountry(), ',')));
        stateChanged(CN_PARENTALRATING, SonyUtil.newStringType(StringUtils.join(clr.getParentalRating(), ',')));
        stateChanged(CN_PARENTALSYSTEM, SonyUtil.newStringType(StringUtils.join(clr.getParentalSystem(), ',')));

        stateChanged(CN_PRODUCTID, SonyUtil.newStringType(clr.getProductID()));
        stateChanged(CN_PROGRAMMEDIATYPE, SonyUtil.newStringType(clr.getProgramMediaType()));
        stateChanged(CN_PROGRAMNUM, SonyUtil.newDecimalType(clr.getProgramNum()));
        stateChanged(CN_SIZEMB, SonyUtil.newQuantityType(clr.getSizeMB(), MetricPrefix.MEGA(SmartHomeUnits.BYTE)));

        stateChanged(CN_STARTDATETIME, SonyUtil.newStringType(clr.getStartDateTime()));
        stateChanged(CN_STORAGEURI, SonyUtil.newStringType(clr.getStorageUri()));

        stateChanged(CN_SUBTITLELANGUAGE, SonyUtil.newStringType(StringUtils.join(clr.getSubtitleLanguage(), ',')));
        stateChanged(CN_SUBTITLETITLE, SonyUtil.newStringType(StringUtils.join(clr.getSubtitleTitle(), ',')));

        stateChanged(CN_TITLE, SonyUtil.newStringType(clr.getTitle()));
        stateChanged(CN_TRIPLETSTR, SonyUtil.newStringType(clr.getTripletStr()));
        stateChanged(CN_USERCONTENTFLAG, SonyUtil.newBooleanType(clr.isUserContentFlag()));

        stateChanged(CN_VIDEOCODEC, SonyUtil.newStringType(clr.getVideoCodec()));
    }

    /**
     * Updates the content channels from the 1.4 result
     * 
     * @param clr the non-null content list result
     */
    private void notifyContentListResult(final ContentListResult_1_4 clr) {
        Objects.requireNonNull(clr, "clr cannot be null");

        stateChanged(CN_ALBUMNAME, SonyUtil.newStringType(clr.getAlbumName()));
        stateChanged(CN_ARTIST, SonyUtil.newStringType(clr.getArtist()));

        final AudioInfo[] audioInfo = clr.getAudioInfo();
        if (audioInfo != null) {
            stateChanged(CN_AUDIOCHANNEL, SonyUtil.newStringType(
                    Arrays.stream(audioInfo).map(ai -> ai.getChannel()).collect(Collectors.joining(","))));
            stateChanged(CN_AUDIOCODEC, SonyUtil
                    .newStringType(Arrays.stream(audioInfo).map(ai -> ai.getCodec()).collect(Collectors.joining(","))));
            stateChanged(CN_AUDIOFREQUENCY, SonyUtil.newStringType(
                    Arrays.stream(audioInfo).map(ai -> ai.getFrequency()).collect(Collectors.joining(","))));
        }

        stateChanged(CN_BROADCASTFREQ, SonyUtil.newQuantityType(clr.getBroadcastFreq(), SmartHomeUnits.HERTZ));
        stateChanged(CN_BROADCASTFREQBAND, SonyUtil.newStringType(clr.getBroadcastFreqBand()));
        stateChanged(CN_CHANNELNAME, SonyUtil.newStringType(clr.getChannelName()));

        stateChanged(CN_CHANNELSURFINGVISIBILITY, SonyUtil.newStringType(clr.getChannelSurfingVisibility()));
        stateChanged(CN_EPGVISIBILITY, SonyUtil.newStringType(clr.getEpgVisibility()));
        stateChanged(CN_VISIBILITY, SonyUtil.newStringType(clr.getVisibility()));

        stateChanged(CN_CHAPTERCOUNT, SonyUtil.newDecimalType(clr.getChapterCount()));
        stateChanged(CN_CONTENTKIND, SonyUtil.newStringType(clr.getContentKind()));
        stateChanged(CN_CONTENTTYPE, SonyUtil.newStringType(clr.getContentType()));
        stateChanged(CN_CREATEDTIME, SonyUtil.newStringType(clr.getCreatedTime()));

        stateChanged(CN_DIRECTREMOTENUM, SonyUtil.newDecimalType(clr.getDirectRemoteNum()));
        stateChanged(CN_DISPNUM, SonyUtil.newStringType(clr.getDispNum()));
        stateChanged(CN_DURATIONMSEC,
                SonyUtil.newQuantityType(clr.getDurationMSec(), MetricPrefix.MILLI(SmartHomeUnits.SECOND)));
        stateChanged(CN_FILENO, SonyUtil.newStringType(clr.getFileNo()));
        stateChanged(CN_FILESIZEBYTE, SonyUtil.newQuantityType(clr.getFileSizeByte(), SmartHomeUnits.BYTE));
        stateChanged(CN_FOLDERNO, SonyUtil.newStringType(clr.getFolderNo()));
        stateChanged(CN_GENRE, SonyUtil.newStringType(clr.getGenre()));
        stateChanged(CN_IS3D, SonyUtil.newStringType(clr.is3D()));
        stateChanged(CN_ISALREADYPLAYED, SonyUtil.newStringType(clr.isAlreadyPlayed()));
        stateChanged(CN_ISBROWSABLE, SonyUtil.newStringType(clr.isBrowsable()));
        stateChanged(CN_ISPLAYABLE, SonyUtil.newStringType(clr.isPlayable()));
        stateChanged(CN_ISPROTECTED, SonyUtil.newStringType(clr.isProtected()));
        stateChanged(CN_ORIGINALDISPNUM, SonyUtil.newStringType(clr.getOriginalDispNum()));

        final ParentalInfo[] pis = clr.getParentalInfo();
        if (pis != null) {
            stateChanged(CN_PARENTALCOUNTRY, SonyUtil
                    .newStringType(Arrays.stream(pis).map(pi -> pi.getCountry()).collect(Collectors.joining(","))));
            stateChanged(CN_PARENTALRATING, SonyUtil
                    .newStringType(Arrays.stream(pis).map(pi -> pi.getRating()).collect(Collectors.joining(","))));
            stateChanged(CN_PARENTALSYSTEM, SonyUtil
                    .newStringType(Arrays.stream(pis).map(pi -> pi.getSystem()).collect(Collectors.joining(","))));
        }
        stateChanged(CN_PARENTINDEX, SonyUtil.newDecimalType(clr.getParentIndex()));
        stateChanged(CN_PATH, SonyUtil.newStringType(clr.getPath()));
        stateChanged(CN_PLAYLISTNAME, SonyUtil.newStringType(clr.getPlaylistName()));
        stateChanged(CN_PODCASTNAME, SonyUtil.newStringType(clr.getPodcastName()));
        stateChanged(CN_PRODUCTID, SonyUtil.newStringType(clr.getProductID()));
        stateChanged(CN_PROGRAMMEDIATYPE, SonyUtil.newStringType(clr.getProgramMediaType()));
        stateChanged(CN_PROGRAMNUM, SonyUtil.newDecimalType(clr.getProgramNum()));
        stateChanged(CN_REMOTEPLAYTYPE, SonyUtil.newStringType(clr.getRemotePlayType()));
        stateChanged(CN_SIZEMB, SonyUtil.newQuantityType(clr.getSizeMB(), MetricPrefix.MEGA(SmartHomeUnits.BYTE)));

        stateChanged(CN_STARTDATETIME, SonyUtil.newStringType(clr.getStartDateTime()));
        stateChanged(CN_STORAGEURI, SonyUtil.newStringType(clr.getStorageUri()));

        final SubtitleInfo[] subInfos = clr.getSubtitleInfo();
        if (subInfos != null) {
            stateChanged(CN_SUBTITLELANGUAGE, SonyUtil.newStringType(
                    Arrays.stream(subInfos).map(subi -> subi.getLangauge()).collect(Collectors.joining(","))));
            stateChanged(CN_SUBTITLETITLE, SonyUtil.newStringType(
                    Arrays.stream(subInfos).map(subi -> subi.getTitle()).collect(Collectors.joining(","))));
        }
        stateChanged(CN_TITLE, SonyUtil.newStringType(clr.getTitle()));
        stateChanged(CN_TRIPLETSTR, SonyUtil.newStringType(clr.getTripletStr()));
        stateChanged(CN_USERCONTENTFLAG, SonyUtil.newBooleanType(clr.getUserContentFlag()));

        final VideoInfo vis = clr.getVideoInfo();
        if (vis != null) {
            stateChanged(CN_VIDEOCODEC, SonyUtil.newStringType(vis.getCodec()));
        }
    }

    /**
     * Updates the content channels from the 1.5 result
     * 
     * @param clr the non-null content list result
     */
    private void notifyContentListResult(final ContentListResult_1_5 clr) {
        Objects.requireNonNull(clr, "clr cannot be null");
        stateChanged(CN_ALBUMNAME, SonyUtil.newStringType(clr.getAlbumName()));
        stateChanged(CN_APPLICATIONNAME, SonyUtil.newStringType(clr.getApplicationName()));
        stateChanged(CN_ARTIST, SonyUtil.newStringType(clr.getArtist()));

        final AudioInfo[] audioInfo = clr.getAudioInfo();
        if (audioInfo != null) {
            stateChanged(CN_AUDIOCHANNEL, SonyUtil.newStringType(
                    Arrays.stream(audioInfo).map(ai -> ai.getChannel()).collect(Collectors.joining(","))));
            stateChanged(CN_AUDIOCODEC, SonyUtil
                    .newStringType(Arrays.stream(audioInfo).map(ai -> ai.getCodec()).collect(Collectors.joining(","))));
            stateChanged(CN_AUDIOFREQUENCY, SonyUtil.newStringType(
                    Arrays.stream(audioInfo).map(ai -> ai.getFrequency()).collect(Collectors.joining(","))));
        }

        final BivlInfo bivlInfo = clr.getBivlInfo();
        if (bivlInfo != null) {
            stateChanged(CN_BIVLSERVICEID, SonyUtil.newStringType(bivlInfo.getServiceId()));
            stateChanged(CN_BIVLASSETID, SonyUtil.newStringType(bivlInfo.getAssetId()));
            stateChanged(CN_BIVLPROVIDER, SonyUtil.newStringType(bivlInfo.getProvider()));
        }

        final BroadcastFreq bf = clr.getBroadcastFreq();
        if (bf != null) {
            stateChanged(CN_BROADCASTFREQ, SonyUtil.newQuantityType(bf.getFrequency(), SmartHomeUnits.HERTZ));
            stateChanged(CN_BROADCASTFREQBAND, SonyUtil.newStringType(bf.getBand()));
        }
        stateChanged(CN_CHANNELNAME, SonyUtil.newStringType(clr.getChannelName()));

        final Visibility visibility = clr.getVisibility();
        if (visibility != null) {
            stateChanged(CN_CHANNELSURFINGVISIBILITY, SonyUtil.newStringType(visibility.getChannelSurfingVisibility()));
            stateChanged(CN_EPGVISIBILITY, SonyUtil.newStringType(visibility.getEpgVisibility()));
            stateChanged(CN_VISIBILITY, SonyUtil.newStringType(visibility.getVisibility()));
        }

        stateChanged(CN_CHAPTERCOUNT, SonyUtil.newDecimalType(clr.getChapterCount()));
        stateChanged(CN_CHAPTERINDEX, SonyUtil.newDecimalType(clr.getChapterIndex()));
        stateChanged(CN_CLIPCOUNT, SonyUtil.newDecimalType(clr.getClipCount()));
        stateChanged(CN_CONTENTKIND, SonyUtil.newStringType(clr.getContentKind()));
        stateChanged(CN_CONTENTTYPE, SonyUtil.newStringType(clr.getContentType()));
        stateChanged(CN_CREATEDTIME, SonyUtil.newStringType(clr.getCreatedTime()));

        final DabInfo dab = clr.getDabInfo();
        if (dab != null) {
            stateChanged(CN_DABCOMPONENTLABEL, SonyUtil.newStringType(dab.getComponentLabel()));
            stateChanged(CN_DABDYNAMICLABEL, SonyUtil.newStringType(dab.getDynamicLabel()));
            stateChanged(CN_DABENSEMBLELABEL, SonyUtil.newStringType(dab.getEnsembleLabel()));
            stateChanged(CN_DABSERVICELABEL, SonyUtil.newStringType(dab.getServiceLabel()));
        }

        // TODO: find out description format
        // final Description desc = clr.getDescription();
        // if (desc != null) {
        // stateChanged(CN_DESCRIPTION, SonyUtil.newStringType(desc.));
        // }
        stateChanged(CN_DIRECTREMOTENUM, SonyUtil.newDecimalType(clr.getDirectRemoteNum()));
        stateChanged(CN_DISPNUM, SonyUtil.newStringType(clr.getDispNum()));

        final Duration duration = clr.getDuration();
        if (duration != null) {
            stateChanged(CN_DURATIONMSEC,
                    SonyUtil.newQuantityType(duration.getMillseconds(), MetricPrefix.MILLI(SmartHomeUnits.SECOND)));
            stateChanged(CN_DURATIONSEC, SonyUtil.newQuantityType(duration.getSeconds(), SmartHomeUnits.SECOND));
        }

        stateChanged(CN_EVENTID, SonyUtil.newStringType(clr.getEventId()));
        stateChanged(CN_FILENO, SonyUtil.newStringType(clr.getFileNo()));
        stateChanged(CN_FILESIZEBYTE, SonyUtil.newQuantityType(clr.getFileSizeByte(), SmartHomeUnits.BYTE));
        stateChanged(CN_FOLDERNO, SonyUtil.newStringType(clr.getFolderNo()));
        stateChanged(CN_GENRE, SonyUtil.newStringType(clr.getGenre()));
        stateChanged(CN_GLOBALPLAYBACKCOUNT, SonyUtil.newDecimalType(clr.getGlobalPlaybackCount()));
        stateChanged(CN_HASRESUME, SonyUtil.newStringType(clr.getHasResume()));
        stateChanged(CN_IS3D, SonyUtil.newStringType(clr.is3D()));
        stateChanged(CN_IS4K, SonyUtil.newStringType(clr.is4K()));
        stateChanged(CN_ISALREADYPLAYED, SonyUtil.newStringType(clr.isAlreadyPlayed()));
        stateChanged(CN_ISAUTODELETE, SonyUtil.newStringType(clr.isAutoDelete()));
        stateChanged(CN_ISBROWSABLE, SonyUtil.newStringType(clr.isBrowsable()));
        stateChanged(CN_ISNEW, SonyUtil.newStringType(clr.isNew()));
        stateChanged(CN_ISPLAYABLE, SonyUtil.newStringType(clr.isPlayable()));
        stateChanged(CN_ISPLAYLIST, SonyUtil.newStringType(clr.isPlaylist()));
        stateChanged(CN_ISPROTECTED, SonyUtil.newStringType(clr.isProtected()));
        stateChanged(CN_ISSOUNDPHOTO, SonyUtil.newStringType(clr.isSoundPhoto()));
        stateChanged(CN_MEDIATYPE, SonyUtil.newStringType(clr.getMediaType()));
        stateChanged(CN_ORIGINALDISPNUM, SonyUtil.newStringType(clr.getOriginalDispNum()));
        stateChanged(CN_OUTPUT, SonyUtil.newStringType(clr.getOutput()));

        final ParentalInfo[] pis = clr.getParentalInfo();
        if (pis != null) {
            stateChanged(CN_PARENTALCOUNTRY, SonyUtil
                    .newStringType(Arrays.stream(pis).map(pi -> pi.getCountry()).collect(Collectors.joining(","))));
            stateChanged(CN_PARENTALRATING, SonyUtil
                    .newStringType(Arrays.stream(pis).map(pi -> pi.getRating()).collect(Collectors.joining(","))));
            stateChanged(CN_PARENTALSYSTEM, SonyUtil
                    .newStringType(Arrays.stream(pis).map(pi -> pi.getSystem()).collect(Collectors.joining(","))));
        }
        stateChanged(CN_PARENTINDEX, SonyUtil.newDecimalType(clr.getParentIndex()));
        stateChanged(CN_PLAYLISTNAME, SonyUtil.newStringType(clr.getPlaylistName()));
        stateChanged(CN_PODCASTNAME, SonyUtil.newStringType(clr.getPodcastName()));
        stateChanged(CN_PRODUCTID, SonyUtil.newStringType(clr.getProductID()));
        stateChanged(CN_PROGRAMMEDIATYPE, SonyUtil.newStringType(clr.getProgramMediaType()));
        stateChanged(CN_PROGRAMNUM, SonyUtil.newDecimalType(clr.getProgramNum()));
        stateChanged(CN_PROGRAMSERVICETYPE, SonyUtil.newStringType(clr.getProgramServiceType()));
        stateChanged(CN_PROGRAMTITLE, SonyUtil.newStringType(clr.getProgramTitle()));
        stateChanged(CN_REMOTEPLAYTYPE, SonyUtil.newStringType(clr.getRemotePlayType()));
        stateChanged(CN_REPEATTYPE, SonyUtil.newStringType(clr.getRepeatType()));
        stateChanged(CN_SERVICE, SonyUtil.newStringType(clr.getService()));
        stateChanged(CN_SIZEMB, SonyUtil.newQuantityType(clr.getSizeMB(), MetricPrefix.MEGA(SmartHomeUnits.BYTE)));
        stateChanged(CN_SOURCE, SonyUtil.newStringType(clr.getSource()));
        stateChanged(CN_SOURCELABEL, SonyUtil.newStringType(clr.getSourceLabel()));

        final StateInfo si = clr.getStateInfo();
        if (si != null) {
            stateChanged(CN_STATE, SonyUtil.newStringType(si.getState()));
            stateChanged(CN_STATESUPPLEMENT, SonyUtil.newStringType(si.getSupplement()));
        }
        stateChanged(CN_STARTDATETIME, SonyUtil.newStringType(clr.getStartDateTime()));
        stateChanged(CN_STORAGEURI, SonyUtil.newStringType(clr.getStorageUri()));

        final SubtitleInfo[] subInfos = clr.getSubtitleInfo();
        if (subInfos != null) {
            stateChanged(CN_SUBTITLELANGUAGE, SonyUtil.newStringType(
                    Arrays.stream(subInfos).map(subi -> subi.getLangauge()).collect(Collectors.joining(","))));
            stateChanged(CN_SUBTITLETITLE, SonyUtil.newStringType(
                    Arrays.stream(subInfos).map(subi -> subi.getTitle()).collect(Collectors.joining(","))));
        }
        stateChanged(CN_SYNCCONTENTPRIORITY, SonyUtil.newStringType(clr.getSyncContentPriority()));
        stateChanged(CN_TITLE, SonyUtil.newStringType(clr.getTitle()));
        stateChanged(CN_TOTALCOUNT, SonyUtil.newDecimalType(clr.getTotalCount()));
        stateChanged(CN_TRIPLETSTR, SonyUtil.newStringType(clr.getTripletStr()));
        stateChanged(CN_USERCONTENTFLAG, SonyUtil.newBooleanType(clr.getUserContentFlag()));

        final VideoInfo[] vis = clr.getVideoInfo();
        if (vis != null) {
            stateChanged(CN_VIDEOCODEC, SonyUtil
                    .newStringType(Arrays.stream(vis).map(vi -> vi.getCodec()).collect(Collectors.joining(","))));
        }
    }

    /**
     * Handle the notification of a terminal status (and update all related channels)
     * 
     * @param term a non-null terminal status
     */
    private void notifyCurrentTerminalStatus(final CurrentExternalTerminalsStatus_1_0 term) {
        Objects.requireNonNull(term, "term cannot be null");
        final String termUri = term.getUri();
        for (final ScalarWebChannel chnl : getChannelTracker()
                .getLinkedChannelsForCategory(TERM_URI, TERM_TITLE, TERM_CONNECTION, TERM_LABEL, TERM_ICON, TERM_ACTIVE)
                .stream().toArray(ScalarWebChannel[]::new)) {
            if (StringUtils.equalsIgnoreCase(termUri, chnl.getPathPart(0))) {
                notifyCurrentTerminalStatus(chnl, term);
            }
        }
    }

    /**
     * Notify the notification of a terminal status for a specific channel
     * 
     * @param channel a non-null channel
     */
    private void notifyCurrentTerminalStatus(final ScalarWebChannel channel) {
        Objects.requireNonNull(channel, "channel cannot be null");
        try {
            for (final CurrentExternalTerminalsStatus_1_0 term : execute(
                    ScalarWebMethod.GETCURRENTEXTERNALTERMINALSSTATUS)
                            .asArray(CurrentExternalTerminalsStatus_1_0.class)) {
                final String termUri = term.getUri();
                if (StringUtils.equalsIgnoreCase(termUri, channel.getPathPart(0))) {
                    notifyCurrentTerminalStatus(channel, term);
                }
            }
        } catch (final IOException e) {
            logger.debug("Error notify current terminal status {}", e.getMessage());
        }
    }

    /**
     * Handl notification of a terminal status for a specific channel
     * 
     * @param channel a non-null channel
     * @param cets a non-null terminal status
     */
    private void notifyCurrentTerminalStatus(final ScalarWebChannel channel,
            final CurrentExternalTerminalsStatus_1_0 cets) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(cets, "cets cannot be null");

        final String id = channel.getId();
        stateChanged(TERM_TITLE, id, SonyUtil.newStringType(cets.getTitle()));
        stateChanged(TERM_CONNECTION, id, SonyUtil.newStringType(cets.getConnection()));
        stateChanged(TERM_LABEL, id, SonyUtil.newStringType(cets.getLabel()));
        stateChanged(TERM_ACTIVE, id, SonyUtil.newStringType(cets.getActive()));

        final String iconUrl = cets.getIconUrl();
        if (iconUrl == null || StringUtils.isEmpty(iconUrl)) {
            stateChanged(TERM_ICON, id, UnDefType.UNDEF);
        } else {
            try (SonyHttpTransport transport = SonyTransportFactory
                    .createHttpTransport(getService().getTransport().getBaseUri().toString())) {
                final RawType rawType = NetUtil.getRawType(transport, iconUrl);
                stateChanged(TERM_ICON, id, rawType == null ? UnDefType.UNDEF : rawType);
            } catch (URISyntaxException e) {
                logger.debug("Exception occurred getting application icon: {}", e.getMessage());
            }
        }
    }

    /**
     * Handle notification of an input status (v1.0) for a specific channel
     * 
     * @param channel a non-null channel
     * @param status a non-null status
     */
    private void notifyInputStatus(final ScalarWebChannel channel, final CurrentExternalInputsStatus_1_0 status) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(status, "status cannot be null");

        final String id = channel.getId();
        stateChanged(IN_TITLE, id, SonyUtil.newStringType(status.getTitle()));
        stateChanged(IN_CONNECTION, id, status.isConnection() ? OnOffType.ON : OnOffType.OFF);
        stateChanged(IN_LABEL, id, SonyUtil.newStringType(status.getLabel()));
        stateChanged(IN_ICON, id, SonyUtil.newStringType(status.getIcon()));
    }

    /**
     * Handle notification of an input status (v1.1) for a specific channel
     * 
     * @param channel a non-null channel
     * @param status a non-null status
     */
    private void notifyInputStatus(final ScalarWebChannel channel, final CurrentExternalInputsStatus_1_1 status) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(status, "status cannot be null");

        notifyInputStatus(channel, (CurrentExternalInputsStatus_1_0) status);

        final String id = channel.getId();
        stateChanged(IN_STATUS, id, SonyUtil.newStringType(status.getStatus()));
    }

    /**
     * Handle notification of an parental rating (v1.0)
     * 
     * @param prs the non-null parental rating
     */
    private void notifyParentalRating(final ParentalRatingSetting_1_0 prs) {
        Objects.requireNonNull(prs, "prs cannot be null");
        stateChanged(PR_RATINGTYPEAGE, SonyUtil.newDecimalType(prs.getRatingTypeAge()));
        stateChanged(PR_RATINGTYPESONY, SonyUtil.newStringType(prs.getRatingTypeSony()));
        stateChanged(PR_RATINGCOUNTRY, SonyUtil.newStringType(prs.getRatingCountry()));
        stateChanged(PR_RATINGCUSTOMTYPETV, SonyUtil.newStringType(StringUtils.join(prs.getRatingCustomTypeTV())));
        stateChanged(PR_RATINGCUSTOMTYPEMPAA, SonyUtil.newStringType(prs.getRatingCustomTypeMpaa()));
        stateChanged(PR_RATINGCUSTOMTYPECAENGLISH, SonyUtil.newStringType(prs.getRatingCustomTypeCaEnglish()));
        stateChanged(PR_RATINGCUSTOMTYPECAFRENCH, SonyUtil.newStringType(prs.getRatingCustomTypeCaFrench()));
        stateChanged(PR_UNRATEDLOCK, prs.isUnratedLock() ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Handle notification of an playing content (v1.0) for a specific output
     * 
     * @param pci the non-null playing content info
     * @param id the non-null, non-empty output
     */
    private void notifyPlayingContentInfo(final PlayingContentInfoResult_1_0 pci, final String id) {
        Objects.requireNonNull(pci, "pic cannot be null");
        Validate.notEmpty(id, "id cannot be null");
        stateChanged(PL_BIVLASSETID, id, SonyUtil.newStringType(pci.getBivlAssetId()));
        stateChanged(PL_BIVLPROVIDER, id, SonyUtil.newStringType(pci.getBivlProvider()));
        stateChanged(PL_BIVLSERVICEID, id, SonyUtil.newStringType(pci.getBivlServiceId()));
        stateChanged(PL_DISPNUM, id, SonyUtil.newStringType(pci.getDispNum()));
        stateChanged(PL_DURATIONSEC, id, SonyUtil.newQuantityType(pci.getDurationSec(), SmartHomeUnits.SECOND));
        stateChanged(PL_MEDIATYPE, id, SonyUtil.newStringType(pci.getMediaType()));
        stateChanged(PL_ORIGINALDISPNUM, id, SonyUtil.newStringType(pci.getOriginalDispNum()));
        stateChanged(PL_PLAYSPEED, id, SonyUtil.newStringType(pci.getPlaySpeed()));
        stateChanged(PL_PROGRAMNUM, id, SonyUtil.newDecimalType(pci.getProgramNum()));
        stateChanged(PL_PROGRAMTITLE, id, SonyUtil.newStringType(pci.getProgramTitle()));
        stateChanged(PL_SOURCE, id, SonyUtil.newStringType(pci.getSource()));
        stateChanged(PL_STARTDATETIME, id, SonyUtil.newStringType(pci.getStartDateTime()));
        stateChanged(PL_TITLE, id, SonyUtil.newStringType(pci.getTitle()));
        stateChanged(PL_TRIPLETSTR, id, SonyUtil.newStringType(pci.getTripletStr()));

        final String pciSourceUri = pci.getSource();
        if (pciSourceUri != null && StringUtils.isNotEmpty(pciSourceUri)) {
            final String pciScheme = Source.getSchemePart(pciSourceUri);
            final String pciSource = Source.getSourcePart(pciSourceUri);

            // only do the following for TV or RADIO schemes
            if (pciScheme != null && (StringUtils.equalsIgnoreCase(pciScheme, Scheme.TV)
                    || StringUtils.equalsIgnoreCase(pciScheme, Scheme.RADIO))) {
                for (final Source src : getSources()) {
                    final String srcScheme = src.getSchemePart();
                    final String srcSource = src.getSourcePart();

                    // if we have the same scheme...
                    if (pciSource != null && srcScheme != null && srcSource != null
                            && StringUtils.equalsIgnoreCase(pciScheme, srcScheme)) {
                        // set the value if the same source, otherwise move undef to it
                        stateChanged(PS_CHANNEL, srcSource,
                                StringUtils.equalsIgnoreCase(srcSource, pciSource)
                                        ? SonyUtil.newStringType(pci.getDispNum())
                                        : UnDefType.UNDEF);
                    }
                }
            }
        }

        final String sourceUri = pci.getUri();
        if (sourceUri != null && StringUtils.isNotEmpty(sourceUri)) {
            // statePlaying.put(output, new PlayingState(sourceUri, preset));
            statePlaying.compute(id, (k, v) -> {
                if (v == null) {
                    int preset = 1;
                    final Matcher m = Source.RADIOPATTERN.matcher(sourceUri);
                    if (m.matches() && m.groupCount() > 1) {
                        try {
                            preset = Integer.parseInt(m.group(2));
                        } catch (final NumberFormatException e) {
                            logger.debug("Radio preset number is not a valid number: {}", sourceUri);
                        }
                    }
                    return new PlayingState(sourceUri, preset);
                } else {
                    return new PlayingState(sourceUri, v.getPreset());
                }
            });
            stateChanged(PL_URI, id, SonyUtil.newStringType(sourceUri));
            // stateChanged(PL_PRESET, id, SonyUtil.newDecimalType(preset));

            stateChanged(TERM_SOURCE, id, SonyUtil.newStringType(getSourceFromUri(sourceUri)));
        }
    }

    /**
     * Handle notification of an playing content (v1.2) for a specific output
     * 
     * @param pci the non-null playing content info
     * @param id the non-null, non-empty output
     */
    private void notifyPlayingContentInfo(final PlayingContentInfoResult_1_2 pci, final String id) {
        Objects.requireNonNull(pci, "pic cannot be null");
        Validate.notEmpty(id, "id cannot be null");
        notifyPlayingContentInfo((PlayingContentInfoResult_1_0) pci, id);

        stateChanged(PL_ALBUMNAME, id, SonyUtil.newStringType(pci.getAlbumName()));
        stateChanged(PL_APPLICATIONNAME, id, SonyUtil.newStringType(pci.getApplicationName()));
        stateChanged(PL_ARTIST, id, SonyUtil.newStringType(pci.getArtist()));

        final AudioInfo[] ais = pci.getAudioInfo();
        stateChanged(PL_AUDIOCHANNEL, id, SonyUtil.newStringType(
                ais == null ? null : Arrays.stream(ais).map(a -> a.getChannel()).collect(Collectors.joining(","))));
        stateChanged(PL_AUDIOCODEC, id, SonyUtil.newStringType(
                ais == null ? null : Arrays.stream(ais).map(a -> a.getCodec()).collect(Collectors.joining(","))));
        stateChanged(PL_AUDIOFREQUENCY, id, SonyUtil.newStringType(
                ais == null ? null : Arrays.stream(ais).map(a -> a.getFrequency()).collect(Collectors.joining(","))));

        stateChanged(PL_BROADCASTFREQ, id, SonyUtil.newQuantityType(pci.getBroadcastFreq(), SmartHomeUnits.HERTZ));
        stateChanged(PL_BROADCASTFREQBAND, id, SonyUtil.newStringType(pci.getBroadcastFreqBand()));
        stateChanged(PL_CHANNELNAME, id, SonyUtil.newStringType(pci.getChannelName()));
        stateChanged(PL_CHAPTERCOUNT, id, SonyUtil.newDecimalType(pci.getChapterCount()));
        stateChanged(PL_CHAPTERINDEX, id, SonyUtil.newDecimalType(pci.getChapterIndex()));
        stateChanged(PL_CONTENTKIND, id, SonyUtil.newStringType(pci.getContentKind()));

        final DabInfo di = pci.getDabInfo();
        stateChanged(PL_DABCOMPONENTLABEL, id, SonyUtil.newStringType(di == null ? null : di.getComponentLabel()));
        stateChanged(PL_DABDYNAMICLABEL, id, SonyUtil.newStringType(di == null ? null : di.getDynamicLabel()));
        stateChanged(PL_DABENSEMBLELABEL, id, SonyUtil.newStringType(di == null ? null : di.getEnsembleLabel()));
        stateChanged(PL_DABSERVICELABEL, id, SonyUtil.newStringType(di == null ? null : di.getServiceLabel()));

        stateChanged(PL_DURATIONMSEC, id,
                SonyUtil.newQuantityType(pci.getDurationMsec(), MetricPrefix.MILLI(SmartHomeUnits.SECOND)));
        stateChanged(PL_FILENO, id, SonyUtil.newStringType(pci.getFileNo()));
        stateChanged(PL_GENRE, id, SonyUtil.newStringType(pci.getGenre()));
        stateChanged(PL_INDEX, id, SonyUtil.newDecimalType(pci.getIndex()));
        stateChanged(PL_IS3D, id, SonyUtil.newStringType(pci.getIs3D()));
        stateChanged(PL_OUTPUT, id, SonyUtil.newStringType(pci.getOutput()));
        stateChanged(PL_PARENTINDEX, id, SonyUtil.newDecimalType(pci.getParentIndex()));
        stateChanged(PL_PARENTURI, id, SonyUtil.newStringType(pci.getParentUri()));
        stateChanged(PL_PATH, id, SonyUtil.newStringType(pci.getPath()));
        stateChanged(PL_PLAYLISTNAME, id, SonyUtil.newStringType(pci.getPlaylistName()));
        stateChanged(PL_PLAYSTEPSPEED, id, SonyUtil.newDecimalType(pci.getPlayStepSpeed()));
        stateChanged(PL_PODCASTNAME, id, SonyUtil.newStringType(pci.getPodcastName()));
        stateChanged(PL_POSITIONMSEC, id,
                SonyUtil.newQuantityType(pci.getPositionMsec(), MetricPrefix.MILLI(SmartHomeUnits.SECOND)));
        stateChanged(PL_POSITIONSEC, id, SonyUtil.newQuantityType(pci.getPositionSec(), SmartHomeUnits.SECOND));
        stateChanged(PL_REPEATTYPE, id, SonyUtil.newStringType(pci.getRepeatType()));
        stateChanged(PL_SERVICE, id, SonyUtil.newStringType(pci.getService()));
        stateChanged(PL_SOURCELABEL, id, SonyUtil.newStringType(pci.getSourceLabel()));

        final StateInfo si = pci.getStateInfo();
        stateChanged(PL_STATE, id, SonyUtil.newStringType(si == null ? null : si.getState()));
        stateChanged(PL_STATESUPPLEMENT, id, SonyUtil.newStringType(si == null ? null : si.getSupplement()));

        stateChanged(PL_SUBTITLEINDEX, id, SonyUtil.newDecimalType(pci.getSubtitleIndex()));
        stateChanged(PL_TOTALCOUNT, id, SonyUtil.newDecimalType(pci.getTotalCount()));

        final VideoInfo vi = pci.getVideoInfo();
        stateChanged(PL_VIDEOCODEC, id, SonyUtil.newStringType(vi == null ? null : vi.getCodec()));
    }

    @Override
    public void refreshChannel(final ScalarWebChannel channel) {
        Objects.requireNonNull(channel, "channel cannot be null");

        final String ctgy = channel.getCategory();
        if (StringUtils.equalsIgnoreCase(ctgy, SCHEMES)) {
            refreshSchemes();
        } else if (StringUtils.equalsIgnoreCase(ctgy, SOURCES)) {
            refreshSources();
        } else if (StringUtils.startsWith(ctgy, PARENTRATING)) {
            refreshParentalRating();
            refreshParentalRating();
        } else if (StringUtils.startsWith(ctgy, PLAYING)) {
            refreshPlayingContentInfo();
        } else if (StringUtils.startsWith(ctgy, INPUT)) {
            refreshCurrentExternalInputStatus(Collections.singleton(channel));
        } else if (StringUtils.startsWith(ctgy, TERM)) {
            notifyCurrentTerminalStatus(channel);
        } else if (StringUtils.startsWith(ctgy, CONTENT)) {
            refreshContent();
        } else if (StringUtils.equalsIgnoreCase(ctgy, BLUETOOTHSETTINGS)) {
            refreshGeneralSettings(Collections.singleton(channel), ScalarWebMethod.GETBLUETOOTHSETTINGS);
        } else if (StringUtils.equalsIgnoreCase(ctgy, PLAYBACKSETTINGS)) {
            refreshGeneralSettings(Collections.singleton(channel), ScalarWebMethod.GETPLAYBACKMODESETTINGS);
        } else if (StringUtils.equalsIgnoreCase(ctgy, PS_CHANNEL)) {
            refreshPresetChannelStateDescription(Collections.singletonList(channel));
        } else {
            logger.debug("Unknown refresh channel: {}", channel);
        }
    }

    /**
     * Refresh content
     */
    private void refreshContent() {
        final ContentState state = stateContent.get();

        if (StringUtils.isEmpty(state.getParentUri())) {
            notifyContentListResult();
        } else {
            Count ct;

            try {
                ct = execute(ScalarWebMethod.GETCONTENTCOUNT, version -> {
                    if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1,
                            ScalarWebMethod.V1_2)) {
                        return new ContentCount_1_0(state.getParentUri());
                    }
                    return new ContentCount_1_3(state.getParentUri());
                }).as(Count.class);

            } catch (final IOException e) {
                ct = new Count(-1);
            }

            // update child count
            stateChanged(CN_PARENTURI, SonyUtil.newStringType(state.getParentUri()));
            stateChanged(CN_CHILDCOUNT, SonyUtil.newDecimalType(ct.getCount()));

            try {
                final ScalarWebResult res = execute(ScalarWebMethod.GETCONTENTLIST, version -> {
                    if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1,
                            ScalarWebMethod.V1_2, ScalarWebMethod.V1_3)) {
                        return new ContentListRequest_1_0(state.getParentUri(), state.getIdx(), 1);
                    }
                    return new ContentListRequest_1_4(state.getParentUri(), state.getIdx(), 1);
                });

                String childUri = null;
                Integer childIdx = null;

                // For USB - if you ask for 1, you'll always get two results
                // 1. The actual index you asked for
                // 2. A row describing the storage itself (idx = -1)
                // so we need to filter for just our result
                final String version = getVersion(ScalarWebMethod.GETCONTENTLIST);
                if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1)) {
                    for (final ContentListResult_1_0 clr : res.asArray(ContentListResult_1_0.class)) {
                        if (clr.getIndex() == state.getIdx()) {
                            notifyContentListResult(clr);
                            childUri = clr.getUri();
                            childIdx = clr.getIndex();
                        }
                    }
                } else if (VersionUtilities.equals(version, ScalarWebMethod.V1_2, ScalarWebMethod.V1_3)) {
                    for (final ContentListResult_1_2 clr : res.asArray(ContentListResult_1_2.class)) {
                        if (clr.getIndex() == state.getIdx()) {
                            notifyContentListResult(clr);
                            childUri = clr.getUri();
                            childIdx = clr.getIndex();
                        }
                    }
                } else if (VersionUtilities.equals(version, ScalarWebMethod.V1_4)) {
                    for (final ContentListResult_1_4 clr : res.asArray(ContentListResult_1_4.class)) {
                        if (clr.getIndex() == state.getIdx()) {
                            notifyContentListResult(clr);
                            childUri = clr.getUri();
                            childIdx = clr.getIndex();
                        }
                    }
                } else if (VersionUtilities.equals(version, ScalarWebMethod.V1_5)) {
                    for (final ContentListResult_1_5 clr : res.asArray(ContentListResult_1_5.class)) {
                        if (clr.getIndex() == state.getIdx()) {
                            notifyContentListResult(clr);
                            childUri = clr.getUri();
                            childIdx = clr.getIndex();
                        }
                    }
                }

                if (childIdx != null && childUri != null) {
                    final String finalUri = childUri;
                    final Integer finalIdx = childIdx;
                    stateContent.updateAndGet(cs -> new ContentState(cs.getParentUri(), finalUri, finalIdx));
                }
                stateChanged(CN_URI, SonyUtil.newStringType(childUri));
                stateChanged(CN_INDEX, SonyUtil.newDecimalType(state.getIdx()));

            } catch (final IOException e) {
                notifyContentListResult();
            }
        }
    }

    /**
     * Refresh current external input status for a set of channels
     *
     * @param channels a non-null, possibly empty set of channels
     */
    private void refreshCurrentExternalInputStatus(final Set<ScalarWebChannel> channels) {
        Objects.requireNonNull(channels, "channels cannot be null");
        if (getService().hasMethod(ScalarWebMethod.GETCURRENTEXTERNALINPUTSSTATUS)) {
            try {
                final ScalarWebResult result = getInputStatus();
                final String version = getService().getVersion(ScalarWebMethod.GETCURRENTEXTERNALINPUTSSTATUS);

                if (VersionUtilities.equals(version, ScalarWebMethod.V1_0)) {
                    for (final CurrentExternalInputsStatus_1_0 inp : result
                            .asArray(CurrentExternalInputsStatus_1_0.class)) {
                        final String inpUri = inp.getUri();
                        for (final ScalarWebChannel chnl : channels) {
                            if (StringUtils.equalsIgnoreCase(inpUri, chnl.getPathPart(0))) {
                                notifyInputStatus(chnl, inp);
                            }
                        }
                    }
                } else if (VersionUtilities.equals(version, ScalarWebMethod.V1_1)) {
                    for (final CurrentExternalInputsStatus_1_1 inp : result
                            .asArray(CurrentExternalInputsStatus_1_1.class)) {
                        final String inpUri = inp.getUri();
                        for (final ScalarWebChannel chnl : channels) {
                            if (StringUtils.equalsIgnoreCase(inpUri, chnl.getPathPart(0))) {
                                notifyInputStatus(chnl, inp);
                            }
                        }
                    }
                }
            } catch (final IOException e) {
                logger.debug("Error refreshing current external input status {}", e.getMessage());
            }
        }
    }

    /**
     * Refresh current external input status
     */
    private void refreshCurrentExternalTerminalsStatus() {
        if (getService().hasMethod(ScalarWebMethod.GETCURRENTEXTERNALTERMINALSSTATUS)) {
            for (final CurrentExternalTerminalsStatus_1_0 term : getTerminalStatuses(true)) {
                notifyCurrentTerminalStatus(term);
            }
        }
    }

    /**
     * Refresh the parental rating
     */
    private void refreshParentalRating() {
        try {
            notifyParentalRating(getParentalRating().as(ParentalRatingSetting_1_0.class));
        } catch (final IOException e) {
            logger.debug("Exception occurred retrieving the parental rating setting: {}", e.getMessage());
        }
    }

    /**
     * Refresh the playing content info
     */
    private void refreshPlayingContentInfo() {
        try {
            final ScalarWebResult result = getPlayingContentInfo();
            final String version = getService().getVersion(ScalarWebMethod.GETPLAYINGCONTENTINFO);
            if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1)) {
                for (final PlayingContentInfoResult_1_0 res : result.asArray(PlayingContentInfoResult_1_0.class)) {
                    notifyPlayingContentInfo(res, getIdForOutput(MAINOUTPUT));
                }
            } else if (VersionUtilities.equals(version, ScalarWebMethod.V1_2)) {
                for (final PlayingContentInfoResult_1_2 res : result.asArray(PlayingContentInfoResult_1_2.class)) {
                    final String output = res.getOutput(MAINOUTPUT);
                    notifyPlayingContentInfo(res, getIdForOutput(output));
                }
            }
        } catch (final IOException e) {
            logger.debug("Error refreshing playing content info {}", e.getMessage());
        }
    }

    /**
     * Refresh the schemes
     */
    private void refreshSchemes() {
        final String schemes = getSchemes(true).stream().map(s -> s.getScheme()).collect(Collectors.joining(","));
        stateSources.clear(); // clear sources to reretrieve them since schemes changed
        stateChanged(SCHEMES, SonyUtil.newStringType(schemes));
    }

    /**
     * Refresh the sources
     */
    private void refreshSources() {
        final List<String> sources = new ArrayList<>();
        for (final Source src : getSources(true)) {
            final String source = src.getSource();
            if (source != null && StringUtils.isNotEmpty(source)) {
                sources.add(source);
            }
        }

        if (!sources.isEmpty()) {
            stateChanged(SOURCES, SonyUtil.newStringType(StringUtils.join(sources, ',')));
        }
    }

    @Override
    public void refreshState(boolean initial) {
        final ScalarWebChannelTracker tracker = getChannelTracker();

        // always refresh these since they are used in lookups
        refreshSchemes();
        refreshSources();

        if (tracker.isCategoryLinked(ctgy -> StringUtils.startsWith(ctgy, PARENTRATING))) {
            refreshParentalRating();
        }

        if (initial || !notificationHelper.isEnabled(ScalarWebEvent.NOTIFYPLAYINGCONTENTINFO)) {
            if (tracker.isCategoryLinked(ctgy -> StringUtils.startsWith(ctgy, PLAYING))) {
                refreshPlayingContentInfo();
            }
        }

        refreshCurrentExternalInputStatus(
                tracker.getLinkedChannelsForCategory(ctgy -> StringUtils.startsWith(ctgy, INPUT)));

        if (initial || !notificationHelper.isEnabled(ScalarWebEvent.NOTIFYEXTERNALTERMINALSTATUS)) {
            refreshCurrentExternalTerminalsStatus();
        }

        if (tracker.isCategoryLinked(ctgy -> StringUtils.startsWith(ctgy, CONTENT))) {
            refreshContent();
        }

        if (tracker.isCategoryLinked(BLUETOOTHSETTINGS)) {
            refreshGeneralSettings(tracker.getLinkedChannelsForCategory(BLUETOOTHSETTINGS),
                    ScalarWebMethod.GETBLUETOOTHSETTINGS);
        }
        if (tracker.isCategoryLinked(PLAYBACKSETTINGS)) {
            refreshGeneralSettings(tracker.getLinkedChannelsForCategory(PLAYBACKSETTINGS),
                    ScalarWebMethod.GETPLAYBACKMODESETTINGS);
        }

        // Very heavy call - let's just make them restart binding when a preset changes if they
        // want it to show up on a dynamic state for the UI
        // if (tracker.isCategoryLinked(PS_CHANNEL)) {
        // refreshPresetChannelStateDescription(tracker.getLinkedChannelsForCategory(PS_CHANNEL));
        // }
    }

    @Override
    public void setChannel(final ScalarWebChannel channel, final Command command) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        if (StringUtils.equalsIgnoreCase(channel.getCategory(), BLUETOOTHSETTINGS)) {
            setGeneralSetting(ScalarWebMethod.SETBLUETOOTHSETTINGS, channel, command);
        } else if (StringUtils.equalsIgnoreCase(channel.getCategory(), PLAYBACKSETTINGS)) {
            setGeneralSetting(ScalarWebMethod.SETPLAYBACKMODESETTINGS, channel, command);
        } else if (StringUtils.equalsIgnoreCase(channel.getCategory(), PL_CMD)) {
            final String uri = channel.getPathPart(0);
            if (uri == null || StringUtils.isEmpty(uri)) {
                logger.debug("{} command - channel has no uri: {}", PL_CMD, channel);
                return;
            }
            if (command instanceof StringType) {
                setPlayingCommand(uri, channel.getId(), command.toString());
            } else {
                logger.debug("{} command not an StringType: {}", PL_CMD, command);
            }
        } else if (StringUtils.equalsIgnoreCase(channel.getCategory(), PS_CHANNEL)) {
            final String srcId = channel.getPathPart(0);
            if (srcId == null || StringUtils.isEmpty(srcId)) {
                logger.debug("{} command - channel has no srcId: {}", PS_CHANNEL, channel);
                return;
            }
            if (command instanceof StringType) {
                setPlayPresetChannel(srcId, command.toString());
            } else {
                logger.debug("{} command not an StringType: {}", PS_CHANNEL, command);
            }
        } else if (StringUtils.equalsIgnoreCase(channel.getCategory(), PL_PRESET)) {
            final String output = channel.getId();
            if (command instanceof DecimalType) {
                final int preset = ((DecimalType) command).intValue();
                statePlaying.compute(output, (k, v) -> {
                    if (v == null) {
                        return new PlayingState("", preset);
                    } else {
                        return new PlayingState(v.getUri(), preset);
                    }
                });
            } else {
                logger.debug("{} command not an DecimalType: {}", PL_PRESET, command);
            }
        } else if (StringUtils.startsWith(channel.getCategory(), TERM)) {
            final String uri = channel.getPathPart(0);
            if (uri == null || StringUtils.isEmpty(uri)) {
                logger.debug("{} command - channel has no uri: {}", TERM, channel);
                return;
            }
            switch (channel.getCategory()) {
                case TERM_SOURCE: {
                    if (command instanceof StringType) {
                        setTerminalSource(uri, command.toString());
                    } else {
                        logger.debug("{} command not an StringType: {}", TERM_SOURCE, command);
                    }
                    break;
                }
                case TERM_ACTIVE: {
                    if (command instanceof OnOffType) {
                        setTerminalStatus(uri, command == OnOffType.ON);
                    } else {
                        logger.debug("{} command not an OnOffType: {}", TERM_ACTIVE, command);
                    }
                    break;
                }
            }
        } else if (StringUtils.startsWith(channel.getCategory(), CONTENT)) {
            switch (channel.getCategory()) {
                case CN_PARENTURI: {
                    if (command instanceof StringType) {
                        stateContent.set(new ContentState(command.toString(), "", 0));
                        getContext().getScheduler().execute(() -> refreshContent());
                    } else {
                        logger.debug("{} command not an StringType: {}", CN_PARENTURI, command);
                    }
                    break;
                }
                case CN_INDEX: {
                    if (command instanceof DecimalType) {
                        stateContent.updateAndGet(cs -> new ContentState(cs.getParentUri(), cs.getUri(),
                                ((DecimalType) command).intValue()));
                        getContext().getScheduler().execute(() -> refreshContent());
                    } else {
                        logger.debug("{} command not an DecimalType: {}", CN_INDEX, command);
                    }
                    break;
                }
                case CN_CMD: {
                    if (command instanceof StringType) {
                        final String cmd = command.toString();
                        if (StringUtils.equalsIgnoreCase(cmd, "select")) {
                            final ContentState state = stateContent.get();
                            setPlayContent(state.getUri(), null);
                        } else {
                            logger.debug("{} command received an unknown command: {}", CN_CMD, cmd);
                        }
                    } else {
                        logger.debug("{} command not an StringType: {}", CN_CMD, command);
                    }
                    break;
                }
                case CN_ISPROTECTED: {
                    if (command instanceof OnOffType) {
                        setContentProtection(command == OnOffType.ON);
                    } else {
                        logger.debug("{} command not an OnOffType: {}", CN_ISPROTECTED, command);
                    }
                    break;
                }
                case CN_EPGVISIBILITY: {
                    if (command instanceof StringType) {
                        setTvContentVisibility(command.toString(), null, null);
                    } else {
                        logger.debug("{} command not an StringType: {}", CN_EPGVISIBILITY, command);
                    }
                    break;
                }
                case CN_CHANNELSURFINGVISIBILITY: {
                    if (command instanceof StringType) {
                        setTvContentVisibility(null, command.toString(), null);
                    } else {
                        logger.debug("{} command not an StringType: {}", CN_CHANNELSURFINGVISIBILITY, command);
                    }
                    break;
                }
                case CN_VISIBILITY: {
                    if (command instanceof StringType) {
                        setTvContentVisibility(null, null, command.toString());
                    } else {
                        logger.debug("{} command not an StringType: {}", CN_VISIBILITY, command);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Sets the content protection on or off
     *
     * @param on true to turn on protection, false otherwise
     */
    private void setContentProtection(final boolean on) {
        final ContentState cs = stateContent.get();
        handleExecute(ScalarWebMethod.SETDELETEPROTECTION, new DeleteProtection(cs.getUri(), on));
    }

    /**
     * Plays (or stops) the specified content - potentially on a specified output
     *
     * @param uri the non-null, possibly empty URI
     * @param output the possibly null, possibly empty output to play on
     */
    private void setPlayContent(final String uri, final @Nullable String output) {
        Objects.requireNonNull(uri, "uri cannot be null");

        final String translatedOutput = getTranslatedOutput(output);
        handleExecute(ScalarWebMethod.SETPLAYCONTENT, version -> {
            if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1)) {
                return new PlayContent_1_0(uri);
            }
            return new PlayContent_1_2(uri, translatedOutput);
        });
    }

    /**
     * Returns the translated output (ie MAINOUTPUT would be translated to an empty string)
     * 
     * @param output a possibly null, possibly empty output
     * @return a non-null, possibly empty translated output
     */
    private static String getTranslatedOutput(@Nullable String output) {
        return output == null || StringUtils.equalsIgnoreCase(output, MAINOUTPUT) ? "" : output;
    }

    /**
     * Handles the playing command. A playing command can handle the following:
     * <ol>
     * <li>play - plays the current selection</li>
     * <li>pause - pauses the currently playing selection</li>
     * <li>stop - stops the currently playing selection</li>
     * <li>next - plays the next content (if a radio, scans forward)</li>
     * <li>prev - plays the previous content (if a radio, scans backward)</li>
     * <li>fwd - fast-forward content (if a radio, seeks forward manually)</li>
     * <li>bwd - rewinds content (if a radio, seeks backward manually)</li>
     * <li>fwdseek - if a radio, seeks forward automatically</li>
     * <li>bwdseek - if a radio, seeks backward automatically</li>
     * <li>setpreset - if a radio, sets the current preset</li>
     * <li>getpreset - if a radio, restores the current preset</li>
     * </ol>
     * 
     * @param output a non-null, non-empty output to play on
     * @param id a non-null, non-empty id
     * @param command a non-null non-empty command to execute
     */
    private void setPlayingCommand(final String output, final String id, final String command) {
        Validate.notEmpty(output, "output cannot be empty");
        Validate.notEmpty(id, "id cannot be empty");
        Validate.notEmpty(command, "id cannot be empty");

        final PlayingState state = statePlaying.get(id);
        final String playingUri = state == null ? "" : state.getUri();

        final String translatedOutput = getTranslatedOutput(output);

        final Matcher ms = Source.RADIOPATTERN.matcher(playingUri);
        final boolean isRadio = ms.matches();

        switch (command.toLowerCase()) {
            case "play":
                // don't use the "setPlayContent" method here because it will start playing from beginning
                // by just passing an output, it continues play after a pause/fast forward etc
                handleExecute(ScalarWebMethod.SETPLAYCONTENT, new Output(translatedOutput));
                break;

            case "pause":
                handleExecute(ScalarWebMethod.PAUSEPLAYINGCONTENT, new Output(translatedOutput));
                break;

            case "stop":
                if (getService().hasMethod(ScalarWebMethod.STOPPLAYINGCONTENT)) {
                    handleExecute(ScalarWebMethod.STOPPLAYINGCONTENT, new Output(playingUri));
                } else {
                    handleExecute(ScalarWebMethod.DELETECOUNT, new DeleteContent(playingUri));
                }
                break;

            case "next":
                if (isRadio) {
                    handleExecute(ScalarWebMethod.SCANPLAYINGCONTENT,
                            new ScanPlayingContent_1_0(true, translatedOutput));
                } else {
                    handleExecute(ScalarWebMethod.SETPLAYNEXTCONTENT, new Output(translatedOutput));
                }
                break;

            case "prev":
                if (isRadio) {
                    handleExecute(ScalarWebMethod.SCANPLAYINGCONTENT,
                            new ScanPlayingContent_1_0(false, translatedOutput));
                } else {
                    handleExecute(ScalarWebMethod.SETPLAYPREVIOUSCONTENT, new Output(translatedOutput));
                }
                break;

            case "fwd":
                if (isRadio) {
                    handleExecute(ScalarWebMethod.SEEKBROADCASTSTATION, new SeekBroadcastStation_1_0(true, false));
                } else {
                    handleExecute(ScalarWebMethod.SCANPLAYINGCONTENT,
                            new ScanPlayingContent_1_0(true, translatedOutput));
                }
                break;

            case "bwd":
                if (isRadio) {
                    handleExecute(ScalarWebMethod.SEEKBROADCASTSTATION, new SeekBroadcastStation_1_0(false, false));
                } else {
                    handleExecute(ScalarWebMethod.SCANPLAYINGCONTENT,
                            new ScanPlayingContent_1_0(false, translatedOutput));
                }
                break;

            case "fwdseek":
                if (isRadio) {
                    handleExecute(ScalarWebMethod.SEEKBROADCASTSTATION, new SeekBroadcastStation_1_0(true, true));
                } else {
                    logger.debug("Not playing a radio currently: {}", playingUri);
                }
                break;

            case "bwdseek":
                if (isRadio) {
                    handleExecute(ScalarWebMethod.SEEKBROADCASTSTATION, new SeekBroadcastStation_1_0(false, true));
                } else {
                    logger.debug("Not playing a radio currently: {}", playingUri);
                }
                break;

            case "setpreset":
                if (isRadio) {
                    final int preset = state == null ? 1 : state.getPreset();
                    final String presetUri = ms.groupCount() == 1 ? ("?contentId=" + preset)
                            : ms.replaceFirst("$1" + preset);
                    handleExecute(ScalarWebMethod.PRESETBROADCASTSTATION, new PresetBroadcastStation(presetUri));
                } else {
                    logger.debug("Not playing a radio currently: {}", playingUri);
                }
                break;

            case "getpreset":
                if (isRadio) {
                    final int preset = state == null ? 1 : state.getPreset();
                    final String presetUri = ms.groupCount() == 1 ? ("?contentId=" + preset)
                            : ms.replaceFirst("$1" + preset);
                    setPlayContent(presetUri, translatedOutput);
                } else {
                    logger.debug("Not playing a radio currently: {}", playingUri);
                }
                break;

            default:
                break;
        }
    }

    /**
     * Sets the source for the specified terminal
     * 
     * @param output a non-null, non-empty output
     * @param source a non-null, non-empty source
     */
    private void setTerminalSource(final String output, final String source) {
        Validate.notEmpty(output, "output cannot be empty");
        Validate.notEmpty(source, "source cannot be empty");

        final Optional<Source> src = getSources().stream().filter(s -> s.isMatch(source)).findFirst();
        final String srcUri = src.isPresent() ? src.get().getSource() : null;
        setPlayContent(StringUtils.defaultIfEmpty(srcUri, source), output);

        getContext().getScheduler().execute(() -> {
            stateContent.set(new ContentState(source, "", 0));
            refreshContent();
        });
    }

    /**
     * Sets the terminal status to active or not
     *
     * @param uri the non-null, non-empty terminal status
     * @param on true if playing, false otherwise
     */
    private void setTerminalStatus(final String uri, final boolean on) {
        Validate.notEmpty(uri, "uri cannot be empty");

        handleExecute(ScalarWebMethod.SETACTIVETERMINAL,
                new ActiveTerminal(uri, on ? ActiveTerminal.ACTIVE : ActiveTerminal.INACTIVE));

        // Turn off any other channel status
        for (final ScalarWebChannel chnl : getChannelTracker().getLinkedChannelsForCategory(TERM_ACTIVE)) {
            if (!StringUtils.equalsIgnoreCase(chnl.getPathPart(0), uri)) {
                stateChanged(TERM_ACTIVE, chnl.getId(), OnOffType.OFF);
            }
        }
    }

    /**
     * Sets the tv content visibility.
     *
     * @param epgVisibility the epg visibility (null if not specified)
     * @param channelSurfingVisibility the channel surfing visibility (null if not specified)
     * @param visibility the visibility (null if not specified)
     */
    private void setTvContentVisibility(final @Nullable String epgVisibility,
            final @Nullable String channelSurfingVisibility, final @Nullable String visibility) {
        final ContentState cs = stateContent.get();
        handleExecute(ScalarWebMethod.SETTVCONTENTVISIBILITY,
                new TvContentVisibility(cs.getUri(), epgVisibility, channelSurfingVisibility, visibility));
    }

    /**
     * Plays the preset channel specified by it's display name
     * 
     * @param srcId a non-null, non-empty source id
     * @param dispName a non-null, non-empty display name
     */
    private void setPlayPresetChannel(final String srcId, final String dispName) {
        Validate.notEmpty(srcId, "srcId cannot be empty");
        Validate.notEmpty(dispName, "dispName cannot be empty");

        processContentList(srcId, res -> {
            if (StringUtils.equalsIgnoreCase(dispName, res.getDispNum())) {
                final String uri = res.getUri();
                if (uri == null) {
                    logger.debug(
                            "Cannot play preset channel {} because the ContentListResult didn't have a valid URI: {}",
                            dispName, res);
                } else {
                    setPlayContent(uri, null);
                }
                return false;
            }
            return true;
        });
    }

    /**
     * Refreshs the preset channel state description for the specified channels
     * 
     * @param channels a non-null, possibly empty list of channels
     */
    private void refreshPresetChannelStateDescription(final List<ScalarWebChannel> channels) {
        Objects.requireNonNull(channels, "channels cannot be null");

        for (final ScalarWebChannel chl : channels) {
            final String srcId = chl.getPathPart(0);
            if (srcId == null || StringUtils.isEmpty(srcId)) {
                logger.debug("{} command - channel has no srcId: {}", PS_CHANNEL, chl);
                continue;
            }

            final List<ContentListResult_1_0> presets = new ArrayList<>();
            processContentList(srcId, res -> {
                presets.add(res);
                return true;
            });

            if (!presets.isEmpty()) {
                final StateDescriptionFragmentBuilder bld = StateDescriptionFragmentBuilder.create()
                        .withOptions(presets.stream().map(e -> {
                            final String title = e.getTitle();
                            final String dispNum = e.getDispNum();
                            return dispNum == null || StringUtils.isEmpty(dispNum) ? null
                                    : new StateOption(dispNum, StringUtils.defaultIfEmpty(title, dispNum));
                        }).filter(e -> e != null).sorted((a, b) -> ObjectUtils.compare(a.getLabel(), b.getLabel()))
                                .collect(Collectors.toList()));

                final StateDescription sd = bld.build().toStateDescription();
                if (sd != null) {
                    getContext().getStateProvider().addStateOverride(getContext().getThingUID(), chl.getChannelId(),
                            sd);
                }
            }
        }
    }

    /**
     * Processes a content list request and calls back the provided callback until either processed or nothing left
     * 
     * @param uriOrSource a non-null, non-empty uri or source
     * @param callback a non-null callback to use
     */
    private void processContentList(final String uriOrSource, final ContentListCallback callback) {
        Validate.notEmpty(uriOrSource, "uriOrSource cannot be empty");
        Objects.requireNonNull(callback, "callback cannot be null");

        Count ct;
        try {
            ct = execute(ScalarWebMethod.GETCONTENTCOUNT, version -> {
                if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1,
                        ScalarWebMethod.V1_2)) {
                    return new ContentCount_1_0(uriOrSource);
                }
                return new ContentCount_1_3(uriOrSource);
            }).as(Count.class);

        } catch (final IOException e) {
            ct = new Count(0);
        }

        final int i = ct.getCount();
        final int max = (int) Math.ceil((double) i / MAX_CT);
        for (int idx = 0; idx < max; idx++) {
            final int localIdx = (idx + 1) * MAX_CT;
            try {
                final ScalarWebResult res = execute(ScalarWebMethod.GETCONTENTLIST, version -> {
                    if (VersionUtilities.equals(version, ScalarWebMethod.V1_0, ScalarWebMethod.V1_1,
                            ScalarWebMethod.V1_2, ScalarWebMethod.V1_3)) {
                        return new ContentListRequest_1_0(uriOrSource, localIdx, MAX_CT);
                    }
                    return new ContentListRequest_1_4(uriOrSource, localIdx, MAX_CT);
                });

                for (final ContentListResult_1_0 clr : res.asArray(ContentListResult_1_0.class)) {
                    if (!callback.processContentListResult(clr)) {
                        return;
                    }
                }
            } catch (final IOException e) {
                logger.debug("IOException getting {} for {} [idx: {}, max: {}]: {}", ScalarWebMethod.GETCONTENTLIST,
                        uriOrSource, localIdx, MAX_CT, e.getMessage());
            }
        }
    }

    /**
     * Converts a URI to a channel id by parsing the URI and using the port/zone name (host name in the URI) and the
     * port/zone number (port/zone query parameter). Please note that we specifically short 'extInput' to 'in' and
     * 'extOutput' to 'out' for ease of writing channel names.
     * 
     * Note: CEC schemes are unique by port number/logical address. We will further add a "-{addr}" if a logical address
     * exists
     * 
     * Note2: if we have inputs only (not a terminal), then the "in" part will be ignored.
     * 
     * The following demonstrates the various possible inputs and the output of this function:
     * 
     * <table>
     * <thead>
     * <tr>
     * <td>Input</td>
     * <td>Output</td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>notValid</td>
     * <td>notValid</td>
     * </tr>
     * <tr>
     * <td>extInput:bd-dvd</td>
     * <td>in-bd-dvd (bd-dvd for input only)</td>
     * </tr>
     * <tr>
     * <td>extInput:hdmi</td>
     * <td>in-hdmi (hdmi for input only)</td>
     * </tr>
     * <tr>
     * <td>extInput:hdmi?port=1</td>
     * <td>in-hdmi1</td>
     * </tr>
     * <tr>
     * <td>extInput:video?port=2</td>
     * <td>in-video2</td>
     * </tr>
     * <tr>
     * <td>extOutput:zone?zone=2</td>
     * <td>out-zone2</td>
     * </tr>
     * <tr>
     * <td>extInput:bluetooth?blah=3</td>
     * <td>in-bluetooth</td>
     * </tr>
     * <tr>
     * <td>extInput:cec?type=recorder&port=2&logicalAddr=1</td>
     * <td>in-cec2-1</td>
     * </tr>
     * 
     * @param uri a non-null, non-empty uri
     * @param isInput true if we are doing inputs, false otherwise
     * @return a non-null channel identifier
     */
    private String createChannelId(final String uri, final boolean isInput) {
        Validate.notEmpty(uri, "uri cannot be empty");

        // Return the uri if just a word (not really a uri!)
        final int colIdx = uri.indexOf(":");
        if (colIdx < 0) {
            return SonyUtil.createValidChannelUId(uri);
        }

        String scheme = uri.substring(0, colIdx);
        if (isInput) {
            scheme = "";
        } else if (StringUtils.equalsIgnoreCase("extinput", scheme)) {
            scheme = "in-";
        } else if (StringUtils.equalsIgnoreCase("extoutput", scheme)) {
            scheme = "out-";
        } else {
            scheme = scheme + "-";
        }
        final String finalScheme = scheme; // must make final - ugh

        final int portNameIdx = uri.indexOf("?");
        if (portNameIdx < 0) {
            return SonyUtil.createValidChannelUId(finalScheme + uri.substring(colIdx + 1));
        }

        final String portName = uri.substring(colIdx + 1, portNameIdx);
        final String query = uri.substring(portNameIdx + 1);

        // Handle when there is no query path
        if (StringUtils.isEmpty(query)) {
            return SonyUtil.createValidChannelUId(finalScheme + portName);
        }

        // Note that this won't handle multiple parms with the same name - we just hope sony won't ever do that
        final Map<String, String> queryParms = Arrays.stream(query.split("&")).map(o -> o.split("="))
                .collect(Collectors.toMap(k -> k[0], v -> v[1]));

        final String portNbr = queryParms.get("port");
        final String zoneNbr = queryParms.get("zone");

        String channelId = finalScheme + portName;

        if (StringUtils.isNotEmpty(zoneNbr)) {
            channelId += zoneNbr;
        } else if (StringUtils.isNotEmpty(portNbr)) {
            channelId += portNbr;
        }

        // Should only be for CEC - but let's let it ride on all
        final String logAddr = queryParms.get("logicalAddr");
        if (StringUtils.isNotEmpty(logAddr)) {
            channelId += ("-" + logAddr);
        }

        return SonyUtil.createValidChannelUId(channelId);
    }

    /**
     * helper method to conver tthe terminal status to a map of terminal URL to terminal title
     * 
     * @param terms a non-null, possibly empty list of terminal status
     * @return a map of terminal title by terminal uri
     */
    private static Map<String, String> getTerminalOutputs(final List<CurrentExternalTerminalsStatus_1_0> terms) {
        Objects.requireNonNull(terms, "terms cannot be nul");

        final Map<String, String> outputs = new HashMap<>();
        for (final CurrentExternalTerminalsStatus_1_0 term : terms) {
            final String uri = term.getUri();
            if (uri != null && term.isOutput()) {
                outputs.put(uri, term.getTitle(uri));
            }
        }

        return outputs;
    }

    /**
     * Helper class to track the current content state (uris and index)
     */
    @NonNullByDefault
    private static class ContentState {
        /** The parent uri */
        private final String parentUri;

        /** The content uri */
        private final String uri;

        /** The content index position */
        private final int idx;

        /** Constructs the state with no URIs and a 0 index position */
        public ContentState() {
            this("", "", 0);
        }

        /**
         * Constructs the state with the specified uris and index position
         * 
         * @param parentUri a non null, possibly empty parent uri
         * @param uri a non null, possibly empty uri
         * @param idx a greater than or equal to 0 index position
         */
        public ContentState(final String parentUri, final String uri, final int idx) {
            Objects.requireNonNull(parentUri, "parentUri cannot be null");
            Objects.requireNonNull(uri, "uri cannot be null");
            if (idx < 0) {
                throw new IllegalArgumentException("idx cannot be below zero: " + idx);
            }
            this.parentUri = parentUri;
            this.uri = uri;
            this.idx = idx;
        }

        /**
         * Gets the parent URI
         * 
         * @return the parentUri
         */
        public String getParentUri() {
            return parentUri;
        }

        /**
         * Gets the URI
         * 
         * @return the uri
         */
        public String getUri() {
            return uri;
        }

        /**
         * Gets the index position
         * 
         * @return the index position
         */
        public int getIdx() {
            return idx;
        }
    }

    /**
     * This class represets the current playing state
     */
    @NonNullByDefault
    private static class PlayingState {
        /** The playing URI */
        private final String uri;

        /** The play preset */
        private final int preset;

        /**
         * Constructs the playing state from the URI
         * 
         * @param uri a non-null, possibly empty URI
         * @param preset a greater than or equal to 0 preset
         */
        public PlayingState(final String uri, final int preset) {
            Objects.requireNonNull(uri, "uri cannot be null");
            this.uri = uri;
            this.preset = preset;
        }

        /**
         * The URI
         * 
         * @return the uri
         */
        public String getUri() {
            return uri;
        }

        /**
         * The preset
         * 
         * @return the preset
         */
        public int getPreset() {
            return preset;
        }
    }

    /**
     * A helper class to store the input source attributes
     */
    @NonNullByDefault
    private static class InputSource {
        /** The URI of the input */
        private final String uri;

        /** The title of the input */
        private final String title;

        /** The outputs linked to the input */
        private final List<String> outputs;

        /**
         * Constructs the input source based on the parms
         * 
         * @param uri a non-null, non-empty input URI
         * @param title a possibly null, possibly empty input title
         * @param outputs a possibly null, possibly empty list of outputs
         */
        public InputSource(final String uri, final @Nullable String title, final @Nullable List<String> outputs) {
            Validate.notEmpty(uri, "uri cannot be empty");
            this.uri = uri;
            this.title = StringUtils.defaultIfEmpty(title, uri);
            this.outputs = outputs == null ? new ArrayList<>() : outputs;
        }

        /**
         * The URI
         * 
         * @return the uri
         */
        public String getUri() {
            return uri;
        }

        /**
         * The title
         * 
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * The outputs
         * 
         * @return the outputs
         */
        public List<String> getOutputs() {
            return outputs;
        }
    }
}
