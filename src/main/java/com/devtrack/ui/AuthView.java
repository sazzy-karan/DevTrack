package com.devtrack.ui;

import com.devtrack.model.User;
import com.devtrack.service.AuthService;
import com.devtrack.service.PasswordResetService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.function.BiConsumer;

public final class AuthView extends HBox {
    private final AuthService auth;
    private final PasswordResetService passwordReset;
    private final BiConsumer<Integer, String> onLogin;
    private final TextField username = new TextField();
    private final TextField email = new TextField();
    private final PasswordField password = new PasswordField();
    private final Label message = new Label();
    private boolean registering;

    public AuthView(AuthService auth, PasswordResetService passwordReset, BiConsumer<Integer, String> onLogin) {
        this.auth = auth;
        this.passwordReset = passwordReset;
        this.onLogin = onLogin;
        setStyle("-fx-background-color: white;");

        Label mark = new Label("D");
        mark.setAlignment(Pos.CENTER);
        mark.setMinSize(46, 46);
        mark.setStyle("-fx-background-color: #8879ff; -fx-background-radius: 15;"
                + "-fx-text-fill: white; -fx-font-size: 25px; -fx-font-weight: bold;");
        Label brand = new Label("devtrack");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 23px; -fx-font-weight: bold;");
        HBox brandLockup = new HBox(12, mark, brand);
        brandLockup.setAlignment(Pos.CENTER_LEFT);

        Label welcome = new Label("A little practice.\nA lot of progress.");
        welcome.setWrapText(true);
        welcome.setStyle("-fx-text-fill: white; -fx-font-size: 33px; -fx-font-weight: bold;");
        Label introduction = new Label(
                "Your personal space to build skills, find your rhythm, and feel ready for what's next.");
        introduction.setWrapText(true);
        introduction.setStyle("-fx-text-fill: #d6d4f4; -fx-font-size: 14px; -fx-line-spacing: 5px;");

        VBox benefits = new VBox(15,
                benefit("01", "Track every practice session"),
                benefit("02", "See your progress, topic by topic"),
                benefit("03", "Come back to concepts at the right time"));
        Region flexibleSpace = new Region();
        VBox.setVgrow(flexibleSpace, Priority.ALWAYS);
        Label privacy = new Label("YOUR LEARNING JOURNEY, YOURS ALONE");
        privacy.setStyle("-fx-text-fill: #c9c4ff; -fx-font-size: 9px; -fx-font-weight: bold;"
                + "-fx-letter-spacing: 1px;");
        VBox welcomePanel = new VBox(24, brandLockup, flexibleSpace, welcome, introduction,
                benefits, flexibleSpace(), privacy);
        welcomePanel.setPadding(new Insets(42));
        welcomePanel.setPrefWidth(390);
        welcomePanel.setMinWidth(330);
        welcomePanel.setStyle("-fx-background-color: linear-gradient(to bottom right, #262254, #5748c8);");

        VBox formPanel = new VBox(12);
        formPanel.setAlignment(Pos.CENTER_LEFT);
        formPanel.setPadding(new Insets(52));
        formPanel.setMaxWidth(470);
        HBox.setHgrow(formPanel, Priority.ALWAYS);

        Label title = new Label("Welcome back");
        title.setStyle("-fx-font-size: 27px; -fx-font-weight: bold; -fx-text-fill: #19243a;");
        Label subtitle = new Label("Pick up where you left off. Your next step starts here.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: #758198; -fx-font-size: 12px;");

        username.setPromptText("Username");
        email.setPromptText("Email address");
        password.setPromptText("Password (minimum 8 characters)");
        username.setMaxWidth(Double.MAX_VALUE);
        email.setMaxWidth(Double.MAX_VALUE);
        password.setMaxWidth(Double.MAX_VALUE);
        username.setStyle(fieldStyle());
        email.setStyle(fieldStyle());
        password.setStyle(fieldStyle());

        Button submit = new Button("Log in");
        submit.setDefaultButton(true);
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setStyle("-fx-background-color: #6857e8; -fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-background-radius: 11; -fx-padding: 12;"
                + "-fx-cursor: hand;");
        submit.setOnAction(event -> submit());
        Button toggle = new Button("Create an account");
        toggle.setMaxWidth(Double.MAX_VALUE);
        toggle.setStyle("-fx-background-color: transparent; -fx-text-fill: #6857e8;"
                + "-fx-font-weight: bold; -fx-padding: 5; -fx-cursor: hand;");
        toggle.setOnAction(event -> {
            registering = !registering;
            email.setVisible(registering);
            email.setManaged(registering);
            submit.setText(registering ? "Register" : "Log in");
            toggle.setText(registering ? "I already have an account" : "Create an account");
            title.setText(registering ? "Create your account" : "Welcome back");
            subtitle.setText(registering
                    ? "Your next chapter of consistent practice starts here."
                    : "Pick up where you left off. Your next step starts here.");
            message.setText("");
        });
        email.setVisible(false);
        email.setManaged(false);
        Button forgotPassword = new Button("Forgot password?");
        forgotPassword.setMaxWidth(Double.MAX_VALUE);
        forgotPassword.setStyle("-fx-background-color: transparent; -fx-text-fill: #758198;"
                + "-fx-padding: 4; -fx-cursor: hand;");
        forgotPassword.setOnAction(event -> requestPasswordReset());
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        Region formSpacer = new Region();
        VBox.setVgrow(formSpacer, Priority.ALWAYS);
        Region formBottomSpacer = new Region();
        VBox.setVgrow(formBottomSpacer, Priority.ALWAYS);
        formPanel.getChildren().addAll(title, subtitle, formSpacer, username, email, password,
                submit, toggle, forgotPassword, message, formBottomSpacer);
        getChildren().addAll(welcomePanel, formPanel);
    }

