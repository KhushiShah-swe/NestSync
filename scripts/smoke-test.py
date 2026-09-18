#!/usr/bin/env python3
"""Exercise the deployed API through the frontend proxy using disposable test records."""
import json
import os
import urllib.request
import uuid

BASE = os.environ.get("NESTSYNC_URL", "http://127.0.0.1:3000")
def request(path, body=None, token=None, method=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode() if body is not None else None,
                                 headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=20) as response:
        data = response.read()
        return json.loads(data) if data else None

def main():
    assert request("/actuator/health")["status"] == "UP"
    user = request("/api/auth/register", {"name": "Smoke test", "email": f"smoke-{uuid.uuid4().hex}@example.com", "password": uuid.uuid4().hex})
    token = user["token"]
    expense = request("/api/expenses", {"title": "Smoke test expense", "amount": "10.01", "date": "2026-09-18", "category": "Other", "splitType": "EQUAL"}, token)
    assert expense["amount"] == 10.01
    assert "password" not in expense["paidBy"]
    balances = request("/api/balances", token=token)
    assert balances[0]["netAmount"] == 0
    request(f"/api/expenses/{expense['expenseId']}", token=token, method="DELETE")
    print("Smoke test passed: health, registration, authentication, expense, balances, deletion.")

if __name__ == "__main__":
    main()
