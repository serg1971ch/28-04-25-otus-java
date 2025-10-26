package ru.skypro.homework.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;

@Table(name = "ads")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@Entity
public class AdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ImageAdEntity image;
    private Integer price;
    private String title;
    private String description;
    @ManyToOne
    private UserEntity author;
    @OneToMany
    private List<CommentEntity> comments;

    public AdEntity() {
    }
    public void setComment(CommentEntity comment) {
        comments.add(comment);
    }
}
