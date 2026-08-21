const BASE = '/api';

// fetch kaster ikke selv fejl ved fx 400 eller 404, så status tjekkes efter hvert kald.
async function get(path) {
    const res = await fetch(BASE + path);
    if (!res.ok) throw await toError(res);
    return res.json();
}

async function send(path, method, body) {
    const res = await fetch(BASE + path, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: body ? JSON.stringify(body) : undefined
    });
    if (!res.ok) throw await toError(res);
    return res.status === 204 ? null : res.json();
}

async function toError(res) {
    let detail = `HTTP ${res.status}`;
    try {
        const pd = await res.json();
        if (pd && pd.detail) detail = pd.detail;
    } catch (_) { }
    const err = new Error(detail);
    err.status = res.status;
    return err;
}

export const fetchAllSuppliers = () => get('/suppliers');

export const fetchAllComponents   = ()    => get('/components');
export const createComponent      = (dto) => send('/components', 'POST', dto);
export const discontinueComponent = (id)  => send(`/components/${id}/discontinue`, 'PATCH');

export const fetchOrders     = ()       => get('/orders?status=open');
export const fetchOrder      = (id)     => get(`/orders/${id}`);
export const createOrder     = (dto)    => send('/orders', 'POST', dto);
export const addOrderLine    = (id, dto)=> send(`/orders/${id}/lines`, 'POST', dto);
export const markOrderSent   = (id)     => send(`/orders/${id}/send`, 'PATCH');
export const markOrderReceived = (id)   => send(`/orders/${id}/receive`, 'PATCH');

export const fetchInventory      = ()                  => get('/inventory');
export const recordStockCount    = (componentId, dto)  => send(`/inventory/${componentId}/count`, 'POST', dto);

export const fetchAllAssemblies = ()   => get('/assemblies');
