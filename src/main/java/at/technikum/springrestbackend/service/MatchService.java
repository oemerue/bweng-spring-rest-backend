package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.LikeRequestDTO;
import at.technikum.springrestbackend.dto.ProfileDTO;

import java.util.List;

public interface MatchService {
    void like(LikeRequestDTO req);
    List<ProfileDTO> myMatches(Long userId);
    List<ProfileDTO> recommend(Long userId, int page, int size);
}
