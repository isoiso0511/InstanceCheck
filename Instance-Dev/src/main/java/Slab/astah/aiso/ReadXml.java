package Slab.astah.aiso;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//DOMの使用

public class ReadXml{
	public static void main(String[] args) throws Exception{
		ReadXml xml = new ReadXml();
	}
	ReadXml(){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File("test.xml"));//xml読み込み
			Element element = doc.getDocumentElement();
			System.out.println("Node: " + element.getNodeName());//ノードの取得
			//System.out.println("code: " + element.getAttribute("id"));//属性値取得
			NodeList nodeList = element.getChildNodes();
			for(int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					Element ele = (Element)node;
					if(ele.getNodeName().equals("object")) {//ノード名の取得
						System.out.println( ele.getAttribute("id")+":"+ele.getNodeName() + ": " + ele.getFirstChild().getNodeValue());
					}else if(ele.getNodeName().equals("link")) {
						System.out.println(ele.getNodeName() + ": " + ele.getTextContent());
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}