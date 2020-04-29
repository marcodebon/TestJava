package it.marcodebon.testjava;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.marcodebon.testjava.beans.FileFIS;
import it.marcodebon.testjava.beans.Protocollo;
import it.marcodebon.testjava.beans.Tipologia;

public class Main {

	final static Logger logger = LogManager.getLogger(Main.class);
	private static List<String> errorList;
	private static Date mainDate;

	public static void main(String[] args) {

		if ( checkArgs(args) ) {
			long mainStartTime = System.currentTimeMillis();
			mainDate = new Date(mainStartTime);
	
			logger.info("INIZIO BATCH");
			errorList = new ArrayList<String>();
	
			logger.trace("Lettura configurazione");
			try {
				Settings.initialize();
			} catch (IOException e) {
				logger.fatal("File di configurazione non trovato o impossibile da aprire");
				System.exit(-1);
			}
			
			logger.debug("Connessione a database");
			if (Database.init()) {
				elaborazioneTask(args[0]);
				logger.debug("Chiusura connessione a database");
				if (!Database.close())
					errorList.add("Errore nella chiusura della connessione a Database.");
			} else
				errorList.add("Errore nella connessione a Database.");
	
			if (!errorList.isEmpty()) {
				logger.info("Invio report con errori:");
				String message = "Report accorpamento file FIS<br/>";
				for (String error : errorList) {
					logger.info("  " + error);
					message += error + "<br/>";
				}
				Utility.sendEmail("Report accorpamento file FIS", message, mainDate);
			}
			
			logger.info("FINE BATCH in {}", Utility.elapsedTime(mainStartTime));
		}
	}
	
	private static boolean checkArgs(String[] args) {
		if (args.length != 1) {
			logger.fatal("Parametro in linea comando assente: specificare un identificatore di raggruppamento Tipologie di FileFIS da elaborare.");
			return false;
		}
		else {
			logger.info("Raggruppamento Tipologie di FileFIS da elaborare: {}", args[0]);
		}
		return true;
	}
	
	private static void elaborazioneTask(String raggruppamento) {
		logger.debug("Inizio elaborazione");
		
		logger.debug("Ripristino esecuzione precedente");
		if (!Database.fileFISReset()) {
			errorList.add("Errore nel ripristino dell'elaborazione precedente.");
			return;
		}
		
		logger.debug("Pulizia cartella temporanea");
		Utility.clearDirectory(new File(Settings.Dir.temp));
		
		logger.debug("Caricamento tipologie");
		List<Tipologia> tipologie = Database.listaTipologie(raggruppamento);
		if (tipologie.size() <= 0) {
			logger.warn("[listaTipologie] - Nessuna tipologia FileFIS trovata per raggruppamento: {}", raggruppamento);
			errorList.add(String.format("Warning: Nessuna tipologia FileFIS trovata per raggruppamento \"%s\"",raggruppamento));
		}
			

		for (Tipologia tipologia : tipologie) {
			//Date date;
			//String protocollo = null;
			logger.info("Elaborazione tipologia: {}", tipologia.getDescrizione());
			
			String nomeFileFIS = tipologia.getNomeFileFIS();
			logger.debug("  nomeFileFIS: {}", nomeFileFIS);
			if (Utility.isStringEmpty(nomeFileFIS)) {
				logger.error("Campo nomeFileFIS vuoto");
				errorList.add("Errore: nome file FIS vuoto.");
				continue;
			}
			
			String outboundFolder = tipologia.getOutboundFolder();
			logger.debug("  outDir: {}", outboundFolder);
			if (Utility.isValidDir(outboundFolder) == null) {
				logger.error("Campo outboundFolder vuoto o non valido");
				errorList.add("Errore: outbound folder non valida.");
				continue;
			}
			
			logger.trace("  Caricamento lista file FIS...");
			List<FileFIS> fileFIS = Database.listaFileFIS(tipologia.getId());
			if (fileFIS.isEmpty()) {
				logger.trace("  ...lista vuota.");
				continue;
			}

			nomeFileFIS = Utility.replaceDatePattern(nomeFileFIS, mainDate);
			elaboraTipologia(fileFIS, nomeFileFIS, tipologia, outboundFolder);

			logger.debug("  finalizzazione file FIS");
			if (!Database.finalizzaFileFIS(tipologia.getId())) {
				logger.error("Errore nella procedura di finalizzazione");
				errorList.add("Errore nella procedura di finalizzazione.");
			}
		}

		logger.debug("Fine elaborazione");
	}
	
