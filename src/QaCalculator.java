import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;



/**
 * Created by wwang on 8/28/16.
 */
public class QaCalculator {
    public static final int long_length = 2500;
    public double count;
    public int total;
    private PrintWriter pw;
    private static final String SAMPLE = "sample";
    private static final String ID = "id";
    private static final String TYPING = "typing";
    private static final String SEQUENCE_BLOCK = "consensus-sequence-block";
    private static final String SEQUENCE = "sequence";
    private static final String GLString = "glstring";
    private static final String SEQ_ID= "reference-sequence-id";
    private static final String PHASE_SET = "phase-set";
    private static final String HAPLOID = "haploid";
    private static final String HAPLOID_TYPE = "type";
    private static final String HAPLOID_Locus = "locus";
    private String folderName;

    public QaCalculator(String folderName){
        this.folderName = folderName;
    }


    public void run(File input, PrintWriter pw) throws IOException, SAXException, ParserConfigurationException {
        this.pw = pw;

        // Initialize doc
        Document doc = getDoc(input);

        // Get all sample nodes
        NodeList sampleList = doc.getElementsByTagName(SAMPLE);
        for (int i = 0; i < sampleList.getLength(); i++) {
            parseSample(sampleList.item(i));
        }
    }

    /**
     * Parse the sample node.
     *
     * @param node The sample node.
     */
    private void parseSample(Node node) {
        Element sample = (Element) node;
        String sampleID = sample.getAttribute(ID);
        // Get all typing nodes
        NodeList typingList = sample.getElementsByTagName(TYPING);
        for (int j = 0; j < typingList.getLength(); j++) {
            parseTyping(typingList.item(j), sampleID);
        }
    }

    /**
     * Parse the typing node.
     *
     * @param hla The typing node.
     */
    public void parseTyping(Node hla, String sampleID) {
        Element element = (Element) hla;
        if(hasGls(element)){
            //phase gls
            parseGls(element, sampleID);
        }else {
            //phase haploid
            parseHap(element, sampleID);
        }

    }

    private void parseGls(Element element, String sampleID) {
        if(twoGls(element)){
            String [] glsArray = getGlsArray(getGLString(element));
            printFirstGls(glsArray, element, sampleID);
            printSecondGls(glsArray, element, sampleID);
        }else {
            //one gls
            //print header
            pw.print(folderName);
            pw.print(",");
            pw.print(sampleID);
            pw.print(",");
            pw.print("alleleOne");
            pw.print(",");

            pw.print(getGLString(element));

            pw.print(",");
            printGls(element);
            pw.print(",");

            StringBuilder sequence = new StringBuilder();
            NodeList  sequenceList = element.getElementsByTagName(SEQUENCE_BLOCK);
            if(sequenceList.getLength() == 1){
                Element seq = (Element) sequenceList.item(0);
                sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
            }else {
                for(int i = 0; i < sequenceList.getLength(); i++){
                    Element seq = (Element) sequenceList.item(i);
                    sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
                }
            }
            pw.println(sequence.toString());
        }
    }



    private String[] getGlsArray(String gls) {
        if (!gls.contains("+")) {
            throw new RuntimeException("Can not get gls array if there is no plus sign");
        }
        String[] glsArray = new String[2];
        if (gls.contains("|")) {
            String[] hlaList = gls.split("\\|");
            StringBuilder hla0 = new StringBuilder();
            StringBuilder hla1 = new StringBuilder();
            for (int j = 0; j < hlaList.length; j++) {
                String[] subHla = hlaList[j].split("\\+");
                //Do not add duplicate hla string
                if (!hla0.toString().contains(subHla[0])) {
                    if (hla0.length() != 0) {
                        hla0.append("/");
                    }
                    hla0.append(subHla[0]);

                }

                //Do not add duplicate hla string
                if (!hla1.toString().contains(subHla[1])) {
                    if (hla1.length() != 0) {
                        hla1.append("/");
                    }
                    hla1.append(subHla[1]);
                }
            }
            glsArray[0] = hla0.toString();
            glsArray[1] = hla1.toString();
        }else{
            glsArray = gls.split("\\+");
        }
        return glsArray;
    }

    private boolean twoGls(Element element){
        NodeList glsElement = element.getElementsByTagName(GLString);
        String gls = "";
        if(glsElement.item(0) != null){
            gls = glsElement.item(0).getTextContent().trim();
        }
        return gls.contains("+");
    }


    private String getGLString(Element element){
        String gls = "";
        NodeList glsElement = element.getElementsByTagName(GLString);
        if(glsElement.item(0) != null){
            gls = glsElement.item(0).getTextContent().trim();
        }
        return gls;
    }
    private void printFirstGls(String[] gls, Element element, String sampleID){
        pw.print(folderName);
        pw.print(",");
        pw.print(sampleID);
        pw.print(",");

        if(gls[0].equals(gls[1])){
            pw.print("Homozygous");
            pw.print(",");
        }else {
            pw.print("allele1");
            pw.print(",");
        }

        pw.print(gls[0]);

        pw.print(",");
        printGls(element);
        pw.print(",");

        StringBuilder sequence = new StringBuilder();
        NodeList  sequenceList = element.getElementsByTagName(SEQUENCE_BLOCK);
        if(sequenceList.getLength() == 1){
            Element seq = (Element) sequenceList.item(0);
            sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
        }else {
            for(int i = 0; i < sequenceList.getLength()/2; i++){
                Element seq = (Element) sequenceList.item(i);
                sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
            }
        }

        pw.println(sequence.toString());

    }

