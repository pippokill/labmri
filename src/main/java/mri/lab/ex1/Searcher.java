/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mri.lab.ex1;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author pierpaolo
 */
public class Searcher {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {
        if (args.length==3) {
            FSDirectory fsdir = FSDirectory.open(new File(args[0]));
            DirectoryReader idxReader = DirectoryReader.open(fsdir);
            IndexSearcher searcher=new IndexSearcher(idxReader);
            QueryParser parser=new QueryParser("content", new StandardAnalyzer());
            Query q=parser.parse(args[1]);
            TopDocs topDocs = searcher.search(q, Integer.parseInt(args[2]));
            ScoreDoc[] sd=topDocs.scoreDocs;
            for (ScoreDoc d:sd) {
                System.out.println(searcher.doc(d.doc).get("path")+"\t"+d.score);
            }
        } else {
            System.err.println("Sono richiesti tre parametri: index_dir, query, num_docs");
            System.exit(1);
        }
    }
    
}
