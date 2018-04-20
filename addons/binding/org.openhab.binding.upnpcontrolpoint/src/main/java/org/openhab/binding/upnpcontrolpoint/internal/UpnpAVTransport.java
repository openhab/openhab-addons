/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrolpoint.internal;

/**
 *
 * @author Mark Herwege - Initial contribution
 */
public class UpnpAVTransport {
    private String transportState;
    private String transportStatus;
    private String currentMediaCategory;
    private String playBackStorageMedium;
    private String recordStorageMedium;
    private String possiblePlaybackStorageMedia;
    private String possibleRecordStorageMedia;
    private String currentPlayMode = "NORMAL";
    private String transportPlaySpeed = "1";
    private String recordMediumWriteStatus;
    private String currentRecordQualityModes;
    private String possibleRecordQualityModes;
    private Integer numberOfTrack;
    private Integer currentTrack;
    private String currentTrackDuration;
    private String currentTrackMetaData;
    private String currentTrackURI;
    private String avTransportURI;
    private String avTransportURIMetaData;
    private String nextAVTransportURI;
    private String nextAVTransportURIMetaData;
    private String relativeTimePosition;
    private String absoluteTimePosition;
    private Integer relativeCounterPosition;
    private Integer absoluteCounterPosition;
    private String currentTransportActions;
    private String lastChange;
    private String drmState;
    private String syncOffset;
    private String SeekMode;
    private String SeekTarget;
    private Integer InstanceID;
    private String deviceUDN;
    private String serviceType;
    private String serviceID;
    private String stateVariableValuePairs;
    private String stateVariableList;
    private String playlistData;
    private Integer playlistDataLength;
    private Integer playlistOffset;
    private Integer playlistTotalLength;
    private String playlistMIMEType;
    private String playlistExtendedType;
    private String playlistStep;
    private String playlistType;
    private String playlistInfo;
    private String playlistStartObjID;
    private String playlistStartGroupID;
    private String syncOffsetAdj;
    private String presentationTime;
    private String clockId;
}
