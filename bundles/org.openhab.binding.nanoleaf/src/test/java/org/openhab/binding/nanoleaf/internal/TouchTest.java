package org.openhab.binding.nanoleaf.internal;

import com.google.gson.Gson;
import org.junit.Test;
import org.openhab.binding.nanoleaf.internal.model.TouchEvents;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TouchTest {

    private final Gson gson = new Gson();

    @Test
    public void testTheRightLayoutView() {
        String json = "{\"events\":[{\"panelId\":48111,\"gesture\":1}]}";
        final Gson gson = new Gson();
        TouchEvents touchEvents = gson.fromJson(json, TouchEvents.class);
        assertThat(touchEvents.getEvents().size(), is(1));
        assertThat(touchEvents.getEvents().get(0).getPanelId(),is("48111"));
        assertThat(touchEvents.getEvents().get(0).getGesture(),is(1));
    }
}
