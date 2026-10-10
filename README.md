# 🍔 FoodFrenzy — Full-Stack Java Food E-Commerce Platform

FoodFrenzy is an enterprise-grade, production-style food e-commerce and kitchen management web application built with **Spring Boot 3**, **Spring Security 6**, **Spring Data JPA**, **Hibernate**, **MySQL 8** (with zero-config H2 dev profile fallback), and **Thymeleaf + Bootstrap 5**.

---

## 🌟 Key Features

### 👤 Customer Experience
* **Interactive Storefront**: Modern hero banner, category quick-scroll pills, featured chef specials, customer review spotlights, and newsletter signup.
* **Menu Browsing & Search**: Real-time category filtering (Burgers, Pizzas, Asian Bowls, Wraps, Desserts), text keyword search, sort by price (low to high, high to low), and in-stock only filtering.
* **Detailed Product Pages**: High-resolution food photography, ingredient badges, prep time, calorie counts, stock availability indicator, and direct quantity selector.
* **Server-Validated Cart**: Dynamic item additions, live quantity adjustments with instant stock ceiling checks, item removals, subtotal calculation, 8% tax calculation, and free delivery threshold indicator ($50.00).
* **Robust Checkout**: Address collection (Full name, phone, address, city, state, postal code, special delivery instructions) with Cash on Delivery (COD) payment method.
* **Real-Time Order Tracking**: Visual progress timeline showing order state transitions: `PLACED` ➔ `CONFIRMED` ➔ `PREPARING` ➔ `OUT_FOR_DELIVERY` ➔ `DELIVERED`.
* **Safe Order Cancellation**: Customers can cancel orders while in `PLACED` or `CONFIRMED` status; triggers automatic, transaction-safe inventory restoration.
* **Account & Profile Management**: Customer self-registration with BCrypt password hashing, profile updates, and historical order browsing.

