package gima.neo4j.testsuite.osmcheck;

import java.util.ArrayList;
import java.util.Iterator;

public class Node {

    public int id;
    public double lat;
    public double lon;
    public boolean referenced;
    public ArrayList<Tag> tags = new ArrayList<Tag>();
    
    public void addTag(Tag t) {
        tags.add(t);
    }

    public boolean hasTags() {
        if (tags.size() > 0) {
            Iterator it = tags.iterator();
            while (it.hasNext()) {
                Tag tag = (Tag) it.next();
                System.out.println(tag.k + "," + tag.v);
            }
            return true;
        }
        return false;
    }

    public boolean validPoint(ArrayList<String> pointTags) {
        if (tags.size() > 0) {
            if (inList(pointTags)) {
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
