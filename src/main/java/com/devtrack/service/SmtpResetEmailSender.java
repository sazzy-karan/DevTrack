package com.devtrack.service;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class SmtpResetEmailSender implements PasswordResetService.ResetEmailSender {
    @Override
    public void send(String recipient, String code) throws IOException {
        String host = requiredSetting("DEVTRACK_SMTP_HOST");
        int port;
        try {
            port = Integer.parseInt(requiredSetting("DEVTRACK_SMTP_PORT"));
        } catch (NumberFormatException exception) {
            throw new IOException("DEVTRACK_SMTP_PORT must be a number.", exception);
        }
        String username = requiredSetting("DEVTRACK_SMTP_USER");
        String password = requiredSetting("DEVTRACK_SMTP_PASSWORD");
        String from = requiredSetting("DEVTRACK_SMTP_FROM");
        validateAddress(recipient);
        validateAddress(from);

        try (Socket plainSocket = new Socket(host, port)) {
            plainSocket.setSoTimeout(15_000);
            SmtpConnection connection = new SmtpConnection(plainSocket);
            connection.expect(220);
            connection.command("EHLO devtrack.local", 250);
            connection.command("STARTTLS", 220);

            SSLSocket tlsSocket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                    .createSocket(plainSocket, host, port, true);
            tlsSocket.setSoTimeout(15_000);
            var sslParameters = tlsSocket.getSSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
            tlsSocket.setSSLParameters(sslParameters);
            tlsSocket.startHandshake();

            try (tlsSocket) {
                connection = new SmtpConnection(tlsSocket);
                connection.command("EHLO devtrack.local", 250);
                connection.command("AUTH LOGIN", 334);
                connection.command(base64(username), 334);
                connection.command(base64(password), 235);
                connection.command("MAIL FROM:<" + from + ">", 250);
                connection.command("RCPT TO:<" + recipient + ">", 250, 251);
                connection.command("DATA", 354);
                connection.message(from, recipient, code);
                connection.expect(250);
                connection.command("QUIT", 221);
            }
        }
    }

    @Override
    public void validateConfiguration() throws IOException {
        requiredSetting("DEVTRACK_SMTP_HOST");
        try {
            int port = Integer.parseInt(requiredSetting("DEVTRACK_SMTP_PORT"));
            if (port < 1 || port > 65535) {
                throw new IOException("DEVTRACK_SMTP_PORT must be between 1 and 65535.");
            }
        } catch (NumberFormatException exception) {
            throw new IOException("DEVTRACK_SMTP_PORT must be a number.", exception);
        }
        requiredSetting("DEVTRACK_SMTP_USER");
        requiredSetting("DEVTRACK_SMTP_PASSWORD");
        validateAddress(requiredSetting("DEVTRACK_SMTP_FROM"));
    }

    private String requiredSetting(String name) throws IOException {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        if (value == null || value.isBlank()) {
            throw new IOException("Password recovery email is not configured. Set " + name + ".");
        }
        return value.trim();
    }

    private void validateAddress(String address) throws IOException {
        if (!address.matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")) {
            throw new IOException("SMTP sender or recipient email address is invalid.");
        }
    }

    private String base64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static final class SmtpConnection {
        private final BufferedReader reader;
        private final BufferedWriter writer;

        private SmtpConnection(Socket socket) throws IOException {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        private void command(String command, int... expectedCodes) throws IOException {
            writer.write(command);
            writer.write("\r\n");
            writer.flush();
            expect(expectedCodes);
        }

        private void message(String from, String to, String code) throws IOException {
            writer.write("From: <" + from + ">\r\n");
            writer.write("To: <" + to + ">\r\n");
            writer.write("Subject: DevTrack password reset code\r\n");
            writer.write("MIME-Version: 1.0\r\n");
            writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
            writer.write("\r\n");
            writer.write("Your DevTrack password reset code is:\r\n\r\n");
            writer.write(code);
            writer.write("\r\n\r\nThis code expires in 15 minutes. If you did not request it, ignore this email.\r\n");
            writer.write(".\r\n");
            writer.flush();
        }

        private void expect(int... expectedCodes) throws IOException {
            String line = reader.readLine();
            if (line == null || line.length() < 3) {
                throw new IOException("SMTP server closed the connection unexpectedly.");
            }
            int responseCode;
            try {
                responseCode = Integer.parseInt(line.substring(0, 3));
            } catch (NumberFormatException exception) {
                throw new IOException("SMTP server returned an invalid response.", exception);
            }
            while (line.length() > 3 && line.charAt(3) == '-') {
                line = reader.readLine();
                if (line == null) {
                    throw new IOException("SMTP server closed the connection unexpectedly.");
                }
            }
            for (int expected : expectedCodes) {
                if (responseCode == expected) {
                    return;
                }
            }
            throw new IOException("SMTP server rejected the reset email (response " + responseCode + ").");
        }
    }
}
