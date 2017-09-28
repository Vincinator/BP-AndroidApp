package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.dynamicObstacleFragmentEditor.ObstacleViewModel;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

import bp.common.model.WayBlacklist;
import bp.common.model.obstacles.Obstacle;
import bp.common.model.ways.Way;

/**
 * Created by deniz on 23.09.17.
 */

public class RoadDataSingleton {


    private ArrayList<WayBlacklist> blacklistedRoads = new ArrayList<>();

    private static volatile RoadDataSingleton instance = null;
    private Way way;


    public ArrayList<Overlay> currentOverlayItems = new ArrayList<>();



    /*  ##########################################################
        #Attribute needed for Export Tools
        ##########################################################*/
    private long id_way;
    private long id_firstnode;
    private long id_lastnode;
    private long id_LASTfirstnode;
    private long id_LASTlastnode;
    private long id_firstWAY;
    private long id_secondWAY;

    /*  ##########################################################
        #Attribute needed for Export Tools
        ##########################################################*/

    private RoadDataSingleton() {
    }

    public static RoadDataSingleton getInstance() {
        if (instance == null) {
            synchronized (ObstacleDataSingleton.class) {
                if (instance == null) {
                    instance = new RoadDataSingleton();
                    instance.setWAY(new Way());
                }
            }
        }
        return instance;
    }

    public Way getWay() {
        return way;
    }

    public void setWAY(Way way) {
        this.way = way;
    }


    /*  ##########################################################
        #Function needed for Export Tools
        ##########################################################*/

    /**
     * set the 3 IDs values needed for ExportTool in the current Obstacle Object
     * before sending it to server
     *
     * @return true if successful, meaning the Obstacle is already created and exists
     */
    public boolean saveThreeIdAttributes() {
        if (instance != null) {
            instance.getWay().setId(id_way);
            instance.getWay().setOsmid_firstWay(id_firstWAY);
            instance.getWay().setOsmid_firstWayFirstNode(id_firstnode);
            instance.getWay().setOsmid_firstWaySecondNode(id_lastnode);
            instance.getWay().setOsmid_secondWay(id_secondWAY);
            instance.getWay().setOsmid_secondWayFirstNode(id_LASTfirstnode);
            instance.getWay().setOsmid_secondWaySecondNode(id_LASTlastnode);
            return true;
        } else return false;
    }

    public long getId_way() {
        return id_way;
    }

    public void setId_way(long id_way) {
        this.id_way = id_way;
    }

    public long getId_firstnode() {
        return id_firstnode;
    }

    public void setId_firstnode(long id_firstnode) {
        this.id_firstnode = id_firstnode;
    }

    public long getId_lastnode() {
        return id_lastnode;
    }

    public void setId_lastnode(long id_lastnode) {
        this.id_lastnode = id_lastnode;
    }

    public long getId_LASTfirstnode() {
        return id_LASTfirstnode;
    }

    public void setId_LASTfirstnode(long id_firstnode) {
        this.id_LASTfirstnode = id_firstnode;
    }

    public long getId_LASTlastnode() {
        return id_LASTlastnode;
    }

    public void setId_LASTlastnode(long id_lastnode) {
        this.id_LASTlastnode = id_lastnode;
    }

    public void setId_firstWAY(long id_firstWAY) {
        this.id_firstWAY = id_firstWAY;
    }
    public long getId_firstWAY() {
        return this.id_firstWAY;
    }
    public void setId_secondWAY(long id_secondWAY) {
        this.id_secondWAY = id_secondWAY;
    }
    
    public long getId_secondWAY() {
        return this.id_secondWAY;

    }
    public ArrayList<WayBlacklist> getBlacklistedRoads() {
        return blacklistedRoads;
    }

    public void setBlacklistedRoads(ArrayList<WayBlacklist> blacklistedRoads) {
        this.blacklistedRoads = blacklistedRoads;
    }

    /*  ##########################################################
        #Functions needed for Export Tools
        ##########################################################*/
}