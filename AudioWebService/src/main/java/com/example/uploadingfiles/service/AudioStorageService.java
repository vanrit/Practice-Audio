package com.example.uploadingfiles.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import com.example.uploadingfiles.exceptions.StorageException;
import com.example.uploadingfiles.exceptions.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageProperties;
import com.example.uploadingfiles.storage.StorageService;
import com.example.uploadingfiles.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AudioStorageService implements StorageService {

	private final Path rootLocation;

	@Autowired
	public AudioStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	public Path getRootLocation() {
		return rootLocation;
	}

	@Override
	public void store(MultipartFile file, String scope) {
		try {

			String folderName = "";

			if (scope.equals("private")) {
				folderName = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
			}
			else {
				folderName = "public";
			}

			Path saveDirectory = this.rootLocation.resolve(Paths.get(folderName));

			if (!saveDirectory.toFile().exists()) {
				Files.createDirectory(saveDirectory);
			}

			if (file.isEmpty()) {
				throw new StorageException("Audio file is Empty!");
			}
			Path destinationFile = saveDirectory.resolve(
					Paths.get(file.getOriginalFilename()))
					.normalize().toAbsolutePath();
			if (!destinationFile.getParent().equals(saveDirectory.toAbsolutePath())) {
				throw new StorageException(
						"Cannot store audio file outside current directory.");
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile,
					StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		}
	}


	@Override
	public Path load(String recordName, String scope) {

		Path loadDirectory;
		if (scope.equals("private")) {
			String username = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
			loadDirectory = this.rootLocation.resolve(Paths.get(username));
		}
		else {
			loadDirectory = this.rootLocation.resolve(Paths.get("public"));
		}

		return loadDirectory.resolve(recordName);
	}

	@Override
	public Resource loadAsResource(String filename, String scope) {
		try {
			Path file = load(filename, scope);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException(
						"Could not read file: " + filename);

			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(rootLocation);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}
}
