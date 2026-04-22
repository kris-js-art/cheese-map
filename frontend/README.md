# Cheese Map Frontend (React SPA)

## What is implemented
- SPA client on React (`Vite`).
- Integration with existing Spring API (`/api/...`).
- Two modes:
  - Buyer mode: cheese catalog, search, category filter, and creating reviews for cheeses.
  - Admin mode: create producers, create shops, create categories for cheeses, add/edit/delete cheeses.
- Rendering of relations:
  - `ManyToMany`: cheese categories in catalog cards.
- Currency in UI: `BYN`.
- Cheese images:
  - base mapping from `src/data/cheeseImages.json`,
  - admin override URL per cheese saved in browser `localStorage`.

## Run
1. Start backend on `http://localhost:8080`.
2. In this folder run:
```bash
npm install
npm run dev
```
3. Open `http://localhost:5173`.

`vite.config.js` proxies `/api` to `http://localhost:8080`.

## Optional API base URL
You can override the proxy by setting:
```bash
VITE_API_BASE_URL=http://localhost:8080
```
