package Slab.astah.aiso;

import java.util.ArrayList;
import java.util.List;

class ObjectModel{
	private int objectId;
	private String scenario = "test";
	private List<InstModel> instList = new ArrayList<InstModel>();
	private List<LinkModel> linkList = new ArrayList<LinkModel>();

	public InstModel getInst(int i){
		return this.instList.get(i);
	}

	public void setInst(int i,InstModel _inst){
		this.instList.set(i,_inst);
	}

	public void addInstList(InstModel _inst) {
		this.instList.add(_inst);
	}

	public List<InstModel> getInstList(){
		return instList;
	}

	public LinkModel getLink(int i){
		return this.linkList.get(i);
	}

	public void setLink(int i,LinkModel _link){
		this.linkList.set(i,_link);
	}

	public void addLinkList(LinkModel _link) {
		this.linkList.add(_link);
	}

	public List<LinkModel> getLinkList(){
		return linkList;
	}

	public String getScenario(){
		return this.scenario;
	}

	public void setScenario(String _scenario){
		//_scenario = _scenario.replace("\n","");
		_scenario = _scenario.replace("\t","");
		_scenario = _scenario.replace(" ","");
		this.scenario = _scenario;
	}

	public ObjectModel getObject(){
		return this;
	}
}