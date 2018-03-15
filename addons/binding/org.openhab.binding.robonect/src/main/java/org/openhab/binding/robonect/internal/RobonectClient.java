/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.internal;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.B64Code;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.VersionInfo;
import org.openhab.binding.robonect.internal.model.cmd.Command;
import org.openhab.binding.robonect.internal.model.cmd.ErrorCommand;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.binding.robonect.internal.model.cmd.NameCommand;
import org.openhab.binding.robonect.internal.model.cmd.StartCommand;
import org.openhab.binding.robonect.internal.model.cmd.StatusCommand;
import org.openhab.binding.robonect.internal.model.cmd.StopCommand;
import org.openhab.binding.robonect.internal.model.cmd.VersionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RobonectClient} class is responsible to communicate with the robonect module via it's HTTP interface.
 * 
 * The API of the module is documented here: http://robonect.de/viewtopic.php?f=10&t=37
 * 
 * @author Marco Meyer - Initial contribution
 */
public class RobonectClient {

    private final Logger logger = LoggerFactory.getLogger(RobonectClient.class);

    private final HttpClient httpClient;

    private final ModelParser parser;

    /**
     * The {@link JobSettings} class holds the values required for starting a job.
     */
    public class JobSettings {
        private static final String TIME_REGEX = "^[012]\\d:\\d\\d$";
        private ModeCommand.RemoteStart remoteStart;
        private ModeCommand.Mode after;
        private String start;
        private String end;

        /**
         * returns the 'remote start' setting for the job. See {@link ModeCommand.RemoteStart} for details.
         * @return - the remote start settings for the job.
         */
        public ModeCommand.RemoteStart getRemoteStart() {
            if (remoteStart != null) {
                return remoteStart;
            } else {
                logger.debug("No explicit remote start set. Returnt STANDARD.");
                return ModeCommand.RemoteStart.STANDARD;
            }
        }

        /**
         * Sets the desired 'remote start' settings for the job.
         * @param remoteStart - The 'remote start' settings. See {@link ModeCommand.RemoteStart} for the allowed modes.
         */
        public void setRemoteStart(ModeCommand.RemoteStart remoteStart) {
            this.remoteStart = remoteStart;
        }

        /**
         * Returns the mode the mower should be set to after the job is complete.
         * @return - the mode after compleness of the job.
         */
        public ModeCommand.Mode getAfter() {
            return after;
        }

        /**
         * Sets the mode after the mower is complete with the job.
         * @param after - the desired mode after job completeness.
         */
        public void setAfter(ModeCommand.Mode after) {
            this.after = after;
        }

        /**
         * Returns the start time of the job in the format HH:MM (H = Hour, M = Minute).
         * @return - the start time of the job.
         */
        public String getStart() {
            return start;
        }

        /**
         * Sets the start time of the job. This needs to be specified in the format HH:MM (H = Hour, M = Minute).
         * @param start - the start time of the job.
         */
        public void setStart(String start) {
            if (start != null && start.matches(TIME_REGEX)) {
                this.start = start;
            } else {
                logger.debug("Got start value {} but expected something matching {}", start, TIME_REGEX);
            }
        }

        /**
         * Returns the end time of the job in the format HH:MM (H = Hour, M = Minute).
         *
         * @return - the end time of the job.
         */
        public String getEnd() {
            return end;
        }

        /**
         * Sets the end time of the job. This needs to be specified in the format HH:MM (H = Hour, M = Minute).
         *
         * @param end - the end time of the job.
         */
        public void setEnd(String end) {
            if (end != null && end.matches(TIME_REGEX)) {
                this.end = end;
            } else {
                logger.debug("Got end value {} but expected something matching {}", end, TIME_REGEX);
            }
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

        public URI getURI() {
            return this.uri;
        }

        public void apply(Request request) {
            request.header(this.header, this.value);
        }

        public String toString() {
            return String.format("Basic authentication result for %s", this.uri);
        }
    }

    private JobSettings jobSettings;

    private final String baseUrl;

    /**
     * Creates an instance of RobonectClient which allows to communicate with the specified endpoint via the passed
     * httpClient instance.
     * @param httpClient - The HttpClient to use for the communication.
     * @param endpoint - The endpoint information for connecting and issuing commands.
     */
    public RobonectClient(HttpClient httpClient, RobonectEndpoint endpoint) {
        this.httpClient = httpClient;
        this.baseUrl = "http://" + endpoint.getIpAddress() + "/json";
        this.parser = new ModelParser();
        this.jobSettings = new JobSettings();
        if (endpoint.isUseAuthentication()) {
            addPreemptiveAuthentication(httpClient, endpoint);
        }
    }

    private void addPreemptiveAuthentication(HttpClient httpClient, RobonectEndpoint endpoint) {
        AuthenticationStore auth = httpClient.getAuthenticationStore();
        URI uri = URI.create(baseUrl);
        auth.addAuthenticationResult(new BasicResult(HttpHeader.AUTHORIZATION, uri, "Basic " + B64Code
                .encode(endpoint.getUser() + ":" + endpoint.getPassword(), StandardCharsets.ISO_8859_1)));
    }

