package org.chenjingbin.springboot.email.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;

/**
 * @author chenjb
 * @Date 2018/9/18 15:53
 **/
@Service
public class EmailService {
    @Value("${spring.mail.username}")
    public String from;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    TemplateEngine templateEngine;
    /**
     * 简单邮件发送
     * @param to 发送人email
     * @param subject 主题
     * @param content 内容
     * @throws MessagingException
     */
    public void sendSimpleEmail(String to,String subject,String content) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(content);
        javaMailSender.send(message);
    }

    /**
     * html邮件发送
     * @param to 发送人email
     * @param subject 主题
     * @param content 内容
     * @throws MessagingException
     */
    public void sendHtmlEmail(String to,String subject,String content) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message,true);
        messageHelper.setTo(to);
        messageHelper.setFrom(from);
        messageHelper.setSubject(subject);
        messageHelper.setText(content,true);
        javaMailSender.send(message);
    }

    /**
     * 发送带有附件的邮件
     * @param to 接收人的email
     * @param subject 主题
     * @param content 内容
     * @param filePath 附件地址
     * @throws MessagingException
     */
    public void sendHtmlAttachmentEmail(String to,String subject,String content,String filePath)throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message,true);
        messageHelper.setTo(to);
        messageHelper.setFrom(from);
        messageHelper.setSubject(subject);
        messageHelper.setText(content,true);
        FileSystemResource fileSystemResource=new FileSystemResource(new File(filePath));
        messageHelper.addAttachment("附件",fileSystemResource);
        javaMailSender.send(message);
    }

    /**
     * 发送模板邮件
     * @param to 接收人邮件
     * @param subject 主题
     * @param template 模板html
     * @param map 传递参数集
     */
    public void sendTemplateHtmlEmail(String to, String subject, String template, Map<String,Object> map) throws MessagingException {
        Context context = new Context();
        map.forEach((k,v)->{context.setVariable(k,v); });
        String content = templateEngine.process(template,context);
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message,true);
        messageHelper.setTo(to);
        messageHelper.setFrom(from);
        messageHelper.setSubject(subject);
        messageHelper.setText(content,true);
        javaMailSender.send(message);
    }
}
