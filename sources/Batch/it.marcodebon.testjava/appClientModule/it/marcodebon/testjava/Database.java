package it.marcodebon.testjava;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.marcodebon.testjava.beans.FileFIS;
import it.marcodebon.testjava.beans.Protocollo;
import it.marcodebon.testjava.beans.SimpleResult;
import it.marcodebon.testjava.beans.Tipologia;

public class Database {
	
	private final static Logger logger = LogManager.getLogger(Database.class);
	
	private static Connection conn = null;
	private static long phaseStartTime;
	private static CallableStatement statement = null;
	private static ResultSet resultSet = null;
	
	/**
	 * Inizializzazione database
	 * 
	 * @return Esito dell'operazione
	 */
	public static boolean init() {
		logger.trace("[init] - Caricamento driver jdbc '{}' in corso...", Settings.Db.driver);
		phaseStartTime = System.currentTimeMillis();
		try {
			Class.forName(Settings.Db.driver);
		} catch (ClassNotFoundException ex) {
			logger.fatal("[init] - JDBC Driver '{}' non trovato: forse un problema di classpath ?", Settings.Db.driver);
			return false;
		}
		logger.trace("[init] - Driver jdbc caricato in {}", Utility.elapsedTime(phaseStartTime));
		
		logger.trace("[init] - Connessione a database con stringa di connessione '{}' e username '{}' in corso...", Settings.Db.connectionString, Settings.Db.username);
		phaseStartTime = System.currentTimeMillis();
		try {
			conn = DriverManager.getConnection(Settings.Db.connectionString, Settings.Db.username, Settings.Db.password);
		} catch (SQLException ex) {
			logger.fatal("[init] - Connessione a database fallita: {} (SQLState: {})", ex.getMessage(), ex.getSQLState());
			return false;
		}
		logger.debug("[init] - Connessione a database eseguita in {}", Utility.elapsedTime(phaseStartTime));
		return true;
	}
	
	/**
	 * Chiusura connessione database
	 */
	public static boolean close() {
		boolean ret = true;
		try {
			if (conn != null && !conn.isClosed()) {
				logger.trace("[close] - Chiusura connessione in corso...");
				phaseStartTime = System.currentTimeMillis();
				conn.close();
				logger.debug("[close] - Chiusura connessione eseguita in {}", Utility.elapsedTime(phaseStartTime));
			} else {
				logger.warn("[close] - Chiusura connessione non necessaria? (conn=null)");
				ret = false;
			}
		} catch (SQLException ex) {
			logger.error("[close] - Errore in chiusura connessione: {} (SQLState: {})", ex.getMessage(), ex.getSQLState());
			ret = false;
		}
		return ret;
	}
	