### 🛡️ Admin Dashboard & Kitchen Operations
* **Live Analytics & KPI Cards**: Total revenue, total orders placed, active customer count, low-stock alerts, and recent orders overview.
* **Customer Management**: View registered customers, view spending metrics, toggle active status, and create new customer profiles.
* **Product Catalog CRUD**: Add, edit, or soft-delete products; manage pricing, images, categories, and active storefront visibility.
* **Inventory Management & Audit Log**: Real-time stock levels, low-stock highlighting, manual adjustments (Restock / Correction / Damage / Return), and complete audit logging of every stock movement linked to orders or admin actions.
* **Order Status Workflow**: Strict order state machine transitions (`CONFIRMED`, `PREPARING`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`) with automatic payment status updates upon delivery.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Backend Framework** | Spring Boot 3.3.4 (Java 17 / 21 / 25 compatible) |
| **Security** | Spring Security 6, BCrypt, Session-based auth, Role-based authorization, CSRF protection |
| **Persistence** | Spring Data JPA, Hibernate ORM 6 |
| **Database** | MySQL 8.x (Production/Container), H2 In-Memory (Zero-setup Dev/Test) |
| **Migration** | Flyway DB migrations (`db/migration/V1__init_schema.sql`) |
| **View Engine** | Thymeleaf 3, Thymeleaf Layouts & Extras for Spring Security |
| **Styling & UI** | Bootstrap 5.3.3, Bootstrap Icons 1.11.3, Custom CSS |
| **Testing** | JUnit 5, Mockito 5, Spring MockMvc, Spring Security Test |
| **Build & Packaging** | Apache Maven 3.9+ with customized portable wrapper (`./mvnw`) |

---

## 🔑 Pre-Seeded Demonstration Accounts

Upon first launch, `DataInitializer` automatically provisions standard test credentials:

| Role | Email Address | Password | Permissions |
|---|---|---|---|
| **System Admin** | `admin@foodfrenzy.com` | `Admin@12345` | Full access to `/admin/**` dashboards, orders, inventory, customers |
| **Demo Customer** | `customer@foodfrenzy.com` | `Customer@12345` | Standard storefront ordering, cart, checkout, profile, order history |

> **Quick Autofill**: The login screen (`/login`) includes one-click buttons to instantly fill admin or customer credentials for testing convenience.

---

## 🚀 Getting Started

### Prerequisites
* Java JDK 17, 21, or 25 installed.
* Docker & Docker Compose (optional, for running containerized MySQL).

### 1. Instant Run (Zero Setup Dev Profile)
By default, the application runs with an embedded in-memory database pre-seeded with 5 categories, 20 gourmet food items with photos, sample customer orders, and initial inventory:

```bash
# Execute tests (all 25 unit and integration tests)
./mvnw test

# Run application on default port 8080
./mvnw spring-boot:run
```

Access the application in your browser:
* **Storefront**: [http://localhost:8080/](http://localhost:8080/)
* **Explore Menu**: [http://localhost:8080/menu](http://localhost:8080/menu)
* **Login**: [http://localhost:8080/login](http://localhost:8080/login)
* **Admin Dashboard**: [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard)

---

### 2. Running with MySQL 8 & Docker Compose

To run the application backed by a dedicated MySQL 8 container:

```bash
# 1. Start MySQL 8 container in background
docker-compose up -d

# 2. Run the application with the MySQL profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Or configure your own MySQL instance using environment variables:
```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/foodfrenzy?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
export SPRING_DATASOURCE_USERNAME="foodfrenzy_user"
export SPRING_DATASOURCE_PASSWORD="foodfrenzy_pass"

./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

---

### 3. Packaging Standalone Executable JAR

```bash
./mvnw clean package

# Run the packaged executable JAR
java -jar target/foodfrenzy-1.0.0.jar --spring.profiles.active=dev
```

---

## 📐 Architecture & Domain Model

### Entity Relationships
```
[User] 1 ──── * [Role]
  │
  ├── 1 ──── 1 [Cart] 1 ──── * [CartItem] * ──── 1 [Product] * ──── 1 [Category]
  │                                                    │
  ├── 1 ──── * [CustomerAddress]                       └── 1 ──── * [InventoryTransaction]
  │
  └── 1 ──── * [Order] 1 ──── * [OrderItem]
```

### Order State Machine Transitions
```
                ┌───────────────► CANCELLED (Restores Stock)
                │                      ▲
                │                      │ (Customer can cancel before preparation)
   PLACED ──────┼────────► CONFIRMED ──┘
                │
                └────────► PREPARING ────► OUT_FOR_DELIVERY ────► DELIVERED (Payment Settled)
```

### Business Rules & Safeguards
1. **Server-Side Price Calculation**: Order pricing, cart totals, taxes, and delivery fees are calculated strictly server-side using `BigDecimal` with half-up rounding. Never trust client-submitted numbers.
2. **Atomic Inventory Operations**: Order placement validates stock and atomically decrements inventory with an audit entry. Order cancellations reverse stock decrements with an audit record.
3. **Optimistic Locking**: Prevents race conditions during simultaneous stock deductions.
4. **Role Isolation**: Customers attempting to access `/admin/**` receive HTTP 403 Forbidden; unauthenticated visitors are redirected to `/login`.

---

## 🧪 Test Suite

The test suite covers controllers, services, security boundaries, and repositories:
```bash
./mvnw test
```

* **`AdminAccessSecurityTest`**: Confirms that `/admin/**` endpoints redirect unauthenticated users to `/login`, reject customers with HTTP 403, and grant admin access.
* **`AuthControllerTest`**: Tests registration validation, password confirmation mismatch handling, and duplicate email prevention.
* **`MenuControllerTest`**: Tests catalog browsing, category filtering, keyword searches, and product details.
* **`ProductServiceTest`**: Validates product retrieval, category assignment, low-stock threshold querying, and soft deletion.
* **`CartServiceTest`**: Validates add-to-cart operations, stock boundary enforcement, tax calculation (8%), and free delivery threshold ($50.00).
* **`OrderServiceTest`**: Validates order creation, stock deduction, status changes, and stock restoration upon cancellation.
* **`InventoryServiceTest`**: Tests restock additions, order deductions, negative stock prevention, and transaction auditing.

---

## 📄 License
Open source under the [MIT License](LICENSE).
