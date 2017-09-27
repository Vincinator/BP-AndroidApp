package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.R;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.PostStreetToServerTask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ObstacleDataSingleton;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.RoadDataSingleton;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.activities.BrowseMapActivity;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.activities.PlaceObstacleActivity;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import bp.common.model.ways.Node;
import bp.common.model.ways.Way;

import static com.tudarmstadt.barrierefreiesrouting.datacollectionapp.R.id.userInputDialog;

/**
 * Created by vincent on 26.09.17.
 */

public class ActionButtonClickListener implements View.OnClickListener {

    private ArrayList<Node> nodeList = new ArrayList<Node>();


    @Override
    public void onClick(final View view) {

        if(!((BrowseMapActivity) view.getContext()).roadEditMode) {
            Intent intent = new Intent(view.getContext(), PlaceObstacleActivity.class);
            view.getContext().startActivity(intent);
        } else{
            BrowseMapActivity browseMapActivity = (BrowseMapActivity) view.getContext();
            nodeList.clear();
            List<GeoPoint> geop = new ArrayList<GeoPoint>();
            List<Overlay> xx = browseMapActivity.mapEditorFragment.map.getOverlays();
            for (int i = xx.size() - 1; i > 0; i--) {
                if (Polyline.class.isInstance(xx.get(i)) || Marker.class.isInstance(xx.get(i))) {
                    if (Marker.class.isInstance(xx.get(i))) {
                        xx.get(i).isEnabled();
                        geop.add(((Marker) xx.get(i)).getPosition());
                    }
                } else {
                    break;
                }
            }
            for (GeoPoint gp : geop) {
                nodeList.add(new Node(gp.getLatitude(), gp.getLongitude()));
            }
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(view.getContext());
            View mView = layoutInflaterAndroid.inflate(R.layout.activity_place_road, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(view.getContext());
            alertDialogBuilderUserInput.setView(mView);
            final EditText userInputDialogEditText = (EditText) mView.findViewById(userInputDialog);
            alertDialogBuilderUserInput
                    .setCancelable(false)
                    .setPositiveButton("An Server Senden ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogBox, int id) {
                            String result = userInputDialogEditText.getText().toString();
                            Way way = new Way(result, nodeList);

                            RoadDataSingleton.getInstance().setWAY(way);

                            //RoadDataSingleton.getInstance().setWAY(convertAttributeMapToObstacle(ObstacleDataSingleton.getInstance().getmObstacleViewModel()));
                            // wait for the Obstacle instance to be updated, then save 3 Ids into that Obstacle Instance before upload to the server
                            RoadDataSingleton.getInstance().saveThreeIdAttributes();

                            long xx = RoadDataSingleton.getInstance().getId_firstWAY();
                            long xx1 = RoadDataSingleton.getInstance().getId_secondWAY();
                            PostStreetToServerTask.PostStreet(RoadDataSingleton.getInstance().getWay());

                            // TODO: place this in the success of the server message (?) and update the BrowseMapActivity manually
                            ObstacleDataSingleton.getInstance().obstacleDataCollectionCompleted = true;

                            Toast.makeText(view.getContext(), R.string.Way_saved, Toast.LENGTH_SHORT).show();

                        }
                    })

                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    dialogBox.cancel();
                                }
                            });

            AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
            alertDialogAndroid.show();

            //String ret = result.getText().toString() ;
        }
    }
}
