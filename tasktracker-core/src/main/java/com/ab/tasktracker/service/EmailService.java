package com.ab.tasktracker.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * This class is general method for sending mails
 */
@Configuration
@Component
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private Environment environment;

    @Autowired
    private JavaMailSender javaMailSender;

    @Bean
    public JavaMailSender getMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(environment.getProperty("spring.mail.host"));
        javaMailSender.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.mail.port"))));
        javaMailSender.setUsername(environment.getProperty("spring.mail.username"));
        javaMailSender.setPassword(environment.getProperty("spring.mail.password"));

        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.starttls.enable", environment.getProperty("spring.mail.properties.mail.smtp.starttls.enable"));
        javaMailProperties.put("mail.smtp.auth", environment.getProperty("spring.mail.properties.mail.smtp.auth"));
        javaMailProperties.put("mail.transport.protocol", "smtp");
        javaMailProperties.put("mail.debug", "true");
        javaMailProperties.put("mail.smtp.ssl.trust", "*");

        javaMailSender.setJavaMailProperties(javaMailProperties);
        return javaMailSender;
    }

    public Boolean sendMail(Map<String, String> mailMap) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            LOGGER.debug("Sending Mail to {}", mailMap.get("mailFrom"));
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, Boolean.parseBoolean(mailMap.get("isMultipart")));
            mimeMessageHelper.setSubject(mailMap.get("subject"));
            mimeMessageHelper.setFrom(new InternetAddress(mailMap.get("mailFrom")));
            mimeMessageHelper.setTo(mailMap.get("mailTo"));
//          todo: Send complete message in text, wherever sendMail is called from.
//          mimeMessageHelper.setText(String.format("Please use this OTP %s to verify your account", mailMap.get("text")));
            mimeMessageHelper.setText(mailMap.get("text"));
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            return true;
        } catch (MessagingException e) {
            LOGGER.error("MessagingException while Sending Mail{}", e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.error("Exception wile Sending Mail{}", e.getMessage());
            return false;
        }
    }
}
