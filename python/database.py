"""SQLite persistence layer for the Python implementation.

The database file (``library.db``) is created in the current working
directory on first use. Every table matches one domain model of
``library_system.py``; dates are stored as ISO-8601 strings.
"""

from __future__ import annotations

import os
import sqlite3
from contextlib import closing

DB_FILE = "library.db"

SCHEMA = """
CREATE TABLE IF NOT EXISTS students (
    username   TEXT PRIMARY KEY,
    name       TEXT NOT NULL,
    student_id TEXT NOT NULL,
    password   TEXT NOT NULL,
    is_active  INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS employees (
    username    TEXT PRIMARY KEY,
    name        TEXT NOT NULL,
    employee_id TEXT NOT NULL,
    password    TEXT NOT NULL,
    role        TEXT NOT NULL CHECK (role IN ('manager', 'staff'))
);

CREATE TABLE IF NOT EXISTS books (
    isbn             TEXT PRIMARY KEY,
    title            TEXT NOT NULL,
    author           TEXT NOT NULL,
    publication_year INTEGER NOT NULL,
    available        INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS borrow_requests (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    student_username TEXT NOT NULL,
    book_isbn        TEXT NOT NULL,
    request_date     TEXT NOT NULL,
    desired_start    TEXT NOT NULL,
    desired_end      TEXT NOT NULL,
    status           TEXT NOT NULL DEFAULT 'pending'
                     CHECK (status IN ('pending', 'approved', 'rejected')),
    rejection_reason TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS borrow_records (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    student_username   TEXT NOT NULL,
    book_isbn          TEXT NOT NULL,
    borrow_date        TEXT NOT NULL,
    due_date           TEXT NOT NULL,
    return_date        TEXT,
    actual_pickup_date TEXT,
    returned           INTEGER NOT NULL DEFAULT 0,
    picked_up          INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS employee_performance (
    employee_username        TEXT PRIMARY KEY,
    books_added              INTEGER NOT NULL DEFAULT 0,
    books_edited             INTEGER NOT NULL DEFAULT 0,
    books_borrowed           INTEGER NOT NULL DEFAULT 0,
    books_returned           INTEGER NOT NULL DEFAULT 0,
    borrow_requests_approved INTEGER NOT NULL DEFAULT 0,
    borrow_requests_rejected INTEGER NOT NULL DEFAULT 0,
    last_activity_date       TEXT NOT NULL
);
"""

DEFAULT_EMPLOYEES = [
    ("mr.shahi", "1111", "admin", "1234", "manager"),
    ("mrs.tiem", "2222", "staff", "1234", "staff"),
]


def get_connection() -> sqlite3.Connection:
    connection = sqlite3.connect(os.path.join(os.getcwd(), DB_FILE))
    connection.row_factory = sqlite3.Row
    return connection


def query_all(sql: str, params: tuple = ()) -> list:
    with closing(get_connection()) as connection:
        return connection.execute(sql, params).fetchall()


def query_one(sql: str, params: tuple = ()):
    with closing(get_connection()) as connection:
        return connection.execute(sql, params).fetchone()


def execute(sql: str, params: tuple = ()) -> int:
    """Run a single statement and return the number of affected rows."""
    with closing(get_connection()) as connection:
        cursor = connection.execute(sql, params)
        connection.commit()
        return cursor.rowcount


def init_db() -> None:
    """Create the schema (if needed) and seed the default employees."""
    with closing(get_connection()) as connection:
        connection.executescript(SCHEMA)
        connection.executemany(
            "INSERT OR IGNORE INTO employees (name, employee_id, username, password, role) "
            "VALUES (?, ?, ?, ?, ?)",
            DEFAULT_EMPLOYEES,
        )
        connection.commit()