	private static String makeProtocollo(Tipologia tipologia, String sistemaAlimentante) throws Exception {
		Date date;
		String protocollo = null;
		String source = "";
		logger.debug("  Generazione nuovo protocollo...");
		Protocollo proto = Database.newProtocollo();
		String patternProtocollo = tipologia.getPatternProtocollo();
		if (proto == null) {
			logger.error("Errore generazione nuovo protocollo");
			errorList.add("Errore nella generazione di un nuovo numero di protocollo.");
			throw new Exception();
		}

		SimpleDateFormat sdfDataOra = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			date = sdfDataOra.parse(proto.getDataPresentazione());
		} catch (ParseException e) {
			logger.error("Errore nel parse della data di presentazione del protocollo (usata la data di elaborazione)");
			date = mainDate;
		}
		if("PREFISI".equals(sistemaAlimentante)) {
		    source = "02";
		    logger.trace("  Source: {}", source);
		}else {
		    source = "01";
		    logger.trace("  Source: {}", source);
		}
		protocollo = patternProtocollo.replaceAll("<source>", source);
		protocollo = protocollo.replaceAll("<proto>", proto.getProtocollo());
		protocollo = Utility.replaceDatePattern(protocollo, date) + Utility.repeat(' ', tipologia.getBlankFiller());
		logger.info("  Protocollo: {}", protocollo);		
		return protocollo;
	}
	
	private static void elaboraTipologia(List<FileFIS> fileFIS, String outFile, Tipologia tipologia, String destDir) {
		String protocollo = null;
		File tmpFileUNIX = new File(Settings.Dir.temp, outFile+"-UNIX");
		File tmpFileDOS = new File(Settings.Dir.temp, outFile+"-DOS");
		for (FileFIS file : fileFIS) {
			long id = file.getId();
			String nomeFile = file.getNomeFile();
			logger.info("  file FIS: id={}, {}", id, nomeFile);
			if (Utility.isStringEmpty(nomeFile)) {
				logger.error("Campo nomeFile vuoto");
				errorList.add("Errore: nome file vuoto.");
				logger.info("  impostazione flagElaborazione = 'E'");
				if (!Database.aggiornaFlagElaborazione(id, "E", null)) {
					logger.error("Errore nell'impostazione del flag elaborazione (E)");
					errorList.add("Errore nell'impostazione del flag elaborazione (E).");
				}
				continue;
			}
			
			byte[] fileContent = file.getFileContent();
			if (null == fileContent || fileContent.length == 0) {
				logger.error("Campo fileContent vuoto");
				errorList.add("Errore: contenuto del file vuoto.");
				logger.info("  impostazione flagElaborazione = 'E'");
				if (!Database.aggiornaFlagElaborazione(id, "E", null)) {
					logger.error("Errore nell'impostazione del flag elaborazione (E)");
					errorList.add("Errore nell'impostazione del flag elaborazione (E).");
				}
				continue;
			}
			
			String sistemaAlimentante = file.getSistemaAlimentante();
			
			logger.trace("  contenuto: {} bytes", fileContent.length);
			try {
				BufferedWriter bufWriter =  new BufferedWriter(new FileWriter(tmpFileUNIX, true));
				if ( ! Utility.isStringEmpty(tipologia.getPatternProtocollo()) ) {
					try {
						protocollo = makeProtocollo(tipologia, sistemaAlimentante);
					}
					catch(Exception e) {
					    logger.error("Errore: {}", e.getMessage());
						//l'errore � gi� censito in generateProtocollo
						continue;
					}
				}
				
				if (protocollo == null) {
					logger.trace("  concatenazione semplice (protocollo nullo)");
					bufWriter.write(new String(fileContent).toUpperCase()); 
				} else {
					logger.trace("  concatenazione con protocollo");
					InputStream is = new ByteArrayInputStream(fileContent);
					BufferedReader bufReader = new BufferedReader(new InputStreamReader(is));
		            String line = null;
		            while ((line = bufReader.readLine()) != null) {
		            	line = line.toUpperCase();
		            	bufWriter.write(line.concat(protocollo));
						bufWriter.newLine();
					}
				}
				bufWriter.flush();
				bufWriter.close();
			} catch (IOException e1) {
				logger.error("Errore nella concatenazione");
				errorList.add("Errore nella concatenazione.");
				continue;
			}

			logger.debug("  impostazione flagElaborazione = 'A'");
			if (!Database.aggiornaFlagElaborazione(id, "A", protocollo)) {
				logger.error("Errore nell'impostazione del flag elaborazione (A)");
				errorList.add("Errore nell'impostazione del flag elaborazione (A).");
			}
		}
		
		if (tmpFileUNIX.exists()) {
			try {
				BufferedReader read = new BufferedReader(new FileReader(tmpFileUNIX));
				FileWriter fwri = new FileWriter(tmpFileDOS);
				String line;
				while ((line = read.readLine()) != null) {
				    fwri.write(line+"\r\n"); // if you want the system line delimiter use the System property for that.
				}
				fwri.flush();
				fwri.close();
				read.close();
								
				if (tmpFileUNIX.exists()) {
					tmpFileUNIX.delete();
				}				
			} catch (FileNotFoundException e) {
				logger.error("Errore apertura file UNIX: FileNotFoundException - " + e.getMessage());
				errorList.add("Errore apertura file UNIX.");
			} catch (IOException e) {
				logger.error("Errore conversione file in formato UNIX a file in formato DOS: IOException - " + e.getMessage());
				errorList.add("Errore apertura file UNIX.");
			}
		}
		else {
			logger.error("File in formato UNIX {} non trovato",tmpFileUNIX.getAbsolutePath());
			errorList.add("File in formato UNIX " + tmpFileUNIX.getAbsolutePath() + " non trovato");
		}
		
		if ( tmpFileDOS.exists() ) {
			File destFile = new File(destDir, outFile);
			logger.debug("  spostamento file {} in {}",tmpFileDOS.getAbsolutePath(), destFile.getAbsolutePath());
			try {
				FileUtils.copyFile(tmpFileDOS, destFile);
				FileUtils.deleteQuietly(tmpFileDOS);
			} catch (IOException e) {
				logger.error("Errore nello spostamento del file accorpato da {} a {}",tmpFileDOS.getAbsolutePath(), destFile.getAbsolutePath());
				errorList.add("Errore nello spostamento del file accorpato da " + tmpFileDOS.getAbsolutePath() + " a " + destFile.getAbsolutePath());
			}
		}
		else {
			logger.error("File in formato DOS {} non trovato",tmpFileDOS.getAbsolutePath());
			errorList.add("File in formato DOS " + tmpFileDOS.getAbsolutePath() + " non trovato");			
		}
	}
}
