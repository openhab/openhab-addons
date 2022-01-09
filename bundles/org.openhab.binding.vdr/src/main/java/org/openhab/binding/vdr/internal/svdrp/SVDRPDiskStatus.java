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
package org.openhab.binding.vdr.internal.svdrp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SVDRPDiskStatus} contains SVDRP Response Data for DiskStatus queries
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPDiskStatus {
    private static final Pattern PATTERN_DISK_STATUS = Pattern.compile("([\\d]*)MB ([\\d]*)MB ([\\d]*)%");

    private long megaBytesFree = -1;
    private long megaBytesTotal = -1;
    private int percentUsed = -1;

    private SVDRPDiskStatus() {
    }

    /**
     * parse object from SVDRP Client Response
     *
     * @param message SVDRP Client Response
     * @return Disk Status Object
     * @throws SVDRPParseResponseException thrown if response data is not parseable
     */
    public static SVDRPDiskStatus parse(String message) throws SVDRPParseResponseException {
        SVDRPDiskStatus status = new SVDRPDiskStatus();
        Matcher matcher = PATTERN_DISK_STATUS.matcher(message);
        if (matcher.find() && matcher.groupCount() == 3) {
            status.setMegaBytesTotal(Long.parseLong(matcher.group(1)));
            status.setMegaBytesFree(Long.parseLong(matcher.group(2)));
            status.setPercentUsed(Integer.parseInt(matcher.group(3)));
        }
        return status;
    }

    /**
     * Get Megabytes Free on Disk
     *
     * @return megabytes free
     */
    public long getMegaBytesFree() {
        return megaBytesFree;
    }

    /**
     * Set Megabytes Free on Disk
     *
     * @param megaBytesFree megabytes free
     */
    public void setMegaBytesFree(long megaBytesFree) {
        this.megaBytesFree = megaBytesFree;
    }

    /**
     * Get Megabytes Total on Disk
     *
     * @return megabytes total
     */
    public long getMegaBytesTotal() {
        return megaBytesTotal;
    }

    /**
     * Set Megabytes Total on Disk
     *
     * @param megaBytesTotal megabytes total
     */
    public void setMegaBytesTotal(long megaBytesTotal) {
        this.megaBytesTotal = megaBytesTotal;
    }

    /**
     * Get Percentage Used on Disk
     *
     * @return percentage used
     */
    public int getPercentUsed() {
        return percentUsed;
    }

    /**
     * Set Percentage Used on Disk
     *
     * @param percentUsed percentage used
     */
    public void setPercentUsed(int percentUsed) {
        this.percentUsed = percentUsed;
    }

    /**
     * String Representation of SVDRPDiskStatus Object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (megaBytesTotal >= 0) {
            sb.append("Total: " + megaBytesTotal + "MB" + System.lineSeparator());
        }
        if (megaBytesFree >= 0) {
            sb.append("Free: " + megaBytesFree + "MB" + System.lineSeparator());
        }
        if (percentUsed >= 0) {
            sb.append("Free: " + percentUsed + "%" + System.lineSeparator());
        }
        return sb.toString();
    }
}
