package com.library.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileStorageUtil {

	@Value("${cloudinary.cloud-name}")
	private String cloudName;

	@Value("${cloudinary.api-key}")
	private String apiKey;

	@Value("${cloudinary.api-secret}")
	private String apiSecret;

	private Cloudinary cloudinary;

	private Cloudinary cloudinary() {
		if (cloudinary == null) {
			cloudinary = new Cloudinary(ObjectUtils.asMap(
					"cloud_name", cloudName,
					"api_key", apiKey,
					"api_secret", apiSecret,
					"secure", true));
		}
		return cloudinary;
	}

	@SuppressWarnings("unchecked")
	public String store(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}
		try {
			Map<String, Object> result = cloudinary().uploader().upload(
					file.getBytes(),
					ObjectUtils.asMap("folder", "library-students"));
			return (String) result.get("secure_url");
		} catch (Exception e) {
			throw new RuntimeException("File upload failed: " + e.getMessage(), e);
		}
	}

	public void delete(String url) {
		if (url == null || url.isBlank() || !url.contains("res.cloudinary.com")) {
			return;
		}
		try {
			String publicId = extractPublicId(url);
			if (publicId != null) {
				cloudinary().uploader().destroy(publicId, ObjectUtils.emptyMap());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String extractPublicId(String url) {
		Matcher matcher = Pattern.compile("/upload/(?:v\\d+/)?(.+)\\.[a-zA-Z0-9]+(?:\\?.*)?$").matcher(url);
		return matcher.find() ? matcher.group(1) : null;
	}
}