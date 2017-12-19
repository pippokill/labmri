/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mri.lab.cran;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author pierpaolo
 */
public class CranSearcher {

    private static final String[] fields = new String[]{"title", "abst"};

    private static void writeResults(String queryId, List<SearchResult> results, Writer writer) throws IOException {
        int rank = 1;
        for (SearchResult r : results) {
            writer.append(queryId).append(" 0 ").append(r.getId()).append(" ").append(String.valueOf(rank)).append(" ").append(String.valueOf(r.getScore())).append(" exp_0\n");
            rank++;
        }
    }

    /**
     * index_dir query_file results_file
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length == 3) {
                SearchEngine se = new SearchEngine(new File(args[0]));
                se.open();
                BufferedReader reader = new BufferedReader(new FileReader(new File(args[1])));
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(args[2])));
                String id = null;
                StringBuilder query = new StringBuilder();
                char code = ' ';
                int c = 1;
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line.startsWith(".I")) {
                        if (id != null) {
                            //search
                            List<SearchResult> search = se.search(query.toString(), fields, 1000);
                            writeResults(id, search, writer);
                            query = new StringBuilder();
                            c++;
                        }
                        id = String.valueOf(c); //id in cranqrel is the query order in query file
                        /*id = line.substring(2).trim();
                        if (id.startsWith("00")) {
                            id=id.substring(2,3);
                        } else if (id.startsWith("0")) {
                            id=id.substring(1,3);
                        }*/
                    } else if (line.startsWith(".W")) {
                        code = 'W';
                    } else {
                        switch (code) {
                            case 'W':
                                query.append(line).append(" ");
                                break;
                            default:
                                break;
                        }
                    }
                }
                reader.close();
                //store last documents
                if (id != null) {
                    List<SearchResult> search = se.search(query.toString(), fields, 1000);
                    writeResults(id, search, writer);
                    c++;
                }
                System.out.println("Total queries: " + c);
                se.close();
                writer.close();
            } else {
                Logger.getLogger(CranIndexer.class.getName()).log(Level.SEVERE, "Illegal arguments");
            }
        } catch (IOException ioex) {
            Logger.getLogger(CranIndexer.class.getName()).log(Level.SEVERE, null, ioex);
        } catch (ParseException ex) {
            Logger.getLogger(CranSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
