package ru.upmt.webServerBot.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import ru.upmt.webServerBot.model.User;
import ru.upmt.webServerBot.repository.UserRepository;
import ru.upmt.webServerBot.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUser(String name) {
        log.info("Getting user with name " + name);
        User user = userRepository.getUserByLastName(name.trim());
        log.info("User: " + user.getFirstName());
        return user;
    }

    @Transactional
    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public List<Map<String, String>> getUsers(Long notificationId) {
        List<Map<String, String>> results = userRepository.findUsersByNotificationId(notificationId);

        return results.stream()
                .filter(row -> row.get("firstName") != null && row.get("lastName") != null)
                .map(row -> {
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("firstName", (String) row.get("firstName"));
                    userMap.put("lastName", (String) row.get("lastName"));
                    return userMap;
                })
                .collect(Collectors.toList());
    }
}
