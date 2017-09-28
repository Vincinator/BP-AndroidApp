package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener;

import android.graphics.Color;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.mapoperator.RoadEditorOperator;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ParcedOverpassRoad;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.MapEditorFragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by deniz on 20.08.17.
 */

public class DragObstacleListener implements Marker.OnMarkerDragListener {
    List<GeoPoint> roadPoints = new ArrayList<>();
    MapEditorFragment mapEditorFragment;
    MapView mv;
    RoadEditorOperator roadEditorOperator;
    PlaceStartOfRoadOnPolyline ProadEditorOperator;
    ParcedOverpassRoad road;
    boolean switcher = false;
    boolean once = false;

    public DragObstacleListener(ParcedOverpassRoad road, MapEditorFragment mapEditorFragment, List<GeoPoint> roadPoints, RoadEditorOperator roadEditorOperator ) {
        this.mapEditorFragment = mapEditorFragment;
        this.roadPoints = roadPoints;
        this.roadEditorOperator = roadEditorOperator;
        this.road = road;
        this.switcher = false;
    }
    public DragObstacleListener(ParcedOverpassRoad road, MapView mv, List<GeoPoint> roadPoints, PlaceStartOfRoadOnPolyline ProadEditorOperator ) {
        this.mv = mv;
        this.roadPoints = roadPoints;
        this.ProadEditorOperator = ProadEditorOperator;
        this.road = road;
        this.switcher = true;
        this.once = true;
    }


    @Override
    public void onMarkerDrag(Marker marker) {
        marker.setPosition(new GeoPoint(marker.getPosition().getLatitude(), marker.getPosition().getLongitude()));


    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        GeoPoint geopoint = marker.getPosition();
        Polyline streetLine = new Polyline();
        List<GeoPoint> roadEndPointsCrob = new ArrayList<>();

        if(switcher){
            mv.getOverlays().remove(mv.getOverlays().size() - 1);

        }else{
            mapEditorFragment.map.getOverlays().remove(mapEditorFragment.map.getOverlays().size() - 1);
        }

        this.road.setRoadPoints(geopoint);
        this.roadPoints.add(geopoint);
        // roadEndPointsCrob.add(road.getRoadPoints().get(road.getRoadPoints().size()-3));
        if(switcher){
            roadEndPointsCrob.add(road.getRoadPoints().get(road.getRoadPoints().size() - 4));

        }else{
            roadEndPointsCrob.add(road.getRoadPoints().get(road.getRoadPoints().size() - 3));

        }


        roadEndPointsCrob.add(geopoint);

        if(switcher){
            road.getRoadPoints().remove(road.getRoadPoints().size() - 1);
            road.getRoadPoints().remove(road.getRoadPoints().size() - 1);
            road.getRoadPoints().remove(road.getRoadPoints().size() - 1);
        }else{
            road.getRoadPoints().remove(road.getRoadPoints().size() - 2);
        }


        if(switcher){
            if(once){
                once = false;
                road.setRoadPoints(geopoint);
                List<Overlay> xxxx = mv.getOverlays();
                Overlay ov = mv.getOverlays().get(mv.getOverlays().size()-2);
                Polyline poly = (Polyline)ov;
                poly.setColor(Color.TRANSPARENT);

            }else{
                road.setRoadPoints(geopoint);
            }

            streetLine = ProadEditorOperator.setupPolyline(streetLine, mv, roadEndPointsCrob);
            mv.getOverlayManager().add(streetLine);
            mv.invalidate();
        }else{
            streetLine = roadEditorOperator.setUPPoly(streetLine, mapEditorFragment, roadEndPointsCrob);
            mapEditorFragment.map.getOverlayManager().add(streetLine);
            mapEditorFragment.map.invalidate();
        }


    }

    @Override
    public void onMarkerDragStart(Marker marker) {


    }
}


