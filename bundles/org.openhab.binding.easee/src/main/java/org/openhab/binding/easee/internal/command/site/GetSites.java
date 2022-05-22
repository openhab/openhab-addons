package org.openhab.binding.easee.internal.command.site;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.easee.internal.command.AbstractCommand;
import org.openhab.binding.easee.internal.command.EaseeCommand;
import org.openhab.binding.easee.internal.handler.EaseeHandler;

/**
 * implements the get sites api call of the site.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GetSites extends AbstractCommand implements EaseeCommand {

    public GetSites(EaseeHandler handler) {
        // retry does not make much sense as it is a polling command, command should always succeed therefore update
        // handler on failure.
        super(handler, false, true);
    }

    @Override
    protected Request prepareRequest(@NonNull Request requestToPrepare) {
        requestToPrepare.method(HttpMethod.GET);
        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        String url = GET_SITES_URL;
        return url;
    }

    @Override
    protected String getChannelGroup() {
        return CHANNEL_GROUP_NONE;
    }
}
