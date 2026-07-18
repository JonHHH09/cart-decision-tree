document.addEventListener("htmx:afterSwap", (event) => {
    const target = event.detail.target;
    const announcement = target.querySelector("[role='alert'], .success, .prediction-card strong");
    if (announcement) {
        announcement.setAttribute("tabindex", "-1");
        announcement.focus({ preventScroll: true });
    }
});
