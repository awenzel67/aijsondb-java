package io.github.awenzel67.controller;

import io.github.awenzel67.model.Options;
import io.github.awenzel67.model.QAEntry;
import io.github.awenzel67.service.Analyser;
import io.github.awenzel67.util.MarkdownUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@SessionAttributes({"history", "uploadedFilePath", "fileUploaded", "options"})
public class WebController {

    private final Analyser analyser;

    public WebController() {
        this.analyser = new Analyser();
    }

    @GetMapping("/")
    public String index(Model model) {
        if (!model.containsAttribute("history")) {
            model.addAttribute("history", new ArrayList<QAEntry>());
        }
        if (!model.containsAttribute("fileUploaded")) {
            model.addAttribute("fileUploaded", false);
        }
        if (!model.containsAttribute("options")) {
            model.addAttribute("options", new Options());
        }
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute("history") List<QAEntry> history,
            @ModelAttribute("options") Options options,
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
            
            // Store in session
            String uploadedFilePath = filePath.toString();
            redirectAttributes.addFlashAttribute("uploadedFilePath", uploadedFilePath);
            redirectAttributes.addFlashAttribute("fileUploaded", true);
            redirectAttributes.addFlashAttribute("options", options);
            
            // Clear history when new file is uploaded
            if (history != null) {
                history.clear();
            }
            redirectAttributes.addFlashAttribute("history", history);
            
            // Import data into analyser
            analyser.importData(uploadedFilePath);
            
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
            @ModelAttribute("options") Options options,
            @ModelAttribute("history") List<QAEntry> history,
            Model model) {
        
        boolean fileUploaded = (Boolean) model.getAttribute("fileUploaded");
        if (!fileUploaded) {
            model.addAttribute("error", "Please upload a file first.");
            model.addAttribute("fileUploaded", false);
            return "index";
        }
        try {
            String answer = analyser.analyse(question, options);
            
            // Convert markdown to HTML
            String questionHtml = MarkdownUtil.markdownToHtml(question);
            String answerHtml = MarkdownUtil.markdownToHtml(answer);
            
            // Add to history
            if (history == null) {
                history = new ArrayList<>();
            }
            history.add(new QAEntry(questionHtml, answerHtml));
            
            model.addAttribute("history", history);
            model.addAttribute("answer", answerHtml);
            model.addAttribute("question", questionHtml);
            model.addAttribute("fileUploaded", true);
            model.addAttribute("options", options);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to analyze question: " + e.getMessage());
        }
        return "index";
    }

    @PostMapping("/reset")
    public String resetSession(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }
}
