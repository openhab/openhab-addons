package org.openhab.binding.siemenshvac.internal.network;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Response.ContentListener;
import org.eclipse.jetty.client.api.Response.FailureListener;
import org.eclipse.jetty.client.api.Response.SuccessListener;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataRegistryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class SiemensHvacRequestListener extends BufferingResponseListener
        implements SuccessListener, FailureListener, ContentListener, CompleteListener {

    private static final Logger logger = LoggerFactory.getLogger(SiemensHvacMetadataRegistryImpl.class);
    private SiemensHvacConnector hvacConnector;

    /**
     * Callback to execute on complete response
     */
    private final SiemensHvacCallback callback;

    /**
     * Constructor
     *
     * @param callback Callback which execute method has to be called.
     */
    public SiemensHvacRequestListener(SiemensHvacCallback callback, SiemensHvacConnector hvacConnector) {
        this.callback = callback;
        this.hvacConnector = hvacConnector;
    }

    @Override
    public void onSuccess(@NonNullByDefault({}) Response response) {
        // logger.debug("{} response: {}", response.getRequest().getURI(), response.getStatus());
    }

    @Override
    public void onFailure(@NonNullByDefault({}) Response response, @NonNullByDefault({}) Throwable failure) {
        logger.debug("response failed: {}  {}", response.getRequest().getURI(), failure.getLocalizedMessage(), failure);
    }

    @Override
    public void onComplete(@NonNullByDefault({}) Result result) {
        hvacConnector.onComplete(result.getRequest());

        try {
            String content = getContentAsString();
            logger.trace("response complete: {}", content);

            if (result.getResponse().getStatus() != 200) {
                logger.debug("bad gateway !!!");
                return;
            }

            if (content != null) {
                JsonObject resultObj = null;
                try {
                    Gson gson = SiemensHvacConnectorImpl.getGson();
                    resultObj = gson.fromJson(content, JsonObject.class);
                } catch (Exception ex) {
                    logger.debug("error:" + ex.toString());
                }

                if (resultObj.has("Result")) {
                    JsonObject subResultObj = resultObj.getAsJsonObject("Result");

                    if (subResultObj.has("Success")) {
                        boolean resultVal = subResultObj.get("Success").getAsBoolean();

                        if (resultVal) {

                            callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(), resultObj);
                            return;
                        } else {
                            logger.debug("error : " + subResultObj);
                        }
                    } else {
                        logger.debug("error");
                    }

                } else {
                    logger.debug("error");
                }

                return;
            }

            callback.execute(result.getRequest().getURI(), result.getResponse().getStatus(), content);
        } catch (Exception ex) {
            logger.debug("error");
        }
    }

}
