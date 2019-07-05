package com.achersoft.email;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class EmailClient {
    
    private static final Properties MAIL_PROPERTIES;
    private @Autowired Environment env;
    
    static {
        MAIL_PROPERTIES = System.getProperties();
        MAIL_PROPERTIES.put("mail.smtp.port", "587");
        MAIL_PROPERTIES.put("mail.smtp.auth", "true");
        MAIL_PROPERTIES.put("mail.smtp.starttls.enable", "true");
    }
    
    public void sendEmail(String recipient, String subject, String body) throws Exception {
        Session getMailSession;
	MimeMessage generateMailMessage;
        
        try {
            getMailSession = Session.getDefaultInstance(MAIL_PROPERTIES, null);
            generateMailMessage = new MimeMessage(getMailSession);
            generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            generateMailMessage.setSubject(subject);
            String emailBody = body;
            generateMailMessage.setContent(emailBody, "text/html");

            Transport transport = getMailSession.getTransport("smtp");

            // Enter your correct gmail UserID and Password
            // if you have 2FA enabled then provide App Specific Password
            transport.connect("smtp.gmail.com", env.getProperty("smtp.user"), env.getProperty("smtp.password"));
            transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
            transport.close();
            }
        catch(Exception e){
            System.err.println(e);
            throw new Exception();
        }
    }
}
