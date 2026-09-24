/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.robonect.internal.model.BatteryInfo;
import org.openhab.binding.robonect.internal.model.DoorInfo;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.HourInfo;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.MotorInfo;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.PortalInfo;
import org.openhab.binding.robonect.internal.model.RemoteInfo;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.TimerInfo;
import org.openhab.binding.robonect.internal.model.TimersInfo;
import org.openhab.binding.robonect.internal.model.VersionInfo;
import org.openhab.binding.robonect.internal.model.WeatherInfo;
import org.openhab.binding.robonect.internal.model.WireInfo;
import org.openhab.binding.robonect.internal.model.cmd.BatteryCommand;
import org.openhab.binding.robonect.internal.model.cmd.Command;
import org.openhab.binding.robonect.internal.model.cmd.ErrorCommand;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand.Direction;
import org.openhab.binding.robonect.internal.model.cmd.HourCommand;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.binding.robonect.internal.model.cmd.MotorCommand;
import org.openhab.binding.robonect.internal.model.cmd.NameCommand;
import org.openhab.binding.robonect.internal.model.cmd.PortalCommand;
import org.openhab.binding.robonect.internal.model.cmd.RemoteCommand;
import org.openhab.binding.robonect.internal.model.cmd.ServiceCommand;
import org.openhab.binding.robonect.internal.model.cmd.ServiceCommand.Operation;
import org.openhab.binding.robonect.internal.model.cmd.StartCommand;
import org.openhab.binding.robonect.internal.model.cmd.StatusCommand;
import org.openhab.binding.robonect.internal.model.cmd.StopCommand;
import org.openhab.binding.robonect.internal.model.cmd.TimerCommand;
import org.openhab.binding.robonect.internal.model.cmd.VersionCommand;
import org.openhab.binding.robonect.internal.model.cmd.WeatherCommand;
import org.openhab.binding.robonect.internal.model.cmd.WireCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RobonectClient} class is responsible to communicate with the robonect module via it's HTTP interface.
 *
 * @see <a href="http://robonect.de/viewtopic.php?f=10&t=37">
 *      http://robonect.de/viewtopic.php?f=10&amp;t=37</a>. The API of the module is documented here.
 *
 * @author Marco Meyer - Initial contribution
 */
public class RobonectClient {

    private final Logger logger = LoggerFactory.getLogger(RobonectClient.class);

    private final String baseUrl;

    private final HttpClient httpClient;

    private final ModelParser parser;

    private boolean jobRunning;

    /**
     * The {@link JobSettings} class holds the values required for starting a job.
     */
    public static class JobSettings {

        private final Logger logger = LoggerFactory.getLogger(RobonectClient.class);

        private ModeCommand.RemoteStart remoteStart;
        private ModeCommand.Mode after;
        private int duration;
        private Integer start;
        private Integer end;

        /**
         * returns the 'remote start' setting for the job. See {@link ModeCommand.RemoteStart} for details.
         *
         * @return - the remote start settings for the job.
         */
        public ModeCommand.RemoteStart getRemoteStart() {
            if (remoteStart != null) {
                return remoteStart;
            } else {
                logger.debug("No explicit remote start set. Return STANDARD.");
                return ModeCommand.RemoteStart.STANDARD;
            }
        }

        /**
         * Sets the desired 'remote start' settings for the job.
         *
         * @param remoteStart - The 'remote start' settings. See {@link ModeCommand.RemoteStart} for the allowed modes.
         */
        public JobSettings withRemoteStart(ModeCommand.RemoteStart remoteStart) {
            this.remoteStart = remoteStart;
            return this;
        }

        /**
         * Returns the mode the mower should be set to after the job is complete.
         *
         * @return - the mode after compleness of the job.
         */
        public ModeCommand.Mode getAfterMode() {
            return after;
        }

