package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoadsHelperOverlayChangedEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener.PlaceObstacleOnPolygonListener;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.apiContracts.RamplerOverpassAPI;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.NearestRoadsOverlay;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.OsmParser;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.CustomPolyline;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ParcedOverpassRoad;

import org.greenrobot.eventbus.EventBus;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A Asynctask Class to get the osm data from a certain area while longpressed
 * This is used to find the nearest roads to a chosen point on the map
 */
public class GetWaysFromOverpassAPITask extends AsyncTask<Object, Object, Response> {
    ProgressDialog progressDialog;
    NearestRoadsOverlay roadsOverlay;
    public GetWaysFromOverpassAPITask(Activity activity, NearestRoadsOverlay roadsOverlay) {
        this.roadsOverlay = roadsOverlay;
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
    protected void onPostExecute(Response response) {
        super.onPostExecute(response);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (response != null && response.isSuccessful()) {

            try {
                ArrayList<Polyline> polylines = new ArrayList<>();

                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                OsmParser parser = new OsmParser();
                String ss = response.body().string();
                InputSource source = new InputSource(new StringReader(ss));

                saxParser.parse(source, parser);
                List<ParcedOverpassRoad> give = roadsOverlay.nearestRoads;

                roadsOverlay.nearestRoads = parser.getRoads();
                for (ParcedOverpassRoad r : give) {
                    roadsOverlay.nearestRoads.add(r);
                }

                roadsOverlay.nearestRoads = parser.getRoads();

                if (roadsOverlay.nearestRoads.isEmpty() || roadsOverlay.nearestRoads.getFirst().getRoadPoints().isEmpty())
                    return;

                for (ParcedOverpassRoad r : roadsOverlay.nearestRoads) {

                    CustomPolyline polyline = new CustomPolyline();
                    polyline.setRoad(r);
                    polyline.setPoints(r.getRoadPoints());
                    polyline.setColor(Color.BLACK);
                    polyline.setWidth(18);
                    // See onClick() method in this class.
                    polyline.setOnClickListener(new PlaceObstacleOnPolygonListener());
                    polylines.add(polyline);
                }

                EventBus.getDefault().post(new RoadsHelperOverlayChangedEvent(polylines));

            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}