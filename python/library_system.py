#!/usr/bin/env python3
"""University Library Management System — Python implementation.

Console application for the Advanced Programming (AP) course project:
four user roles (Guest, Student, Employee, Manager), a full borrow-request
workflow with start/end dates, employee performance tracking and statistics.

Data is persisted as JSON files in the current working directory.
Requires Python 3.8+ (standard library only).
"""

from __future__ import annotations

import json
import os
import sys
from dataclasses import dataclass, asdict
from datetime import date, timedelta
from typing import Callable, List, Optional, Tuple

import database


# ---------------------------------------------------------------------------
# Persistence helpers
# ---------------------------------------------------------------------------

def _load_json(filename: str, default):
    path = os.path.join(os.getcwd(), filename)
    if not os.path.exists(path):
        return default
    try:
        with open(path, "r", encoding="utf-8") as fh:
            return json.load(fh)
    except (OSError, json.JSONDecodeError) as exc:
        print(f"Error loading {filename}: {exc}")
        return default


def _save_json(filename: str, data) -> None:
    path = os.path.join(os.getcwd(), filename)
    try:
        with open(path, "w", encoding="utf-8") as fh:
            json.dump(data, fh, indent=2, ensure_ascii=False)
    except OSError as exc:
        print(f"Error saving {filename}: {exc}")


# ---------------------------------------------------------------------------
# Domain models (dates are stored as ISO-8601 strings)
# ---------------------------------------------------------------------------

@dataclass
class Book:
    title: str
    author: str
    publication_year: int
    isbn: str
    available: bool = True

    def info(self, with_status: bool) -> str:
        line = (f"Title: {self.title} | Author: {self.author} | "
                f"Year: {self.publication_year} | ISBN: {self.isbn}")
        if with_status:
            line += " | Status: " + ("Available" if self.available else "Borrowed")
        return line

    def matches(self, title=None, author=None, year=None, isbn=None) -> bool:
        """In-memory convenience filter (the managers query SQLite directly)."""
        if title and title.lower() not in self.title.lower():
            return False
        if author and author.lower() not in self.author.lower():
            return False
        if year is not None and self.publication_year != year:
            return False
        if isbn and isbn.lower() not in self.isbn.lower():
            return False
        return True


@dataclass
class Student:
    name: str
    student_id: str
    username: str
    password: str
    is_active: bool = True

    def __str__(self) -> str:
        status = "Active" if self.is_active else "Inactive"
        return (f"Name: {self.name} | Student ID: {self.student_id} | "
                f"Username: {self.username} | Status: {status}")


@dataclass
class Employee:
    name: str
    employee_id: str
    username: str
    password: str
    role: str

    def is_manager(self) -> bool:
        return self.role.lower() == "manager"

    def is_staff(self) -> bool:
        return self.role.lower() == "staff"

    def __str__(self) -> str:
        return (f"Name: {self.name} | Employee ID: {self.employee_id} | "
                f"Username: {self.username} | Role: {self.role}")


@dataclass
class BorrowRequest:
    student_username: str
    book_isbn: str
    request_date: str
    desired_start: str
    desired_end: str
    status: str = "pending"
    rejection_reason: str = ""
    id: Optional[int] = None

    def is_pending(self) -> bool:
        return self.status == "pending"

    def is_approved(self) -> bool:
        return self.status == "approved"

    def is_rejected(self) -> bool:
        return self.status == "rejected"

    def is_approvable_date(self) -> bool:
        """Only requests whose start date is today or yesterday may be approved."""
        today = date.today().isoformat()
        yesterday = (date.today() - timedelta(days=1)).isoformat()
        return self.desired_start in (today, yesterday)

    def __str__(self) -> str:
        line = (f"Student: {self.student_username} | Book ISBN: {self.book_isbn} | "
                f"Request Date: {self.request_date} | "
                f"Desired Period: {self.desired_start} to {self.desired_end} | "
                f"Status: {self.status}")
        if self.is_rejected():
            line += f" | Reason: {self.rejection_reason}"
        return line


@dataclass
class BorrowRecord:
    student_username: str
    book_isbn: str
    borrow_date: str
    due_date: str
    return_date: Optional[str] = None
    actual_pickup_date: Optional[str] = None
    returned: bool = False
    picked_up: bool = False
    id: Optional[int] = None

    def is_overdue(self) -> bool:
        return (self.picked_up and not self.returned
                and date.today().isoformat() > self.due_date)

    def is_returned_late(self) -> bool:
        return self.returned and self.return_date is not None and self.return_date > self.due_date

    def is_late(self) -> bool:
        return self.is_returned_late() or self.is_overdue()

    def is_ready_for_pickup(self) -> bool:
        return not self.picked_up and not self.returned

    def days_overdue(self) -> int:
        if not self.is_overdue():
            return 0
        return (date.today() - date.fromisoformat(self.due_date)).days

    def __str__(self) -> str:
        if self.returned:
            status = ("Returned late on " + self.return_date
                      if self.is_returned_late() else "Returned on " + self.return_date)
        elif self.picked_up:
            status = (f"OVERDUE ({self.days_overdue()} days)"
                      if self.is_overdue() else f"Due on {self.due_date}")
        else:
            status = "Ready for pickup"
        picked = f" | Picked up: {self.actual_pickup_date}" if self.actual_pickup_date else ""
        return (f"Book ISBN: {self.book_isbn} | Borrowed: {self.borrow_date} | "
                f"Due: {self.due_date}{picked} | Status: {status}")


