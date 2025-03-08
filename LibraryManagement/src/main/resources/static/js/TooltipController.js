class TooltipController {
	static showTooltip(event, bookItem) {
	            let infoBox = bookItem.querySelector(".book-info");
	            infoBox.style.display = "block";
	         // Lấy kích thước của tooltip
	            let tooltipWidth = infoBox.offsetWidth;
	            let tooltipHeight = infoBox.offsetHeight;

	         // Tính toán vị trí tooltip dựa trên vị trí chuột so với phần tử cha
	            let posX = event.clientX - bookItem.getBoundingClientRect().left + 10;
	            let posY = event.clientY - bookItem.getBoundingClientRect().top + 10;

	            // Đảm bảo tooltip không bị tràn khỏi màn hình
	            if (posX + tooltipWidth > window.innerWidth) {
	                posX -= tooltipWidth + 20;
	            }
	            if (posY + tooltipHeight > window.innerHeight) {
	                posY -= tooltipHeight + 20;
	            }
	            infoBox.style.left = posX + "px";
	            infoBox.style.top = posY + "px";
	        }

	        static hideTooltip(bookItem) {
	            let infoBox = bookItem.querySelector(".book-info");
	            infoBox.style.display = "none";
	        }
}

// Gắn sự kiện cho tất cả các phần tử có class "book-item"
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".book-item").forEach(bookItem => {
        bookItem.addEventListener("mousemove", (event) => TooltipController.showTooltip(event, bookItem));
        bookItem.addEventListener("mouseleave", () => TooltipController.hideTooltip(bookItem));
    });
});
