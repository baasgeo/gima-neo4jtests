/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.osmcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author bartbaas
 */
public class Parser extends DefaultHandler {

    public boolean isLoaded = false;
    public OpenStreetMap openStreetMap;
    private Way way;
    private Node node;
    private Tag tag;
    private String XmlOsmFile = "";
    private StringBuffer chars = new StringBuffer();

    public Parser(String osmfile) {
        this.XmlOsmFile = osmfile;
    }

    public void runExample() {
        parseDocument();
        printData();
    }

    public void startDocument() {
        openStreetMap = new OpenStreetMap();
        this.isLoaded = true;
    }

    public void endDocument() {
        this.isLoaded = false;
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(XmlOsmFile, this);

        } catch (SAXException se) {
        } catch (ParserConfigurationException pce) {
        } catch (IOException ie) {
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {

        System.out.println("Not validated:");
        System.out.println("No of Nodes " + openStreetMap.nodes.size());
        System.out.println("No of Ways " + openStreetMap.ways.size());
        System.out.println("No of Relations " + openStreetMap.relations);

        System.out.println("--");
        System.out.println("No of Points " + openStreetMap.countPoints());
        System.out.println("No of Ways " + openStreetMap.countWays());
        System.out.println("No of Lines " + openStreetMap.countPolygons());
        //System.out.println("Total " + (openStreetMap.countPoints()  + openStreetMap.countPolygons() + openStreetMap.countLineStrings()));
    }

    //Event Handlers
    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) throws SAXException {
        //System.out.println("start element    : " + qName);
        if (qName.equalsIgnoreCase("osm")) {
            openStreetMap.setGenerator(atts.getValue("generator"));
            openStreetMap.setVersion(atts.getValue("version"));
            return;
        } else if (qName.equalsIgnoreCase("bounds")) {
            openStreetMap.setMinlat(atts.getValue("minlat"));
            openStreetMap.setMaxlat(atts.getValue("maxlat"));
            openStreetMap.setMinlon(atts.getValue("minlon"));
            openStreetMap.setMaxlon(atts.getValue("maxlon"));
            return;
        } else if (qName.equalsIgnoreCase("node")) {
            //increment node counter
            node = new Node();
            node.lat = Double.parseDouble(atts.getValue("lat"));
            node.id = Integer.parseInt(atts.getValue("id"));
            node.lon = Double.parseDouble(atts.getValue("lon"));
            //node.x = Float.parseFloat(node.lon);
            //node.y = Float.parseFloat(node.lat);
            return;
        } else if (qName.equalsIgnoreCase("relation")) {
            openStreetMap.addRelation();
            return;
        } else if (qName.equalsIgnoreCase("way")) {
            way = new Way();
            way.id = Integer.parseInt(atts.getValue("id"));
            return;
        } else if (qName.equalsIgnoreCase("nd")) {
            way.addNode(Integer.parseInt(atts.getValue("ref")));
            return;

        } else if (qName.equalsIgnoreCase("tag")) {
            tag = new Tag();
            tag.k = atts.getValue("k");
            tag.v = atts.getValue("v");
            if (way != null) {
                way.addTag(tag);
            } else {
                node.addTag(tag);
            }
            return;
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        //System.out.println("end element      : " + qName);
        if (qName.equalsIgnoreCase("way")) {

            openStreetMap.addWay(way);
            return;
        } else if (qName.equalsIgnoreCase("node")) {
            openStreetMap.addNode(node);

            return;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        //System.out.println("start characters : " + new String(ch, start, length));
    }
}
