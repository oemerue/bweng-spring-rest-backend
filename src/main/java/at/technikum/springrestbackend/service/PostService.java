package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.constant.AppConstants;
import at.technikum.springrestbackend.dto.PostCreateDTO;
import at.technikum.springrestbackend.dto.PostDTO;
import at.technikum.springrestbackend.dto.PostUpdateDTO;
import at.technikum.springrestbackend.entity.Post;
import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.repository.PostRepository;
import at.technikum.springrestbackend.util.ImageUtil;
import at.technikum.springrestbackend.util.SecurityUtil;
import at.technikum.springrestbackend.util.StringUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final MinioService minioService;

    public PostService(PostRepository postRepository,
                       MinioService minioService) {
        this.postRepository = postRepository;
        this.minioService = minioService;
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getAll(String query, Pageable pageable) {
        Page<Post> page = StringUtil.getNonBlank(query)
                .map(q -> postRepository
                        .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                                q, q, pageable))
                .orElseGet(() -> postRepository.findAll(pageable));

        return page.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public PostDTO getById(Long id) {
        return toDTO(findPostOrThrow(id));
    }

    public PostDTO create(PostCreateDTO dto) {
        Profile author = SecurityUtil.currentProfileOrThrow();

        String title = dto.getTitle() == null
                ? ""
                : dto.getTitle().trim();
        String content = dto.getContent() == null
                ? ""
                : dto.getContent().trim();

        if (title.isBlank() || content.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title and content are required");
        }

        Post post = new Post();
        post.setAuthor(author);
        post.setTitle(title);
        post.setContent(content);

        return toDTO(postRepository.save(post));
    }

    public PostDTO update(Long id, PostUpdateDTO dto) {
        Post post = findPostOrThrow(id);
        SecurityUtil.ensureOwnerOrAdmin(post.getAuthor().getId());

        boolean changed = false;

        if (StringUtil.getNonBlank(dto.getTitle()).isPresent()) {
            post.setTitle(dto.getTitle().trim());
            changed = true;
        }

        if (StringUtil.getNonBlank(dto.getContent()).isPresent()) {
            post.setContent(dto.getContent().trim());
            changed = true;
        }

        if (!changed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No fields to update");
        }

        return toDTO(postRepository.save(post));
    }

    public void delete(Long id) {
        Post post = findPostOrThrow(id);
        SecurityUtil.ensureOwnerOrAdmin(post.getAuthor().getId());

        // delete file first (safe even if key is null)
        minioService.delete(post.getImageObjectKey());

        postRepository.delete(post);
    }

    public PostDTO uploadImage(Long postId, MultipartFile file) {
        Post post = findPostOrThrow(postId);
        SecurityUtil.ensureOwnerOrAdmin(post.getAuthor().getId());

        ImageUtil.validateImageFile(file);

        minioService.delete(post.getImageObjectKey());

        String objectKey = AppConstants.POST_IMAGES_PATH
                + post.getId()
                + "/"
                + UUID.randomUUID()
                + ImageUtil.getExtension(file.getContentType());

        minioService.upload(objectKey, file);

        post.setImageObjectKey(objectKey);
        return toDTO(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getAllForAdmin(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::toDTO);
    }

    public void deleteAsAdmin(Long id) {
        Post post = findPostOrThrow(id);
        minioService.delete(post.getImageObjectKey());
        postRepository.delete(post);
    }

    private Post findPostOrThrow(Long id) {
        return postRepository.findWithAuthorById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Post not found with id: " + id));
    }

    private PostDTO toDTO(Post post) {
        return new PostDTO(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getUsernameDisplay(),
                post.getTitle(),
                post.getContent(),
                ImageUtil.buildFileUrl(post.getImageObjectKey()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
