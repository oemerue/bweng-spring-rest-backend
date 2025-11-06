package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.dto.LikeRequestDTO;
import at.technikum.springrestbackend.dto.ProfileDTO;
import at.technikum.springrestbackend.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matches")
public class MatchController {

    private final MatchService matches;

    public MatchController(MatchService matches) { this.matches = matches; }

    @PostMapping("/like")
    @ResponseStatus(HttpStatus.CREATED)
    public void like(@RequestBody @Valid LikeRequestDTO req) {
        matches.like(req); // erzeugt ggf. ein Match (beidseitiger Like)
    }

    @GetMapping("/my/{userId}")
    public List<ProfileDTO> myMatches(@PathVariable Long userId) {
        return matches.myMatches(userId);
    }

    @GetMapping("/recommendations/{userId}")
    public List<ProfileDTO> recommendations(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return matches.recommend(userId, page, size);
    }
}
