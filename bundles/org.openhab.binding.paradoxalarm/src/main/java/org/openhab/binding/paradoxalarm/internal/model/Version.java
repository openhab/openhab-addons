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
package org.openhab.binding.paradoxalarm.internal.model;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Version} This class holds version information
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class Version {
    private final Logger logger = LoggerFactory.getLogger(Version.class);

    private Byte version;
    private Byte revision;
    private Byte build;
    private Date buildTime;

    public Version(Byte version, Byte revision) {
        this(version, revision, null);
    }

    public Version(Byte version, Byte revision, Byte build) {
        this(version, revision, build, null);
    }

    public Version(Byte version, Byte revision, Byte build, Date buildTime) {
        this.version = version;
        this.revision = revision;
        this.build = build;
        this.buildTime = buildTime;
        logger.debug("version={}", this);
    }

    public Byte getVersion() {
        return version;
    }

    public void setVersion(Byte version) {
        this.version = version;
    }

    public Byte getRevision() {
        return revision;
    }

    public void setRevision(Byte revision) {
        this.revision = revision;
    }

    public Byte getBuild() {
        return build;
    }

    public void setBuild(Byte build) {
        this.build = build;
    }

    public Date getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(Date buildTime) {
        this.buildTime = buildTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Version: ");
        sb.append(version);
        sb.append(".");
        sb.append(revision);
        if (build != null) {
            sb.append(".");
            sb.append(build);
        }
        if (buildTime != null) {
            sb.append("/");
            sb.append(buildTime);
        }
        return sb.toString();
    }
}
