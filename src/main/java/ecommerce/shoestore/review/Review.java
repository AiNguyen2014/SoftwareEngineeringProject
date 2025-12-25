package ecommerce.shoestore.review;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.shoes.Shoes;
import ecommerce.shoestore.order.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"reviewId\"")
    private Long reviewId;

    @Column(name = "rate", nullable = false)
    private Integer rate;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "\"reviewDate\"")
    private LocalDateTime reviewDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"userId\"", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"shoeId\"", nullable = false)
    private Shoes shoes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"orderItemId\"", nullable = false, unique = true)
    private OrderItem orderItem;

    @PrePersist
    public void prePersist() {
        this.reviewDate = LocalDateTime.now();
    }
}