package gima.neo4j.testsuite.osmcheck;

import java.util.ArrayList;
import java.util.Iterator;

public class Way {

    public int id;
    public ArrayList<Integer> nd = new ArrayList<Integer>();
    public ArrayList<Tag> tags = new ArrayList<Tag>();

    public void addNode(int nodeId) {
        nd.add(nodeId);
    }

    public void addTag(Tag t) {
        tags.add(t);
    }

    public int getNdSize() {
        return nd.size();

    }

    public int getTagsSize() {
        return tags.size();
    }

    public String getAreaTag() {
        Iterator it = tags.iterator();
        while (it.hasNext()) {
            Tag tag = (Tag) it.next();
            if (tag.k.equals("area") && tag.v.equals("yes")) {
                return "Area";
            }
        }
        return "NoArea";
    }

    public String getType() {
        if (nd.get(0).equals(nd.get(nd.size() - 1))) {
            return "Polyline";
        } else {
            return "Line";
        }
    }

    public boolean validWay(ArrayList<String> wayTags) {
        if (tags.size() > 0) {
            if (inList(wayTags)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPolyline(ArrayList<String> polyTags) {
        if (nd.get(0).equals(nd.get(nd.size() - 1))) {
            if (hasAreaTag()) {
                return true;
            }
            if (inList(polyTags)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAreaTag() {
        Iterator it = tags.iterator();
        while (it.hasNext()) {
            Tag tag = (Tag) it.next();
            if (tag.k.equals("area") && tag.v.equals("yes")) {
                return true;
            }
        }
        return false;
    }

    private boolean inList(ArrayList<String> list) {
        Iterator it = tags.iterator();
        while (it.hasNext()) {
            Tag tag = (Tag) it.next();
            if (list.contains(tag.k)) {
                return true;
            }

        }
        return false;
    }
}