@dataclass
class EmployeePerformance:
    employee_username: str
    books_added: int = 0
    books_edited: int = 0
    books_borrowed: int = 0
    books_returned: int = 0
    borrow_requests_approved: int = 0
    borrow_requests_rejected: int = 0
    last_activity_date: str = ""

    def __post_init__(self):
        if not self.last_activity_date:
            self.last_activity_date = date.today().isoformat()

    def total_activities(self) -> int:
        return (self.books_added + self.books_edited + self.books_borrowed
                + self.books_returned + self.borrow_requests_approved
                + self.borrow_requests_rejected)

    def display(self) -> None:
        print("\n=== Employee Performance Report ===")
        print(f"Employee: {self.employee_username}")
        print(f"Books Added: {self.books_added}")
        print(f"Books Edited: {self.books_edited}")
        print(f"Books Lent: {self.books_borrowed}")
        print(f"Books Received Back: {self.books_returned}")
        print(f"Borrow Requests Approved: {self.borrow_requests_approved}")
        print(f"Borrow Requests Rejected: {self.borrow_requests_rejected}")
        print(f"Total Activities: {self.total_activities()}")
        print(f"Last Activity Date: {self.last_activity_date}")


# ---------------------------------------------------------------------------
# Managers
# ---------------------------------------------------------------------------

def _row_to_student(row) -> Student:
    return Student(row["name"], row["student_id"], row["username"],
                   row["password"], bool(row["is_active"]))


class StudentManager:
    def find(self, username: str) -> Optional[Student]:
        row = database.query_one("SELECT * FROM students WHERE username = ?", (username,))
        return _row_to_student(row) if row else None

    def all(self) -> List[Student]:
        return [_row_to_student(row)
                for row in database.query_all("SELECT * FROM students ORDER BY username")]

    def register(self, name, student_id, username, password) -> None:
        if not username or not password:
            print("Username and password cannot be empty.")
            return
        if self.find(username):
            print("This username already exists. Please choose a different username.")
            return
        database.execute(
            "INSERT INTO students (name, student_id, username, password) VALUES (?, ?, ?, ?)",
            (name, student_id, username, password))
        print("Student registration completed successfully.")

    def authenticate(self, username: str, password: str) -> Optional[Student]:
        student = self.find(username)
        if student is None or student.password != password:
            return None
        if not student.is_active:
            print("Your account is inactive. Please contact the library administrator.")
            return None
        return student

    def update_info(self, username, new_name, new_password) -> bool:
        assignments, params = [], []
        if new_name:
            assignments.append("name = ?")
            params.append(new_name)
        if new_password:
            assignments.append("password = ?")
            params.append(new_password)
        if not assignments:
            return False
        params.append(username)
        return database.execute(
            "UPDATE students SET " + ", ".join(assignments) + " WHERE username = ?",
            tuple(params)) > 0

    def toggle_status(self, username: str) -> bool:
        return database.execute(
            "UPDATE students SET is_active = 1 - is_active WHERE username = ?",
            (username,)) > 0

    def count(self) -> int:
        return database.query_one("SELECT COUNT(*) AS total FROM students")["total"]

    def display_students_with_most_late(self, limit: int, records: List[BorrowRecord]) -> None:
        infos = []
        for student in self.all():
            late_count = sum(1 for r in records
                             if r.student_username == student.username and r.is_late())
            if late_count > 0:
                infos.append((late_count, student))
        infos.sort(key=lambda item: item[0], reverse=True)

        print(f"\n=== Top {limit} Students with Most Late Books ===")
        if not infos:
            print("No students with late books.")
            return
        for i, (late_count, student) in enumerate(infos[:limit], start=1):
            print(f"{i}. {student.name} ({student.username}) - {late_count} late book(s)")


class EmployeeManager:
    def find(self, username: str) -> Optional[Employee]:
        row = database.query_one("SELECT * FROM employees WHERE username = ?", (username,))
        return Employee(row["name"], row["employee_id"], row["username"],
                        row["password"], row["role"]) if row else None

    def register(self, name, employee_id, username, password, role) -> None:
        if not username or not password:
            print("Username and password cannot be empty.")
            return
        if self.find(username):
            print("This username already exists. Please choose a different username.")
            return
        database.execute(
            "INSERT INTO employees (name, employee_id, username, password, role) "
            "VALUES (?, ?, ?, ?, ?)", (name, employee_id, username, password, role))
        print("Employee registration completed successfully.")

    def authenticate(self, username: str, password: str) -> Optional[Employee]:
        employee = self.find(username)
        if employee is not None and employee.password == password:
            return employee
        return None

    def change_password(self, username, current_password, new_password) -> bool:
        return database.execute(
            "UPDATE employees SET password = ? WHERE username = ? AND password = ?",
            (new_password, username, current_password)) > 0

    def display_all(self) -> None:
        print("\n--- List of Employees ---")
        rows = database.query_all("SELECT * FROM employees ORDER BY username")
        if not rows:
            print("No employees registered yet.")
            return
        for row in rows:
            print(Employee(row["name"], row["employee_id"], row["username"],
                           row["password"], row["role"]))


def _row_to_book(row) -> Book:
    return Book(row["title"], row["author"], row["publication_year"],
                row["isbn"], bool(row["available"]))


