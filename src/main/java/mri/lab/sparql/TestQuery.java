/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mri.lab.sparql;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

/**
 *
 * @author pierpaolo
 */
public class TestQuery {

    private static final String QUERY1 = "select ?p ?y where {<http://dbpedia.org/resource/Bari> ?p ?y}";

    private static final String DBPEDIA_SPARQL_ENDPOINT = "https://dbpedia.org/sparql";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        QueryExecution e = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL_ENDPOINT, QUERY1);
        ResultSet rs = e.execSelect();
        while (rs.hasNext()) {
            QuerySolution nextSolution = rs.nextSolution();
            System.out.println(nextSolution.get("p").toString()+"\t"+nextSolution.get("y").toString());
        }
        e.close();
    }

}
