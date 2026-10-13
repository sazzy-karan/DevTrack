package com.devtrack.ui;

import com.devtrack.dao.DashboardDAO;
import com.devtrack.model.DashboardStats;
import com.devtrack.model.PracticeRecord;
import com.devtrack.model.Revision;
import com.devtrack.model.Topic;
import com.devtrack.model.TopicStatus;
import com.devtrack.service.PracticeService;
import com.devtrack.service.RevisionService;
import com.devtrack.service.TopicService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

public final class DashboardView extends BorderPane {
    private static final String INK = "#19243a";
    private static final String MUTED = "#758198";
    private static final String ACCENT = "#6857e8";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d");

    private final int userId;
    private final String username;
    private final TopicService topics;
    private final PracticeService practices;
    private final RevisionService revisions;
    private final DashboardDAO dashboard = new DashboardDAO();
    private final TableView<Topic> topicTable = new TableView<>();
    private final TableView<PracticeRecord> practiceTable = new TableView<>();
    private final TableView<Revision> revisionTable = new TableView<>();
    private final ComboBox<Topic> practiceTopic = new ComboBox<>();
    private final ObservableList<Topic> topicItems = FXCollections.observableArrayList();
    private final Label topicValue = new Label("0 / 0");
    private final Label solvedValue = new Label("0");
    private final Label practiceDaysValue = new Label("0 / 7");
    private final Label revisionsValue = new Label("0");
    private final Label progressCaption = new Label("Add a topic to start your roadmap.");
    private final Label duePreviewMessage = new Label("Loading your revision plan…");
    private final ProgressBar progressBar = new ProgressBar(0);
    private TabPane tabs;
    private Runnable onLogout;

    public DashboardView(int userId, String username, TopicService topics,
                         PracticeService practices, RevisionService revisions) {
        this.userId = userId;
        this.username = username;
        this.topics = topics;
        this.practices = practices;
        this.revisions = revisions;
        setPadding(new Insets(20, 28, 24, 28));
        setStyle("-fx-background-color: #f4f6fb;");

        setTop(buildHeader());
        tabs = new TabPane(
                new Tab("Overview", overviewTab()),
                new Tab("My topics", topicsTab()),
                new Tab("Practice log", practiceTab()),
                new Tab("Revision plan", revisionsTab()));
        tabs.getTabs().forEach(tab -> tab.setClosable(false));
        tabs.setTabMinWidth(128);
        setCenter(tabs);
        refresh();
    }

    public void setOnLogout(Runnable callback) {
        onLogout = callback;
    }

