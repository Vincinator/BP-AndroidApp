package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.mapoperator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.R;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.NewRoadMarkerPlacedEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.ObstacleOverlayItemLongPressEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoadsHelperOverlayChangedEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoutingServerRoadDownloadEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener.DragObstacleListener;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener.PlaceStartOfRoadOnPolyline;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.DownloadObstaclesTask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.DownloadOverPassRoadsTask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.DownloadRoadTask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.apiContracts.RamplerOverpassAPI;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.apiContracts.RoutingServerAPI;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.DefaultNearestRoadsDirector;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.NearestRoadsOverlay;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.NearestRoadsOverlayBuilder;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.OsmParser;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.interfaces.IUserInteractionWithMap;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.CustomPolyline;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ParcedOverpassRoad;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.RoadDataSingleton;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.MapEditorFragment;

import org.greenrobot.eventbus.EventBus;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import bp.common.model.ways.Node;
import bp.common.model.ways.Way;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by vincent on 8/16/17.
 */
public class RoadEditorOperator implements IUserInteractionWithMap {

    private List<GeoPoint> roadEndPoints = new ArrayList<>();
    private List<ParcedOverpassRoad> RoadList = new ArrayList<>();
    private List<Polyline> currentRoadCapture = new ArrayList<>();

    // Longpress auf die Map
    @Override
    public boolean longPressHelper(GeoPoint p, Activity context, MapEditorFragment mapEditorFragment) {
        ParcedOverpassRoad newStreet = new ParcedOverpassRoad();
        if (RoadList.size() != 0) {
            newStreet.id = RoadList.get(RoadList.size() - 1).id + 1;
        } else {
            newStreet.id = 0;
        }

        newStreet.name = "Street: " + newStreet.id;

        RoadList.add(newStreet);

        DefaultNearestRoadsDirector roadsDirector = new DefaultNearestRoadsDirector(new NearestRoadsOverlayBuilder());
        mapEditorFragment.roadsOverlay = roadsDirector.construct(p);
        mapEditorFragment.placeNewObstacleOverlay.removeAllItems();


        DownloadOverPassRoadsTask dt = new DownloadOverPassRoadsTask(context);
        dt.execute(mapEditorFragment.roadsOverlay.center, mapEditorFragment.roadsOverlay.radius);

        //Downloads all custom roads.
        DownloadRoadTask.downloadroad();


        int i = 0;

        if (RoadList.size() != 0) {
            for (ParcedOverpassRoad road : RoadList) {
                for (Polyline polyline : road.polylines) {
                    polyline.setColor(Color.BLACK);
                    mapEditorFragment.map.getOverlayManager().add(polyline);
                }
            }
            //mapEditorFragment.map.invalidate();
            currentRoadCapture.clear();
        }
        
        return true;
    }


    // Single Tap auf die Map
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint, Activity context, final MapEditorFragment mapEditorFragment) {
        if (RoadList.size() == 0) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
            builder1.setTitle("No existing road!");
            builder1.setMessage("Please verify that you first do a LONGPRESS to create a road \n\n" +
                    "Single tap only works if a road exists, adding further nodes.");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });


            AlertDialog alert11 = builder1.create();
            alert11.show();


            return false;
        }

        List<GeoPoint> geoPointsForRoadList = new ArrayList<GeoPoint>();
        ArrayList<Overlay> xx = RoadDataSingleton.getInstance().currentOverlayItems;

        // Workaround: skip when initial polyline and marker are not set
        if(xx.size() < 4){
            return false;
        }

        ParcedOverpassRoad road = RoadList.get(RoadList.size() - 1);

        road.polylines.add((Polyline) xx.get(xx.size() - 1));
        road.polylines.add((Polyline) xx.get(xx.size() - 3));

        Marker x = (Marker) xx.get(xx.size() - 4);
        geoPointsForRoadList.add(x.getPosition());
        x = (Marker) xx.get(xx.size() - 2);
        geoPointsForRoadList.add(x.getPosition());
        road.setRoadList(geoPointsForRoadList);


        if (road.getRoadPoints().size() > 0) {

            List<GeoPoint> roadEndPointsCrob = new ArrayList<>();
            Polyline streetLine = new Polyline();

            road.setRoadPoints(geoPoint);

            roadEndPointsCrob.add(road.getRoadPoints().get(road.getRoadPoints().size() - 2));
            roadEndPointsCrob.add(geoPoint);
            streetLine = setUPPoly(streetLine, mapEditorFragment, roadEndPointsCrob);

            Marker end = new Marker(mapEditorFragment.map);
            end.setPosition(geoPoint);
            end.setTitle("endPunkt");
            end.setDraggable(true);
            end.isDraggable();
            end.setOnMarkerDragListener(new DragObstacleListener(road, mapEditorFragment, roadEndPoints, this));

            Overlay ov = RoadDataSingleton.getInstance().currentOverlayItems.get(RoadDataSingleton.getInstance().currentOverlayItems.size() -2);
            Marker mark = (Marker)ov;
            mark.setDraggable(false);

            end.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            addMapOverlay(end, streetLine);
            EventBus.getDefault().post(new NewRoadMarkerPlacedEvent());

            return true;
        } else {
            return false;
        }

    }

    public void addMapOverlay(Marker marker, Polyline polyline) {

        RoadDataSingleton.getInstance().currentOverlayItems.add(marker);
        RoadDataSingleton.getInstance().currentOverlayItems.add(polyline);
        // mapEditorFragment.map.invalidate();
    }

    public Polyline setUPPoly(Polyline streetLine, MapEditorFragment mapEditorFragment, List<GeoPoint> list) {

        streetLine.setTitle("Text param");
        streetLine.setWidth(10f);
        streetLine.setColor(Color.BLUE);
        streetLine.setPoints(list);
        streetLine.setGeodesic(true);
        streetLine.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, mapEditorFragment.map));

        currentRoadCapture.add(streetLine);

        return streetLine;
    }

}
