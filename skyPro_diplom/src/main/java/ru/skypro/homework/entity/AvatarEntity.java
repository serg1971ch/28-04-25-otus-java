package ru.skypro.homework.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Table(name = "avatars")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@Entity
@Builder
public class AvatarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String path;
    private long fileSize;
    private String mediaType;
    @JsonIgnore
    private byte[] data;
    @OneToOne
    @JsonIgnore
    private UserEntity user;

    public AvatarEntity() {
    }
}

