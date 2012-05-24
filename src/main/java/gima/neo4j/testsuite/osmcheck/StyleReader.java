/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gima.neo4j.testsuite.osmcheck;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bartbaas
 */
public class StyleReader {

    public static final String DEFAULT = "/usr/local/share/osm2pgsql/default.style";
    public static final String LINE = "linear";
    public static final String POLYGON = "polygon";

    public static ArrayList<String> readWays() {
        String line = "";
        ArrayList<String> data = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(DEFAULT);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] theline = split(line, " ", true);

                    if (theline.length > 3) {
                        if (theline[0].contains("way") & !theline[3].contains("delete")) {
                            data.add(theline[1]);
                        }
                    }
                }
            }
        } catch (FileNotFoundException fN) {
            fN.printStackTrace();
        } catch (IOException e) {
            System.out.println(e);
        }
        return data;
    }

    public static ArrayList<String> readNodes() {
        String line = "";
        ArrayList<String> data = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(DEFAULT);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] theline = split(line, " ", true);

                    if (theline.length > 3) {
                        if (theline[0].contains("node") & !theline[3].contains("delete")) {
                            data.add(theline[1]);
                        }
                    }
                }
            }
        } catch (FileNotFoundException fN) {
            fN.printStackTrace();
        } catch (IOException e) {
            System.out.println(e);
        }
        return data;
    }

    public static ArrayList<String> readPolyCandidates() {
        String line = "";
        ArrayList<String> data = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(DEFAULT);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] theline = split(line, " ", true);

                    if (theline.length > 3) {
                        if (theline[0].contains("way") & theline[3].contains(POLYGON)) {
                            data.add(theline[1]);
                        }
                    }
                }
            }
        } catch (FileNotFoundException fN) {
            fN.printStackTrace();
        } catch (IOException e) {
            System.out.println(e);
        }
        return data;
    }

    /**
     * Split a string based on the given delimiter, optionally removing
     * empty elements.
     *
     * @param str     The string to be split.
     * @param delimiter Split string based on this delimiter.
     * @param removeEmpty If <tt>true</tt> then remove empty elements.
     *
     * @return  Array of split strings. Guaranteeded to be not null.
     */
    private static String[] split(String str, String delimiter, boolean removeEmpty) {
        // Return empty list if source string is empty.
        final int len = (str == null) ? 0 : str.length();
        if (len == 0) {
            return new String[0];
        }

        final List<String> result = new ArrayList<String>();
        String elem = null;
        int i = 0, j = 0;
        while (j != -1 && j < len) {
            j = str.indexOf(delimiter, i);
            elem = (j != -1) ? str.substring(i, j) : str.substring(i);
            i = j + 1;
            if (!removeEmpty || !(elem == null || elem.length() == 0)) {
                result.add(elem);
            }
        }
        return result.toArray(new String[result.size()]);
    }
}