class BookManager:
    def find(self, isbn: str) -> Optional[Book]:
        row = database.query_one("SELECT * FROM books WHERE isbn = ?", (isbn,))
        return _row_to_book(row) if row else None

    def all(self) -> List[Book]:
        return [_row_to_book(row)
                for row in database.query_all("SELECT * FROM books ORDER BY isbn")]

    def add_new_book(self, book: Book) -> None:
        if self.find(book.isbn):
            print("A book with this ISBN already exists.")
            return
        database.execute(
            "INSERT INTO books (isbn, title, author, publication_year) VALUES (?, ?, ?, ?)",
            (book.isbn, book.title, book.author, book.publication_year))
        print("Book added successfully!")

    def search_books(self, title=None, author=None, year=None, isbn=None) -> List[Book]:
        clauses, params = [], []
        if title:
            clauses.append("LOWER(title) LIKE ?")
            params.append(f"%{title.lower()}%")
        if author:
            clauses.append("LOWER(author) LIKE ?")
            params.append(f"%{author.lower()}%")
        if year is not None:
            clauses.append("publication_year = ?")
            params.append(year)
        if isbn:
            clauses.append("LOWER(isbn) LIKE ?")
            params.append(f"%{isbn.lower()}%")
        where = (" WHERE " + " AND ".join(clauses)) if clauses else ""
        rows = database.query_all("SELECT * FROM books" + where + " ORDER BY isbn",
                                  tuple(params))
        return [_row_to_book(row) for row in rows]

    def search_by_title(self, title: str) -> List[Book]:
        if not title:
            return []
        return self.search_books(title=title)

    def edit_book(self, isbn, new_title, new_author, new_year) -> bool:
        assignments, params = [], []
        if new_title:
            assignments.append("title = ?")
            params.append(new_title)
        if new_author:
            assignments.append("author = ?")
            params.append(new_author)
        if new_year:
            assignments.append("publication_year = ?")
            params.append(new_year)
        if not assignments:
            return False
        params.append(isbn)
        return database.execute(
            "UPDATE books SET " + ", ".join(assignments) + " WHERE isbn = ?",
            tuple(params)) > 0

    def set_available(self, isbn: str, available: bool) -> bool:
        return database.execute(
            "UPDATE books SET available = ? WHERE isbn = ? AND available <> ?",
            (int(available), isbn, int(available))) > 0

    def total_count(self) -> int:
        return database.query_one("SELECT COUNT(*) AS total FROM books")["total"]

    def display_all(self) -> None:
        print("\n--- All Books in Library ---")
        books = self.all()
        if not books:
            print("No books available in the library.")
            return
        for book in books:
            print(book.info(with_status=True))

    def display_available(self) -> None:
        print("\n--- Available Books ---")
        available = [b for b in self.all() if b.available]
        if not available:
            print("No books available at the moment.")
            return
        for book in available:
            print(book.info(with_status=False))

    @staticmethod
    def display_search_results(results: List[Book], include_status: bool) -> None:
        if not results:
            print("No books found matching your criteria.")
            return
        print("\n--- Search Results ---")
        for book in results:
            print(book.info(with_status=include_status))
        print(f"Found {len(results)} book(s)")


def _row_to_request(row) -> BorrowRequest:
    return BorrowRequest(row["student_username"], row["book_isbn"], row["request_date"],
                         row["desired_start"], row["desired_end"], row["status"],
                         row["rejection_reason"], row["id"])


class BorrowRequestManager:
    def create(self, username, isbn, start_iso, end_iso) -> None:
        if self.find_pending(username, isbn) is not None:
            print("You already have a pending request for this book.")
            return
        database.execute(
            "INSERT INTO borrow_requests (student_username, book_isbn, request_date, "
            "desired_start, desired_end) VALUES (?, ?, ?, ?, ?)",
            (username, isbn, date.today().isoformat(), start_iso, end_iso))
        print("Borrow request submitted successfully. Waiting for approval.")

    def all_requests(self) -> List[BorrowRequest]:
        return [_row_to_request(row)
                for row in database.query_all("SELECT * FROM borrow_requests ORDER BY id")]

    def pending(self) -> List[BorrowRequest]:
        return [_row_to_request(row) for row in database.query_all(
            "SELECT * FROM borrow_requests WHERE status = 'pending' ORDER BY id")]

    def pending_approvable(self) -> List[BorrowRequest]:
        today = date.today().isoformat()
        yesterday = (date.today() - timedelta(days=1)).isoformat()
        return [_row_to_request(row) for row in database.query_all(
            "SELECT * FROM borrow_requests WHERE status = 'pending' "
            "AND desired_start IN (?, ?) ORDER BY id", (today, yesterday))]

    def for_student(self, username: str) -> List[BorrowRequest]:
        return [_row_to_request(row) for row in database.query_all(
            "SELECT * FROM borrow_requests WHERE student_username = ? ORDER BY id",
            (username,))]

    def find_pending(self, username: str, isbn: str) -> Optional[BorrowRequest]:
        row = database.query_one(
            "SELECT * FROM borrow_requests WHERE student_username = ? AND book_isbn = ? "
            "AND status = 'pending' ORDER BY id LIMIT 1", (username, isbn))
        return _row_to_request(row) if row else None

    def approve(self, request: BorrowRequest) -> None:
        database.execute(
            "UPDATE borrow_requests SET status = 'approved', rejection_reason = '' "
            "WHERE id = ?", (request.id,))
        request.status = "approved"
        request.rejection_reason = ""

    def reject(self, request: BorrowRequest, reason: str) -> None:
        database.execute(
            "UPDATE borrow_requests SET status = 'rejected', rejection_reason = ? "
            "WHERE id = ?", (reason, request.id))
        request.status = "rejected"
        request.rejection_reason = reason

    def display_pending(self) -> None:
        print("\n--- Pending Borrow Requests ---")
        pending = self.pending()
        if not pending:
            print("No pending borrow requests.")
            return
        for request in pending:
            print(request)

    def display_approvable(self) -> None:
        print("\n--- Pending Borrow Requests (Start Date: Today or Yesterday) ---")
        approvable = self.pending_approvable()
        if not approvable:
            print("No pending borrow requests to approve today.")
            return
        for request in approvable:
            print(request)


def _row_to_record(row) -> BorrowRecord:
    return BorrowRecord(row["student_username"], row["book_isbn"], row["borrow_date"],
                        row["due_date"], row["return_date"], row["actual_pickup_date"],
                        bool(row["returned"]), bool(row["picked_up"]), row["id"])


