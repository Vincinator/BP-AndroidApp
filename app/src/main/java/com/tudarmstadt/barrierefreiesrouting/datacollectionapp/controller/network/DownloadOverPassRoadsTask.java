package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network;

/**
 * Created by vincent on 28.09.17.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.OverpassRoadDownloadEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoutingServerRoadDownloadEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.apiContracts.RamplerOverpassAPI;

import org.greenrobot.eventbus.EventBus;
import org.osmdroid.util.GeoPoint;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A Asynctask Class to get the osm data from a certain area while longpressed
 * This is used to find the nearest roads to a chosen point on the map
 */
public class DownloadOverPassRoadsTask extends AsyncTask<Object, Object, Response> {
    ProgressDialog progressDialog;

    public DownloadOverPassRoadsTask(Activity activity) {

        progressDialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog.setMessage("Lade Straßen in der nähe..");
        this.progressDialog.show();
    }

    @Override
    protected Response doInBackground(Object... params) {

        GeoPoint p = (GeoPoint) params[0];
        int radius = (int) params[1];
        DownloadObstaclesTask task = new DownloadObstaclesTask();
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), RamplerOverpassAPI.getNearestHighwaysPayload(p, radius));

        Request request = new Request.Builder()
                .url(RamplerOverpassAPI.baseURL + RamplerOverpassAPI.stairsResource)
                .method("POST", body)
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
    protected void onPostExecute(Response result) {
        super.onPostExecute(result);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        EventBus.getDefault().post(new OverpassRoadDownloadEvent(result));

        //processRoads(result);

    }
}