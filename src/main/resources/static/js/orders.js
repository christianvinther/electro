import {
    fetchOrders,
    fetchOrder,
    createOrder,
    addOrderLine,
    markOrderSent,
    markOrderReceived,
    fetchAllSuppliers,
    fetchAllComponents
} from './api.js';
import { openDrawer } from './drawer.js';
import { escapeHtml, formatDate, statusPill } from './util.js';

const app = document.getElementById('app');

export async function renderOrders(navigateTo) {
    app.innerHTML = '<p class="loading-state">Indlæser bestillinger…</p>';

    try {
        const orders = await fetchOrders();
        displayOrders(orders, navigateTo);
    } catch (err) {
        app.innerHTML = `<p class="empty-state error">${escapeHtml(err.message)}</p>`;
    }
}

function displayOrders(orders, navigateTo) {
    const counts = {
        DRAFT: orders.filter(o => o.status === 'DRAFT').length,
        SENT: orders.filter(o => o.status === 'SENT').length,
        RECEIVED: orders.filter(o => o.status === 'RECEIVED').length
    };

    const rows = orders.length === 0
        ? `<div class="empty-cell">Ingen bestillinger endnu.</div>`
        : orders.map(renderOrderRow).join('');

    app.innerHTML = `
        <div class="page-header">
            <div class="titles">
                <span class="eyebrow">Indkøb</span>
                <h1 class="page-title">Bestillinger</h1>
                <span class="page-lede">Alle bestillinger til leverandører — fra kladde til modtagelse.</span>
            </div>
            <div class="page-actions">
                <button class="btn primary" data-action="new">
                    <span class="plus">+</span><span>Ny bestilling</span>
                </button>
            </div>
        </div>

        <div class="table-wrap" id="orders-table">
            <div class="row row-head">
                <div class="cell w-110">Sporing</div>
                <div class="cell w-grow">Leverandør</div>
                <div class="cell w-110">Status</div>
                <div class="cell w-140">Sendt</div>
                <div class="cell w-140">Forventet</div>
                <div class="cell w-60 right">Linjer</div>
            </div>
            ${rows}
        </div>

        <div class="page-meta">
            <span><span class="num">${orders.length}</span> bestillinger</span>
            <span class="sep">·</span>
            <span><span class="num">${counts.DRAFT}</span> kladde</span>
            <span class="sep">·</span>
            <span><span class="num warn">${counts.SENT}</span> sendt</span>
            <span class="sep">·</span>
            <span><span class="num">${counts.RECEIVED}</span> modtaget</span>
        </div>
    `;

    app.querySelector('[data-action="new"]')
        .addEventListener('click', () => openCreateOrderDrawer(navigateTo));
    app.querySelector('#orders-table')
        .addEventListener('click', event => onOrdersClick(event, navigateTo));
}

function renderOrderRow(o) {
    return `
        <div class="row row-clickable" data-id="${o.id}">
            <div class="cell w-110 mono">${escapeHtml(o.trackingCode ?? '—')}</div>
            <div class="cell w-grow name">${escapeHtml(o.supplierName ?? '—')}</div>
            <div class="cell w-110">${statusPill(o.status)}</div>
            <div class="cell w-140 mono">${escapeHtml(formatDate(o.sentDate)) || '<span class="dim">—</span>'}</div>
            <div class="cell w-140 mono">${escapeHtml(formatDate(o.expectedDeliveryDate)) || '<span class="dim">—</span>'}</div>
            <div class="cell w-60 right mono">${o.lines?.length ?? 0}</div>
        </div>
    `;
}

function onOrdersClick(e, navigateTo) {
    const row = e.target.closest('.row[data-id]');
    if (!row) return;
    navigateTo('order-detail', Number(row.dataset.id));
}

export async function renderOrderDetail(orderId, navigateTo) {
    app.innerHTML = '<p class="loading-state">Indlæser bestilling…</p>';

    try {
        const [order, components] = await Promise.all([
            fetchOrder(orderId),
            fetchAllComponents()
        ]);
        displayOrderDetail(order, components, navigateTo);
    } catch (err) {
        app.innerHTML = `
            <div class="page-header">
                <div class="titles">
                    <span class="eyebrow">Indkøb</span>
                    <h1 class="page-title">Bestilling</h1>
                </div>
                <div class="page-actions">
                    <button class="btn" data-action="back">← Tilbage</button>
                </div>
            </div>
            <p class="empty-state error">${escapeHtml(err.message)}</p>
        `;
        app.querySelector('[data-action="back"]')
            .addEventListener('click', () => navigateTo('orders'));
    }
}

