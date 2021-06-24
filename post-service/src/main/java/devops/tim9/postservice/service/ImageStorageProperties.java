package devops.tim9.postservice.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "file")
@Component
public class ImageStorageProperties {

	private String uploadDir;

	public ImageStorageProperties() {

	}

	public ImageStorageProperties(String uploadDir) {
		super();
		this.uploadDir = uploadDir;
	}

	public String getUploadDir() {
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}

}
