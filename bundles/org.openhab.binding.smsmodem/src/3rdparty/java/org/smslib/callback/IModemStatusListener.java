package org.smslib.callback;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.smslib.Modem.Status;

/**
 * Implement this interface to get status change
 *
 * Extracted from SMSLib
 */
@NonNullByDefault
public interface IModemStatusListener {

    boolean processStatusCallback(Status oldStatus, Status newStatus);
}
