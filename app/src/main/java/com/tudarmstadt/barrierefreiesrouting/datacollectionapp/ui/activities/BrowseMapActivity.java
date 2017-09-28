package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.R;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.BlacklistedRoadsTaskDownloadedEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.ObstacleOverlayItemSingleTapEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.ObstaclePositionSelectedOnPolylineEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoadPositionSelectedOnPolylineEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoadsHelperOverlayChangedEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoutingServerObstaclePostedEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoutingServerObstaclesDownloadedEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.RoutingServerRoadDownloadEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.eventsystem.StartEditObstacleEvent;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener.ActionButtonClickListener;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.listener.PlaceObstacleOnPolygonListener;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.mapoperator.PlaceNearestRoadsOnMapOperator;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.mapoperator.RoadEditorOperator;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.DownloadBlacklistedRoadsTask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.network.DownloadObstaclesTask;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.interfaces.IObstacleProvider;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.CustomPolyline;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ObstacleDataSingleton;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ObstacleOverlayItem;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ParcedOverpassRoad;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.RoadDataSingleton;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.MapEditorFragment;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.ObstacleDetailsViewerFragment;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.attributeEditFragments.CheckBoxAttributeFragment;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.attributeEditFragments.NumberAttributeFragment;
import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.ui.fragments.attributeEditFragments.TextAttributeFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

import java.util.ArrayList;
import java.util.List;

import bp.common.model.WayBlacklist;
import bp.common.model.obstacles.Construction;
import bp.common.model.obstacles.Elevator;
import bp.common.model.obstacles.FastTrafficLight;
import bp.common.model.obstacles.Obstacle;
import bp.common.model.obstacles.Stairs;
import bp.common.model.obstacles.TightPassage;
import bp.common.model.obstacles.Unevenness;
import bp.common.model.ways.Node;
import bp.common.model.ways.Way;
import okhttp3.Response;

/**
 * The starting point of the app.
 * <p>
 * This Activity displays the map fragment, the bottom sheet and the searchView.
 * <p>
 * <p>
 * Events send via the EventBus that require an update on the map, are handled in the
 * onMessageEvent() methods with the respective Event class as Parameter.
 */
