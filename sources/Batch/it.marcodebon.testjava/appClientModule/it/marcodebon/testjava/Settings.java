package it.marcodebon.testjava;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Settings {

	final static Logger logger = LogManager.getLogger(Settings.class);

	public static class db {
		public static String driver;
		public static String connectionString;
		public static String username;
		public static String password;
	}
	
	public static class mail {
		public static String host;
		public static String port;
		public static String from;
		public static String to;
		public static String cc;
	}
	
	public static class dir {
		public static String temp;
	}

	public static void initialize() throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream("config/configurations.properties"));
		
		db.driver = properties.get("db.driver").toString();
		db.connectionString = properties.get("db.connectionString").toString();
		db.username = properties.get("db.username").toString();
		db.password = properties.get("db.password").toString();
	
		mail.host = properties.get("mail.host").toString();
		mail.port = properties.get("mail.port").toString();
		mail.from = properties.get("mail.from").toString();
		mail.to = properties.get("mail.to").toString();
		mail.cc = null;
		
		dir.temp = properties.get("dir.temp").toString();

		logger.info("db.driver..........: '{}'", db.driver);
		logger.info("db.connectionString: '{}'", db.connectionString);
		logger.info("db.username........: '{}'", db.username);
		logger.info("db.password........: '{}'", db.password);
		logger.info("mail.host..........: '{}'", mail.host);
		logger.info("mail.port..........: '{}'", mail.port);
		logger.info("mail.from..........: '{}'", mail.from);
		logger.info("mail.to............: '{}'", mail.to);
		logger.info("dir.temp...........: '{}'", dir.temp);
	}
}
