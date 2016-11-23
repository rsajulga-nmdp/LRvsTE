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
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * Created by wwang on 8/28/16.
 */
public class QaCalculator {
    public static final int long_length = 2500;
    public double count;
    public int total;
    private PrintWriter pw;
    private static final String SAMPLE = "ns2:sample";
    private static final String ID = "id";
    private static final String TYPING = "ns2:typing";
    private static final String SEQUENCE_BLOCK = "ns2:consensus-sequence-block";
    private static final String SEQUENCE = "ns2:sequence";
    private static final String GLString = "ns2:glstring";
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
        NodeList  sequenceList = element.getElementsByTagName(SEQUENCE_BLOCK);
        int size = sequenceList.getLength();
        for(int i=0; i<size; i++){
            total++;
            Element seq = (Element) sequenceList.item(i);
            pw.print(sampleID);
            pw.print(",");
            NodeList glsElement = element.getElementsByTagName(GLString);
            String hlaString = "";
            if(glsElement.item(0) != null){
                hlaString = glsElement.item(0).getTextContent().trim();
            }
            if(hlaString.contains("+")){
                String[] hlaList = hlaString.split("\\+");
                pw.print(hlaList[i%2]);
            }else {
                pw.print(hlaString);
            }
            pw.print(",");
            pw.print(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().length());
            pw.print(",");
            pw.println(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().trim());
            if(isShort(seq.getElementsByTagName(SEQUENCE).item(0).getTextContent().length())){
                count++;
                return;
            }
        }

    }


    public String getQa(){
        NumberFormat formatter = new DecimalFormat("#0.0000");
        return formatter.format(count/total *100) + "%";
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
