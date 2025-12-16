package com.inventory.service;

import com.inventory.util.LoggerUtil;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    private static final String SENDER_EMAIL = "starktvshows@gmail.com"; 
    private static final String SENDER_PASSWORD = "bkwo slyu kqby fwdm"; 
    private static final String RECIPIENT_EMAIL = "rpavishkadilhara@gmail.com"; 
    private static final String LOGO_PATH = "src/main/resources/com/inventory/view/logo.png";
    
    // Correct path to your HTML files
    private static final String TEMPLATE_PATH = "/email/";

    // ============================================================
    //  1. PUBLIC METHODS (For Controller to use)
    // ============================================================

    /**
     * loads the correct HTML template, fills in the data, and returns the full HTML string.
     * Used by ReportsController for both Printing and Emailing.
     */
    public static String buildReportHtml(String reportType, String htmlTableRows, String fromDate, String toDate, String totalRevenue) {
        // 1. Determine which file to load
        String filename;
        switch (reportType) {
            case "Daily Sales Report":       filename = "sales_report.html"; break;
            case "Product Performance Report": filename = "product_metrics.html"; break;
            case "Low Stock Alerts":         filename = "stock_alert_single.html"; break; // or make a batch one
            case "Inventory Valuation":      filename = "inventory_value.html"; break;
            default:                         filename = "sales_report.html"; break;
        }

        // 2. Load the template
        String htmlContent = loadTemplate(filename);

        // 3. Replace the placeholders
        return htmlContent
            .replace("{{fromDate}}", fromDate)
            .replace("{{toDate}}", toDate)
            .replace("{{generatedDate}}", java.time.LocalDate.now().toString())
            .replace("{{tableRows}}", htmlTableRows)
            .replace("{{totalRevenue}}", totalRevenue)
            .replace("{{reportType}}", reportType);
    }

    /**
     * Sends an email with the already-generated HTML content.
     */
    public static void sendHtmlEmail(String subject, String htmlContent) {
        new Thread(() -> {
            sendEmail(subject, htmlContent);
        }).start();
    }

    // ============================================================
    //  2. ALERTS (Keep these for your other system checks)
    // ============================================================

    public static void sendLowStockAlert(String productName, int currentStock) {
        new Thread(() -> {
            String subject = "⚠️ Low Stock Alert: " + productName;
            String html = loadTemplate("stock_alert_single.html");
            html = html.replace("{{productName}}", productName)
                       .replace("{{currentStock}}", String.valueOf(currentStock));
            sendEmail(subject, html);
        }).start();
    }

    public static void sendLoginAlert(String username, String role) {
        new Thread(() -> {
            String subject = "🔐 Login Alert: " + username;
            String html = loadTemplate("login_alert.html");
            html = html.replace("{{username}}", username)
                       .replace("{{role}}", role)
                       .replace("{{time}}", java.time.LocalDateTime.now()
                           .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            sendEmail(subject, html);
        }).start();
    }

    //  3. INTERNAL HELPER METHODS
    
    private static String loadTemplate(String fileName) {
        String fullPath = TEMPLATE_PATH + fileName;
        try (InputStream is = EmailService.class.getResourceAsStream(fullPath)) {
            if (is == null) {
                LoggerUtil.logError("Template not found: " + fullPath, null);
                return "<html><body>Error: Template not found (" + fullPath + ")</body></html>";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LoggerUtil.logError("Error reading template: " + fileName, e);
            return "<html><body>Error loading template</body></html>";
        }
    }

    private static void sendEmail(String subject, String htmlContent) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "City Mart System"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECIPIENT_EMAIL));
            message.setSubject(subject);

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Attach Logo
            File logoFile = new File(LOGO_PATH);
            if (logoFile.exists()) {
                MimeBodyPart imagePart = new MimeBodyPart();
                DataSource fds = new FileDataSource(logoFile);
                imagePart.setDataHandler(new DataHandler(fds));
                imagePart.setHeader("Content-ID", "<logoImage>");
                multipart.addBodyPart(imagePart);
            }

            message.setContent(multipart);
            Transport.send(message);
            
            LoggerUtil.logInfo("Email Sent: " + subject);
            System.out.println(">> ✅ Email Sent Successfully!");

        } catch (Exception e) {
            LoggerUtil.logError("Email Failed", e);
            e.printStackTrace();
        }
    }
}