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

public class XmlReader{
	public static void main(String[] args) throws Exception{
		XmlReader xml = new XmlReader();
	}

	XmlReader(){
		readDom();
		//readSax();
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
					if(node.getNodeName().equals("inst")) {//ノード名の取得
						System.out.println("--instance--");
						System.out.println(ele.getNodeName() +":"+ele.getAttribute("id"));

						//objectの子ノードを出力
						NodeList instChild = node.getChildNodes();
						for(int j=0 ; j < instChild.getLength(); j++) {
							Node instNode = instChild.item(j);
							if(instNode.getNodeType() == Node.ELEMENT_NODE) {
								ele = (Element)instNode;
								if(instNode.getNodeName().equals("class")) {
									System.out.println(ele.getNodeName() + ": " + ele.getFirstChild().getNodeValue());
								}else if(instNode.getNodeName().equals("attribute")) {
									System.out.println(ele.getNodeName() + ": " + ele.getFirstChild().getNodeValue()+":"+ele.getAttribute("name"));
								}
							}
						}
						System.out.println("------------");


					}else if(ele.getNodeName().equals("link")) {
						System.out.println("--link--");
						System.out.println(ele.getNodeName());

						//linkの子ノードを出力
						NodeList linkChild = node.getChildNodes();
						for(int j=0 ; j < linkChild.getLength(); j++) {
							Node linkNode = linkChild.item(j);
							if(linkNode.getNodeType() == Node.ELEMENT_NODE) {
								ele = (Element)linkNode;
								if(linkNode.getNodeName().equals("name")) {
									System.out.println(ele.getNodeName() + ": " + ele.getFirstChild().getNodeValue());
								}else if(linkNode.getNodeName().equals("start")) {
									System.out.println(ele.getNodeName() + ": " + ele.getFirstChild().getNodeValue());
								}else if(linkNode.getNodeName().equals("end")) {
									System.out.println(ele.getNodeName() + ": " + ele.getFirstChild().getNodeValue());
								}
							}
						}
						System.out.println("------------");
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
			parser.parse(new File("test.xml"), new SaxReader());
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}

class SaxReader extends DefaultHandler{
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