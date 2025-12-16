# 🛒 CityMart Retail Store System

![Java](https://img.shields.io/badge/Java-JDK%2017-orange)
![JavaFX](https://img.shields.io/badge/UI-JavaFX-blue)
![MySQL](https://img.shields.io/badge/Database-MySQL-lightgrey)
![Status](https://img.shields.io/badge/Status-Completed-brightgreen)

A comprehensive **Inventory Management & Point of Sale (POS) System** designed for modern retail environments. This application replaces manual record-keeping with an automated solution for stock tracking, billing, and business intelligence reporting.

---

## 📂 Project Structure (MVC Architecture)

The system follows a strict **Model-View-Controller (MVC)** design pattern to separate logic, data, and interface.

### **1. Controllers (UI Logic)**
Handles user interactions. All controllers extend `BaseController` for shared functionality like shortcuts.
![Controller Structure](Screenshot/Screenshot%202025-12-02%20011136.png)

### **2. Services & Views**
* **Service:** Contains business logic like `EmailService` (background threading).
* **View:** Contains `.fxml` layouts and assets.
![Service & View Structure](Screenshot/Screenshot%202025-12-02%20011208.png)

### **3. Dependencies & Libraries**
Uses `JavaFX 19`, `MySQL Connector`, `BCrypt` (Security), and `JavaMail`.
![Dependencies](Screenshot/Screenshot%202025-12-02%20011219.png)

---

## 🚀 Key Features

### **1. Automated Email Alerts**
The system sends instant HTML-formatted emails to the Manager for security and inventory events.
* **Low Stock Alert:** Triggered immediately when item stock drops below the reorder level.
* **Login Alert:** Triggered on every successful login for security monitoring.

![Email Alert](Screenshot/WhatsApp%20Image%202025-12-04%20at%2023.10.52_ee42ba6f.jpg)

### **2. Dynamic Reporting Engine**
Generates Sales, Inventory, and Performance reports with a single click.
* **Format:** HTML/PDF (Printable).
* **Delivery:** Can be emailed directly from the dashboard.

![Report Preview](Screenshot/Screenshot%202025-12-04%20225954.png)

---

## 🛠️ Technology Stack

* **Language:** Java 17
* **UI Framework:** JavaFX 19 (Modular)
* **Database:** MySQL 8.0
* **Build Tool:** Apache Maven
* **Security:** BCrypt (Password Hashing)
* **Communication:** JavaMail API (SMTP)

---

## ⚙️ Installation & Setup

### **Prerequisites**
* Java Development Kit (JDK) 17 or higher
* Maven 3.8+
* MySQL Server 8.0

### **1. Database Setup**
Create a MySQL database named `citymart_db` and execute the provided SQL script to set up tables (`users`, `products`, `sales`, `sale_items`).

### **2. Configuration**
Update the `src/com/inventory/util/DBConnection.java` file with your MySQL credentials:
```java
private static final String URL = "jdbc:mysql://localhost:3306/citymart_db";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
⌨️ Keyboard ShortcutsKey,Action
F1,Go to Dashboard
F2,Go to Inventory
F3,Go to Billing (POS)
F4,Go to Reports
Ctrl + P,Print Current Report
Ctrl + E,Email Report to Manager
ESC,Logout / Exit
```
👨‍💻 Author
Avishka D. Rajapaksha