        /**
         * Sets the mode after the mower is complete with the job.
         *
         * @param after - the desired mode after job completeness.
         */
        public JobSettings withAfterMode(ModeCommand.Mode after) {
            this.after = after;
            return this;
        }

        public int getDuration() {
            return duration;
        }

        public JobSettings withDuration(int duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Sets the job start time as minutes after midnight.
         *
         * @param start the start time, or {@code null} to omit it from the command
         * @return this job settings instance
         */
        public JobSettings withStart(Integer start) {
            this.start = start;
            return this;
        }

        /**
         * Sets the job end time as minutes after midnight.
         *
         * @param end the end time, or {@code null} to omit it from the command
         * @return this job settings instance
         */
        public JobSettings withEnd(Integer end) {
            this.end = end;
            return this;
        }

        private String getStart() {
            return formatTime(start);
        }

        private String getEnd() {
            return formatTime(end);
        }

        private String formatTime(Integer minutesAfterMidnight) {
            if (minutesAfterMidnight == null) {
                return null;
            }
            if (minutesAfterMidnight < 0 || minutesAfterMidnight >= 24 * 60) {
                throw new IllegalArgumentException("Time must be between 0 and 1439 minutes after midnight");
            }
            return "%02d:%02d".formatted(minutesAfterMidnight / 60, minutesAfterMidnight % 60);
        }
    }

    private static class BasicResult implements Authentication.Result {

        private final HttpHeader header;
        private final URI uri;
        private final String value;

        public BasicResult(HttpHeader header, URI uri, String value) {
            this.header = header;
            this.uri = uri;
            this.value = value;
        }

        @Override
        public URI getURI() {
            return this.uri;
        }

        @Override
        public void apply(Request request) {
            request.header(this.header, this.value);
        }

        @Override
        public String toString() {
            return String.format("Basic authentication result for %s", this.uri);
        }
    }

    /**
     * Creates an instance of RobonectClient which allows to communicate with the specified endpoint via the passed
     * httpClient instance.
     *
     * @param httpClient - The HttpClient to use for the communication.
     * @param endpoint - The endpoint information for connecting and issuing commands.
     */
    public RobonectClient(HttpClient httpClient, RobonectEndpoint endpoint) {
        this.httpClient = httpClient;
        this.baseUrl = "http://" + endpoint.getIpAddress() + "/json";
        this.parser = new ModelParser();

        if (endpoint.isUseAuthentication()) {
            addPreemptiveAuthentication(httpClient, endpoint);
        }
    }

    private void addPreemptiveAuthentication(HttpClient httpClient, RobonectEndpoint endpoint) {
        AuthenticationStore auth = httpClient.getAuthenticationStore();
        URI uri = URI.create(baseUrl);
        auth.addAuthenticationResult(
                new BasicResult(HttpHeader.AUTHORIZATION, uri, "Basic " + Base64.getEncoder().encodeToString(
                        (endpoint.getUser() + ":" + endpoint.getPassword()).getBytes(StandardCharsets.ISO_8859_1))));
    }

    /**
     * returns general mower information. See {@link MowerInfo} for the detailed information.
     *
     * @return - the general mower information including a general success status.
     */
    public MowerInfo getMowerInfo() {
        String responseString = sendCommand(new StatusCommand());
        MowerInfo mowerInfo = parser.parse(responseString, MowerInfo.class);
        if (jobRunning) {
            // mode might have been changed on the mower. Also Mode JOB does not really exist on the mower, thus cannot
            // be checked here
            if (mowerInfo.getStatus().getMode() == MowerMode.AUTO
                    || mowerInfo.getStatus().getMode() == MowerMode.HOME) {
                jobRunning = false;
            } else if (mowerInfo.getError() != null) {
                jobRunning = false;
            }
        }
        return mowerInfo;
    }

    /**
     * sends a start command to the mower.
     *
     * @return - a general answer with success status.
     */
    public RobonectAnswer start() {
        String responseString = sendCommand(new StartCommand());
        return parser.parse(responseString, RobonectAnswer.class);
    }

