package Slab.astah.aiso;

class ObjectModel{
	private int objectId;
	private String scenario;
	private InstModel[] inst;
	private LinkModel link;

	public InstModel getInst(int instNumber){
		return this.inst[instNumber];
	}

	public void setInst(int instNumber,InstModel _inst){
		this.inst[instNumber] = _inst;
	}

	public LinkModel getLink(){
		return this.link;
	}

	public void setLink(LinkModel _link){
		this.link = _link;
	}

	public String getScenario(){
		return this.scenario;
	}

	public void setScenario(String _scenario){
		this.scenario = _scenario;
	}

	public ObjectModel getObject(){
		return this;
	}
}