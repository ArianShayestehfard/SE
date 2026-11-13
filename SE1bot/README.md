# SE1bot

بات ساده‌ی بله که به هر پیامی فقط جواب «خوبم» می‌دهد.

## نصب

```bash
pip install -r requirements.txt
```

## ساخت بات

1. در بله به @BotFather پیام بده و `/newbot` را بفرست.
2. توکن دریافتی را نگه دار و در جای عمومی منتشر نکن.

## اجرا

ویندوز (PowerShell):

```powershell
$env:BALE_TOKEN = "توکن_بات"
python bot.py
```

لینوکس و مک:

```bash
export BALE_TOKEN="توکن_بات"
python3 bot.py
```

## امنیت

توکن هرگز نباید داخل کد یا گیت قرار بگیرد. این پروژه توکن را فقط از متغیر محیطی `BALE_TOKEN` می‌خواند.
