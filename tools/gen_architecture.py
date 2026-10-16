#!/usr/bin/env python3
"""Convert the SE architecture scene into docs/architecture.excalidraw and .svg."""

import json
import os
import random

E = [
    {"id": "title", "type": "text", "x": 430, "y": 15, "width": 640, "height": 30,
     "text": "University Library Management System — Architecture"},
    {"id": "role_guest", "type": "rectangle", "x": 260, "y": 70, "width": 200, "height": 55,
     "backgroundColor": "#ffec99", "label": "Guest"},
    {"id": "role_student", "type": "rectangle", "x": 520, "y": 70, "width": 200, "height": 55,
     "backgroundColor": "#a5d8ff", "label": "Student"},
    {"id": "role_employee", "type": "rectangle", "x": 780, "y": 70, "width": 200, "height": 55,
     "backgroundColor": "#b2f2bb", "label": "Employee"},
    {"id": "role_manager", "type": "rectangle", "x": 1040, "y": 70, "width": 200, "height": 55,
     "backgroundColor": "#ffc9c9", "label": "Manager"},
    {"id": "ui", "type": "rectangle", "x": 400, "y": 195, "width": 700, "height": 70,
     "backgroundColor": "#fff3bf",
     "label": "Console UI — MenuHandler\n(dynamic menus, input reading & validation)"},
    {"id": "facade", "type": "rectangle", "x": 400, "y": 335, "width": 700, "height": 70,
     "backgroundColor": "#b2f2bb",
     "label": "LibrarySystem — Application Facade\n(role guards, borrow workflow orchestration, persistence calls)"},
    {"id": "stats", "type": "rectangle", "x": 1180, "y": 305, "width": 300, "height": 130,
     "backgroundColor": "#ffd8a8",
     "label": "Statistics & Reports\nStatisticsManager\nBorrowStatistics\nStudentStatistics\nStudentBorrowReport"},
    {"id": "mgr_container", "type": "rectangle", "x": 200, "y": 470, "width": 1100, "height": 195,
     "backgroundColor": "#f1f3f5", "strokeColor": "#495057"},
    {"id": "mgr_container_title", "type": "text", "x": 550, "y": 482, "width": 400, "height": 24,
     "text": "Domain Managers"},
    {"id": "mgr_student", "type": "rectangle", "x": 235, "y": 515, "width": 320, "height": 50,
     "backgroundColor": "#d0bfff", "label": "StudentManager"},
    {"id": "mgr_book", "type": "rectangle", "x": 590, "y": 515, "width": 320, "height": 50,
     "backgroundColor": "#d0bfff", "label": "BookManager"},
    {"id": "mgr_borrow", "type": "rectangle", "x": 945, "y": 515, "width": 320, "height": 50,
     "backgroundColor": "#d0bfff", "label": "BorrowManager"},
    {"id": "mgr_request", "type": "rectangle", "x": 235, "y": 585, "width": 320, "height": 50,
     "backgroundColor": "#d0bfff", "label": "BorrowRequestManager"},
    {"id": "mgr_employee", "type": "rectangle", "x": 590, "y": 585, "width": 320, "height": 50,
     "backgroundColor": "#d0bfff", "label": "EmployeeManager"},
    {"id": "mgr_performance", "type": "rectangle", "x": 945, "y": 585, "width": 320, "height": 50,
     "backgroundColor": "#d0bfff", "label": "EmployeePerformanceManager"},
    {"id": "persistence", "type": "rectangle", "x": 200, "y": 730, "width": 1100, "height": 90,
     "backgroundColor": "#ced4da",
     "label": "Persistence Layer\nJava: *FileManager → .dat (serialization)   |   Python: JSON files"},
    {"id": "a_guest", "type": "arrow", "x": 360, "y": 125, "points": [[0, 0], [140, 70]]},
    {"id": "a_student", "type": "arrow", "x": 620, "y": 125, "points": [[0, 0], [0, 70]]},
    {"id": "a_employee", "type": "arrow", "x": 880, "y": 125, "points": [[0, 0], [-10, 70]]},
    {"id": "a_manager", "type": "arrow", "x": 1140, "y": 125, "points": [[0, 0], [-140, 70]]},
    {"id": "a_ui_facade", "type": "arrow", "x": 750, "y": 265, "points": [[0, 0], [0, 70]]},
    {"id": "a_facade_stats", "type": "arrow", "x": 1100, "y": 370, "points": [[0, 0], [80, 0]]},
    {"id": "a_facade_mgr", "type": "arrow", "x": 750, "y": 405, "points": [[0, 0], [0, 65]]},
    {"id": "a_mgr_stats", "type": "arrow", "x": 1300, "y": 560, "points": [[0, 0], [30, -120]]},
    {"id": "a_mgr_persist", "type": "arrow", "x": 750, "y": 665, "points": [[0, 0], [0, 65]]},
]

STROKE = "#343a40"
LINE_HEIGHT = 25
FONT_SIZE = 20


