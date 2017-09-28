package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.R;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoadPositionSelectedOnPolylineEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.interfaces.IUserInteractionWithMap;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.CustomPolyline;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ParcedOverpassRoad;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.RoadDataSingleton;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.MapEditorFragment;

import org.greenrobot.eventbus.EventBus;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by deniz on 26.08.17.
 */

public class PlaceStartOfRoadOnPolyline implements Polyline.OnClickListener, IUserInteractionWithMap {
    public List<GeoPoint> roadEndPoints = new ArrayList<>();
    public boolean last;
    public ParcedOverpassRoad newStreet;
    public ArrayList<PlaceStartOfRoadOnPolyline> pl = new ArrayList<>();
    public List<Marker> RoadMarker = new ArrayList<>();
    public List<ParcedOverpassRoad> RoadList = new ArrayList<>();
    public MapEditorFragment mapEditorFragment;
    public List<Polyline> currentRoadCapture = new ArrayList<>();

    public PlaceStartOfRoadOnPolyline() {
        pl.add(this);
    }


    @Override
    public boolean onClick(Polyline polyline, MapView mapView, GeoPoint geoPoint) {
        if (!last) {
            try {
                for (PlaceStartOfRoadOnPolyline pp : pl) {
                    pp.last = true;
                }

                Point projectedPoint = mapView.getProjection().toProjectedPixels(geoPoint.getLatitude(), geoPoint.getLongitude(), null);
                Point finalPoint = mapView.getProjection().toPixelsFromProjected(projectedPoint, null);

                // TODO: Bi Check
                if (polyline instanceof CustomPolyline) {
                    CustomPolyline cuspo = (CustomPolyline) polyline;
                    RoadDataSingleton.getInstance().setId_firstWAY(cuspo.getRoad().id);
                }
                // Send Event that an Obstacle Position has been set, and send the position on the line with the event.
                // Subscriber will be notified about this post, but only one specified method will be called  getClosestPointOnPolyLine(mapView,polyline, finalPoint))
                EventBus.getDefault().post(new RoadPositionSelectedOnPolylineEvent(getClosestPointOnPolyLine(mapView, polyline, finalPoint)));

            } catch (Exception e) {
                e.printStackTrace();
            }


            last = true;

            newStreet = new ParcedOverpassRoad();


            Polyline streetLine = new Polyline();
            roadEndPoints.add(new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude()));
            roadEndPoints.add(new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude() + 0.0002));
            //roadEndPoints.add(new GeoPoint(geoPoint.getLatitude() + 0.0002, geoPoint.getLongitude()));
            newStreet.setRoadList(roadEndPoints);

            streetLine = setUPPoly(streetLine, mapView, roadEndPoints);


            Marker startMarker = new Marker(mapView);
            startMarker.setPosition(roadEndPoints.get(0));
            startMarker.setTitle("Start point for creating new ParcedOverpassRoad");
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            RoadMarker.add(startMarker);

            Marker endM = new Marker(mapView);
            endM.setPosition(roadEndPoints.get(1));
            endM.setTitle("Start point for creating new ParcedOverpassRoad");
            endM.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            ParcedOverpassRoad road = new ParcedOverpassRoad();
            road.setRoadList(roadEndPoints);
            endM.setDraggable(true);
            endM.isDraggable();
            endM.setOnMarkerDragListener(new DragObstacleListener(road, mapView, roadEndPoints, this));


            RoadMarker.add(endM);

            addMapOverlay(startMarker, streetLine, mapView);
            addMapOverlay(endM, streetLine, mapView);


            RoadList.add(newStreet);

            Log.d("myTag", "This is my message");
            //roadEndPoints.clear();
            EventBus.getDefault().post(new RoadPositionSelectedOnPolylineEvent(geoPoint));


