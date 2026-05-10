package com.demo.warehouse.storage;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class StubImageStorageService implements ImageStorageService {

    @Override
    public String upload(byte[] content, String fileName, String contentType) {
        return "https://images.example.com/" + UUID.randomUUID() + "-" + fileName;
    }
}