class BorrowManager:
    def all_records(self) -> List[BorrowRecord]:
        return [_row_to_record(row)
                for row in database.query_all("SELECT * FROM borrow_records ORDER BY id")]

    def create_borrow(self, username, isbn, start_iso, end_iso) -> None:
        database.execute(
            "INSERT INTO borrow_records (student_username, book_isbn, borrow_date, due_date) "
            "VALUES (?, ?, ?, ?)", (username, isbn, start_iso, end_iso))

    def mark_picked_up(self, username: str, isbn: str) -> bool:
        return database.execute(
            "UPDATE borrow_records SET picked_up = 1, actual_pickup_date = ? "
            "WHERE student_username = ? AND book_isbn = ? AND picked_up = 0 AND returned = 0",
            (date.today().isoformat(), username, isbn)) > 0

    def find_ready_for_pickup(self, username: str, isbn: str) -> Optional[BorrowRecord]:
        row = database.query_one(
            "SELECT * FROM borrow_records WHERE student_username = ? AND book_isbn = ? "
            "AND picked_up = 0 AND returned = 0 ORDER BY id LIMIT 1", (username, isbn))
        return _row_to_record(row) if row else None

    def ready_for_pickup(self) -> List[BorrowRecord]:
        return [_row_to_record(row) for row in database.query_all(
            "SELECT * FROM borrow_records WHERE picked_up = 0 AND returned = 0 ORDER BY id")]

    def display_ready_for_pickup(self) -> None:
        print("\n--- Books Ready for Pickup ---")
        ready = self.ready_for_pickup()
        if not ready:
            print("No books ready for pickup.")
            return
        for record in ready:
            print(record)

    def find_active(self, username: str, isbn: str) -> Optional[BorrowRecord]:
        row = database.query_one(
            "SELECT * FROM borrow_records WHERE student_username = ? AND book_isbn = ? "
            "AND returned = 0 ORDER BY id LIMIT 1", (username, isbn))
        return _row_to_record(row) if row else None

    def return_book(self, username: str, isbn: str) -> bool:
        return database.execute(
            "UPDATE borrow_records SET returned = 1, return_date = ? "
            "WHERE student_username = ? AND book_isbn = ? AND picked_up = 1 AND returned = 0",
            (date.today().isoformat(), username, isbn)) > 0

    def not_returned(self) -> List[BorrowRecord]:
        return [_row_to_record(row) for row in database.query_all(
            "SELECT * FROM borrow_records WHERE returned = 0 ORDER BY id")]

    def for_student(self, username: str) -> List[BorrowRecord]:
        return [_row_to_record(row) for row in database.query_all(
            "SELECT * FROM borrow_records WHERE student_username = ? ORDER BY id",
            (username,))]

    def total_count(self) -> int:
        return database.query_one("SELECT COUNT(*) AS total FROM borrow_records")["total"]

    def borrowed_out_count(self) -> int:
        return database.query_one(
            "SELECT COUNT(*) AS total FROM borrow_records "
            "WHERE picked_up = 1 AND returned = 0")["total"]


class EmployeePerformanceManager:
    FILE = "employee_performance.json"

    def __init__(self):
        self.records: dict = {u: EmployeePerformance(**d)
                              for u, d in _load_json(self.FILE, {}).items()}

    def _save(self) -> None:
        _save_json(self.FILE, {u: asdict(p) for u, p in self.records.items()})

    def _ensure(self, username: str) -> EmployeePerformance:
        if username not in self.records:
            self.records[username] = EmployeePerformance(username)
        return self.records[username]

    def record_book_added(self, username): self._ensure(username).books_added += 1; self._save()
    def record_book_edited(self, username): self._ensure(username).books_edited += 1; self._save()
    def record_book_lent(self, username): self._ensure(username).books_borrowed += 1; self._save()
    def record_book_received(self, username): self._ensure(username).books_returned += 1; self._save()
    def record_request_approved(self, username): self._ensure(username).borrow_requests_approved += 1; self._save()
    def record_request_rejected(self, username): self._ensure(username).borrow_requests_rejected += 1; self._save()

    def display(self, username: str) -> None:
        self._ensure(username).display()

    def display_all(self) -> None:
        print("\n=== All Employees Performance Report ===")
        if not self.records:
            print("No performance records found.")
            return
        for performance in self.records.values():
            print(performance)


# ---------------------------------------------------------------------------
# Statistics and reports
# ---------------------------------------------------------------------------

class StatisticsManager:
    def __init__(self, students: StudentManager, books: BookManager, borrows: BorrowManager):
        self.students = students
        self.books = books
        self.borrows = borrows

    def display(self) -> None:
        print("\n=== Library Statistics ===")
        print(f"Registered Students: {self.students.count()}")
        print(f"Total Books: {self.books.total_count()}")
        print(f"Total Borrows: {self.borrows.total_count()}")
        print(f"Books Currently Borrowed: {self.borrows.borrowed_out_count()}")


class StudentBorrowReport:
    def __init__(self, username: str, records: List[BorrowRecord]):
        self.username = username
        self.history = records
        self.total = len(records)
        self.not_delivered = sum(1 for r in records if not r.picked_up and not r.returned)
        self.active = sum(1 for r in records if r.picked_up and not r.returned)
        self.currently_overdue = sum(1 for r in records if r.is_overdue())
        self.returned_late = sum(1 for r in records if r.is_returned_late())
        self.returned = sum(1 for r in records if r.returned)

    def display(self) -> None:
        print("\n=== Student Borrow Report ===")
        print(f"Student Username: {self.username}")
        print(f"Total Borrows: {self.total}")
        print(f"Not Delivered Yet (Ready for Pickup): {self.not_delivered}")
        print(f"Active Borrows: {self.active}")
        print(f"Currently Overdue: {self.currently_overdue}")
        print(f"Returned Late: {self.returned_late}")
        print(f"Returned: {self.returned}")

        print("\n--- Borrow History ---")
        if not self.history:
            print("No borrow history found.")
        else:
            for record in self.history:
                print(record)

        rate = (self.returned * 100.0 / self.total) if self.total else 0
        print(f"\nReturn Rate: {rate:.1f}%")
        if self.currently_overdue > 0:
            print(f"This student has {self.currently_overdue} currently overdue book(s)!")
        if self.returned_late > 0:
            print(f"This student has returned {self.returned_late} book(s) late!")


