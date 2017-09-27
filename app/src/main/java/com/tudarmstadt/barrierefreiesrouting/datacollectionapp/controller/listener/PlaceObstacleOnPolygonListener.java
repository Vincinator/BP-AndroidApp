package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener;

import android.graphics.Point;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.ObstaclePositionSelectedOnPolylineEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.CustomPolyline;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ObstacleDataSingleton;

import org.greenrobot.eventbus.EventBus;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

/**
 * Places an Obstacle on the Polygon that this class listens for Click Events.
 */
public class PlaceObstacleOnPolygonListener implements Polyline.OnClickListener {


    /**
     * Callback function gets called after the user has clicked on the polyline that this
     * Listener is set to listen to.
     *
     * @param polyline
     * @param mapView
     * @param eventPos
     * @return
     */
    @Override
    public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
        //ObstacleDataSingleton.getInstance().currentStartingPositionOfSetObstacle = null;
        try {

            Point projectedPoint = mapView.getProjection().toProjectedPixels(eventPos.getLatitude(), eventPos.getLongitude(), null);
            Point finalPoint = mapView.getProjection().toPixelsFromProjected(projectedPoint, null);

            // TODO: Bi Check
            if (polyline instanceof CustomPolyline) {
                CustomPolyline cuspo = (CustomPolyline) polyline;
                ObstacleDataSingleton.getInstance().setId_way(cuspo.getRoad().id);
            }
            // Send Event that an Obstacle Position has been set, and send the position on the line with the event.
            // Subscriber will be notified about this post, but only one specified method will be called
            EventBus.getDefault().post(new ObstaclePositionSelectedOnPolylineEvent(getClosestPointOnPolyLine(mapView, polyline, finalPoint), (CustomPolyline) polyline));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * @param mapView
     * @param polyline
     * @param point
     * @return
     */
    private GeoPoint getClosestPointOnPolyLine(MapView mapView, Polyline polyline, Point point) {

        if (polyline.getPoints().size() < 2)
            return null;

        GeoPoint start = null;
        GeoPoint end = null;
        GeoPoint candidate = null;
        Point candidatePoint = null;

        /**
         * IMPORTANT HINT FOR BI:
         * The nodes are ordered in the polyline.
         * this means, that we can iterate through the polyline and get the nearest line
         * of the polyline to the placed Point.
         *
         */
        for (int curIndex = 0; curIndex + 1 < polyline.getPoints().size(); curIndex++) {
            start = polyline.getPoints().get(curIndex);
            end = polyline.getPoints().get(curIndex + 1);

            try {
                Point projectedPointStart = mapView.getProjection().toProjectedPixels(start.getLatitude(), start.getLongitude(), null);
                Point pointA = mapView.getProjection().toPixelsFromProjected(projectedPointStart, null);

                Point projectedPointEnd = mapView.getProjection().toProjectedPixels(end.getLatitude(), end.getLongitude(), null);
                Point pointB = mapView.getProjection().toPixelsFromProjected(projectedPointEnd, null);

                if (isProjectedPointOnLineSegment(pointA, pointB, point)) {

                    Point closestPointOnLine = getClosestPointOnLine(pointA, pointB, point);

                    // Is there a candidate?
                    // Is the new candidatePoint nearer than the old one?
                    // candidatePoints are ALWAYS on some line.
                    // point is the user placed position, which is probably not on the line and for
                    // which we want to search the position on the line.s
                    if (candidate == null || (getDistance(candidatePoint, point) > getDistance(closestPointOnLine, point))) {
                        candidatePoint = closestPointOnLine;
                        candidate = (GeoPoint) mapView.getProjection().fromPixels(closestPointOnLine.x, closestPointOnLine.y);
                        // save the ID of the 2 nodes surrounding the current Obstacle node in ObstacleDataSingleton
                        // TODO: From Bi, maybe something incorect, check later
                        // check if polyline is really from type Custom
                        if (polyline instanceof CustomPolyline) {
                            CustomPolyline cuspo = (CustomPolyline) polyline;
                            ObstacleDataSingleton.getInstance().setId_firstnode(cuspo.getRoad().getRoadNodes().get(curIndex).id);
                            ObstacleDataSingleton.getInstance().setId_lastnode(cuspo.getRoad().getRoadNodes().get(curIndex + 1).id);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return candidate;
    }

    public double getDistance(Point a, Point b) {
        double temp = Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2);
        return Math.sqrt(temp);

    }

    public boolean isProjectedPointOnLineSegment(Point v1, Point v2, Point p) {
        Point e1 = new Point(v2.x - v1.x, v2.y - v1.y);
        int recAreaToTest = dot(e1, e1);

        Point e2 = new Point(p.x - v1.x, p.y - v1.y);
        double value = dot(e1, e2);
        return (value > 0 && value < recAreaToTest);
    }

    /**
     * Search closest point between the line from pointA to pointB and pointC
     */
    private Point getClosestPointOnLine(Point v1, Point v2, Point p) {

        // get dot product of e1, e2
        Point e1 = new Point(v2.x - v1.x, v2.y - v1.y);
        Point e2 = new Point(p.x - v1.x, p.y - v1.y);
        double valDp = dot(e1, e2);

        double lenLineE1 = Math.sqrt(e1.x * e1.x + e1.y * e1.y);
        double lenLineE2 = Math.sqrt(e2.x * e2.x + e2.y * e2.y);
        double cos = valDp / (lenLineE1 * lenLineE2);

        double projLenOfLine = cos * lenLineE2;
        Point p2 = new Point((int) (v1.x + (projLenOfLine * e1.x) / lenLineE1),
                (int) (v1.y + (projLenOfLine * e1.y) / lenLineE1));
        return p2;
    }

    private int dot(Point v1, Point v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }
}
