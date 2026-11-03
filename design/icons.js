const HZ_ICONS = {
  bowl: '<path d="M3.5 11h17a8.5 8.5 0 0 1-17 0z"/><path d="M8 8.2c.55-1 .55-2.1 0-3.1M12 8.2c.55-1 .55-2.1 0-3.1M16 8.2c.55-1 .55-2.1 0-3.1"/>',
  bus: '<rect x="4" y="4" width="16" height="12.5" rx="2.4"/><path d="M4 10.5h16"/><path d="M7.5 16.5v2.2M16.5 16.5v2.2"/><circle cx="8" cy="13.6" r=".9" fill="currentColor" stroke="none"/><circle cx="16" cy="13.6" r=".9" fill="currentColor" stroke="none"/>',
  bag: '<path d="M5.4 8.2h13.2L17.4 20H6.6z"/><path d="M9 8.2V6.4a3 3 0 0 1 6 0v1.8"/>',
  home: '<path d="M4 10.6 12 4l8 6.6V20H4z"/><path d="M9.8 20v-5.2h4.4V20"/>',
  ticket: '<path d="M4 8.2A2.2 2.2 0 0 1 6.2 6h11.6A2.2 2.2 0 0 1 20 8.2v1.4a2.4 2.4 0 0 0 0 4.8v1.4a2.2 2.2 0 0 1-2.2 2.2H6.2A2.2 2.2 0 0 1 4 15.8v-1.4a2.4 2.4 0 0 0 0-4.8z"/><path d="M14.5 6.4v11.2" stroke-dasharray="2 2.4"/>',
  cross: '<rect x="3.2" y="9" width="17.6" height="6" rx="3" transform="rotate(-45 12 12)"/><path d="M8.4 15.6l7.2-7.2"/>',
  book: '<path d="M5 5.4A1.9 1.9 0 0 1 6.9 3.5H19V17H6.9A1.9 1.9 0 0 0 5 18.9z"/><path d="M5 18.9A1.9 1.9 0 0 0 6.9 20.8H19V17"/>',
  gift: '<rect x="4" y="9" width="16" height="4" rx="1"/><path d="M5.5 13v7h13v-7M12 9v11"/><path d="M12 9S10.8 4.5 8.4 4.5a2.1 2.1 0 0 0 0 4.5H12zM12 9s1.2-4.5 3.6-4.5a2.1 2.1 0 0 1 0 4.5H12z"/>',
  phone: '<rect x="7" y="3" width="10" height="18" rx="2.4"/><path d="M10.5 6.2h3"/><circle cx="12" cy="17.8" r=".7" fill="currentColor" stroke="none"/>',
  dots: '<circle cx="6" cy="12" r="1.1" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1.1" fill="currentColor" stroke="none"/><circle cx="18" cy="12" r="1.1" fill="currentColor" stroke="none"/>',
  back: '<path d="M20 6H9.5L4 12l5.5 6H20a1.5 1.5 0 0 0 1.5-1.5v-9A1.5 1.5 0 0 0 20 6z"/><path d="M12.5 9.5l5 5M17.5 9.5l-5 5"/>',
  left: '<path d="M14.5 5.5 8 12l6.5 6.5"/>',
  right: '<path d="M9.5 5.5 16 12l-6.5 6.5"/>',
  down: '<path d="M12 4v11"/><path d="M7.5 10.5 12 15l4.5-4.5"/><path d="M4 17v2.2h16V17"/>',
  up: '<path d="M12 16V5"/><path d="M7.5 9.5 12 5l4.5 4.5"/><path d="M4 17v2.2h16V17"/>',
  trash: '<path d="M4.5 6.5h15"/><path d="M9.5 6.5V4.8a1.3 1.3 0 0 1 1.3-1.3h2.4a1.3 1.3 0 0 1 1.3 1.3v1.7"/><path d="M6.5 6.5 7.5 20h9l1-13.5"/><path d="M10.5 10v6M13.5 10v6"/>',
  arrow: '<path d="M4 12h15"/><path d="M14 6.5 19.5 12 14 17.5"/>',
  pen: '<path d="M4 20l4.2-1L19 8.2a2.1 2.1 0 0 0-3-3L5.2 16 4 20z"/><path d="M13.8 6.4l3.8 3.8"/>',
  list: '<path d="M8 7h12M8 12h12M8 17h12"/><circle cx="4" cy="7" r=".9" fill="currentColor" stroke="none"/><circle cx="4" cy="12" r=".9" fill="currentColor" stroke="none"/><circle cx="4" cy="17" r=".9" fill="currentColor" stroke="none"/>',
  chart: '<path d="M5 19V11M12 19V5M19 19v-5"/>',
  gear: '<circle cx="12" cy="12" r="3.1"/><path d="M19.4 14.6a1.7 1.7 0 0 0 .34 1.88l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1 1.55V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.55 1.7 1.7 0 0 0-1.88.34l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.7 1.7 0 0 0 .34-1.88 1.7 1.7 0 0 0-1.55-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.55-1.1 1.7 1.7 0 0 0-.34-1.88l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.7 1.7 0 0 0 1.88.34H9a1.7 1.7 0 0 0 1-1.55V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.55 1.7 1.7 0 0 0 1.88-.34l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.7 1.7 0 0 0-.34 1.88V9a1.7 1.7 0 0 0 1.55 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/>'
};

document.querySelectorAll('[data-icon]').forEach(function (el) {
  const name = el.getAttribute('data-icon');
  const path = HZ_ICONS[name];
  if (!path) return;
  el.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.35" '
    + 'stroke-linecap="round" stroke-linejoin="round">' + path + '</svg>';
});
