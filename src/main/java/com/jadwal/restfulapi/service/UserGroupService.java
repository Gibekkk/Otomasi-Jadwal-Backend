package com.jadwal.restfulapi.service;

import java.util.Optional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadwal.restfulapi.model.UserGroup;
import com.jadwal.restfulapi.repository.UserGroupRepository;

@Service
public class UserGroupService {

    @Autowired
    private UserGroupRepository userGroupRepository;

    public Boolean isProdiExistById(String groupId) {
        return userGroupRepository.findById(groupId).isPresent();
    }

    public Optional<UserGroup> findUserGroupById(String id) {
        return userGroupRepository.findById(id);
    }

    public List<UserGroup> findAllUserGroup() {
        return userGroupRepository.findAll();
    }

    public Optional<UserGroup> findUserGroupByName(String name) {
        return userGroupRepository.findByName(name);
    }

    public Optional<UserGroup> findUserGroupByIdAndNotSuperAdmin(String id) {
        return userGroupRepository.findByIdAndNameNot(id, "Super Admin");
    }

    public List<UserGroup> findAllAndNotSuperAdmin() {
        return userGroupRepository.findByNameNot("Super Admin");
    }

}