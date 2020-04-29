package it.marcodebon.testjava.beans;

public class FileFIS extends GenericBean<Protocollo> {

	// properties
	private long id;
	private String sistemaAlimentante;
	private String nomeFile;
	private byte[] fileContent;
	private String codErr;
	private String descErr;

	// getters & setters
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String getNomeFile() {
		return nomeFile;
	}
	public void setNomeFile(String nomeFile) {
		this.nomeFile = nomeFile;
	}

	public byte[] getFileContent() {
		return fileContent;
	}
	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}

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
    public String getSistemaAlimentante() {
        return sistemaAlimentante;
    }
    public void setSistemaAlimentante(String sistemaAlimentante) {
        this.sistemaAlimentante = sistemaAlimentante;
    }
}
