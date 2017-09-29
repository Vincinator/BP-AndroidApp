package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.overlayBuilder;

import com.tudarmstadt.barrierefreiesrouting.datacollectionapp.model.ParcedOverpassRoad;

import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Parse the Response from the overpass api to a list of roads (and nodes).
 */
public class OsmParser extends DefaultHandler {

    /**
     * Stores all nodes from the overpass api response
     */
    private HashMap<Long, bp.common.model.ways.Node> nodes = new HashMap<>();
    /**
     * Stores all Roads from the overpass api response
     */
    private LinkedList<ParcedOverpassRoad> roads = new LinkedList<>();
    private ParcedOverpassRoad currentRoad;
    private bp.common.model.ways.Node currentNode;

    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equalsIgnoreCase("node")) {
            currentNode = new bp.common.model.ways.Node();

            currentNode.setOsm_id(Long.parseLong(attributes.getValue("id")));
            double lat = Double.parseDouble(attributes.getValue("lat"));
            double lon = Double.parseDouble(attributes.getValue("lon"));
            currentNode.setLatitude(lat);
            currentNode.setLongitude(lon);
            currentNode.setId(Long.parseLong(attributes.getValue("id")));

        } else if (qName.equalsIgnoreCase("way")) {
            currentRoad = new ParcedOverpassRoad();
            currentRoad.id = Long.parseLong(attributes.getValue("id"));

        } else if (qName.equalsIgnoreCase("nd")) {
            if(nodes.get(Long.parseLong(attributes.getValue("ref"))) != null)
            currentRoad.getRoadPoints().add(
                    new GeoPoint(nodes.get(Long.parseLong(attributes.getValue("ref"))).getLatitude(),
                    nodes.get(Long.parseLong(attributes.getValue("ref"))).getLongitude())

            );
            currentRoad.getRoadNodes().add(
                    nodes.get(Long.parseLong(attributes.getValue("ref")))
            );
        }
    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("node")) {
            nodes.put(currentNode.getId(), currentNode);

        } else if (qName.equalsIgnoreCase("way")) {
            roads.add(currentRoad);
        }
    }

    @Override
    public void characters(char ch[],
                           int start, int length) throws SAXException {

    }

    public LinkedList<ParcedOverpassRoad> getRoads() {
        return roads;
    }
}