    /**
     * sends a stop command to the mower.
     *
     * @return - a general answer with success status.
     */
    public RobonectAnswer stop() {
        String responseString = sendCommand(new StopCommand());
        return parser.parse(responseString, RobonectAnswer.class);
    }

    /**
     * resets the errors on the mower.
     *
     * @return - a general answer with success status.
     */
    public RobonectAnswer resetErrors() {
        String responseString = sendCommand(new ErrorCommand().withReset(true));
        return parser.parse(responseString, RobonectAnswer.class);
    }

    /**
     * Changes the Robonect module's operating state.
     *
     * @param operation the requested service operation
     * @return the operation result including its success status
     */
    public RobonectAnswer performServiceOperation(Operation operation) {
        return parser.parse(sendCommand(new ServiceCommand(operation)), RobonectAnswer.class);
    }

    /**
     * Returns the state of the optional door system.
     *
     * @return the door information including its success status
     */
    public DoorInfo getDoorInfo() {
        return parser.parse(sendCommand(new DoorCommand()), DoorInfo.class);
    }

    /**
     * Opens or closes the optional door system.
     *
     * @param direction the requested movement direction
     * @return the door information including its success status
     */
    public DoorInfo setDoorDirection(Direction direction) {
        return parser.parse(sendCommand(new DoorCommand().withDirection(direction)), DoorInfo.class);
    }

    /**
     * Releases an arrested optional door system.
     *
     * @return the door information including its success status
     */
    public DoorInfo releaseDoor() {
        return parser.parse(sendCommand(new DoorCommand().withRelease()), DoorInfo.class);
    }

    /**
     * returns the list of all errors happened since last reset.
     *
     * @return - the list of errors.
     */
    public ErrorList errorList() {
        String responseString = sendCommand(new ErrorCommand());
        return parser.parse(responseString, ErrorList.class);
    }

    /**
     * Returns detailed battery information reported by the mower.
     *
     * @return the battery information including its success status
     */
    public BatteryInfo getBatteryInfo() {
        return parser.parse(sendCommand(new BatteryCommand()), BatteryInfo.class);
    }

    /**
     * Returns detailed motor information reported by the mower.
     *
     * @return the motor information including its success status
     */
    public MotorInfo getMotorInfo() {
        return parser.parse(sendCommand(new MotorCommand()), MotorInfo.class);
    }

    /**
     * Returns aggregate operating statistics reported by the mower.
     *
     * @return the operating statistics including their success status
     */
    public HourInfo getHourInfo() {
        return parser.parse(sendCommand(new HourCommand()), HourInfo.class);
    }

    /**
     * Returns the configured remote-start points.
     *
     * @return the remote-start information including its success status
     */
    public RemoteInfo getRemoteInfo() {
        return parser.parse(sendCommand(new RemoteCommand()), RemoteInfo.class);
    }

    /**
     * Returns boundary-wire sensor information.
     *
     * @return the sensor information including its success status
     */
    public WireInfo getWireInfo() {
        return parser.parse(sendCommand(new WireCommand()), WireInfo.class);
    }

    /**
     * Returns the remote portal connection status.
     *
     * @return the portal information including its success status
     */
    public PortalInfo getPortalInfo() {
        return parser.parse(sendCommand(new PortalCommand()), PortalInfo.class);
    }

    /**
     * Returns all timer configurations reported by the mower.
     *
     * @return the timer configurations including their success status
     */
    public TimersInfo getTimersInfo() {
        return parser.parse(sendCommand(new TimerCommand()), TimersInfo.class);
    }

    /**
     * Updates one value of an individual timer.
     *
     * @param timer timer number from 1 to 14
     * @param parameter API parameter name
     * @param value API parameter value
     * @return the updated timer including its success status
     */
    public TimerInfo updateTimer(int timer, String parameter, String value) {
        return parser.parse(sendCommand(new TimerCommand().withTimer(timer).withValue(parameter, value)),
                TimerInfo.class);
    }

