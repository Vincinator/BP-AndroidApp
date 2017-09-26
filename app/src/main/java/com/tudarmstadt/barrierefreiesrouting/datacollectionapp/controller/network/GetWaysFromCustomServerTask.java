package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoadsHelperOverlayChangedEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener.PlaceStartOfRoadOnPolylineListener;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.apiContracts.RamplerOverpassAPI;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.NearestRoadsOverlay;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ParcedOverpassRoad;

import org.greenrobot.eventbus.EventBus;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bp.common.model.ways.Node;
import bp.common.model.ways.Way;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by vincent on 27.09.17.
 */

public class GetWaysFromCustomServerTask extends AsyncTask<Object, Object, Response> {
    ProgressDialog progressDialog;
    NearestRoadsOverlay roadsOverlay;
    public GetWaysFromCustomServerTask(Activity activity, NearestRoadsOverlay roadsOverlay) {
        this.roadsOverlay = roadsOverlay;
        progressDialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog.setMessage("Lade Custom Straßen in der nähe..");
        this.progressDialog.show();
    }

    @Override
    protected Response doInBackground(Object... params) {

        GeoPoint p = (GeoPoint) params[0];
        int radius = (int) params[1];
        // DownloadObstaclesTask task = new DownloadObstaclesTask();
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), RamplerOverpassAPI.getNearestHighwaysPayload(p, radius));

        Request request = new Request.Builder()
                .url("https://routing.vincinator.de/api/barriers/ways/radius?lat1=" + p.getLatitude() + "&long1=" + p.getLongitude() + "&radius=" + radius)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (!response.isSuccessful()) {
            //TODO: handle unsuccessful server responses
        }


        return response;
    }

    @Override
    protected void onPostExecute(Response response) {
        super.onPostExecute(response);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        ArrayList<PlaceStartOfRoadOnPolylineListener> list = new ArrayList<>();
        if (response != null && response.isSuccessful()) {

            try {
                ArrayList<Polyline> polylines = new ArrayList<>();

                String ss = response.body().string();

                final ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


                final List<Way> wayList = mapper.readValue(ss, new TypeReference<List<Way>>() {
                });

                for (Way w : wayList) {
                    List<GeoPoint> node = new ArrayList<>();
                    ParcedOverpassRoad r = new ParcedOverpassRoad();

                    for (Node n : w.getNodes()) {
                        GeoPoint g = new GeoPoint(n.getLatitude(), n.getLongitude());
                        node.add(g);

                    }
                    r.setROADList(node);
                    roadsOverlay.nearestRoads.add(r);
                }

                EventBus.getDefault().post(new RoadsHelperOverlayChangedEvent(polylines));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}