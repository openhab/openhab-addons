/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.updateopenhab.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Executors;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.updateopenhab.internal.TargetVersion;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseUpdater} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG - Initial contribution
 */
@NonNullByDefault
public class BaseUpdater implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(BaseUpdater.class);

    private static final String RELEASE_ARTIFACT_URL = "https://openhab.jfrog.io/artifactory/libs-release-local/org/openhab/distro/openhab/maven-metadata.xml";
    private static final String MILESTONE_ARTIFACT_URL = "https://openhab.jfrog.io/artifactory/libs-milestone-local/org/openhab/distro/openhab/maven-metadata.xml";

    private static final String UNKNOWN_VERSION = "VERSION NOT DETERMINED";

    private static final String SNAPSHOT = "SNAPSHOT";

    protected static final String FILE_ID = "auto-update-created";

    protected TargetVersion targetVersion;
    protected String password;

    /**
     * Private class to read and process the standard output of the shell command
     *
     * @author Andrew Fiddian-Green - Initial contribution
     */
    protected static class InputStreamEater implements Runnable {

        private InputStream inputStream;
        private Logger logger;

        public InputStreamEater(InputStream inputStream, Logger logger) {
            this.inputStream = inputStream;
            this.logger = logger;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach((line) -> {
                logger.trace(line);
            });
        }
    }

    public BaseUpdater(TargetVersion targetVersion, String password) {
        this.targetVersion = targetVersion;
        this.password = password;
    }

    protected boolean writeFile(String filename, String contents) {
        try (FileWriter file = new FileWriter(filename); BufferedWriter writer = new BufferedWriter(file)) {
            writer.write(contents);
            return true;
        } catch (IOException e1) {
        }
        return false;
    }

    protected void executeProcess(ProcessBuilder builder) {
        String latestVersion = getLatestVersion();
        try {
            logger.info("Starting OpenHAB update script: version {}", latestVersion);
            Process process = builder.start();
            InputStreamEater inputStreamEater = new InputStreamEater(process.getInputStream(), logger);
            Executors.newSingleThreadExecutor().submit(inputStreamEater);
            int exitcode = process.waitFor();
            if (exitcode == 0) {
                logger.info("Finished OpenHAB update script: version {}", latestVersion);
            } else {
                logger.warn("Failed OpenHAB update script with exit code '{}'", exitcode);
            }
        } catch (IOException e) {
            logger.warn("Failed OpenHAB update script with error '{}'", e.getMessage());
        } catch (InterruptedException e) {
            logger.warn("Failed OpenHAB update script - interruped");
        }
    }

    protected boolean prerequisitesOk() {
        String latestVersion = getLatestVersion();
        if (UNKNOWN_VERSION.equals(latestVersion)) {
            logger.warn("Cannot run OpenHAB update script: {}", latestVersion);
            return false;
        }
        return true;
    }

    protected String getArtifactoryLatestVersion(String url) {
        String latestVersion = UNKNOWN_VERSION;
        XMLStreamReader reader;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(new URL(url).openStream());
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    if ("latest".equals(reader.getLocalName())) {
                        latestVersion = reader.getElementText();
                        break;
                    }
                }
            }
            reader.close();
        } catch (IOException | XMLStreamException e) {
        }
        return latestVersion;
    }

    protected String getLatestVersion() {
        switch (targetVersion) {
            case STABLE:
                return getArtifactoryLatestVersion(RELEASE_ARTIFACT_URL);
            case MILESTONE:
                return getArtifactoryLatestVersion(MILESTONE_ARTIFACT_URL);
            case SNAPSHOT:
                String oldVersion = OpenHAB.getVersion();
                if (!oldVersion.isEmpty()) {
                    return oldVersion + "-" + SNAPSHOT;
                }
        }
        return UNKNOWN_VERSION;
    }

    @Override
    public void run() {
    }

    private static String getRunningVersion() {
        String oldVersion = OpenHAB.getVersion();
        return oldVersion.isEmpty() ? UNKNOWN_VERSION : oldVersion;
    }

    public static State getRunningVersionState() {
        return StringType.valueOf(getRunningVersion());
    }

    public State getLatestVersionState() {
        String newVersion = getLatestVersion();
        return UNKNOWN_VERSION.equals(newVersion) ? UnDefType.UNDEF : StringType.valueOf(newVersion);
    }

    private Integer safeConvertInteger(String[] strings, int index) {
        try {
            return index < strings.length ? Integer.valueOf(strings[index]) : 0;
        } catch (NumberFormatException e) {
        }
        return 0;
    }

    public State getUpdateAvailableState() {
        String oldVersion = OpenHAB.getVersion();
        String newVersion = getLatestVersion();
        String[] oldParts = oldVersion.replace("-", ".").split("\\.");
        String[] newParts = newVersion.replace("-", ".").split("\\.");

        int compareOverall = 0;
        for (int i = 0; i < 3; i++) {
            int compareResult = safeConvertInteger(oldParts, i).compareTo(safeConvertInteger(newParts, i));
            if (compareResult != 0) {
                compareOverall = compareResult;
                break;
            }
        }
        if (compareOverall == 0) {
            switch (targetVersion) {
                case SNAPSHOT:
                    return UnDefType.UNDEF;
                case MILESTONE:
                    if (newParts.length > 3) {
                        compareOverall = oldParts.length > 3 ? oldParts[3].compareTo(newParts[3]) : 1;
                    }
                default:
            }
        }
        return OnOffType.from(compareOverall < 0);
    }
}
