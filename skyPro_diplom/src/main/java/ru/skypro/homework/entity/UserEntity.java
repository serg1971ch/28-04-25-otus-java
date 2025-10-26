package ru.skypro.homework.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.skypro.homework.dto.RoleDto;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String phone;
    @OneToOne
    private AvatarEntity avatar;
    @OneToMany
    private List<CommentEntity> comments;
    @Enumerated(EnumType.STRING)
    private RoleDto role;


    public UserEntity(Long id, String email, String firstName, String lastName, String password, String phone, AvatarEntity avatar, List<CommentEntity> comments, RoleDto role) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.phone = phone;
        this.avatar = avatar;
        this.comments = comments;
        this.role = role;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AvatarEntity getAvatar() {
        return avatar;
    }

    public void setAvatar(AvatarEntity avatar) {
        this.avatar = avatar;
    }

    public List<CommentEntity> getComments() {
        return comments;
    }

    public void setComments(List<CommentEntity> comments) {
        this.comments = comments;
    }

    public RoleDto getRole() {
        return role;
    }

    public void setRole(RoleDto role) {
        this.role = role;
    }

    public UserEntity() {
    }
}
