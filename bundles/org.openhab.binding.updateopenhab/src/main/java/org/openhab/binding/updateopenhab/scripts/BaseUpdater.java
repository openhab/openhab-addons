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
import java.io.File;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseUpdater} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG
 */
@NonNullByDefault
public class BaseUpdater implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(BaseUpdater.class);

    private static final String RELEASE_ARTIFACT_URL = "https://openhab.jfrog.io/artifactory/libs-release-local/org/openhab/distro/openhab/maven-metadata.xml";
    private static final String MILESTONE_ARTIFACT_URL = "https://openhab.jfrog.io/artifactory/libs-milestone-local/org/openhab/distro/openhab/maven-metadata.xml";

    private static final String UPDATE_SCRIPT_FILENAME = "openhab-auto-update";
    private static final String UNKNOWN_VERSION = "VERSION NOT DETERMINED";

    protected TargetVersion targetVersion;
    protected String runDirectory;
    protected String fileExtension;

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

    public BaseUpdater(TargetVersion targetVersion) {
        this.targetVersion = targetVersion;
        runDirectory = File.separator;
        fileExtension = "";
    }

    protected String getScriptFileName() {
        return runDirectory + UPDATE_SCRIPT_FILENAME + fileExtension;
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

    protected String getUserHomeFolder() {
        String folder = System.getProperty("user.home");
        if (folder == null) {
            folder = "";
        }
        if (folder.endsWith(File.separator)) {
            return folder;
        } else {
            return folder + File.separator;
        }
    }

    protected String getOpenHabRootFolder() {
        String folder = OpenHAB.getConfigFolder().replace("conf", "");
        if (folder.isEmpty()) {
            return getUserHomeFolder();
        }
        if (folder.endsWith(File.separator)) {
            return folder;
        } else {
            return folder + File.separator;
        }
    }

    protected String getArtifactoryLatestVersion(String url) {
        String version = "";
        XMLStreamReader reader;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(new URL(url).openStream());
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    if ("latest".equals(reader.getLocalName())) {
                        version = reader.getElementText();
                        break;
                    }
                }
            }
            reader.close();
        } catch (IOException | XMLStreamException e) {
        }
        return version;
    }

    public String getLatestVersion() {
        switch (targetVersion) {
            case STABLE:
                return getArtifactoryLatestVersion(RELEASE_ARTIFACT_URL);
            case MILESTONE:
                return getArtifactoryLatestVersion(MILESTONE_ARTIFACT_URL);
            case SNAPSHOT:
                try {
                    return OpenHAB.getVersion() + "-SNAPSHOT";
                } catch (Exception e) {
                }
        }
        return UNKNOWN_VERSION;
    }

    @Override
    public void run() {
    }
}