class StudentStatistics:
    def __init__(self, students: List[Student], records: List[BorrowRecord]):
        self.total_students = len(students)
        self.active_students = sum(1 for s in students if s.is_active)
        self.inactive_students = self.total_students - self.active_students
        self.total_borrows = len(records)
        self.undelivered = sum(1 for r in records if not r.picked_up and not r.returned)
        self.late_borrows = sum(1 for r in records if r.is_late())
        self.students_with_borrows = sum(
            1 for s in students if any(r.student_username == s.username for r in records))
        self.students_with_late = sum(
            1 for s in students if any(r.student_username == s.username and r.is_late()
                                       for r in records))
        self.average = (self.total_borrows / self.students_with_borrows
                        if self.students_with_borrows else 0.0)

    def display(self) -> None:
        print("\n=== Student Statistics ===")
        print(f"Total Students: {self.total_students}")
        print(f"Active Students: {self.active_students}")
        print(f"Inactive Students: {self.inactive_students}")
        print("\n--- Borrow Statistics (All Students) ---")
        print(f"Total Borrows: {self.total_borrows}")
        print(f"Undelivered Books (Ready for Pickup): {self.undelivered}")
        print(f"Late Borrows: {self.late_borrows}")
        print(f"Students with Borrows: {self.students_with_borrows}")
        print(f"Students with Late Books: {self.students_with_late}")
        print(f"Average Borrows per Student: {self.average:.2f}")


class BorrowStatistics:
    def __init__(self, records: List[BorrowRecord], requests: List[BorrowRequest]):
        self.total_requests = len(requests)
        self.approved = sum(1 for r in requests if r.is_approved())
        self.rejected = sum(1 for r in requests if r.is_rejected())
        self.pending = sum(1 for r in requests if r.is_pending())
        self.total_borrows = len(records)
        self.active = sum(1 for r in records if r.picked_up and not r.returned)
        self.completed = sum(1 for r in records if r.returned)
        self.overdue = sum(1 for r in records if r.is_overdue())
        durations = [(date.fromisoformat(r.return_date) - date.fromisoformat(r.actual_pickup_date)).days
                     for r in records
                     if r.returned and r.actual_pickup_date and r.return_date]
        self.average_days = (sum(durations) / len(durations)) if durations else 0.0

    def display(self) -> None:
        print("\n=== Borrow Statistics ===")
        print("=== Borrow Requests ===")
        print(f"Total Borrow Requests: {self.total_requests}")
        print(f"Approved Requests: {self.approved}")
        print(f"Rejected Requests: {self.rejected}")
        print(f"Pending Requests: {self.pending}")
        print("\n=== Borrow Operations ===")
        print(f"Total Borrows: {self.total_borrows}")
        print(f"Active Borrows: {self.active}")
        print(f"Completed Borrows: {self.completed}")
        print(f"Overdue Borrows: {self.overdue}")
        print(f"Average Borrow Duration: {self.average_days:.2f} days")
        if self.total_requests:
            print(f"\nApproval Rate: {self.approved * 100.0 / self.total_requests:.1f}%")
            print(f"Rejection Rate: {self.rejected * 100.0 / self.total_requests:.1f}%")
        if self.total_borrows:
            print(f"Completion Rate: {self.completed * 100.0 / self.total_borrows:.1f}%")
            print(f"Overdue Rate: {self.overdue * 100.0 / self.total_borrows:.1f}%")


# ---------------------------------------------------------------------------
# Application facade
# ---------------------------------------------------------------------------

