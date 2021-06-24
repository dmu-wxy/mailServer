package com.adskn.mailserver.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Map;

@Component
public class MailReceiver {
    public static final Logger logger = LoggerFactory.getLogger(MailReceiver.class);

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    MailProperties mailProperties;

    @Autowired
    TemplateEngine templateEngine;

    @RabbitListener(queues = "adskn.mail.ensure")
    public void handler(Map<String,String> map){
        logger.info("消息队列》》》》to:" + map.get("email"));
        String to = map.get("email");
        if(to == null || to.equals("")){
            // 没有邮箱不发送
            return;
        }
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try{
            helper.setFrom(mailProperties.getUsername());
            helper.setSubject("adskn确认注册邮件");
            helper.setSentDate(new Date());
            StringBuilder url = new StringBuilder("https://www.adskn.com/login/mail.html?mod=");
            //${mod}&etoken=${etoken}&email=${email}";
            url.append(map.get("mod"))
                    .append("&etoken=")
                    .append(map.get("etoken"))
                    .append("&email=")
                    .append(map.get("email"));
            Context context = new Context();
            context.setVariable("url",url.toString());
            String mail = templateEngine.process("mail",context);
            helper.setText(mail,true);
            helper.setTo(to);
            javaMailSender.send(mimeMessage);
        } catch (Exception e){
            logger.error("邮件发送失败：to-->" + to + ",errorMessage-->" +e.getMessage());
        }

    }
}
