import {
    fetchAllComponents,
    fetchAllSuppliers,
    createComponent,
    discontinueComponent
} from './api.js';
import { openDrawer } from './drawer.js';
import { escapeHtml } from './util.js';

const app = document.getElementById('app');

export async function renderComponents() {
    app.innerHTML = '<p class="loading-state">Indlæser komponenter…</p>';

    try {
        const [components, suppliers] = await Promise.all([
            fetchAllComponents(),
            fetchAllSuppliers()
        ]);
        displayComponents(components, suppliers);
    } catch (err) {
        app.innerHTML = `<p class="empty-state error">${escapeHtml(err.message)}</p>`;
    }
}

function displayComponents(components, suppliers) {
    const active = components.filter(c => !c.discontinued);
    const discontinued = components.length - active.length;

    const rows = components.length === 0
        ? `<div class="empty-cell">Ingen komponenter endnu. Opret den første med “Ny komponent”.</div>`
        : components.map(renderRow).join('');

    app.innerHTML = `
        <div class="page-header">
            <div class="titles">
                <span class="eyebrow">Katalog</span>
                <h1 class="page-title">Komponenter</h1>
                <span class="page-lede">Alle komponenter på tværs af leverandører.</span>
            </div>
            <div class="page-actions">
                <button class="btn primary" data-action="new">
                    <span class="plus">+</span><span>Ny komponent</span>
                </button>
            </div>
        </div>

        <div class="table-wrap" id="components-table">
            <div class="row row-head">
                <div class="cell w-90">Internt nr.</div>
                <div class="cell w-grow">Navn</div>
                <div class="cell w-160">Eksternt varenr.</div>
                <div class="cell w-140">Leverandør</div>
                <div class="cell w-110">Status</div>
                <div class="cell w-110 right">Handling</div>
            </div>
            ${rows}
        </div>

        <div class="page-meta">
            <span><span class="num">${components.length}</span> komponenter</span>
            <span class="sep">·</span>
            <span><span class="num">${active.length}</span> aktive</span>
            ${discontinued > 0 ? `<span class="sep">·</span><span><span class="num danger">${discontinued}</span> udgået</span>` : ''}
        </div>
    `;

    app.querySelector('[data-action="new"]')
        .addEventListener('click', () => openCreateDrawer(suppliers));
    app.querySelector('#components-table')
        .addEventListener('click', onTableClick);
}

function renderRow(c) {
    let pill;
    if (c.discontinued) {
        pill = `<span class="pill danger">Udgået</span>`;
    } else if (!c.orderable) {
        pill = `<span class="pill draft">Samlesæt</span>`;
    } else {
        pill = `<span class="pill active">Aktiv</span>`;
    }

    const disabled = c.discontinued ? 'disabled' : '';
    return `
        <div class="row">
            <div class="cell w-90 mono">${escapeHtml(c.internalNumber)}</div>
            <div class="cell w-grow name">${escapeHtml(c.name)}</div>
            <div class="cell w-160 mono">${escapeHtml(c.externalPartNumber ?? '')}</div>
            <div class="cell w-140">${escapeHtml(c.supplierName ?? '—')}</div>
            <div class="cell w-110">${pill}</div>
            <div class="cell w-110 actions">
                <button class="btn danger" data-action="discontinue" data-id="${c.id}" ${disabled}>
                    Marker udgået
                </button>
            </div>
        </div>
    `;
}

async function onTableClick(e) {
    const btn = e.target.closest('button[data-action="discontinue"]');
    if (!btn || btn.disabled) return;
    if (!confirm('Marker komponenten som udgået?')) return;
    try {
        await discontinueComponent(Number(btn.dataset.id));
        await renderComponents();
    } catch (err) {
        alert(err.message);
    }
}

function openCreateDrawer(suppliers) {
    const supplierOptions = suppliers
        .map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`)
        .join('');

    const body = `
        <div class="field">
            <label for="c-name">Navn</label>
            <input id="c-name" name="name" type="text" required>
        </div>
        <div class="field">
            <label for="c-internal">Internt nummer</label>
            <input id="c-internal" name="internalNumber" type="number" required>
        </div>
        <div class="field">
            <label for="c-external">Eksternt varenummer</label>
            <input id="c-external" name="externalPartNumber" type="text">
        </div>
        <div class="field">
            <label for="c-supplier">Leverandør</label>
            <select id="c-supplier" name="supplierId" required>
                <option value="">Vælg leverandør</option>
                ${supplierOptions}
            </select>
        </div>
    `;

    openDrawer({
        title: 'Ny komponent',
        body,
        submitLabel: 'Opret komponent',
        onSubmit: async (data) => {
            const dto = {
                name: data.name,
                internalNumber: Number(data.internalNumber),
                externalPartNumber: data.externalPartNumber || null,
                supplierId: Number(data.supplierId)
            };
            await createComponent(dto);
            await renderComponents();
        }
    });
}
