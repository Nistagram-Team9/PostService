package devops.tim9.postservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import devops.tim9.postservice.exception.ImageStorageException;

@Service
public class ImageStorageService {
	
	@Value("${file.upload-dir}")
	private String fileUploadDir;

	private final Path fileStorageLocation;

	@Autowired
	public ImageStorageService(ImageStorageProperties fileStorageProperties)
			throws ImageStorageException{
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new ImageStorageException("Could not create the directory where the uploaded files will be stored.",
					ex);
		}
	}

	public String storeImage(MultipartFile file, String username) throws ImageStorageException {
		String fileName = null;
		if (file.getOriginalFilename() != null) {
			fileName = username + "_" + StringUtils.cleanPath(file.getOriginalFilename());
		} else {
			throw new ImageStorageException("Invalid file name!");
		}

		try {
			// Check if the file's name contains invalid characters
			if (fileName.contains("..")) {
				throw new ImageStorageException("Sorry! Filename contains invalid path sequence " + fileName);
			}

			// Copy file to the target location (Replacing existing file with the same name)
			Path targetLocation = this.fileStorageLocation.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			return fileName;
		} catch (IOException ex) {
			throw new ImageStorageException("Could not store file " + fileName + ". Please try again!", ex);
		}
	}

}
