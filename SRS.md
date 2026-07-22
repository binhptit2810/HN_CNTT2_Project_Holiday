# Software Requirements Specification (SRS)  
## Hệ thống Quản lý Nhà hàng (Restaurant Management System - RMS)

> **Phạm vi tài liệu:** Bản SRS cập nhật theo hiện trạng dự án `HN_CNTT2_Project_Holiday` (Spring Boot 4.x, Java 17, MySQL, Thymeleaf/React-ready, Cloudinary, VietQR).

## Mục lục
- [1. GIỚI THIỆU](#1-giới-thiệu)
  - [1.1 Mục đích tài liệu](#11-mục-đích-tài-liệu)
  - [1.2 Phạm vi tài liệu](#12-phạm-vi-tài-liệu)
  - [1.3 Tổng quan ứng dụng](#13-tổng-quan-ứng-dụng)
  - [1.4 Thuật ngữ viết tắt](#14-thuật-ngữ-viết-tắt)
- [2. YÊU CẦU TỔNG THỂ](#2-yêu-cầu-tổng-thể)
  - [2.1 Sơ đồ ERD](#21-sơ-đồ-erd)
  - [2.2 Sơ đồ Use Case tổng thể](#22-sơ-đồ-use-case-tổng-thể)
  - [2.3 Sơ đồ luồng tổng quát](#23-sơ-đồ-luồng-tổng-quát)
  - [2.4 Sơ đồ chuyển trạng thái hóa đơn](#24-sơ-đồ-chuyển-trạng-thái-hóa-đơn)
  - [2.5 Phân quyền hệ thống](#25-phân-quyền-hệ-thống)
  - [2.6 Site Map](#26-site-map)
- [3. CHỨC NĂNG CHI TIẾT (7 USE CASES)](#3-chức-năng-chi-tiết-7-use-cases)
  - [3.1 UC-01: Đặt bàn & Sơ đồ bàn](#31-uc-01-đặt-bàn--sơ-đồ-bàn)
  - [3.2 UC-02: Gọi món tại bàn (Order Entry)](#32-uc-02-gọi-món-tại-bàn-order-entry)
  - [3.3 UC-03: Điều phối chế biến tại bếp (KDS)](#33-uc-03-điều-phối-chế-biến-tại-bếp-kds)
  - [3.4 UC-04: Thanh toán & In hóa đơn (POS Billing)](#34-uc-04-thanh-toán--in-hóa-đơn-pos-billing)
  - [3.5 UC-05: Quản lý kho nguyên liệu](#35-uc-05-quản-lý-kho-nguyên-liệu)
  - [3.6 UC-06: Cấu hình thực đơn & giá](#36-uc-06-cấu-hình-thực-đơn--giá)
  - [3.7 UC-07: Xem báo cáo doanh thu](#37-uc-07-xem-báo-cáo-doanh-thu)
- [4. COMPONENT, THÔNG BÁO, CẢNH BÁO](#4-component-thông-báo-cảnh-báo)
- [5. LINK ISSUE](#5-link-issue)

---

## 1. GIỚI THIỆU

### 1.1 Mục đích tài liệu
Tài liệu này mô tả đầy đủ yêu cầu nghiệp vụ, yêu cầu hệ thống, luồng xử lý và phạm vi triển khai cho **RMS** nhằm đồng bộ giữa nhóm phát triển, kiểm thử, vận hành và stakeholders.

### 1.2 Phạm vi tài liệu
Tài liệu bao gồm:
- Yêu cầu tổng thể và mô hình dữ liệu.
- 7 Use Case trọng tâm từ vận hành bàn đến thanh toán và báo cáo.
- Định nghĩa phân quyền, thông báo hệ thống, cấu trúc màn hình.
- Ràng buộc kỹ thuật cho stack hiện tại.

### 1.3 Tổng quan ứng dụng
RMS là hệ thống quản lý nhà hàng hỗ trợ quy trình khép kín:
1. Quản lý bàn (mở bàn, giữ bàn, dọn bàn).
2. Gọi món tại bàn hoặc qua QR.
3. Điều phối chế biến tại bếp theo FIFO.
4. Thanh toán tại POS, hỗ trợ **tiền mặt / thẻ / VietQR**, áp mã giảm giá, tách hóa đơn.
5. Quản lý kho nguyên liệu và định mức món.
6. Quản lý thực đơn, hình ảnh món qua Cloudinary.
7. Theo dõi doanh thu và top món bán chạy.

**Stack hệ thống:**
- **Backend:** Spring Boot (Java 17)
- **Database:** MySQL
- **Frontend:** Thymeleaf (hiện tại) / React (mở rộng)
- **Integration:** Cloudinary + VietQR

### 1.4 Thuật ngữ viết tắt

| Viết tắt | Ý nghĩa |
|---|---|
| SRS | Software Requirements Specification |
| RMS | Restaurant Management System |
| ERD | Entity Relationship Diagram |
| UC | Use Case |
| POS | Point of Sale |
| KDS | Kitchen Display System |
| QR | Quick Response |
| VAT | Value Added Tax |
| FIFO | First In, First Out |

---

## 2. YÊU CẦU TỔNG THỂ

### 2.1 Sơ đồ ERD

```mermaid
erDiagram
    USER {
        bigint id PK
        string username
        string password
        string role
        string full_name
        string phone
        string email
        boolean active
    }
    RESTAURANT_TABLE {
        bigint id PK
        string table_number
        int capacity
        string status
        boolean payment_requested
        bigint current_customer_id FK
    }
    MENU_ITEM {
        bigint id PK
        string name
        decimal price
        string category
        string status
        text description
        string image_url
    }
    INGREDIENT {
        bigint id PK
        string ingredient_name
        decimal quantity_in_stock
        string unit
        decimal reorder_level
    }
    MENU_ITEM_RECIPE {
        bigint id PK
        bigint menu_item_id FK
        bigint ingredient_id FK
        decimal quantity_required
    }
    ORDER_ENTITY {
        bigint id PK
        string table_number
        string status
        datetime created_at
    }
    ORDER_DETAIL {
        bigint id PK
        bigint order_id FK
        bigint user_id FK
        bigint menu_item_id FK
        int quantity
        decimal unit_price
        string status
        text note
        datetime created_at
    }
    BILL {
        bigint id PK
        bigint order_id FK
        decimal subtotal
        decimal discount_amount
        decimal vat_rate
        decimal total_amount
        string payment_method
        string status
        datetime created_at
    }
    SHIFT {
        bigint id PK
        bigint user_id FK
        datetime opened_at
        datetime closed_at
        decimal start_balance
        decimal end_balance_declared
        decimal end_balance_calculated
        decimal difference
        string status
    }
    CART {
        bigint id PK
        bigint user_id FK
        datetime updated_at
    }
    CART_ITEM {
        bigint id PK
        bigint cart_id FK
        bigint menu_item_id FK
        int quantity
        text note
    }
    FAVORITE {
        bigint id PK
        bigint user_id FK
        bigint menu_item_id FK
        datetime created_at
    }
    VOUCHER {
        bigint id PK
        string code
        string discount_type
        decimal discount_value
        decimal min_order_value
        decimal max_discount_amount
        datetime valid_from
        datetime valid_to
        boolean active
    }
    CUSTOMER_ORDER_REQUEST {
        bigint id PK
        string table_number
        string status
        bigint user_id FK
        datetime created_at
    }
    CUSTOMER_ORDER_REQUEST_DETAIL {
        bigint id PK
        bigint request_id FK
        bigint menu_item_id FK
        int quantity
        text note
    }

    USER ||--o{ SHIFT : opens
    USER ||--o| CART : owns
    USER ||--o{ FAVORITE : bookmarks
    USER ||--o{ ORDER_DETAIL : requests
    USER ||--o{ CUSTOMER_ORDER_REQUEST : creates

    RESTAURANT_TABLE }o--|| USER : current_customer

    MENU_ITEM ||--o{ ORDER_DETAIL : ordered_in
    MENU_ITEM ||--o{ CART_ITEM : selected_in
    MENU_ITEM ||--o{ FAVORITE : favorited
    MENU_ITEM ||--o{ MENU_ITEM_RECIPE : has_recipe
    MENU_ITEM ||--o{ CUSTOMER_ORDER_REQUEST_DETAIL : requested

    INGREDIENT ||--o{ MENU_ITEM_RECIPE : consumed_by

    ORDER_ENTITY ||--o{ ORDER_DETAIL : contains
    ORDER_ENTITY ||--o| BILL : billed_by

    CART ||--o{ CART_ITEM : includes

    CUSTOMER_ORDER_REQUEST ||--o{ CUSTOMER_ORDER_REQUEST_DETAIL : includes
```

### 2.2 Sơ đồ Use Case tổng thể

```mermaid
flowchart LR
    A[Khách hàng] --> UC01[UC-01 Đặt bàn & sơ đồ bàn]
    A --> UC02[UC-02 Gọi món tại bàn]
    A --> UC04[UC-04 Thanh toán]

    W[Phục vụ - Waiter] --> UC01
    W --> UC02
    W --> UC03

    K[Nhân viên bếp - Kitchen] --> UC03[UC-03 Điều phối chế biến KDS]

    C[Thu ngân - Cashier] --> UC04[UC-04 POS Billing & in hóa đơn]

    M[Quản lý - Manager] --> UC05[UC-05 Quản lý kho]
    M --> UC06[UC-06 Cấu hình thực đơn & giá]
    M --> UC07[UC-07 Báo cáo doanh thu]

    AD[Admin] --> UC01
    AD --> UC02
    AD --> UC03
    AD --> UC04
    AD --> UC05
    AD --> UC06
    AD --> UC07
```

### 2.3 Sơ đồ luồng tổng quát

```mermaid
flowchart TD
    S([Khách vào nhà hàng]) --> T{Có bàn trống?}
    T -- Không --> R[Đặt trước/đưa vào danh sách chờ]
    T -- Có --> O[Mở bàn]
    O --> M[Nhận order tại bàn hoặc QR]
    M --> KDS[Chuyển món xuống bếp]
    KDS --> C1[Chế biến món]
    C1 --> C2[Chuyển trạng thái READY]
    C2 --> W[Phục vụ món]
    W --> A{Khách gọi thêm?}
    A -- Có --> M
    A -- Không --> P[Khách yêu cầu thanh toán]
    P --> B[POS tính tiền + voucher + VAT]
    B --> PM{Phương thức thanh toán}
    PM -->|Tiền mặt| D1[Cập nhật bill PAID]
    PM -->|Thẻ| D1
    PM -->|VietQR| D1
    D1 --> I[In hóa đơn]
    I --> E[Dọn bàn & đóng order]
    E --> X([Kết thúc phiên phục vụ])
```

### 2.4 Sơ đồ chuyển trạng thái hóa đơn

```mermaid
stateDiagram-v2
    [*] --> UNPAID: Tạo bill
    UNPAID --> PAID: Thanh toán thành công
    UNPAID --> CANCELLED: Hủy/void hóa đơn
    PAID --> REFUND_PENDING: Yêu cầu hoàn tiền
    REFUND_PENDING --> REFUNDED: Xác nhận hoàn tiền
    REFUND_PENDING --> PAID: Từ chối hoàn tiền
    CANCELLED --> [*]
    REFUNDED --> [*]
    PAID --> [*]
```

### 2.5 Phân quyền hệ thống

| Chức năng | Admin | Manager | Cashier | Waiter | Kitchen |
|---|:---:|:---:|:---:|:---:|:---:|
| Quản lý người dùng | ✓ |  |  |  |  |
| Quản lý bàn | ✓ | ✓ |  | ✓ |  |
| Tạo/Cập nhật order tại bàn | ✓ |  |  | ✓ |  |
| Duyệt yêu cầu QR | ✓ |  |  | ✓ |  |
| Cập nhật trạng thái nấu | ✓ |  |  |  | ✓ |
| Thanh toán & in hóa đơn | ✓ | ✓ | ✓ |  |  |
| Tách hóa đơn | ✓ | ✓ | ✓ |  |  |
| Quản lý kho nguyên liệu | ✓ | ✓ |  |  |  |
| Quản lý menu & định mức | ✓ | ✓ |  |  |  |
| Quản lý voucher | ✓ | ✓ | ✓ |  |  |
| Xem dashboard doanh thu | ✓ | ✓ |  |  |  |

> **Ghi chú:** Role *Manager* là lớp nghiệp vụ quản trị vận hành; trong triển khai hiện tại có thể được ánh xạ bởi quyền Admin hoặc role mở rộng.

### 2.6 Site Map

```mermaid
flowchart TB
    ROOT[/RMS/]
    ROOT --> AUTH[Đăng nhập/Đăng ký]
    ROOT --> POS[Khối POS vận hành]
    ROOT --> ADMIN[Khối Admin]
    ROOT --> CUSTOMER[Khối Khách hàng]

    POS --> TBL[/tables]
    POS --> ORD[/order/{tableNumber}]
    POS --> KIT[/kitchen]
    POS --> POSPAY[/admin/pos]

    ADMIN --> DASH[/admin/dashboard]
    ADMIN --> USR[/admin/users]
    ADMIN --> MENU[/admin/menu]
    ADMIN --> INV[/admin/inventory]
    ADMIN --> VOU[/admin/vouchers]
    ADMIN --> BILLH[/admin/bills]

    CUSTOMER --> UMENU[/user/menu]
    CUSTOMER --> FCART[/user/cart]
    CUSTOMER --> FFAV[/user/favorites]
    CUSTOMER --> PROF[/user/profile]
    CUSTOMER --> QRO[/qr/order/{tableNumber}]
```

---

## 3. CHỨC NĂNG CHI TIẾT (7 USE CASES)

### 3.1 UC-01: Đặt bàn & Sơ đồ bàn

#### 3.1.1 Đặc tả Use Case

| Thuộc tính | Giá trị |
|---|---|
| Use Case ID | UC-01 |
| Tên chức năng | Đặt bàn & Sơ đồ bàn |
| Mô tả | Quản lý trạng thái bàn: trống, đang phục vụ, đã đặt trước; mở/giữ/dọn bàn |
| Tác nhân | Waiter, Admin, Manager |
| Trigger | Khách vào quán hoặc yêu cầu đặt/chuyển trạng thái bàn |
| Điều kiện tiên quyết | Người dùng đã đăng nhập, có quyền quản lý bàn |
| Hậu điều kiện | Trạng thái bàn và order đang hoạt động được cập nhật nhất quán |

**Luồng cơ bản:**
1. Nhân viên mở màn hình sơ đồ bàn.
2. Chọn bàn trống.
3. Nhập số khách và mở bàn.
4. Hệ thống chuyển bàn sang `OCCUPIED`.
5. Cho phép điều hướng sang màn hình gọi món.

**Luồng thay thế:**
- A1: Đặt trước bàn → trạng thái `RESERVED`.
- A2: Dọn bàn khi hoàn tất thanh toán → trạng thái `EMPTY`.

**Luồng ngoại lệ:**
- E1: Bàn không tồn tại hoặc đang bị khóa trạng thái.
- E2: Bàn chưa thanh toán nhưng yêu cầu dọn bàn.

**Ràng buộc:**
- Không cho mở cùng lúc 2 order active trên cùng bàn.
- Dọn bàn cần đảm bảo không còn món active.

**Yêu cầu phi chức năng:**
- Tải sơ đồ bàn < **2 giây** với 100 bàn.

#### 3.1.2 Sơ đồ Use Case phân rã

```mermaid
flowchart LR
    U1[Waiter/Admin] --> B1[Xem sơ đồ bàn]
    U1 --> B2[Mở bàn]
    U1 --> B3[Đặt trước bàn]
    U1 --> B4[Dọn bàn]
    B2 --> B5[Tạo order active]
```

#### 3.1.3 Sơ đồ tuần tự (Sequence)

```mermaid
sequenceDiagram
    actor W as Waiter
    participant UI as Tables UI
    participant C as WaiterController
    participant S as TableOrderService
    participant DB as MySQL

    W->>UI: Chọn bàn + số khách
    UI->>C: POST /tables/open
    C->>S: openTable(tableNumber, numGuests)
    S->>DB: Update restaurant_tables + orders
    DB-->>S: OK
    S-->>C: Success
    C-->>UI: Redirect /tables
```

#### 3.1.4 Mô tả giao diện
- Màn hình dạng grid thể hiện trạng thái bàn bằng màu.
- Khối cảnh báo yêu cầu QR đang chờ duyệt.
- Nút thao tác nhanh: **Mở bàn**, **Giữ bàn**, **Dọn bàn**, **Vào order**.

#### 3.1.5 Mô tả chi tiết dữ liệu

| Tên tiếng Việt | Tên tiếng Anh | Loại | Bắt buộc | Mô tả |
|---|---|---|---|---|
| Mã bàn | tableNumber | String | Có | Định danh bàn duy nhất |
| Sức chứa | capacity | Integer | Có | Số khách tối đa |
| Trạng thái bàn | status | Enum | Có | EMPTY/OCCUPIED/RESERVED |
| Yêu cầu thanh toán | paymentRequested | Boolean | Có | Cờ khách yêu cầu tính tiền |

---

### 3.2 UC-02: Gọi món tại bàn (Order Entry)

#### 3.2.1 Đặc tả Use Case

| Thuộc tính | Giá trị |
|---|---|
| Use Case ID | UC-02 |
| Tên chức năng | Gọi món tại bàn |
| Mô tả | Nhập món từ nhân viên hoặc yêu cầu QR và chuyển xuống bếp |
| Tác nhân | Waiter, Customer (QR), Admin |
| Trigger | Khách chọn món |
| Điều kiện tiên quyết | Bàn đang `OCCUPIED` hoặc có request QR hợp lệ |
| Hậu điều kiện | Tạo/cập nhật `Order` + `OrderDetail` ở trạng thái PENDING |

**Luồng cơ bản:**
1. Chọn bàn và mở trang order.
2. Chọn món, số lượng, ghi chú.
3. Gửi order.
4. Hệ thống tạo `OrderDetail` và chuyển xuống bếp.

**Luồng thay thế:**
- A1: Khách gửi request qua QR, waiter duyệt trước khi đẩy vào bếp.
- A2: Cập nhật trạng thái từng món bởi waiter khi phục vụ.

**Luồng ngoại lệ:**
- E1: Món `OUT_OF_STOCK`.
- E2: Bàn chưa mở nhưng yêu cầu gọi món.

**Ràng buộc:**
- Món phải tồn tại trong menu và khả dụng.

**Yêu cầu phi chức năng:**
- Trả phản hồi gửi món dưới **500ms** cho yêu cầu thông thường.

#### 3.2.2 Sơ đồ Use Case phân rã

```mermaid
flowchart LR
    CUS[Customer/Waiter] --> O1[Chọn món]
    O1 --> O2[Nhập số lượng + ghi chú]
    O2 --> O3[Gửi yêu cầu]
    O3 --> O4[Tạo OrderDetail PENDING]
    O4 --> O5[Đẩy KDS]
```

#### 3.2.3 Sơ đồ tuần tự (Sequence)

```mermaid
sequenceDiagram
    actor U as Waiter/Customer
    participant UI as Order UI/QR UI
    participant WC as WaiterController/CustomerController
    participant TS as TableOrderService
    participant DB as MySQL

    U->>UI: Chọn món + qty + note
    UI->>WC: POST /order/{table}/add hoặc /qr/order/{table}/request
    WC->>TS: addItemsToOrder(...)
    TS->>DB: Insert order_details
    DB-->>TS: OK
    TS-->>WC: success
    WC-->>UI: "Gửi bếp thành công"
```

#### 3.2.4 Mô tả giao diện
- Danh sách món theo category, giá và trạng thái món.
- Giỏ gọi món theo bàn, hiển thị món đã gửi.
- QR page tối ưu cho điện thoại, không bắt buộc đăng nhập.

#### 3.2.5 Mô tả chi tiết dữ liệu

| Tên tiếng Việt | Tên tiếng Anh | Loại | Bắt buộc | Mô tả |
|---|---|---|---|---|
| Mã đơn hàng | orderId | Long | Có | Đơn active theo bàn |
| Mã món | menuItemId | Long | Có | Món được gọi |
| Số lượng | quantity | Integer | Có | >0 |
| Ghi chú bếp | note | String | Không | Ít cay, không hành... |
| Trạng thái món | status | Enum | Có | PENDING/COOKING/READY/SERVED |

---

### 3.3 UC-03: Điều phối chế biến tại bếp (KDS)

#### 3.3.1 Đặc tả Use Case

| Thuộc tính | Giá trị |
|---|---|
| Use Case ID | UC-03 |
| Tên chức năng | Điều phối chế biến tại bếp |
| Mô tả | Bếp nhận món chờ và cập nhật trạng thái chế biến |
| Tác nhân | Kitchen, Admin |
| Trigger | Có order detail mới ở trạng thái PENDING |
| Điều kiện tiên quyết | User có quyền bếp |
| Hậu điều kiện | Trạng thái món chuyển đúng luồng và waiter thấy món READY |

**Luồng cơ bản:**
1. Bếp mở màn hình KDS.
2. Hệ thống tải danh sách món active theo FIFO.
3. Bếp chuyển trạng thái `PENDING -> COOKING -> READY`.
4. Waiter nhận thông tin món READY để phục vụ.

**Luồng thay thế:**
- A1: Món bị hủy: `CANCELLED`.

**Luồng ngoại lệ:**
- E1: Cập nhật sai trạng thái hợp lệ.

**Ràng buộc:**
- Không cho nhảy trạng thái sai thứ tự nghiệp vụ.

**Yêu cầu phi chức năng:**
- Màn hình KDS cập nhật realtime/polling tối đa 5 giây.

#### 3.3.2 Sơ đồ Use Case phân rã

```mermaid
flowchart LR
    K[Kitchen] --> K1[Xem danh sách món active]
    K --> K2[Nhận món nấu]
    K --> K3[Cập nhật COOKING]
    K --> K4[Cập nhật READY]
    K4 --> K5[Thông báo waiter phục vụ]
```

#### 3.3.3 Sơ đồ tuần tự (Sequence)

```mermaid
sequenceDiagram
    actor K as Kitchen
    participant UI as Kitchen UI
    participant KC as KitchenController
    participant TS as TableOrderService
    participant DB as MySQL

    K->>UI: Chọn detail + trạng thái mới
    UI->>KC: POST /kitchen/detail/update-status
    KC->>TS: updateOrderDetailStatus(detailId, status)
    TS->>DB: UPDATE order_details.status
    DB-->>TS: OK
    TS-->>KC: Success
    KC-->>UI: Redirect /kitchen
```

#### 3.3.4 Mô tả giao diện
- Danh sách món theo thứ tự thời gian tạo.
- Badge trạng thái nổi bật: PENDING/COOKING/READY.
- Nút thao tác nhanh chuyển trạng thái.

#### 3.3.5 Mô tả chi tiết dữ liệu

| Tên tiếng Việt | Tên tiếng Anh | Loại | Bắt buộc | Mô tả |
|---|---|---|---|---|
| Mã chi tiết món | detailId | Long | Có | Định danh món trong order |
| Mã bàn | tableNumber | String | Có | Bàn phục vụ |
| Trạng thái bếp | status | Enum | Có | PENDING/COOKING/READY/CANCELLED |
| Thời điểm tạo | createdAt | DateTime | Có | Ưu tiên FIFO |

---

### 3.4 UC-04: Thanh toán & In hóa đơn (POS Billing)

#### 3.4.1 Đặc tả Use Case

| Thuộc tính | Giá trị |
|---|---|
| Use Case ID | UC-04 |
| Tên chức năng | Thanh toán & in hóa đơn |
| Mô tả | Tính tiền, áp voucher, chọn phương thức thanh toán, in bill |
| Tác nhân | Cashier, Admin, Manager |
| Trigger | Khách yêu cầu thanh toán |
| Điều kiện tiên quyết | Order đã có món, bàn yêu cầu thanh toán |
| Hậu điều kiện | Bill chuyển PAID, order đóng và bàn sẵn sàng dọn |

**Luồng cơ bản:**
1. Thu ngân chọn order cần thanh toán.
2. Hệ thống tính subtotal.
3. Áp voucher (nếu có), VAT.
4. Chọn phương thức: CASH/CARD/BANKING(VietQR).
5. Xác nhận thanh toán và in hóa đơn.

**Luồng thay thế (Tách hóa đơn):**
- A1: Thu ngân chọn một phần món để tách hóa đơn.
- A2: Hệ thống tạo order mới cho phần tách và thanh toán độc lập.

**Luồng ngoại lệ:**
- E1: Voucher không hợp lệ/hết hạn.
- E2: Chưa chọn món nhưng yêu cầu tách bill.

**Ràng buộc:**
- Chỉ cho checkout order active.

**Yêu cầu phi chức năng:**
- Tạo bill hoàn tất dưới 2 giây, đảm bảo toàn vẹn giao dịch.

#### 3.4.2 Sơ đồ Use Case phân rã

```mermaid
flowchart LR
    CSH[Cashier] --> P1[Chọn order]
    P1 --> P2[Tính tiền]
    P2 --> P3[Áp voucher]
    P3 --> P4[Chọn phương thức thanh toán]
    P4 --> P5[Xác nhận trả tiền]
    P5 --> P6[In hóa đơn]
    P1 --> SP[Tách hóa đơn]
```

#### 3.4.3 Sơ đồ tuần tự (Sequence)

```mermaid
sequenceDiagram
    actor C as Cashier
    participant UI as POS UI
    participant PC as PosController
    participant BS as BillingService
    participant DB as MySQL

    C->>UI: Chọn order + paymentMethod
    UI->>PC: POST /admin/pos/checkout
    PC->>BS: checkout(orderId, voucherCode, method)
    BS->>DB: Insert bill + update order status
    DB-->>BS: OK
    BS-->>PC: Bill PAID
    PC-->>UI: In hóa đơn / redirect POS
```

#### 3.4.4 Mô tả giao diện
- Danh sách order chưa thanh toán.
- Khối tổng tiền: tạm tính, giảm giá, VAT, tổng thanh toán.
- Popup **VietQR** hiển thị mã thanh toán động.
- Panel chọn món để **tách hóa đơn**.

#### 3.4.5 Mô tả chi tiết dữ liệu

| Tên tiếng Việt | Tên tiếng Anh | Loại | Bắt buộc | Mô tả |
|---|---|---|---|---|
| Mã hóa đơn | billId | Long | Có | Định danh hóa đơn |
| Mã đơn | orderId | Long | Có | Đơn cần thanh toán |
| Tạm tính | subtotal | Decimal | Có | Tổng trước giảm giá/VAT |
| Giảm giá | discountAmount | Decimal | Có | Giá trị giảm thực tế |
| VAT | vatRate | Decimal | Có | Tỉ lệ thuế |
| Tổng tiền | totalAmount | Decimal | Có | Số tiền cuối |
| PTTT | paymentMethod | Enum | Có | CASH/CARD/BANKING |
| Trạng thái bill | status | Enum | Có | UNPAID/PAID |

**Ví dụ JSON checkout:**
```json
{
  "orderId": 1024,
  "voucherCode": "SUMMER10",
  "paymentMethod": "BANKING",
  "splitDetailIds": [2001, 2002]
}
```

---

### 3.5 UC-05: Quản lý kho nguyên liệu

#### 3.5.1 Đặc tả Use Case

| Thuộc tính | Giá trị |
|---|---|
| Use Case ID | UC-05 |
| Tên chức năng | Quản lý kho nguyên liệu |
| Mô tả | Theo dõi tồn kho, ngưỡng cảnh báo, cập nhật nhập/xuất |
| Tác nhân | Admin, Manager |
| Trigger | Nhập hàng mới hoặc tiêu hao từ order |
| Điều kiện tiên quyết | Đăng nhập quyền quản lý kho |
| Hậu điều kiện | Số lượng tồn và cảnh báo kho được cập nhật |

**Luồng cơ bản:**
1. Mở màn hình kho.
2. Thêm mới nguyên liệu hoặc cập nhật tồn.
3. Hệ thống lưu dữ liệu và kiểm tra ngưỡng cảnh báo.

**Luồng thay thế:**
- A1: Cập nhật tồn kho hàng loạt theo phiếu nhập.

**Luồng ngoại lệ:**
- E1: Nhập số lượng âm/đơn vị không hợp lệ.

**Ràng buộc:**
- Không cho tồn kho âm sau khi trừ định mức.

**Yêu cầu phi chức năng:**
- Truy vấn danh sách kho dưới 1 giây với 10.000 bản ghi.

#### 3.5.2 Sơ đồ Use Case phân rã

```mermaid
flowchart LR
    MGR[Manager/Admin] --> I1[Xem tồn kho]
    MGR --> I2[Thêm nguyên liệu]
    MGR --> I3[Cập nhật số lượng tồn]
    I3 --> I4[Kiểm tra reorder level]
    I4 --> I5[Sinh cảnh báo tồn thấp]
```

#### 3.5.3 Sơ đồ tuần tự (Sequence)

```mermaid
sequenceDiagram
    actor M as Manager
    participant UI as Inventory UI
    participant IC as InventoryController
    participant IS as InventoryService
    participant DB as MySQL

    M->>UI: Cập nhật tồn kho
    UI->>IC: POST /admin/inventory/update-stock
    IC->>IS: updateStock(...)
    IS->>DB: UPDATE ingredients
    DB-->>IS: OK
    IS-->>IC: success
    IC-->>UI: Thông báo kết quả
```

#### 3.5.4 Mô tả giao diện
- Bảng kho gồm tên, đơn vị, tồn hiện tại, ngưỡng đặt hàng.
- Cột cảnh báo màu khi tồn <= ngưỡng.
- Form cập nhật nhanh tồn kho.

#### 3.5.5 Mô tả chi tiết dữ liệu

| Tên tiếng Việt | Tên tiếng Anh | Loại | Bắt buộc | Mô tả |
|---|---|---|---|---|
| Mã nguyên liệu | ingredientId | Long | Có | Định danh nguyên liệu |
| Tên nguyên liệu | ingredientName | String | Có | Duy nhất |
| Tồn kho | quantityInStock | Decimal | Có | Số lượng còn lại |
| Đơn vị | unit | String | Có | gram/ml/chai... |
| Ngưỡng đặt hàng | reorderLevel | Decimal | Có | Mức cảnh báo |

**Ví dụ SQL kiểm tra tồn thấp:**
```sql
SELECT ingredient_name, quantity_in_stock, reorder_level
FROM ingredients
WHERE quantity_in_stock <= reorder_level
ORDER BY quantity_in_stock ASC;
```

---

### 3.6 UC-06: Cấu hình thực đơn & giá

#### 3.6.1 Đặc tả Use Case

| Thuộc tính | Giá trị |
|---|---|
| Use Case ID | UC-06 |
| Tên chức năng | Cấu hình thực đơn & giá |
| Mô tả | Quản lý món, giá bán, ảnh món, trạng thái và định mức nguyên liệu |
| Tác nhân | Admin, Manager |
| Trigger | Cập nhật menu theo mùa/chính sách giá |
| Điều kiện tiên quyết | Có quyền quản trị menu |
| Hậu điều kiện | Menu đồng bộ trạng thái và recipe định mức |

**Luồng cơ bản:**
1. Quản trị mở trang menu.
2. Thêm/sửa món, giá, mô tả.
3. Upload ảnh món qua Cloudinary.
4. Gán định mức nguyên liệu cho món.
5. Lưu và công bố trạng thái món.

**Luồng thay thế:**
- A1: Ẩn món tạm thời bằng trạng thái `OUT_OF_STOCK`.

**Luồng ngoại lệ:**
- E1: Upload ảnh lỗi hoặc dữ liệu giá không hợp lệ.

**Ràng buộc:**
- Tên món duy nhất, giá > 0.

**Yêu cầu phi chức năng:**
- Upload ảnh và trả URL thành công > 99% trong điều kiện mạng ổn định.

#### 3.6.2 Sơ đồ Use Case phân rã

```mermaid
flowchart LR
    A[Admin/Manager] --> M1[Thêm món]
    A --> M2[Sửa giá món]
    A --> M3[Đổi trạng thái món]
    A --> M4[Upload ảnh Cloudinary]
    A --> M5[Cấu hình công thức món]
```

#### 3.6.3 Sơ đồ tuần tự (Sequence)

```mermaid
sequenceDiagram
    actor A as Admin
    participant UI as Menu Admin UI
    participant MC as MenuManagementController
    participant CS as CloudinaryService
    participant DB as MySQL

    A->>UI: Nhập thông tin món + chọn ảnh
    UI->>MC: POST /admin/menu/add
    MC->>CS: uploadImage(file)
    CS-->>MC: imageUrl
    MC->>DB: Insert menu_items/menu_item_recipes
    DB-->>MC: OK
    MC-->>UI: Lưu thành công
```

#### 3.6.4 Mô tả giao diện
- Bảng món: tên, danh mục, giá, trạng thái, ảnh.
- Form công thức món (ingredient + quantity_required).
- Nút bật/tắt khả dụng món.

#### 3.6.5 Mô tả chi tiết dữ liệu

| Tên tiếng Việt | Tên tiếng Anh | Loại | Bắt buộc | Mô tả |
|---|---|---|---|---|
| Mã món | menuItemId | Long | Có | Định danh món |
| Tên món | name | String | Có | Duy nhất |
| Giá bán | price | Decimal | Có | > 0 |
| Danh mục | category | Enum | Có | APPETIZER/MAIN/DRINK/DESSERT |
| Trạng thái món | status | Enum | Có | AVAILABLE/OUT_OF_STOCK |
| Ảnh món | imageUrl | String | Không | URL Cloudinary |

---

### 3.7 UC-07: Xem báo cáo doanh thu

#### 3.7.1 Đặc tả Use Case

| Thuộc tính | Giá trị |
|---|---|
| Use Case ID | UC-07 |
| Tên chức năng | Xem báo cáo doanh thu |
| Mô tả | Dashboard doanh thu tháng/ngày, số bill, top món, cảnh báo kho |
| Tác nhân | Admin, Manager |
| Trigger | Người dùng mở trang dashboard |
| Điều kiện tiên quyết | Có quyền xem báo cáo |
| Hậu điều kiện | Dữ liệu KPI hiển thị chính xác theo kỳ lọc |

**Luồng cơ bản:**
1. Truy cập dashboard.
2. Hệ thống tính tổng doanh thu hóa đơn `PAID` theo kỳ.
3. Hiển thị số bill, biểu đồ 7 ngày, top món.
4. Hiển thị danh sách nguyên liệu tồn thấp.

**Luồng thay thế:**
- A1: Không có dữ liệu kỳ hiện tại → hiển thị 0 và thông báo.

**Luồng ngoại lệ:**
- E1: Lỗi truy vấn dữ liệu thống kê.

**Ràng buộc:**
- Chỉ tính doanh thu từ bill `PAID`.

**Yêu cầu phi chức năng:**
- Dashboard tải trong vòng 3 giây với dữ liệu chuẩn vận hành.

#### 3.7.2 Sơ đồ Use Case phân rã

```mermaid
flowchart LR
    MG[Manager/Admin] --> R1[Xem KPI doanh thu]
    MG --> R2[Xem số lượng bill]
    MG --> R3[Xem top món bán chạy]
    MG --> R4[Xem biểu đồ 7 ngày]
    MG --> R5[Xem cảnh báo tồn kho]
```

#### 3.7.3 Sơ đồ tuần tự (Sequence)

```mermaid
sequenceDiagram
    actor M as Manager
    participant UI as Dashboard UI
    participant DC as DashboardController
    participant RS as ReportService
    participant IS as InventoryService
    participant DB as MySQL

    M->>UI: Mở /admin/dashboard
    UI->>DC: GET /admin/dashboard
    DC->>RS: getRevenue/getBillCount/getTopSellingItems
    RS->>DB: Query bills + order_details
    DB-->>RS: Dataset
    DC->>IS: getAllIngredients
    IS->>DB: Query ingredients
    DB-->>IS: Dataset
    DC-->>UI: Render chart + cảnh báo
```

#### 3.7.4 Mô tả giao diện
- Card KPI doanh thu tháng, số bill tháng.
- Biểu đồ doanh thu 7 ngày gần nhất.
- Biểu đồ top món bán chạy.
- Bảng cảnh báo nguyên liệu dưới ngưỡng.

#### 3.7.5 Mô tả chi tiết dữ liệu

| Tên tiếng Việt | Tên tiếng Anh | Loại | Bắt buộc | Mô tả |
|---|---|---|---|---|
| Doanh thu kỳ | revenue | Decimal | Có | Tổng totalAmount bill PAID |
| Số hóa đơn | billCount | Long | Có | Số bill PAID theo kỳ |
| Top món | topItems | Map<String,Integer> | Có | Tên món và số lượng bán |
| Tồn thấp | lowStock | List<Ingredient> | Có | DS nguyên liệu <= ngưỡng |

---

## 4. COMPONENT, THÔNG BÁO, CẢNH BÁO

### 4.1 Bảng thông báo hệ thống

| Loại | Màu chuẩn | Ký hiệu | Ý nghĩa | Ví dụ |
|---|---|---|---|---|
| **THÔNG TIN** | Xanh dương | ⓘ | Cập nhật trung tính | ⓘ Đơn hàng #1024 đang chờ bếp xác nhận |
| **THÀNH CÔNG** | Xanh lá | ✓ | Thao tác hoàn tất | ✓ Thanh toán hóa đơn #B-20260722-01 thành công |
| **CẢNH BÁO** | Vàng/Cam | ⚠ | Cần chú ý ngay | ⚠ Nguyên liệu "Phô mai" sắp hết (<= reorder level) |
| **LỖI NGHIÊM TRỌNG** | Đỏ | ✕ | Giao dịch thất bại hoặc dữ liệu lỗi | ✕ Không thể tạo hóa đơn, vui lòng thử lại hoặc liên hệ quản trị |

### 4.2 Quy tắc hiển thị
- Thông báo phải rõ ràng, có ngữ cảnh (mã bàn, mã đơn, mã hóa đơn).
- Lỗi nghiệp vụ và lỗi hệ thống cần tách thông điệp.
- Với lỗi nghiêm trọng, bắt buộc hiển thị hành động tiếp theo cho người dùng.

> **Lưu ý quan trọng:** Mọi thông báo tài chính (checkout, split bill, refund) phải ghi log để truy vết giao dịch.

---

## 5. LINK ISSUE

| Issue Key | Tiêu đề | Mô tả ngắn |
|---|---|---|
| RMS-101 | Table Layout & Reservation | Xây dựng sơ đồ bàn và thao tác mở/giữ/dọn bàn |
| RMS-102 | Table Order Entry | Gọi món tại bàn, cập nhật món theo bàn |
| RMS-103 | Kitchen Display Workflow | Điều phối trạng thái món trong bếp |
| RMS-104 | POS Billing & Receipt | Thanh toán, in hóa đơn, hỗ trợ VietQR |
| RMS-105 | Inventory Management | Quản lý nguyên liệu, ngưỡng cảnh báo |
| RMS-106 | Menu & Pricing Configuration | Quản lý món, giá, ảnh Cloudinary, công thức |
| RMS-107 | Revenue Dashboard | Doanh thu, số bill, top món, biểu đồ |
| RMS-108 | Voucher Lifecycle | Quản lý mã giảm giá và validate |
| RMS-109 | Shift Opening/Closing | Mở ca/đóng ca và đối soát quỹ |

