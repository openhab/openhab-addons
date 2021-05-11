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
package org.openhab.binding.updateopenhab.updaters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseUpdater} is a base for extending classes that will update OpenHAB.
 *
 * For each different operating system (resp. Linux package manager variety) there will be a different class that
 * extends this class and executes the respective update process for the target system.
 *
 * @author AndrewFG - Initial contribution
 */
@NonNullByDefault
public abstract class BaseUpdater implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(BaseUpdater.class);

    private static final String TITLE = "OpenHAB Updater";

    private static final String RELEASE_ARTIFACT_URL = "https://openhab.jfrog.io/artifactory/libs-release-local/org/openhab/distro/openhab/maven-metadata.xml";
    private static final String MILESTONE_ARTIFACT_URL = "https://openhab.jfrog.io/artifactory/libs-milestone-local/org/openhab/distro/openhab/maven-metadata.xml";

    private static final String SNAPSHOT = "SNAPSHOT";

    private static final Boolean RUN_LOCK = Boolean.valueOf(true);

    public static final int MIN_SLEEP_SECS = 5; // seconds
    public static final int DEF_SLEEP_SECS = 20;
    public static final int MAX_SLEEP_SECS = 60;

    public static final TargetVersionType DEF_TARGET_VER = TargetVersionType.STABLE;

    private TargetVersionType targetVersionType = DEF_TARGET_VER;

    protected static final String FILE_ID = "update-openhab";

    /**
     * Place holders are keys in the script templates to be replaced by values at runtime
     */
    protected enum PlaceHolder {
        // BASE place holders: values will be provided by this BaseUpdater class
        TITLE("[TITLE]"),
        PASSWORD("[PASSWORD]"),
        SLEEP_TIME("[SLEEP_TIME]"),
        TARGET_TYPE("[TARGET_TYPE]"),
        TARGET_VERSION("[TARGET_VERSION]"),
        USERDATA_FOLDER("[USERDATA_FOLDER]"),
        LOGBACK_FILENAME("[LOGBACK_FILENAME]"),
        // EXTENDED place holders: values must be provided by classes that extend BaseUpdater
        EXECUTE_COMMAND("[EXECUTE_COMMAND]"),
        EXECUTE_FOLDER("[EXECUTE_FOLDER]"),
        EXECUTE_FILENAME("[EXECUTE_FILENAME]"),
        RUNTIME_FOLDER("[RUNTIME_FOLDER]");

        protected final String key;

        private PlaceHolder(String key) {
            this.key = key;
        }
    }

    protected final Map<PlaceHolder, String> placeHolders = new HashMap<>();

    public static final String VERSION_NOT_DEFINED = "VERSION_NOT_DEFINED";

    /**
     * Constructor.
     */
    public BaseUpdater() {
        placeHolders.put(PlaceHolder.TITLE, TITLE);
        placeHolders.put(PlaceHolder.PASSWORD, "");
        placeHolders.put(PlaceHolder.SLEEP_TIME, Integer.toString(DEF_SLEEP_SECS));
        placeHolders.put(PlaceHolder.TARGET_TYPE, targetVersionType.label);
        placeHolders.put(PlaceHolder.USERDATA_FOLDER, OpenHAB.getUserDataFolder());
        placeHolders.put(PlaceHolder.LOGBACK_FILENAME, FILE_ID + ".log");
        initializeExtendedPlaceholders();
        if (logger.isDebugEnabled()) {
            logAndDeleteLogbackFile(true, false);
        } else {
            logAndDeleteLogbackFile(true, true);
            deletePriorScriptFile();
        }
    }

    /**
     * This method is used to initialise the extended place holders.
     * <p>
     * Note: it is not allowed to instantiate BaseUpdater directly, and it would throw an exception. Instead one must
     * instantiate a class that extends BaseUpdater and overrides this method.
     * <p>
     * NOTE: classes that extend from BaseUpdater MUST set the following place holder values
     * <li>EXECUTE_FOLDER = the folder where the update script shall be run from
     * <li>EXECUTE_FILENAME = the file name of the update script
     * <li>EXECUTE_COMMAND = the shell command use to execute the update script
     * <li>RUNTIME_FOLDER = the OpenHAB runtime folder
     * <p>
     */
    protected abstract void initializeExtendedPlaceholders();

    /**
     * Property setter.
     *
     * @param targetVersion
     * @return this instance
     */
    public BaseUpdater setTargetVersion(TargetVersionType targetVersion) {
        this.targetVersionType = targetVersion;
        placeHolders.put(PlaceHolder.TARGET_TYPE, targetVersion.label);
        return this;
    }

    /**
     * Property setter.
     *
     * @param password
     * @return this instance
     */
    public BaseUpdater setPassword(String password) {
        placeHolders.put(PlaceHolder.PASSWORD, password);
        return this;
    }

    /**
     * Property setter.
     *
     * @param sleepTime
     * @return this instance
     */
    public BaseUpdater setSleepTime(int sleepTime) {
        if ((MIN_SLEEP_SECS <= sleepTime) && (sleepTime <= MAX_SLEEP_SECS)) {
            placeHolders.put(PlaceHolder.SLEEP_TIME, Integer.toString(sleepTime));
        }
        return this;
    }

    /**
     * Start an external process to execute the script file.
     *
     * @return success
     */
    private boolean runUpdateScript() {
        String executeCommand = placeHolders.get(PlaceHolder.EXECUTE_COMMAND);
        String directory = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);
        if (executeCommand != null && directory != null) {
            // split command from the and arguments (if any)
            List<String> commands;
            int splitIndex = executeCommand.indexOf(" ");
            String cmnd;
            String args;
            if (splitIndex >= 0) {
                commands = Arrays.asList(executeCommand.substring(0, splitIndex),
                        executeCommand.substring(splitIndex + 1));
                cmnd = commands.get(0);
                args = commands.get(1);
            } else {
                commands = Arrays.asList(executeCommand);
                cmnd = commands.get(0);
                args = "null";
            }
            logger.debug("Process builder: directory={}, command={}, arguments={}", directory, cmnd, args);
            ProcessBuilder builder = new ProcessBuilder(commands).directory(new File(directory));
            try {
                builder.start();
                return true;
            } catch (IOException e) {
                logger.debug("Failed to start script file: {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * Check if all of the place holders have values.
     *
     * @return true if all place holders are non empty strings
     */
    private boolean checkPlaceHoldersExist() {
        boolean fail = false;
        for (PlaceHolder placeHolder : PlaceHolder.values()) {
            String placeHolderValue = placeHolders.get(placeHolder);
            logger.debug("{}:{}", placeHolder.name(), placeHolderValue);
            fail = fail || (placeHolderValue == null);
        }
        return !fail;
    }

    /**
     * Replace any place holders in the execute command with their actual values.
     *
     * @return success
     */
    private boolean fixupExecuteCommand() {
        String executeCommand = placeHolders.get(PlaceHolder.EXECUTE_COMMAND);
        if (executeCommand != null) {
            for (Map.Entry<PlaceHolder, String> placeHolder : placeHolders.entrySet()) {
                executeCommand = executeCommand.replace(placeHolder.getKey().key, placeHolder.getValue());
            }
            placeHolders.put(PlaceHolder.EXECUTE_COMMAND, executeCommand);
            return true;
        }
        return false;
    }

    /**
     * Read a text resource from within the jar, convert its place holders to actual values, and write it to
     * the script file to be executed.
     *
     * @return success
     */
    private boolean createUpdateScriptFile() {
        String execFolder = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);
        if (execFolder == null) {
            // no need to log; null value should never occur
            return false;
        }
        String execFilename = placeHolders.get(PlaceHolder.EXECUTE_FILENAME);
        if (execFilename == null) {
            // no need to log; null value should never occur
            return false;
        }

        // create an input stream to access the resource
        String resourceId = "/scripts/" + getClass().getSimpleName() + ".txt";
        InputStream resourceStream = getClass().getResourceAsStream(resourceId);
        if (resourceStream == null) {
            logger.debug("Could not find resource id: {}", resourceId);
            return false;
        }

        // read script lines from the resource
        @SuppressWarnings("null")
        List<String> lines = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.toList());

        // replace all place holders in the script with their actual values
        for (int line = 0; line < lines.size(); line++) {
            for (Map.Entry<PlaceHolder, String> placeHolder : placeHolders.entrySet()) {
                lines.set(line, lines.get(line).replace(placeHolder.getKey().key, placeHolder.getValue()));
            }
        }

        // write the script to the file
        Path execFilePath = Paths.get(execFolder + File.separator + execFilename);
        try {
            Files.write(execFilePath, lines);
            return true;
        } catch (IOException e) {
            logger.debug("Error writing script file: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Create the log-back file with the given message in it.
     *
     * @param logMessage is the message to be written in the file
     * @return success
     */
    private boolean createLogbackFile(String logMessage) {
        String execFolder = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);
        if (execFolder == null) {
            // no need to log; null value should never occur
            return false;
        }
        String logbackFilename = placeHolders.get(PlaceHolder.LOGBACK_FILENAME);
        if (logbackFilename == null) {
            // no need to log; null value should never occur
            return false;
        }
        Path logbackPath = Paths.get(execFolder + File.separator + logbackFilename);
        try {
            Files.write(logbackPath, Arrays.asList(logMessage));
            return true;
        } catch (IOException e) {
            logger.debug("Error writing log-back file: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Read the log-back file (if present) log its contents and delete the file.
     *
     * @param outputLog if true then send the log file contents to logger.info()
     * @param deleteFile if true delete the log file
     * @return success
     */
    private boolean logAndDeleteLogbackFile(boolean outputLog, boolean deleteFile) {
        String execFolder = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);
        if (execFolder == null) {
            // no need to log; null value should never occur
            return false;
        }
        String logbackFilename = placeHolders.get(PlaceHolder.LOGBACK_FILENAME);
        if (logbackFilename == null) {
            // no need to log; null value should never occur
            return false;
        }
        Path logbackPath = Paths.get(execFolder + File.separator + logbackFilename);
        try {
            if (outputLog) {
                List<String> lines = Files.readAllLines(logbackPath);
                if (lines != null) {
                    for (String line : lines) {
                        logger.info(line);
                    }
                }
            }
            if (deleteFile) {
                Files.delete(logbackPath);
            }
            return true;
        } catch (IOException e) {
            // no need to log; the log-back file may simply not be there
        }
        return false;
    }

    /**
     * Delete any prior script file.
     */
    private void deletePriorScriptFile() {
        String execFolder = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);
        if (execFolder == null) {
            // no need to log; null value should never occur
            return;
        }
        String execFilename = placeHolders.get(PlaceHolder.EXECUTE_FILENAME);
        if (execFilename == null) {
            // no need to log; null value should never occur
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(execFolder + File.separator + execFilename));
        } catch (IOException e) {
            logger.debug("Error deleting prior script file: {}", e.getMessage());
        }
    }

    /**
     * Download maven-metadata.xml from the given url and extract its latest OH version.
     *
     * @param url source of a maven-metadata.xml file
     * @return latest available OH version or UNKNOWN_VERSION
     */
    private String getArtifactoryLatestVersion(String url) {
        String result = VERSION_NOT_DEFINED;
        XMLStreamReader reader;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(new URL(url).openStream());
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    if ("latest".equals(reader.getLocalName())) {
                        result = reader.getElementText();
                        break;
                    }
                }
            }
            reader.close();
        } catch (IOException | XMLStreamException e) {
            logger.debug("Error reading maven metadata from artifactory: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Get the latest online available OH version for the target upgrade type.
     *
     * @return version e.g. 3.0.2, 3.1.0.M4, 3.1.0-SNAPSHOT, or VERSION_NOT_DEFINED
     */
    public String getRemoteVersion() {
        String result;
        switch (getTargetVersionType()) {
            case STABLE:
                result = getArtifactoryLatestVersion(RELEASE_ARTIFACT_URL);
                break;
            case MILESTONE:
                result = getArtifactoryLatestVersion(MILESTONE_ARTIFACT_URL);
                break;
            case SNAPSHOT:
                String oldVersion = OpenHAB.getVersion();
                if (!oldVersion.isEmpty()) {
                    result = oldVersion + "-" + SNAPSHOT;
                    break;
                }
            default:
                result = VERSION_NOT_DEFINED;
        }
        placeHolders.put(PlaceHolder.TARGET_VERSION, result);
        return result;
    }

    /**
     * Get the actual running version of OpenHAB on this system.
     *
     * @return the version number, or VERSION_NOT_DEFINED
     */
    public static String getActualVersion() {
        String oldVersion = OpenHAB.getVersion();
        return oldVersion.isEmpty() ? VERSION_NOT_DEFINED : oldVersion;
    }

    /**
     * Return the version type to upgrade to.
     *
     * @return target version = STABLE, MILESTONE, or SNAPSHOT
     */
    public TargetVersionType getTargetVersionType() {
        return targetVersionType;
    }

    /**
     * This a helper method that converts the index'th element of a version String[] array to an integer value. If there
     * is no index'th element or its value is not an integer, this is interpreted as 0.
     *
     * @param verStringArray set of parts of a version String[] array e.g. "3.2.1"
     * @param index the element of the String[] array to convert e.g. index 0 of "3.2.1" is "3"
     * @return the integer value, or 0 if no integer found
     */
    private static Integer convertStringArrayElementToInteger(String[] verStringArray, int index) {
        try {
            return index < verStringArray.length ? Integer.valueOf(verStringArray[index]) : 0;
        } catch (NumberFormatException e) {
        }
        return 0;
    }

    /**
     * Indicates if an update is available. Compares the actual running version against the latest remote version, and
     * returns whether the latter is a higher version than the former.
     * <p>
     * e.g. compares two strings such as 3.2.1 / 3.2.1.M1 / 3.2.1-SNAPSHOT against each other.
     * <p>
     * The comparison behaves differently depending on the value of {@link TargetVersionType} ..
     * <li>{@link TargetVersionType.STABLE} compares each integer part of the two version strings
     * <li>{@link TargetVersionType.MILESTONE} as 'STABLE' and if the same, compares 'M' parts
     * <li>{@link TargetVersionType.SNAPSHOT} as for 'STABLE' and if the same, returns {@link TriState.DONT_KNOW}
     *
     * @return {@link TriState.YES}, or {@link TriState.NO}, or {@link TriState.DONT_KNOW}
     */
    public TriState getRemoteVersionHigher() {
        // split the version strings into parts
        String[] actVerParts = getActualVersion().replace("-", ".").split("\\.");
        String[] remVerParts = getRemoteVersion().replace("-", ".").split("\\.");

        // compare the first three parts e.g. 3.0.0 <=> 3.0.1
        int compareOverall = 0;
        for (int i = 0; i < 3; i++) {
            int compareResult = convertStringArrayElementToInteger(actVerParts, i)
                    .compareTo(convertStringArrayElementToInteger(remVerParts, i));
            if (compareResult != 0) {
                compareOverall = compareResult;
                break;
            }
        }
        // if the first three parts are all equal, compare the fourth part
        if (compareOverall == 0) {
            switch (getTargetVersionType()) {
                case SNAPSHOT:
                    // can't say if one snapshot is newer than another e.g. 3.0.0-SNAPSHOT
                    return TriState.DONT_KNOW;
                case MILESTONE:
                    // compare the fourth part e.g. 3.0.0.M1 <=> 3.0.0.M2
                    if (remVerParts.length > 3) {
                        // alpha numeric compare (works until M9)
                        compareOverall = actVerParts.length > 3 ? actVerParts[3].compareTo(remVerParts[3]) : 1;
                    }
                default:
            }
        }
        return compareOverall < 0 ? TriState.YES : TriState.NO;
    }

    /**
     * Implementation of {@link Runnable}.run() interface method that first creates and then runs an updater script.
     */
    @Override
    public void run() {
        synchronized (RUN_LOCK) {
            // check the update target version is defined
            String latestVersion = getRemoteVersion();
            if (VERSION_NOT_DEFINED.equals(latestVersion)) {
                logger.debug("Target version is not defined");
                return;
            }
            // check that all place holders exist
            if (!checkPlaceHoldersExist()) {
                logger.debug("Some placeholders do not exist");
                return;
            }
            // replace place holders in the command line with actual values
            if (!fixupExecuteCommand()) {
                logger.debug("Could not fix up the command line");
                return;
            }
            // create the update script file
            if (!createUpdateScriptFile()) {
                logger.debug("Failed to create OpenHAB update script file");
                return;
            }
            // create the log-back file
            if (!createLogbackFile(
                    String.format("Restarted OpenHAB after update process [%s => %s] was initiated on %tc",
                            getActualVersion(), latestVersion, Calendar.getInstance()))) {
                logger.debug("Failed to create log back file");
                return;
            }
            // execute the update script
            if (!runUpdateScript()) {
                logAndDeleteLogbackFile(true, false);
                logger.debug("Failed to run OpenHAB update script");
                return;
            }
            logger.info("Stopping OpenHAB and initiating update process [{} => {}]", getActualVersion(), latestVersion);
        }
    }
}