def base_element(el, version):
    now = int(1697000000 * 1000)
    out = {
        "id": el["id"], "type": el["type"], "x": el["x"], "y": el["y"],
        "width": el.get("width", 0), "height": el.get("height", 0),
        "angle": 0, "strokeColor": el.get("strokeColor", STROKE),
        "backgroundColor": el.get("backgroundColor", "transparent"),
        "fillStyle": "solid", "strokeWidth": 2, "strokeStyle": "solid",
        "roughness": 1, "opacity": 100, "groupIds": [], "frameId": None,
        "roundness": None, "seed": random.randint(1, 2**31),
        "version": version, "versionNonce": random.randint(1, 2**31),
        "isDeleted": False, "boundElements": None, "updated": now,
        "link": None, "locked": False,
    }
    return out


def build_excalidraw():
    elements, version = [], 1
    for el in E:
        item = base_element(el, version)
        version += 1
        if el["type"] == "rectangle" and "label" in el:
            label_id = el["id"] + "-label"
            lines = el["label"].split("\n")
            item["boundElements"] = [{"id": label_id, "type": "text"}]
            item["roundness"] = {"type": 3}
            label = base_element({"id": label_id, "type": "text",
                                  "x": el["x"] + 8, "y": el["y"] + el["height"] / 2 - LINE_HEIGHT * len(lines) / 2,
                                  "width": el["width"] - 16,
                                  "height": LINE_HEIGHT * len(lines)}, version)
            version += 1
            label.update({
                "text": el["label"], "fontSize": FONT_SIZE, "fontFamily": 3,
                "textAlign": "center", "verticalAlign": "middle",
                "containerId": el["id"], "originalText": el["label"],
                "lineHeight": 1.25, "baseline": 17,
            })
            elements += [item, label]
        elif el["type"] == "text":
            item.update({
                "text": el["text"], "fontSize": FONT_SIZE, "fontFamily": 3,
                "textAlign": "center", "verticalAlign": "top", "containerId": None,
                "originalText": el["text"], "lineHeight": 1.25, "baseline": 17,
            })
            elements.append(item)
        elif el["type"] == "arrow":
            item["points"] = el["points"]
            item["lastCommittedPoint"] = None
            item["startBinding"] = None
            item["endBinding"] = None
            item["startArrowhead"] = None
            item["endArrowhead"] = "arrow"
            elements.append(item)
        else:
            item["roundness"] = {"type": 3}
            elements.append(item)

    return {
        "type": "excalidraw", "version": 2, "source": "https://excalidraw.com",
        "elements": elements, "appState": {"gridSize": None, "viewBackgroundColor": "#ffffff"},
        "files": {},
    }


def esc(s):
    return (s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))


def build_svg():
    parts = ['<?xml version="1.0" encoding="UTF-8"?>',
             '<svg xmlns="http://www.w3.org/2000/svg" width="1076" height="660" '
             'viewBox="145 5 1345 825" font-family="Segoe UI, Helvetica, Arial, sans-serif">',
             '<defs><marker id="arr" markerWidth="12" markerHeight="10" refX="9" refY="4" '
             'orient="auto"><path d="M0,0 L9,4 L0,8 z" fill="#343a40"/></marker></defs>',
             '<rect x="145" y="5" width="1345" height="825" fill="#ffffff"/>']
    # rects first, then arrows, then text (painting order)
    for el in E:
        if el["type"] == "rectangle":
            parts.append(f'<rect x="{el["x"]}" y="{el["y"]}" width="{el["width"]}" '
                         f'height="{el["height"]}" rx="8" fill="{el.get("backgroundColor", "none")}" '
                         f'stroke="{el.get("strokeColor", STROKE)}" stroke-width="2"/>')
    for el in E:
        if el["type"] == "arrow":
            x1, y1 = el["x"], el["y"]
            x2, y2 = el["x"] + el["points"][1][0], el["y"] + el["points"][1][1]
            parts.append(f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" '
                         f'stroke="{STROKE}" stroke-width="2" marker-end="url(#arr)"/>')
    for el in E:
        if el["type"] == "text":
            cx = el["x"] + el["width"] / 2
            lines = el["text"].split("\n")
            for i, line in enumerate(lines):
                parts.append(f'<text x="{cx}" y="{el["y"] + 18 + LINE_HEIGHT * i}" '
                             f'text-anchor="middle" font-size="{FONT_SIZE}" '
                             f'fill="#1e1e1e">{esc(line)}</text>')
        elif el["type"] == "rectangle" and "label" in el:
            lines = el["label"].split("\n")
            top = el["y"] + el["height"] / 2 - LINE_HEIGHT * len(lines) / 2
            cx = el["x"] + el["width"] / 2
            for i, line in enumerate(lines):
                parts.append(f'<text x="{cx}" y="{top + 17 + LINE_HEIGHT * i}" '
                             f'text-anchor="middle" font-size="{FONT_SIZE}" '
                             f'fill="#1e1e1e">{esc(line)}</text>')
    parts.append("</svg>")
    return "\n".join(parts)


os.makedirs("docs", exist_ok=True)
with open("docs/architecture.excalidraw", "w", encoding="utf-8") as fh:
    json.dump(build_excalidraw(), fh, ensure_ascii=False, indent=1)
with open("docs/architecture.svg", "w", encoding="utf-8") as fh:
    fh.write(build_svg())
print("written:", sorted(os.listdir("docs")))
