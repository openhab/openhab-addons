package org.openhab.binding.myuplink.internal.command;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.myuplink.internal.handler.MyUplinkThingHandler;
import org.openhab.binding.myuplink.internal.model.ValidationException;

public abstract class AbstractPagingCommand extends AbstractCommand {

    public AbstractPagingCommand(MyUplinkThingHandler handler, RetryOnFailure retryOnFailure,
            ProcessFailureResponse processFailureResponse, JsonResultProcessor resultProcessor) {
        super(handler, retryOnFailure, processFailureResponse, resultProcessor);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) throws ValidationException {
        requestToPrepare.param(WEB_REQUEST_PARAM_PAGE_SIZE_KEY, String.valueOf(WEB_REQUEST_PARAM_PAGE_SIZE_VALUE));
        requestToPrepare.method(HttpMethod.GET);
        return requestToPrepare;
    }
}
