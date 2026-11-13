import os
import time
import requests

TOKEN = os.environ.get("BALE_TOKEN")
if not TOKEN:
    raise SystemExit("متغیر محیطی BALE_TOKEN تنظیم نشده است.")

API = f"https://tapi.bale.ai/bot{TOKEN}"
REPLY_TEXT = "خوبم"


def get_updates(offset=None):
    params = {"timeout": 30}
    if offset is not None:
        params["offset"] = offset
    response = requests.get(f"{API}/getUpdates", params=params, timeout=40)
    return response.json().get("result", [])


def send_message(chat_id, text):
    requests.post(
        f"{API}/sendMessage",
        json={"chat_id": chat_id, "text": text},
        timeout=20,
    )


def main():
    offset = None
    print("بات شروع به کار کرد.")
    while True:
        try:
            updates = get_updates(offset)
        except requests.RequestException as e:
            print("خطا در دریافت پیام‌ها:", e)
            time.sleep(3)
            continue

        for update in updates:
            offset = update["update_id"] + 1
            message = update.get("message")
            if not message:
                continue
            try:
                send_message(message["chat"]["id"], REPLY_TEXT)
            except requests.RequestException as e:
                print("خطا در ارسال پیام:", e)


if __name__ == "__main__":
    main()
