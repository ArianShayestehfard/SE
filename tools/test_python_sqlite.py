#!/usr/bin/env python3
"""End-to-end check of the SQLite-backed Python implementation.

Exercises the full borrow workflow through the manager API (no console
input) against a throwaway database in a temporary directory:

    python tools/test_python_sqlite.py

Prints one line per check and exits non-zero on the first failure.
"""

import os
import sys
import tempfile
from datetime import date, timedelta

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(REPO_ROOT, "python"))
os.chdir(tempfile.mkdtemp(prefix="se-sqlite-test-"))

import database  # noqa: E402  (needs sys.path/cwd set up first)
import library_system as lib  # noqa: E402

CHECKS = 0


def check(condition, message):
    global CHECKS
    if not condition:
        raise AssertionError(f"FAILED: {message}")
    CHECKS += 1
    print(f"ok {CHECKS} - {message}")


system = lib.LibrarySystem()

# -- registration, authentication and seeding ---------------------------------
system.register_student("Ali Reza", "401234", "ali", "1234")
system.register_student("Ali Again", "999999", "ali", "xyz")
check(system.students.count() == 1, "duplicate username is rejected")

student = system.students.authenticate("ali", "1234")
check(student is not None and student.name == "Ali Reza", "student can log in")
check(system.employees.find("admin").is_manager(), "default manager account is seeded")
check(system.employees.authenticate("staff", "1234") is not None,
      "default staff account is seeded")

# -- books ---------------------------------------------------------------------
system.books.add_new_book(lib.Book("Java Programming", "Ali Alavi", 2020, "1234567890"))
system.books.add_new_book(lib.Book("Data Structures", "Sara Saraei", 2019, "2222222222"))
check(system.books.total_count() == 2, "two books are stored")
check(system.books.find("1234567890").available, "new book is available")

results = system.books.search_books(title="java", year=2020)
check(len(results) == 1 and results[0].isbn == "1234567890",
      "combined search by title and year works")

# -- borrow workflow ------------------------------------------------------------
today = date.today()
system.requests.create("ali", "1234567890", today.isoformat(),
                       (today + timedelta(days=7)).isoformat())
check(len(system.requests.pending()) == 1, "request is stored as pending")
check(system.requests.pending_approvable(), "request for today is approvable")

future_request = lib.BorrowRequest("ali", "2222222222", today.isoformat(),
                                   (today + timedelta(days=2)).isoformat(),
                                   (today + timedelta(days=9)).isoformat())
check(not future_request.is_approvable_date(),
      "a request starting in the future is not approvable yet")

request = system.requests.find_pending("ali", "1234567890")
system.requests.approve(request)
system.books.set_available("1234567890", False)
system.borrows.create_borrow("ali", "1234567890",
                             request.desired_start, request.desired_end)
check(system.requests.find_pending("ali", "1234567890") is None,
      "approved request leaves the pending state")

check(system.borrows.mark_picked_up("ali", "1234567890"), "book can be picked up")
check(not system.borrows.mark_picked_up("ali", "1234567890"),
      "double pickup is rejected")
check(system.borrows.return_book("ali", "1234567890"), "book can be returned")
check(not system.borrows.return_book("ali", "1234567890"),
      "double return is rejected")
check(system.books.set_available("1234567890", True),
      "receive flow restores availability (as LibrarySystem.receive_returned_book does)")
check(system.books.find("1234567890").available, "returned book is available again")

report = lib.StudentBorrowReport("ali", system.borrows.for_student("ali"))
check(report.total == 1 and report.returned == 1 and report.returned_late == 0,
      "student report reflects the returned loan")

# -- persistence across instances --------------------------------------------------
reopened = lib.LibrarySystem()
check(reopened.students.count() == 1 and reopened.books.total_count() == 2,
      "data survives a fresh LibrarySystem instance (SQLite file)")
check(reopened.borrows.total_count() == 1, "borrow record is persisted")

# -- activation and performance -----------------------------------------------------
reopened.students.toggle_status("ali")
check(reopened.students.authenticate("ali", "1234") is None,
      "deactivated student cannot log in")
reopened.students.toggle_status("ali")

reopened.current_employee = reopened.employees.find("admin")
reopened.performance.record_book_added("admin")
reopened.performance.record_request_approved("admin")
row = database.query_one(
    "SELECT * FROM employee_performance WHERE employee_username = 'admin'")
check(row["books_added"] == 1 and row["borrow_requests_approved"] == 1,
      "performance counters are stored in SQLite")

print(f"\nall {CHECKS} checks passed (db: {os.path.join(os.getcwd(), database.DB_FILE)})")
