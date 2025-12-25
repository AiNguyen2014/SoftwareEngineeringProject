package ecommerce.shoestore.review.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long orderItemId;
    private Long shoeId;
    private Integer rate;
    private String comment;
}