	//-------------------------------------------------------------------------------------------------------------
	// Chiamate a Database
	//-------------------------------------------------------------------------------------------------------------
	public static boolean fileFISReset() {
		boolean ret = false;
		SimpleResult result = null;
		
		logger.trace("[fileFISReset] - Esecuzione spweb_batch_FILEFISRESET");
		try {
			statement = conn.prepareCall("{CALL spweb_batch_FILEFISRESET()}");
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				try {
					result = SimpleResult.fromResultSet(resultSet, SimpleResult.class);
					if (result.getCodErr().equals("000"))
						ret = true;
					else
						logger.fatal("[fileFISReset] - Errore esecuzione: {} - {}", result.getCodErr(), result.getDescErr());
				} catch (Exception e) {
					logger.fatal("[fileFISReset] - Errore interpretazione resultset");
				}
			} else
				logger.fatal("[fileFISReset] - Resultset vuoto");
		} catch(SQLException e) {
			logger.fatal("[fileFISReset] - Errore esecuzione");
		} finally {
			releaseResources();
		}
		return ret;
	}
	
	public static List<Tipologia> listaTipologie(String raggruppamento) {
		List<Tipologia> tipologie = new ArrayList<Tipologia>();

		logger.trace("[listaTipologie] - Esecuzione spweb_batch_FILEFISLISTATIPOLOGIE");
		try {
			statement = conn.prepareCall("{CALL spweb_batch_FILEFISLISTATIPOLOGIE(?)}");
			statement.setString(1, raggruppamento);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				try {
					Tipologia item = Tipologia.fromResultSet(resultSet, Tipologia.class);
					tipologie.add(item);
				} catch (Exception e) {
					logger.fatal("[listaTipologie] - Errore interpretazione resultset");
				}
			}
		} catch(SQLException e) {
			logger.fatal("[listaTipologie] - Errore esecuzione");
		} finally {
			releaseResources();
		}
		logger.info("[listaTipologie] - Numero tipologie trovate: {}", tipologie.size());
		return tipologie;
	}
	
	public static Protocollo newProtocollo() {
		Protocollo result = null;
		
		logger.trace("[newProtocollo] - Esecuzione spweb_batch_FILEFISNEWPROTOCOLLO");
		try {
			statement = conn.prepareCall("{CALL spweb_batch_FILEFISNEWPROTOCOLLO()}");
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				try {
					result = Protocollo.fromResultSet(resultSet, Protocollo.class);
				} catch (Exception e) {
					logger.fatal("[newProtocollo] - Errore interpretazione resultset");
				}
			} else
				logger.fatal("[newProtocollo] - Resultset vuoto");
		} catch(SQLException e) {
			logger.fatal("[newProtocollo] - Errore esecuzione");
		} finally {
			releaseResources();
		}
		return result;
	}
	
	public static List<FileFIS> listaFileFIS(long idTipologia) {
		List<FileFIS> fileFis = new ArrayList<FileFIS>();

		logger.trace("[listaFileFIS] - Esecuzione spweb_batch_FILEFISLISTA");
		try {
			statement = conn.prepareCall("{CALL spweb_batch_FILEFISLISTA(?)}");
			statement.setString(1, Long.toString(idTipologia));
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				try {
					FileFIS item = FileFIS.fromResultSet(resultSet, FileFIS.class);
					if (item.getCodErr().equals("000"))
						fileFis.add(item);
					else
						logger.fatal("[listaFileFIS] - Errore esecuzione: {} - {}", item.getCodErr(), item.getDescErr());
				} catch (Exception e) {
					logger.fatal("[listaFileFIS] - Errore interpretazione resultset");
				}
			}
		} catch(SQLException e) {
			logger.fatal("[listaFileFIS] - Errore esecuzione");
		} finally {
			releaseResources();
		}
		logger.debug("[listaFileFIS] - Numero file FIS trovati: {}", fileFis.size());
		return fileFis;
	}

	public static boolean aggiornaFlagElaborazione(long idFileFIS, String flag, String protocollo) {
		boolean ret = false;
		SimpleResult result = null;
		
		logger.trace("[aggiornaFlagElaborazione] - Esecuzione spweb_batch_FILEFISUPDATE");
		try {
			statement = conn.prepareCall("{CALL spweb_batch_FILEFISUPDATE(?,?,?)}");
			statement.setString(1, Long.toString(idFileFIS));
			statement.setString(2, flag);
			statement.setString(3, protocollo);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				try {
					result = SimpleResult.fromResultSet(resultSet, SimpleResult.class);
					if (result.getCodErr().equals("000"))
						ret = true;
					else
						logger.fatal("[aggiornaFlagElaborazione] - Errore esecuzione: {} - {}", result.getCodErr(), result.getDescErr());
				} catch (Exception e) {
					logger.fatal("[aggiornaFlagElaborazione] - Errore interpretazione resultset");
				}
			} else
				logger.fatal("[aggiornaFlagElaborazione] - Resultset vuoto");
		} catch(SQLException e) {
			logger.fatal("[aggiornaFlagElaborazione] - Errore esecuzione");
		} finally {
			releaseResources();
		}
		return ret;
	}
	
	public static boolean finalizzaFileFIS(long idTipologia) {
		boolean ret = false;
		SimpleResult result = null;
		
		logger.trace("[finalizzaFileFIS] - Esecuzione spweb_batch_FILEFISFINALIZZA");
		try {
			statement = conn.prepareCall("{CALL spweb_batch_FILEFISFINALIZZA(?)}");
			statement.setString(1, Long.toString(idTipologia));
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				try {
					result = SimpleResult.fromResultSet(resultSet, SimpleResult.class);
					if (result.getCodErr().equals("000"))
						ret = true;
					else
						logger.fatal("[finalizzaFileFIS] - Errore esecuzione: {} - {}", result.getCodErr(), result.getDescErr());
				} catch (Exception e) {
					logger.fatal("[finalizzaFileFIS] - Errore interpretazione resultset");
				}
			} else
				logger.fatal("[finalizzaFileFIS] - Resultset vuoto");
		} catch(SQLException e) {
			logger.fatal("[finalizzaFileFIS] - Errore esecuzione");
		} finally {
			releaseResources();
		}
		return ret;
	}
	
	
	private static void releaseResources() {
		try {
			if (resultSet != null)
				resultSet.close();
			if (statement != null)
				statement.close();
		} catch(Throwable e) {
		}
		resultSet = null;
		statement = null;
	}		
}
