package com.demo.warehouse.storage;

public interface ImageStorageService {
    String upload(byte[] content, String fileName, String contentType);
}
