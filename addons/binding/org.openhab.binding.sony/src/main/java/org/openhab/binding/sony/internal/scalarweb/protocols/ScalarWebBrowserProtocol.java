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
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.openhab.binding.sony.internal.scalarweb.models.api.BrowserControl;
import org.openhab.binding.sony.internal.scalarweb.models.api.TextUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebBrowserProtocol.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
class ScalarWebBrowserProtocol<T extends ThingCallback<ScalarWebChannel>> extends AbstractScalarWebProtocol<T> {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebSystemProtocol.class);

    /** The Constant BROWSERCONTROL. */
    private final static String BROWSERCONTROL = "browsercontrol";

    /** The Constant TEXTURL. */
    private final static String TEXTURL = "texturl";

    /** The Constant TEXTTITLE. */
    private final static String TEXTTITLE = "texttitle";

    /** The Constant TEXTTYPE. */
    private final static String TEXTTYPE = "texttype";

    /** The Constant TEXTFAVICON. */
    private final static String TEXTFAVICON = "textfavicon";

    /**
     * Instantiates a new scalar web browser protocol.
     *
     * @param tracker the tracker
     * @param state the state
     * @param service the service
     * @param callback the callback
     */
    ScalarWebBrowserProtocol(ScalarWebChannelTracker tracker, ScalarWebState state, ScalarWebService service,
            T callback) {
        super(tracker, state, service, callback);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#getChannelDescriptors()
     */
    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors() {

        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        if (service.getMethod(ScalarWebMethod.ActivateBrowserControl) != null) {
            descriptors.add(createDescriptor(createChannel(BROWSERCONTROL), "String", "scalarbrowseractivate"));
        }

        if (service.getMethod(ScalarWebMethod.GetTextUrl) != null) {
            descriptors.add(createDescriptor(createChannel(TEXTURL), "String", "scalarbrowsertexturl"));
            descriptors.add(createDescriptor(createChannel(TEXTTITLE), "String", "scalarbrowsertexttitle"));
            descriptors.add(createDescriptor(createChannel(TEXTTYPE), "String", "scalarbrowsertexttype"));
            descriptors.add(createDescriptor(createChannel(TEXTFAVICON), "String", "scalarbrowsertextfavicon"));
        }

        return descriptors;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshState()
     */
    @Override
    public void refreshState() {
        if (isLinked(BROWSERCONTROL)) {
            refreshBrowserControl();
        }

        if (isLinked(TEXTURL, TEXTTITLE, TEXTTYPE, TEXTFAVICON)) {
            refreshTextUrl();
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel)
     */
    @Override
    public void refreshChannel(ScalarWebChannel channel) {
        switch (channel.getId()) {
            case BROWSERCONTROL:
                refreshBrowserControl();
                break;

            case TEXTURL:
            case TEXTTITLE:
            case TEXTTYPE:
            case TEXTFAVICON:
                refreshTextUrl();
                break;

            default:
                logger.debug("Unknown refresh channel: {}", channel);
                break;
        }

    }

    /**
     * Refresh browser control.
     */
    private void refreshBrowserControl() {
        callback.stateChanged(createChannel(BROWSERCONTROL), StringType.EMPTY);
    }

    /**
     * Refresh text url.
     */
    private void refreshTextUrl() {
        try {
            final TextUrl url = execute(ScalarWebMethod.GetTextUrl).as(TextUrl.class);

            callback.stateChanged(createChannel(TEXTURL), new StringType(url.getUrl()));
            callback.stateChanged(createChannel(TEXTTITLE), new StringType(url.getTitle()));
            callback.stateChanged(createChannel(TEXTTYPE), new StringType(url.getType()));
            callback.stateChanged(createChannel(TEXTFAVICON), new StringType(url.getFavicon()));
        } catch (IOException e) {
            // do nothing
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#setChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel, org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void setChannel(ScalarWebChannel channel, Command command) {
        switch (channel.getId()) {
            case BROWSERCONTROL:
                if (command instanceof StringType) {
                    setActivateBrowserControl(command.toString());
                } else {
                    logger.debug("BROWSERCONTROL mode command not an StringType: {}", command);
                }

                break;

            case TEXTURL:
                if (command instanceof StringType) {
                    setTextUrl(command.toString());
                } else {
                    logger.debug("TEXTURL command not an StringType: {}", command);
                }

                break;

            default:
                logger.debug("Unhandled channel command: {} - {}", channel, command);
                break;
        }
    }

    /**
     * Sets the activate browser control.
     *
     * @param control the new activate browser control
     */
    private void setActivateBrowserControl(String control) {
        handleExecute(ScalarWebMethod.ActivateBrowserControl,
                new BrowserControl(StringUtils.isEmpty(control) ? null : control));
    }

    /**
     * Sets the text url.
     *
     * @param url the new text url
     */
    private void setTextUrl(String url) {
        handleExecute(ScalarWebMethod.SetTextUrl, new TextUrl(url));
    }
}
