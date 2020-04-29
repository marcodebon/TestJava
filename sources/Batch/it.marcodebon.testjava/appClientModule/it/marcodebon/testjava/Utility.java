package it.marcodebon.testjava;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utility {

	final static Logger logger = LogManager.getLogger(Utility.class);
	
	//-----------------------------------------------------------------------------------------------------
	// Time
	//-----------------------------------------------------------------------------------------------------
	public static String elapsedTime(long startTime) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		final long hr = TimeUnit.MILLISECONDS.toHours(elapsedTime);
        final long min = TimeUnit.MILLISECONDS.toMinutes(elapsedTime - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(elapsedTime - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(elapsedTime - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
	}
	
	public static String replaceDatePattern(String s, Date date) {
		SimpleDateFormat sdfAnno = new SimpleDateFormat("yyyy");    
		SimpleDateFormat sdfData = new SimpleDateFormat("yyyyMMdd");    
		SimpleDateFormat sdfOra  = new SimpleDateFormat("HHmmss");
		s = s.replaceAll("<anno>", sdfAnno.format(date));
		s = s.replaceAll("<data>", sdfData.format(date));
		s = s.replaceAll("<ora>", sdfOra.format(date));
		return s;
	}
	
	//-----------------------------------------------------------------------------------------------------
	// Strings 
	//-----------------------------------------------------------------------------------------------------
	public static boolean isStringEmpty(String s) {
		return s == null || s.isEmpty();
	}
	
	public static String repeat(char c, int length) {
		if (length <= 0)
			return "";
		else {
			char[] chars = new char[length];
			Arrays.fill(chars, c);
			return String.valueOf(chars);
		}
	}
	  
	//-----------------------------------------------------------------------------------------------------
	// Directory 
	//-----------------------------------------------------------------------------------------------------
	public static void clearDirectory(File dir) {
		if (dir.exists() && dir.isDirectory()) {
	        File[] files = dir.listFiles();
	        if (files != null && files.length > 0) {
	            for (File aFile : files) {
	                removeDirectory(aFile);
	            }
	        }
		} else if (!dir.exists()) {
			dir.mkdir();
		} else {
			dir.delete();
			dir.mkdir();
		}
		
	}
	
	public static void removeDirectory(File dir) {
	    if (dir.isDirectory()) {
	        File[] files = dir.listFiles();
	        if (files != null && files.length > 0) {
	            for (File aFile : files) {
	                removeDirectory(aFile);
	            }
	        }
	        dir.delete();
	    } else {
	        dir.delete();
	    }
	}
	
	public static File isValidDir(String s) {
		if (isStringEmpty(s))
			return null;
		File dir = new File(s);
		if (dir.exists()) {
			return dir.isDirectory() ? dir : null;
		} else {
			dir.mkdirs();
			return dir;
		}
	}
	
	//-----------------------------------------------------------------------------------------------------
	// Email 
	//-----------------------------------------------------------------------------------------------------
    public static void sendEmail(String subject, String message, Date date) {

        // Get a Properties object
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", Settings.mail.host);
        props.setProperty("mail.smtp.port", Settings.mail.port);

        // quitwait = true: waits the response to the QUIT command; false: don't
        props.put("mail.smtps.quitwait", "false");

        // Create a new message
        Session session = Session.getInstance(props, null);
        final MimeMessage msg = new MimeMessage(session);

        // Set the FROM and TO fields
        try {
			msg.setFrom(new InternetAddress(Settings.mail.from));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Settings.mail.to));
			if (Settings.mail.cc != null && !Settings.mail.cc.isEmpty())
				msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(Settings.mail.cc));
			
			msg.setSubject(subject);
			msg.setContent(message, "text/html; charset=utf-8");
			msg.setSentDate(date);
			
			Transport.send(msg);
        } catch (MessagingException ex) {
			logger.fatal("[sendEmail] - Errore invio email {}", ex.getMessage());
        }
    }
}
