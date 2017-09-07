package org.openhab.binding.nibeuplink.internal.command;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Response.ContentListener;
import org.eclipse.jetty.client.api.Response.FailureListener;
import org.eclipse.jetty.client.api.Response.SuccessListener;
import org.openhab.binding.nibeuplink.internal.connector.StatusUpdateListener;

/**
 * public interface for all commands
 *
 * @author afriese
 *
 */
public interface NibeUplinkCommand extends SuccessListener, FailureListener, ContentListener, CompleteListener {

    public static int MAX_RETRIES = 5;

    /**
     * this method is to be called by the UplinkWebinterface class
     *
     * @param asyncclient
     */
    public void performAction(HttpClient asyncclient);

    /**
     * get the current listener
     *
     * @return
     */
    public StatusUpdateListener getListener();

    /**
     * register a listener
     *
     * @param listener
     */
    public void setListener(StatusUpdateListener listener);

}
