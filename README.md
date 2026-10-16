# SE — University Library Management System

سیستم مدیریت کتابخانه دانشگاه — پروژه درس برنامه‌نویسی پیشرفته (AP)

A console-based University Library Management System written in Java, implementing four user roles (Guest, Student, Employee, Manager) with full borrow-request workflow, statistics and reporting.

## امکانات

### 🧑‍🎓 دانشجو (Student)
- ثبت‌نام در سیستم و ورود با نام کاربری و کلمه عبور
- جستجوی ترکیبی کتاب بر اساس عنوان / نویسنده / سال نشر همراه با وضعیت امانت
- ثبت درخواست امانت با **بازه زمانی** (تاریخ شروع و پایان)

### 👤 مهمان (Guest)
- مشاهده آمار کلی (تعداد دانشجویان، کتاب‌ها، امانت‌ها، کتاب‌های در امانت)
- جستجو فقط بر اساس نام کتاب — نتایج صرفاً شامل اطلاعات کتاب

### 👨‍💼 کارمند (Employee)
- ثبت، جستجو و ویرایش اطلاعات کتاب
- بررسی و تایید درخواست‌های امانتی با تاریخ شروع «امروز یا دیروز»
- امانت دادن کتاب به دانشجو و ثبت زمان تحویل / دریافت کتاب برگشتی و ثبت زمان دریافت
- مشاهده گزارش تاریخچه امانات یک دانشجو (کل امانت‌ها، تحویل‌نشده‌ها، تحویل‌های دیرهنگام)
- فعال / غیرفعال کردن دانشجو
- تغییر رمز عبور

### 👔 مدیر (Manager)
- تعریف کارمند جدید
- مشاهده عملکرد کارمندان (کتاب ثبت‌شده، امانت داده، تحویل گرفته)
- آمار امانات: تعداد درخواست‌ها، کل امانت‌ها، میانگین مدت امانت
- آمار دانشجویان + ۱۰ دانشجو با بیشترین تاخیر در تحویل

## اجرا (Run)

نیازمندی: **JDK 8+**

```bash
# با IntelliJ IDEA
پوشه پروژه را باز کنید و کلاس ap.projects.finalproject.LibrarySystem را اجرا کنید.

# یا با خط فرمان
javac -encoding UTF-8 -d out src/ap/projects/finalproject/*.java
java -cp out ap.projects.finalproject.LibrarySystem
```

### حساب‌های پیش‌فرض

| نقش | نام کاربری | رمز |
|------|-----------|------|
| مدیر | `admin` | `1234` |
| کارمند | `staff` | `1234` |

## ساختار (Structure)

```
src/ap/projects/finalproject/
├── LibrarySystem.java        # نقطه شروع برنامه (main) و منطق اصلی
├── MenuHandler.java          # منوهای کنسولی و تعامل با کاربر
├── Book / BookManager / BookFileManager
├── Student / StudentManager / StudentFileManager
├── Employee / EmployeeManager / EmployeeFileManager
├── EmployeePerformance(+Manager/FileManager)   # عملکرد کارمندان
├── BorrowRecord / BorrowManager / BorrowFileManager
├── BorrowRequest / BorrowRequestManager / BorrowRequestFileManager
├── BorrowStatistics / StudentStatistics / StudentBorrowReport
└── StatisticsManager         # آمار عمومی کتابخانه
```

داده‌ها به‌صورت خودکار با Java Serialization در فایل‌های `.dat` کنار برنامه ذخیره می‌شوند.

## Tech

- Java 8 (Console I/O)
- Object Serialization for persistence
- Streams API for queries and statistics
