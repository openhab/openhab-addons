package org.smslib.callback;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IDeviceInformationListener} will receive informations
 * and statistics
 * Extracted from SMSLib
 */
@NonNullByDefault
public interface IDeviceInformationListener {

    void setManufacturer(String manufacturer);

    void setModel(String string);

    void setSwVersion(String swVersion);

    void setSerialNo(String serialNo);

    void setImsi(String imsi);

    void setRssi(String rssi);

    void setMode(String mode);

    public void setTotalSent(String totalSent);

    public void setTotalFailed(String totalFailed);

    public void setTotalReceived(String totalReceived);

    public void setTotalFailures(String totalFailure);
}
