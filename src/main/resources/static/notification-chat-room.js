function requestNotificationPermission() {
    if (!("Notification" in window)) {
        console.log("This browser does not support notifications.");
        return;
    }

    if (Notification.permission === "default") {
        Notification.requestPermission().then(permission => {
            if (permission === "granted") {
                console.log("Notification permission granted.");
            } else {
                console.log("Notification permission denied.");
            }
        });
    }
}

function showBrowserNotification() {
    if (Notification.permission === "granted") {
        new Notification("New Chat Activity", {
            body: "You have new messages in your Chat!",
        });
    } else if (Notification.permission === "default") {
        Notification.requestPermission().then(permission => {
            if (permission === "granted") {
                new Notification("New Chat Activity", {
                    body: "You have new messages in your Chat!",
                });
            }
        });
    }
}

let unreadCount = 0;
let isBlinking = false;
let blinkCount = 0;
const maxBlinks = 2;
let originalTitle = "Chat";

function updateTitleBadge() {
    document.title = unreadCount > 0 ? `(${unreadCount}) ${originalTitle}` : originalTitle;
}

function startBlinking() {
    const blinkInterval = setInterval(() => {
        document.title = isBlinking ? originalTitle : `(${unreadCount}) New Messages!`;
        isBlinking = !isBlinking;

        blinkCount++;

        if (blinkCount >= maxBlinks) { // 2 toggles per blink
            clearInterval(blinkInterval); // Stop after 3 full blinks
            updateTitleBadge();
            blinkCount = 0;
        }
    }, 1000); // Blink every 1 second
}

window.addEventListener('focus', () => {
    unreadCount = 0; // Reset on focus
    updateTitleBadge();
});