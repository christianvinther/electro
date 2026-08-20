import { fetchAllAssemblies } from './api.js';
import { escapeHtml } from './util.js';

const app = document.getElementById('app');

export async function renderAssemblies() {
    app.innerHTML = '<p class="loading-state">Indlæser styklister…</p>';

    try {
        const assemblies = await fetchAllAssemblies();
        displayAssemblies(assemblies);
    } catch (err) {
        app.innerHTML = `<p class="empty-state error">${escapeHtml(err.message)}</p>`;
    }
}

function displayAssemblies(assemblies) {
    const totalParts = assemblies.reduce((sum, a) => sum + (a.parts?.length ?? 0), 0);

    const cards = assemblies.length === 0
        ? `<p class="empty-state">Ingen styklister oprettet endnu.</p>`
        : `<div class="cards-grid">${assemblies.map(renderCard).join('')}</div>`;

    app.innerHTML = `
        <div class="page-header">
            <div class="titles">
                <span class="eyebrow">Katalog</span>
                <h1 class="page-title">Styklister</h1>
                <span class="page-lede">Sammensætninger af komponenter til færdige produkter.</span>
            </div>
        </div>

        ${cards}

        <div class="page-meta">
            <span><span class="num">${assemblies.length}</span> styklister</span>
            <span class="sep">·</span>
            <span><span class="num">${totalParts}</span> deltyper i alt</span>
        </div>
    `;
}

function renderCard(a) {
    const parts = (a.parts?.length ?? 0) === 0
        ? `<li class="empty-state assembly-empty">Ingen dele.</li>`
        : a.parts.map(p => `
            <li>
                <span class="qty">${p.quantity}×</span>
                <span>${escapeHtml(p.componentName ?? '')}</span>
            </li>
        `).join('');

    return `
        <div class="assembly-card">
            <div class="head">
                <h3>${escapeHtml(a.producedComponentName ?? '')}</h3>
                <span class="count">${a.parts?.length ?? 0} ${a.parts?.length === 1 ? 'del' : 'dele'}</span>
            </div>
            <ul>${parts}</ul>
        </div>
    `;
}