    private void printSecondGls(String[] gls, Element element, String sampleID) {
        pw.print(folderName);
        pw.print(",");
        pw.print(sampleID);
        pw.print(",");

        if(gls[0].equals(gls[1])){
            pw.print("Homozygous");
            pw.print(",");
        }else {
            pw.print("allele2");
            pw.print(",");
        }

        pw.print(gls[1]);
        pw.print(",");
        printGls(element);
        pw.print(",");

        StringBuilder sequence = new StringBuilder();
        NodeList  sequenceList = element.getElementsByTagName(SEQUENCE_BLOCK);
        if(sequenceList.getLength() == 1){
            Element seq = (Element) sequenceList.item(0);
            sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
        }else {
            for(int i = sequenceList.getLength()/2; i < sequenceList.getLength(); i++){
                Element seq = (Element) sequenceList.item(i);
                sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
            }
        }

        pw.println(sequence.toString());
    }

    private void parseHap(Element element, String sampleID) {
        if(twoHap(element)){
            String [] hapArray = getHapArray(element);
            printFirstHap(hapArray, element, sampleID);
            printSecondHap(hapArray, element, sampleID);
        }else {
            //one haploid
            pw.print(folderName);
            pw.print(",");
            pw.print(sampleID);
            pw.print(",");

            pw.print("alleleOne");
            pw.print(",");

            NodeList hapElement = element.getElementsByTagName(HAPLOID);
            //There is one haploid
            Element hla = (Element) hapElement.item(0);
            String haploid = hla.getAttribute(HAPLOID_Locus) + "*" + hla.getAttribute(HAPLOID_TYPE);
            pw.print(haploid);
            pw.print(",");
            printGls(element);
            pw.print(",");

            StringBuilder sequence = new StringBuilder();
            NodeList  sequenceList = element.getElementsByTagName(SEQUENCE_BLOCK);
            if(sequenceList.getLength() == 1){
                Element seq = (Element) sequenceList.item(0);
                sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
            }else {
                for(int i = sequenceList.getLength(); i < sequenceList.getLength(); i++){
                    Element seq = (Element) sequenceList.item(i);
                    sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
                }
            }
            pw.println(sequence.toString());
        }
    }

    private void printSecondHap(String[] hapArray, Element element, String sampleID) {
        pw.print(folderName);
        pw.print(",");
        pw.print(sampleID);
        pw.print(",");

        pw.print("allele2");
        pw.print(",");
        pw.print(hapArray[1]);
        pw.print(",");
        printGls(element);
        pw.print(",");

        StringBuilder sequence = new StringBuilder();
        NodeList  sequenceList = element.getElementsByTagName(SEQUENCE_BLOCK);
        if(sequenceList.getLength() == 1){
            Element seq = (Element) sequenceList.item(0);
            sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
        }else {
            for(int i = sequenceList.getLength()/2; i < sequenceList.getLength(); i++){
                Element seq = (Element) sequenceList.item(i);
                sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
            }
        }

        pw.println(sequence.toString());
    }

    private void printFirstHap(String[] hapArray, Element element, String sampleID) {
        pw.print(folderName);
        pw.print(",");
        pw.print(sampleID);
        pw.print(",");

        pw.print("allele1");
        pw.print(",");
        pw.print(hapArray[0]);
        pw.print(",");
        printGls(element);
        pw.print(",");

        StringBuilder sequence = new StringBuilder();
        NodeList  sequenceList = element.getElementsByTagName(SEQUENCE_BLOCK);
        if(sequenceList.getLength() == 1){
            Element seq = (Element) sequenceList.item(0);
            sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
        }else {
            for(int i = 0; i < sequenceList.getLength()/2; i++){
                Element seq = (Element) sequenceList.item(i);
                sequence.append(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
            }
        }

        pw.println(sequence.toString());
    }

    private String[] getHapArray(Element element) {
        NodeList hapElement = element.getElementsByTagName(HAPLOID);
        //There is two haploids
        Element hla1 = (Element) hapElement.item(0);
        Element hla2 = (Element) hapElement.item(1);
        String[] hap = new String[2];
        hap[0] = hla1.getAttribute(HAPLOID_Locus) + "*" + hla1.getAttribute(HAPLOID_TYPE);
        hap[1] = hla2.getAttribute(HAPLOID_Locus) + "*" + hla2.getAttribute(HAPLOID_TYPE);
        return  hap;
    }

    private boolean twoHap(Element element) {
        NodeList hapElement = element.getElementsByTagName(HAPLOID);
        return hapElement.getLength() == 2;
    }

    private void printGls(Element element) {
        if(hasGls(element)){
            NodeList glsElement = element.getElementsByTagName(GLString);
            String hlaString = "";
            if(glsElement.item(0) != null){
                hlaString = glsElement.item(0).getTextContent().trim();
            }
            pw.print(hlaString);

        }else {
            pw.print("haploid");
        }

    }








    private boolean hasGls(Element element){
        NodeList glsElement = element.getElementsByTagName(GLString);
        return glsElement.item(0) != null;
    }



    private boolean isShort(int l){
        return l< long_length;
    }

    /**
     * Get doc from input file.
     *
     * @return A document.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document getDoc(File input) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(input);
        doc.getDocumentElement().normalize();
        return doc;
    }
}
