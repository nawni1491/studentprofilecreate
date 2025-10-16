package com.stock.userproflie.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.stock.userproflie.dto.UserProfileDto;
import com.stock.userproflie.model.UserProfile;
import com.stock.userproflie.repository.UserProfileRepository;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository repository;

    private static final String UPLOAD_DIR = "uploads/";
    private static final String DEFAULT_PROFILE_PIC = "i3.jpg";

    // --- File Handling ---
    private Path getUploadDirectory() throws IOException {
        Path path = Paths.get(UPLOAD_DIR);
        if (!Files.exists(path)) Files.createDirectories(path);
        return path;
    }

    private String handleFileUpload(MultipartFile file, String existingProfilePicture) throws IOException {
        if (file != null && !file.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), getUploadDirectory().resolve(filename));
            return filename;
        } else if (existingProfilePicture != null && !existingProfilePicture.equals(DEFAULT_PROFILE_PIC)) {
            return existingProfilePicture;
        } else {
            return DEFAULT_PROFILE_PIC;
        }
    }

    // --- DTO to Entity ---
    private UserProfile convertDtoToEntity(UserProfileDto dto) {
        UserProfile profile = new UserProfile();
        profile.setName(dto.getName());
        profile.setEmail(dto.getEmail());
        profile.setAddress(dto.getAddress());
        profile.setGender(dto.getGender());
        profile.setPhoneNumber(dto.getPhoneNumber());
        return profile;
    }

    // --- CRUD Operations ---

    public List<UserProfile> getAllProfiles() { return repository.findAll(); }

    public Optional<UserProfile> getProfileById(Long id) { return repository.findById(id); }

    public UserProfile createProfile(UserProfileDto dto, MultipartFile file) throws IOException {
        // 🔹 Check for duplicate email
        repository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        });

        UserProfile profile = convertDtoToEntity(dto);
        profile.setProfilePicture(handleFileUpload(file, null));
        return repository.save(profile);
    }

    public Optional<UserProfile> updateProfile(Long id, UserProfileDto dto, MultipartFile file) throws IOException {
        return repository.findById(id).map(existing -> {
            // 🔹 Check if email exists for another user
            repository.findByEmail(dto.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new IllegalArgumentException("Email already exists: " + dto.getEmail()); });

            existing.setName(dto.getName());
            existing.setEmail(dto.getEmail());
            existing.setAddress(dto.getAddress());
            existing.setGender(dto.getGender());
            existing.setPhoneNumber(dto.getPhoneNumber());
            try {
                existing.setProfilePicture(handleFileUpload(file, existing.getProfilePicture()));
            } catch (IOException e) { e.printStackTrace(); }
            return repository.save(existing);
        });
    }

    public void deleteProfile(Long id) { repository.deleteById(id); }

    public List<UserProfile> searchByFullname(String keyword) {
        return repository.findByNameContainingIgnoreCase(keyword);
    }

}
