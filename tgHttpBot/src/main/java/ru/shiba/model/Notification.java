package ru.otus.httpBot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @Column(name = "position")
    private String position;

    @Column(name= "comment")
    private String comment;

    @Column(name= "status")
    private NotificationStatus status = NotificationStatus.COMPLETED;

    @Column(name= "notification_date")
    private LocalDateTime notificationDate;

    public Notification(int chatId, String position,  String comment, NotificationStatus status, LocalDateTime now) {
        this.chatId = chatId;
        this.position = position;
        this.comment = comment;
        this.status = status;
        this.notificationDate = now;
    }

    @Override
    public String toString() {
        return "Notification: " +
                ", chatId = " + chatId +
                ", position = " + position +
                ", comment = '" + comment + '\'' +
                ", notificationDate = " + notificationDate +
                ", status = " + status;
    }

    public void setAsSent() {
        this.notificationDate = LocalDateTime.now();
        this.status = NotificationStatus.COMPLETED;
    }


    public int getChatId() {
        return  chatId;
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
        return notificationDate;
    }

    public void setChatId(Long chatId) {
        this.chatId = Math.toIntExact(chatId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification that)) return false;
        return chatId == that.chatId && Objects.equals(id, that.id) && Objects.equals(position, that.position) && Objects.equals(comment, that.comment) && status == that.status && Objects.equals(notificationDate, that.notificationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, position, comment, status, notificationDate);
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
        this.notificationDate = notificationDate;
    }
}