    private Node buildHeader() {
        Label mark = new Label("D");
        mark.setAlignment(Pos.CENTER);
        mark.setMinSize(42, 42);
        mark.setStyle("-fx-background-color: " + ACCENT + "; -fx-background-radius: 14;"
                + "-fx-text-fill: white; -fx-font-size: 23px; -fx-font-weight: bold;");

        Label brand = new Label("devtrack");
        brand.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: " + INK + ";");
        Label tagline = new Label("YOUR PRACTICE, WITH PURPOSE");
        tagline.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: " + MUTED
                + "; -fx-letter-spacing: 1.2px;");
        VBox brandText = new VBox(1, brand, tagline);
        HBox identity = new HBox(11, mark, brandText);
        identity.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(LocalDate.now().format(DATE_FORMAT));
        date.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 12px;");
        Label user = new Label("  " + username + "  ");
        user.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 9 14;"
                + "-fx-font-weight: bold; -fx-text-fill: " + INK + ";");
        Button logout = secondaryButton("Log out");
        logout.setOnAction(event -> {
            if (onLogout != null) {
                onLogout.run();
            }
        });
        HBox right = new HBox(12, date, user, logout);
        right.setAlignment(Pos.CENTER_RIGHT);
        HBox header = new HBox(identity, spacer, right);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 18, 0));
        return header;
    }

    private ScrollPane overviewTab() {
        Label eyebrow = new Label("YOUR LEARNING SPACE");
        eyebrow.setStyle("-fx-text-fill: #c9c4ff; -fx-font-size: 10px; -fx-font-weight: bold;"
                + "-fx-letter-spacing: 1.6px;");
        Label title = new Label("Small steps. Serious progress.");
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 27px; -fx-font-weight: bold;");
        Label subtitle = new Label("Keep showing up. Your future self is already cheering you on.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: #d6d8e7; -fx-font-size: 13px;");
        Button startPractice = primaryButton("＋  Log today's practice");
        startPractice.setOnAction(event -> tabs.getSelectionModel().select(2));
        VBox heroText = new VBox(9, eyebrow, title, subtitle, startPractice);
        heroText.setPadding(new Insets(24, 30, 24, 30));
        heroText.setStyle("-fx-background-color: linear-gradient(to right, #30296c, #6254d9);"
                + "-fx-background-radius: 20;");

        GridPane stats = new GridPane();
        stats.setHgap(14);
        stats.setVgap(14);
        for (int column = 0; column < 4; column++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(25);
            constraints.setHgrow(Priority.ALWAYS);
            stats.getColumnConstraints().add(constraints);
        }
        stats.add(statCard("TOPIC ROADMAP", topicValue, "complete", "#eeeaff", "#6857e8"),
                0, 0);
        stats.add(statCard("PROBLEMS SOLVED", solvedValue, "nice work!", "#e5f7f0", "#13835f"),
                1, 0);
        stats.add(statCard("PRACTICE THIS WEEK", practiceDaysValue, "days active", "#fff3df", "#bd7414"),
                2, 0);
        stats.add(statCard("REVISIONS TO DO", revisionsValue, "due or overdue", "#ffebed", "#d54f60"),
                3, 0);

        VBox roadmap = new VBox(12);
        VBox roadmapTitle = sectionTitle("Your topic roadmap", "A little progress adds up.");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(12);
        progressBar.setStyle("-fx-accent: " + ACCENT + ";");
        progressCaption.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 12px;");
        roadmap.getChildren().addAll(roadmapTitle, progressBar, progressCaption);
        roadmap.setPadding(new Insets(20));
        roadmap.setStyle(cardStyle());

        VBox duePanel = new VBox(12);
        VBox dueHeading = sectionTitle("On your revision list", "Topics ready for another look.");
        Button seeRevisions = linkButton("Open revision plan  →");
        seeRevisions.setOnAction(event -> tabs.getSelectionModel().select(3));
        HBox dueHeader = new HBox(dueHeading, growSpacer(), seeRevisions);
        dueHeader.setAlignment(Pos.CENTER_LEFT);
        duePreviewMessage.setWrapText(true);
        duePreviewMessage.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 13px;");
        duePanel.setId("due-preview-panel");
        duePreviewMessage.setId("due-preview-message");
        duePanel.getChildren().addAll(dueHeader, duePreviewMessage);
        duePanel.setPadding(new Insets(20));
        duePanel.setStyle(cardStyle());

        VBox content = new VBox(16, heroText, stats, roadmap, duePanel);
        content.setPadding(new Insets(18, 18, 24, 18));
        ScrollPane scroll = scroll(content);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private Node statCard(String caption, Label value, String footnote, String tint, String accent) {
        Label label = new Label(caption);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 9px; -fx-font-weight: bold;"
                + "-fx-letter-spacing: .5px;");
        value.setStyle("-fx-text-fill: " + INK + "; -fx-font-size: 25px; -fx-font-weight: bold;");
        Label note = new Label(footnote);
        note.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 10px; -fx-font-weight: bold;");
        Region accentLine = new Region();
        accentLine.setPrefHeight(3);
        accentLine.setMaxWidth(34);
        accentLine.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 3;");
        VBox card = new VBox(7, label, value, note, accentLine);
        card.setPadding(new Insets(15));
        card.setMinHeight(126);
        card.setStyle("-fx-background-color: " + tint + "; -fx-background-radius: 16;");
        return card;
    }

    private ScrollPane topicsTab() {
        configureTable(topicTable, "No topics yet",
                "Add a topic above, or use the DSA starter pack to build your roadmap.");
        topicTable.getColumns().setAll(
                column("TOPIC", Topic::name),
                column("STATUS", topic -> topic.status().toString()),
                column("PRACTICE", topic -> topic.practiceCount() + " problems"));
        topicTable.getColumns().get(0).setPrefWidth(380);

        TextField nameInput = new TextField();
        nameInput.setPromptText("e.g. Dynamic Programming");
        nameInput.setPrefWidth(240);
        TextField search = new TextField();
        search.setPromptText("⌕  Find a topic");
        search.setPrefWidth(200);
        FilteredList<Topic> filteredTopics = new FilteredList<>(topicItems, ignored -> true);
        topicTable.setItems(filteredTopics);
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase(Locale.ROOT);
            filteredTopics.setPredicate(topic -> query.isEmpty()
                    || topic.name().toLowerCase(Locale.ROOT).contains(query)
                    || topic.status().toString().toLowerCase(Locale.ROOT).contains(query));
        });

        Button add = primaryButton("＋  Add topic");
        add.setOnAction(event -> runAction(() -> topics.create(userId, nameInput.getText()), () -> {
            nameInput.clear();
            refresh();
        }));
        Button starterPack = secondaryButton("✦  DSA starter pack");
        starterPack.setOnAction(event -> runAction(
                () -> {
                    int added = topics.addCommonDsaTopics(userId);
                    showInfo(added == 0
                            ? "Your DSA topic list is already complete. Happy practising!"
                            : "Added " + added + " interview-ready topics to your roadmap.");
                },
                this::refresh));

        ComboBox<TopicStatus> statusInput = new ComboBox<>(
                FXCollections.observableArrayList(TopicStatus.values()));
        statusInput.setPromptText("Choose a status");
        Button update = secondaryButton("Update status");
        update.setOnAction(event -> {
            Topic selected = topicTable.getSelectionModel().getSelectedItem();
            if (selected == null || statusInput.getValue() == null) {
                showError("Choose a topic in the list and select its new status.");
                return;
            }
            runAction(() -> topics.updateStatus(userId, selected.id(), statusInput.getValue()), this::refresh);
        });
        Button delete = secondaryButton("Remove");
        delete.setOnAction(event -> {
            Topic selected = topicTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Choose a topic in the list first.");
                return;
            }
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                    "Remove \"" + selected.name() + "\" and its practice/revision history?");
            confirmation.setTitle("Remove topic");
            confirmation.setHeaderText("This action cannot be undone.");
            confirmation.showAndWait().filter(result -> result == javafx.scene.control.ButtonType.OK)
                    .ifPresent(result -> runAction(() -> topics.delete(userId, selected.id()), this::refresh));
        });

        HBox titleRow = new HBox(sectionTitle("My topics", "Your personal map of what you're learning."),
                growSpacer(), search);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox addRow = new HBox(9, nameInput, add, starterPack);
        addRow.setAlignment(Pos.CENTER_LEFT);
        HBox editRow = new HBox(9, statusInput, update, delete);
        editRow.setAlignment(Pos.CENTER_LEFT);
        VBox content = new VBox(14, titleRow, addRow, editRow, topicTable);
        content.setPadding(new Insets(18));
        return scroll(content);
    }

    private ScrollPane practiceTab() {
        configureTable(practiceTable, "No practice logged yet",
                "Your problem-solving history will appear here. Log your first problem above.");
        practiceTable.getColumns().setAll(
                column("PROBLEM", PracticeRecord::problemTitle),
                column("TOPIC", PracticeRecord::topicName),
                column("LEVEL", record -> friendlyEnum(record.difficulty())),
                column("RESULT", record -> friendlyEnum(record.status())),
                column("DATE", record -> record.practisedOn().format(DATE_FORMAT)));

        practiceTopic.setPromptText("Choose a topic");
        practiceTopic.setPrefWidth(210);
        practiceTopic.setMaxWidth(Double.MAX_VALUE);
        practiceTopic.setStyle(fieldStyle());
        TextField title = new TextField();
        title.setPromptText("Problem name");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setStyle(fieldStyle());
        TextField platform = new TextField();
        platform.setPromptText("Platform or source (optional)");
        platform.setMaxWidth(Double.MAX_VALUE);
        platform.setStyle(fieldStyle());
        ComboBox<String> difficulty = new ComboBox<>(FXCollections.observableArrayList("EASY", "MEDIUM", "HARD"));
        difficulty.setPromptText("Difficulty");
        difficulty.setMaxWidth(Double.MAX_VALUE);
        difficulty.setStyle(fieldStyle());
        ComboBox<String> status = new ComboBox<>(
                FXCollections.observableArrayList("SOLVED", "ATTEMPTED", "NEEDS_REVISIT"));
        status.setPromptText("How did it go?");
        status.setValue("SOLVED");
        status.setMaxWidth(Double.MAX_VALUE);
        status.setStyle(fieldStyle());
        DatePicker date = new DatePicker(LocalDate.now());
        date.setMaxWidth(Double.MAX_VALUE);
        date.setStyle(fieldStyle());
        TextArea notes = new TextArea();
        notes.setPromptText("Notes, approach, or something to remember (optional)");
        notes.setPrefRowCount(2);
        notes.setWrapText(true);
        notes.setStyle(fieldStyle());

        Button save = primaryButton("Save practice");
        save.setOnAction(event -> {
            if (practiceTopic.getValue() == null || difficulty.getValue() == null) {
                showError("Choose the topic and difficulty so your practice can be tracked.");
                return;
            }
            runAction(() -> practices.record(userId, practiceTopic.getValue().id(), title.getText(),
                    platform.getText(), difficulty.getValue(), status.getValue(), date.getValue(), notes.getText()),
                    () -> {
                        title.clear();
                        platform.clear();
                        notes.clear();
                        refresh();
                    });
        });

        GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(10);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(33.333);
            constraints.setHgrow(Priority.ALWAYS);
            fields.getColumnConstraints().add(constraints);
        }
        fields.add(practiceTopic, 0, 0);
        fields.add(title, 1, 0);
        fields.add(platform, 2, 0);
        fields.add(difficulty, 0, 1);
        fields.add(status, 1, 1);
        fields.add(date, 2, 1);
        GridPane.setColumnSpan(notes, 3);
        fields.add(notes, 0, 2);

        VBox form = new VBox(12, sectionTitle("Capture a practice session", "Every solved problem is a step forward."),
                fields, save);
        form.setPadding(new Insets(18));
        form.setStyle(cardStyle());
        VBox history = new VBox(12,
                sectionTitle("Recent practice", "Your latest 100 entries, newest first."),
                practiceTable);
        VBox.setVgrow(practiceTable, Priority.ALWAYS);
        VBox content = new VBox(16, form, history);
        content.setPadding(new Insets(18));
        return scroll(content);
    }

    private ScrollPane revisionsTab() {
        configureTable(revisionTable, "You're all caught up!",
                "No revisions are due right now. Your future self says thanks.");
        revisionTable.getColumns().setAll(
                column("TOPIC TO REVISIT", Revision::topicName),
                column("DUE DATE", revision -> revision.scheduledDate().format(DATE_FORMAT)),
                column("STATUS", revision ->
                        revision.scheduledDate().isBefore(LocalDate.now()) ? "Overdue" : "Due today"));
        revisionTable.getColumns().get(0).setPrefWidth(380);
        Button complete = primaryButton("✓  Mark as revised");
        complete.setOnAction(event -> {
            Revision selected = revisionTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a due topic in the list to mark it as revised.");
                return;
            }
            runAction(() -> revisions.complete(userId, selected), this::refresh);
        });
        VBox content = new VBox(14,
                sectionTitle("Revision plan", "A quick review today makes recall stronger tomorrow."),
                new Label("Your schedule grows gently: 1, 3, 7, 15, then 30 days."),
                revisionTable,
                complete);
        content.setPadding(new Insets(18));
        return scroll(content);
    }

    private void refresh() {
        try {
            topicItems.setAll(topics.list(userId));
            practiceTopic.setItems(FXCollections.observableArrayList(topicItems));
            var practiceItems = practices.listRecent(userId);
            practiceTable.setItems(FXCollections.observableArrayList(practiceItems));
            var dueItems = revisions.due(userId);
            revisionTable.setItems(FXCollections.observableArrayList(dueItems));

            DashboardStats stats = dashboard.summarize(userId, LocalDate.now());
            topicValue.setText(stats.completedTopics() + " / " + stats.totalTopics());
            solvedValue.setText(Integer.toString(stats.solvedProblems()));
            practiceDaysValue.setText(stats.practiceDaysThisWeek() + " / 7");
            revisionsValue.setText(Integer.toString(stats.dueRevisions() + stats.overdueRevisions()));
            progressBar.setProgress(stats.totalTopics() == 0 ? 0
                    : (double) stats.completedTopics() / stats.totalTopics());
            progressCaption.setText(stats.totalTopics() == 0
                    ? "Add a topic to start your roadmap."
                    : stats.completionPercent() + "% complete  ·  "
                    + (stats.totalTopics() - stats.completedTopics()) + " topics still in progress");

            updateDuePreview(dueItems, stats);
        } catch (SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void updateDuePreview(java.util.List<Revision> dueItems, DashboardStats stats) {
        if (dueItems.isEmpty()) {
            duePreviewMessage.setText("Nothing due right now — enjoy the clear space, or add a new practice session.");
            return;
        }
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < Math.min(3, dueItems.size()); index++) {
            Revision revision = dueItems.get(index);
            if (index > 0) {
                text.append("\n");
            }
            String state = revision.scheduledDate().isBefore(LocalDate.now()) ? "OVERDUE" : "DUE TODAY";
            text.append("•  ").append(revision.topicName()).append("   ·   ").append(state);
        }
        if (stats.dueRevisions() + stats.overdueRevisions() > 3) {
            text.append("\n…and ")
                    .append(stats.dueRevisions() + stats.overdueRevisions() - 3)
                    .append(" more in your revision plan.");
        }
        duePreviewMessage.setText(text.toString());
    }

    private void configureTable(TableView<?> table, String emptyTitle, String emptyMessage) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(emptyState(emptyTitle, emptyMessage));
        table.setStyle("-fx-background-color: white; -fx-background-radius: 14;"
                + "-fx-border-color: #e9ecf3; -fx-border-radius: 14;");
        table.setFixedCellSize(43);
    }

    private Node emptyState(String title, String message) {
        Label icon = new Label("✦");
        icon.setStyle("-fx-text-fill: #8b7ef0; -fx-font-size: 24px;");
        Label heading = new Label(title);
        heading.setStyle("-fx-text-fill: " + INK + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label detail = new Label(message);
        detail.setWrapText(true);
        detail.setMaxWidth(380);
        detail.setAlignment(Pos.CENTER);
        detail.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 12px;");
        VBox empty = new VBox(7, icon, heading, detail);
        empty.setAlignment(Pos.CENTER);
        return empty;
    }

    private <T> TableColumn<T, String> column(String title, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        return column;
    }

    private String friendlyEnum(String value) {
        String[] words = value.toLowerCase(Locale.ROOT).split("_");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return label.toString();
    }

    private ScrollPane scroll(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scroll;
    }

    private VBox sectionTitle(String title, String subtitle) {
        Label heading = new Label(title);
        heading.setStyle("-fx-text-fill: " + INK + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label detail = new Label(subtitle);
        detail.setStyle("-fx-text-fill: " + MUTED + "; -fx-font-size: 11px;");
        return new VBox(4, heading, detail);
    }

    private Button primaryButton(String title) {
        Button button = new Button(title);
        button.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-background-radius: 11; -fx-padding: 10 16;"
                + "-fx-cursor: hand;");
        return button;
    }

    private Button secondaryButton(String title) {
        Button button = new Button(title);
        button.setStyle("-fx-background-color: white; -fx-text-fill: " + INK + ";"
                + "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 9 13;"
                + "-fx-border-color: #e6e9f1; -fx-border-radius: 10; -fx-cursor: hand;");
        return button;
    }

    private Button linkButton(String title) {
        Button button = new Button(title);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT
                + "; -fx-font-weight: bold; -fx-cursor: hand;");
        return button;
    }

    private String fieldStyle() {
        return "-fx-background-color: white; -fx-background-radius: 10;"
                + "-fx-border-color: #e4e8f0; -fx-border-radius: 10; -fx-padding: 9;";
    }

    private String cardStyle() {
        return "-fx-background-color: white; -fx-background-radius: 16;"
                + "-fx-border-color: #e9ecf3; -fx-border-radius: 16;";
    }

    private Region growSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void runAction(SqlAction action, Runnable onSuccess) {
        try {
            action.run();
            onSuccess.run();
        } catch (SQLException | IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("DevTrack");
        alert.setHeaderText("Unable to complete that action");
        alert.setContentText(message == null ? "An unexpected error occurred." : message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("DevTrack");
        alert.setHeaderText("DSA starter pack");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws SQLException;
    }
}
