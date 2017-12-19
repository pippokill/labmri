/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mri.lab.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.htmlparser.jericho.Config;
import static net.htmlparser.jericho.Config.LoggerProvider;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.w3c.dom.Document;

/**
 *
 * @author pierpaolo
 */
public class MapTitleURL {

    private static Set<String> extractGenre(Source source) {
        Set<String> set = new HashSet<>();
        List<Element> allElementsByClass = source.getAllElementsByClass("see-more inline canwrap");
        for (Element e : allElementsByClass) {
            if (e.getAttributeValue("itemprop").equals("genre")) {
                List<Element> childs = e.getChildElements();
                for (Element ce : childs) {
                    if (ce.getName().equals("a")) {
                        set.add(ce.getTextExtractor().toString());
                    }
                }
                return set;
            }
        }
        return set;
    }

    private static Object[] extractTuple(Element e) {
        String label = null;
        Set<String> set = new HashSet<>();
        List<Element> childElements = e.getChildElements();
        for (Element child : childElements) {
            if (label == null && child.getName().equals("h4")) {
                label = child.getTextExtractor().toString().replace(":", "").trim().toLowerCase();
            } else if (child.getName().equals("span")) {
                if (!(child.getAttributeValue("class") != null && child.getAttributeValue("class").equals("ghost"))) {
                    String text = child.getTextExtractor().toString().trim();
                    text = text.replace(",", "");
                    text = text.replace("\n+", "");
                    text = text.replaceAll("\\(.+\\)", "").trim().replaceAll("\\s+", "_");
                    set.add(text);
                }
            }
        }
        return new Object[]{label, set};
    }

    private static Map<String, Set<String>> getContent(String url) throws IOException {
        Map<String, Set<String>> map = new HashMap<>();
        Source source = new Source(new URL(url));
        String summary = null;
        List<Element> results = source.getAllElementsByClass("summary_text");
        if (!results.isEmpty()) {
            summary = results.get(0).getTextExtractor().toString();
        }
        System.out.println(summary);
        Set<String> set = new HashSet<>();
        set.add(summary);
        map.put("summary", set);
        results = source.getAllElementsByClass("credit_summary_item");
        for (Element er : results) {
            Object[] tuple = extractTuple(er);
            System.out.print(tuple[0]);
            System.out.print(" ");
            System.out.println(tuple[1]);
            map.put(tuple[0].toString(), (Set<String>) tuple[1]);
        }
        Set<String> extractGenre = extractGenre(source);
        System.out.println(extractGenre);
        map.put("genres", extractGenre);
        return map;
    }

    private static String getMovieUrl(String urlstr) throws IOException {
        URL url = new URL(urlstr);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setReadTimeout(60000);
        Source source = new Source(urlConnection);
        String titleUrl = null;
        List<Element> results = source.getAllElementsByClass("result_text");
        if (!results.isEmpty()) {
            List<Element> childElements = results.get(0).getChildElements();
            int k = 0;
            while (titleUrl == null) {
                if (childElements.get(k).getName().equals("a")) {
                    titleUrl = "http://www.imdb.com" + childElements.get(k).getAttributeValue("href");
                }
            }
        }
        return titleUrl;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException {
        Config.LoggerProvider = LoggerProvider.DISABLED;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "ISO-8859-1"));
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        // root elements
        Document doc = docBuilder.newDocument();
        org.w3c.dom.Element rootElement = doc.createElement("movies");
        doc.appendChild(rootElement);
        int c = 0;
        while (reader.ready()) {
            String[] split = reader.readLine().split("\\|");
            if (split.length > 0) {
                org.w3c.dom.Element movieElement = doc.createElement("movie");
                rootElement.appendChild(movieElement);
                org.w3c.dom.Element idElement = doc.createElement("id");
                idElement.setTextContent(split[0]);
                movieElement.appendChild(idElement);
                String qtitle = split[1];
                org.w3c.dom.Element titleElement = doc.createElement("title");
                titleElement.setTextContent(split[1]);
                movieElement.appendChild(titleElement);
                System.out.println(qtitle);
                String qurl = "http://www.imdb.com/find?q=" + URLEncoder.encode(qtitle, "utf-8") + "&tt=on";
                org.w3c.dom.Element qurlElement = doc.createElement("qurl");
                qurlElement.setTextContent(qurl);
                movieElement.appendChild(qurlElement);
                try {
                    String movieUrl = getMovieUrl(qurl);
                    org.w3c.dom.Element urlElement = doc.createElement("url");
                    urlElement.setTextContent(movieUrl);
                    movieElement.appendChild(urlElement);
                    Map<String, Set<String>> content = getContent(movieUrl);
                    if (content.containsKey("summary")) {
                        org.w3c.dom.Element summaryElement = doc.createElement("summary");
                        Set<String> set = content.get("summary");
                        for (String s : set) {
                            org.w3c.dom.Element e = doc.createElement("content");
                            e.setTextContent(s);
                            summaryElement.appendChild(e);
                        }
                        movieElement.appendChild(summaryElement);
                    }
                    if (content.containsKey("director")) {
                        org.w3c.dom.Element cElement = doc.createElement("directors");
                        Set<String> set = content.get("director");
                        for (String s : set) {
                            org.w3c.dom.Element e = doc.createElement("director");
                            e.setTextContent(s);
                            cElement.appendChild(e);
                        }
                        movieElement.appendChild(cElement);
                    }
                    if (content.containsKey("writers")) {
                        org.w3c.dom.Element cElement = doc.createElement("writers");
                        Set<String> set = content.get("writers");
                        for (String s : set) {
                            org.w3c.dom.Element e = doc.createElement("writer");
                            e.setTextContent(s);
                            cElement.appendChild(e);
                        }
                        movieElement.appendChild(cElement);
                    }
                    if (content.containsKey("stars")) {
                        org.w3c.dom.Element cElement = doc.createElement("stars");
                        Set<String> set = content.get("stars");
                        for (String s : set) {
                            org.w3c.dom.Element e = doc.createElement("star");
                            e.setTextContent(s);
                            cElement.appendChild(e);
                        }
                        movieElement.appendChild(cElement);
                    }
                    if (content.containsKey("genres")) {
                        org.w3c.dom.Element cElement = doc.createElement("genres");
                        Set<String> set = content.get("genres");
                        for (String s : set) {
                            org.w3c.dom.Element e = doc.createElement("genre");
                            e.setTextContent(s);
                            cElement.appendChild(e);
                        }
                        movieElement.appendChild(cElement);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MapTitleURL.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            c++;
            if (c % 30 == 0) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MapTitleURL.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        reader.close();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(args[1]));
        transformer.transform(source, result);
    }

}