    private Node benefit(String number, String text) {
        Label index = new Label(number);
        index.setStyle("-fx-text-fill: #b9b2ff; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label description = new Label(text);
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        HBox row = new HBox(12, index, description);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Region flexibleSpace() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private String fieldStyle() {
        return "-fx-background-color: #f7f8fc; -fx-background-radius: 10;"
                + "-fx-border-color: #e7eaf2; -fx-border-radius: 10; -fx-padding: 11;";
    }

    private void submit() {
        try {
            User user = registering
                    ? auth.register(username.getText(), email.getText(), password.getText())
                    : auth.login(username.getText(), password.getText());
            onLogin.accept(user.id(), user.username());
        } catch (SQLException | GeneralSecurityException | IllegalArgumentException exception) {
            message.setText(exception.getMessage());
            message.setStyle("-fx-text-fill: #b42318;");
        }
    }

    private void requestPasswordReset() {
        TextField resetUsername = new TextField();
        resetUsername.setPromptText("Username");
        TextField resetEmail = new TextField();
        resetEmail.setPromptText("Registered email address");
        Dialog<ButtonType> requestDialog = new Dialog<>();
        requestDialog.setTitle("Request password reset");
        requestDialog.setHeaderText("A one-time code will be sent to the registered email address.");
        requestDialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Send code", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        GridPane requestFields = new GridPane();
        requestFields.setHgap(10);
        requestFields.setVgap(10);
        requestFields.setPadding(new Insets(12));
        requestFields.addRow(0, new Label("Username"), resetUsername);
        requestFields.addRow(1, new Label("Email"), resetEmail);
        requestDialog.getDialogPane().setContent(requestFields);
        requestDialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return;
            }
            try {
                passwordReset.requestCode(resetUsername.getText(), resetEmail.getText());
                showNotice("If the details match an account, a one-time code was sent to its registered email. "
                        + "The code is valid for 15 minutes.");
                showSetNewPasswordDialog();
            } catch (SQLException | IOException | IllegalArgumentException exception) {
                showError(exception.getMessage());
            }
        });
    }

    private void showSetNewPasswordDialog() {
        TextField code = new TextField();
        code.setPromptText("One-time code from email");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New password (minimum 8 characters)");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm new password");

        Dialog<ButtonType> resetDialog = new Dialog<>();
        resetDialog.setTitle("Set a new password");
        resetDialog.setHeaderText("Enter the emailed code and choose a new password.");
        resetDialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Reset password", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        GridPane resetFields = new GridPane();
        resetFields.setHgap(10);
        resetFields.setVgap(10);
        resetFields.setPadding(new Insets(12));
        resetFields.addRow(0, new Label("Reset code"), code);
        resetFields.addRow(1, new Label("New password"), newPassword);
        resetFields.addRow(2, new Label("Confirm password"), confirmPassword);
        resetDialog.getDialogPane().setContent(resetFields);
        resetDialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return;
            }
            if (!newPassword.getText().equals(confirmPassword.getText())) {
                showError("The new passwords do not match.");
                return;
            }
            try {
                passwordReset.resetPassword(code.getText(), newPassword.getText());
                showNotice("Your password has been reset. You can now log in.");
            } catch (SQLException | GeneralSecurityException | IllegalArgumentException exception) {
                showError(exception.getMessage());
            }
        });
    }

    private void showNotice(String text) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("DevTrack");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    private void showError(String text) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("DevTrack");
        alert.setHeaderText("Unable to reset password");
        alert.setContentText(text == null ? "An unexpected error occurred." : text);
        alert.showAndWait();
    }
}
