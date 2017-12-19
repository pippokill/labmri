/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mri.lab.tika;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;

/**
 *
 * @author pierpaolo
 */
public class TikaIndexer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LATEST, new StandardAnalyzer());
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(FSDirectory.open(new File(args[1])), iwc);
            Tika tika = new Tika();
            File dir = new File(args[0]);
            File[] listFiles = dir.listFiles();
            for (File file : listFiles) {
                if (file.isFile()) {
                    Metadata meta = new Metadata();
                    String text = tika.parseToString(new FileInputStream(file), meta);
                    Document doc = new Document();
                    doc.add(new StringField("path", file.getAbsolutePath(), Field.Store.YES));
                    doc.add(new TextField("text", text, Field.Store.NO));
                    String[] names = meta.names();
                    for (String name : names) {
                        String value = meta.get(name);
                        if (value != null) {
                            doc.add(new StringField(name, value, Field.Store.YES));
                        }
                    }
                    writer.addDocument(doc);
                }
            }
            writer.close();
        } catch (IOException | TikaException ioex) {
            Logger.getLogger(TikaIndexer.class.getName()).log(Level.SEVERE, null, ioex);
        }
    }

}
