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
import java.nio.file.Paths;
import java.util.ArrayList;
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

    private static final Integer MIN_SLEEP_SECS = 5; // seconds
    private static final Integer DEFAULT_SLEEP_SECS = 20;
    private static final Integer MAX_SLEEP_SECS = 60;

    private TargetVersionType targetVersionType = TargetVersionType.STABLE;

    protected static final String FILE_ID = "oh-update";

    /**
     * Place holders are keys in the script templates to be replaced by values at runtime
     */
    protected enum PlaceHolder {
        // COMMON place holders: values will be provided by this BaseUpdater class
        TITLE("[TITLE]"),
        USER_NAME("[USER_NAME]"),
        PASSWORD("[PASSWORD]"),
        SLEEP_TIME("[SLEEP_TIME]"),
        TARGET_TYPE("[TARGET_TYPE]"),
        TARGET_VERSION("[TARGET_VERSION]"),
        USERDATA_FOLDER("[USERDATA_FOLDER]"),
        LOGBACK_FILENAME("[LOGBACK_FILENAME]"),
        // EXTENDED place holders: values **MUST** be provided by classes that extend BaseUpdater
        EXECUTE_COMMAND("[EXECUTE_COMMAND]"),
        EXECUTE_FOLDER("[EXECUTE_FOLDER]"),
        EXECUTE_FILENAME("[EXECUTE_FILENAME]"),
        RUNTIME_FOLDER("[RUNTIME_FOLDER]"),
        // EXTENDED place holders: values **MAY** be provided by classes that extend BaseUpdater
        STD_OUT_FILENAME("[STD_OUT_FILENAME]");

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
        // initialise COMMON place holders
        placeHolders.put(PlaceHolder.TITLE, TITLE);
        placeHolders.put(PlaceHolder.USER_NAME, "");
        placeHolders.put(PlaceHolder.PASSWORD, "");
        placeHolders.put(PlaceHolder.SLEEP_TIME, DEFAULT_SLEEP_SECS.toString());
        placeHolders.put(PlaceHolder.TARGET_TYPE, targetVersionType.label);
        placeHolders.put(PlaceHolder.USERDATA_FOLDER, OpenHAB.getUserDataFolder());
        placeHolders.put(PlaceHolder.LOGBACK_FILENAME, FILE_ID + ".log");
        placeHolders.put(PlaceHolder.STD_OUT_FILENAME, "");
        // call the extending class method to initialise the EXTENDED place holders
        initializeExtendedPlaceholders();
        // log the prior log-back file entries
        logggerInfoLogbackFile();
        // delete prior files
        deletePriorFiles();
    }

    // ================== Run() method ==================

    /**
     * Implementation of Runnable.run() interface method that creates and runs an updater script.
     */
    @Override
    public void run() {
        synchronized (RUN_LOCK) {
            // refresh the remote version
            getRemoteLatestVersion();

            // check that all place holders exist
            if (!checkPlaceHoldersExist()) {
                logger.debug("Some placeholders do not exist");
                return;
            }

            // check the remote version is defined
            if (VERSION_NOT_DEFINED.equals(placeHolders.get(PlaceHolder.TARGET_VERSION))) {
                logger.debug("Target version is not defined");
                return;
            }

            // replace place holders in the command line with actual values
            if (!fixupExecuteCommand()) {
                logger.debug("Could not fix up the command line");
                return;
            }

            // log place holder values
            loggerDebugPlaceHolderValues();

            // create the update script file
            if (!createScriptFile()) {
                logger.debug("Failed to create OpenHAB update script file");
                return;
            }

            // create the log-back file
            if (!createLogbackFile()) {
                logger.debug("Failed to create log back file");
                return;
            }

            // execute the update script
            if (!runScriptFile()) {
                logger.debug("Failed to run OpenHAB update script");
                deletePriorFiles();
                return;
            }

            // inform the user via the log file
            loggerInfoUpdateStarted();
        }
    }

    // ================== Setters ==================

    /**
     * Set the target version.
     *
     * @param targetVersionString string representation of a target version i.e. STABLE, MILESTONE, SNAPSHOT
     * @throws IllegalArgumentException
     */
    public void setTargetVersion(String targetVersionString) throws IllegalArgumentException {
        targetVersionType = TargetVersionType.valueOf(targetVersionString.toUpperCase());
        placeHolders.put(PlaceHolder.TARGET_TYPE, targetVersionType.label);
    }

    /**
     * Set the password.
     *
     * @param password a valid password containing no whitespace, and 20 or fewer characters long
     * @throws IllegalArgumentException
     */
    public void setPassword(String password) throws IllegalArgumentException {
        boolean fail = false;
        // prevent buffer overrun by checking that the string length is <= 20 (say)
        if (password.length() > 20) {
            fail = true;
        }
        // prevent script injection by checking that the string contains no whitespace
        for (int i = 0; i < password.length(); i++) {
            if (Character.isWhitespace(password.charAt(i))) {
                fail = true;
                break;
            }
        }
        if (fail) {
            IllegalArgumentException e = new IllegalArgumentException(
                    String.format("Password %s is invalid.", password));
            logger.debug("{}", e.getMessage());
            throw e;
        }
        placeHolders.put(PlaceHolder.PASSWORD, password);
    }

    /**
     * Set the user name.
     *
     * @param userName a valid user name containing only alpha numerics, and 20 or fewer characters long
     * @throws IllegalArgumentException
     */
    public void setUserName(String userName) throws IllegalArgumentException {
        boolean fail = false;
        // prevent buffer overrun by checking that the string length is <= 20 (say)
        if (userName.length() > 20) {
            fail = true;
        }
        // prevent script injection by checking that the string contains only alpha-numeric characters
        for (int i = 0; i < userName.length(); i++) {
            if (!Character.isLetter('c')) { // OrDigit(userName.charAt(i))) {
                fail = true;
                break;
            }
        }
        if (fail) {
            IllegalArgumentException e = new IllegalArgumentException(
                    String.format("User name %s is invalid.", userName));
            logger.debug("{}", e.getMessage());
            throw e;
        }
        placeHolders.put(PlaceHolder.USER_NAME, userName);
    }

    /**
     * Set the sleepTime.
     *
     * @param sleepTimeString string representation of an integer between 5 and 30 (seconds)
     * @throws IllegalArgumentException
     */
    public void setSleepTime(String sleepTimeString) throws IllegalArgumentException {
        Integer sleepTimeInteger;
        try {
            sleepTimeInteger = Integer.valueOf(sleepTimeString);
        } catch (NumberFormatException e) {
            sleepTimeInteger = -1;
        }
        if ((MIN_SLEEP_SECS > sleepTimeInteger) || (sleepTimeInteger > MAX_SLEEP_SECS)) {
            IllegalArgumentException e = new IllegalArgumentException(
                    String.format("Argument value {} is invalid.", sleepTimeString));
            logger.debug("{}", e.getMessage());
            throw e;
        }
        placeHolders.put(PlaceHolder.SLEEP_TIME, sleepTimeInteger.toString());
    }

    // ================== Getters ==================

    /**
     * Get the latest online available OH version for the target upgrade type.
     *
     * @return version e.g. 3.0.2, 3.1.0.M4, 3.1.0-SNAPSHOT, or VERSION_NOT_DEFINED
     */
    public String getRemoteLatestVersion() {
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
        String oldVersion;
        try {
            oldVersion = OpenHAB.getVersion();
        } catch (NullPointerException e) {
            // OpenHAB.getVersion() throws an NPE when calling it offline in a JUnit test
            oldVersion = "";
        }
        return oldVersion.isEmpty() ? VERSION_NOT_DEFINED : oldVersion;
    }

    /**
     * Get the version type to upgrade to.
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
     * Get whether an update is available. Compares the actual running version against the latest remote version, and
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
        String[] remVerParts = getRemoteLatestVersion().replace("-", ".").split("\\.");

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
     * Get the latest version on the artifactory. Download maven-metadata.xml from the given url and extract its latest
     * OH version.
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

    // ================== MUST be overridden ==================

    /**
     * This method is used to initialise the extended place holders.
     * <p>
     * Note: it is not allowed to instantiate BaseUpdater directly, and it would throw an exception. Instead one must
     * instantiate a class that extends BaseUpdater and overrides this method.
     * <p>
     * NOTE: classes that extend from BaseUpdater **MUST** set the following place holder values
     * <li>EXECUTE_FOLDER = the folder where the update script shall be run from
     * <li>EXECUTE_FILENAME = the file name of the update script
     * <li>EXECUTE_COMMAND = the shell command use to execute the update script
     * <li>RUNTIME_FOLDER = the OpenHAB runtime folder
     * <p>
     * NOTE: classes that extend from BaseUpdater **MAY** set the following place holder values
     * <li>STD_OUT_FILENAME = the STDOUT and STDERR redirect file
     */
    protected abstract void initializeExtendedPlaceholders();

    // ================== Update Process Actions ==================

    /**
     * Check if all of the place holders have values.
     *
     * @return true if all place holders are non empty strings
     */
    private boolean checkPlaceHoldersExist() {
        boolean fail = false;
        for (PlaceHolder placeHolder : PlaceHolder.values()) {
            fail = fail || (placeHolders.get(placeHolder) == null);
        }
        return !fail;
    }

    /**
     * Fix-up place holders in the execute command with their runtime values.
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
     * Log the place holder values to DEBUG.
     */
    private void loggerDebugPlaceHolderValues() {
        if (logger.isDebugEnabled()) {
            for (PlaceHolder placeHolder : PlaceHolder.values()) {
                logger.debug("{}:{}", placeHolder.name(), placeHolders.get(placeHolder));
            }
        }
    }

    /**
     * Read a text resource from within the jar, convert its place holders to actual values, and write it to
     * the script file to be executed. This method MAY be overridden in extending classes.
     *
     * @return success
     */
    protected boolean createScriptFile() {
        String folder = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);
        String filename = placeHolders.get(PlaceHolder.EXECUTE_FILENAME);

        // create an input stream to access the resource
        String resourceId = "/scripts/" + getClass().getSimpleName() + ".txt";
        InputStream resourceStream = getClass().getResourceAsStream(resourceId);
        if (resourceStream == null) {
            logger.debug("Could not find resource id: {}", resourceId);
            return false;
        }

        // read script lines from the resource
        @SuppressWarnings("null")
        List<String> templateLines = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.toList());

        // remove empty lines and comments and fix-up place holders to their runtime values
        List<String> outputLines = new ArrayList<>();
        for (int i = 0; i < templateLines.size(); i++) {
            String line = templateLines.get(i);
            if ((!line.isBlank() && !line.startsWith("# ") && !line.startsWith("::"))
                    || line.contains(PlaceHolder.EXECUTE_COMMAND.key)) {
                for (Map.Entry<PlaceHolder, String> placeHolder : placeHolders.entrySet()) {
                    line = line.replace(placeHolder.getKey().key, placeHolder.getValue());
                }
                outputLines.add(line);
            }
        }

        // write the script lines to the file
        try {
            Files.write(Paths.get(folder + File.separator + filename), outputLines);
            return true;
        } catch (IOException e) {
            logger.debug("Error creating script file: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Create the log-back file with a message in it.
     *
     * @return success
     */
    private boolean createLogbackFile() {
        String folder = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);
        String filename = placeHolders.get(PlaceHolder.LOGBACK_FILENAME);
        try {
            Files.write(Paths.get(folder + File.separator + filename),
                    Arrays.asList(String.format("Update [%s => %s] start at %tc; Restarted OpenHAB", getActualVersion(),
                            placeHolders.get(PlaceHolder.TARGET_VERSION), Calendar.getInstance())));
            return true;
        } catch (IOException e) {
            logger.debug("Error creating log-back file: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Run the update script file.
     *
     * @return success
     */
    private boolean runScriptFile() {
        String command = placeHolders.getOrDefault(PlaceHolder.EXECUTE_COMMAND, "");
        String folder = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(Arrays.asList(command.split(" ")));
        builder.directory(new File(folder));

        String redirect = placeHolders.getOrDefault(PlaceHolder.STD_OUT_FILENAME, "");
        if (!redirect.isEmpty()) {
            File outFile = new File(folder + File.separator + redirect);
            builder.redirectOutput(outFile);
            builder.redirectError(outFile);
        } else {
            redirect = "none";
        }

        try {
            logger.debug("Starting process: directory={}, command={}, redirect={}", folder, command, redirect);
            builder.start();
            logger.debug("Started process: directory={}, command={}, redirect={}", folder, command, redirect);
            return true;
        } catch (IOException e) {
            logger.debug("Failed to start script: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Read the log-back file (if present) and log its contents to INFO.
     */
    private void logggerInfoLogbackFile() {
        if (logger.isInfoEnabled()) {
            String folder = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);
            String filename = placeHolders.get(PlaceHolder.LOGBACK_FILENAME);
            try {
                List<String> lines = Files.readAllLines(Paths.get(folder + File.separator + filename));
                if (lines != null) {
                    for (String line : lines) {
                        logger.info("{}", line);
                    }
                }
            } catch (IOException e) {
                // do not log this as an error, as the file may simply not be there any more
            }
        }
    }

    /**
     * Helper method to delete a specific prior script file.
     *
     * @param fileId identifies the file to be deleted
     */
    private void deletePriorFile(PlaceHolder fileId) {
        String folder = placeHolders.get(PlaceHolder.EXECUTE_FOLDER);
        String filename = placeHolders.get(fileId);
        try {
            Files.deleteIfExists(Paths.get(folder + File.separator + filename));
        } catch (IOException e) {
            logger.debug("Error deleting prior {} file: {}", fileId.name(), e.getMessage());
        }
    }

    /**
     * Delete all prior script files (except when in logger debug mode).
     */
    private void deletePriorFiles() {
        if (!logger.isDebugEnabled()) {
            deletePriorFile(PlaceHolder.LOGBACK_FILENAME);
            deletePriorFile(PlaceHolder.STD_OUT_FILENAME);
            deletePriorFile(PlaceHolder.EXECUTE_FILENAME);
        }
    }

    /**
     * Log an INFO entry in the log file to indicate that update process has started. This method MAY be overridden in
     * extending classes.
     *
     * @param latestVersion
     */
    protected void loggerInfoUpdateStarted() {
        logger.info("Stopping OpenHAB; Starting update [{} => {}]", getActualVersion(),
                placeHolders.get(PlaceHolder.TARGET_VERSION));
    }
}
