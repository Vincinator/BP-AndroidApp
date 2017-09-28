package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem;

import okhttp3.Response;

/**
 * Created by vincent on 28.09.17.
 */

public class OverpassRoadDownloadEvent {

    private Response result;
    public OverpassRoadDownloadEvent(Response result) {
        this.result = result;
    }

    public Response getResult() {
        return result;
    }
}