    /**
     * Returns weather information calculated by the module.
     *
     * @return the weather information including its success status
     */
    public WeatherInfo getWeatherInfo() {
        return parser.parse(sendCommand(new WeatherCommand()), WeatherInfo.class);
    }

    /**
     * Sets the mode of the mower. See {@link ModeCommand.Mode} for details about the available modes. Not allowed is
     * mode
     * {@link ModeCommand.Mode#JOB}.
     *
     * @param mode - the desired mower mode.
     * @return - a general answer with success status.
     */
    public RobonectAnswer setMode(ModeCommand.Mode mode) {
        String responseString = sendCommand(createCommand(mode));
        if (jobRunning) {
            jobRunning = false;
        }
        return parser.parse(responseString, RobonectAnswer.class);
    }

    private ModeCommand createCommand(ModeCommand.Mode mode) {
        return new ModeCommand(mode);
    }

    /**
     * Returns the name of the mower.
     *
     * @return - The name including a general answer with success status.
     */
    public Name getName() {
        String responseString = sendCommand(new NameCommand());
        return parser.parse(responseString, Name.class);
    }

    /**
     * Allows to set the name of the mower.
     *
     * @param name - the desired name.
     * @return - The resulting name including a general answer with success status.
     */
    public Name setName(String name) {
        String responseString = sendCommand(new NameCommand().withNewName(name));
        return parser.parse(responseString, Name.class);
    }

    private String sendCommand(Command command) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("send HTTP GET to: {} ", command.toCommandURL(baseUrl));
            }
            ContentResponse response = httpClient.newRequest(command.toCommandURL(baseUrl)).method(HttpMethod.GET)
                    .timeout(30000, TimeUnit.MILLISECONDS).send();
            String responseString = null;

            // jetty uses UTF-8 as default encoding. However, HTTP 1.1 specifies ISO_8859_1
            if (response.getEncoding() == null || response.getEncoding().isBlank()) {
                responseString = new String(response.getContent(), StandardCharsets.ISO_8859_1);
            } else {
                // currently v0.9e Robonect does not specifiy the encoding. But if later versions will
                // add, it should work with the default method to get the content as string.
                responseString = response.getContentAsString();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Response body was: {} ", responseString);
            }
            return responseString;
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw new RobonectCommunicationException("Could not send command " + command.toCommandURL(baseUrl), e);
        }
    }

    /**
     * Retrieve the version information of the mower and module. See {@link VersionInfo} for details.
     *
     * @return - the Version Information including the successful status.
     */
    public VersionInfo getVersionInfo() {
        String versionResponse = sendCommand(new VersionCommand());
        return parser.parse(versionResponse, VersionInfo.class);
    }

    public boolean isJobRunning() {
        return jobRunning;
    }

    public RobonectAnswer startJob(JobSettings settings) {
        Command jobCommand = new ModeCommand(ModeCommand.Mode.JOB).withRemoteStart(settings.getRemoteStart())
                .withAfter(settings.getAfterMode()).withDuration(settings.getDuration()).withStart(settings.getStart())
                .withEnd(settings.getEnd());
        String responseString = sendCommand(jobCommand);
        RobonectAnswer answer = parser.parse(responseString, RobonectAnswer.class);
        if (answer.isSuccessful()) {
            jobRunning = true;
        } else {
            jobRunning = false;
        }
        return answer;
    }

    public RobonectAnswer stopJob(JobSettings settings) {
        RobonectAnswer answer = null;
        if (jobRunning) {
            answer = setMode(settings.after);
            if (answer.isSuccessful()) {
                jobRunning = false;
            }
        } else {
            answer = new RobonectAnswer();
            // this is not an error, thus return success
            answer.setSuccessful(true);
        }
        return answer;
    }
}
