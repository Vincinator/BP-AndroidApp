package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.dynamicObstacleFragmentEditor;

import java.util.HashMap;
import java.util.Map;

import bp.common.model.Obstacle;

/**
 * The ObstacleViewModel stores model data of the Obstacle, that is used to display (model->view)
 * the Obstacle and to store the changes (view -> model).
 *
 * The Attributes are loaded on runtime.
 */
public class ObstacleViewModel {

    /**
     * The attributes mapped to the attributeName
     */
    public Map<String, ObstacleAttribute<?>> attributesMap = new HashMap<>();

    private Obstacle mObstacleData;

    public ObstacleViewModel(Map<String, ObstacleAttribute<?>> attributes, Obstacle obstacle) {
        attributesMap = attributes;
        mObstacleData = obstacle;
    }

    public Obstacle getObstacleData() {
        return mObstacleData;
    }

    public void setObstacleData(Obstacle mObstacleData) {
        this.mObstacleData = mObstacleData;
    }
}