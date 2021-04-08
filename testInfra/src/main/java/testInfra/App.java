package testInfra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class App {
    // static String host = "127.0.0.1";
    static String host = "67.159.89.186";
    public static void main(String[] args) throws IOException,InterruptedException {
        if (args[0].equals("1")) {
            testFunction();
        } else if (args[0].equals("2")){
            String numStr = args[1];
            int num = Integer.parseInt(numStr);
            System.out.println("performing " + num + " requests");
            testScalibility(num);
        } else {
            System.out.println("Wrong argument:" + args[0] + ".");
        }
    }
    static void testFunction()throws IOException{
        String xml = constructCreateXML();
        execut(xml);
        String xmlerror = constructCreateXML("1","10000","SPY","-1000");
        execut(xmlerror);
       String order1 = constructOrderXML("1234567","SPY","300","125");
       execut(order1);
       String order2 = constructOrderXML("123456","SPY","-100","130");
       execut(order2);
    //    String cancel1 = constructQueryCancelXML(1,"4","12345");
    //    execut(cancel1);
    //    String trans = constructQueryAndCancelXML("3","12345");
    //    execut(trans);

       String order3 = constructOrderXML("1234567","SPY","200","127");
       execut(order3);
       String order4 = constructOrderXML("123456","SPY","-500","128");
       execut(order4);
       String order5 = constructOrderXML("123456","SPY","-200","140");
       execut(order5);
       String order6 = constructOrderXML("1234567","SPY","400","125");
       execut(order6);
        String ordererror1 = constructOrderXML("1234567","SPY","100000","125");
        execut(ordererror1);
        String ordererror2 = constructOrderXML("123456","SPY","-100000","125");
        execut(ordererror2);
       String query5 = constructQueryCancelXML(2,"5","123456");
       execut(query5);

        String order7 = constructOrderXML("123456","SPY","-400","124");
        execut(order7);
        String query7 = constructQueryCancelXML(2,"7","123456");
        execut(query7);
        String query1 = constructQueryCancelXML(2,"1","1234567");
        execut(query1);
        String query3 = constructQueryCancelXML(2,"3","1234567");
        execut(query3);

        String order8 = constructOrderXML("1234567","SPY","300","129");
        execut(order8);
        String cancel4 = constructQueryCancelXML(1,"4","123456");
        execut(cancel4);
        String query4 = constructQueryCancelXML(2,"4","123456");
        execut(query4);
        String query8 = constructQueryCancelXML(2,"8","1234567");
        execut(query8);

    }

    static void execut(String xml) throws IOException {
         Connection connection = new Connection(host, 12345);
//        Connection connection = new Connection("127.0.0.1", 12345);
//        System.out.println("Sending xml:\n" + xml);
        connection.sendMsg(xml);
        String recv = connection.recevMsg();
        System.out.println("receive xml:\n" + recv);
    }

    static String constructCreateXML() {
        String ans = "";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("create");
            doc.appendChild(root);

            //create account
            Element accountNode = doc.createElement("account");
            accountNode.setAttribute("id", "123456");
            accountNode.setAttribute("balance", "1000000");
            root.appendChild(accountNode);
            //create symbol
            Element symbolNode = doc.createElement("symbol");
            symbolNode.setAttribute("sym", "SPY");
            root.appendChild(symbolNode);

            Element subAccount = doc.createElement("account");
            subAccount.setAttribute("id", "123456");
            subAccount.appendChild(doc.createTextNode("10000"));
            symbolNode.appendChild(subAccount);

            //create the same symbol to the same account
            Element symbolNode2 = doc.createElement("symbol");
            symbolNode2.setAttribute("sym", "SPY");
            root.appendChild(symbolNode2);

            Element subAccount2 = doc.createElement("account");
            subAccount2.setAttribute("id", "123456");
            subAccount2.appendChild(doc.createTextNode("1000"));
            symbolNode2.appendChild(subAccount2);
           // create the same account
            Element accountNode1 = doc.createElement("account");
            accountNode1.setAttribute("id", "123456");
            accountNode1.setAttribute("balance", "1000000");
            root.appendChild(accountNode1);

            //create symbol with nonexist account
            Element symbolNode1 = doc.createElement("symbol");
            symbolNode1.setAttribute("sym", "SPY");
            root.appendChild(symbolNode1);

            Element subAccount1 = doc.createElement("account");
            subAccount1.setAttribute("id", "12345");
            subAccount1.appendChild(doc.createTextNode("1000"));
            symbolNode1.appendChild(subAccount1);
            //create another account
            Element accountNode2 = doc.createElement("account");
            accountNode2.setAttribute("id", "1234567");
            accountNode2.setAttribute("balance", "1000000");
            root.appendChild(accountNode2);

            TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            StreamResult stringResult = new StreamResult(bytes);
            transformer.transform(source, stringResult);
            ans = bytes.toString();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (DOMException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return ans;
    }

    static String constructOrderXML(String accountId, String symbol,String amount,String limit){
        String ans = "";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("transactions");
            root.setAttribute("id", accountId);
            doc.appendChild(root);

            Element accountNode = doc.createElement("order");
            accountNode.setAttribute("sym", symbol);
            accountNode.setAttribute("amount", amount);
            accountNode.setAttribute("limit", limit);
            root.appendChild(accountNode);

            TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            StreamResult stringResult = new StreamResult(bytes);
            transformer.transform(source, stringResult);
            ans = bytes.toString();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (DOMException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return ans;
    }

    static String constructQueryCancelXML(int i,String trasacId, String accountId){
        String ans = "";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("transactions");
            root.setAttribute("id", accountId);
            doc.appendChild(root);
            if(i == 1){
                Element accountNode = doc.createElement("cancel");
                accountNode.setAttribute("id", trasacId);
                root.appendChild(accountNode);
            } else {
                Element accountNode = doc.createElement("query");
                accountNode.setAttribute("id", trasacId);
                root.appendChild(accountNode);
            }

            TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            StreamResult stringResult = new StreamResult(bytes);
            transformer.transform(source, stringResult);
            ans = bytes.toString();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (DOMException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return ans;
    }

    /**
     * generate request for create symbol account and symbol
     * @param id
     * @param balance
     * @param symbol
     * @param symAmount
     * @return
     */
    static String constructCreateXML(String id, String balance,String symbol,String symAmount){
        String ans = "";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("create");
            doc.appendChild(root);

            Element accountNode = doc.createElement("account");
            accountNode.setAttribute("id", id);
            accountNode.setAttribute("balance", balance);
            root.appendChild(accountNode);
            Element symbolNode = doc.createElement("symbol");
            symbolNode.setAttribute("sym", symbol);
            root.appendChild(symbolNode);

            Element subAccount = doc.createElement("account");
            subAccount.setAttribute("id", id);
            subAccount.appendChild(doc.createTextNode(symAmount));
            symbolNode.appendChild(subAccount);
            TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            StreamResult stringResult = new StreamResult(bytes);
            transformer.transform(source, stringResult);
            ans = bytes.toString();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (DOMException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return ans;
    }
    static String constructQueryAndCancelXML(String trasacId, String accountId){
        String ans = "";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("transactions");
            root.setAttribute("id", accountId);
            doc.appendChild(root);
            Element accountNode1 = doc.createElement("query");
            accountNode1.setAttribute("id", trasacId);
            root.appendChild(accountNode1);

            Element accountNode = doc.createElement("cancel");
                accountNode.setAttribute("id", trasacId);
                root.appendChild(accountNode);

            TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            StreamResult stringResult = new StreamResult(bytes);
            transformer.transform(source, stringResult);
            ans = bytes.toString();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (DOMException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return ans;
    }

    static void execut2(String xml) throws IOException{
        // Connection connection = new Connection("67.159.89.186", 12345);
        Connection connection = new Connection(host, 12345);
        connection.sendMsg(xml);
        connection.recevMsg();
    }

    static void testScalibility(int num) throws IOException, InterruptedException{
        String account1 = constructCreateXML("1","1000000","1","10000");
        execut2(account1);
        String account2 = constructCreateXML("2","1000000","2","10000");
        execut2(account2);
        String account3 = constructCreateXML("3","1000000","3","10000");
        execut2(account3);
        Random r = new Random();

        LocalDateTime before = LocalDateTime.now();
        System.out.println(before.toString());

        ArrayList<Connection> myConnections = new ArrayList<>();
        int numConn = num;
        for (int i = 0; i < numConn; i++) { // spawning threads
            Connection connection = new Connection(host, 12345, i, r);
            // Connection connection = new Connection("127.0.0.1", 12345, i, r);
            myConnections.add(connection);
            connection.start();
        }
        for (int i = 0; i < numConn; ++i) { // joinning threads
            myConnections.get(i).join();
        }

        LocalDateTime after = LocalDateTime.now();
        System.out.println(after.toString());

        Duration duration = Duration.between(before, after); // calc duration
        long milliseconds = duration.getNano() / 1000 / 1000;
        long secs = duration.getSeconds();
//        System.out.println("Execution time: " + milliseconds + "ms");
        System.out.println("Execution time: " + secs+"s, "+milliseconds+"ms");
    }
}


