package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.mapoperator;

import android.app.Activity;
import android.graphics.Color;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.GetWaysFromCustomServerTask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.GetWaysFromOverpassAPITask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.DefaultNearestRoadsDirector;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.NearestRoadsOverlay;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.NearestRoadsOverlayBuilder;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.interfaces.IUserInteractionWithMap;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ParcedOverpassRoad;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.MapEditorFragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vincent on 8/16/17.
 */
public class RoadEditorOperator implements IUserInteractionWithMap {

    public NearestRoadsOverlay roadsOverlay;
    public List<ParcedOverpassRoad> RoadList = new ArrayList<>();
    public List<Polyline> currentRoadCapture = new ArrayList<>();
    public GetWaysFromOverpassAPITask task;
    public GetWaysFromCustomServerTask task2;

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
        roadsOverlay = roadsDirector.construct(p);
        mapEditorFragment.placeNewObstacleOverlay.removeAllItems();

        task2 = new GetWaysFromCustomServerTask(context,roadsOverlay);
        task2.execute(roadsOverlay.center, roadsOverlay.radius);

        task = new GetWaysFromOverpassAPITask(context,roadsOverlay);
        task.execute(roadsOverlay.center, roadsOverlay.radius);

        if (RoadList.size() != 0) {
            for (ParcedOverpassRoad road : RoadList) {
                for (Polyline polyline : road.polylines) {
                    polyline.setColor(Color.BLACK);
                    mapEditorFragment.map.getOverlayManager().add(polyline);
                }
            }
            currentRoadCapture.clear();
        }

        return true;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p, Activity context, final MapEditorFragment mapEditorFragment) {

        return false;

    }


}
