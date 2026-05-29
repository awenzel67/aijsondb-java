package io.github.awenzel67.controller;

import io.github.awenzel67.model.Options;
import io.github.awenzel67.service.Analyser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@Controller
public class WebController {

    private final Analyser analyser;
    private String uploadedFilePath;
    private boolean fileUploaded = false;

    public WebController() {
        this.analyser = new Analyser();
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("fileUploaded", fileUploaded);
        model.addAttribute("options", new Options());
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/";
        }

        try {
            // Save the file temporarily
            String uploadDir = "uploads";
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            uploadedFilePath = filePath.toString();
            
            // Import data into analyser
            analyser.importData(uploadedFilePath);
            fileUploaded = true;
            
            redirectAttributes.addFlashAttribute("message", 
                "File uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to upload file: " + e.getMessage());
        }
        
        return "redirect:/";
    }

    @PostMapping("/ask")
    public String handleQuestion(
            @RequestParam("question") String question,
            Options options,
            Model model) {
        
        if (!fileUploaded) {
            model.addAttribute("error", "Please upload a file first.");
            model.addAttribute("fileUploaded", false);
            return "index";
        }
        try {
            String answer = analyser.analyse(question, options);
            model.addAttribute("answer", answer);
            model.addAttribute("question", question);
            model.addAttribute("fileUploaded", true);
            model.addAttribute("options", options);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to analyze question: " + e.getMessage());
        }
        return "index";
    }
}
