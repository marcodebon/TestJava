package it.marcodebon.testjava.beans;

public class Tipologia extends GenericBean<Tipologia> {

	// properties
	private long id;
	private String descrizione;
	private String nomeFileFIS;
	private String outboundFolder;
	private String patternProtocollo;
	private int blankFiller;

	// getters & setters
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String getDescrizione() {
		return descrizione;
	}
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getNomeFileFIS() {
		return nomeFileFIS;
	}
	public void setNomeFileFIS(String nomeFileFIS) {
		this.nomeFileFIS = nomeFileFIS;
	}

	public String getOutboundFolder() {
		return outboundFolder;
	}
	public void setOutboundFolder(String outboundFolder) {
		this.outboundFolder = outboundFolder;
	}

	public String getPatternProtocollo() {
		return patternProtocollo;
	}
	public void setPatternProtocollo(String patternProtocollo) {
		this.patternProtocollo = patternProtocollo;
	}

	public int getBlankFiller() {
		return blankFiller;
	}
	public void setBlankFiller(int blankFiller) {
		this.blankFiller = blankFiller;
	}
}
