package Slab.astah.aiso;

import java.util.ArrayList;
import java.util.List;

class LinkModel{
	private String name;//リンクの名前
	private int linkStart;//リンク先
	private int linkEnd;
	
	//リンクの端の2つのインスタンスのIDを保持
	private List<String> linkPointList = new ArrayList<String>();

	public void addLinkPoint(String pointId){
		linkPointList.add(pointId);
	}
	public String getName() {
		return this.name;
	}

	public void setLinkName(String _name) {
		this.name = _name;
	}

	public int getLinkStart() {
		return this.linkStart;
	}

	public void setLinkStart(int _number) {
		this.linkStart = _number;
	}

	public int getLinkEnd() {
		return this.linkEnd;
	}

	public void setLinkEnd(int _number) {
		this.linkEnd = _number;
	}
}