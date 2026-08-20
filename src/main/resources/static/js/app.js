import { renderComponents } from './components.js';
import { renderOrders, renderOrderDetail } from './orders.js';
import { renderInventory } from './inventory.js';
import { renderAssemblies } from './assemblies.js';

const views = {
    components: { fn: renderComponents, crumb: ['Katalog', 'Komponenter'] },
    assemblies: { fn: renderAssemblies, crumb: ['Katalog', 'Styklister'] },
    inventory:  { fn: renderInventory,  crumb: ['Lager', 'Beholdning'] },
    orders: {
        fn: () => renderOrders(navigateTo),
        crumb: ['Indkøb', 'Bestillinger']
    },
    'order-detail': {
        fn: orderId => renderOrderDetail(orderId, navigateTo),
        crumb: ['Indkøb', 'Bestillinger']
    }
};

function navigateTo(name, arg) {
    const topName = name === 'order-detail' ? 'orders' : name;
    document.querySelectorAll('#top-nav .nav-item')
        .forEach(b => b.classList.toggle('active', b.dataset.view === topName));
    setCrumbs(name, arg);
    const view = views[name] ?? views.components;
    view.fn(arg);
}

function setCrumbs(name, arg) {
    const el = document.getElementById('crumbs');
    if (!el) return;
    const view = views[name] ?? views.components;
    const [group, page] = view.crumb;
    if (name === 'order-detail') {
        el.innerHTML = `
            <span>${group}</span>
            <span class="sep">/</span>
            <button type="button" data-crumb="orders">${page}</button>
            <span class="sep">/</span>
            <span class="current mono">#${arg ?? ''}</span>
        `;
        el.querySelector('[data-crumb="orders"]')
            .addEventListener('click', () => navigateTo('orders'));
    } else {
        el.innerHTML = `
            <span>${group}</span>
            <span class="sep">/</span>
            <span class="current">${page}</span>
        `;
    }
}

function initApp() {
    document.querySelectorAll('#top-nav .nav-item').forEach(btn => {
        btn.addEventListener('click', () => navigateTo(btn.dataset.view));
    });
    navigateTo('components');
}

window.addEventListener('DOMContentLoaded', initApp);
