package gima.neo4j.testsuite.osmcheck;

import java.util.ArrayList;
import java.util.Iterator;

public class OpenStreetMap {
    /* basic */

    public String minlat, minlon, maxlat, maxlon;
    String version, generator;
    public int nodesnum = 0;
    public int relations = 0;
    ArrayList<Node> nodes = new ArrayList<Node>();
    ArrayList<Way> ways = new ArrayList<Way>();
    public static String separator = "__";

    public Node getNode(int _id) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).id == _id) {
                return nodes.get(i);
            }
        }
        return null;
    }

    public String[] searchNode(String pattern) {
        String coordinates[] = new String[2];
        // see origin -w- or -n-

        Way w;
        Node n;
        coordinates[0] = "" + 0.0;
        coordinates[1] = "" + 0.0;
        for (int i = 0; i < ways.size(); i++) {
            w = ways.get(i);

            for (int j = 0; j < w.tags.size(); j++) {

                if (w.tags.get(j).k.equals("name") && w.tags.get(j).v.equals(pattern)) {
                    // prendi il primo punto che trovi, non � sengato, ovviamente, dal numero civico
                    n = getNode(w.nd.get(0));

                    if (n != null) {
                        coordinates[0] = "" + n.lat;//Double.parseDouble(n.lat);
                        coordinates[1] = "" + n.lon;//Double.parseDouble(n.lon);
                    } else {
                        coordinates[0] = "Nodo non trovato";
                        coordinates[1] = "Nodo non trovato"; //w.nd.get(0);//""+Double.parseDouble(w.nd.get(0));
                    }
                    return coordinates;
                }
            }
        }
        for (int ii = 0; ii < nodes.size(); ii++) {
            n = nodes.get(ii);

            for (int jj = 0; jj < n.tags.size(); jj++) {
                //eepContent += w.tags.get(j).k +"\n";
                if (n.tags.get(jj).k.equals("name") && n.tags.get(jj).v.equals(pattern)) {
                    coordinates[0] = "" + n.lat;
                    coordinates[1] = "" + n.lon;
                    return coordinates;
                }
            }
        }
        return coordinates;

    }

    public String generateTagString() {

        String deepContent = "";
        Way w;
        Node n;
        for (int i = 0; i < ways.size(); i++) {
            w = ways.get(i);

            for (int j = 0; j < w.tags.size(); j++) {

                if (w.tags.get(j).k.equals("name") || w.tags.get(j).k.equals("amenity")) {
                    deepContent += separator + w.tags.get(j).v + "-w-";
                } else {
                    continue;
                }
            }
        }
        for (int ii = 0; ii < nodes.size(); ii++) {
            n = nodes.get(ii);

            for (int jj = 0; jj < n.tags.size(); jj++) {
                //eepContent += w.tags.get(j).k +"\n";
                if (n.tags.get(jj).k.equals("name")) {
                    deepContent += separator + n.tags.get(jj).v + "-n-";
                    //deepContent += separator + w.tags.get(j).v;
                } else {
                    continue;
                }
            }
        }
        return deepContent;
    }

    public String getXML() {
        return ("version : " + version + "\n"
                + "generator : " + generator + "\n\n"
                + "bounds : \n"
                + " - min Lat : " + minlat + "\n"
                + " - min Lon : " + minlon + "\n"
                + " - max Lat : " + maxlat + "\n"
                + " - max Lon : " + maxlon + "\n\n"
                + "nodes : " + nodes.size() + "\n"
                + "relations : " + relations + "\n"
                + "ways : " + ways.size() + "( way 1 : rel " + ways.get(1).getNdSize() + ", tag " + ways.get(1).getTagsSize() + ")");
    }

    public int countPoints() {
        int counter = 0;
        ArrayList<String> pointTags = StyleReader.readNodes();
        
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            Node node = (Node) it.next();
            if (node.validPoint(pointTags)) {
                counter++;
            }
        }
        return counter;
    }

    public int countWays() {
        int counter = 0;
        ArrayList<String> lineTags = StyleReader.readWays();

        Iterator it = ways.iterator();
        while (it.hasNext()) {
            Way way = (Way) it.next();
            if (way.validWay(lineTags)) {
                counter++;
            }
        }
        return counter;
    }

    public int countPolygons() {
        int counter = 0;
        //ArrayList<String> lineTags = StyleReader.readWays();
        ArrayList<String> polyTags = StyleReader.readPolyCandidates();

        Iterator it = ways.iterator();
        while (it.hasNext()) {
            Way way = (Way) it.next();
            if (way.isPolyline(polyTags)) {
                counter++;
            }
        }
        return counter;
    }

    public void addNode(Node n) {
        nodes.add(n);
    }

    public void addRelation() {
        relations++;
    }

    public void addWay(Way w) {
        ways.add(w);

    }

    public String getMinlat() {
        return minlat;
    }

    public void setMinlat(String minlat) {
        this.minlat = minlat;
    }

    public String getMinlon() {
        return minlon;
    }

    public void setMinlon(String minlon) {
        this.minlon = minlon;
    }

    public String getMaxlat() {
        return maxlat;
    }

    public void setMaxlat(String maxlat) {
        this.maxlat = maxlat;
    }

    public String getMaxlon() {
        return maxlon;
    }

    public void setMaxlon(String maxlon) {
        this.maxlon = maxlon;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }
}
