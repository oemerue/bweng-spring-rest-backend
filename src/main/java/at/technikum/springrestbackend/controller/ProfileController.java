package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.CreateProfileDTO;
import at.technikum.springrestbackend.dto.ProfileDTO;
import at.technikum.springrestbackend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
public class ProfileController {
    private final ProfileService profiles;

    public ProfileController(ProfileService profiles) { this.profiles = profiles; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileDTO create(@RequestBody @Valid CreateProfileDTO req) {
        return profiles.create(req);
    }

    @GetMapping("/{userId}")
    public ProfileDTO get(@PathVariable Long userId) {
        return profiles.getByUserId(userId);
    }

    @PutMapping("/{userId}")
    public ProfileDTO update(@PathVariable Long userId,
                             @RequestBody @Valid CreateProfileDTO req) {
        return profiles.update(userId, req);
    }
}
