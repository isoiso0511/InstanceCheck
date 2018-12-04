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

class XmlReader{

	private ObjectModel object = new ObjectModel();//xmlのオブジェクトを保存先

	private InstModel inst;
	private LinkModel link;
	private AttributeModel attri;

	private int instCount = 0;
	private int linkCount = 0;

	//test用
	/*
	public static void main(String[] args) throws Exception{
		XmlReader xml = new XmlReader();
	}
	*/

	XmlReader(){
		readDom();
		//readSax();
	}

	private void readDom() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			//Document doc = builder.parse(new File("test.xml"));//xml読み込み
			Document doc = builder.parse(new File("C:/work/InstCheck/Instance-Dev/test.xml"));//astahのtest用
			Element element = doc.getDocumentElement();
			//System.out.println("Node: " + element.getNodeName());//ルートノードの取得
			//System.out.println("code: " + element.getAttribute("id"));//属性値取得
			NodeList nodeList = element.getChildNodes();
			for(int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					Element ele = (Element)node;
					if(node.getNodeName().equals("scenario")){//scenarioノード取得
						this.object.setScenario(ele.getFirstChild().getNodeValue());
					}else if(node.getNodeName().equals("inst")) {//instノード取得
						//System.out.println("--instance--");
						//System.out.println(ele.getNodeName() +":"+ele.getAttribute("id"));
						inst = new InstModel();
						inst.setName(ele.getFirstChild().getNodeValue());
						inst.setInstId(Integer.parseInt(ele.getAttribute("id")));//string→int変換
						object.addInstList(inst);
						//instの子ノード
						NodeList instChild = node.getChildNodes();
						for(int j=0; j < instChild.getLength(); j++) {//instの子ノードを取得
							Node instNode = instChild.item(j);
							if(instNode.getNodeType() == Node.ELEMENT_NODE) {
								ele = (Element)instNode;
								if(instNode.getNodeName().equals("class")) {
									//System.out.println(ele.getNodeName() + ": " + ele.getFirstChild().getNodeValue());
									inst.setClassName(ele.getFirstChild().getNodeValue());

								}else if(instNode.getNodeName().equals("attribute")) {
									attri = new AttributeModel();
									attri.setName(ele.getAttribute("name"));//属性名の取得
									attri.setValue(ele.getFirstChild().getNodeValue());//属性値の取得
									inst.addAttriList(attri);//instのattributelistに追加
								}
							}
						}
						object.addInstList(inst);
						instCount++;

					}else if(ele.getNodeName().equals("link")) {//linkノード取得
						link = new LinkModel();
						//linkの子ノード取得
						NodeList linkChild = node.getChildNodes();
						for(int j=0 ; j < linkChild.getLength(); j++) {//linkの子ノード取得
							Node linkNode = linkChild.item(j);
							if(linkNode.getNodeType() == Node.ELEMENT_NODE) {
								ele = (Element)linkNode;
								if(linkNode.getNodeName().equals("name")) {
									//System.out.println(ele.getNodeName() + ": " + ele.getFirstChild().getNodeValue());
									link.setLinkName(ele.getFirstChild().getNodeValue());

								}else if(linkNode.getNodeName().equals("start")) {
									//System.out.println(ele.getNodeName() + ": " + ele.getFirstChild().getNodeValue());
									link.setLinkStart(Integer.parseInt(ele.getFirstChild().getNodeValue()));


								}else if(linkNode.getNodeName().equals("end")) {
									//System.out.println(ele.getNodeName() + ": " + ele.getFirstChild().getNodeValue());
									link.setLinkEnd(Integer.parseInt(ele.getFirstChild().getNodeValue()));
								}
							}
						}
						object.addLinkList(link);
						linkCount++;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ObjectModel getObject() {
		return object;
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