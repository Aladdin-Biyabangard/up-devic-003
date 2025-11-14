package com.team.updevic001.controllers;

import com.team.updevic001.model.dtos.page.CustomPage;
import com.team.updevic001.model.dtos.page.CustomPageRequest;
import com.team.updevic001.model.dtos.request.EmailDraftRequest;
import com.team.updevic001.model.dtos.response.notification.EmailDraftResponse;
import com.team.updevic001.model.enums.EmailStatus;
import com.team.updevic001.services.impl.notification.EmailDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/email-drafts")
@RequiredArgsConstructor
public class EmailDraftController {

    private final EmailDraftService emailDraftService;

    @PostMapping(path = "/draft/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void saveDraft(
            @ModelAttribute("emailDraftRequest") EmailDraftRequest emailDraftRequest,
            @RequestPart(value = "file", required = false) final MultipartFile attachment) {

        emailDraftService.saveEmailDraft(emailDraftRequest, attachment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailDraftResponse> getDraft(@PathVariable("id") Long draftId) {
        EmailDraftResponse response = emailDraftService.getEmailDraft(draftId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public CustomPage<EmailDraftResponse> getDrafts(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) EmailStatus status,
            CustomPageRequest pageRequest) {

        return emailDraftService.getDrafts(pageRequest, subject, status);
    }
}
