package Slab.astah.aiso;
class LinkModel{
	private String linkName;//リンクの名前
	private int linkStart;//リンク先
	private int linkEnd;//


	public String getLinkName() {
		return this.linkName;
	}

	public void setLinkName(String _name) {
		this.linkName = _name;
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