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
        return userGroupRepository.findByIdAndDeletedAtIsNull(groupId).isPresent();
    }

    public Optional<UserGroup> findUserGroupById(String id) {
        return userGroupRepository.findByIdAndDeletedAtIsNull(id);
    }

    public List<UserGroup> findAllUserGroup() {
        return userGroupRepository.findAllByDeletedAtIsNull();
    }

    public Optional<UserGroup> findUserGroupByName(String name) {
        return userGroupRepository.findByNameAndDeletedAtIsNull(name);
    }

    public Optional<UserGroup> findUserGroupByIdAndNotSuperAdmin(String id) {
        return userGroupRepository.findByIdAndNameNotAndDeletedAtIsNull(id, "Super Admin");
    }

}