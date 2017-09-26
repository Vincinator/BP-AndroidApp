package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener;

import android.content.Intent;
import android.view.View;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.activities.BrowseMapActivity;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.activities.PlaceObstacleActivity;

import java.util.ArrayList;

import bp.common.model.ways.Node;

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

        }
    }
}
