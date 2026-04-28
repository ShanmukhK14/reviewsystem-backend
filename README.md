# ReviewSystem Backend — Spring Boot + MySQL
## FSAD-PS33 | Sem End Project

---

## ⚙️ Prerequisites
- Java 17+
- Maven
- MySQL 8.0+
- Spring Tool Suite (STS)

---

## 🗄️ Step 1 — Setup MySQL Database

Open MySQL Workbench or MySQL command line and run:

```sql
CREATE DATABASE reviewsystemdb;
```

That's it! Spring Boot will auto-create all tables on first run.

---

## ⚙️ Step 2 — Configure Database Password

Open: `src/main/resources/application.properties`

Change these lines to match YOUR MySQL setup:
```properties
spring.datasource.username=root
spring.datasource.password=root   ← change this to your MySQL password
```

---

## ▶️ Step 3 — Run in STS

1. File → Import → Maven → Existing Maven Projects
2. Browse to this folder → Finish
3. Right-click `ReviewSystemApplication.java` → Run As → Spring Boot App
4. Watch console for: `🚀 ReviewSystem MySQL database ready!`

---

## ✅ Step 4 — Verify

Open browser:
```
http://localhost:8080/api/forms
```
Should return JSON with 3 forms.

---

## 🔑 Default Accounts (auto-seeded)
| Role    | Email                        | Password  |
|---------|------------------------------|-----------|
| Admin   | professor@university.edu     | password  |
| Student | student@university.edu       | password  |
| Student | priya@university.edu         | password  |

---

## 📡 All API Endpoints

| Method | URL                              | Description           |
|--------|----------------------------------|-----------------------|
| POST   | /api/auth/login                  | Login                 |
| POST   | /api/auth/register               | Register              |
| GET    | /api/auth/users                  | All users             |
| GET    | /api/forms                       | All forms             |
| GET    | /api/forms/active                | Active forms only     |
| GET    | /api/forms/{id}                  | Get form by ID        |
| GET    | /api/forms/admin/{adminId}       | Forms by admin        |
| POST   | /api/forms?adminId=1             | Create form           |
| PUT    | /api/forms/{id}                  | Update form           |
| DELETE | /api/forms/{id}                  | Delete form           |
| POST   | /api/submissions                 | Submit feedback       |
| GET    | /api/submissions/check           | Check if submitted    |
| GET    | /api/submissions/student/{id}    | Student submissions   |
| GET    | /api/submissions/form/{id}       | Form submissions      |
| GET    | /api/analytics/dashboard         | Dashboard stats       |
| GET    | /api/analytics/{formId}          | Form analytics        |

---

## 🗄️ MySQL Tables Created Automatically
- `users`
- `feedback_forms`
- `questions`
- `submissions`
- `answers`
