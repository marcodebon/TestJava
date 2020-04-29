package it.marcodebon.testjava.beans;

public class SimpleResult extends GenericBean<SimpleResult> {

	// properties
	private String codErr;
	private String descErr;

	// getters & setters
	public String getCodErr() {
		return codErr;
	}
	public void setCodErr(String codErr) {
		this.codErr = codErr;
	}
	
	public String getDescErr() {
		return descErr;
	}
	public void setDescErr(String descErr) {
		this.descErr = descErr;
	}
}
