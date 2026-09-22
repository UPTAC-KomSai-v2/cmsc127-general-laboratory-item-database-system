# General Laboratory Items Management System (GLIMS)

![Project Status](https://img.shields.io/badge/status-stable-green.svg)
![Java Version](https://img.shields.io/badge/java-11%2B-blue.svg)
![Database](https://img.shields.io/badge/database-MySQL_8.0-orange.svg)
![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)

A robust, full-stack desktop application built with Java Swing and MySQL for managing and tracking laboratory equipment and materials for the UP Tacloban College General Laboratory.

---

## Table of Contents

- [About The Project](#about-the-project)
- [Key Features](#key-features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
- [Project Architecture](#project-architecture)
- [Database Schema](#database-schema)
- [Authors](#authors)
- [License](#license)

---

## About The Project

GLIMS is an all-in-one solution designed to replace manual, paper-based inventory tracking with an efficient, reliable, and user-friendly digital system. It provides laboratory staff with the tools to oversee the entire lifecycle of lab items—from borrowing and returning to inventory updates and historical tracking.

The system is built with data integrity and user experience as top priorities, featuring a clean MVC architecture, robust database transaction management, and a responsive Swing-based user interface.

---

## Key Features

✨ **Secure Authentication:** A dedicated login screen to ensure only authorized staff can access the system.

📋 **Multi-Step Borrowing Process:**
- Browse items by category.
- Add multiple items to a "borrow basket".
- Adjust quantities before checkout.
- Record detailed borrower and course information.

↩️ **Efficient Item Returns:**
- View a list of all active borrowers.
- Select specific items and quantities to return.
- Automatic calculation of late fees based on database triggers.

📦 **Comprehensive Inventory Management:**
- View item stock levels by category.
- Directly update item quantities in a table view.
- Add new items or permanently remove obsolete ones.
- One-click import of initial inventory from a CSV file.

📈 **Complete Transaction History:**
- A chronological log of all borrow and return events.
- Filter the view to see only borrows, only returns, or all transactions.

🧾 **Automated Receipt Generation:**
- Automatically creates and saves `.txt` receipts for both borrow and return transactions for auditing and record-keeping.

🧰 **Manual Database Setup:**
- The schema and reference data are provided as plain SQL files under `database/`; import them once before first use (see Installation below).

---

## Screenshots

| Login Screen                                     | Main Menu                               |
| ------------------------------------------------ | --------------------------------------- |
| ![Login Screen](docs/screenshots/login.png)      | ![Main Menu](docs/screenshots/main_menu.png) |
| **Borrowing Process (Step 1: Item Selection)**   | **Inventory Management Panel**          |
| ![Borrow Items](docs/screenshots/borrow_items.png) | ![Update Inventory](docs/screenshots/inventory.png) |

---

## Tech Stack

- **Frontend:** Java Swing
- **Backend:** Java 17
- **Build:** Maven
- **Database:** MySQL 8.0+
- **Connectivity:** JDBC (Java Database Connectivity)

---

## Getting Started

Follow these instructions to get a local copy of GLIMS up and running on your machine.

### Prerequisites

You must have the following software installed on your system:

- **Java Development Kit (JDK):** Version 17 or newer.
- **Apache Maven**
- **MySQL Server:** Version 8.0 or newer.
- **An IDE (Optional but Recommended):** IntelliJ IDEA, Eclipse, or VS Code with Java extensions (VS Code's Java extension pack auto-detects the Maven `pom.xml`).

### Installation

1.  **Clone the Repository**
    ```sh
    git clone https://github.com/UPTAC-KomSai-v2/cmsc127-general-laboratory-item-database-system.git
    cd cmsc127-general-laboratory-item-database-system
    ```

2.  **Create the database and load the schema**
    There is no automatic database provisioning on login — run the SQL files yourself, once, before first launch:
    ```sh
    mysql -u <user> -p < database/schema.sql
    mysql -u <user> -p genlab_db < database/seed.sql   # optional reference/sample data
    ```
    `database/schema.sql` creates the `genlab_db` database, tables, triggers, and views. `database/seed.sql` loads course/section/instructor/category reference data plus a handful of synthetic sample borrowers for local testing — it contains no real personal data.

3.  **Configure the database connection**
    By default the app connects to `jdbc:mysql://localhost:3306/genlab_db`. To point at a different host, set the `GLIMS_DB_URL` environment variable before launching, e.g.:
    ```sh
    export GLIMS_DB_URL="jdbc:mysql://localhost:3306/genlab_db"
    ```
    The MySQL **username and password are entered at the login screen** — they're used directly as your MySQL credentials, so log in with a MySQL user that has privileges on `genlab_db`.

4.  **Build and run**
    ```sh
    mvn -q package
    java -jar target/glims-1.0.0.jar
    ```
    Run this from the repository root (or copy the `Assets/`, `Borrow Receipts/`, and `Return Receipts/` folders next to the jar) — images, fonts, and receipts are resolved relative to the working directory.

---

## Usage

1.  **Login** with your staff credentials.
2.  Use the four main buttons on the **Main Menu** to navigate to the desired function:
    - **Borrow Item:** To check out new items for a borrower.
    - **Borrower List:** To view active loans and process returns.
    - **Update Inventory:** To manage the master item list.
    - **Transaction History:** To review past activities.
3.  Follow the on-screen instructions and prompts within each panel.
4.  Use the **"Go Back"** button to return to the Main Menu from any sub-panel.
5.  Always use the **"Logout"** button to securely exit the application.

---

## Project Architecture

The project is structured using the **Model-View-Controller (MVC)** design pattern to ensure a clean separation of concerns. Source lives under `src/main/java/com/glims/` (Maven's standard layout); there is currently a single `com.glims` package rather than a deeper repository/service split.

-   **Model:** Represents the data and business logic.
    -   `database/schema.sql`, `database/seed.sql`: The database schema and reference/sample data (see [Database Schema](#database-schema)).
    -   `Queries.java`: Acts as the Data Access Object (DAO), handling all JDBC communication and SQL queries. It uses `PreparedStatement` to prevent SQL injection and manages database transactions for data integrity.
    -   Database Triggers: Core business logic (e.g., updating stock on borrow/return) is embedded in the database itself for maximum robustness.

-   **View:** The user interface of the application.
    -   `GUI*.java` files: All classes prefixed with `GUI` are responsible for rendering the UI components. They do not contain business logic.
    -   `Branding.java`: A centralized class for managing UI constants like colors, fonts, and icons, ensuring a consistent look and feel.
    -   `LoadingScreen.java`: Provides user feedback during long-running initial data loads.

-   **Controller:** Acts as the intermediary between the Model and the View.
    -   `Controller.java`: Manages the application's state by caching data from the database on startup. It handles user input from the View, processes it, and calls the appropriate methods in the Model. It then updates the View with the results.
    -   `GraphicalUserInterface.java`: Acts as the main event handler, orchestrating the swapping of different View panels and delegating actions to the `Controller`. It uses `SwingWorker` for background processing to keep the UI responsive.

---

## Database Schema

The database is designed to be normalized and robust, with foreign key constraints, indexes for performance, and views for simplifying complex queries.

A brief overview of the key tables:
- `borrower`: Stores information about individuals who borrow items.
- `item`: The master list of all laboratory items, their quantities, and categories.
- `borrow`: A transactional table linking borrowers to the items they've borrowed, including dates and quantities.
- `return_log`: A log of all returned items, which automatically calculates and stores any applicable late fees.
- `category`, `course`, `section`, `instructor`: Supporting tables that normalize the data.

An Entity-Relationship Diagram (ERD) would look like this:

![ERD](docs/erd/database_schema.png)

### Configuration

| Variable        | Default                                    | Purpose                        |
| ---------------- | ------------------------------------------- | ------------------------------- |
| `GLIMS_DB_URL`   | `jdbc:mysql://localhost:3306/genlab_db`     | JDBC connection URL             |

The database username and password are entered at the login screen (not read from environment variables) and used directly as your MySQL credentials — there is no separate application-level user table. Never commit real credentials or a `.env` file to the repository.

---

## Authors

This project was developed by:

- **Sean Harvey Bantanos** - *Database Architect*
- **Mac Darren Louis Calimba** - *Frontend Developer*
- **Norman Enrico Eulin** - *Frontend Developer*
- **Rolf Genree Garces** - *Backend Developer*
- **Jhun Kenneth Iniego** - *Backend Developer*
- **Jade Eric Petilla** - *Database Architect*
- **Gian Angelo Tongzon** - *Frontend Developer*

---

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