function displayOrderDetail(order, components, navigateTo) {
    const isDraft = order.status === 'DRAFT';
    const isSent = order.status === 'SENT';

    const totalQty = order.lines.reduce((sum, l) => sum + l.quantity, 0);

    const lineRows = order.lines.length === 0
        ? `<div class="empty-cell">Ingen linjer endnu.${isDraft ? ' Brug “Tilføj linje” for at komme i gang.' : ''}</div>`
        : order.lines.map(renderLineRow).join('');

    app.innerHTML = `
        <div class="detail-header">
            <div class="titles">
                <span class="eyebrow">Bestilling</span>
                <div class="title-row">
                    <h1 class="page-title mono">${escapeHtml(order.trackingCode ?? `#${order.id}`)}</h1>
                    ${statusPill(order.status, { large: true })}
                </div>
                <span class="meta-line">${escapeHtml(order.supplierName ?? '—')}${order.sentDate ? ` · sendt ${escapeHtml(formatDate(order.sentDate))}` : ''}</span>
            </div>
            <div class="page-actions">
                <button class="btn" data-action="back">← Tilbage</button>
                ${detailActions(isDraft, isSent)}
            </div>
        </div>

        <div class="detail-body">
            <div class="detail-main">
                <div class="panel">
                    <div class="panel-header">
                        <div class="panel-heading">
                            <h2>Bestillingslinjer</h2>
                            <span class="panel-meta">${order.lines.length} ${order.lines.length === 1 ? 'linje' : 'linjer'}</span>
                        </div>
                        <span class="panel-note">${isDraft ? 'Kan redigeres' : 'Låst — kun DRAFT kan redigeres'}</span>
                    </div>
                    <div class="row row-head">
                        <div class="cell w-grow">Komponent</div>
                        <div class="cell w-80 right">Antal</div>
                    </div>
                    ${lineRows}
                    ${order.lines.length > 0 ? `
                        <div class="row order-total-row">
                            <div class="cell w-grow name">Total stk.</div>
                            <div class="cell w-80 right mono total-quantity">${totalQty}</div>
                        </div>
                    ` : ''}
                </div>
            </div>
            <aside class="detail-aside">
                ${renderDetailsCard(order)}
                ${renderActivityCard(order)}
            </aside>
        </div>
    `;

    app.querySelector('[data-action="back"]')
        .addEventListener('click', () => navigateTo('orders'));

    const addBtn = app.querySelector('[data-action="add-line"]');
    if (addBtn) {
        addBtn.addEventListener('click', () => openAddLineDrawer(order.id, components, navigateTo));
    }

    const sendBtn = app.querySelector('[data-action="send"]');
    if (sendBtn) sendBtn.addEventListener('click', () => onSend(order.id, navigateTo));

    const receiveBtn = app.querySelector('[data-action="receive"]');
    if (receiveBtn) receiveBtn.addEventListener('click', () => onReceive(order.id, navigateTo));
}

function detailActions(isDraft, isSent) {
    if (isDraft) {
        return `
            <button class="btn" data-action="add-line">+ Tilføj linje</button>
            <button class="btn primary" data-action="send">Send →</button>
        `;
    }
    if (isSent) {
        return `<button class="btn primary" data-action="receive">Modtag varer →</button>`;
    }
    return '';
}

function renderLineRow(l) {
    return `
        <div class="row">
            <div class="cell w-grow">
                <div class="name">${escapeHtml(l.componentName ?? '')}</div>
            </div>
            <div class="cell w-80 right mono">${l.quantity}</div>
        </div>
    `;
}

function renderDetailsCard(order) {
    return `
        <div class="aside-card">
            <span class="section-label">Detaljer</span>
            <div class="kv">
                <span class="k">Leverandør</span>
                <span class="v">${escapeHtml(order.supplierName ?? '—')}</span>
            </div>
            <hr>
            <div class="kv">
                <span class="k">Forventet levering</span>
                <span class="v">${escapeHtml(formatDate(order.expectedDeliveryDate)) || '<span class="v-sub">ikke planlagt</span>'}</span>
            </div>
            <hr>
            <div class="kv">
                <span class="k">Sporingskode</span>
                <span class="v mono">${escapeHtml(order.trackingCode ?? '—')}</span>
            </div>
        </div>
    `;
}

function renderActivityCard(order) {
    const events = [];
    if (order.receivedDate) {
        events.push({
            title: 'Bestilling modtaget',
            sub: 'SENT → RECEIVED',
            when: formatDate(order.receivedDate),
            dot: 'active'
        });
    }
    if (order.sentDate) {
        events.push({
            title: 'Bestilling sendt',
            sub: 'DRAFT → SENT',
            when: formatDate(order.sentDate),
            dot: 'warn'
        });
    }
    events.push({
        title: 'Bestilling oprettet',
        sub: `DRAFT · ${order.lines.length} ${order.lines.length === 1 ? 'linje' : 'linjer'}`,
        when: '',
        dot: 'draft'
    });

    const rows = events.map((e, i) => `
        <div class="timeline-row">
            <div class="timeline-rail">
                <span class="timeline-dot ${e.dot}"></span>
                ${i < events.length - 1 ? '<span class="timeline-line"></span>' : ''}
            </div>
            <div class="timeline-body">
                <span class="timeline-title">${escapeHtml(e.title)}</span>
                <span class="timeline-sub">${escapeHtml(e.sub)}</span>
                ${e.when ? `<span class="timeline-when">${escapeHtml(e.when)}</span>` : ''}
            </div>
        </div>
    `).join('');

    return `
        <div class="aside-card">
            <span class="section-label">Aktivitet</span>
            <div class="timeline">${rows}</div>
        </div>
    `;
}

async function openCreateOrderDrawer(navigateTo) {
    try {
        const suppliers = await fetchAllSuppliers();
        const options = suppliers
            .map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`)
            .join('');

        openDrawer({
            title: 'Ny bestilling',
            submitLabel: 'Opret bestilling',
            body: `
                <div class="field">
                    <label for="o-supplier">Leverandør</label>
                    <select id="o-supplier" name="supplierId" required>
                        <option value="">Vælg leverandør</option>
                        ${options}
                    </select>
                </div>
                <div class="field">
                    <label for="o-tracking">Sporingskode</label>
                    <input id="o-tracking" name="trackingCode" type="text" placeholder="fx PO-2026-0142">
                </div>
                <div class="field">
                    <label for="o-expected">Forventet levering</label>
                    <input id="o-expected" name="expectedDeliveryDate" type="date">
                </div>
            `,
            onSubmit: async (data) => {
                const dto = {
                    supplierId: Number(data.supplierId),
                    trackingCode: data.trackingCode || null,
                    expectedDeliveryDate: data.expectedDeliveryDate || null
                };
                const created = await createOrder(dto);
                navigateTo('order-detail', created.id);
            }
        });
    } catch (err) {
        alert(err.message);
    }
}

