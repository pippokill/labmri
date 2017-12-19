/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mri.lab.ex1;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author pierpaolo
 */
public class Indexer {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 3) {
            FSDirectory fsdir = FSDirectory.open(new File(args[1]));
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LATEST, new StandardAnalyzer());
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(fsdir, iwc);
            FieldType myType = new FieldType(TextField.TYPE_NOT_STORED);
            switch (args[2]) {
                case "tv":
                    myType.setStoreTermVectors(true);
                    break;
                case "tvp":
                    myType.setStoreTermVectors(true);
                    myType.setStoreTermVectorPositions(true);
                    break;
                case "tvo":
                    myType.setStoreTermVectors(true);
                    myType.setStoreTermVectorPositions(true);
                    myType.setStoreTermVectorOffsets(true);
                    break;
                default:
                    myType.setTokenized(true);
                    break;
            }
            File dir = new File(args[0]);
            File[] listFiles = dir.listFiles();
            for (File file : listFiles) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    Document doc = new Document();
                    doc.add(new StringField("path", file.getAbsolutePath(), Field.Store.YES));
                    doc.add(new Field("content", new FileReader(file), myType));
                    writer.addDocument(doc);
                }
            }
            writer.close();
        } else {
            System.err.println("Sono richiesti tre parametri: input_dir, index_dir, field_type");
            System.exit(1);
        }
    }

}
