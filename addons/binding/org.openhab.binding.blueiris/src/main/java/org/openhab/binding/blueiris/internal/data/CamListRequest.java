package org.openhab.binding.blueiris.internal.data;

import java.io.InputStreamReader;

import com.google.gson.Gson;

/**
 * cam list data from blue iris.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class CamListRequest extends BlueIrisCommandRequest {
    private CamListReply reply;

    public CamListRequest() {
        super("camlist");
    }

    public CamListReply getReply() {
        return reply;
    }

    /**
     * How to handle the reply, doing the deserialization and returning the right result.
     */
    @Override
    public CamListReply deserializeReply(InputStreamReader str, Gson gson) {
        CamListReply.Data[] stuff = new CamListReply.Data[0];
        stuff = gson.fromJson(str, stuff.getClass());
        this.reply = new CamListReply(stuff);
        return this.reply;
    }
}
