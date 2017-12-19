/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mri.lab.merge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mri.lab.cran.SearchResult;
import org.apache.commons.math3.stat.StatUtils;

/**
 *
 * @author pierpaolo
 */
public class Merge {

    private static List<SearchResult> normalize(List<SearchResult> list) {
        List<SearchResult> normList = new ArrayList<>(list.size());
        double[] s = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            s[i] = list.get(i).getScore();
        }
        double mean = StatUtils.mean(s);
        double variance = StatUtils.variance(s, mean);
        for (SearchResult r : list) {
            normList.add(new SearchResult(r.getId(), (r.getScore() - mean) / variance));
        }
        return normList;
    }

    private static Map<String, List<SearchResult>> buildResults(File file) throws IOException {
        Map<String, List<SearchResult>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while (reader.ready()) {
            String[] split = reader.readLine().split("\\s+");
            List<SearchResult> list = map.get(split[0]);
            if (list == null) {
                list = new ArrayList<>();
                map.put(split[0], list);
            }
            list.add(new SearchResult(split[2], Double.parseDouble(split[4])));
        }
        reader.close();
        return map;
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 3) {
            String[] filenames = args[0].split(":");
            System.out.print("Weights");
            String[] wvalues = args[1].split(":");
            double[] wl = new double[wvalues.length];
            for (int i = 0; i < wvalues.length; i++) {
                wl[i] = Double.parseDouble(wvalues[i]);
                System.out.print(" " + wl[i]);
            }
            System.out.println();
            List<Map<String, List<SearchResult>>> results = new ArrayList<>();
            for (String filename : filenames) {
                Map<String, List<SearchResult>> map = buildResults(new File(filename));
                for (String key : map.keySet()) {
                    List<SearchResult> get = map.get(key);
                    List<SearchResult> nget = normalize(get);
                    map.put(key, nget);
                }
                results.add(map);
            }
            if (!results.isEmpty()) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
                List<String> keys = new ArrayList<>(results.get(0).keySet());
                Collections.sort(keys);
                for (String key : keys) {
                    Map<String, SearchResult> merge = new HashMap<>();
                    for (int i = 0; i < results.size(); i++) {
                        List<SearchResult> list = results.get(i).get(key);
                        for (SearchResult r : list) {
                            SearchResult result = merge.get(r.getId());
                            if (result == null) {
                                merge.put(r.getId(), new SearchResult(r.getId(), wl[i] * r.getScore()));
                            } else {
                                result.setScore(result.getScore() + wl[i] * r.getScore());
                            }
                        }
                    }
                    List<SearchResult> finalList = new ArrayList<>(merge.values());
                    Collections.sort(finalList, Collections.reverseOrder());
                    if (finalList.size() > 1000) {
                        finalList = finalList.subList(0, 1000);
                    }
                    int rank = 1;
                    for (SearchResult r : finalList) {
                        writer.append(key).append(" Q0 ").append(r.getId()).append(" ")
                                .append(String.valueOf(rank)).append(" ")
                                .append(String.valueOf(r.getScore())).append(" merge");
                        writer.newLine();
                        rank++;
                    }
                }
                writer.close();
            }
        }
    }

}
