package ecommerce.shoestore.review;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.review.dto.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<?> addReview(
            @RequestBody ReviewRequest reviewRequest,
            @AuthenticationPrincipal User currentUser // Lấy user từ context Security
    ) {
        try {
            Review savedReview = reviewService.createReview(reviewRequest, currentUser);
            return ResponseEntity.ok("Đánh giá thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}