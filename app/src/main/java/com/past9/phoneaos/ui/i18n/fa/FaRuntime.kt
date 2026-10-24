package com.past9.phoneaos.ui.i18n.fa

/**
 * The words of the running agent: status lines, notifications, the questions its tools ask,
 * and the buttons those questions offer. Last in [faParts], so it is the tie-breaker for
 * the buttons ("Done", "Not now") that more than one screen uses.
 *
 * Persian notes: ZWNJ (‌) inside compound words, Persian digits via the fa-IR format locale,
 * polite شما for the user, brand names (WhatsApp, …) and Android's own switch labels stay Latin.
 */
internal val faRuntime: Map<String, String> = mapOf(

    // ---- The agent's own name and the work it says it is doing --------------------------------

    "Agent" to "دستیار",
    "The main agent" to "دستیار اصلی",
    "You" to "شما",

    "Working" to "در حال کار",
    "Browsing" to "در حال مرور",
    "Remembering" to "در حال یادداشت",
    "Using your apps" to "با استفاده از اپلیکیشن‌های خود",
    "Helpers working" to "کمکی‌ها در حال کار",
    "Reading the web" to "خواندن وب",
    "Updating tasks" to "در حال به‌روزرسانی کارها",
    "Setting up a routine" to "تنظیم یک روتین",
    "Waiting for you" to "در انتظار شما",
    "Working on it" to "روی آن کار می‌کنند",
    "Working: %1\$d helpers" to "در حال کار: %1\$d کمکی",
    "%1\$s is working" to "%1\$s در حال کار است",
    "%1\$s is working: %2\$d helpers" to "%1\$s در حال کار است: %2\$d کمکی",
    "You can leave the app. I will let you know." to "می‌توانید برنامه را ترک کنید. من به شما اطلاع خواهم داد.",

    // ---- Notification channels and question notifications -------------------------------------

    "Messages from your agent" to "پیام‌های دستیار شما",
    "Agent working" to "دستیار در حال کار",
    "Connect %1\$s?" to "اتصال به %1\$s؟",
    "Approve?" to "تأیید می‌کنید؟",
    "%1\$s needs you" to "%1\$s به شما نیاز دارد",
    "Your agent has a question" to "دستیار شما یک سوال دارد",
    "Asked %1\$s: %2\$s" to "از %1\$s پرسیدم: %2\$s",

    // ---- What the agent says when something goes wrong -----------------------------------------

    "Your API key was rejected. Check it in Settings." to "کلید API شما رد شد. آن را در تنظیمات بررسی کنید.",
    "The AI provider says we are sending too much right now (rate limit or out of credit). Try again shortly." to
        "سرویس هوش مصنوعی می‌گوید الان درخواست‌ها زیادی زیاد است (محدودیت تعداد درخواست یا تمام‌شدن اعتبار). کمی بعد دوباره امتحان کنید.",
    "I could not reach the AI provider. Check your connection." to "من نتوانستم به سرویس‌دهنده هوش مصنوعی برسم. اتصال خود را بررسی کنید.",
    "This model can't look at images. Pick a vision model in Settings, or send it as text." to
        "این مدل نمی‌تواند تصویر ببیند. در تنظیمات یک مدل بینایی انتخاب کنید، یا آن را به‌صورت متن بفرستید.",
    "Something went wrong: %1\$s" to "مشکلی پیش آمد: %1\$s",
    "No server address is saved for this provider. Open Settings, tap Change key, pick Other and enter the address, like https://your-server/v1." to
        "برای این سرویس هیچ آدرس سروری ذخیره نشده است. در تنظیمات، Change key را بزنید، Other را انتخاب کنید و آدرس را وارد کنید؛ مثل https://your-server/v1.",
    "This server uses plain HTTP, which doesn't encrypt your requests or API key. Use an https:// address, or tick \"Allow unencrypted HTTP for this server\"." to
        "این سرور از HTTP ساده استفاده می‌کند و درخواست‌ها و کلید API شما را رمزگذاری نمی‌کند. آدرس https:// به کار ببرید، یا گزینهٔ «اجازهٔ HTTP رمزنشده برای این سرور» را فعال کنید.",
    "The base URL must start with https:// (or http:// with the opt-in)." to
        "آدرس پایه باید با https:// شروع شود (یا با http://، همراه با اجازهٔ مربوطه).",

    // ---- The questions the tools ask before they touch the phone --------------------------------

    "Can I use your location so I can %1\$s?" to "می‌توانم از مکان شما استفاده کنم تا %1\$s؟",
    "Can I read your %1\$s notifications so I can %2\$s?" to "می‌توانم اعلان‌های %1\$s شما را بخوانم تا %2\$s؟",
    "Can I look at your latest photos?" to "آیا می‌توانم به آخرین عکس‌های شما نگاه کنم؟",
    "Pick %1\$s for me?" to "%1\$s را برایم انتخاب می‌کنید؟",
    "Let me look through the files on your phone? Android will show a switch called \"Allow access to manage all files\". You can turn it off any time." to
        "اجازه می‌دهید فایل‌های گوشی‌تان را ببینم؟ اندروید کلیدی به نام \"Allow access to manage all files\" نشان می‌دهد. هر وقت بخواهید می‌توانید خاموشش کنید.",
    "%1\$s Open the browser, do it, then tap Done." to "%1\$s مرورگر را باز کنید، کار را انجام دهید و بعد «انجام شد» را بزنید.",
    "Upload a file to transcribe on %1\$s" to "آپلود فایل برای تبدیل گفتار به نوشتار روی %1\$s",

    // ---- Permission notifications the tools raise ------------------------------------------------

    "Allow location?" to "اجازهٔ مکان؟",
    "Open the app to let your agent use your location" to "برنامه را باز کنید تا دستیار شما از موقعیت مکانی شما استفاده کند",
    "Allow photos?" to "اجازهٔ عکس‌ها؟",
    "Open the app to let your agent see your photos" to "برنامه را باز کنید تا دستیار شما عکس‌های شما را ببیند",
    "Pick a file" to "یک فایل را انتخاب کنید",

    // ---- Progress lines from the tools, shown in the chat ---------------------------------------

    "Checking your location" to "در حال بررسی موقعیت مکانی شما",
    "Couldn't get a location fix" to "تصحیح مکان دریافت نشد",
    "Got your location: %1\$s" to "موقعیت شما را گرفتم: %1\$s",
    "Got your location" to "موقعیت شما را گرفتم",
    "Now reading %1\$s notifications" to "در حال خواندن اعلان‌های %1\$s",
    "Looked at %1\$s" to "نگاه کردم به %1\$s",
    "Waiting for the code from %1\$s" to "منتظر کد از %1\$s",
    "Got the code from %1\$s" to "کد از %1\$s رسید",
    "No code arrived" to "کدی نرسید",
    "Read %1\$d file you picked" to "%1\$d فایل انتخابی‌تان را خواندم",
    "Read %1\$d files you picked" to "%1\$d فایل انتخابی‌تان را خواندم",
    "Listening to %1\$s" to "در حال گوش دادن به %1\$s",
    "Sending %1\$s to %2\$s to transcribe" to "در حال فرستادن %1\$s به %2\$s برای تبدیل گفتار به نوشتار",
    "Browsing %1\$s" to "در حال مرور %1\$s",
    "Opened %1\$s" to "%1\$s باز شد",
    "Refused: %1\$s" to "رد شد: %1\$s",
    "Checking how %1\$s behaves" to "در حال بررسی رفتار %1\$s",
    "Clicked %1\$s" to "روی %1\$s کلیک کردم",
    "Uploaded %1\$s" to "%1\$s آپلود شد",
    "Ran a script on %1\$s" to "روی %1\$s اسکریپت اجرا شد",
    "Typed into the page" to "در صفحه تایپ کرد",
    "Screenshot" to "اسکرین‌شات",
    "You allowed access to your files" to "شما اجازه دسترسی به فایل‌های خود را داده اید",

    // ---- The buttons the agent's questions offer. English keys go to the model; these are shown. --

    "No" to "خیر",
    "Yes" to "بله",
    "Open browser" to "باز کردن مرورگر",
    "Done" to "انجام شد",
    "Skip" to "رد کردن",
    "Approve" to "تأیید",
    "Decline" to "رد کردن",
    "Allow" to "اجازه می‌دهم",
    "Allow this time" to "این زمان را مجاز کنید",
    "Not now" to "فعلاً نه",
    "Open settings" to "باز کردن تنظیمات",
    "Choose files" to "انتخاب فایل‌ها",
    "Connect" to "اتصال",
    "Sent" to "ارسال شد",
    "Voice calls" to "تماس‌های صوتی",
    "On a call. Tap to go back to it." to "در یک تماس. برای بازگشت به آن بزنید.",
    "Background browsing" to "مرور در پس‌زمینه",
    "%1\$s is browsing" to "%1\$s در حال مرور است",
    "Tap to watch or take over" to "برای تماشا یا تحویل گرفتن بزنید",
    "I'm browsing. Tap to watch or take over." to "دارم مرور میکنم برای تماشا یا تحویل گرفتن بزنید.",
    "Open the app to choose %1\$s" to "برای انتخاب %1\$s اپلیکیشن را باز کنید",
    "Picked up again after the app restarted" to "پس از راه اندازی مجدد برنامه، دوباره انتخاب شد",
)
