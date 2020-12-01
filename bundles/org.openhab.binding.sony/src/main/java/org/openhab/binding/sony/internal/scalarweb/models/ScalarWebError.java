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
package org.openhab.binding.sony.internal.scalarweb.models;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class provides various constants for scalar web errors
 * 
 * https://developer.sony.com/develop/audio-control-api/api-references/error-codes
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebError {
    // Common result error codes
    public static final int UNKNOWN = -1;
    public static final int HTTPERROR = -2;
    public static final int NONE = 0;

    public static final int ANY = 1;
    public static final int TIMEOUT = 2;
    public static final int ILLEGALARGUMENT = 3;
    public static final int ILLEGALREQUEST = 5;
    public static final int ILLEGALSTATE = 7; // such as pip status when not in pip
    public static final int NOTIMPLEMENTED = 12;
    public static final int UNSUPPORTEDVERSION = 14;
    public static final int UNSUPPORTEDOPERATION = 15;
    public static final int FORBIDDEN = 403;
    public static final int FAILEDTOLAUNCH = 41401;

    public static final int REQUESTRETRY = 40000;
    public static final int CLIENTOVERMAXIMUM = 40001;
    public static final int ENCRYPTIONFAILED = 40002;
    public static final int REQUESTDUPLICATED = 40003;
    public static final int MULTIPLESETTINGSFAILED = 40004;
    public static final int DISPLAYISOFF = 40005;

    // System service specific
    public static final int PASSWORDEXPIRED = 40200;
    public static final int ACPOWERREQUIRED = 40201;

    // Audio service specific
    public static final int TARGETNOTSUPPORTED = 40800;
    public static final int VOLUMEOUTOFRANGE = 40801;

    // AV Content service specific
    public static final int CONTENTISPROTECTED = 41000;
    public static final int CONTENTDOESNTEXIST = 41001;
    public static final int STORAGEHASNOTCONTENT = 41002;
    public static final int SOMECONTENTCOULDNTBEDELETED = 41003;
    public static final int CHANNELFIXEDBYUSBRECORDING = 41011;
    public static final int CHANNELFIXEDBYSCARTRECORDING = 41012;
    public static final int CHAPTERDOESNTEXIST = 41013;
    public static final int CHANNELCANTBEUNIQUELYDETERMINED = 41014;
    public static final int EMPTYCHANNELLIST = 41015;
    public static final int STORAGEDOESNTEXIST = 41020;
    public static final int STORAGEISFULL = 41021;
    public static final int CONTENTATTRIBUTESETTINGFAILED = 41022;
    public static final int UNKNOWNGROUPID = 41023;
    public static final int CONTENTISNOTSUPPORTED = 41024;
}
