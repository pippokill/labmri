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
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;

/**
 *
 * @author pierpaolo
 */
public class TikaTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                Tika tika = new Tika();
                String type = tika.detect(new File(args[0]));
                System.out.println("File type: " + type);
                Metadata metadata = new Metadata();
                String text = tika.parseToString(new FileInputStream(new File(args[0])),
                        metadata);
                System.out.println("Metadata");
                System.out.println("===========");
                String[] names = metadata.names();
                for (String name : names) {
                    String value = metadata.get(name);
                    if (value != null) {
                        System.out.println(name + "=" + value);
                    }
                }
                System.out.println("Text");
                System.out.println("=======");
                System.out.println(text);
            } else {
                System.out.println("Filename is required.");
            }
        } catch (IOException | TikaException ex) {
            Logger.getLogger(TikaTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
