package com.lenin.hotel.hotel.service;

import jakarta.validation.Valid;

import java.util.Map;

public interface ILocationService {
    void createLocation(Map<String, String> location);
}