            return true;
        } else {

            try {

                Point projectedPoint = mapView.getProjection().toProjectedPixels(geoPoint.getLatitude(), geoPoint.getLongitude(), null);
                Point finalPoint = mapView.getProjection().toPixelsFromProjected(projectedPoint, null);

                // TODO: Bi Check
                if (polyline instanceof CustomPolyline) {
                    CustomPolyline cuspo = (CustomPolyline) polyline;
                    RoadDataSingleton.getInstance().setId_secondWAY(cuspo.getRoad().id);
                }
                // Send Event that an Obstacle Position has been set, and send the position on the line with the event.
                // Subscriber will be notified about this post, but only one specified method will be called  getClosestPointOnPolyLine(mapView,polyline, finalPoint))
                EventBus.getDefault().post(new RoadPositionSelectedOnPolylineEvent(getLASTClosestPointOnPolyLine(mapView, polyline, finalPoint)));

            } catch (Exception e) {
                e.printStackTrace();
            }
            newStreet = new ParcedOverpassRoad();
            roadEndPoints.add(new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude()));
            newStreet.setRoadList(roadEndPoints);
            RoadList.add(newStreet);

            List<GeoPoint> gp = new ArrayList<GeoPoint>();
            List<Overlay> xx = mapView.getOverlays();

            // Workaround: when initial polyline and marker for road editor is not set yet
            if(  !(xx.get(xx.size() - 1) instanceof Polyline) ||
                    !(xx.get(xx.size() - 3) instanceof Polyline) ||
                    !(xx.get(xx.size() - 2) instanceof Marker) ||
                    !(xx.get(xx.size() - 4) instanceof Marker)){
                return false;

            }

            ParcedOverpassRoad road = RoadList.get(RoadList.size() - 1);
            road.polylines.add((Polyline) xx.get(xx.size() - 1));
            road.polylines.add((Polyline) xx.get(xx.size() - 3));

            Marker x = (Marker) xx.get(xx.size() - 4);
            gp.add(x.getPosition());
            x = (Marker) xx.get(xx.size() - 2);
            gp.add(x.getPosition());
            road.setRoadList(gp);


            if (road.getRoadPoints().size() > 0) {

                List<GeoPoint> roadEndPointsCrob = new ArrayList<>();
                Polyline streetLine = new Polyline();


                road.setRoadPoints(geoPoint);


                roadEndPointsCrob.add(road.getRoadPoints().get(road.getRoadPoints().size() - 2));
                roadEndPointsCrob.add(geoPoint);
                streetLine = setUPPoly(streetLine, mapView, roadEndPointsCrob);

                Marker end = new Marker(mapView);
                end.setPosition(geoPoint);
                end.setTitle("endPunkt");


                end.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                RoadMarker.add(end);


                addMapOverlay(end, streetLine, mapView);


            }
            return true;
        }
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
                            RoadDataSingleton.getInstance().setId_firstnode(cuspo.getRoad().getRoadNodes().get(curIndex).id);
                            RoadDataSingleton.getInstance().setId_lastnode(cuspo.getRoad().getRoadNodes().get(curIndex + 1).id);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return candidate;
    }

    private GeoPoint getLASTClosestPointOnPolyLine(MapView mapView, Polyline polyline, Point point) {

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
                            RoadDataSingleton.getInstance().setId_LASTfirstnode(cuspo.getRoad().getRoadNodes().get(curIndex).id);
                            RoadDataSingleton.getInstance().setId_LASTlastnode(cuspo.getRoad().getRoadNodes().get(curIndex + 1).id);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return candidate;
    }


    public void addMapOverlay(Marker marker, Polyline polyline, MapView map) {
        map.getOverlays().add(marker);
        map.getOverlayManager().add(polyline);
        map.invalidate();
    }

    public Polyline setUPPoly(Polyline streetLine, MapView map, List<GeoPoint> list) {

        streetLine.setTitle("Text param");
        streetLine.setWidth(10f);
        streetLine.setColor(Color.BLUE);
        streetLine.setPoints(list);
        streetLine.setGeodesic(true);
        streetLine.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, map));

        currentRoadCapture.add(streetLine);

        return streetLine;
    }


    @Override
    public boolean longPressHelper(GeoPoint p, Activity context, MapEditorFragment mapEditorFragment) {
        return false;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p, Activity context, MapEditorFragment mapEditorFragment) {
        if (roadEndPoints.size() > 0) {

            List<GeoPoint> roadEndPointsCrob = new ArrayList<>();
            Polyline streetLine = new Polyline(context);


            roadEndPoints.add(p);
            RoadList.get(RoadList.size() - 1).setRoadList(roadEndPoints);

            roadEndPointsCrob.add(roadEndPoints.get(roadEndPoints.size() - 2));
            roadEndPointsCrob.add(p);
            streetLine = setUPPoly(streetLine, mapEditorFragment.map, roadEndPointsCrob);

            Marker end = new Marker(mapEditorFragment.map);
            end.setPosition(p);
            end.setTitle("endPunkt");
            end.setDraggable(true);
            end.isDraggable();
            // end.setOnMarkerDragListener(new DragObstacleListener(mapEditorFragment,roadEndPoints,this,context));


            end.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            RoadMarker.add(end);


            addMapOverlay(end, streetLine, mapEditorFragment.map);


            return true;
        } else {
            return false;
        }
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

    public boolean isProjectedPointOnLineSegment(Point v1, Point v2, Point p, double tolerance) {
        Point e1 = new Point(v2.x - v1.x, v2.y - v1.y);
        int recAreaToTest = dot(e1, e1);

        Point e2 = new Point(p.x - v1.x, p.y - v1.y);
        double value = dot(e1, e2);
        return ((value > 0 + tolerance || value > 0 - tolerance) && (value < recAreaToTest - tolerance || value < recAreaToTest + tolerance));
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
