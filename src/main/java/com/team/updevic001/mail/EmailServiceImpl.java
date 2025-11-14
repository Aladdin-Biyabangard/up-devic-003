//package com.team.updevic001.mail;
//
//import com.team.updevic001.services.interfaces.FileLoadService;
//import jakarta.mail.internet.MimeMessage;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//@Slf4j
//public class EmailServiceImpl {
//
//    final JavaMailSender mailSender;
//    final TemplateEngine templateEngine;
//    final ClassPathResource logo = new ClassPathResource("static/logo.png");
//    final FileLoadService fileLoadService;
//    final RestTemplate restTemplate = new RestTemplate();
//
//    @Value("${n8n.webhook.url}")
//    String n8nWebhookUrl;
//
//    @Async("asyncTaskExecutor")
//    public void sendHtmlEmail(String subject, String to, String templateName, Map<String, Object> variables) {
//        // həm lokal (JavaMailSender) göndər, həm də n8n-ə ötür
////        sendEmailInternal(subject, to, templateName, variables, null, null);
//        sendEmailInternal(subject, to, templateName, variables, null, null);
//    }
//
//    @Async("asyncTaskExecutor")
//    public void sendFileEmail(String subject, String to, String templateName,
//                              Map<String, Object> variables, String fileUrl, MultipartFile imageFile) {
//
//        sendEmailInternal(subject, to, templateName, variables, fileUrl, imageFile);
//    }
//
//
//    private void sendEmailInternal(String subject, String to, String templateName,
//                                   Map<String, Object> variables, String fileUrl, MultipartFile imageFile) {
//        try {
//            Context context = new Context();
//            context.setVariables(variables);
//
//            String body = templateEngine.process("email/" + templateName, context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo(to);
//            helper.setSubject(Optional.ofNullable(subject).orElse("Re-Info"));
//            helper.setText(body, true);
//
//            if (fileUrl != null && !fileUrl.isEmpty()) {
//                byte[] fileBytes = fileLoadService.downloadFileAsBytes(fileUrl);
//                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
//                helper.addAttachment(fileName, new ByteArrayResource(fileBytes));
//            }
//
//            if (imageFile != null && !imageFile.isEmpty()) {
//                helper.addAttachment(
//                        Objects.requireNonNull(imageFile.getOriginalFilename()),
//                        new ByteArrayResource(imageFile.getBytes())
//                );
//            }
//
//            helper.addInline("logo", logo);
//
//            mailSender.send(message);
//            log.info("HTML email sent to {}", to);
//
//        } catch (Exception e) {
//            log.error("Failed to send email: {}", e.getMessage(), e);
//        }
//    }
//
//    private void sendEmailViaN8n(String subject, String to, String templateName,
//                                 Map<String, Object> variables, String fileUrl, MultipartFile imageFile) {
//        try {
//            Context context = new Context();
//            context.setVariables(variables);
//
//            String body = templateEngine.process("email/" + templateName, context);
//
//            Map<String, Object> payload = Map.of(
//                    "to", to,
//                    "subject", Optional.ofNullable(subject).orElse("Re-Info"),
//                    "body", body,
//                    "fileUrl", fileUrl != null ? fileUrl : "",
//                    "hasImage", imageFile != null && !imageFile.isEmpty()
//            );
//
//            restTemplate.postForEntity(n8nWebhookUrl, payload, String.class);
//            log.info("Request sent to n8n for {}", to);
//
//        } catch (Exception e) {
//            log.error("Failed to call n8n: {}", e.getMessage(), e);
//        }
//    }
//
//
//}