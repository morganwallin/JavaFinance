package src;
/*
import javax.mail.Session;
import java.util.Properties;

public class SendEmail {
    private String sendTo, sendFrom, host = "localhost";
    private Properties properties;
    private Session session;
    public SendEmail(String sendTo, String sendFrom) {
        this.sendTo = sendTo;
        this.sendFrom = sendFrom;
        properties = System.getProperties();
        properties.setProperty("mail.smtp", host);

    }

    private void createSession() {
        properties.put("mail.smtp.auth", "false");
        //Put below to false, if no https is needed
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", server);
        properties.put("mail.smtp.port", port);

        session = Session.getInstance(properties);
    }

}
*/






import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
class SendEmail implements Runnable
{
    private String username = "javafinancer@gmail.com";
    private String p = "JavaFinance123!";
    private String sendTo = "morgan_wallin@hotmail.com";
    private Properties props = new Properties();
    private String subject;
    private String message;

    SendEmail() {
        String host = "smtp.gmail.com";

        props.put("mail.smtp.user", username);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "true");
    }

    SendEmail(String sendTo, String subject, String message) {
        this();
        this.sendTo = sendTo;
        this.subject = subject;
        this.message = message;
    }


    @Override
    public void run() {
        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, p);
                    }
                });

        MimeMessage msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress(username));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(sendTo));
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg, username, p);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}