package ru.otus.minioBot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.time.LocalDateTime;
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
    private int chatId;

//    @Column(name = "photo_notes")
//    @CollectionTable(name = "notes_images")
//    @ElementCollection(fetch = FetchType.EAGER)
//    private List<String> images;

    @Column(name = "position")
    private String position;

    @Column(name = "comment")
    private String comment;

    @Column(name = "task_complete")
    private TaskComplete taskComplete;

    @Column(name = "status")
    private NotificationStatus status = NotificationStatus.SENT;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;;


//    private List<String, String> photos;

    public Notification(int chatId, String position, String comment, TaskComplete taskComplete, LocalDateTime sentDate) {
        this.chatId = chatId;
        this.position = position;
        this.comment = comment;
        this.taskComplete = taskComplete;
        this.sentDate = sentDate;
    }

    @Override
    public String toString() {
        return "Notification: " +
                ", chatId = " + chatId +
                ", position = " + position +
                ", comment = '" + comment + '\'' +
                ", notificationDate = " + sentDate +
                ", status = " + status;
    }

    public void setAsSent() {
        this.sentDate = LocalDateTime.now();
        this.status = NotificationStatus.SENT;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getChatId() {
        return chatId;
    }

    public String getPosition() {
        return position;
    }

    public String getComment() {
        return comment;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public LocalDateTime getNotificationDate() {
        return sentDate;
    }

    public void setChatId(Long chatId) {
        this.chatId = Math.toIntExact(chatId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification that)) return false;
        return chatId == that.chatId && Objects.equals(id, that.id) && Objects.equals(position, that.position) && Objects.equals(comment, that.comment) && status == that.status && Objects.equals(sentDate, that.sentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, position, comment, status, sentDate);
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public void setNotificationDate(LocalDateTime notificationDate) {
        this.sentDate = sentDate;
    }
}
