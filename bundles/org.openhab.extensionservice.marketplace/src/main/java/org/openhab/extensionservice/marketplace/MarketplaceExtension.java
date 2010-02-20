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
package org.openhab.extensionservice.marketplace;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.extension.Extension;

/**
 * This is an {@link Extension}, which additionally holds the package format and a download url for its content.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@NonNullByDefault
public class MarketplaceExtension extends Extension {

    // constants used to construct extension IDs
    public static final String EXT_PREFIX = "market:";

    // extension types from marketplace
    public static final String EXT_TYPE_RULE_TEMPLATE = "ruletemplate";
    public static final String EXT_TYPE_BINDING = "binding";
    public static final String EXT_TYPE_VOICE = "voice";

    // extension package formats from marketplace
    public static final String EXT_FORMAT_BUNDLE = "bundle";
    public static final String EXT_FORMAT_JSON = "json";

    // we mark them as transient, so that they are not serialized through GSON
    private transient String downloadUrl;
    private transient String packageFormat;

    public MarketplaceExtension(String id, String type, String label, String version, String link, boolean installed,
            String description, @Nullable String backgroundColor, String imageLink, String downloadUrl,
            String packageFormat) {
        super(id, type, label, version, link, installed, description, backgroundColor, imageLink);
        this.downloadUrl = downloadUrl;
        this.packageFormat = packageFormat;
    }

    /**
     * returns the download url for the content of this extension
     *
     * @return a download url string
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * returns the package format of this extension
     *
     * @return package format of the extension
     */
    public String getPackageFormat() {
        return packageFormat;
    }
}
