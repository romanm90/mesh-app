package rm.test.meshapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rm.test.meshapp.dto.TransferRequest;
import rm.test.meshapp.dto.TransferResponse;
import rm.test.meshapp.security.CurrentUser;
import rm.test.meshapp.service.TransferService;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final CurrentUser currentUser;

    @PostMapping
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transferService.transfer(currentUser.requireUserId(), request.toUserId(), request.value());
    }
}
