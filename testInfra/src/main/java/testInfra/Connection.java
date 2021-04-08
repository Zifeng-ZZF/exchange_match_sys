package testInfra;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Connection extends Thread{
    int id;
    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private InputStreamReader sReader;
    Random random;

    public Connection(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        printWriter = new PrintWriter(socket.getOutputStream(), true);
        sReader = new InputStreamReader(socket.getInputStream());
        bufferedReader = new BufferedReader(sReader);
    }

    public Connection(String hostname, int port, int id, Random random) throws IOException {
        socket = new Socket(hostname, port);
        printWriter = new PrintWriter(socket.getOutputStream(), true);
        sReader = new InputStreamReader(socket.getInputStream());
        bufferedReader = new BufferedReader(sReader);
        this.id = id;
        this.random = random;
    }

    public void sendMsg(String msg) {
        int len = msg.length();
        printWriter.println(len);
        printWriter.println(msg);
    }

    public String recevMsg() throws IOException {
        String msg = bufferedReader.readLine();
        // System.out.println("Receive results:\n" + msg);
        return msg;
    }
    @Override
    public void run()  {
        try{
            randomGen(random);
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
//    @Override
//    public void start(){
//
//    }

    public void randomGen(Random r) throws IOException{
        String xml;
        int opera = r.nextInt(3);
        System.out.println("opera: "+opera);
        if(opera == 0){
            //account id [0,2]
            int accountId = r.nextInt(3);
            //symbol id [0,2]
            int symbol = r.nextInt(3);
            //amount [500,-500]
            int amount = r.nextInt(1000)-500;
            //limit [1,200]
            int limit = r.nextInt(200)+1;
            xml = constructOrderXML(Integer.toString(accountId+1),Integer.toString(symbol+1),
                    Integer.toString(amount),Integer.toString(limit));
        } else {
            int accountId = r.nextInt(3);
            //transac id [1,100]
            int transacId = r.nextInt(100)+1;
            xml = constructQueryCancelXML(opera,Integer.toString(accountId+1),Integer.toString(transacId));
        }
        System.out.println(xml);
        this.sendMsg(xml);
        String res = this.recevMsg();
        System.out.println(res);
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
            if(i == 2){
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

}
