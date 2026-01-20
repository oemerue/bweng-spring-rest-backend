package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.constant.AppConstants;
import at.technikum.springrestbackend.dto.PostCreateDTO;
import at.technikum.springrestbackend.dto.PostDTO;
import at.technikum.springrestbackend.dto.PostUpdateDTO;
import at.technikum.springrestbackend.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(
        value = "/api/posts",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<Page<PostDTO>> getAll(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return ResponseEntity.ok(postService.getAll(q, pageable));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<PostDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDTO> create(
            @Valid @RequestBody PostCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.create(dto));
    }

    @PutMapping(
            value = "/{id:\\d+}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateDTO dto
    ) {
        return ResponseEntity.ok(postService.update(id, dto));
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            value = "/{id:\\d+}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDTO> uploadImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(postService.uploadImage(id, file));
    }

    private Sort parseSort(String sort) {
        try {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            String direction = (parts.length > 1)
                    ? parts[1].trim().toLowerCase()
                    : "desc";

            if (!AppConstants.ALLOWED_POST_SORT_FIELDS.contains(field)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid sort field");
            }

            return "asc".equals(direction)
                    ? Sort.by(field).ascending()
                    : Sort.by(field).descending();

        } catch (ResponseStatusException e) {
            // keep correct error codes for frontend
            throw e;
        } catch (Exception e) {
            // fallback: stable default sorting
            return Sort.by(AppConstants.DEFAULT_SORT_FIELD).descending();
        }
    }
}
