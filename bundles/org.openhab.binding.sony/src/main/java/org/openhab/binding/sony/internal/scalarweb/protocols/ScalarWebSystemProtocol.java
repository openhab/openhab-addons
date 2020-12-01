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
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.ircc.IrccClientFactory;
import org.openhab.binding.sony.internal.ircc.models.IrccClient;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.HttpResponse.SOAPError;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebContext;
import org.openhab.binding.sony.internal.scalarweb.VersionUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebEvent;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.api.CurrentTime;
import org.openhab.binding.sony.internal.scalarweb.models.api.Language;
import org.openhab.binding.sony.internal.scalarweb.models.api.LedIndicatorStatus;
import org.openhab.binding.sony.internal.scalarweb.models.api.NotifySettingUpdate;
import org.openhab.binding.sony.internal.scalarweb.models.api.PostalCode;
import org.openhab.binding.sony.internal.scalarweb.models.api.PowerSavingMode;
import org.openhab.binding.sony.internal.scalarweb.models.api.PowerStatusRequest_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.PowerStatusRequest_1_1;
import org.openhab.binding.sony.internal.scalarweb.models.api.PowerStatusResult_1_0;
import org.openhab.binding.sony.internal.scalarweb.models.api.PowerStatusResult_1_1;
import org.openhab.binding.sony.internal.scalarweb.models.api.Scheme;
import org.openhab.binding.sony.internal.scalarweb.models.api.SoftwareUpdate;
import org.openhab.binding.sony.internal.scalarweb.models.api.Source;
import org.openhab.binding.sony.internal.scalarweb.models.api.StorageListItem_1_1;
import org.openhab.binding.sony.internal.scalarweb.models.api.StorageListItem_1_2;
import org.openhab.binding.sony.internal.scalarweb.models.api.StorageListRequest_1_1;
import org.openhab.binding.sony.internal.scalarweb.models.api.StorageListRequest_1_2;
import org.openhab.binding.sony.internal.scalarweb.models.api.SystemInformation;
import org.openhab.binding.sony.internal.scalarweb.models.api.WolMode;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the protocol handles the System service
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
class ScalarWebSystemProtocol<T extends ThingCallback<String>> extends AbstractScalarWebProtocol<T> {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebSystemProtocol.class);

    // Constants used by protocol
    private static final String CURRENTTIME = "currenttime";
    private static final String LEDINDICATORSTATUS = "ledindicatorstatus";
    private static final String POWERSAVINGMODE = "powersavingmode";
    private static final String POWERSTATUS = "powerstatus";
    private static final String WOLMODE = "wolmode";
    private static final String LANGUAGE = "language";
    private static final String REBOOT = "reboot";
    private static final String SYSCMD = "sysCmd";
    private static final String POSTALCODE = "postalcode";
    private static final String DEVICEMISCSETTING = "devicemiscsettings";
    private static final String POWERSETTINGS = "powersettings";
    private static final String SLEEPSETTINGS = "sleepsettings";
    private static final String WUTANGSETTINGS = "wutangsettings";

    private static final String STORAGE = "st_";
    private static final String ST_DEVICENAME = STORAGE + "deviceName";
    private static final String ST_ERROR = STORAGE + "error";
    private static final String ST_FILESYSTEM = STORAGE + "fileSystem";
    private static final String ST_FINALIZESTATUS = STORAGE + "finalizeStatus";
    private static final String ST_FORMAT = STORAGE + "format";
    private static final String ST_FORMATSTATUS = STORAGE + "formatStatus";
    private static final String ST_FORMATTABLE = STORAGE + "formattable";
    private static final String ST_FORMATTING = STORAGE + "formatting";
    private static final String ST_FREECAPACITYMB = STORAGE + "freeCapacityMB";
    private static final String ST_HASNONSTANDARDDATA = STORAGE + "hasNonStandardData";
    private static final String ST_HASUNSUPPORTEDCONTENTS = STORAGE + "hasUnsupportedContents";
    private static final String ST_ISAVAILABLE = STORAGE + "isAvailable";
    private static final String ST_ISLOCKED = STORAGE + "isLocked";
    private static final String ST_ISMANAGEMENTINFOFULL = STORAGE + "isManagementInfoFull";
    private static final String ST_ISPROTECTED = STORAGE + "isProtected";
    private static final String ST_ISREGISTERED = STORAGE + "isRegistered";
    private static final String ST_ISSELFRECORDED = STORAGE + "isSelfRecorded";
    private static final String ST_ISSQVSUPPORTED = STORAGE + "isSqvSupported";
    private static final String ST_LUN = STORAGE + "lun";
    private static final String ST_MOUNTED = STORAGE + "mounted";
    private static final String ST_PERMISSION = STORAGE + "permission";
    private static final String ST_POSITION = STORAGE + "position";
    private static final String ST_PROTOCOL = STORAGE + "protocol";
    private static final String ST_REGISTRATIONDATE = STORAGE + "registrationDate";
    private static final String ST_SYSTEMAREACAPACITYMB = STORAGE + "systemAreaCapacityMB";
    private static final String ST_TIMESECTOFINALIZE = STORAGE + "timeSecToFinalize";
    private static final String ST_TIMESECTOGETCONTENTS = STORAGE + "timeSecToGetContents";
    private static final String ST_TYPE = STORAGE + "type";
    private static final String ST_URI = STORAGE + "uri";
    private static final String ST_USBDEVICETYPE = STORAGE + "usbDeviceType";
    private static final String ST_VOLUMELABEL = STORAGE + "volumeLabel";
    private static final String ST_WHOLECAPACITYMB = STORAGE + "wholeCapacityMB";

    /** The url for the IRCC service */
    private final @Nullable String irccUrl;

    /** The notifications that are enabled */
    private final NotificationHelper notificationHelper;

    /**
     * Instantiates a new scalar web system protocol.
     *
     * @param factory the non-null factory
     * @param context the non-null context
     * @param service the non-null service
     * @param callback the non-null callback
     * @param irccUrl the possibly null, possibly empty ircc url
     */
    ScalarWebSystemProtocol(final ScalarWebProtocolFactory<T> factory, final ScalarWebContext context,
            final ScalarWebService service, final T callback, final @Nullable String irccUrl) {
        super(factory, context, service, callback);

        this.irccUrl = irccUrl;

        notificationHelper = new NotificationHelper(
                enableNotifications(ScalarWebEvent.NOTIFYPOWERSTATUS, ScalarWebEvent.NOTIFYSTORAGESTATUS,
                        ScalarWebEvent.NOTIFYSETTINGSUPDATE, ScalarWebEvent.NOTIFYSWUPDATEINFO));
    }

    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors(final boolean dynamicOnly) {
        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        // no dynamic channels
        if (dynamicOnly) {
            return descriptors;
        }

        if (getService().hasMethod(ScalarWebMethod.GETCURRENTTIME)) {
            try {
                execute(ScalarWebMethod.GETCURRENTTIME);
                descriptors.add(createDescriptor(createChannel(CURRENTTIME), "DateTime", "scalarsystemcurrenttime"));
            } catch (final IOException e) {
                logger.debug("Exception getting current time: {}", e.getMessage());
            }
        }

        if (getService().hasMethod(ScalarWebMethod.GETSYSTEMINFORMATION)) {
            try {
                execute(ScalarWebMethod.GETSYSTEMINFORMATION);
                descriptors.add(createDescriptor(createChannel(LANGUAGE), "String", "scalarsystemlanguage"));
            } catch (final IOException e) {
                logger.debug("Exception getting system information: {}", e.getMessage());
            }
        }

        if (getService().hasMethod(ScalarWebMethod.GETLEDINDICATORSTATUS)) {
            try {
                execute(ScalarWebMethod.GETLEDINDICATORSTATUS);
                descriptors.add(createDescriptor(createChannel(LEDINDICATORSTATUS), "String",
                        "scalarsystemledindicatorstatus"));
            } catch (final IOException e) {
                logger.debug("Exception getting led indicator status: {}", e.getMessage());
            }
        }

        if (getService().hasMethod(ScalarWebMethod.GETPOWERSAVINGMODE)) {
            try {
                execute(ScalarWebMethod.GETPOWERSAVINGMODE);
                descriptors
                        .add(createDescriptor(createChannel(POWERSAVINGMODE), "String", "scalarsystempowersavingmode"));
            } catch (final IOException e) {
                logger.debug("Exception getting power savings mode: {}", e.getMessage());
            }
        }

        if (getService().hasMethod(ScalarWebMethod.GETPOWERSTATUS)) {
            try {
                execute(ScalarWebMethod.GETPOWERSTATUS);
                descriptors.add(createDescriptor(createChannel(POWERSTATUS), "Switch", "scalarsystempowerstatus"));
            } catch (final IOException e) {
                logger.debug("Exception getting power status: {}", e.getMessage());
            }
        }

        if (getService().hasMethod(ScalarWebMethod.GETWOLMODE)) {
            try {
                execute(ScalarWebMethod.GETWOLMODE);
                descriptors.add(createDescriptor(createChannel(WOLMODE), "Switch", "scalarsystemwolmode"));
            } catch (final IOException e) {
                logger.debug("Exception getting wol mode: {}", e.getMessage());
            }
        }

        if (getService().hasMethod(ScalarWebMethod.GETPOSTALCODE)) {
            try {
                execute(ScalarWebMethod.GETPOSTALCODE);
                descriptors.add(createDescriptor(createChannel(POSTALCODE), "String", "scalarsystempostalcode"));
            } catch (final IOException e) {
                logger.debug("Exception getting postal code: {}", e.getMessage());
            }
        }

        if (service.hasMethod(ScalarWebMethod.REQUESTREBOOT)) {
            descriptors.add(createDescriptor(createChannel(REBOOT), "Switch", "scalarsystemreboot"));
        }

        // IRCC should be available by default
        descriptors.add(createDescriptor(createChannel(SYSCMD), "String", "scalarsystemircc"));

        if (service.hasMethod(ScalarWebMethod.GETDEVICEMISCSETTINGS)) {
            addGeneralSettingsDescriptor(descriptors, ScalarWebMethod.GETDEVICEMISCSETTINGS, DEVICEMISCSETTING,
                    "Device Misc Setting");
        }

        if (service.hasMethod(ScalarWebMethod.GETPOWERSETTINGS)) {
            addGeneralSettingsDescriptor(descriptors, ScalarWebMethod.GETPOWERSETTINGS, POWERSETTINGS, "Power Setting");
        }

        if (service.hasMethod(ScalarWebMethod.GETSLEEPTIMERSETTINGS)) {
            addGeneralSettingsDescriptor(descriptors, ScalarWebMethod.GETSLEEPTIMERSETTINGS, SLEEPSETTINGS,
                    "Sleep Timer Settings");
        }

        if (service.hasMethod(ScalarWebMethod.GETWUTANGINFO)) {
            addGeneralSettingsDescriptor(descriptors, ScalarWebMethod.GETWUTANGINFO, WUTANGSETTINGS, "WuTang Settings");
        }

        if (service.hasMethod(ScalarWebMethod.GETSTORAGELIST)) {
            try {
                addStorageListDescriptors(descriptors);
            } catch (final IOException e) {
                logger.debug("Exception getting storage list descriptors: {}", e.getMessage(), e);
            }
        }

        return descriptors;
    }

    /**
     * Add the storage list descriptors
     * 
     * @param descriptors a non-null, possibly empty list of descriptors to add too
     * @throws IOException if an IO exception occurs
     */
    private void addStorageListDescriptors(final List<ScalarWebChannelDescriptor> descriptors) throws IOException {
        Objects.requireNonNull(descriptors, "descriptors cannot be null");

        final String version = getService().getVersion(ScalarWebMethod.GETSTORAGELIST);
        if (VersionUtilities.equals(version, ScalarWebMethod.V1_1)) {
            for (final StorageListItem_1_1 sl : execute(ScalarWebMethod.GETSTORAGELIST, new StorageListRequest_1_1())
                    .asArray(StorageListItem_1_1.class)) {
                final String uri = sl.getUri();
                if (uri == null || StringUtils.isEmpty(uri)) {
                    logger.debug("Storage List had no URI (which is required): {}", sl);
                    continue;
                }

                final String sourcePart = Source.getSourcePart(uri).toUpperCase();
                final String id = getStorageChannelId(uri);

                descriptors.add(createDescriptor(createChannel(ST_DEVICENAME, id), "String",
                        "scalarsystemstoragedevicename", "Storage Name (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_ERROR, id), "String", "scalarsystemstorageerror",
                        "Storage Error (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_FORMAT, id), "String", "scalarsystemstorageformat",
                        "Storage Format (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_FORMATTABLE, id), "String",
                        "scalarsystemstorageformattable", "Storage Formattable Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_FORMATTING, id), "String",
                        "scalarsystemstorageformatting", "Storage Formatting Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_FREECAPACITYMB, id), "Number:DataAmount",
                        "scalarsystemstoragefreecapacitymb", "Storage Free Capacity (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_ISAVAILABLE, id), "String",
                        "scalarsystemstorageisavailable", "Storage Available Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_LUN, id), "Number", "scalarsystemstoragelun",
                        "Storage LUN (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_MOUNTED, id), "String", "scalarsystemstoragemounted",
                        "Storage Mount Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_PERMISSION, id), "String",
                        "scalarsystemstoragepermission", "Storage Permission Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_POSITION, id), "String",
                        "scalarsystemstorageposition", "Storage Position (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_SYSTEMAREACAPACITYMB, id), "Number:DataAmount",
                        "scalarsystemstoragesystemareacapacitymb", "Storage System Capacity (" + sourcePart + ")",
                        null));
                descriptors.add(createDescriptor(createChannel(ST_TYPE, id), "String", "scalarsystemstoragetype",
                        "Storage Type (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_URI, id), "String", "scalarsystemstorageuri",
                        "Storage URI (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_VOLUMELABEL, id), "String",
                        "scalarsystemstoragevolumelabel", "Storage Label (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_WHOLECAPACITYMB, id), "Number:DataAmount",
                        "scalarsystemstoragewholecapacitymb", "Storage Whole Capacity (" + sourcePart + ")", null));
            }
        } else {
            for (final StorageListItem_1_2 sl : execute(ScalarWebMethod.GETSTORAGELIST, new StorageListRequest_1_2())
                    .asArray(StorageListItem_1_2.class)) {
                final String uri = sl.getUri();
                if (uri == null || StringUtils.isEmpty(uri)) {
                    logger.debug("Storage List had no URI (which is required): {}", sl);
                    continue;
                }

                final String sourcePart = Source.getSourcePart(uri).toUpperCase();
                final String id = getStorageChannelId(uri);

                descriptors.add(createDescriptor(createChannel(ST_DEVICENAME, id), "String",
                        "scalarsystemstoragedevicename", "Storage Name (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_ERROR, id), "String", "scalarsystemstorageerror",
                        "Storage Error (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_FILESYSTEM, id), "String",
                        "scalarsystemstoragefilesystem", "Storage File Size (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_FINALIZESTATUS, id), "String",
                        "scalarsystemstoragefinalizestatus", "Storage Finalize Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_FORMAT, id), "String", "scalarsystemstorageformat",
                        "Storage Format (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_FORMATSTATUS, id), "String",
                        "scalarsystemstorageformatstatus", "Storage Format Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_FORMATTABLE, id), "String",
                        "scalarsystemstorageformattable", "Storage Formattable Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_FREECAPACITYMB, id), "Number:DataAmount",
                        "scalarsystemstoragefreecapacitymb", "Storage Free Capacity (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_HASNONSTANDARDDATA, id), "String",
                        "scalarsystemstoragehasnonstandarddata", "Storage Non-standard Data (" + sourcePart + ")",
                        null));
                descriptors.add(createDescriptor(createChannel(ST_HASUNSUPPORTEDCONTENTS, id), "String",
                        "scalarsystemstoragehasunsupportedcontents",
                        "Storage Unsupported Contents (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_ISAVAILABLE, id), "String",
                        "scalarsystemstorageisavailable", "Storage Available Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_ISLOCKED, id), "String",
                        "scalarsystemstorageislocked", "Storage Locked Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_ISMANAGEMENTINFOFULL, id), "String",
                        "scalarsystemstorageismanagementinfofull",
                        "Storage Management Info Full Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_ISPROTECTED, id), "String",
                        "scalarsystemstorageisprotected", "Storage Protection Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_ISREGISTERED, id), "String",
                        "scalarsystemstorageisregistered", "Storage Registered Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_ISSELFRECORDED, id), "String",
                        "scalarsystemstorageisselfrecorded", "Storage Self Recorded Status (" + sourcePart + ")",
                        null));
                descriptors.add(createDescriptor(createChannel(ST_ISSQVSUPPORTED, id), "String",
                        "scalarsystemstorageissqvsupported", "Storage SQV Supported Status (" + sourcePart + ")",
                        null));
                descriptors.add(createDescriptor(createChannel(ST_LUN, id), "Number", "scalarsystemstoragelun",
                        "Storage LUN (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_MOUNTED, id), "String", "scalarsystemstoragemounted",
                        "Storage Mount Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_PERMISSION, id), "String",
                        "scalarsystemstoragepermission", "Storage Permission Status (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_POSITION, id), "String",
                        "scalarsystemstorageposition", "Storage Position (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_PROTOCOL, id), "String",
                        "scalarsystemstorageprotocol", "Storage Protocol (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_REGISTRATIONDATE, id), "String",
                        "scalarsystemstorageregistrationdate", "Storage Registration Date (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_SYSTEMAREACAPACITYMB, id), "Number:DataAmount",
                        "scalarsystemstoragesystemareacapacitymb", "Storage System Capacity (" + sourcePart + ")",
                        null));
                descriptors.add(createDescriptor(createChannel(ST_TIMESECTOFINALIZE, id), "Number:Time",
                        "scalarsystemstoragetimesectofinalize", "Storage Finalization Time (" + sourcePart + ")",
                        null));
                descriptors.add(createDescriptor(createChannel(ST_TIMESECTOGETCONTENTS, id), "Number:Time",
                        "scalarsystemstoragetimesectogetcontents", "Storage Get Contents Time (" + sourcePart + ")",
                        null));
                descriptors.add(createDescriptor(createChannel(ST_TYPE, id), "String", "scalarsystemstoragetype",
                        "Storage Type (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_URI, id), "String", "scalarsystemstorageuri",
                        "Storage URI (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_USBDEVICETYPE, id), "String",
                        "scalarsystemstorageusbdevicetype", "Storage USB Type (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_VOLUMELABEL, id), "String",
                        "scalarsystemstoragevolumelabel", "Storage Label (" + sourcePart + ")", null));
                descriptors.add(createDescriptor(createChannel(ST_WHOLECAPACITYMB, id), "Number:DataAmount",
                        "scalarsystemstoragewholecapacitymb", "Storage Whole Capacity (" + sourcePart + ")", null));
            }
        }
    }

    /**
     * Gets the channel ID from a given storage URI (commonly 'storage:xxx'). If the scheme is "storage", simply use the
     * source part. If the scheme is not storage - then reformat to 'scheme-src'
     * 
     * @param uri a non-null, non-empty URI
     * @return a channel id for the uri
     */
    private String getStorageChannelId(final String uri) {
        Validate.notEmpty(uri, "uri cannot be empty");
        final String scheme = Source.getSchemePart(uri);
        final String src = Source.getSourcePart(uri);

        return SonyUtil.createValidChannelUId(
                (StringUtils.equalsIgnoreCase(scheme, Scheme.STORAGE) ? "" : (scheme + "-")) + src);
    }

    @Override
    public void refreshState(boolean initial) {
        final ScalarWebChannelTracker tracker = getChannelTracker();
        if (tracker.isCategoryLinked(CURRENTTIME)) {
            refreshCurrentTime();
        }
        if (tracker.isCategoryLinked(LEDINDICATORSTATUS)) {
            refreshLedIndicator();
        }
        if (tracker.isCategoryLinked(LANGUAGE)) {
            refreshLanguage();
        }
        if (tracker.isCategoryLinked(POWERSAVINGMODE)) {
            refreshPowerSavingsMode();
        }

        if (initial || !notificationHelper.isEnabled(ScalarWebEvent.NOTIFYPOWERSTATUS)) {
            if (tracker.isCategoryLinked(POWERSTATUS)) {
                refreshPowerStatus();
            }
        }
        if (tracker.isCategoryLinked(WOLMODE)) {
            refreshWolMode();
        }
        if (tracker.isCategoryLinked(REBOOT)) {
            refreshReboot();
        }
        if (tracker.isCategoryLinked(POSTALCODE)) {
            refreshPostalCode();
        }
        if (tracker.isCategoryLinked(SYSCMD)) {
            refreshSysCmd();
        }

        if (initial || !notificationHelper.isEnabled(ScalarWebEvent.NOTIFYSETTINGSUPDATE)) {
            if (tracker.isCategoryLinked(DEVICEMISCSETTING)) {
                refreshGeneralSettings(tracker.getLinkedChannelsForCategory(DEVICEMISCSETTING),
                        ScalarWebMethod.GETDEVICEMISCSETTINGS);
            }
            if (tracker.isCategoryLinked(POWERSETTINGS)) {
                refreshGeneralSettings(tracker.getLinkedChannelsForCategory(POWERSETTINGS),
                        ScalarWebMethod.GETPOWERSETTINGS);
            }
            if (tracker.isCategoryLinked(SLEEPSETTINGS)) {
                refreshGeneralSettings(tracker.getLinkedChannelsForCategory(SLEEPSETTINGS),
                        ScalarWebMethod.GETSLEEPTIMERSETTINGS);
            }
            if (tracker.isCategoryLinked(WUTANGSETTINGS)) {
                refreshGeneralSettings(tracker.getLinkedChannelsForCategory(WUTANGSETTINGS),
                        ScalarWebMethod.GETWUTANGINFO);
            }
        }

        if (initial || !notificationHelper.isEnabled(ScalarWebEvent.NOTIFYSTORAGESTATUS)) {
            if (tracker.isCategoryLinked(ctgy -> StringUtils.startsWith(ctgy, STORAGE))) {
                refreshStorage();
            }
        }
    }

    @Override
    public void refreshChannel(final ScalarWebChannel channel) {
        Objects.requireNonNull(channel, "channel cannot be null");

        switch (channel.getCategory()) {
            case CURRENTTIME:
                refreshCurrentTime();
                break;

            case LANGUAGE:
                refreshLanguage();
                break;

            case LEDINDICATORSTATUS:
                refreshLedIndicator();
                break;

            case POWERSAVINGMODE:
                refreshPowerSavingsMode();
                break;

            case POWERSTATUS:
                refreshPowerStatus();
                break;

            case WOLMODE:
                refreshWolMode();
                break;

            case REBOOT:
                refreshReboot();
                break;

            case POSTALCODE:
                refreshPostalCode();
                break;

            case SYSCMD:
                refreshSysCmd();
                break;

            case DEVICEMISCSETTING:
                refreshGeneralSettings(Collections.singleton(channel), ScalarWebMethod.GETDEVICEMISCSETTINGS);
                break;

            case POWERSETTINGS:
                refreshGeneralSettings(Collections.singleton(channel), ScalarWebMethod.GETPOWERSETTINGS);
                break;

            case SLEEPSETTINGS:
                refreshGeneralSettings(Collections.singleton(channel), ScalarWebMethod.GETSLEEPTIMERSETTINGS);
                break;

            case WUTANGSETTINGS:
                refreshGeneralSettings(Collections.singleton(channel), ScalarWebMethod.GETWUTANGINFO);
                break;

            default:
                final String ctgy = channel.getCategory();
                if (StringUtils.startsWith(ctgy, STORAGE)) {
                    refreshStorage();
                } else {
                    logger.debug("Unknown refresh channel: {}", channel);
                }
                break;
        }
    }

    /**
     * Refresh current time
     */
    private void refreshCurrentTime() {
        try {
            final CurrentTime ct = execute(ScalarWebMethod.GETCURRENTTIME).as(CurrentTime.class);
            stateChanged(CURRENTTIME,
                    new DateTimeType(
                            ZonedDateTime.ofInstant(ct.getDateTime().toCalendar(Locale.getDefault()).toInstant(),
                                    TimeZone.getDefault().toZoneId()).withFixedOffsetZone()));
        } catch (final IOException e) {
            logger.debug("Cannot get the current time: {}", e.getMessage());
        }
    }

    /**
     * Refresh the language
     */
    private void refreshLanguage() {
        try {
            final SystemInformation sysInfo = execute(ScalarWebMethod.GETSYSTEMINFORMATION).as(SystemInformation.class);
            stateChanged(LANGUAGE, SonyUtil.newStringType(sysInfo.getLanguage()));
        } catch (final IOException e) {
            logger.debug("Cannot get the get system information for refresh langauge: {}", e.getMessage());
        }
    }

    /**
     * Refresh led indicator
     */
    private void refreshLedIndicator() {
        try {
            final LedIndicatorStatus ledStatus = execute(ScalarWebMethod.GETLEDINDICATORSTATUS)
                    .as(LedIndicatorStatus.class);
            stateChanged(LEDINDICATORSTATUS, SonyUtil.newStringType(ledStatus.getMode()));
        } catch (final IOException e) {
            logger.debug("Cannot get the get led indicator status: {}", e.getMessage());
        }
    }

    /**
     * Refresh power savings mode
     */
    private void refreshPowerSavingsMode() {
        try {
            final PowerSavingMode mode = execute(ScalarWebMethod.GETPOWERSAVINGMODE).as(PowerSavingMode.class);
            stateChanged(POWERSAVINGMODE, SonyUtil.newStringType(mode.getMode()));
        } catch (final IOException e) {
            logger.debug("Cannot get the get power savings mode: {}", e.getMessage());
        }
    }

    /**
     * Refresh postal code
     */
    private void refreshPostalCode() {
        try {
            final PostalCode postalCode = execute(ScalarWebMethod.GETPOSTALCODE).as(PostalCode.class);
            stateChanged(POSTALCODE, SonyUtil.newStringType(postalCode.getPostalCode()));
        } catch (final IOException e) {
            logger.debug("Cannot get the get postal code: {}", e.getMessage());
        }
    }

    /**
     * Refresh power status
     */
    private void refreshPowerStatus() {
        try {
            if (VersionUtilities.equals(getVersion(ScalarWebMethod.GETPOWERSTATUS), ScalarWebMethod.V1_0)) {
                notifyPowerStatus(execute(ScalarWebMethod.GETPOWERSTATUS).as(PowerStatusResult_1_0.class));
            } else {
                notifyPowerStatus(execute(ScalarWebMethod.GETPOWERSTATUS).as(PowerStatusResult_1_1.class));
            }
        } catch (final IOException e) {
            logger.debug("Cannot refresh the power status: {}", e.getMessage());
        }
    }

    /**
     * Refresh wol mode
     */
    private void refreshWolMode() {
        try {
            final WolMode mode = execute(ScalarWebMethod.GETWOLMODE).as(WolMode.class);
            stateChanged(WOLMODE, mode.isEnabled() ? OnOffType.ON : OnOffType.OFF);
        } catch (final IOException e) {
            logger.debug("Cannot get the get WOL mode: {}", e.getMessage());
        }
    }

    /**
     * Refresh reboot
     */
    private void refreshReboot() {
        callback.stateChanged(REBOOT, OnOffType.OFF);
    }

    /**
     * Refresh system command
     */
    private void refreshSysCmd() {
        callback.stateChanged(SYSCMD, StringType.EMPTY);
    }

    /**
     * Refresh the playing content info
     */
    private void refreshStorage() {
        try {
            final String version = getService().getVersion(ScalarWebMethod.GETSTORAGELIST);
            if (VersionUtilities.equals(version, ScalarWebMethod.V1_1)) {
                execute(ScalarWebMethod.GETSTORAGELIST, new StorageListRequest_1_1()).asArray(StorageListItem_1_1.class)
                        .forEach(sl -> notifyStorageStatus(sl));
            } else {
                execute(ScalarWebMethod.GETSTORAGELIST, new StorageListRequest_1_2()).asArray(StorageListItem_1_2.class)
                        .forEach(sl -> notifyStorageStatus(sl));
            }
        } catch (final IOException e) {
            logger.debug("Error refreshing playing content info {}", e.getMessage());
        }
    }

    @Override
    public void setChannel(final ScalarWebChannel channel, final Command command) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        switch (channel.getCategory()) {
            case LEDINDICATORSTATUS:
                if (command instanceof StringType) {
                    setLedIndicatorStatus(command.toString());
                } else {
                    logger.debug("LEDINDICATORSTATUS command not an StringType: {}", command);
                }

                break;

            case LANGUAGE:
                if (command instanceof StringType) {
                    setLanguage(command.toString());
                } else {
                    logger.debug("LANGUAGE command not an StringType: {}", command);
                }

                break;

            case POWERSAVINGMODE:
                if (command instanceof StringType) {
                    setPowerSavingMode(command.toString());
                } else {
                    logger.debug("POWERSAVINGMODE command not an StringType: {}", command);
                }

                break;

            case POWERSTATUS:
                if (command instanceof OnOffType) {
                    setPowerStatus(command == OnOffType.ON);
                } else {
                    logger.debug("POWERSTATUS command not an OnOffType: {}", command);
                }

                break;

            case WOLMODE:
                if (command instanceof OnOffType) {
                    setWolMode(command == OnOffType.ON);
                } else {
                    logger.debug("WOLMODE command not an OnOffType: {}", command);
                }

                break;

            case REBOOT:
                if (command instanceof OnOffType && command == OnOffType.ON) {
                    requestReboot();
                } else {
                    logger.debug("REBOOT command not an OnOffType: {}", command);
                }

                break;

            case POSTALCODE:
                if (command instanceof StringType) {
                    setPostalCode(command.toString());
                } else {
                    logger.debug("POSTALCODE command not an StringType: {}", command);
                }

                break;

            case SYSCMD:
                if (command instanceof StringType) {
                    sendIrccCommand(command.toString());
                } else {
                    logger.debug("SYSCMD command not an StringType: {}", command);
                }

                break;

            case DEVICEMISCSETTING:
                setGeneralSetting(ScalarWebMethod.SETDEVICEMISSETTINGS, channel, command);
                break;

            case POWERSETTINGS:
                setGeneralSetting(ScalarWebMethod.SETPOWERSETTINGS, channel, command);
                break;

            case SLEEPSETTINGS:
                setGeneralSetting(ScalarWebMethod.SETSLEEPTIMERSETTINGS, channel, command);
                break;

            case WUTANGSETTINGS:
                setGeneralSetting(ScalarWebMethod.SETWUTANGINFO, channel, command);
                break;

            default:
                logger.debug("Unhandled channel command: {} - {}", channel, command);
                break;
        }
    }

    /**
     * Sets the led indicator status
     *
     * @param mode the non-null, non-empty new led indicator status
     */
    private void setLedIndicatorStatus(final String mode) {
        Validate.notEmpty(mode, "mode cannot be empty");
        handleExecute(ScalarWebMethod.SETLEDINDICATORSTATUS, new LedIndicatorStatus(mode, ""));
    }

    /**
     * Sets the language
     *
     * @param language the non-null, non-empty new language
     */
    private void setLanguage(final String language) {
        Validate.notEmpty(language, "language cannot be empty");
        handleExecute(ScalarWebMethod.SETLANGUAGE, new Language(language));
    }

    /**
     * Sets the power saving mode
     *
     * @param mode the non-null, non-empty new power saving mode
     */
    private void setPowerSavingMode(final String mode) {
        Validate.notEmpty(mode, "mode cannot be empty");
        handleExecute(ScalarWebMethod.SETPOWERSAVINGMODE, new PowerSavingMode(mode));
    }

    /**
     * Sets the power status
     *
     * @param status true for on, false otherwise
     */
    private void setPowerStatus(final boolean status) {
        if (status) {
            SonyUtil.sendWakeOnLan(logger, getContext().getConfig().getDeviceIpAddress(),
                    getContext().getConfig().getDeviceMacAddress());
        }

        handleExecute(ScalarWebMethod.SETPOWERSTATUS, version -> {
            if (VersionUtilities.equals(version, ScalarWebMethod.V1_0)) {
                return new PowerStatusRequest_1_0(status);
            }
            return new PowerStatusRequest_1_1(status);
        });
    }

    /**
     * Sets the wol mode
     *
     * @param enabled true to enable WOL, false otherwise
     */
    private void setWolMode(final boolean enabled) {
        handleExecute(ScalarWebMethod.SETWOLMODE, new WolMode(enabled));
    }

    /**
     * Sets the postal code
     *
     * @param postalCode the non-null, non-empty new postal code
     */
    private void setPostalCode(final String postalCode) {
        Validate.notEmpty(postalCode, "postalCode cannot be empty");
        handleExecute(ScalarWebMethod.SETPOSTALCODE, new PostalCode(postalCode));
    }

    /**
     * Request reboot
     */
    private void requestReboot() {
        handleExecute(ScalarWebMethod.REQUESTREBOOT);
    }

    /**
     * Send an IRCC command
     *
     * @param cmd a possibly null, possibly empty IRCC command to send
     */
    public void sendIrccCommand(final String cmd) {
        if (StringUtils.isEmpty(cmd)) {
            return;
        }

        final String localIrccUrl = irccUrl;
        if (localIrccUrl == null || StringUtils.isEmpty(localIrccUrl)) {
            logger.debug("IRCC URL was not specified in configuration");
        } else {
            try {
                final IrccClient irccClient = IrccClientFactory.get(localIrccUrl);
                final ScalarWebContext context = getContext();
                String localCmd = cmd;

                final String cmdMap = context.getConfig().getCommandsMapFile();

                final TransformationService localTransformService = context.getTransformService();
                if (localTransformService != null && cmdMap != null) {
                    String code;
                    try {
                        code = localTransformService.transform(cmdMap, cmd);

                        if (StringUtils.isNotBlank(code)) {
                            logger.debug("Transformed {} with map file '{}' to {}", cmd, cmdMap, code);

                            try {
                                localCmd = URLDecoder.decode(code, "UTF-8");
                            } catch (final UnsupportedEncodingException e) {
                                localCmd = code;
                            }
                        }
                    } catch (final TransformationException e) {
                        logger.debug("Failed to transform {} using map file '{}', exception={}", cmd, cmdMap,
                                e.getMessage());
                        return;
                    }
                }

                if (localCmd == null || StringUtils.isEmpty(localCmd)) {
                    logger.debug("IRCC command was empty or null - ignoring");
                    return;
                }

                // Always use an http transport to execute soap
                HttpResponse httpResponse;
                try (final SonyHttpTransport httpTransport = SonyTransportFactory
                        .createHttpTransport(irccClient.getBaseUrl().toExternalForm())) {
                    // copy all the options from the parent one (authentication options)
                    getService().getTransport().getOptions().stream().forEach(o -> httpTransport.setOption(o));
                    httpResponse = irccClient.executeSoap(httpTransport, localCmd);
                } catch (final URISyntaxException e) {
                    logger.debug("URI syntax exception: {}", e.getMessage());
                    return;
                }

                switch (httpResponse.getHttpCode()) {
                    case HttpStatus.OK_200:
                        // everything is great!
                        break;

                    case HttpStatus.SERVICE_UNAVAILABLE_503:
                        logger.debug("IRCC service is unavailable (power off?)");
                        break;

                    case HttpStatus.FORBIDDEN_403:
                        logger.debug("IRCC methods have been forbidden on service {} ({}): {}",
                                service.getServiceName(), irccClient.getBaseUrl(), httpResponse);
                        break;

                    case HttpStatus.INTERNAL_SERVER_ERROR_500:
                        final SOAPError soapError = httpResponse.getSOAPError();
                        if (soapError == null) {
                            final IOException e = httpResponse.createException();
                            logger.debug("Communication error for IRCC method on service {} ({}): {}",
                                    service.getServiceName(), irccClient.getBaseUrl(), e.getMessage(), e);
                            callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    e.getMessage());
                            break;
                        } else {
                            logger.debug("SOAP Error: ({}) {}", soapError.getSoapCode(),
                                    soapError.getSoapDescription());
                        }
                        break;

                    default:
                        final IOException e = httpResponse.createException();
                        logger.debug("Communication error for IRCC method on service {} ({}): {}",
                                service.getServiceName(), irccClient.getBaseUrl(), e.getMessage(), e);
                        callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                e.getMessage());
                        break;
                }

            } catch (IOException | URISyntaxException e) {
                logger.debug("Cannot create IRCC client: {}", e.getMessage(), e);
                return;
            }
        }
    }

    @Override
    protected void eventReceived(final ScalarWebEvent event) throws IOException {
        Objects.requireNonNull(event, "event cannot be null");

        final @Nullable String mtd = event.getMethod();
        if (mtd == null || StringUtils.isEmpty(mtd)) {
            logger.debug("Unhandled event received (no method): {}", event);
        } else {
            switch (mtd) {
                case ScalarWebEvent.NOTIFYPOWERSTATUS:
                    final String powerVersion = getVersion(ScalarWebMethod.GETPOWERSTATUS);
                    if (VersionUtilities.equals(powerVersion, ScalarWebMethod.V1_0)) {
                        notifyPowerStatus(event.as(PowerStatusResult_1_0.class));
                    } else {
                        notifyPowerStatus(event.as(PowerStatusResult_1_1.class));
                    }

                    break;

                case ScalarWebEvent.NOTIFYSWUPDATEINFO:
                    notifySoftwareUpdate(event.as(SoftwareUpdate.class));
                    break;

                case ScalarWebEvent.NOTIFYSETTINGSUPDATE:
                    notifySettingUpdate(event.as(NotifySettingUpdate.class));
                    break;

                case ScalarWebEvent.NOTIFYSTORAGESTATUS:
                    final String storVersion = getVersion(ScalarWebMethod.GETSTORAGELIST);
                    if (VersionUtilities.equals(storVersion, ScalarWebMethod.V1_1)) {
                        notifyStorageStatus(event.as(StorageListItem_1_1.class));
                    } else {
                        notifyStorageStatus(event.as(StorageListItem_1_2.class));
                    }

                    break;

                default:
                    logger.debug("Unhandled event received: {}", event);
                    break;
            }
        }
    }

    /**
     * Called when a power state notification (v1.0) is received
     * 
     * @param status a non-null power status notification
     */
    private void notifyPowerStatus(final PowerStatusResult_1_0 status) {
        Objects.requireNonNull(status, "status cannot be null");
        stateChanged(POWERSTATUS, status.isActive() ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Called when a power state notification (v1.1) is received
     * 
     * @param status a non-null power status notification
     */
    private void notifyPowerStatus(final PowerStatusResult_1_1 status) {
        Objects.requireNonNull(status, "status cannot be null");
        stateChanged(POWERSTATUS, status.isActive() ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Called when a storage status notification (v1.1) is received
     * 
     * @param item a non-null item
     */
    private void notifyStorageStatus(final StorageListItem_1_1 item) {
        Objects.requireNonNull(item, "item cannot be null");

        final String uri = item.getUri();
        if (uri == null || StringUtils.isEmpty(uri)) {
            logger.debug("Storage URI is empty - ignoring notification: {}", item);
            return;
        }

        final String id = getStorageChannelId(uri);

        stateChanged(ST_DEVICENAME, id, SonyUtil.newStringType(item.getDeviceName()));
        stateChanged(ST_ERROR, id, SonyUtil.newStringType(item.getError()));
        stateChanged(ST_FORMAT, id, SonyUtil.newStringType(item.getFormat()));
        stateChanged(ST_FORMATTABLE, id, SonyUtil.newStringType(item.getFormattable()));
        stateChanged(ST_FORMATTING, id, SonyUtil.newStringType(item.getFormatting()));
        stateChanged(ST_FREECAPACITYMB, id,
                SonyUtil.newQuantityType(item.getFreeCapacityMB(), MetricPrefix.MEGA(SmartHomeUnits.BYTE)));
        stateChanged(ST_ISAVAILABLE, id, SonyUtil.newStringType(item.getIsAvailable()));
        stateChanged(ST_LUN, id, SonyUtil.newDecimalType(item.getLun()));
        stateChanged(ST_MOUNTED, id, SonyUtil.newStringType(item.getMounted()));
        stateChanged(ST_PERMISSION, id, SonyUtil.newStringType(item.getPermission()));
        stateChanged(ST_POSITION, id, SonyUtil.newStringType(item.getPosition()));
        stateChanged(ST_SYSTEMAREACAPACITYMB,
                SonyUtil.newQuantityType(item.getSystemAreaCapacityMB(), MetricPrefix.MEGA(SmartHomeUnits.BYTE)));
        stateChanged(ST_TYPE, id, SonyUtil.newStringType(item.getType()));
        stateChanged(ST_URI, id, SonyUtil.newStringType(uri));
        stateChanged(ST_VOLUMELABEL, id, SonyUtil.newStringType(item.getVolumeLabel()));
        stateChanged(ST_WHOLECAPACITYMB, id,
                SonyUtil.newQuantityType(item.getWholeCapacityMB(), MetricPrefix.MEGA(SmartHomeUnits.BYTE)));
    }

    /**
     * Called when a storage status notification (v1.2) is received
     * 
     * @param item a non-null item
     */
    private void notifyStorageStatus(final StorageListItem_1_2 item) {
        Objects.requireNonNull(item, "item cannot be null");

        final String uri = item.getUri();
        if (uri == null || StringUtils.isEmpty(uri)) {
            logger.debug("Storage URI is empty - ignoring notification: {}", item);
            return;
        }

        final String id = getStorageChannelId(uri);

        stateChanged(ST_DEVICENAME, id, SonyUtil.newStringType(item.getDeviceName()));
        stateChanged(ST_ERROR, id, SonyUtil.newStringType(item.getError()));
        stateChanged(ST_FILESYSTEM, id, SonyUtil.newStringType(item.getFileSystem()));
        stateChanged(ST_FINALIZESTATUS, id, SonyUtil.newStringType(item.getFinalizeStatus()));
        stateChanged(ST_FORMAT, id, SonyUtil.newStringType(item.getFormat()));
        stateChanged(ST_FORMATSTATUS, id, SonyUtil.newStringType(item.getFormatStatus()));
        stateChanged(ST_FORMATTABLE, id, SonyUtil.newStringType(item.getFormattable()));
        stateChanged(ST_FREECAPACITYMB, id,
                SonyUtil.newQuantityType(item.getFreeCapacityMB(), MetricPrefix.MEGA(SmartHomeUnits.BYTE)));
        stateChanged(ST_HASNONSTANDARDDATA, id, SonyUtil.newStringType(item.getHasNonStandardData()));
        stateChanged(ST_HASUNSUPPORTEDCONTENTS, id, SonyUtil.newStringType(item.getHasUnsupportedContents()));
        stateChanged(ST_ISAVAILABLE, id, SonyUtil.newStringType(item.getIsAvailable()));
        stateChanged(ST_ISLOCKED, SonyUtil.newStringType(item.getIsLocked()));
        stateChanged(ST_ISMANAGEMENTINFOFULL, id, SonyUtil.newStringType(item.getIsManagementInfoFull()));
        stateChanged(ST_ISPROTECTED, id, SonyUtil.newStringType(item.getIsProtected()));
        stateChanged(ST_ISREGISTERED, id, SonyUtil.newStringType(item.getIsRegistered()));
        stateChanged(ST_ISSELFRECORDED, id, SonyUtil.newStringType(item.getIsSelfRecorded()));
        stateChanged(ST_ISSQVSUPPORTED, id, SonyUtil.newStringType(item.getIsSqvSupported()));
        stateChanged(ST_LUN, id, SonyUtil.newDecimalType(item.getLun()));
        stateChanged(ST_MOUNTED, id, SonyUtil.newStringType(item.getMounted()));
        stateChanged(ST_PERMISSION, id, SonyUtil.newStringType(item.getPermission()));
        stateChanged(ST_POSITION, id, SonyUtil.newStringType(item.getPosition()));
        stateChanged(ST_PROTOCOL, id, SonyUtil.newStringType(item.getProtocol()));
        stateChanged(ST_REGISTRATIONDATE, id, SonyUtil.newStringType(item.getRegistrationDate()));
        stateChanged(ST_SYSTEMAREACAPACITYMB, id,
                SonyUtil.newQuantityType(item.getSystemAreaCapacityMB(), MetricPrefix.MEGA(SmartHomeUnits.BYTE)));
        stateChanged(ST_TIMESECTOFINALIZE, id,
                SonyUtil.newQuantityType(item.getTimeSecToFinalize(), SmartHomeUnits.SECOND));
        stateChanged(ST_TIMESECTOGETCONTENTS, id,
                SonyUtil.newQuantityType(item.getTimeSecToGetContents(), SmartHomeUnits.SECOND));
        stateChanged(ST_TYPE, id, SonyUtil.newStringType(item.getType()));
        stateChanged(ST_URI, id, SonyUtil.newStringType(uri));
        stateChanged(ST_USBDEVICETYPE, id, SonyUtil.newStringType(item.getUsbDeviceType()));
        stateChanged(ST_VOLUMELABEL, id, SonyUtil.newStringType(item.getVolumeLabel()));
        stateChanged(ST_WHOLECAPACITYMB, id,
                SonyUtil.newQuantityType(item.getWholeCapacityMB(), MetricPrefix.MEGA(SmartHomeUnits.BYTE)));
    }

    /**
     * Called when a software update notification has been done
     * 
     * @param update a non-null software update
     */
    private void notifySoftwareUpdate(final SoftwareUpdate update) {
        Objects.requireNonNull(update, "update cannot be null");
        // TODO - do firmware update stuff
    }

    @Override
    public void close() {
        super.close();
    }
}
