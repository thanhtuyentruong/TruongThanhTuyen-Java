package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentRepository paymentRepo;
    private final ShippingAddressRepository shippingRepo;
    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    /**
     * ✅ Lấy tất cả đơn hàng — gồm user, items, payment, address
     */
    @Transactional
    public List<Order> findAll() {
        List<Order> orders = orderRepo.findAll();
        orders.forEach(this::loadRelations);
        return orders;
    }

    /**
     * ✅ Lấy đơn hàng theo ID
     */
    @Transactional
    public Order findById(Integer id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + id));
        loadRelations(order);
        return order;
    }

    /**
     * ✅ Lấy danh sách đơn hàng theo userId
     */
    @Transactional
    public List<Order> findByUserId(Integer userId) {
        User user = userRepo.findById(userId.longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));
        List<Order> orders = orderRepo.findByUserOrderByCreatedAtDesc(user);
        orders.forEach(this::loadRelations);
        return orders;
    }

    /**
     * ✅ Lấy danh sách đơn hàng theo email user
     */
    @Transactional
    public List<Order> findByUserEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + email));
        List<Order> orders = orderRepo.findByUserOrderByCreatedAtDesc(user);
        orders.forEach(this::loadRelations);
        return orders;
    }

    /**
     * ✅ Load các bảng liên quan (items, payment, shipping)
     */
    private void loadRelations(Order order) {
        if (order == null)
            return;

        if (order.getItems() == null || order.getItems().isEmpty()) {
            order.setItems(orderItemRepo.findByOrder_Id(order.getId()));
        }

        order.setPayment(paymentRepo.findByOrder_Id(order.getId()).orElse(null));
        order.setShippingAddress(shippingRepo.findByOrder_Id(order.getId()).orElse(null));
    }

    /**
     * ✅ Thanh toán từ giỏ hàng
     */
    @Transactional
    public Order checkout(Integer userId, Integer cartId, ShippingAddress address, String paymentMethod) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng ID: " + cartId));

        List<CartItem> cartItems = cartItemRepo.findByCart_Id(cartId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể thanh toán!");
        }

        User user = userRepo.findById(userId.longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));

        // ✅ Tạo đơn hàng
        Order order = Order.builder()
                .user(user)
                .total(cart.getTotal())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        order = orderRepo.save(order);

        // ✅ Thêm items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cartItems) {
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .quantity(ci.getQuantity())
                    .price(ci.getProduct().getPrice())
                    .build();
            orderItems.add(oi);
        }
        orderItemRepo.saveAll(orderItems);

        // ✅ Payment
        Payment payment = Payment.builder()
                .order(order)
                .amount(cart.getTotal())
                .status("PENDING")
                .paymentMethod(paymentMethod != null ? paymentMethod : "COD")
                .paymentDate(LocalDateTime.now())
                .build();
        paymentRepo.save(payment);

        // ✅ Shipping
        ShippingAddress shipping = ShippingAddress.builder()
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .district(address.getDistrict())
                .ward(address.getWard())
                .note(address.getNote())
                .isDefault(address.getIsDefault())
                .createdAt(LocalDateTime.now())
                .user(user)
                .order(order)
                .build();
        shippingRepo.save(shipping);

        // ✅ Dọn giỏ hàng
        cartItemRepo.deleteAll(cartItems);
        cart.setTotal(0.0);
        cartRepo.save(cart);

        // ✅ Gán liên kết
        order.setItems(orderItems);
        order.setPayment(payment);
        order.setShippingAddress(shipping);

        return orderRepo.save(order);
    }

    /**
     * ✅ Mua ngay 1 sản phẩm — fix giá chính xác
     */
    @Transactional
    public Order buyNow(Integer userId, Integer productId, Double price, Integer quantity,
            ShippingAddress address, String paymentMethod) {

        // 1️⃣ Lấy user & product
        User user = userRepo.findById(userId.longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productId));

        // 2️⃣ Kiểm tra hợp lệ
        if (quantity == null || quantity <= 0)
            throw new RuntimeException("Số lượng không hợp lệ");
        if (price == null || price <= 0)
            throw new RuntimeException("Giá không hợp lệ");

        // 3️⃣ Tính giá thực tế
        Double discountPrice = product.getDiscountPrice();
        Double originalPrice = product.getPrice();
        double realPrice = originalPrice;

        // 🔹 Ưu tiên giá giảm nếu có
        if (discountPrice != null && discountPrice > 0) {
            realPrice = discountPrice;
        }

        // 🔹 Nếu frontend gửi giá nhỏ hơn giá gốc → chấp nhận giá đó (do flash sale)
        if (price < realPrice) {
            realPrice = price;
        }

        System.out.println("💰 buyNow() ✅ realPrice dùng = " + realPrice +
                " | giá gửi lên = " + price +
                " | gốc = " + originalPrice +
                " | discount = " + discountPrice);

        // 4️⃣ Tạo đơn hàng
        double total = realPrice * quantity;

        Order order = Order.builder()
                .user(user)
                .status("PENDING")
                .total(total)
                .createdAt(LocalDateTime.now())
                .build();
        orderRepo.save(order);

        // 5️⃣ Tạo OrderItem
        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(realPrice)
                .build();
        orderItemRepo.save(item);

        // 6️⃣ Tạo Payment
        Payment payment = Payment.builder()
                .order(order)
                .amount(total)
                .paymentMethod(paymentMethod)
                .status("PENDING")
                .paymentDate(LocalDateTime.now())
                .build();
        paymentRepo.save(payment);

        // 7️⃣ Tạo ShippingAddress
        ShippingAddress ship = ShippingAddress.builder()
                .order(order)
                .user(user)
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .district(address.getDistrict())
                .ward(address.getWard())
                .note(address.getNote())
                .isDefault(address.getIsDefault())
                .createdAt(LocalDateTime.now())
                .build();
        shippingRepo.save(ship);

        // 8️⃣ Gán liên kết an toàn
        order.setItems(new ArrayList<>(List.of(item)));
        order.setPayment(payment);
        order.setShippingAddress(ship);

        return orderRepo.save(order);
    }

    /**
     * ✅ Cập nhật trạng thái đơn hàng
     */
    @Transactional
    public Order updateStatus(Integer orderId, String status) {
        Order order = findById(orderId);
        order.setStatus(status);
        return orderRepo.save(order);
    }

    /**
     * ✅ Xóa đơn hàng và dữ liệu liên quan
     */
    @Transactional
    public void delete(Integer orderId) {
        Order order = findById(orderId);
        orderItemRepo.deleteAll(orderItemRepo.findByOrder_Id(orderId));
        paymentRepo.findByOrder_Id(orderId).ifPresent(paymentRepo::delete);
        shippingRepo.findByOrder_Id(orderId).ifPresent(shippingRepo::delete);
        orderRepo.delete(order);
    }
}
