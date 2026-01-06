package ecommerce.shoestore.review;

import ecommerce.shoestore.order.Order;
import ecommerce.shoestore.order.OrderItem;
import ecommerce.shoestore.order.OrderService;
import ecommerce.shoestore.review.dto.ReviewForm;
import ecommerce.shoestore.review.dto.ReviewRequest;
import ecommerce.shoestore.shoes.ShoesRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/order/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final OrderService orderService;
    private final ShoesRepository shoesRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping("/{orderId}")
    @Transactional(readOnly = true)
    public String showReviewPage(@PathVariable Long orderId, Model model, RedirectAttributes ra) {

        // 1. Kiểm tra nếu đơn hàng này đã được đánh giá rồi thì không cho phép truy cập trang
        if (reviewRepository.existsByOrderItem_OrderId(orderId)) {
            ra.addFlashAttribute("errorMessage", "Đơn hàng này đã được đánh giá trước đó.");
            return "redirect:/order/history";
        }

        List<OrderItem> orderItems = orderService.getOrderItems(orderId);
        if (orderItems == null || orderItems.isEmpty()) {
            return "redirect:/order/history";
        }

        ReviewForm reviewForm = new ReviewForm();
        reviewForm.setOrderId(orderId);

        List<ReviewRequest> requests = orderItems.stream().map(oi -> {
            ReviewRequest req = new ReviewRequest();
            req.setOrderItemId(oi.getOrderItemId());
            req.setShoeId(oi.getShoeId());
            req.setRate(5);
            req.setComment("");
            return req;
        }).collect(Collectors.toList());

        reviewForm.setItems(requests);

        Map<Long, String> productImages = orderItems.stream()
                .map(OrderItem::getShoeId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> shoesRepository.findById(id).map(s -> {
                            if (s.getImages() != null && !s.getImages().isEmpty()) {
                                return s.getImages().get(0).getUrl();
                            }
                            return "/images/default-shoe.png";
                        }).orElse("/images/default-shoe.png"),
                        (existing, replacement) -> existing
                ));

        model.addAttribute("reviewForm", reviewForm);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("productImages", productImages);

        return "user/order-review-page";
    }

    @PostMapping("/submit")
    public String submitAllReviews(@ModelAttribute("reviewForm") ReviewForm reviewForm,
                                   HttpSession session,
                                   RedirectAttributes ra) {

        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            ra.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để đánh giá.");
            return "redirect:/auth/login";
        }

        try {
            if (reviewForm.getItems() == null || reviewForm.getItems().isEmpty()) {
                throw new RuntimeException("Không có sản phẩm nào để đánh giá.");
            }

            for (ReviewRequest item : reviewForm.getItems()) {
                ReviewRequest req = new ReviewRequest();
                req.setOrderItemId(item.getOrderItemId());
                req.setShoeId(item.getShoeId());
                req.setRate(item.getRate());
                req.setComment(item.getComment());

                reviewService.createReview(req, userId);
            }
            ra.addFlashAttribute("successMessage", "Đánh giá của bạn đã được gửi thành công!");

        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/order/history";
    }

}