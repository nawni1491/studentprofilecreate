package com.stock.userproflie.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.stock.userproflie.dto.UserProfileDto;
import com.stock.userproflie.model.UserProfile;
import com.stock.userproflie.service.UserProfileService;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/userprofile")
public class UserProfileController {

    @Autowired
    private UserProfileService service;

    // ---------------- List Profiles ----------------
    @GetMapping
    public String listProfiles(Model model) {
        List<UserProfile> userprofiles = service.getAllProfiles();
        model.addAttribute("userprofiles", userprofiles);
        return "userprofile/list";
    }

    // ---------------- Create Form ----------------
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("userProfileDto", new UserProfileDto());
        return "userprofile/create";
    }

    // ---------------- Save Profile ----------------
    @PostMapping("/save")
    public String saveProfile(@ModelAttribute UserProfileDto dto, MultipartFile file, Model model) {
        try {
            service.createProfile(dto, file);
            model.addAttribute("successMessage", "Profile created successfully!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "userprofile/create";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "File upload failed!");
            return "userprofile/create";
        }
        return "redirect:/userprofile"; // ✅ FIXED
    }



    // ---------------- Edit Form ----------------
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<UserProfile> profileOpt = service.getProfileById(id);
        if (profileOpt.isEmpty()) return "redirect:/userprofile";
        model.addAttribute("userProfile", profileOpt.get());
        return "userprofile/edit";
    }

    // ---------------- Update Profile ----------------
    @PostMapping("/update/{id}")
    public String updateProfile(@PathVariable Long id,
                                @Valid @ModelAttribute("userProfile") UserProfileDto dto,
                                BindingResult bindingResult,
                                @RequestParam(value = "file", required = false) MultipartFile file,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("id", id);
            return "userprofile/edit";
        }

        try {
            Optional<UserProfile> updated = service.updateProfile(id, dto, file);
            if (updated.isEmpty()) return "redirect:/userprofile";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Error updating profile or uploading file.");
            e.printStackTrace();
            service.getProfileById(id).ifPresent(profile -> model.addAttribute("userProfile", profile));
            return "userprofile/edit";
        }

        return "redirect:/userprofile";
    }

    // ---------------- Delete Profile ----------------
    @GetMapping("/delete/{id}")
    public String deleteProfile(@PathVariable Long id) {
        service.deleteProfile(id);
        return "redirect:/userprofile";
    }

    // ---------------- Search ----------------
    @GetMapping("/search")
    public String searchForm() {
        return "userprofile/search";
    }

    @PostMapping("/search")
    public String searchResults(@RequestParam String keyword, Model model) {
    	List<UserProfile> results = service.searchByFullname(keyword);
        model.addAttribute("userprofiles", results);
        model.addAttribute("keyword", keyword);
        if (results.isEmpty()) model.addAttribute("message", "No profiles found for: " + keyword);
        return "userprofile/list";
    }

}

