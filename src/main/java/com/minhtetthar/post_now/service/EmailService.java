package com.minhtetthar.post_now.service;

import com.minhtetthar.post_now.entity.Post;
import com.minhtetthar.post_now.entity.User;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EmailService {

    private final Resend resendClient;
    private final String fromEmail;

    public EmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String fromEmail) {
        this.resendClient = new Resend(apiKey);
        this.fromEmail = fromEmail;
        log.info("Email service initialized with from address: {}", fromEmail);
    }

    @Async
    public void sendNewPostEmails(User author, Post post, List<User> followers) {
        if (followers.isEmpty()) {
            log.info("No followers to notify for new post from {}", author.getUsername());
            return;
        }

        log.info("Sending new post notification emails to {} followers", followers.size());

        String subject = author.getUsername() + " posted something new!";
        String postTitle = post.getTitle() != null && !post.getTitle().isEmpty()
                ? post.getTitle()
                : "Untitled";

        for (User follower : followers) {
            try {
                String htmlContent = buildNewPostEmailHtml(author, post, follower, postTitle);

                CreateEmailOptions email = CreateEmailOptions.builder()
                        .from(fromEmail)
                        // .to(follower.getEmail())
                        .to("minhtettharutycc@gmail.com")
                        .subject(subject)
                        .html(htmlContent)
                        .build();

                CreateEmailResponse response = resendClient.emails().send(email);
                log.info("Email sent to {}: {}", follower.getEmail(), response.getId());

            } catch (ResendException e) {
                log.error("Failed to send email to {}: {}", follower.getEmail(), e.getMessage(), e);
            } catch (Exception e) {
                log.error("Unexpected error sending email to {}: {}", follower.getEmail(), e.getMessage(), e);
            }
        }
    }

    private String buildNewPostEmailHtml(User author, Post post, User follower, String postTitle) {
        String postUrl = "http://localhost:8090/api/posts/" + post.getId();
        String profileUrl = "http://localhost:8090/api/users/" + author.getUsername();

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #4CAF50;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .content {
                            background-color: #f9f9f9;
                            padding: 20px;
                            border: 1px solid #ddd;
                        }
                        .post-title {
                            font-size: 20px;
                            font-weight: bold;
                            margin: 15px 0;
                            color: #2c3e50;
                        }
                        .post-content {
                            margin: 15px 0;
                            padding: 15px;
                            background-color: white;
                            border-left: 4px solid #4CAF50;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 24px;
                            background-color: #4CAF50;
                            color: white;
                            text-decoration: none;
                            border-radius: 4px;
                            margin: 15px 0;
                        }
                        .footer {
                            margin-top: 20px;
                            text-align: center;
                            font-size: 12px;
                            color: #666;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>New Post from %s</h1>
                    </div>
                    <div class="content">
                        <p>Hi %s,</p>
                        <p>%s just posted something new:</p>

                        <div class="post-title">%s</div>

                        <div class="post-content">
                            %s
                        </div>

                        <a href="%s" class="button">View Post</a>

                        <p>Keep up with <a href="%s">%s</a> on PostNow!</p>
                    </div>
                    <div class="footer">
                        <p>You're receiving this email because you follow %s on PostNow.</p>
                        <p>PostNow &copy; 2026</p>
                    </div>
                </body>
                </html>
                """.formatted(
                author.getUsername(),
                follower.getUsername(),
                author.getUsername(),
                postTitle,
                truncateContent(post.getContent(), 200),
                postUrl,
                profileUrl,
                author.getUsername(),
                author.getUsername());
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content != null ? content : "";
        }
        return content.substring(0, maxLength) + "...";
    }
}