class LibrarySystem:
    def __init__(self):
        database.init_db()
        self.students = StudentManager()
        self.books = BookManager()
        self.borrows = BorrowManager()
        self.requests = BorrowRequestManager()
        self.employees = EmployeeManager()
        self.performance = EmployeePerformanceManager()
        self.statistics = StatisticsManager(self.students, self.books, self.borrows)
        self.current_employee: Optional[Employee] = None

    # -- guards ------------------------------------------------------------

    def _employee_logged_in(self) -> bool:
        if self.current_employee is None:
            print("Only employees can perform this action.")
            return False
        return True

    def _manager_logged_in(self) -> bool:
        if self.current_employee is None or not self.current_employee.is_manager():
            print("Only managers can perform this action.")
            return False
        return True

    # -- performance recording ----------------------------------------------

    def _record(self, counter: str) -> None:
        if self.current_employee is not None:
            getattr(self.performance, counter)(self.current_employee.username)

    # -- guest --------------------------------------------------------------

    def display_statistics(self) -> None:
        self.statistics.display()

    def search_books_by_title(self) -> None:
        print("\n--- Search Books by Title ---")
        title = input("Enter book title to search: ").strip()
        if not title:
            print("Please enter a title to search.")
            return
        BookManager.display_search_results(self.books.search_by_title(title),
                                           include_status=False)

    # -- student -------------------------------------------------------------

    def register_student(self, name, student_id, username, password) -> None:
        self.students.register(name, student_id, username, password)

    def authenticate_student(self, username, password) -> Optional[Student]:
        return self.students.authenticate(username, password)

    def search_books_for_student(self) -> None:
        print("\n--- Book Search ---")
        title = input("Title (press Enter to skip): ").strip()
        author = input("Author (press Enter to skip): ").strip()
        year = read_optional_year()
        BookManager.display_search_results(
            self.books.search_books(title=title, author=author, year=year),
            include_status=True)

    def display_available_books(self) -> None:
        self.books.display_available()

    def request_borrow_book(self, student: Student) -> None:
        print("\n--- Request to Borrow a Book ---")
        self.books.display_available()

        isbn = input("Enter ISBN of the book you want to borrow: ").strip()
        book = self.books.find(isbn)
        if book is None:
            print("Invalid ISBN.")
            return
        if not book.available:
            print("Book is not available.")
            return

        start_raw = input("Enter desired start date (YYYY-MM-DD) or press Enter for today: ").strip()
        start = date.today()
        if start_raw:
            try:
                start = date.fromisoformat(start_raw)
            except ValueError:
                print("Invalid date format. Please use YYYY-MM-DD.")
                return
        if start < date.today():
            print("Start date cannot be in the past.")
            return

        end_raw = input("Enter desired end date (YYYY-MM-DD): ").strip()
        try:
            end = date.fromisoformat(end_raw)
        except ValueError:
            print("Invalid date format. Please use YYYY-MM-DD.")
            return
        if end <= start:
            print("End date must be after the start date.")
            return

        if not student.is_active:
            print("Your account is inactive. You cannot borrow books.")
            return

        self.requests.create(student.username, isbn, start.isoformat(), end.isoformat())

    def view_my_requests(self, student: Student) -> None:
        print("\n--- My Borrow Requests ---")
        requests = self.requests.for_student(student.username)
        if not requests:
            print("You have no borrow requests.")
            return
        for request in requests:
            print(request)

    def view_my_borrows(self, student: Student) -> None:
        print("\n--- My Borrow History ---")
        records = self.borrows.for_student(student.username)
        if not records:
            print("You have no borrow history.")
            return
        for record in records:
            print(record)

    def edit_student_information(self, student: Student) -> None:
        print("\n--- Edit My Information ---")
        new_name = input(f"New name (current: {student.name}): ").strip()
        new_password = input("New password (press Enter to keep the current one): ").strip()
        if self.students.update_info(student.username, new_name, new_password):
            print("Information updated successfully.")
        else:
            print("Nothing to update.")

    # -- employee -------------------------------------------------------------

    def add_new_book(self) -> None:
        if not self._employee_logged_in():
            return
        print("\n--- Add New Book ---")
        title = input("Book title: ").strip()
        author = input("Author: ").strip()
        year = read_int("Publication year: ", 1000, 2100)
        if year is None:
            return
        isbn = input("ISBN: ").strip()
        if not isbn or len(isbn) < 10:
            print("Invalid ISBN. Please enter a valid ISBN (minimum 10 characters).")
            return
        self.books.add_new_book(Book(title, author, year, isbn))
        self._record("record_book_added")

    def edit_book(self) -> None:
        if not self._employee_logged_in():
            return
        print("\n--- Edit Book Information ---")
        isbn = input("Enter ISBN of the book to edit: ").strip()
        book = self.books.find(isbn)
        if book is None:
            print(f"Book with ISBN {isbn} not found.")
            return
        print("\nCurrent book information:")
        print(book.info(with_status=True))
        print("\nEnter new information (press Enter to keep current value):")
        new_title = input(f"New title (current: {book.title}): ").strip()
        new_author = input(f"New author (current: {book.author}): ").strip()
        year_raw = input(f"New publication year (current: {book.publication_year}): ").strip()
        new_year = None
        if year_raw:
            try:
                new_year = int(year_raw)
            except ValueError:
                print("Invalid year format.")
                return
            if not 1000 <= new_year <= 2100:
                print("Invalid year. Must be between 1000 and 2100.")
                return
        if self.books.edit_book(isbn, new_title, new_author, new_year):
            print("Book information updated successfully!")
            print(self.books.find(isbn).info(with_status=True))
            self._record("record_book_edited")
        else:
            print("Failed to update book information.")

    def search_books_advanced(self) -> None:
        if not self._employee_logged_in():
            return
        print("\n--- Advanced Book Search ---")
        title = input("Title (press Enter to skip): ").strip()
        author = input("Author (press Enter to skip): ").strip()
        year = read_optional_year()
        isbn = input("ISBN (press Enter to skip): ").strip()
        BookManager.display_search_results(
            self.books.search_books(title, author, year, isbn), include_status=True)

    def display_all_books(self) -> None:
        self.books.display_all()

    def display_pending_approvable(self) -> None:
        if not self._employee_logged_in():
            return
        self.requests.display_approvable()

    def approve_borrow_request(self) -> None:
        if not self._employee_logged_in():
            return
        print("\n--- Approve Borrow Request ---")
        self.requests.display_approvable()

        username = input("\nEnter student username to approve: ").strip()
        isbn = input("Enter book ISBN to approve: ").strip()

        student = self.students.find(username)
        if student is None:
            print("Student not found.")
            return
        if not student.is_active:
            print("Student is inactive and cannot borrow books.")
            return

        request = self.requests.find_pending(username, isbn)
        if request is None:
            print("No pending borrow request found for this student and book.")
            return
        if not request.is_approvable_date():
            print("Only requests whose start date is today or yesterday can be approved.")
            return

        book = self.books.find(isbn)
        if book is None:
            print("Invalid ISBN.")
            return
        if not book.available:
            print("Book is not available.")
            return

        self.requests.approve(request)
        self.books.set_available(isbn, False)
        self.borrows.create_borrow(username, isbn,
                                   request.desired_start, request.desired_end)
        self._record("record_request_approved")
        print("Borrow request approved. The book can now be lent to the student.")

    def reject_borrow_request(self) -> None:
        if not self._employee_logged_in():
            return
        print("\n--- Reject Borrow Request ---")
        self.requests.display_pending()

        username = input("\nEnter student username to reject: ").strip()
        isbn = input("Enter book ISBN to reject: ").strip()
        reason = input("Enter rejection reason: ").strip()

        request = self.requests.find_pending(username, isbn)
        if request is None:
            print("No pending borrow request found for this student and book.")
            return
        self.requests.reject(request, reason)
        self._record("record_request_rejected")
        print("Borrow request rejected successfully!")

    def lend_book_to_student(self) -> None:
        if not self._employee_logged_in():
            return
        print("\n--- Lend Book to Student ---")
        self.borrows.display_ready_for_pickup()

        username = input("Enter student username: ").strip()
        isbn = input("Enter book ISBN: ").strip()

        if self.borrows.mark_picked_up(username, isbn):
            self._record("record_book_lent")
            print("Book handed to the student. Pickup time recorded.")
            record = self.borrows.find_active(username, isbn)
            if record is not None:
                print(f"Due date: {record.due_date}")
        else:
            print("No approved book ready for pickup found for this student and ISBN.")

    def receive_returned_book(self) -> None:
        if not self._employee_logged_in():
            return
        print("\n--- Receive Returned Book ---")
        borrowed = self.borrows.not_returned()
        if not borrowed:
            print("No books are currently borrowed.")
            return
        print("\nCurrently borrowed books:")
        for record in borrowed:
            print(record)

        username = input("Enter student username: ").strip()
        isbn = input("Enter book ISBN: ").strip()

        if self.borrows.return_book(username, isbn):
            self.books.set_available(isbn, True)
            self._record("record_book_received")
            print("Book received back. Return time recorded.")
        else:
            print("No borrowed book found for this student and ISBN "
                  "(or it was never picked up).")

    def view_student_borrow_report(self) -> None:
        if not self._employee_logged_in():
            return
        print("\n=== Student Borrow Report ===")
        username = input("Enter student username: ").strip()
        student = self.students.find(username)
        if student is None:
            print("Student not found.")
            return
        StudentBorrowReport(username, self.borrows.for_student(username)).display()

    def toggle_student_activation(self) -> None:
        if not self._employee_logged_in():
            return
        print("\n--- Activate/Deactivate Student ---")
        username = input("Enter student username: ").strip()
        student = self.students.find(username)
        if student is None:
            print("Student not found.")
            return
        if self.students.toggle_status(username):
            state = "activated" if student.is_active else "deactivated"
            print(f"Student status updated successfully. Student is now {state}.")
        else:
            print("Failed to update student status.")

    # -- manager ---------------------------------------------------------------

    def register_employee(self, name, employee_id, username, password, role) -> None:
        if not self._manager_logged_in():
            return
        self.employees.register(name, employee_id, username, password, role)

    def display_all_employees(self) -> None:
        if not self._manager_logged_in():
            return
        self.employees.display_all()

    def display_all_employees_performance(self) -> None:
        if not self._manager_logged_in():
            return
        self.performance.display_all()

    def view_borrow_statistics(self) -> None:
        if not self._manager_logged_in():
            return
        BorrowStatistics(self.borrows.all_records(),
                         self.requests.all_requests()).display()

    def display_student_statistics_and_top_late(self) -> None:
        if not self._manager_logged_in():
            return
        records = self.borrows.all_records()
        StudentStatistics(self.students.all(), records).display()
        self.students.display_students_with_most_late(10, records)

    def display_my_performance(self) -> None:
        if not self._employee_logged_in():
            return
        self.performance.display(self.current_employee.username)

    # -- authentication ----------------------------------------------------------

    def authenticate_employee(self, username, password) -> Optional[Employee]:
        return self.employees.authenticate(username, password)

    def change_employee_password(self, username, current_password, new_password) -> bool:
        return self.employees.change_password(username, current_password, new_password)


