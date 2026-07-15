package com.library.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Equivalent of Node's multer + multer-storage-cloudinary combo, but stores
 * files on local disk under `file.upload-dir` (served back from /uploads/**,
 * see WebConfig). Swap this out for a Cloudinary/S3 client for production use.
 */
@Component
public class FileStorageUtil {

	@Value("${file.upload-dir}")
	private String uploadDir;

	public String store(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}
		try {
			Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
			Files.createDirectories(dir);

			String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
			String ext = "";
			int dot = original.lastIndexOf('.');
			if (dot >= 0) ext = original.substring(dot);

			String fileName = UUID.randomUUID() + ext;
			Path target = dir.resolve(fileName);
			Files.copy(file.getInputStream(), target);

			return "/uploads/" + fileName;
		} catch (IOException e) {
			throw new RuntimeException("File upload failed: " + e.getMessage(), e);
		}
	}
}
