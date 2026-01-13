package ru.upmt.webServerBot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "position")
    private String position;

    @Column(name = "comment")
    private String comment;

    @Enumerated()
    @Column(name = "task_complete")
    private TaskComplete taskComplete = TaskComplete.UNCOMPLETED;
//    @Column(name = "status")
//    private NotificationStatus status;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;;

    @OneToMany(mappedBy = "notification", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private List<ImageTask> imageTasks = new ArrayList<>();

//    public Notification(Long chatId, String position, String comment, TaskComplete taskComplete, NotificationStatus status, LocalDateTime sentDate) {
//        this.chatId = chatId;
//        this.position = position;
//        this.comment = comment;
//        this.taskComplete = taskComplete;
//        this.status = status;
//        this.sentDate = sentDate;
//    }

    public Notification(Long chatId, String comment,  LocalDateTime now) {
        this.chatId = chatId;
        this.comment = comment;
        this.sentDate = now;
    }

    @Override
    public String toString() {
        return "Notification: " +
                ", chatId = " + chatId +
                ", position = " + position +
                ", comment = '" + comment + '\'' +
                ", task_complete = '" + taskComplete + '\'' +
                ", notificationDate = " + sentDate;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getPosition() {
        return position;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getNotificationDate() {
        return sentDate;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification that)) return false;
        return chatId == that.chatId && Objects.equals(id, that.id) && Objects.equals(position, that.position) && Objects.equals(comment, that.comment)  && Objects.equals(sentDate, that.sentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, position, comment, sentDate);
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setImageTasks(List<ImageTask> imageTasks) {
        this.imageTasks = imageTasks;
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public List<ImageTask> getImageTasks() {
        return imageTasks;
    }

    public void setTaskComplete(TaskComplete taskComplete) {
        this.taskComplete = taskComplete;
    }

    public TaskComplete getTaskComplete() {
        return taskComplete;
    }
}
