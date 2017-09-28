package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.R;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener.SelectObstacleForDetailsViewListener;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder.NearestRoadsOverlay;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.MapEditorState;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ObstacleOverlayItem;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.activities.BrowseMapActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

/**
 * This Fragment holds the MapView and is responsible for initializing Map Overlays.
 */
public class MapEditorFragment extends Fragment implements MapEventsReceiver {

    public MyLocationNewOverlay mLocationOverlay;
    public MapView map;
    public ItemizedOverlayWithFocus<ObstacleOverlayItem> obstacleOverlay;
    public ItemizedOverlayWithFocus<OverlayItem> placeNewObstacleOverlay;
    public ItemizedOverlayWithFocus<OverlayItem> placeRoadEditMarkerOverlay;
    public NearestRoadsOverlay roadsOverlay;



    private MapEventsOverlay evOverlay;
    /**
     * All existing Obstacles items
     */
    private ArrayList<ObstacleOverlayItem> obstacleItems = new ArrayList<ObstacleOverlayItem>();

    /**
     * The temporary Item (usually just one item) that is placed on a polyline.
     */
    private ArrayList<OverlayItem> tempObstacleItems = new ArrayList<OverlayItem>();


    private OnFragmentInteractionListener mListener;

    /**
     * This is a Singleton Class and holds the active MapEditor State, which includes the current
     * selected Map Editor Operator
     */
    private MapEditorState mapStateHandler;

    // Leerer Constructor wird benötigt
    public MapEditorFragment() {
    }

    public static MapEditorFragment newInstance(BrowseMapActivity browseMapActivity) {
        MapEditorFragment fragment = new MapEditorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.mapStateHandler = new MapEditorState(browseMapActivity, fragment);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_editor_fragment, container, false);

        Context context = getActivity().getApplicationContext();

        map = (MapView) v.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.setClickable(true);

        this.evOverlay = new MapEventsOverlay(this);
        map.getOverlays().add(evOverlay);
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
        this.mLocationOverlay.enableMyLocation();
        mLocationOverlay.setDrawAccuracyEnabled(false);
        map.getOverlays().add(this.mLocationOverlay);

        // Auf der Startposition wird die Map zentriert - 49.8728, 8.6512 => Luisenplatz ;)
        GeoPoint startPoint = new GeoPoint(49.8728, 8.6512);
        IMapController mapController = map.getController();
        mapController.setCenter(startPoint);
        map.setMaxZoomLevel(21);

        map.getController().setZoom(19);
        map.setTilesScaledToDpi(true);

        mLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                map.getController().animateTo(mLocationOverlay.getMyLocation());
            }
        });


        obstacleOverlay = new ItemizedOverlayWithFocus<>(getActivity(), obstacleItems,
                new SelectObstacleForDetailsViewListener());

        placeNewObstacleOverlay = new ItemizedOverlayWithFocus<>(getActivity(), tempObstacleItems,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int i, OverlayItem overlayItem) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int i, OverlayItem overlayItem) {
                        return false;
                    }
                });

        placeRoadEditMarkerOverlay = new ItemizedOverlayWithFocus<>(getActivity(), tempObstacleItems,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int i, OverlayItem overlayItem) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int i, OverlayItem overlayItem) {
                        return false;
                    }
                });

        obstacleOverlay.setFocusItemsOnTap(false);
        map.getOverlays().add(obstacleOverlay);
        map.getOverlays().add(placeNewObstacleOverlay);
        map.getOverlays().add(placeRoadEditMarkerOverlay);

        v.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return mapStateHandler.getActiveOperator().singleTapConfirmedHelper(p, getActivity(), this);
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return mapStateHandler.getActiveOperator().longPressHelper(p, getActivity(), this);
    }

    public MapEditorState getStateHandler() {
        return mapStateHandler;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
