package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener;

import android.app.Activity;
import android.content.Context;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.interfaces.IUserInteractionWithMap;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.MapEditorFragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

/**
 * Created by deniz on 26.08.17.
 */

public class PlaceStartOfRoadOnPolylineListener implements Polyline.OnClickListener, IUserInteractionWithMap {

    public ArrayList<PlaceStartOfRoadOnPolylineListener> pl = new ArrayList<>();
    public Context context;

    public PlaceStartOfRoadOnPolylineListener(Context context, ArrayList<PlaceStartOfRoadOnPolylineListener> pl) {
        this.context = context;
        this.pl = pl;
    }


    @Override
    public boolean onClick(Polyline polyline, MapView mapView, GeoPoint geoPoint) {

        return false;
    }




    @Override
    public boolean longPressHelper(GeoPoint p, Activity context, MapEditorFragment mapEditorFragment) {
        return false;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p, Activity context, MapEditorFragment mapEditorFragment) {

        return false;
    }




}
