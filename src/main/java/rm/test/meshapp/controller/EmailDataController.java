package rm.test.meshapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rm.test.meshapp.dto.EmailRequest;
import rm.test.meshapp.dto.EmailResponse;
import rm.test.meshapp.security.CurrentUser;
import rm.test.meshapp.service.EmailDataService;

@RestController
@RequestMapping("/api/users/me/emails")
@RequiredArgsConstructor
public class EmailDataController {

    private final EmailDataService emailDataService;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmailResponse add(@Valid @RequestBody EmailRequest request) {
        return emailDataService.add(currentUser.requireUserId(), request);
    }

    @PutMapping("/{id}")
    public EmailResponse update(@PathVariable Long id, @Valid @RequestBody EmailRequest request) {
        return emailDataService.update(currentUser.requireUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        emailDataService.delete(currentUser.requireUserId(), id);
    }
}
