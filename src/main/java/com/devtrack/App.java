package com.devtrack;

import com.devtrack.dao.PracticeDAO;
import com.devtrack.dao.PasswordResetDAO;
import com.devtrack.dao.RevisionDAO;
import com.devtrack.dao.TopicDAO;
import com.devtrack.dao.UserDAO;
import com.devtrack.service.AuthService;
import com.devtrack.service.PracticeService;
import com.devtrack.service.PasswordResetService;
import com.devtrack.service.RevisionService;
import com.devtrack.service.SmtpResetEmailSender;
import com.devtrack.service.TopicService;
import com.devtrack.ui.AuthView;
import com.devtrack.ui.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class App extends Application {
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("DevTrack");
        showAuthentication();
        stage.show();
    }

    private void showAuthentication() {
        AuthView view = new AuthView(
                new AuthService(new UserDAO()),
                new PasswordResetService(new PasswordResetDAO(), new SmtpResetEmailSender()),
                this::showDashboard);
        stage.setScene(new Scene(view, 960, 660));
        stage.setMinWidth(820);
        stage.setMinHeight(600);
    }

    private void showDashboard(int userId, String username) {
        DashboardView view = new DashboardView(
                userId,
                username,
                new TopicService(new TopicDAO()),
                new PracticeService(new PracticeDAO(), new RevisionDAO(), new TopicDAO()),
                new RevisionService(new RevisionDAO(), new TopicDAO()));
        view.setOnLogout(this::showAuthentication);
        stage.setScene(new Scene(view, 1120, 760));
        stage.setMinWidth(900);
        stage.setMinHeight(650);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
