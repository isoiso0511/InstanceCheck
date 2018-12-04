package Slab.astah.aiso;

import java.util.ArrayList;
import java.util.List;

class InstModel{
	private String instName;//instance名前
	private int instId;//instanceのid
	private String className;//instanceのクラス名

	private List<AttributeModel> attriList = new ArrayList<AttributeModel>();

	private String linkName;//リンクの名前
	private int linkPoint;//リンク先


	public InstModel getInst(){
		return this;
	}

	public String getName() {
		return this.instName;
	}

	public void setName(String _name) {
		this.instName = _name;
	}

	public void setInstId(int _id) {
		this.instId = _id;
	}

	public int getInstId() {
		return this.instId;
	}

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String _name) {
		this.className = _name;
	}

	public void addAttriList(AttributeModel _attri) {
		this.attriList.add(_attri);
	}

	public List<AttributeModel> getAttriList(){
		return attriList;
	}

	public AttributeModel getAttribute(int i){
		return attriList.get(i);
	}

	public String getLinkName() {
		return this.linkName;
	}

	public void setLinkName(String _name) {
		this.linkName = _name;
	}

	public int getLinkPoint() {
		return this.linkPoint;
	}

	public void setLinkPoint(int _number) {
		this.linkPoint = _number;
	}
}