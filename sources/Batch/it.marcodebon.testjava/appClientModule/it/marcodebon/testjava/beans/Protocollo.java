package it.marcodebon.testjava.beans;

public class Protocollo extends GenericBean<Protocollo> {

	// properties
	private String protocollo;
	private String dataPresentazione;

	// getters & setters
	public String getProtocollo() {
		return protocollo;
	}
	public void setProtocollo(String protocollo) {
		this.protocollo = protocollo;
	}

	public String getDataPresentazione() {
		return dataPresentazione;
	}
	public void setDataPresentazione(String dataPresentazione) {
		this.dataPresentazione = dataPresentazione;
	}
}
