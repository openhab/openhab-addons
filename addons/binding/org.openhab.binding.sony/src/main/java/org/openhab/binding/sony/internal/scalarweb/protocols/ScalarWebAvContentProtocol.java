/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/*
 *
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConfig;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.openhab.binding.sony.internal.scalarweb.models.api.ContentListRequest;
import org.openhab.binding.sony.internal.scalarweb.models.api.ContentListResult;
import org.openhab.binding.sony.internal.scalarweb.models.api.Count;
import org.openhab.binding.sony.internal.scalarweb.models.api.CurrentExternalInputsStatus;
import org.openhab.binding.sony.internal.scalarweb.models.api.DeleteContent;
import org.openhab.binding.sony.internal.scalarweb.models.api.DeleteProtection;
import org.openhab.binding.sony.internal.scalarweb.models.api.ParentalRatingSetting;
import org.openhab.binding.sony.internal.scalarweb.models.api.PlayContent;
import org.openhab.binding.sony.internal.scalarweb.models.api.PlayingContentInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.Scheme;
import org.openhab.binding.sony.internal.scalarweb.models.api.Source;
import org.openhab.binding.sony.internal.scalarweb.models.api.TvContentVisibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebAvContentProtocol.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
class ScalarWebAvContentProtocol<T extends ThingCallback<ScalarWebChannel>> extends AbstractScalarWebProtocol<T> {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebAvContentProtocol.class);

    /** The Constant SCHEMES. */
    private final static String SCHEMES = "schemes";

    /** The Constant SOURCES. */
    private final static String SOURCES = "sources";

    /** The Constant PARENTALRATING. */
    private final static String PARENTALRATING = "parentalrating";

    /** The Constant PR_RATINGTYPEAGE. */
    private final static String PR_RATINGTYPEAGE = "pr_ratingtypeage";

    /** The Constant PR_RATINGTYPESONY. */
    private final static String PR_RATINGTYPESONY = "pr_ratingtypesony";

    /** The Constant PR_RATINGCOUNTRY. */
    private final static String PR_RATINGCOUNTRY = "pr_ratingcountry";

    /** The Constant PR_RATINGCUSTOMTYPETV. */
    private final static String PR_RATINGCUSTOMTYPETV = "pr_ratingcustomtypetv";

    /** The Constant PR_RATINGCUSTOMTYPEMPAA. */
    private final static String PR_RATINGCUSTOMTYPEMPAA = "pr_ratingcustomtypempaa";

    /** The Constant PR_RATINGCUSTOMTYPECAENGLISH. */
    private final static String PR_RATINGCUSTOMTYPECAENGLISH = "pr_ratingcustomtypecaenglish";

    /** The Constant PR_RATINGCUSTOMTYPECAFRENCH. */
    private final static String PR_RATINGCUSTOMTYPECAFRENCH = "pr_ratingcustomtypecafrench";

    /** The Constant PR_UNRATEDLOCK. */
    private final static String PR_UNRATEDLOCK = "pr_unratedlock";

    /** The Constant PLAYING. */
    private final static String PLAYING = "playing";

    /** The Constant PL_URI. */
    private final static String PL_URI = "pl_uri";

    /** The Constant PL_SOURCE. */
    private final static String PL_SOURCE = "pl_source";

    /** The Constant PL_TITLE. */
    private final static String PL_TITLE = "pl_title";

    /** The Constant PL_DISPNUM. */
    private final static String PL_DISPNUM = "pl_dispnum";

    /** The Constant PL_ORIGINALDISPNUM. */
    private final static String PL_ORIGINALDISPNUM = "pl_originaldispnum";

    /** The Constant PL_TRIPLETSTR. */
    private final static String PL_TRIPLETSTR = "pl_tripletstr";

    /** The Constant PL_PROGRAMNUM. */
    private final static String PL_PROGRAMNUM = "pl_programnum";

    /** The Constant PL_PROGRAMTITLE. */
    private final static String PL_PROGRAMTITLE = "pl_programtitle";

    /** The Constant PL_STARTDATETIME. */
    private final static String PL_STARTDATETIME = "pl_startdatetime";

    /** The Constant PL_DURATIONSEC. */
    private final static String PL_DURATIONSEC = "pl_durationsec";

    /** The Constant PL_MEDIATYPE. */
    private final static String PL_MEDIATYPE = "pl_mediatype";

    /** The Constant PL_PLAYSPEED. */
    private final static String PL_PLAYSPEED = "pl_playspeed";

    /** The Constant PL_BIVLSERVICEID. */
    private final static String PL_BIVLSERVICEID = "pl_bivlserviceid";

    /** The Constant PL_BIVLASSETID. */
    private final static String PL_BIVLASSETID = "pl_bivlassetid";

    /** The Constant PL_BIVLPROVIDER. */
    private final static String PL_BIVLPROVIDER = "pl_bivlprovider";

    /** The Constant INPUTSTATUS. */
    private final static String INPUTSTATUS = "inpstatus";

    /** The Constant IN_URI. */
    private final static String IN_URI = "in_uri";

    /** The Constant IN_TITLE. */
    private final static String IN_TITLE = "in_title";

    /** The Constant IN_CONNECTION. */
    private final static String IN_CONNECTION = "in_connection";

    /** The Constant IN_LABEL. */
    private final static String IN_LABEL = "in_label";

    /** The Constant IN_ICON. */
    private final static String IN_ICON = "in_icon";

    /** The Constant IN_STATUS. */
    private final static String IN_STATUS = "in_status";

    /** The Constant CONTENT. */
    private final static String CONTENT = "content";

    /** The Constant CN_STATUS. */
    private final static String CN_STATUS = "cn_status";

    /** The Constant CN_URI. */
    private final static String CN_URI = "cn_uri";

    /** The Constant CN_TITLE. */
    private final static String CN_TITLE = "cn_title";

    /** The Constant CN_IDX. */
    private final static String CN_IDX = "cn_idx";

    /** The Constant CN_DISPNUM. */
    private final static String CN_DISPNUM = "cn_dispnum";

    /** The Constant CN_ORIGINALDISPNUM. */
    private final static String CN_ORIGINALDISPNUM = "cn_originaldispnum";

    /** The Constant CN_TRIPLETSTR. */
    private final static String CN_TRIPLETSTR = "cn_tripletstr";

    /** The Constant CN_PROGRAMNUM. */
    private final static String CN_PROGRAMNUM = "cn_programnum";

    /** The Constant CN_PROGRAMMEDIATYPE. */
    private final static String CN_PROGRAMMEDIATYPE = "cn_programmediatype";

    /** The Constant CN_DIRECTREMOTENUM. */
    private final static String CN_DIRECTREMOTENUM = "cn_directremotenum";

    /** The Constant CN_EPGVISIBILITY. */
    private final static String CN_EPGVISIBILITY = "cn_epgvisibility";

    /** The Constant CN_CHANNELSURFINGVISIBILITY. */
    private final static String CN_CHANNELSURFINGVISIBILITY = "cn_channelsurfingvisibility";

    /** The Constant CN_VISIBILITY. */
    private final static String CN_VISIBILITY = "cn_visibility";

    /** The Constant CN_STARTDATETIME. */
    private final static String CN_STARTDATETIME = "cn_startdatetime";

    /** The Constant CN_CHANNELNAME. */
    private final static String CN_CHANNELNAME = "cn_channelname";

    /** The Constant CN_FILESIZEBYTE. */
    private final static String CN_FILESIZEBYTE = "cn_filesizebyte";

    /** The Constant CN_ISPROTECTED. */
    private final static String CN_ISPROTECTED = "cn_isprotected";

    /** The Constant CN_ISALREADYPLAYED. */
    private final static String CN_ISALREADYPLAYED = "cn_isalreadyplayed";

    /** The Constant CN_PRODUCTID. */
    private final static String CN_PRODUCTID = "cn_productid";

    /** The Constant CN_CONTENTTYPE. */
    private final static String CN_CONTENTTYPE = "cn_contenttype";

    /** The Constant CN_STORAGEURI. */
    private final static String CN_STORAGEURI = "cn_storageuri";

    /** The Constant CN_VIDEOCODEC. */
    private final static String CN_VIDEOCODEC = "cn_videocodec";

    /** The Constant CN_CHAPTERCOUNT. */
    private final static String CN_CHAPTERCOUNT = "cn_chaptercount";

    /** The Constant CN_DURATIONSEC. */
    private final static String CN_DURATIONSEC = "cn_durationsec";

    /** The Constant CN_AUDIOCODEC. */
    private final static String CN_AUDIOCODEC = "cn_audiocodec";

    /** The Constant CN_AUDIOFREQUENCY. */
    private final static String CN_AUDIOFREQUENCY = "cn_audiofrequency";

    /** The Constant CN_AUDIOCHANNEL. */
    private final static String CN_AUDIOCHANNEL = "cn_audiochannel";

    /** The Constant CN_SUBTITLELANGUAGE. */
    private final static String CN_SUBTITLELANGUAGE = "cn_subtitlelanguage";

    /** The Constant CN_SUBTITLETITLE. */
    private final static String CN_SUBTITLETITLE = "cn_subtitletitle";

    /** The Constant CN_PARENTALRATING. */
    private final static String CN_PARENTALRATING = "cn_parentalrating";

    /** The Constant CN_PARENTALSYSTEM. */
    private final static String CN_PARENTALSYSTEM = "cn_parentalsystem";

    /** The Constant CN_PARENTALCOUNTRY. */
    private final static String CN_PARENTALCOUNTRY = "cn_parentalcountry";

    /** The Constant CN_SIZEMB. */
    private final static String CN_SIZEMB = "cn_sizemb";

    /** The Constant CN_CREATEDTIME. */
    private final static String CN_CREATEDTIME = "cn_createdtime";

    /** The Constant CN_USERCONTENTFLAG. */
    private final static String CN_USERCONTENTFLAG = "cn_usercontentflag";

    /** The Constant MAX_CT. */
    private final static int MAX_CT = 50;

    /** The config. */
    private final ScalarWebConfig config;

    /** The items lock. */
    private final Lock itemsLock = new ReentrantLock();

    /** The items by index. */
    private final Map<SourceIndex, ContentItem> itemsByIndex = new HashMap<SourceIndex, ContentItem>();

    /**
     * Instantiates a new scalar web av content protocol.
     *
     * @param tracker the tracker
     * @param state the state
     * @param service the service
     * @param config the config
     * @param callback the callback
     */
    ScalarWebAvContentProtocol(ScalarWebChannelTracker tracker, ScalarWebState state, ScalarWebService service,
            ScalarWebConfig config, T callback) {
        super(tracker, state, service, callback);
        this.config = config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#getChannelDescriptors()
     */
    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors() {

        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        if (service.getMethod(ScalarWebMethod.GetSchemeList) != null) {
            descriptors.add(createDescriptor(createChannel(SCHEMES), "String", "scalarwebavcontrolschemes"));
        }
        if (service.getMethod(ScalarWebMethod.GetSourceList) != null) {
            descriptors.add(createDescriptor(createChannel(SOURCES), "String", "scalarwebavcontrolsources"));
        }

        itemsLock.lock();
        try {
            itemsByIndex.clear();

            for (Scheme scheme : getSchemes()) {

                // if ignoring all tv channels - ignore the scheme.
                if (StringUtils.equalsIgnoreCase(Scheme.TV, scheme.getScheme())) {
                    if (StringUtils.equalsIgnoreCase("all", config.getIgnoreChannels())) {
                        continue;
                    }
                }

                for (Source source : getSources(scheme)) {

                    // Ignore analog and/or digit channels if specified by the config
                    if (StringUtils.equalsIgnoreCase(Scheme.TV, scheme.getScheme())) {
                        if (StringUtils.equalsIgnoreCase(Source.TV_ANALOG, source.getSource())
                                && StringUtils.equalsIgnoreCase("analog", config.getIgnoreChannels())) {
                            continue;
                        }
                        if (StringUtils.equalsIgnoreCase(Source.TV_DIGITAL, source.getSource())
                                && StringUtils.equalsIgnoreCase("digital", config.getIgnoreChannels())) {
                            continue;
                        }
                    }

                    Count ct;

                    try {
                        ct = execute(ScalarWebMethod.GetContentCount, source).as(Count.class);
                    } catch (Exception e) {
                        ct = new Count(0);
                    }

                    final Set<String> seenUris = new HashSet<String>();
                    final int max = ct.getCount() + MAX_CT;
                    for (int idx = 0; idx < max; idx += MAX_CT) {

                        try {
                            final List<ContentListResult> clrs = execute(ScalarWebMethod.GetContentList,
                                    new ContentListRequest(source.getSource(), idx, MAX_CT))
                                            .asArray(ContentListResult.class);
                            for (ContentListResult clr : clrs) {
                                if (!clr.hasIndex()) {
                                    logger.warn("Content list result had no index (which is required): {}", clr);
                                    continue;

                                }

                                if (!clr.hasUri()) {
                                    logger.warn("Content list result had no URI (which is required): {}", clr);
                                    continue;

                                }

                                if (seenUris.contains(clr.getUri())) {
                                    logger.warn("Sony Bug - ignoring duplicate URI: {}", clr);
                                    continue;
                                }
                                seenUris.add(clr.getUri());

                                itemsByIndex.put(
                                        new SourceIndex(scheme.getScheme(), source.getSource(), clr.getIndex()),
                                        new ContentItem(clr.getUri()));

                                final String index = convertIndex(clr.getIndex());
                                String title = null;
                                if (clr.hasDispNum()
                                        && StringUtils.equalsIgnoreCase(Source.TV_DIGITAL, source.getSource())) {
                                    title = "Digital Channel " + clr.getDispNum();
                                } else if (clr.hasDispNum()
                                        && StringUtils.equalsIgnoreCase(Source.TV_ANALOG, source.getSource())) {
                                    title = "Analog Channel " + clr.getDispNum();
                                } else if (clr.hasTitle()) {
                                    title = clr.getTitle();
                                }

                                descriptors.add(createDescriptor(
                                        createChannel(CN_STATUS, CONTENT, scheme.getScheme(), source.getSource(),
                                                index),
                                        "Switch", "scalarwebavcontrolcontentstatus",
                                        title == null ? null : ("Content " + title + " Status"), null));

                                descriptors
                                        .add(createDescriptor(
                                                createChannel(CN_URI, CONTENT, scheme.getScheme(), source.getSource(),
                                                        index),
                                                "String", "scalarwebavcontrolcontenturi",
                                                title == null ? null : ("Content " + title + " URI"), null));

                                if (clr.hasTitle()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_TITLE, CONTENT, scheme.getScheme(), source.getSource(),
                                                    index),
                                            "String", "scalarwebavcontrolcontenttitle",
                                            title == null ? null : ("Content " + title + " Title"), null));
                                }

                                if (clr.hasIndex()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_IDX, CONTENT, scheme.getScheme(), source.getSource(),
                                                    index),
                                            "Number", "scalarwebavcontrolcontentidx",
                                            title == null ? null : ("Content " + title + " Index"), null));
                                }

                                if (clr.hasDispNum()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_DISPNUM, CONTENT, scheme.getScheme(), source.getSource(),
                                                    index),
                                            "String", "scalarwebavcontrolcontentdispnum",
                                            title == null ? null : ("Content " + title + " Display Number"), null));
                                }

                                if (clr.hasOriginalDispNum()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_ORIGINALDISPNUM, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentoriginaldispnum",
                                            title == null ? null : ("Content " + title + " Original Display Number"),
                                            null));
                                }

                                if (clr.hasTripletStr()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_TRIPLETSTR, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontenttripletstr",
                                            title == null ? null : ("Content " + title + " Channel Triplet"), null));
                                }

                                if (clr.hasProgramNum()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_PROGRAMNUM, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "Number", "scalarwebavcontrolcontentprogramnum",
                                            title == null ? null : ("Content " + title + " Channel"), null));
                                }

                                if (clr.hasProgramMediaType()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_PROGRAMMEDIATYPE, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentprogrammediatype",
                                            title == null ? null : ("Content " + title + " Media Type"), null));
                                }

                                if (clr.hasDirectRemoteNum()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_DIRECTREMOTENUM, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "Number", "scalarwebavcontrolcontentdirectremotenum",
                                            title == null ? null : ("Content " + title + " Direct Remote Number"),
                                            null));
                                }

                                if (clr.hasEpgVisibility()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_EPGVISIBILITY, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentepgvisibility",
                                            title == null ? null : ("Content " + title + " EPG Visibility"), null));
                                }

                                if (clr.hasChannelSurfingVisibility()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_CHANNELSURFINGVISIBILITY, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentchannelsurfingvisibility",
                                            title == null ? null : ("Content " + title + " Surfing Visibility"), null));
                                }

                                if (clr.hasVisibility()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_VISIBILITY, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentvisibility",
                                            title == null ? null : ("Content " + title + " Visibility"), null));
                                }

                                if (clr.hasStartDateTime()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_STARTDATETIME, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentstartdatetime",
                                            title == null ? null : ("Content " + title + " Start Date/Time"), null));
                                }

                                if (clr.hasChannelName()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_CHANNELNAME, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentchannelname",
                                            title == null ? null : ("Content " + title + " Channel Name"), null));
                                }

                                if (clr.hasFileSizeByte()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_FILESIZEBYTE, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "Number", "scalarwebavcontrolcontentfilesizebyte",
                                            title == null ? null : ("Content " + title + " File Size (bytes)"), null));
                                }

                                if (clr.hasIsProtected()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_ISPROTECTED, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "Switch", "scalarwebavcontrolcontentisprotected",
                                            title == null ? null : ("Content " + title + " Protected"), null));
                                }

                                if (clr.hasIsAlreadyPlayed()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_ISALREADYPLAYED, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "Switch", "scalarwebavcontrolcontentisalreadyplayed",
                                            title == null ? null : ("Content " + title + " Already Played"), null));
                                }

                                if (clr.hasProductID()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_PRODUCTID, CONTENT, scheme.getScheme(), source.getSource(),
                                                    index),
                                            "String", "scalarwebavcontrolcontentproductid",
                                            title == null ? null : ("Content " + title + " Product ID"), null));
                                }

                                if (clr.hasContentType()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_CONTENTTYPE, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentcontenttype",
                                            title == null ? null : ("Content " + title + " Content Type"), null));
                                }

                                if (clr.hasStorageUri()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_STORAGEURI, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentstorageuri",
                                            title == null ? null : ("Content " + title + " Storage URI"), null));
                                }

                                if (clr.hasVideoCodec()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_VIDEOCODEC, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentvideocodec",
                                            title == null ? null : ("Content " + title + " Video Codec(s)"), null));
                                }

                                if (clr.hasChapterCount()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_CHAPTERCOUNT, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "Number", "scalarwebavcontrolcontentchaptercount",
                                            title == null ? null : ("Content " + title + " Chapter Count"), null));
                                }

                                if (clr.hasDurationSec()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_DURATIONSEC, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "Number", "scalarwebavcontrolcontentdurationsec",
                                            title == null ? null : ("Content " + title + " Duration (seconds)"), null));
                                }
                                if (clr.hasAudioCodec()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_AUDIOCODEC, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentaudiocodec",
                                            title == null ? null : ("Content " + title + " Audio Codec(s)"), null));
                                }

                                if (clr.hasAudioFrequency()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_AUDIOFREQUENCY, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentaudiofrequency",
                                            title == null ? null : ("Content " + title + " Audio Frequency"), null));
                                }

                                if (clr.hasAudioChannel()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_AUDIOCHANNEL, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentaudiochannel",
                                            title == null ? null : ("Content " + title + " Audio Channel"), null));
                                }

                                if (clr.hasSubtitleLanguage()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_SUBTITLELANGUAGE, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentsubtitlelanguage",
                                            title == null ? null : ("Content " + title + " Subtitle Language"), null));
                                }

                                if (clr.hasSubtitleTitle()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_SUBTITLETITLE, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentsubtitletitle",
                                            title == null ? null : ("Content " + title + " Subtitle Title"), null));
                                }

                                if (clr.hasParentalRating()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_PARENTALRATING, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentparentalrating",
                                            title == null ? null : ("Content " + title + " Parental Rating"), null));
                                }

                                if (clr.hasParentalSystem()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_PARENTALSYSTEM, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentparentalsystem",
                                            title == null ? null : ("Content " + title + " Parental Rating System"),
                                            null));
                                }

                                if (clr.hasParentalCountry()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_PARENTALCOUNTRY, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentparentalcountry",
                                            title == null ? null : ("Content " + title + " Parental Rating Country"),
                                            null));
                                }

                                if (clr.hasSizeMB()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_SIZEMB, CONTENT, scheme.getScheme(), source.getSource(),
                                                    index),
                                            "Number", "scalarwebavcontrolcontentsizemb",
                                            title == null ? null : ("Content " + title + " Content Size (MB)"), null));
                                }

                                if (clr.hasCreatedTime()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_CREATEDTIME, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "String", "scalarwebavcontrolcontentcreatedtime",
                                            title == null ? null : ("Content " + title + " Create Time"), null));
                                }

                                if (clr.hasUserContentFlag()) {
                                    descriptors.add(createDescriptor(
                                            createChannel(CN_USERCONTENTFLAG, CONTENT, scheme.getScheme(),
                                                    source.getSource(), index),
                                            "Switch", "scalarwebavcontrolcontentusercontentflag",
                                            title == null ? null : ("Content " + title + " Content Flag"), null));
                                }

                            }
                        } catch (IOException e) {
                            logger.error(
                                    "Exception occurred retrieving the content list for scheme '{}' and source '{}': {}",
                                    scheme.getScheme(), source.getSource(), e.getMessage(), e);
                        }
                    }
                }
            }
        } finally {
            itemsLock.unlock();
        }

        try {
            for (final CurrentExternalInputsStatus ceis : execute(ScalarWebMethod.GetCurrentExternalInputsStatus)
                    .asArray(CurrentExternalInputsStatus.class)) {

                if (!ceis.hasUri()) {
                    logger.warn("External Input status had no URI (which is required): {}", ceis);
                    continue;
                }

                final String title = ceis.hasTitle() ? ceis.getTitle() : null;

                descriptors.add(createDescriptor(createChannel(IN_URI, INPUTSTATUS, ceis.getUri()), "String",
                        "scalarwebavcontrolinpstatusuri", title == null ? null : ("Input " + title + " URI"), null));

                if (ceis.hasTitle()) {
                    descriptors.add(createDescriptor(createChannel(IN_TITLE, INPUTSTATUS, ceis.getUri()), "String",
                            "scalarwebavcontrolinpstatustitle", title == null ? null : ("Input " + title + " Title"),
                            null));
                }

                if (ceis.hasConnection()) {
                    descriptors.add(createDescriptor(createChannel(IN_CONNECTION, INPUTSTATUS, ceis.getUri()), "Switch",
                            "scalarwebavcontrolinpstatusconnection",
                            title == null ? null : ("Input " + title + " Connected"), null));
                }

                if (ceis.hasLabel()) {
                    descriptors.add(createDescriptor(createChannel(IN_LABEL, INPUTSTATUS, ceis.getUri()), "String",
                            "scalarwebavcontrolinpstatuslabel", title == null ? null : ("Input " + title + " Label"),
                            null));
                }

                if (ceis.hasIcon()) {
                    descriptors.add(createDescriptor(createChannel(IN_ICON, INPUTSTATUS, ceis.getUri()), "String",
                            "scalarwebavcontrolinpstatusicon", title == null ? null : ("Input " + title + " Icon"),
                            null));
                }

                if (ceis.hasStatus()) {
                    descriptors.add(createDescriptor(createChannel(IN_STATUS, INPUTSTATUS, ceis.getUri()), "String",
                            "scalarwebavcontrolinpstatusstatus", title == null ? null : ("Input " + title + " Status"),
                            null));
                }
            }
        } catch (IOException e) {
            logger.error("Exception occurring getting the current external input status: {}", e.getMessage(), e);
        }

        try {
            final ParentalRatingSetting prs = execute(ScalarWebMethod.GetParentalRatingSettings)
                    .as(ParentalRatingSetting.class);

            if (prs.hasRatingTypeAge()) {
                descriptors.add(createDescriptor(createChannel(PR_RATINGTYPEAGE, PARENTALRATING), "Number",
                        "scalarwebavcontrolprratingtypeage"));
            }

            if (prs.hasRatingTypeSony()) {
                descriptors.add(createDescriptor(createChannel(PR_RATINGTYPESONY, PARENTALRATING), "String",
                        "scalarwebavcontrolprratingtypesony"));
            }

            if (prs.hasRatingCountry()) {
                descriptors.add(createDescriptor(createChannel(PR_RATINGCOUNTRY, PARENTALRATING), "String",
                        "scalarwebavcontrolprratingcountry"));
            }

            if (prs.hasRatingCustomTypeTV()) {
                descriptors.add(createDescriptor(createChannel(PR_RATINGCUSTOMTYPETV, PARENTALRATING), "String",
                        "scalarwebavcontrolprratingcustomtypetv"));
            }

            if (prs.hasRatingCustomTypeMpaa()) {
                descriptors.add(createDescriptor(createChannel(PR_RATINGCUSTOMTYPEMPAA, PARENTALRATING), "String",
                        "scalarwebavcontrolprratingcustomtypempaa"));
            }

            if (prs.hasRatingCustomTypeCaEnglish()) {
                descriptors.add(createDescriptor(createChannel(PR_RATINGCUSTOMTYPECAENGLISH, PARENTALRATING), "String",
                        "scalarwebavcontrolprratingcustomtypecaenglish"));
            }

            if (prs.hasRatingCustomTypeCaFrench()) {
                descriptors.add(createDescriptor(createChannel(PR_RATINGCUSTOMTYPECAFRENCH, PARENTALRATING), "String",
                        "scalarwebavcontrolprratingcustomtypecafrench"));
            }

            if (prs.hasUnratedLock()) {
                descriptors.add(createDescriptor(createChannel(PR_UNRATEDLOCK, PARENTALRATING), "Switch",
                        "scalarwebavcontrolprunratedlock"));
            }
        } catch (IOException e) {
            logger.error("Exception occurring getting the parental ratings: {}", e.getMessage(), e);
        }

        try {
            final PlayingContentInfo pci = execute(ScalarWebMethod.GetPlayingContentInfo).as(PlayingContentInfo.class);

            if (pci.hasUri()) {
                descriptors.add(createDescriptor(createChannel(PL_URI, PLAYING), "String", "scalarwebavcontrolpluri"));
            }

            if (pci.hasSource()) {
                descriptors.add(
                        createDescriptor(createChannel(PL_SOURCE, PLAYING), "String", "scalarwebavcontrolplsource"));
            }

            if (pci.hasTitle()) {
                descriptors
                        .add(createDescriptor(createChannel(PL_TITLE, PLAYING), "String", "scalarwebavcontrolpltitle"));
            }

            if (pci.hasDispNum()) {
                descriptors.add(
                        createDescriptor(createChannel(PL_DISPNUM, PLAYING), "String", "scalarwebavcontrolpldispnum"));
            }

            if (pci.hasOriginalDispNum()) {
                descriptors.add(createDescriptor(createChannel(PL_ORIGINALDISPNUM, PLAYING), "String",
                        "scalarwebavcontrolploriginaldispnum"));
            }

            if (pci.hasTripletStr()) {
                descriptors.add(createDescriptor(createChannel(PL_TRIPLETSTR, PLAYING), "String",
                        "scalarwebavcontrolpltripletstr"));
            }

            if (pci.hasProgramNum()) {
                descriptors.add(createDescriptor(createChannel(PL_PROGRAMNUM, PLAYING), "Number",
                        "scalarwebavcontrolplprogramnum"));
            }

            if (pci.hasProgramTitle()) {
                descriptors.add(createDescriptor(createChannel(PL_PROGRAMTITLE, PLAYING), "String",
                        "scalarwebavcontrolplprogramtitle"));
            }

            if (pci.hasStartDateTime()) {
                descriptors.add(createDescriptor(createChannel(PL_STARTDATETIME, PLAYING), "String",
                        "scalarwebavcontrolplstartdatetime"));
            }

            if (pci.hasDurationSec()) {
                descriptors.add(createDescriptor(createChannel(PL_DURATIONSEC, PLAYING), "Number",
                        "scalarwebavcontrolpldurationsec"));
            }

            if (pci.hasMediaType()) {
                descriptors.add(createDescriptor(createChannel(PL_MEDIATYPE, PLAYING), "String",
                        "scalarwebavcontrolplmediatype"));
            }

            if (pci.hasPlaySpeed()) {
                descriptors.add(createDescriptor(createChannel(PL_PLAYSPEED, PLAYING), "String",
                        "scalarwebavcontrolplplayspeed"));
            }

            if (pci.hasBivlServiceId()) {
                descriptors.add(createDescriptor(createChannel(PL_BIVLSERVICEID, PLAYING), "String",
                        "scalarwebavcontrolplbivlserviceid"));
            }

            if (pci.hasBivlAssetId()) {
                descriptors.add(createDescriptor(createChannel(PL_BIVLASSETID, PLAYING), "String",
                        "scalarwebavcontrolplbivlassetid"));
            }

            if (pci.hasBivlProvider()) {
                descriptors.add(createDescriptor(createChannel(PL_BIVLPROVIDER, PLAYING), "String",
                        "scalarwebavcontrolplbivlprovider"));
            }
        } catch (IOException e) {
            logger.error("Exception occurring getting the playing content info: {}", e.getMessage(), e);
        }
        return descriptors;
    }

    /**
     * Convert index.
     *
     * @param index the index
     * @return the string
     */
    private static String convertIndex(int index) {
        return StringUtils.leftPad(String.valueOf(index), 4, '0');
    }

    /**
     * Gets the schemes.
     *
     * @return the schemes
     */
    private List<Scheme> getSchemes() {
        try {
            return execute(ScalarWebMethod.GetSchemeList).asArray(Scheme.class);
        } catch (IOException e) {
            return new ArrayList<Scheme>();
        }
    }

    /**
     * Gets the sources.
     *
     * @param scheme the scheme
     * @return the sources
     */
    private List<Source> getSources(Scheme scheme) {
        try {
            return execute(ScalarWebMethod.GetSourceList, scheme).asArray(Source.class);
        } catch (IOException e) {
            return new ArrayList<Source>();
        }
    }

    /**
     * Gets the parental rating setting.
     *
     * @return the parental rating setting
     */
    private ParentalRatingSetting getParentalRatingSetting() {
        try {
            return execute(ScalarWebMethod.GetParentalRatingSettings).as(ParentalRatingSetting.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets the playing content info.
     *
     * @return the playing content info
     */
    private PlayingContentInfo getPlayingContentInfo() {
        try {
            return execute(ScalarWebMethod.GetPlayingContentInfo).as(PlayingContentInfo.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets the current external inputs status.
     *
     * @return the current external inputs status
     */
    private List<CurrentExternalInputsStatus> getCurrentExternalInputsStatus() {
        try {
            return execute(ScalarWebMethod.GetCurrentExternalInputsStatus).asArray(CurrentExternalInputsStatus.class);
        } catch (IOException e) {
            return new ArrayList<CurrentExternalInputsStatus>();
        }

    }

    /**
     * Gets the content list result.
     *
     * @param si the si
     * @return the content list result
     */
    private ContentListResult getContentListResult(SourceIndex si) {
        try {
            final ContentListResult clr = execute(ScalarWebMethod.GetContentList,
                    new ContentListRequest(si.getSource(), si.getIndex(), 1)).as(ContentListResult.class);

            if (!clr.hasIndex()) {
                logger.warn("Content list result had no index (which is required): {}", clr);
                return null;

            }

            if (!clr.hasUri()) {
                logger.warn("Content list result had no URI (which is required): {}", clr);
                return null;

            }

            return clr;
        } catch (IOException e) {
            return null;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshState()
     */
    @Override
    public void refreshState() {
        if (isLinked(SCHEMES)) {
            refreshSchemes();
        }

        if (isLinked(SOURCES)) {
            refreshSources();
        }

        if (isIdLinked(PR_RATINGTYPEAGE, PR_RATINGTYPESONY, PR_RATINGCOUNTRY, PR_RATINGCUSTOMTYPETV,
                PR_RATINGCUSTOMTYPEMPAA, PR_RATINGCUSTOMTYPECAENGLISH, PR_RATINGCUSTOMTYPECAFRENCH, PR_UNRATEDLOCK)) {
            refreshParentalRating();
        }

        if (isIdLinked(CN_STATUS, IN_STATUS, PL_URI, PL_SOURCE, PL_TITLE, PL_DISPNUM, PL_ORIGINALDISPNUM, PL_TRIPLETSTR,
                PL_PROGRAMNUM, PL_PROGRAMTITLE, PL_STARTDATETIME, PL_DURATIONSEC, PL_MEDIATYPE, PL_PLAYSPEED,
                PL_BIVLSERVICEID, PL_BIVLASSETID, PL_BIVLPROVIDER)) {
            refreshPlayingContentInfo();
        }

        if (isIdLinked(IN_URI, IN_TITLE, IN_CONNECTION, IN_LABEL, IN_ICON, IN_STATUS)) {
            refreshCurrentExternalInputStatus();
        }

        final Set<SourceIndex> sis = new HashSet<SourceIndex>();
        for (ScalarWebChannel channel : getLinkedChannelsForId(CN_STATUS, CN_URI, CN_TITLE, CN_IDX, CN_DISPNUM,
                CN_ORIGINALDISPNUM, CN_TRIPLETSTR, CN_PROGRAMNUM, CN_PROGRAMMEDIATYPE, CN_DIRECTREMOTENUM,
                CN_EPGVISIBILITY, CN_CHANNELSURFINGVISIBILITY, CN_VISIBILITY, CN_STARTDATETIME, CN_CHANNELNAME,
                CN_FILESIZEBYTE, CN_ISPROTECTED, CN_ISALREADYPLAYED, CN_PRODUCTID, CN_CONTENTTYPE, CN_STORAGEURI,
                CN_VIDEOCODEC, CN_CHAPTERCOUNT, CN_DURATIONSEC, CN_AUDIOCODEC, CN_AUDIOFREQUENCY, CN_AUDIOCHANNEL,
                CN_SUBTITLELANGUAGE, CN_SUBTITLETITLE, CN_PARENTALRATING, CN_PARENTALSYSTEM, CN_PARENTALCOUNTRY,
                CN_SIZEMB, CN_CREATEDTIME, CN_USERCONTENTFLAG)) {
            sis.add(new SourceIndex(channel));
        }
        for (SourceIndex si : sis) {
            refreshContent(si);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel)
     */
    @Override
    public void refreshChannel(ScalarWebChannel channel) {
        switch (channel.getId()) {
            case SCHEMES:
                refreshSchemes();
                break;

            case SOURCES:
                refreshSources();
                break;

            case PR_RATINGTYPEAGE:
            case PR_RATINGTYPESONY:
            case PR_RATINGCOUNTRY:
            case PR_RATINGCUSTOMTYPETV:
            case PR_RATINGCUSTOMTYPEMPAA:
            case PR_RATINGCUSTOMTYPECAENGLISH:
            case PR_RATINGCUSTOMTYPECAFRENCH:
            case PR_UNRATEDLOCK:
                refreshParentalRating();
                break;

            case CN_STATUS:
            case PL_URI:
            case PL_SOURCE:
            case PL_TITLE:
            case PL_DISPNUM:
            case PL_ORIGINALDISPNUM:
            case PL_TRIPLETSTR:
            case PL_PROGRAMNUM:
            case PL_PROGRAMTITLE:
            case PL_STARTDATETIME:
            case PL_DURATIONSEC:
            case PL_MEDIATYPE:
            case PL_PLAYSPEED:
            case PL_BIVLSERVICEID:
            case PL_BIVLASSETID:
            case PL_BIVLPROVIDER:
                refreshPlayingContentInfo();
                break;

            case IN_URI:
            case IN_TITLE:
            case IN_CONNECTION:
            case IN_LABEL:
            case IN_ICON:
            case IN_STATUS:
                refreshCurrentExternalInputStatus();
                break;

            case CN_URI:
            case CN_TITLE:
            case CN_IDX:
            case CN_DISPNUM:
            case CN_ORIGINALDISPNUM:
            case CN_TRIPLETSTR:
            case CN_PROGRAMNUM:
            case CN_PROGRAMMEDIATYPE:
            case CN_DIRECTREMOTENUM:
            case CN_EPGVISIBILITY:
            case CN_CHANNELSURFINGVISIBILITY:
            case CN_VISIBILITY:
            case CN_STARTDATETIME:
            case CN_CHANNELNAME:
            case CN_FILESIZEBYTE:
            case CN_ISPROTECTED:
            case CN_ISALREADYPLAYED:
            case CN_PRODUCTID:
            case CN_CONTENTTYPE:
            case CN_STORAGEURI:
            case CN_VIDEOCODEC:
            case CN_CHAPTERCOUNT:
            case CN_DURATIONSEC:
            case CN_AUDIOCODEC:
            case CN_AUDIOFREQUENCY:
            case CN_AUDIOCHANNEL:
            case CN_SUBTITLELANGUAGE:
            case CN_SUBTITLETITLE:
            case CN_PARENTALRATING:
            case CN_PARENTALSYSTEM:
            case CN_PARENTALCOUNTRY:
            case CN_SIZEMB:
            case CN_CREATEDTIME:
            case CN_USERCONTENTFLAG:
                refreshContent(new SourceIndex(channel));

            default:
                logger.debug("Unknown refresh channel: {}", channel);
                break;
        }

    }

    /**
     * Refresh schemes.
     */
    private void refreshSchemes() {
        final List<String> schemeNames = new ArrayList<String>();
        for (final Scheme scheme : getSchemes()) {
            schemeNames.add(scheme.getScheme());
        }
        callback.stateChanged(createChannel(SCHEMES), new StringType(StringUtils.join(schemeNames.toArray())));
    }

    /**
     * Refresh sources.
     */
    private void refreshSources() {
        final List<String> sourceNames = new ArrayList<String>();
        for (final Scheme scheme : getSchemes()) {
            for (final Source source : getSources(scheme)) {
                sourceNames.add(source.getSource());
            }
        }
        callback.stateChanged(createChannel(SOURCES), new StringType(StringUtils.join(sourceNames.toArray())));
    }

    /**
     * Refresh parental rating.
     */
    private void refreshParentalRating() {
        final ParentalRatingSetting prs = getParentalRatingSetting();
        if (prs != null) {
            callback.stateChanged(createChannel(PR_RATINGTYPEAGE, PARENTALRATING),
                    new DecimalType(prs.getRatingTypeAge()));
            callback.stateChanged(createChannel(PR_RATINGTYPESONY, PARENTALRATING),
                    new StringType(prs.getRatingTypeSony()));
            callback.stateChanged(createChannel(PR_RATINGCOUNTRY, PARENTALRATING),
                    new StringType(prs.getRatingCountry()));
            callback.stateChanged(createChannel(PR_RATINGCUSTOMTYPETV, PARENTALRATING),
                    new StringType(StringUtils.join(prs.getRatingCustomTypeTV())));
            callback.stateChanged(createChannel(PR_RATINGCUSTOMTYPEMPAA, PARENTALRATING),
                    new StringType(prs.getRatingCustomTypeMpaa()));
            callback.stateChanged(createChannel(PR_RATINGCUSTOMTYPECAENGLISH, PARENTALRATING),
                    new StringType(prs.getRatingCustomTypeCaEnglish()));
            callback.stateChanged(createChannel(PR_RATINGCUSTOMTYPECAFRENCH, PARENTALRATING),
                    new StringType(prs.getRatingCustomTypeCaFrench()));
            callback.stateChanged(createChannel(PR_UNRATEDLOCK, PARENTALRATING),
                    prs.isUnratedLock() ? OnOffType.ON : OnOffType.OFF);
        }
    }

    /**
     * Refresh playing content info.
     */
    private void refreshPlayingContentInfo() {
        final PlayingContentInfo pci = getPlayingContentInfo();

        final String uri = pci == null ? null : pci.getUri();

        itemsLock.lock();
        try {
            for (Entry<SourceIndex, ContentItem> entry : itemsByIndex.entrySet()) {
                final SourceIndex si = entry.getKey();
                final ContentItem ci = entry.getValue();

                final boolean isRunning = StringUtils.equalsIgnoreCase(ci.getUri(), uri);
                final boolean wasRunning = ci.isRunning();

                if (isRunning != wasRunning) {
                    ci.setRunning(isRunning);

                    final String index = convertIndex(si.getIndex());
                    callback.stateChanged(createChannel(CN_STATUS, CONTENT, si.getScheme(), si.getSource(), index),
                            ci.isRunning() ? OnOffType.ON : OnOffType.OFF);
                }
            }

        } finally {
            itemsLock.unlock();
        }

        if (pci != null) {

            callback.stateChanged(createChannel(PL_URI, PLAYING), new StringType(pci.getUri()));
            callback.stateChanged(createChannel(PL_SOURCE, PLAYING), new StringType(pci.getSource()));
            callback.stateChanged(createChannel(PL_TITLE, PLAYING), new StringType(pci.getTitle()));
            callback.stateChanged(createChannel(PL_DISPNUM, PLAYING), new StringType(pci.getDispNum()));
            callback.stateChanged(createChannel(PL_ORIGINALDISPNUM, PLAYING), new StringType(pci.getOriginalDispNum()));
            callback.stateChanged(createChannel(PL_TRIPLETSTR, PLAYING), new StringType(pci.getTripletStr()));
            callback.stateChanged(createChannel(PL_PROGRAMNUM, PLAYING), new DecimalType(pci.getProgramNum()));
            callback.stateChanged(createChannel(PL_PROGRAMTITLE, PLAYING), new StringType(pci.getProgramTitle()));
            callback.stateChanged(createChannel(PL_STARTDATETIME, PLAYING), new StringType(pci.getStartDateTime()));
            callback.stateChanged(createChannel(PL_DURATIONSEC, PLAYING), new DecimalType(pci.getDurationSec()));
            callback.stateChanged(createChannel(PL_MEDIATYPE, PLAYING), new StringType(pci.getMediaType()));
            callback.stateChanged(createChannel(PL_PLAYSPEED, PLAYING), new StringType(pci.getPlaySpeed()));
            callback.stateChanged(createChannel(PL_BIVLSERVICEID, PLAYING), new StringType(pci.getBivlServiceId()));
            callback.stateChanged(createChannel(PL_BIVLASSETID, PLAYING), new StringType(pci.getBivlAssetId()));
            callback.stateChanged(createChannel(PL_BIVLPROVIDER, PLAYING), new StringType(pci.getBivlProvider()));

        }
    }

    /**
     * Refresh current external input status.
     */
    private void refreshCurrentExternalInputStatus() {
        for (CurrentExternalInputsStatus ceis : getCurrentExternalInputsStatus()) {
            callback.stateChanged(createChannel(IN_URI, INPUTSTATUS, ceis.getUri()), new StringType(ceis.getUri()));
            callback.stateChanged(createChannel(IN_TITLE, INPUTSTATUS, ceis.getUri()), new StringType(ceis.getTitle()));
            callback.stateChanged(createChannel(IN_CONNECTION, INPUTSTATUS, ceis.getUri()),
                    ceis.isConnection() ? OnOffType.ON : OnOffType.OFF);
            callback.stateChanged(createChannel(IN_LABEL, INPUTSTATUS, ceis.getUri()), new StringType(ceis.getLabel()));
            callback.stateChanged(createChannel(IN_ICON, INPUTSTATUS, ceis.getUri()), new StringType(ceis.getIcon()));
            callback.stateChanged(createChannel(IN_STATUS, INPUTSTATUS, ceis.getUri()),
                    new StringType(ceis.getStatus()));
        }
    }

    /**
     * Refresh content status.
     *
     * @param si the si
     */
    private void refreshContentStatus(SourceIndex si) {

        itemsLock.lock();
        try {
            ContentItem contentItem = itemsByIndex.get(si);
            if (contentItem == null) {
                final ContentListResult clr = getContentListResult(si);
                if (clr != null) {
                    contentItem = new ContentItem(clr.getUri());
                    itemsByIndex.put(si, contentItem);
                }
            }

            if (contentItem != null) {
                final String index = convertIndex(si.getIndex());

                callback.stateChanged(createChannel(CN_STATUS, CONTENT, si.getScheme(), si.getSource(), index),
                        contentItem.isRunning() ? OnOffType.ON : OnOffType.OFF);
            }
        } finally {
            itemsLock.unlock();
        }
    }

    /**
     * Refresh content.
     *
     * @param si the si
     */
    private void refreshContent(SourceIndex si) {

        final ContentListResult clr = getContentListResult(si);
        if (clr != null) {

            final String index = convertIndex(si.getIndex());

            callback.stateChanged(createChannel(CN_URI, CONTENT, si.getScheme(), si.getSource(), index),
                    new StringType(clr.getUri()));

            callback.stateChanged(createChannel(CN_IDX, CONTENT, si.getScheme(), si.getSource(), index),
                    new DecimalType(clr.getIndex()));

            if (clr.hasTitle()) {
                callback.stateChanged(createChannel(CN_TITLE, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getTitle()));
            }

            if (clr.hasDispNum()) {
                callback.stateChanged(createChannel(CN_DISPNUM, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getDispNum()));
            }

            if (clr.hasOriginalDispNum()) {
                callback.stateChanged(createChannel(CN_ORIGINALDISPNUM, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getOriginalDispNum()));
            }

            if (clr.hasTripletStr()) {
                callback.stateChanged(createChannel(CN_TRIPLETSTR, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getTripletStr()));
            }

            if (clr.hasProgramNum()) {
                callback.stateChanged(createChannel(CN_PROGRAMNUM, CONTENT, si.getScheme(), si.getSource(), index),
                        new DecimalType(clr.getProgramNum()));
            }

            if (clr.hasProgramMediaType()) {
                callback.stateChanged(
                        createChannel(CN_PROGRAMMEDIATYPE, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getProgramMediaType()));
            }

            if (clr.hasDirectRemoteNum()) {
                callback.stateChanged(createChannel(CN_DIRECTREMOTENUM, CONTENT, si.getScheme(), si.getSource(), index),
                        new DecimalType(clr.getDirectRemoteNum()));
            }

            if (clr.hasEpgVisibility()) {
                callback.stateChanged(createChannel(CN_EPGVISIBILITY, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getEpgVisibility()));
            }

            if (clr.hasChannelSurfingVisibility()) {
                callback.stateChanged(
                        createChannel(CN_CHANNELSURFINGVISIBILITY, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getChannelSurfingVisibility()));
            }

            if (clr.hasVisibility()) {
                callback.stateChanged(createChannel(CN_VISIBILITY, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getVisibility()));
            }

            if (clr.hasStartDateTime()) {
                callback.stateChanged(createChannel(CN_STARTDATETIME, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getStartDateTime()));
            }

            if (clr.hasChannelName()) {
                callback.stateChanged(createChannel(CN_CHANNELNAME, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getChannelName()));
            }

            if (clr.hasFileSizeByte()) {
                callback.stateChanged(createChannel(CN_FILESIZEBYTE, CONTENT, si.getScheme(), si.getSource(), index),
                        new DecimalType(clr.getFileSizeByte()));
            }

            if (clr.hasIsProtected()) {
                callback.stateChanged(createChannel(CN_ISPROTECTED, CONTENT, si.getScheme(), si.getSource(), index),
                        clr.isProtected() ? OnOffType.ON : OnOffType.OFF);
            }

            if (clr.hasIsAlreadyPlayed()) {
                callback.stateChanged(createChannel(CN_ISALREADYPLAYED, CONTENT, si.getScheme(), si.getSource(), index),
                        clr.isAlreadyPlayed() ? OnOffType.ON : OnOffType.OFF);
            }

            if (clr.hasProductID()) {
                callback.stateChanged(createChannel(CN_PRODUCTID, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getProductID()));
            }

            if (clr.hasContentType()) {
                callback.stateChanged(createChannel(CN_CONTENTTYPE, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getContentType()));
            }

            if (clr.hasStorageUri()) {
                callback.stateChanged(createChannel(CN_STORAGEURI, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getStorageUri()));
            }

            if (clr.hasVideoCodec()) {
                callback.stateChanged(createChannel(CN_VIDEOCODEC, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getVideoCodec()));
            }

            if (clr.hasChapterCount()) {
                callback.stateChanged(createChannel(CN_CHAPTERCOUNT, CONTENT, si.getScheme(), si.getSource(), index),
                        new DecimalType(clr.getChapterCount()));
            }

            if (clr.hasDurationSec()) {
                callback.stateChanged(createChannel(CN_DURATIONSEC, CONTENT, si.getScheme(), si.getSource(), index),
                        new DecimalType(clr.getDurationSec()));
            }

            if (clr.hasAudioCodec()) {
                callback.stateChanged(createChannel(CN_AUDIOCODEC, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(StringUtils.join(clr.getAudioCodec())));
            }

            if (clr.hasAudioFrequency()) {
                callback.stateChanged(createChannel(CN_AUDIOFREQUENCY, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(StringUtils.join(clr.getAudioFrequency())));
            }

            if (clr.hasAudioChannel()) {
                callback.stateChanged(createChannel(CN_AUDIOCHANNEL, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(StringUtils.join(clr.getAudioChannel())));
            }

            if (clr.hasSubtitleLanguage()) {
                callback.stateChanged(
                        createChannel(CN_SUBTITLELANGUAGE, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(StringUtils.join(clr.getSubtitleLanguage())));
            }

            if (clr.hasSubtitleTitle()) {
                callback.stateChanged(createChannel(CN_SUBTITLETITLE, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(StringUtils.join(clr.getSubtitleTitle())));
            }

            if (clr.hasParentalRating()) {
                callback.stateChanged(createChannel(CN_PARENTALRATING, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(StringUtils.join(clr.getParentalRating())));
            }

            if (clr.hasParentalSystem()) {
                callback.stateChanged(createChannel(CN_PARENTALSYSTEM, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(StringUtils.join(clr.getParentalSystem())));
            }

            if (clr.hasParentalCountry()) {
                callback.stateChanged(createChannel(CN_PARENTALCOUNTRY, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(StringUtils.join(clr.getParentalCountry())));
            }

            if (clr.hasSizeMB()) {
                callback.stateChanged(createChannel(CN_SIZEMB, CONTENT, si.getScheme(), si.getSource(), index),
                        new DecimalType(clr.getSizeMB()));
            }

            if (clr.hasCreatedTime()) {
                callback.stateChanged(createChannel(CN_CREATEDTIME, CONTENT, si.getScheme(), si.getSource(), index),
                        new StringType(clr.getCreatedTime()));
            }

            if (clr.hasUserContentFlag()) {
                callback.stateChanged(createChannel(CN_USERCONTENTFLAG, CONTENT, si.getScheme(), si.getSource(), index),
                        clr.isUserContentFlag() ? OnOffType.ON : OnOffType.OFF);
            }

        }
    }

    /**
     * Gets the content item.
     *
     * @param si the si
     * @return the content item
     */
    private ContentItem getContentItem(SourceIndex si) {
        ContentItem ci;
        itemsLock.lock();
        try {
            return itemsByIndex.get(si);
        } finally {
            itemsLock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#setChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel, org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void setChannel(ScalarWebChannel channel, Command command) {
        final String[] paths = channel.getPaths();
        if (paths.length != 4) {
            logger.debug("Channel path invalid: {}", channel);
        } else {
            final String target = paths[0];

            switch (target) {
                case CONTENT:
                    final SourceIndex si = new SourceIndex(channel);

                    final ContentItem ci = getContentItem(si);
                    if (ci == null) {
                        // TODO Log
                        return;
                    }

                    switch (channel.getId()) {
                        case CN_STATUS: {
                            if (command instanceof OnOffType) {
                                setContentStatus(si, ci, command == OnOffType.ON);
                            } else {
                                logger.debug("CONTENT STATUS command not an OnOffType: {}", command);

                            }
                            break;
                        }
                        case CN_ISPROTECTED: {
                            if (command instanceof OnOffType) {
                                setContentProtection(ci, command == OnOffType.ON);
                            } else {
                                logger.debug("CONTENT ISPROTECTED command not an OnOffType: {}", command);
                            }
                            break;
                        }
                        case CN_EPGVISIBILITY: {
                            if (command instanceof StringType) {
                                setTvContentVisibility(ci, command.toString(), null, null);
                            } else {
                                logger.debug("CONTENT EPGVISIBILITY command not an StringType: {}", command);
                            }
                            break;
                        }
                        case CN_CHANNELSURFINGVISIBILITY: {
                            if (command instanceof StringType) {
                                setTvContentVisibility(ci, null, command.toString(), null);
                            } else {
                                logger.debug("CONTENT CHANNELSURFINGVISIBILITY command not an StringType: {}", command);
                            }
                            break;
                        }
                        case CN_VISIBILITY: {
                            if (command instanceof StringType) {
                                setTvContentVisibility(ci, null, null, command.toString());
                            } else {
                                logger.debug("CONTENT VISIBILITY command not an StringType: {}", command);
                            }
                            break;
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Sets the tv content visibility.
     *
     * @param ci the ci
     * @param epgVisibility the epg visibility
     * @param channelSurfingVisibility the channel surfing visibility
     * @param visibility the visibility
     */
    private void setTvContentVisibility(ContentItem ci, String epgVisibility, String channelSurfingVisibility,
            String visibility) {
        handleExecute(ScalarWebMethod.SetTvContentVisibility,
                new TvContentVisibility(ci.getUri(), epgVisibility, channelSurfingVisibility, visibility));
    }

    /**
     * Sets the content protection.
     *
     * @param ci the ci
     * @param on the on
     */
    private void setContentProtection(ContentItem ci, boolean on) {
        handleExecute(ScalarWebMethod.SetDeleteProtection, new DeleteProtection(ci.getUri(), on));
    }

    /**
     * Sets the content status.
     *
     * @param si the si
     * @param ci the ci
     * @param on the on
     */
    private void setContentStatus(SourceIndex si, ContentItem ci, boolean on) {
        if (on) {
            handleExecute(ScalarWebMethod.SetPlayContent, new PlayContent(ci.getUri()));
            ci.setRunning(true);

            itemsLock.lock();
            try {
                for (Entry<SourceIndex, ContentItem> entry : itemsByIndex.entrySet()) {
                    final SourceIndex otherSi = entry.getKey();
                    if (si.equals(otherSi)) {
                        continue;
                    }

                    final ContentItem otherCi = entry.getValue();
                    if (otherCi.isRunning()) {
                        otherCi.setRunning(false);

                        final String index = convertIndex(otherSi.getIndex());
                        callback.stateChanged(
                                createChannel(CN_STATUS, CONTENT, otherSi.getScheme(), otherSi.getSource(), index),
                                OnOffType.OFF);

                    }
                }
            } finally {
                itemsLock.unlock();
            }
        } else {
            handleExecute(ScalarWebMethod.DeleteContent, new DeleteContent(ci.getUri()));
        }
    }
}
