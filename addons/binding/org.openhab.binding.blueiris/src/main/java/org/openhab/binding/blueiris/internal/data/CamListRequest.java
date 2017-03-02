package org.openhab.binding.blueiris.internal.data;

/**
 * cam list data from blue iris.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class CamListRequest extends BlueIrisCommandRequest<CamListReply> {
    public CamListRequest() {
        super(CamListReply.class, "camlist");
    }
}
