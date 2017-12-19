/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mri.lab.cran;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author pierpaolo
 */
public class SearchEngine {

    private final File dir;

    private IndexWriter writer;

    public SearchEngine(File dir) {
        this.dir = dir;
    }

    public void open() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, new StandardAnalyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        writer = new IndexWriter(FSDirectory.open(dir), config);
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    public void addDocument(Document doc) throws IOException {
        writer.addDocument(doc);
    }

    public List<SearchResult> search(String query, String[] fields, int n) throws IOException, ParseException {
        String eq = QueryParser.escape(query);
        QueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
        Query q = parser.parse(eq);
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(this.dir)));
        List<SearchResult> results = new ArrayList<>();
        TopDocs top = searcher.search(q, n);
        for (ScoreDoc doc : top.scoreDocs) {
            results.add(new SearchResult(searcher.doc(doc.doc).get("id"), doc.score));
        }
        return results;
    }

}