public class BrowseMapActivity extends AppCompatActivity
        implements
        AdapterView.OnItemSelectedListener, MapEditorFragment.OnFragmentInteractionListener,
        TextAttributeFragment.OnFragmentInteractionListener, CheckBoxAttributeFragment.OnFragmentInteractionListener, NumberAttributeFragment.OnFragmentInteractionListener
        , IObstacleProvider {

    public FloatingActionButton floatingActionButton;
    public MapEditorFragment mapEditorFragment;
    private long selectedBarrier;
    private ArrayList<Polyline> currentPolylineArrayList = new ArrayList<>();

    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private CustomPolyline currentPolyline;

    public boolean roadEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the initial Blacklisted ways. Always update after inserting stairs obstacle.
        DownloadBlacklistedRoadsTask.downloadBlacklistedWays();

        setContentView(R.layout.activity_browser_map);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (findViewById(R.id.map_fragment_container) != null) {
            if (savedInstanceState != null)
                return;
            mapEditorFragment = MapEditorFragment.newInstance(this);
            mapEditorFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.map_fragment_container, mapEditorFragment).commit();
        }


        // get the bottom sheet view
        LinearLayout rlBottomLayout = (LinearLayout) findViewById(R.id.bottom_sheet);

        bottomSheetBehavior = BottomSheetBehavior.from(rlBottomLayout);
        bottomSheetBehavior.setHideable(false);
        BottomSheetBehavior.from(rlBottomLayout)
                .setState(BottomSheetBehavior.STATE_COLLAPSED);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });


        floatingActionButton = (FloatingActionButton) findViewById(R.id.action_place_obstacle);
        floatingActionButton.setOnClickListener(new ActionButtonClickListener());
        floatingActionButton.hide();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mapEditorFragment.map.getController().setCenter(new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude));
            }

            @Override
            public void onError(Status status) {
            }
        });

        final RadioButton placeObstacleModeButton = (RadioButton) findViewById(R.id.bottom_sheet_button_place_obstacle_mode);
        placeObstacleModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roadEditMode = false;
                floatingActionButton.hide();
                for (Polyline p : currentPolylineArrayList) {
                    mapEditorFragment.map.getOverlays().remove(p);
                }
                mapEditorFragment.placeNewObstacleOverlay.removeAllItems();
                ObstacleDataSingleton.getInstance().obstacleDataCollectionCompleted = false;
                mapEditorFragment.getStateHandler().setActiveOperator(new PlaceNearestRoadsOnMapOperator());
                mapEditorFragment.map.invalidate();

            }
        });
        final RadioButton roadEditorModeButton = (RadioButton) findViewById(R.id.bottom_sheet_button_road_edit_mode);
        roadEditorModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roadEditMode = true;
                floatingActionButton.hide();
                for (Polyline p : currentPolylineArrayList) {
                    mapEditorFragment.map.getOverlays().remove(p);
                }
                mapEditorFragment.placeNewObstacleOverlay.removeAllItems();
                ObstacleDataSingleton.getInstance().obstacleDataCollectionCompleted = false;
                mapEditorFragment.getStateHandler().setActiveOperator(new RoadEditorOperator());
                mapEditorFragment.map.invalidate();

            }
        });

        AlertDialog.Builder startupAlertBuilder = new AlertDialog.Builder(this);
        startupAlertBuilder.setTitle("Start Hilfe");
        startupAlertBuilder.setMessage("Um die umliegenden Straßen \"auswählbar\" zu machen, erfordert dies ein längeres drücken auf dem Bildschirm");
        startupAlertBuilder.setCancelable(true);
        startupAlertBuilder.setPositiveButton("OK", null);
        AlertDialog startupAlertDialog = startupAlertBuilder.create();
        startupAlertDialog.show();

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        getObstaclesFromServer();

    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Context context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));


        // Check if the last obstacle data collection was completed

        if (ObstacleDataSingleton.getInstance().obstacleDataCollectionCompleted) {
            floatingActionButton.hide();
            mapEditorFragment.placeNewObstacleOverlay.removeAllItems();
            for (Polyline p : currentPolylineArrayList) {
                mapEditorFragment.map.getOverlays().remove(p);
            }

            ObstacleDataSingleton.getInstance().obstacleDataCollectionCompleted = false;
        }

        if (mapEditorFragment != null && mapEditorFragment.mLocationOverlay != null) {
            mapEditorFragment.mLocationOverlay.enableMyLocation();
            mapEditorFragment.mLocationOverlay.enableFollowLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapEditorFragment != null && mapEditorFragment.mLocationOverlay != null) {
            mapEditorFragment.mLocationOverlay.disableMyLocation();
            mapEditorFragment.mLocationOverlay.disableFollowLocation();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        parent.getItemAtPosition(position);
        selectedBarrier = id;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public Obstacle getObstacle() {
        switch (String.valueOf(selectedBarrier)) {
            case "0":
                return new Stairs();
            case "2":
                return new Unevenness();
            case "3":
                return new Construction();
            case "4":
                return new FastTrafficLight();
            case "5":
                return new Elevator();
            case "6":
                return new TightPassage();
            default:
                return new Stairs();
        }
    }

    public void getObstaclesFromServer() {
        DownloadObstaclesTask.downloadObstacles();
    }


    /**
     * Delete all Blacklisted Roads and store them in the blackli
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEvent(BlacklistedRoadsTaskDownloadedEvent event) {

        try {
            Response response = event.getResponse();
            String res = response.body().string();

            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            if (!response.isSuccessful())
                return;

            RoadDataSingleton.getInstance().setBlacklistedRoads(mapper.<ArrayList<WayBlacklist>>readValue(res, new TypeReference<List<Way>>() { }));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(RoutingServerRoadDownloadEvent event) {

        try {
            Response response = event.getResponse();
            String res = response.body().string();

            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            if (!response.isSuccessful())
                return;

            final List<Way> wayList = mapper.readValue(res, new TypeReference<List<Way>>() {
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    for (Way way : wayList) {

                        if(isBlacklisted(way))
                            continue;

                        List<GeoPoint> gp = new ArrayList<GeoPoint>();
                        for (Node node : way.getNodes()) {
                            GeoPoint g = new GeoPoint(node.getLatitude(), node.getLongitude());
                            gp.add(g);
                        }

                        CustomPolyline streetLine = new CustomPolyline();
                        ParcedOverpassRoad r = new ParcedOverpassRoad();
                        r.id = way.id;
                        streetLine.setRoad(r);

                        streetLine.setTitle("Text param");
                        streetLine.setWidth(10f);
                        streetLine.setColor(Color.RED);
                        streetLine.setPoints(gp);
                        streetLine.setGeodesic(true);
                        streetLine.setOnClickListener(new PlaceObstacleOnPolygonListener());
                        streetLine.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, mapEditorFragment.map));
                        mapEditorFragment.map.getOverlays().add(streetLine);

                    }


                    Toast.makeText(getBaseContext(), getString(R.string.action_barrier_loaded),
                            Toast.LENGTH_SHORT).show();
                    mapEditorFragment.map.invalidate();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Very inefficient solution. This can be improved by either
     *  - not using overpass api and getting all roads from the server, and filter the blacklisted roads on the server
     *  - optimized data structure
     * @param way
     * @return
     */
    private boolean isBlacklisted(Way way) {

        for(WayBlacklist wayblacklist : RoadDataSingleton.getInstance().getBlacklistedRoads()){
            if(way.getOsm_id() == wayblacklist.getOsm_id())
                return true;
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(RoutingServerObstaclesDownloadedEvent event) {

        try {
            Response response = event.getResponse();
            String res = response.body().string();

            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            if (!response.isSuccessful())
                return;

            final List<Obstacle> obstacleList = mapper.readValue(res, new TypeReference<List<Obstacle>>() {
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Obstacle obstacle : obstacleList) {
                        // Only the starting point is displayed.
                        ObstacleOverlayItem overlayItem = new ObstacleOverlayItem(obstacle.getName(), getString(R.string.default_description), new GeoPoint(obstacle.getLatitudeStart(), obstacle.getLongitudeStart()), obstacle);
                        overlayItem.setMarker(getResources().getDrawable(R.mipmap.ramppic));
                        mapEditorFragment.obstacleOverlay.addItem(overlayItem);
                    }
                    Toast.makeText(getBaseContext(), getString(R.string.action_barrier_loaded),
                            Toast.LENGTH_SHORT).show();
                    mapEditorFragment.map.invalidate();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(RoutingServerObstaclePostedEvent event) {

        final Obstacle obstacle = event.getObstacle();
        Response response = event.getResponse();

        if (!response.isSuccessful())
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Only the starting point is displayed on the map.
                ObstacleOverlayItem overlayItemStart = new ObstacleOverlayItem(obstacle.getName(), getString(R.string.default_description), new GeoPoint(obstacle.getLatitudeStart(), obstacle.getLatitudeStart()), obstacle);

                overlayItemStart.setMarker(getResources().getDrawable(R.mipmap.ramppic));
                mapEditorFragment.obstacleOverlay.addItem(overlayItemStart);

                Toast.makeText(getBaseContext(), getString(R.string.action_barrier_loaded),
                        Toast.LENGTH_SHORT).show();

                mapEditorFragment.map.invalidate();
            }
        });


    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEvent(RoadPositionSelectedOnPolylineEvent event) {
        mapEditorFragment.placeNewObstacleOverlay.removeAllItems();
        GeoPoint point = event.getPoint();
        if (point != null) {
            OverlayItem overlayItem = new OverlayItem("", "", point);
            mapEditorFragment.placeNewObstacleOverlay.addItem(overlayItem);
            mapEditorFragment.map.invalidate();
            floatingActionButton.show();
        }
        //RoadDataSingleton.getInstance().currentStartingPositionOfSetObstacle = point;

    }






        @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEvent(ObstaclePositionSelectedOnPolylineEvent event) {


        // Assumption: if Startingpoint of Obstacle is already set, the currentPolyline is not null.
        if (event.getPolyline() == currentPolyline) {
            GeoPoint point = event.getPoint();
            if (point != null) {
                OverlayItem overlayItem = new OverlayItem("", "", point);
                Drawable newMarker = this.getResources().getDrawable(R.mipmap.ic_marker_end, null);

                overlayItem.setMarker(newMarker);
                mapEditorFragment.placeNewObstacleOverlay.addItem(overlayItem);

                mapEditorFragment.map.invalidate();
                ObstacleDataSingleton.getInstance().currentEndPositionOfSetObstacle = point;
                ObstacleDataSingleton.getInstance().isDoubleNodeObstacle = true;
                currentPolyline = null;

                floatingActionButton.show();
            } else {

                floatingActionButton.hide();
            }
        } else {
            mapEditorFragment.placeNewObstacleOverlay.removeAllItems();
            GeoPoint point = event.getPoint();
            if (point != null) {
                OverlayItem overlayItem = new OverlayItem("", "", point);

                Drawable newMarker = this.getResources().getDrawable(R.mipmap.ic_marker_start, null);
                overlayItem.setMarker(newMarker);

                mapEditorFragment.placeNewObstacleOverlay.addItem(overlayItem);
                mapEditorFragment.map.invalidate();
                floatingActionButton.show();
                ObstacleDataSingleton.getInstance().currentStartingPositionOfSetObstacle = point;
                // initialize the end position with the start point.
                ObstacleDataSingleton.getInstance().currentEndPositionOfSetObstacle = new GeoPoint(0f,0f);
                ObstacleDataSingleton.getInstance().isDoubleNodeObstacle = false;

                currentPolyline = event.getPolyline();
            } else {

                floatingActionButton.hide();
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEvent(RoadsHelperOverlayChangedEvent event) {
        mapEditorFragment.placeNewObstacleOverlay.removeAllItems();

        for (Polyline p : currentPolylineArrayList) {
            mapEditorFragment.map.getOverlays().remove(p);
        }

        currentPolylineArrayList = event.getRoads();

        for (Polyline p : currentPolylineArrayList) {
            mapEditorFragment.map.getOverlays().add(p);
        }

        mapEditorFragment.map.getOverlays().remove(mapEditorFragment.placeNewObstacleOverlay);
        mapEditorFragment.map.getOverlays().add(mapEditorFragment.placeNewObstacleOverlay);

        mapEditorFragment.map.invalidate();

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ObstacleOverlayItemSingleTapEvent event) {

        ObstacleOverlayItem overlayItem = event.getOverlayItem();

        LinearLayout rlBottomLayout = (LinearLayout) findViewById(R.id.bottom_sheet);
        BottomSheetBehavior.from(rlBottomLayout)
                .setState(BottomSheetBehavior.STATE_EXPANDED);

        ObstacleDetailsViewerFragment obstacleDetailsFragment = ObstacleDetailsViewerFragment.newInstance(event.getOverlayItem().getObstacle());
        obstacleDetailsFragment.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.obstacle_bottom_sheet_details_container, obstacleDetailsFragment).commit();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(StartEditObstacleEvent event) {
        ObstacleDataSingleton.getInstance().setObstacle(event.getObstacle());
        ObstacleDataSingleton.getInstance().currentStartingPositionOfSetObstacle = new GeoPoint(event.getObstacle().latitude_start, event.getObstacle().longitude_start);
        Intent intent = new Intent(BrowseMapActivity.this, PlaceObstacleActivity.class);
        startActivity(intent);
    }

}