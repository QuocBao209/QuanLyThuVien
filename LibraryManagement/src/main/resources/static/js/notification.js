document.addEventListener("DOMContentLoaded", function () {
    document.addEventListener("click", function (event) {
        var notificationContainer = document.getElementById("notification-container");
        var notificationBtn = document.querySelector(".notification-btn");

        if (!notificationContainer.contains(event.target) && !notificationBtn.contains(event.target)) {
            notificationContainer.style.display = "none";
        }
    });

    document.querySelector(".notification-btn").addEventListener("click", function (event) {
        event.stopPropagation();
        var container = document.getElementById("notification-container");
        var list = container.querySelector(".notification-list");
        container.style.display = (container.style.display === "block") ? "none" : "block";
        
        // Cuộn lên đầu nếu có thông báo chưa đọc
        if (container.style.display === "block" && list.querySelector(".unread")) {
            list.scrollTop = 0;
        }
    });
});

function toggleNotificationDropdown() {
    let dropdown = document.getElementById("notification-container");
    let list = dropdown.querySelector(".notification-list");
    dropdown.classList.toggle("hidden");
    
    // Cuộn lên đầu nếu có thông báo chưa đọc
    if (!dropdown.classList.contains("hidden") && list.querySelector(".unread")) {
        list.scrollTop = 0;
    }
}

function markAsRead(element) {
    if (element.classList.contains('unread')) {
        element.classList.remove('unread');
        element.style.opacity = "0.7";

        let form = document.getElementById("form-" + element.dataset.id);
        form.submit();

        let badge = document.querySelector('.badge');
        if (badge) {
            let count = parseInt(badge.textContent);
            if (count > 1) {
                badge.textContent = count - 1;
            } else {
                badge.remove();
            }
        }
    }
}