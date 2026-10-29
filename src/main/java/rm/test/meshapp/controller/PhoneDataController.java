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
import rm.test.meshapp.dto.PhoneRequest;
import rm.test.meshapp.dto.PhoneResponse;
import rm.test.meshapp.security.CurrentUser;
import rm.test.meshapp.service.PhoneDataService;

@RestController
@RequestMapping("/api/users/me/phones")
@RequiredArgsConstructor
public class PhoneDataController {

    private final PhoneDataService phoneDataService;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PhoneResponse add(@Valid @RequestBody PhoneRequest request) {
        return phoneDataService.add(currentUser.requireUserId(), request);
    }

    @PutMapping("/{id}")
    public PhoneResponse update(@PathVariable Long id, @Valid @RequestBody PhoneRequest request) {
        return phoneDataService.update(currentUser.requireUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        phoneDataService.delete(currentUser.requireUserId(), id);
    }
}
