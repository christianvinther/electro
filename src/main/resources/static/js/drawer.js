export function openDrawer({ title, body, onSubmit, submitLabel = 'Gem' }) {
    closeDrawer();
    const scrim = document.createElement('div');
    scrim.className = 'drawer-scrim open';
    scrim.dataset.role = 'drawer-scrim';

    const drawer = document.createElement('aside');
    drawer.className = 'drawer';
    drawer.dataset.role = 'drawer';
    drawer.innerHTML = `
        <header class="drawer-header">
            <h2></h2>
            <button class="drawer-close" type="button" aria-label="Luk">✕</button>
        </header>
        <form class="drawer-body" id="drawer-form"></form>
        <footer class="drawer-footer">
            <button type="button" class="btn ghost" data-action="cancel">Annullér</button>
            <button type="submit" form="drawer-form" class="btn primary">${submitLabel}</button>
        </footer>
    `;
    drawer.querySelector('h2').textContent = title;
    document.body.appendChild(scrim);
    document.body.appendChild(drawer);

    const form = drawer.querySelector('#drawer-form');
    form.innerHTML = body;

    requestAnimationFrame(() => drawer.classList.add('open'));

    drawer.querySelector('.drawer-close').addEventListener('click', closeDrawer);
    drawer.querySelector('[data-action="cancel"]').addEventListener('click', closeDrawer);
    scrim.addEventListener('click', closeDrawer);

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(form));
        try {
            await onSubmit(data, form);
            closeDrawer();
        } catch (err) {
            showDrawerError(err.message);
        }
    });
}

function closeDrawer() {
    document.querySelectorAll('[data-role="drawer-scrim"], [data-role="drawer"]')
        .forEach(el => el.remove());
}

function showDrawerError(message) {
    const form = document.getElementById('drawer-form');
    if (!form) return;
    let el = form.querySelector('.form-error');
    if (!el) {
        el = document.createElement('p');
        el.className = 'form-error';
        form.prepend(el);
    }
    el.textContent = message;
    el.hidden = false;
}
