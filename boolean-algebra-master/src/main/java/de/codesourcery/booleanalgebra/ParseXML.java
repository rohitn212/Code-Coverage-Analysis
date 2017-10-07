package de.codesourcery.booleanalgebra;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ParseXML {

    static final String outputEncoding = "UTF-8";

    private static File[] getFilesInFolder() {
        File folder = new File("xml");
        return folder.listFiles();
    }

    public static void readXML(File xmlFile) throws ParserConfigurationException {
        StringBuilder testName = new StringBuilder(xmlFile.getName().toString());
        testName.delete(testName.length()-4, testName.length());
        System.out.println(testName.toString());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = null;
        try {
            doc = dBuilder.parse(xmlFile);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        doc.getDocumentElement().normalize();
        final String mapInputFileName = "MapInput.txt";
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("sourcefile");
        ArrayList<String> classNames = new ArrayList<String>();
        System.out.println("----------------------------");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            String className = nNode.getAttributes().item(0).getTextContent();
            classNames.add(className);
            System.out.println("\nCurrent Element : " + className);
            if (nNode.getNodeType() != Node.ELEMENT_NODE) continue;
            Element eElement = (Element) nNode;
            NodeList sList = eElement.getElementsByTagName("line");
            for (int i = 0; i < sList.getLength(); i++) {
                Node sNode = sList.item(i);
                if (nNode.getNodeType() != Node.ELEMENT_NODE) continue;
                Element sElement = (Element) sNode;
                String nr = sElement.getAttribute("nr");
                String ci = sElement.getAttribute("ci");
                if (!ci.equalsIgnoreCase("0")) {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(mapInputFileName, true))) {
                        bw.write(nr + " " + className + " " + testName.toString());
                        bw.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws ParserConfigurationException
    {
        File[] xmlFiles = getFilesInFolder();
        for (File xmlFile: xmlFiles) {
            if (!xmlFile.isFile()) continue;
            readXML(xmlFile);
        }
    }
}
