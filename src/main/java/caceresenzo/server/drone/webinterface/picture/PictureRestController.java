package caceresenzo.server.drone.webinterface.picture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import caceresenzo.libs.stream.StreamUtils;
import caceresenzo.server.drone.webinterface.picture.models.Picture;

@RestController
public class PictureRestController {
	
	@GetMapping(value = "/storage/pictures/{file}", produces = "image/*")
	public ResponseEntity<byte[]> getImageAsResponseEntity(@PathVariable("file") String filename) throws IOException {
		Picture picture = Picture.fromName(filename);
		File file = picture.toFile();
		
		if (!file.exists()) {
			return new ResponseEntity<>(new byte[] {}, HttpStatus.NOT_FOUND);
		}
		
		InputStream inputStream = new FileInputStream(file);
		byte[] media = IOUtils.toByteArray(inputStream);
		StreamUtils.close(inputStream);
		
		return new ResponseEntity<>(media, HttpStatus.OK);
	}
	
}