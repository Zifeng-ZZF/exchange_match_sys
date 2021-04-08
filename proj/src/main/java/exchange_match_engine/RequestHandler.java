package exchange_match_engine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class RequestHandler extends Thread {

    public Request startHandling(String tobeHandled) throws IOException {
        Request request = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(tobeHandled.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();
            String type = doc.getDocumentElement().getNodeName();
            //nList all account and symbol
            NodeList nList = doc.getDocumentElement().getChildNodes();
            if (type == "create"){
                request = parseCreate(nList);
            }
            else if (type == "transactions"){
                String accountIdStr = doc.getDocumentElement().getAttribute("id");
                request = parseTransaction(nList, Integer.parseInt(accountIdStr));
            }
            else {
                return null;
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        }
        return request;
    }

    public Request parseCreate(NodeList nList){
        Create create = new Create();
        for(int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if(nNode.getNodeName() == "account"){
                parseAccount(nNode, create);
            }
            if(nNode.getNodeName() == "symbol"){
                parseSymbol(nNode, create);
            }
        }
        return create;
    }

    public void parseAccount(Node nNode, Create request){
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            String id = eElement.getAttribute("id");
            String balance = eElement.getAttribute("balance");
//            System.out.println("id + balance"+ id +balance);

            AccountElement account = new AccountElement(Integer.parseInt(id), Double.parseDouble(balance));
            request.requestElements.add(account);
        }
    }

    public void parseSymbol(Node nNode, Create request){
        String symName = ((Element)nNode).getAttribute("sym");
        NodeList accountList = nNode.getChildNodes();
        for(int j = 0; j < accountList.getLength();j++){
            Node accountNode = accountList.item(j);
            if(accountNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) accountNode;
                String id = eElement.getAttribute("id");
                String symAmount = eElement.getTextContent();
                // add tuple to request

                SymbElement symbolEntry = new SymbElement(symName, Integer.parseInt(id), Integer.parseInt(symAmount));
                request.requestElements.add(symbolEntry);
            }
        }
    }

    public Request parseTransaction(NodeList nList, int accountID) {
        Transaction transaction = new Transaction(accountID);
        for(int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if(nNode.getNodeName() == "order"){
                if(nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String sym = eElement.getAttribute("sym");
                    String amount = eElement.getAttribute("amount");
                    String limit = eElement.getAttribute("limit");
                    Order order = new Order(Double.parseDouble(amount), Double.parseDouble(limit), sym, accountID);
                    transaction.elements.add(order);
                }
            }
            if(nNode.getNodeName() == "query"){
                if(nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String id = eElement.getAttribute("id");
                    QueryElement query = new QueryElement(Integer.parseInt(id));
                    transaction.elements.add(query);
                }
            }
            if(nNode.getNodeName() == "cancel"){
                if(nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String id = eElement.getAttribute("id");
                    CancelElement cancel = new CancelElement(Integer.parseInt(id));
                    transaction.elements.add(cancel);
                }
            }
        }
        return transaction;
    }
}