    /**
     * returns general mower information. See {@MowerInfo} for the detailed information.
     * @return - the general mower information including a general success status.
     * @throws InterruptedException - is thrown in case the http client thread was interrupted while sending the command.
     */
    public MowerInfo getMowerInfo() throws InterruptedException {
        String responseString = sendCommand(new StatusCommand());
        return parser.parse(responseString, MowerInfo.class);
    }

    /**
     * sends a start command to the mower.
     * @return - a general answer with success status.
     * @throws InterruptedException - is thrown in case the http client thread was interrupted while sending the command.
     */
    public RobonectAnswer start() throws InterruptedException {
        String responseString = sendCommand(new StartCommand());
        return parser.parse(responseString, RobonectAnswer.class);
    }

    /**
     * sends a stop command to the mower.
     *
     * @return - a general answer with success status.
     * @throws InterruptedException - is thrown in case the http client thread was interrupted while sending the command.
     */
    public RobonectAnswer stop() throws InterruptedException {
        String responseString = sendCommand(new StopCommand());
        return parser.parse(responseString, RobonectAnswer.class);
    }

    /**
     * resets the errors on the mower.
     * @return - a general answer with success status.
     * @throws InterruptedException - is thrown in case the http client thread was interrupted while sending the command.
     */
    public RobonectAnswer resetErrors() throws InterruptedException {
        String responseString = sendCommand(new ErrorCommand().withReset(true));
        return parser.parse(responseString, RobonectAnswer.class);
    }

    /**
     * returns the list of all errors happened since last reset.
     * @return - the list of errors.
     * @throws InterruptedException - is thrown in case the http client thread was interrupted while sending the command.
     */
    public ErrorList errorList() throws InterruptedException {
        String responseString = sendCommand(new ErrorCommand());
        return parser.parse(responseString, ErrorList.class);
    }

    /**
     * Sets the mode of the mower. See {@link ModeCommand.Mode} for details about the available modes. Not allowed is mode
     * {@link ModeCommand.Mode#JOB}.
     * 
     * @param mode - the desired mower mode.
     * @return - a general answer with success status.
     * @throws InterruptedException - is thrown in case the http client thread was interrupted while sending the command.
     */
    public RobonectAnswer setMode(ModeCommand.Mode mode) throws InterruptedException {
        String responseString = sendCommand(createCommand(mode));
        return parser.parse(responseString, RobonectAnswer.class);
    }

    private ModeCommand createCommand(ModeCommand.Mode mode) {
        if (mode != ModeCommand.Mode.JOB) {
            return new ModeCommand(mode);
        } else {
            return new ModeCommand(mode).withRemoteStart(jobSettings.remoteStart).withAfter(jobSettings.after)
                    .withStart(jobSettings.start).withEnd(jobSettings.end);
        }
    }

    /**
     * Returns the name of the mower.
     * @return - The name including a general answer with success status.
     * @throws InterruptedException - is thrown in case the http client thread was interrupted while sending the command.
     */
    public Name getName() throws InterruptedException {
        String responseString = sendCommand(new NameCommand());
        return parser.parse(responseString, Name.class);
    }

    /**
     * Allows to set the name of the mower.
     * @param name - the desired name.
     * @return - The resulting name including a general answer with success status.
     * @throws InterruptedException - is thrown in case the http client thread was interrupted while sending the command.
     */
    public Name setName(String name) throws InterruptedException {
        String responseString = sendCommand(new NameCommand().withNewName(name));
        return parser.parse(responseString, Name.class);
    }

    private String sendCommand(Command command) throws InterruptedException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("send HTTP GET to: {} ", command.toCommandURL(baseUrl));
            }
            ContentResponse response = httpClient.newRequest(command.toCommandURL(baseUrl)).method(HttpMethod.GET)
                                .timeout(30000, TimeUnit.MILLISECONDS).send();
            String responseString = null;
            
            // jetty uses UTF-8 as default encoding. However, HTTP 1.1 specifies ISO_8859_1
            if(StringUtils.isBlank(response.getEncoding())){
                responseString = new String(response.getContent(), StandardCharsets.ISO_8859_1);
            }else {
                // currently v0.9e Robonect does not specifiy the encoding. But if later versions will
                // add, it should work with the default method to get the content as string.
                responseString = response.getContentAsString();
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("Response body was: {} ", responseString);
            }
            return responseString;
        } catch (ExecutionException | TimeoutException e) {
            throw new RobonectCommunicationException("Could not send command " + command.toCommandURL(baseUrl), e);
        }
    }

    /**
     * Retrieve the version information of the mower and module. See {@link VersionInfo} for details.
     * @return - the Version Information including the successful status.
     * @throws InterruptedException - is thrown in case the http client thread was interrupted while sending the command.
     */
    public VersionInfo getVersionInfo() throws InterruptedException {
        String versionResponse = sendCommand(new VersionCommand());
        return parser.parse(versionResponse, VersionInfo.class);
    }

    /**
     * The currently set job settings which will be used when a job is started.
     * @return - the currently set job settings.
     */
    public JobSettings getJobSettings() {
        return jobSettings;
    }
}
