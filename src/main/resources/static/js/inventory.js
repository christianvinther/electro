import { fetchInventory, recordStockCount } from './api.js';
import { openDrawer } from './drawer.js';
import { escapeHtml, formatDate } from './util.js';

const app = document.getElementById('app');

export async function renderInventory() {
    app.innerHTML = '<p class="loading-state">Indlæser lager…</p>';

    try {
        const items = await fetchInventory();
        displayInventory(items);
    } catch (err) {
        app.innerHTML = `<p class="empty-state error">${escapeHtml(err.message)}</p>`;
    }
}

function displayInventory(items) {
    const total = items.reduce((sum, i) => sum + i.totalReceived, 0);
    const zeros = items.filter(i => i.totalReceived === 0).length;

    const rows = items.length === 0
        ? `<div class="empty-cell">Intet på lager endnu. Bestillinger der modtages opdaterer lageret automatisk.</div>`
        : items.map(renderRow).join('');

    app.innerHTML = `
        <div class="page-header">
            <div class="titles">
                <span class="eyebrow">Lager</span>
                <h1 class="page-title">Beholdning</h1>
                <span class="page-lede">Modtagne stk. pr. komponent og seneste fysiske optælling.</span>
            </div>
        </div>

        <div class="table-wrap" id="inventory-table">
            <div class="row row-head">
                <div class="cell w-grow">Komponent</div>
                <div class="cell w-110 right">Modtaget</div>
                <div class="cell w-160 right">Sidst optalt</div>
                <div class="cell w-140">Optalt af</div>
                <div class="cell w-110 right">Handling</div>
            </div>
            ${rows}
        </div>

        <div class="page-meta">
            <span><span class="num">${items.length}</span> komponenter</span>
            <span class="sep">·</span>
            <span><span class="num">${total}</span> stk. modtaget i alt</span>
            ${zeros > 0 ? `<span class="sep">·</span><span><span class="num warn">${zeros}</span> uden modtagelse</span>` : ''}
        </div>
    `;

    app.querySelector('#inventory-table')
        .addEventListener('click', onTableClick);
}

function renderRow(item) {
    const countedQty = item.lastCountedQuantity !== null && item.lastCountedQuantity !== undefined
        ? `${item.lastCountedQuantity}<span class="dim"> (${escapeHtml(formatDate(item.lastCountedAt))})</span>`
        : '<span class="dim">—</span>';
    const countedBy = item.lastCountedBy
        ? escapeHtml(item.lastCountedBy)
        : '<span class="dim">—</span>';
    return `
        <div class="row">
            <div class="cell w-grow name">${escapeHtml(item.componentName ?? '')}</div>
            <div class="cell w-110 right mono received-quantity">${item.totalReceived}</div>
            <div class="cell w-160 right mono">${countedQty}</div>
            <div class="cell w-140">${countedBy}</div>
            <div class="cell w-110 actions">
                <button class="btn" data-action="count" data-id="${item.componentId}" data-name="${escapeHtml(item.componentName ?? '')}">
                    Optæl
                </button>
            </div>
        </div>
    `;
}

function onTableClick(e) {
    const btn = e.target.closest('button[data-action="count"]');
    if (!btn) return;
    openCountDrawer(Number(btn.dataset.id), btn.dataset.name);
}

function openCountDrawer(componentId, componentName) {
    openDrawer({
        title: `Optælling — ${componentName}`,
        submitLabel: 'Indsend optælling',
        body: `
            <div class="field">
                <label for="sc-qty">Antal optalt</label>
                <input id="sc-qty" name="actualQuantity" type="number" min="0" required>
            </div>
            <div class="field">
                <label for="sc-by">Optalt af</label>
                <input id="sc-by" name="countedBy" type="text" required placeholder="fx Christian">
            </div>
        `,
        onSubmit: async (data) => {
            const dto = {
                actualQuantity: Number(data.actualQuantity),
                countedBy: data.countedBy
            };
            await recordStockCount(componentId, dto);
            await renderInventory();
        }
    });
}
