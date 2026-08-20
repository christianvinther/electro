export function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

export function formatDate(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return iso;
    return d.toLocaleDateString('da-DK', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });
}

export function statusPill(status, { large = false } = {}) {
    const s = String(status ?? '').toUpperCase();
    const cls = {
        DRAFT: 'draft',
        SENT: 'warn',
        RECEIVED: 'active'
    }[s] ?? 'draft';
    return `<span class="pill ${cls}${large ? ' lg' : ''}"><span class="dot"></span>${escapeHtml(s)}</span>`;
}
