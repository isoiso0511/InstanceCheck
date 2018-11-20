package Slab.astah.aiso;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
//saxの使用
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//domの使用
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ReadXml{
	public static void main(String[] args) throws Exception{
		ReadXml xml = new ReadXml();
	}
	ReadXml(){
		readDom();
		readSax();
	}

	private void readDom() {
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

	private void readSax() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File("test.xml"), new SaxXml());
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}

class SaxXml extends DefaultHandler{
	public void startDocument() {
		System.out.println("読み込み開始");
	}

	public void endDocument() {
		System.out.println("読み込み終了");
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		System.out.println("Node: " + qName + " 開始");
		if(qName.equals("info")) {
			System.out.println(attributes.getQName(0) + ": " + attributes.getValue(0));
		}
	}

	public void endElement(String uri, String localName, String qName) {
		System.out.println("Node: " + qName + " 終了");
	}

	public void characters(char[] ch, int start, int length) {
		System.out.println(new String(ch, start, length));
	}
}