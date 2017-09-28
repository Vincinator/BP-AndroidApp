package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for parsing the response from overpass api.
 * <p>
 * Used by OsmParser
 */
public class ParcedOverpassRoad implements Parcelable {

    public long id;
    /**
     * The name of the road to display in the details view of the road.
     */
    public String name = "has no name";
    public ArrayList<Polyline> polylines = new ArrayList<>();
    private ArrayList<GeoPoint> roadPoints = new ArrayList<GeoPoint>();
    /**
     * list of node instead of GeoPoint
     */
    private ArrayList<Node> roadNodes = new ArrayList<Node>();

    public ParcedOverpassRoad(){

    }
    protected ParcedOverpassRoad(Parcel in) {
        id = in.readLong();
        name = in.readString();
        roadPoints = in.createTypedArrayList(GeoPoint.CREATOR);
    }

    public static final Creator<ParcedOverpassRoad> CREATOR = new Creator<ParcedOverpassRoad>() {
        @Override
        public ParcedOverpassRoad createFromParcel(Parcel in) {
            return new ParcedOverpassRoad(in);
        }

        @Override
        public ParcedOverpassRoad[] newArray(int size) {
            return new ParcedOverpassRoad[size];
        }
    };

    public ArrayList<Node> getRoadNodes() {
        return roadNodes;
    }

    /**
     * All GeoPoints that form the ParcedOverpassRoad.
     */
    public void setRoadList(List<GeoPoint> list) {
        roadPoints = (ArrayList) list;
    }

    public ArrayList<GeoPoint> getRoadPoints() {
        return roadPoints;
    }

    public void setRoadPoints(GeoPoint point) {
        roadPoints.add(point);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeTypedList(roadPoints);
    }
}
