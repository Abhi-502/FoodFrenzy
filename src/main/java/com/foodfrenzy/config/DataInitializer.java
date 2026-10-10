package com.foodfrenzy.config;

import com.foodfrenzy.entity.*;
import com.foodfrenzy.enums.OrderStatus;
import com.foodfrenzy.enums.PaymentMethod;
import com.foodfrenzy.enums.PaymentStatus;
import com.foodfrenzy.enums.TransactionType;
import com.foodfrenzy.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@foodfrenzy.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@12345}")
    private String adminPassword;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           CartRepository cartRepository,
                           OrderRepository orderRepository,
                           InventoryTransactionRepository inventoryTransactionRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing FoodFrenzy database seed data...");

        // 1. Roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CUSTOMER")));

        // 2. Admin User
        User admin = userRepository.findByEmail(adminEmail).orElse(null);
        if (admin == null) {
            admin = new User("Administrator", adminEmail, passwordEncoder.encode(adminPassword), "+1-555-0100", "FoodFrenzy HQ, 100 Gourmet Way, Suite 400");
            admin.addRole(adminRole);
            admin = userRepository.save(admin);
            cartRepository.save(new Cart(admin));
            log.info("Created Admin User: {}", adminEmail);
        }

        // 3. Demo Customer User
        String demoCustomerEmail = "customer@foodfrenzy.com";
        User customer = userRepository.findByEmail(demoCustomerEmail).orElse(null);
        if (customer == null) {
            customer = new User("Sarah Jenkins", demoCustomerEmail, passwordEncoder.encode("Customer@12345"), "+1-555-0145", "742 Evergreen Terrace, Springfield, IL 62704");
            customer.addRole(customerRole);
            customer = userRepository.save(customer);
            cartRepository.save(new Cart(customer));
            log.info("Created Demo Customer: {}", demoCustomerEmail);
        }

        // 4. Categories & Products
        if (categoryRepository.count() == 0) {
            seedCategoriesAndProducts(admin, customer);
        }

        log.info("FoodFrenzy seed data initialization completed successfully.");
    }

    private void seedCategoriesAndProducts(User admin, User customer) {
        // Categories
        Category burgers = categoryRepository.save(new Category(
                "Gourmet Burgers",
                "Artisan hand-crafted beef, chicken, and vegetarian burgers with chef sauces",
                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80",
                "bi-egg-fried",
                1
        ));

        Category pizzas = categoryRepository.save(new Category(
                "Artisan Pizzas",
                "Wood-fired thin crust pizzas loaded with premium mozzarella and fresh herbs",
                "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&auto=format&fit=crop&q=80",
                "bi-pie-chart",
                2
        ));

        Category asian = categoryRepository.save(new Category(
                "Asian & Bowls",
                "Fresh ramen, wok bowls, sushi-grade tuna bowls, and aromatic noodle dishes",
                "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80",
                "bi-cup-hot",
                3
        ));

        Category wraps = categoryRepository.save(new Category(
                "Wraps & Sandwiches",
                "Toasted focaccia sandwiches, crispy wraps, and loaded Mediterranean rolls",
                "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=600&auto=format&fit=crop&q=80",
                "bi-bag-check",
                4
        ));

        Category desserts = categoryRepository.save(new Category(
                "Desserts & Drinks",
                "Decadent lava cakes, creamy milkshakes, artisanal gelatos, and specialty brews",
                "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=600&auto=format&fit=crop&q=80",
                "bi-cup-straw",
                5
        ));

        List<Product> products = new ArrayList<>();

        // Burgers (4)
        Product b1 = new Product("Truffle Bacon Smash Burger", "Double black angus beef smash patties, smoked bacon, aged cheddar, truffle aioli on brioche.", burgers, new BigDecimal("14.99"), "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80", 25);
        b1.setFeatured(true);
        b1.setPreparationTimeMinutes(15);
        products.add(b1);

        Product b2 = new Product("Spicy Crispy Chicken Burger", "Crispy buttermilk fried chicken breast, spicy sriracha slaw, pickled jalapenos, house garlic ranch.", burgers, new BigDecimal("12.49"), "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=600&auto=format&fit=crop&q=80", 18);
        b2.setFeatured(true);
        b2.setPreparationTimeMinutes(15);
        products.add(b2);

        Product b3 = new Product("Smokey BBQ Bourbon Burger", "Angus beef patty glazed with bourbon BBQ, beer-battered onion rings, cheddar, crispy lettuce.", burgers, new BigDecimal("13.99"), "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=600&auto=format&fit=crop&q=80", 12);
        b3.setPreparationTimeMinutes(18);
        products.add(b3);

        Product b4 = new Product("Plant-Based Avocado Crunch", "Savory grilled plant patty, smashed Haas avocado, microgreens, chipotle vegan mayo on vegan oat bun.", burgers, new BigDecimal("13.49"), "https://images.unsplash.com/photo-1520072959219-c595dc870360?w=600&auto=format&fit=crop&q=80", 8);
        b4.setVegetarian(true);
        b4.setPreparationTimeMinutes(15);
        products.add(b4);

        // Pizzas (4)
        Product p1 = new Product("Margherita Di Bufala", "San Marzano tomato sauce, fresh buffalo mozzarella, aromatic sweet basil, extra virgin olive oil drizzle.", pizzas, new BigDecimal("15.99"), "https://images.unsplash.com/photo-1604382355076-af4b0eb60143?w=600&auto=format&fit=crop&q=80", 20);
        p1.setFeatured(true);
        p1.setVegetarian(true);
        p1.setPreparationTimeMinutes(20);
        products.add(p1);

        Product p2 = new Product("Fiery Pepperoni & Hot Honey", "Crispy pepperoni cups, crushed red pepper, fresh mozzarella, finished with spicy infused clover honey.", pizzas, new BigDecimal("17.99"), "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=600&auto=format&fit=crop&q=80", 15);
        p2.setFeatured(true);
        p2.setPreparationTimeMinutes(20);
        products.add(p2);

        Product p3 = new Product("Wild Forest Mushroom & Truffle", "Roasted cremini, shiitake, and portobello mushrooms, creamy fontina, thyme, white truffle drizzle.", pizzas, new BigDecimal("18.49"), "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&auto=format&fit=crop&q=80", 4); // Low stock test item
        p3.setVegetarian(true);
        p3.setPreparationTimeMinutes(22);
        products.add(p3);

        Product p4 = new Product("BBQ Pulled Pork Pizza", "Slow-smoked tender pulled pork, sweet red onion, cilantro, smoked gouda, tangy BBQ base.", pizzas, new BigDecimal("16.99"), "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=600&auto=format&fit=crop&q=80", 10);
        p4.setPreparationTimeMinutes(20);
        products.add(p4);

        // Asian & Bowls (4)
        Product a1 = new Product("Tokyo Shoyu Ramen", "Rich slow-simmered pork broth, springy ramen noodles, chashu pork, soft seasoned egg, nori, scallions.", asian, new BigDecimal("14.49"), "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80", 22);
        a1.setFeatured(true);
        a1.setPreparationTimeMinutes(25);
        products.add(a1);

        Product a2 = new Product("Ahi Tuna Poke Power Bowl", "Sashimi-grade yellowfin tuna, sushi rice, avocado, edamame, cucumber ribbons, spicy sesame ponzu.", asian, new BigDecimal("16.99"), "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600&auto=format&fit=crop&q=80", 14);
        a2.setFeatured(true);
        a2.setPreparationTimeMinutes(15);
        products.add(a2);

        Product a3 = new Product("Szechuan Firecracker Chicken Bowl", "Wok-seared crispy chicken, fiery chili sauce, charred broccoli, steamed jasmine rice.", asian, new BigDecimal("13.99"), "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=600&auto=format&fit=crop&q=80", 11);
        a3.setPreparationTimeMinutes(20);
        products.add(a3);

        Product a4 = new Product("Teriyaki Glazed Salmon Bento", "Wild salmon fillet caramelized in rich house teriyaki, sesame greens, seasoned rice, gyoza.", asian, new BigDecimal("18.99"), "https://images.unsplash.com/photo-1546069901-d70a30b42c67?w=600&auto=format&fit=crop&q=80", 0); // Out of stock test item
        a4.setPreparationTimeMinutes(22);
        products.add(a4);

        // Wraps & Sandwiches (4)
        Product w1 = new Product("Mediterranean Grilled Chicken Wrap", "Herb-marinated chicken breast, crisp romaine, kalamata olives, feta, house tzatziki in warm flatbread.", wraps, new BigDecimal("10.99"), "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=600&auto=format&fit=crop&q=80", 20);
        w1.setPreparationTimeMinutes(12);
        products.add(w1);

        Product w2 = new Product("Classic Italian Prosciutto Panini", "Aged prosciutto di Parma, fresh mozzarella, sundried tomato pesto, arugula on pressed artisan ciabatta.", wraps, new BigDecimal("12.99"), "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=600&auto=format&fit=crop&q=80", 16);
        w2.setPreparationTimeMinutes(12);
        products.add(w2);

        Product w3 = new Product("Crispy Falafel Hummus Wrap", "Golden chickpea falafel patties, roasted garlic hummus, pickled turnips, tahini sauce, warm pita.", wraps, new BigDecimal("9.99"), "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600&auto=format&fit=crop&q=80", 15);
        w3.setVegetarian(true);
        w3.setPreparationTimeMinutes(10);
        products.add(w3);

        Product w4 = new Product("Prime Ribeye Steak Sandwich", "Shaved prime ribeye, caramelized sweet onions, melted provolone, roasted garlic aioli on baguette.", wraps, new BigDecimal("15.49"), "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=600&auto=format&fit=crop&q=80", 9);
        w4.setPreparationTimeMinutes(15);
        products.add(w4);

        // Desserts & Drinks (4)
        Product d1 = new Product("Molten Belgian Chocolate Lava Cake", "Warm decadent dark chocolate cake with a luscious molten ganache center and Madagascar vanilla bean drizzle.", desserts, new BigDecimal("7.99"), "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600&auto=format&fit=crop&q=80", 30);
        d1.setFeatured(true);
        d1.setVegetarian(true);
        d1.setPreparationTimeMinutes(10);
        products.add(d1);

        Product d2 = new Product("Signature Strawberry Cheesecake Shake", "Creamy vanilla ice cream blended with real strawberries, graham cracker crumbles, whipped cream crown.", desserts, new BigDecimal("6.49"), "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=600&auto=format&fit=crop&q=80", 25);
        d2.setVegetarian(true);
        d2.setPreparationTimeMinutes(8);
        products.add(d2);

        Product d3 = new Product("Classic Italian Tiramisu Cup", "Espresso-soaked ladyfingers, velvety mascarpone cream, dusted with unsweetened Valrhona cocoa powder.", desserts, new BigDecimal("7.49"), "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=600&auto=format&fit=crop&q=80", 18);
        d3.setVegetarian(true);
        d3.setPreparationTimeMinutes(5);
        products.add(d3);

        Product d4 = new Product("Cold Brew Salted Caramel Float", "Slow-steeped smooth cold brew coffee topped with a generous scoop of artisanal sea salt caramel gelato.", desserts, new BigDecimal("5.99"), "https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=600&auto=format&fit=crop&q=80", 3); // Low stock test item
        d4.setVegetarian(true);
        d4.setPreparationTimeMinutes(5);
        products.add(d4);

        // Save products and create initial stock inventory transactions
        for (Product product : products) {
            Product saved = productRepository.save(product);
            InventoryTransaction tx = new InventoryTransaction(
                    saved,
                    TransactionType.STOCK_IN,
                    saved.getStockQuantity(),
                    saved.getStockQuantity(),
                    "Initial opening stock setup",
                    admin
            );
            inventoryTransactionRepository.save(tx);
        }

        // Seed Sample Orders for Demo Customer to populate the Admin Dashboard & Customer Order History
        seedSampleOrders(customer, products);
    }

    private void seedSampleOrders(User customer, List<Product> products) {
        Product b1 = products.get(0); // Truffle Smash Burger ($14.99)
        Product p1 = products.get(4); // Margherita ($15.99)
        Product d1 = products.get(16); // Lava Cake ($7.99)

        // Sample Completed Order 1
        Order o1 = new Order();
        o1.setOrderNumber("FF-20261001-A1B2C3");
        o1.setUser(customer);
        o1.setOrderStatus(OrderStatus.DELIVERED);
        o1.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        o1.setPaymentStatus(PaymentStatus.PAID);
        o1.setRecipientName(customer.getName());
        o1.setRecipientPhone(customer.getPhone());
        o1.setDeliveryAddress(customer.getAddress());
        o1.setSubtotal(new BigDecimal("30.98"));
        o1.setTaxAmount(new BigDecimal("2.48"));
        o1.setDeliveryFee(new BigDecimal("4.99"));
        o1.setTotalAmount(new BigDecimal("38.45"));

        OrderItem item1 = new OrderItem(o1, b1, b1.getName(), b1.getPrice(), 1, b1.getImageUrl());
        OrderItem item2 = new OrderItem(o1, p1, p1.getName(), p1.getPrice(), 1, p1.getImageUrl());
        o1.addItem(item1);
        o1.addItem(item2);
        orderRepository.save(o1);

        // Sample Active Order 2 (In Preparation)
        Order o2 = new Order();
        o2.setOrderNumber("FF-20261010-X9Y8Z7");
        o2.setUser(customer);
        o2.setOrderStatus(OrderStatus.PREPARING);
        o2.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        o2.setPaymentStatus(PaymentStatus.PENDING);
        o2.setRecipientName(customer.getName());
        o2.setRecipientPhone(customer.getPhone());
        o2.setDeliveryAddress(customer.getAddress());
        o2.setSpecialInstructions("Please leave at the door and ring doorbell");
        o2.setSubtotal(new BigDecimal("22.98"));
        o2.setTaxAmount(new BigDecimal("1.84"));
        o2.setDeliveryFee(new BigDecimal("4.99"));
        o2.setTotalAmount(new BigDecimal("29.81"));

        OrderItem item3 = new OrderItem(o2, b1, b1.getName(), b1.getPrice(), 1, b1.getImageUrl());
        OrderItem item4 = new OrderItem(o2, d1, d1.getName(), d1.getPrice(), 1, d1.getImageUrl());
        o2.addItem(item3);
        o2.addItem(item4);
        orderRepository.save(o2);

        log.info("Seeded 2 sample demonstration orders for customer: {}", customer.getEmail());
    }
}
