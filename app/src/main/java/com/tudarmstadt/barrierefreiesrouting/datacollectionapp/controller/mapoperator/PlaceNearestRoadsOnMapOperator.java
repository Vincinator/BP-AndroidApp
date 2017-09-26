package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.mapoperator;

import android.app.Activity;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.DownloadRoadTask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.GetWaysFromCustomServerTask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.GetWaysFromOverpassAPITask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.DefaultNearestRoadsDirector;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.NearestRoadsOverlay;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.NearestRoadsOverlayBuilder;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.interfaces.IUserInteractionWithMap;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.MapEditorFragment;

import org.osmdroid.util.GeoPoint;

/**
 * This class does not implement an Operator Interface - Not beautiful..
 * This is just the onClick Listener for the overlay.
 * In fact, one can argue that we need both Operators at the same time..
 * Naming this class PlaceNearestRoadsOnMapOperator leads to a less cluttered file structure..
 * In addition, semantically this behaves like an operator for the user.
 * <p>
 * Previous Step: get nearest roads
 * This Step: A new Obstacle is positioned on the Overlay
 * Next Step: get Details for Obstacle
 */

public class PlaceNearestRoadsOnMapOperator implements IUserInteractionWithMap {

    private NearestRoadsOverlay roadsOverlay;

    public PlaceNearestRoadsOnMapOperator() {
    }

    @Override
    public boolean longPressHelper(GeoPoint p, Activity context, MapEditorFragment mapEditorFragment) {

        //Downloads all custom roads.
        DownloadRoadTask.downloadroad();

        DefaultNearestRoadsDirector roadsDirector = new DefaultNearestRoadsDirector(new NearestRoadsOverlayBuilder());
        roadsOverlay = roadsDirector.construct(p);

        // clear the new placed temp Marker Item
        mapEditorFragment.placeNewObstacleOverlay.removeAllItems();

        GetWaysFromOverpassAPITask waysFromOverpassTask = new GetWaysFromOverpassAPITask(context, roadsOverlay);
        GetWaysFromCustomServerTask waysFromCustomServerTask = new GetWaysFromCustomServerTask(context, roadsOverlay);
        waysFromCustomServerTask.execute(roadsOverlay.center, roadsOverlay.radius);
        waysFromOverpassTask.execute(roadsOverlay.center, roadsOverlay.radius);


        return true;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p, Activity context, MapEditorFragment mapEditorFragment) {
        return false;
    }



}