# ---------------------------------------------------------------------------
# Input helpers
# ---------------------------------------------------------------------------

def read_line(prompt: str) -> str:
    return input(prompt).strip()


def read_int(prompt: str, minimum: int, maximum: int) -> Optional[int]:
    while True:
        raw = input(prompt).strip()
        try:
            value = int(raw)
        except ValueError:
            print("Invalid input. Please enter a number: ", end="")
            continue
        if minimum <= value <= maximum:
            return value
        print(f"Please enter a number between {minimum} and {maximum}: ", end="")


def read_optional_year(prompt: str = "Publication year (press Enter to skip): ") -> Optional[int]:
    """Returns the year, or None when empty / invalid (the filter is then skipped)."""
    raw = input(prompt).strip()
    if not raw:
        return None
    try:
        return int(raw)
    except ValueError:
        print("Invalid year format, ignoring the year filter.")
        return None


# ---------------------------------------------------------------------------
# Menus
# ---------------------------------------------------------------------------

class MenuHandler:
    def __init__(self, system: LibrarySystem):
        self.system = system
        self.current_student: Optional[Student] = None
        self.current_employee: Optional[Employee] = None

    # -- helpers -------------------------------------------------------------

    @staticmethod
    def _print_options(options: List[Tuple[str, Callable]]) -> None:
        for i, (label, _) in enumerate(options, start=1):
            print(f"{i}. {label}")
        print("Please enter your choice: ", end="")

    @staticmethod
    def _read_choice(count: int) -> int:
        while True:
            raw = input().strip()
            try:
                value = int(raw)
            except ValueError:
                print("Invalid input. Please enter a number: ", end="")
                continue
            if 1 <= value <= count:
                return value
            print(f"Please enter a number between 1 and {count}: ", end="")

    @staticmethod
    def _run_menu(title: str, options: List[Tuple[str, Callable]],
                  active_predicate: Callable[[], bool]) -> None:
        while active_predicate():
            print(f"\n=== {title} ===")
            MenuHandler._print_options(options)
            choice = MenuHandler._read_choice(len(options))
            options[choice - 1][1]()
            print("___________________________")

    # -- main menu --------------------------------------------------------------

    def run(self) -> None:
        options = [
            ("Student Login", self.student_login),
            ("Employee Login", self.employee_login),
            ("View Library Statistics", self.system.display_statistics),
            ("Search Books by Title", self.system.search_books_by_title),
            ("Student Registration", self.student_registration),
            ("Exit", lambda: print("Exiting system. Goodbye!")),
        ]
        while True:
            print("\n=== University Library Management System ===")
            self._print_options(options)
            choice = self._read_choice(len(options))
            options[choice - 1][1]()
            if choice == len(options):
                return
            print("___________________________")

    def student_registration(self) -> None:
        print("\n--- New Student Registration ---")
        name = read_line("Student name: ").strip()
        student_id = read_line("Student ID: ").strip()
        username = read_line("Username: ").strip()
        password = read_line("Password: ").strip()
        self.system.register_student(name, student_id, username, password)

    def student_login(self) -> None:
        print("\n--- Student Login ---")
        username = read_line("Username: ").strip()
        password = read_line("Password: ").strip()
        self.current_student = self.system.authenticate_student(username, password)
        if self.current_student is not None:
            print(f"Login successful! Welcome, {self.current_student.name}")
            self.student_dashboard()
        else:
            print("Invalid username or password. Please try again.")

    def employee_login(self) -> None:
        print("\n--- Employee Login ---")
        username = read_line("Username: ").strip()
        password = read_line("Password: ").strip()
        self.current_employee = self.system.authenticate_employee(username, password)
        if self.current_employee is not None:
            print(f"Login successful! Welcome, {self.current_employee.name}")
            self.system.current_employee = self.current_employee
            self.employee_dashboard()
        else:
            print("Invalid username or password. Please try again.")

    # -- student dashboard --------------------------------------------------------

    def student_dashboard(self) -> None:
        system = self.system

        def logout():
            self.current_student = None

        options = [
            ("View My Information",
             lambda: (print("\n--- My Information ---"), print(self.current_student))),
            ("Edit My Information",
             lambda: system.edit_student_information(self.current_student)),
            ("Search Books", system.search_books_for_student),
            ("View Available Books", system.display_available_books),
            ("Request to Borrow a Book",
             lambda: system.request_borrow_book(self.current_student)),
            ("View My Borrow Requests",
             lambda: system.view_my_requests(self.current_student)),
            ("View My Borrow History",
             lambda: system.view_my_borrows(self.current_student)),
            ("Logout", logout),
        ]
        self._run_menu("Student Dashboard", options,
                       lambda: self.current_student is not None)
        print("Logged out successfully.")

    # -- employee dashboard ----------------------------------------------------------

    def employee_dashboard(self) -> None:
        system = self.system

        def logout():
            self.current_employee = None
            system.current_employee = None

        def change_password():
            print("\n--- Change Password ---")
            current_password = read_line("Current password: ").strip()
            new_password = read_line("New password: ").strip()
            confirm = read_line("Confirm new password: ").strip()
            if new_password != confirm:
                print("New passwords do not match.")
                return
            if system.change_employee_password(self.current_employee.username,
                                               current_password, new_password):
                print("Password changed successfully!")
            else:
                print("Failed to change password. Please check your current password.")

        def register_employee():
            print("\n--- New Employee Registration ---")
            name = read_line("Employee name: ").strip()
            employee_id = read_line("Employee ID: ").strip()
            username = read_line("Username: ").strip()
            password = read_line("Password: ").strip()
            role = read_line("Role (manager/staff): ").strip()
            if role.lower() not in ("manager", "staff"):
                print("Invalid role. Please enter 'manager' or 'staff'.")
                return
            system.register_employee(name, employee_id, username, password,
                                     role.lower())

        options = [
            ("Add New Book", system.add_new_book),
            ("Search Books", system.search_books_advanced),
            ("View Available Books", system.display_available_books),
            ("View All Books", system.display_all_books),
            ("Edit Book Information", system.edit_book),
            ("View Pending Borrow Requests (Start Date: Today/Yesterday)",
             system.display_pending_approvable),
            ("Approve Borrow Request", system.approve_borrow_request),
            ("Reject Borrow Request", system.reject_borrow_request),
            ("Lend Book to Student (Record Pickup)", system.lend_book_to_student),
            ("Receive Returned Book (Record Return Time)", system.receive_returned_book),
            ("Student Borrow Report", system.view_student_borrow_report),
            ("Activate/Deactivate Student", system.toggle_student_activation),
            ("View My Performance", system.display_my_performance),
            ("Change Password", change_password),
        ]
        if self.current_employee.is_manager():
            options += [
                ("Register New Employee", register_employee),
                ("View All Employees", system.display_all_employees),
                ("View All Employees Performance", system.display_all_employees_performance),
                ("Borrow Statistics", system.view_borrow_statistics),
                ("Student Statistics and Top 10 Late Students",
                 system.display_student_statistics_and_top_late),
            ]
        options.append(("Logout", logout))

        role = " (Manager)" if self.current_employee.is_manager() else ""
        self._run_menu(f"Employee Dashboard{role}", options,
                       lambda: self.current_employee is not None)
        print("Logged out successfully.")


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    try:
        MenuHandler(LibrarySystem()).run()
    except (KeyboardInterrupt, EOFError):
        print("\nExiting system. Goodbye!")


if __name__ == "__main__":
    main()
