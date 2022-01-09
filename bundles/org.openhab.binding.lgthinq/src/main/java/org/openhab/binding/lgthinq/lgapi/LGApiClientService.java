package org.openhab.binding.lgthinq.lgapi;

import org.openhab.binding.lgthinq.errors.LGApiException;
import org.openhab.binding.lgthinq.lgapi.model.LGDevice;

import java.util.List;

public interface LGApiClientService {

    public List<LGDevice> listAccountDevices() throws LGApiException;

}
