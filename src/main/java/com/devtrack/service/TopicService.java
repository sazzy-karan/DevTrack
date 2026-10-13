package com.devtrack.service;

import com.devtrack.dao.TopicDAO;
import com.devtrack.model.Topic;
import com.devtrack.model.TopicStatus;

import java.sql.SQLException;
import java.util.List;

public final class TopicService {
    private static final List<String> COMMON_DSA_TOPICS = List.of(
            "Arrays",
            "Strings",
            "Hashing",
            "Two Pointers",
            "Sliding Window",
            "Prefix Sum",
            "Sorting",
            "Binary Search",
            "Linked Lists",
            "Stacks",
            "Queues",
            "Monotonic Stack and Queue",
            "Trees",
            "Binary Search Trees",
            "Heaps and Priority Queues",
            "Graphs",
            "Graph BFS and DFS",
            "Topological Sort",
            "Union-Find (Disjoint Set)",
            "Tries",
            "Recursion and Backtracking",
            "Dynamic Programming",
            "Greedy Algorithms",
            "Intervals",
            "Bit Manipulation",
            "Math and Number Theory",
            "Matrices",
            "Complexity Analysis");

    private final TopicDAO topics;

    public TopicService(TopicDAO topics) {
        this.topics = topics;
    }

    public List<Topic> list(int userId) throws SQLException {
        return topics.findAll(userId);
    }

    public Topic create(int userId, String name) throws SQLException {
        String cleanName = name == null ? "" : name.trim();
        if (cleanName.isEmpty() || cleanName.length() > 100) {
            throw new IllegalArgumentException("Topic name must contain 1-100 characters.");
        }
        return topics.create(userId, cleanName);
    }

    public int addCommonDsaTopics(int userId) throws SQLException {
        return topics.addMissingTopics(userId, COMMON_DSA_TOPICS);
    }

    public void updateStatus(int userId, int topicId, TopicStatus status) throws SQLException {
        topics.updateStatus(userId, topicId, status);
    }

    public void delete(int userId, int topicId) throws SQLException {
        topics.delete(userId, topicId);
    }
}
