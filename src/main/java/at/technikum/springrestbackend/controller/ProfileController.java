package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.CreateProfileDTO;
import at.technikum.springrestbackend.dto.ProfileResponseDTO;
import at.technikum.springrestbackend.dto.UpdateProfileDTO;
import at.technikum.springrestbackend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProfileResponseDTO> all() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponseDTO> byId(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProfileResponseDTO> create(@Valid @RequestBody CreateProfileDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateProfileDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
