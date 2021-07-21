package com.example.uploadingfiles;

import com.example.uploadingfiles.repository.AudioRepository;
import com.example.uploadingfiles.repository.UsersRepository;
import com.example.uploadingfiles.user.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.example.uploadingfiles.storage.StorageProperties;
import com.example.uploadingfiles.storage.StorageService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class AudioUploadingApplication {


	public static void main(String[] args) {
		SpringApplication.run(AudioUploadingApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService, UsersRepository usersRepository, AudioRepository audioRepository) {
		return (args) -> {
			storageService.init();

			if (usersRepository.findByUsername("admin") == null) {
				User admin = new User();
				BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
				admin.setFirstName("admin");
				admin.setLastName("admin");
				admin.setUsername("admin");
				admin.setPassword(passwordEncoder.encode("q2GkFd23"));
				usersRepository.save(admin);
			}
		};
	}
}
