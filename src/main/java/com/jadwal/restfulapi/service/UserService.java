package com.jadwal.restfulapi.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.enums.Role;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.dto.UserDTO;
import com.jadwal.restfulapi.repository.UserRepository;
import com.jadwal.restfulapi.util.PasswordHasherMatcher;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHasherMatcher passwordMaker;

    public Optional<User> findUserById(String id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .filter(user -> !user.getRole().equals(Role.SUPERADMIN));
    }

    public List<User> findAllUser() {
        return userRepository.findAllByDeletedAtIsNull()
                .stream()
                .filter(user -> !user.getRole().equals(Role.SUPERADMIN))
                .toList();
    }

    public void deleteUser(User user) {
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public User createUser(UserDTO userDTO, Role role, Category prodi) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setName(userDTO.getName());
        user.setPassword(passwordMaker.hashPassword(userDTO.getPassword()));
        user.setRole(role);
        user.setProdiId(prodi);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User createUser(UserDTO userDTO, Role role) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setName(userDTO.getName());
        user.setPassword(passwordMaker.hashPassword(userDTO.getPassword()));
        user.setRole(role);
        user.setProdiId(null);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User editUser(User editedUser, UserDTO userDTO, Role role, Category prodi) {
        editedUser.setId(UUID.randomUUID().toString());
        editedUser.setUsername(userDTO.getUsername());
        editedUser.setName(userDTO.getName());
        editedUser.setPassword(passwordMaker.hashPassword(userDTO.getPassword()));
        editedUser.setRole(role);
        editedUser.setProdiId(prodi);
        editedUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(editedUser);
    }

    public User editUser(User editedUser, UserDTO userDTO, Role role) {
        editedUser.setId(UUID.randomUUID().toString());
        editedUser.setUsername(userDTO.getUsername());
        editedUser.setName(userDTO.getName());
        editedUser.setRole(role);
        editedUser.setProdiId(null);
        editedUser.setUpdatedAt(LocalDateTime.now());

        if (userDTO.getPassword() != null)
            editedUser.setPassword(passwordMaker.hashPassword(userDTO.getPassword()));
        return userRepository.save(editedUser);
    }
}
