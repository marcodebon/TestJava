package it.marcodebon.testjava;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Settings {

	private final static Logger logger = LogManager.getLogger(Settings.class);

	public static class Db {
		public static String driver;
		public static String connectionString;
		public static String username;
		public static String password;
	}
	
	public static class Mail {
		public static String host;
		public static String port;
		public static String from;
		public static String to;
		public static String cc;
	}
	
	public static class Dir {
		public static String temp;
	}

	public static void initialize() throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream("config/configurations.properties"));
		
		Db.driver = properties.get("db.driver").toString();
		Db.connectionString = properties.get("db.connectionString").toString();
		Db.username = properties.get("db.username").toString();
		Db.password = properties.get("db.password").toString();
	
		Mail.host = properties.get("mail.host").toString();
		Mail.port = properties.get("mail.port").toString();
		Mail.from = properties.get("mail.from").toString();
		Mail.to = properties.get("mail.to").toString();
		Mail.cc = null;
		
		Dir.temp = properties.get("dir.temp").toString();

		logger.info("db.driver..........: '{}'", Db.driver);
		logger.info("db.connectionString: '{}'", Db.connectionString);
		logger.info("db.username........: '{}'", Db.username);
		logger.info("db.password........: '{}'", Db.password);
		logger.info("mail.host..........: '{}'", Mail.host);
		logger.info("mail.port..........: '{}'", Mail.port);
		logger.info("mail.from..........: '{}'", Mail.from);
		logger.info("mail.to............: '{}'", Mail.to);
		logger.info("dir.temp...........: '{}'", Dir.temp);
	}
}
