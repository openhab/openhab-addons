package org.openhab.binding.blueiris.internal.control;

import org.openhab.binding.blueiris.internal.data.CamListReply;
import org.openhab.binding.blueiris.internal.data.LoginReply;

public interface ConnectionListener {

    void onLogin(LoginReply loginReply);

    void onCamList(CamListReply camListReply);

    void onFailedToLogin();

}
