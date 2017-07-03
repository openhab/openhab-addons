/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.net.Header;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractScalarWebProtocol.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
public abstract class AbstractScalarWebProtocol<T extends ThingCallback<ScalarWebChannel>>
        implements ScalarWebProtocol<T> {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(AbstractScalarWebProtocol.class);

    /** The tracker. */
    private final ScalarWebChannelTracker tracker;

    /** The state. */
    protected final ScalarWebState state;

    /** The service. */
    protected final ScalarWebService service;

    /** The callback. */
    protected final T callback;

    /**
     * Instantiates a new abstract scalar web protocol.
     *
     * @param tracker the tracker
     * @param state the state
     * @param webService the web service
     * @param callback the callback
     */
    protected AbstractScalarWebProtocol(ScalarWebChannelTracker tracker, ScalarWebState state,
            ScalarWebService webService, T callback) {
        Objects.requireNonNull(tracker, "tracker cannot be null");
        Objects.requireNonNull(state, "state cannot be null");
        Objects.requireNonNull(webService, "audioService cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        this.tracker = tracker;
        this.state = state;
        this.service = webService;
        this.callback = callback;
    }

    /**
     * Execute.
     *
     * @param mthd the mthd
     * @return the scalar web result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ScalarWebResult execute(String mthd) throws IOException {
        return execute(mthd, new Object[0]);
    }

    /**
     * Execute.
     *
     * @param mthd the mthd
     * @param parms the parms
     * @return the scalar web result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ScalarWebResult execute(String mthd, Object... parms) throws IOException {
        final ScalarWebResult result = handleExecute(mthd, parms);
        if (result.isError()) {
            throw result.getHttpResponse().createException();
        }

        return result;
    }

    /**
     * Handle execute.
     *
     * @param mthd the mthd
     * @param parms the parms
     * @return the scalar web result
     */
    protected ScalarWebResult handleExecute(String mthd, Object... parms) {
        final ScalarWebResult result = service.execute(mthd, parms);
        if (result.isError()) {
            final HttpResponse errRsp = result.getHttpResponse();
            switch (errRsp.getHttpCode()) {
                case ScalarWebResult.NotImplemented:
                    logger.debug("Method is not implemented on service {} - {}({}): {}", service.getServiceName(), mthd,
                            StringUtils.join(parms, ','), errRsp);
                    break;

                case ScalarWebResult.IllegalArgument:
                    logger.debug("Method arguments are incorrect on service {} - {}({}): {}", service.getServiceName(),
                            mthd, StringUtils.join(parms, ','), errRsp);
                    break;

                case ScalarWebResult.IllegalState:
                    logger.debug("Method state is incorrect on service {} - {}({}): {}", service.getServiceName(), mthd,
                            StringUtils.join(parms, ','), errRsp);
                    break;

                case ScalarWebResult.DisplayIsOff:
                    logger.debug("The display is off and command cannot be executed on service {} - {}({}): {}",
                            service.getServiceName(), mthd, StringUtils.join(parms, ','), errRsp);
                    break;

                case ScalarWebResult.FailedToLauch:
                    logger.debug("The application failed to launch (probably display is off) {} - {}({}): {}",
                            service.getServiceName(), mthd, StringUtils.join(parms, ','), errRsp);
                    break;

                default:
                    final IOException e = result.getHttpResponse().createException();
                    logger.debug("Communication error executing method {}([]) on service {}: {}", mthd,
                            StringUtils.join(parms, ','), service.getServiceName(), e.getMessage(), e);
                    callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    break;
            }
        }

        return result;
    }

    /**
     * Handle execute xml.
     *
     * @param uri the uri
     * @param body the body
     * @param headers the headers
     * @return the scalar web result
     */
    protected ScalarWebResult handleExecuteXml(String uri, String body, Header... headers) {
        final ScalarWebResult result = service.executeXml(uri, body, headers);
        if (result.isError()) {
            final HttpResponse errRsp = result.getHttpResponse();
            switch (errRsp.getHttpCode()) {
                case HttpStatus.SC_SERVICE_UNAVAILABLE:
                    logger.debug("IRCC service is unavailable (power off?)");
                    break;

                case HttpStatus.SC_FORBIDDEN:
                    logger.debug("IRCC methods have been forbidden on service {} ({}): {}", service.getServiceName(),
                            uri, errRsp);
                    break;

                default:
                    final IOException e = result.getHttpResponse().createException();
                    logger.debug("Communication error for IRCC method on service {} ({}): {}", service.getServiceName(),
                            uri, e.getMessage(), e);
                    callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    break;
            }
        }

        return result;
    }

    /**
     * Creates the channel.
     *
     * @param name the name
     * @param other the other
     * @return the scalar web channel
     */
    protected ScalarWebChannel createChannel(String name, String... other) {
        List<String> pathids = new ArrayList<String>();
        pathids.add(service.getServiceName());
        if (other != null) {
            for (String path : other) {
                pathids.add(path);
            }
        }
        pathids.add(name);
        return new ScalarWebChannel(pathids);
    }

    /**
     * Creates the descriptor.
     *
     * @param channel the channel
     * @param acceptedItemType the accepted item type
     * @param channelType the channel type
     * @return the scalar web channel descriptor
     */
    protected ScalarWebChannelDescriptor createDescriptor(ScalarWebChannel channel, String acceptedItemType,
            String channelType) {
        return createDescriptor(channel, acceptedItemType, channelType, null, null);
    }

    /**
     * Creates the descriptor.
     *
     * @param channel the channel
     * @param acceptedItemType the accepted item type
     * @param channelType the channel type
     * @param label the label
     * @param description the description
     * @return the scalar web channel descriptor
     */
    protected ScalarWebChannelDescriptor createDescriptor(ScalarWebChannel channel, String acceptedItemType,
            String channelType, String label, String description) {
        return new ScalarWebChannelDescriptor(channel, acceptedItemType, channelType, label, description);
    }

    /**
     * Checks if is linked.
     *
     * @param name the name
     * @param other the other
     * @return true, if is linked
     */
    protected boolean isLinked(String name, String... other) {
        return isLinked(createChannel(name, other));
    }

    /**
     * Checks if is linked.
     *
     * @param channel the channel
     * @return true, if is linked
     */
    protected boolean isLinked(ScalarWebChannel channel) {
        return tracker.isLinked(channel);
    }

    /**
     * Checks if is id linked.
     *
     * @param ids the ids
     * @return true, if is id linked
     */
    protected boolean isIdLinked(String... ids) {
        return tracker.isIdLinked(ids);
    }

    /**
     * Gets the linked channels for id.
     *
     * @param ids the ids
     * @return the linked channels for id
     */
    protected List<ScalarWebChannel> getLinkedChannelsForId(String... ids) {
        return tracker.getLinkedChannelsForId(ids);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        // do nothing
    }
}