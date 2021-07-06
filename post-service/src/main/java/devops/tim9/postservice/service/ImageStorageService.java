package devops.tim9.postservice.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import devops.tim9.postservice.exception.ImageStorageException;
import devops.tim9.postservice.model.DriveQuickstart;

@Service
public class ImageStorageService {
	
	@Value("${file.upload-dir}")
	private String fileUploadDir;
	
	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

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

	public String storeImage(MultipartFile file, String username) throws Exception{
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		List<String> webLinks=new ArrayList<String>();
		File item = new File();
		Permission permission = new Permission();
		permission.setRole("reader");
		permission.setType("anyone");
		List<Permission> permis = new ArrayList<Permission>();
		File fileMetadata = new File();
		fileMetadata.setName(file.getName());
		java.io.File filePath = new java.io.File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream( filePath );
        fos.write( file.getBytes() );
        fos.close();
		if (!filePath.exists()) {
			throw new ImageStorageException("Invalid file name!");
		} else {
			FileContent mediaContent = new FileContent("image/jpeg",filePath);
			Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
					DriveQuickstart.getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();

			item = service.files().create(fileMetadata, mediaContent).setFields("id,webViewLink").execute();
			Permission perm = service.permissions().create(item.getId(), permission).execute();
			permis.add(perm);
			item.setPermissions(permis);
			webLinks.add(item.getWebViewLink());
			String[] els = item.getWebViewLink().split("/");
			return els[0]+"//"+els[2]+"/"+"uc?export=view&id="+els[5];
			
		}
	}

}
