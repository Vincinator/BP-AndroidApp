package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.dynamicObstacleFragmentEditor.ObstacleViewModel;

import org.osmdroid.util.GeoPoint;

import bp.common.model.obstacles.Obstacle;
import bp.common.model.obstacles.Stairs;


/**
 * This Singleton holds the current State of the Obstacle that is or will be edited.
 */
public class ObstacleDataSingleton {

    private static volatile ObstacleDataSingleton instance = null;
    public boolean editorIsSyncedWithSelection = false;
    public GeoPoint currentStartingPositionOfSetObstacle = null;
    public GeoPoint currentEndPositionOfSetObstacle = null;
    public boolean isDoubleNodeObstacle = false;
    public boolean obstacleDataCollectionCompleted = false;
    private Obstacle mObstacle;
    private ObstacleViewModel mObstacleViewModel;

    /*  ##########################################################
        #Attribute needed for Export Tools
        ##########################################################*/
    private long id_way;
    private long id_firstnode;
    private long id_lastnode;

    /*  ##########################################################
        #Attribute needed for Export Tools
        ##########################################################*/

    private ObstacleDataSingleton() {
    }

    public static ObstacleDataSingleton getInstance() {
        if (instance == null) {
            synchronized (ObstacleDataSingleton.class) {
                if (instance == null) {
                    instance = new ObstacleDataSingleton();
                    instance.setObstacle(new Stairs());
                }
            }
        }
        return instance;
    }

    public Obstacle getObstacle() {
        return mObstacle;
    }

    public void setObstacle(Obstacle mObstacle) {
        this.mObstacle = mObstacle;
    }

    public ObstacleViewModel getmObstacleViewModel() {
        return mObstacleViewModel;
    }

    public void setObstacleViewModel(ObstacleViewModel mObstacleViewModel) {
        this.mObstacleViewModel = mObstacleViewModel;
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
            instance.getObstacle().setId_way(id_way);
            instance.getObstacle().setId_firstnode(id_firstnode);
            instance.getObstacle().setId_lastnode(id_lastnode);
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

    /*  ##########################################################
        #Functions needed for Export Tools
        ##########################################################*/
}
