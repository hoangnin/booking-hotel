package com.lenin.hotel.authentication.service;

public interface IAdminService {
    Object getUsers(int page, int size);

    Object searchUser(String keyword);
}
