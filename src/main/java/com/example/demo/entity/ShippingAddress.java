package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 🔹 Đổi tên cho khớp frontend
    private String fullName;     // ✅ Trùng với field "fullName" trên React
    private String phone;        // ✅ Trùng với field "phone"
    private String addressLine;
    private String city;
    private String district;
    private String ward;
    private String note;
    private Boolean isDefault;
    private LocalDateTime createdAt;

    // ✅ Liên kết với User
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "password",
            "orders",
            "carts",
            "addresses"
    })
    private User user;

    // ✅ Liên kết với Order (1 order chỉ có 1 địa chỉ)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "shippingAddress"
    })
    private Order order;
}