function openAddLineDrawer(orderId, components, navigateTo) {
    const options = components
        .filter(c => c.orderable)
        .map(c => `<option value="${c.id}">${escapeHtml(c.internalNumber)} — ${escapeHtml(c.name)}</option>`)
        .join('');

    openDrawer({
        title: 'Tilføj linje',
        submitLabel: 'Tilføj',
        body: `
            <div class="field">
                <label for="l-component">Komponent</label>
                <select id="l-component" name="componentId" required>
                    <option value="">Vælg komponent</option>
                    ${options}
                </select>
            </div>
            <div class="field">
                <label for="l-qty">Antal</label>
                <input id="l-qty" name="quantity" type="number" min="1" required>
            </div>
        `,
        onSubmit: async (data) => {
            await addOrderLine(orderId, {
                componentId: Number(data.componentId),
                quantity: Number(data.quantity)
            });
            await renderOrderDetail(orderId, navigateTo);
        }
    });
}

async function onSend(orderId, navigateTo) {
    if (!confirm('Marker bestillingen som sendt? Linjer kan derefter ikke ændres.')) return;
    try {
        await markOrderSent(orderId);
        await renderOrderDetail(orderId, navigateTo);
    } catch (err) {
        alert(err.message);
    }
}

async function onReceive(orderId, navigateTo) {
    if (!confirm('Marker bestillingen som modtaget? Lageret opdateres.')) return;
    try {
        await markOrderReceived(orderId);
        await renderOrderDetail(orderId, navigateTo);
    } catch (err) {
        alert(err.message);
    }
}
