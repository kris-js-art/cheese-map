import { useEffect, useMemo, useState } from "react";
import cheeseImages from "./data/cheeseImages.json";

const API_BASE = import.meta.env.VITE_API_BASE || "";
const IMAGE_OVERRIDES_KEY = "cheese_image_overrides_v1";
const FALLBACK_CHEESE_IMAGE =
  "data:image/svg+xml;utf8," +
  encodeURIComponent(
    "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 800 500'><defs><linearGradient id='g' x1='0' y1='0' x2='1' y2='1'><stop offset='0%' stop-color='#f9efe0'/><stop offset='100%' stop-color='#f5d39f'/></linearGradient></defs><rect width='800' height='500' fill='url(#g)'/><circle cx='220' cy='170' r='26' fill='#e8b86d'/><circle cx='360' cy='230' r='18' fill='#e8b86d'/><circle cx='520' cy='160' r='24' fill='#e8b86d'/><path d='M90 390 L700 390 L625 130 Z' fill='#ffcc73' stroke='#d39b3f' stroke-width='8'/></svg>"
  );

const emptyCheeseForm = {
  id: "",
  name: "",
  fats: "",
  description: "",
  price: "",
  producerId: "",
  shopId: ""
};

const emptyProducerForm = {
  name: "",
  country: "",
  description: ""
};

const emptyShopForm = {
  name: "",
  address: "",
  phone: ""
};

const emptyCategoryForm = {
  cheeseId: "",
  categoryId: "",
  name: "",
  description: ""
};

const emptyReviewForm = {
  author: "",
  rating: "5",
  comment: ""
};

function buildUrl(path) {
  return `${API_BASE}${path}`;
}

function normalizeError(error) {
  if (error?.name === "AbortError") {
    return "Запрос был отменен.";
  }
  if (error instanceof TypeError) {
    return "Назадend is unavailable. Check if Spring app is running and reachable.";
  }
  return error?.message ?? "Неизвестная ошибка";
}

async function apiRequest(path, options = {}) {
  try {
    const method = options.method ?? "GET";
    const isBodyRequest = ["POST", "PUT", "PATCH"].includes(method.toUpperCase());
    const response = await fetch(buildUrl(path), {
      headers: isBodyRequest ? { "Content-Type": "application/json" } : {},
      ...options
    });

    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get("content-type") ?? "";
    const payload = contentType.includes("application/json") ? await response.json() : null;
    const textPayload = !contentType.includes("application/json") ? await response.text() : "";

    if (!response.ok) {
      const validation = payload?.validationErrors
        ? Object.entries(payload.validationErrors)
            .map(([field, messages]) => `${field}: ${(messages ?? []).join(", ")}`)
            .join("; ")
        : "";
      const lowerText = (textPayload ?? "").toLowerCase();
      const isProxyLike500 =
        response.status === 500 &&
        (!textPayload || lowerText.includes("econnrefused") || lowerText.includes("proxy error"));

      if (isProxyLike500) {
        throw new Error("Сервер недоступен на http://localhost:8080 (цель прокси Vite).");
      }

      const message = validation || payload?.message || textPayload || `Request failed with status ${response.status}`;
      throw new Error(message);
    }

    return payload;
  } catch (error) {
    throw new Error(normalizeError(error));
  }
}

function toNumber(value) {
  if (value === "" || value === null || value === undefined) {
    return null;
  }
  const parsed = Number(value);
  return Number.isNaN(parsed) ? null : parsed;
}

function readImageOverrides() {
  try {
    const raw = window.localStorage.getItem(IMAGE_OVERRIDES_KEY);
    if (!raw) {
      return {};
    }
    const parsed = JSON.parse(raw);
    return parsed && typeof parsed === "object" ? parsed : {};
  } catch {
    return {};
  }
}

function resolveCheeseImage(cheese, overrides) {
  const idKey = String(cheese?.id ?? "");
  const normalizedНазвание = (cheese?.name ?? "").trim().toLowerCase();
  return (
    overrides[idKey] ||
    cheeseImages?.byId?.[idKey] ||
    cheeseImages?.byНазвание?.[normalizedНазвание] ||
    FALLBACK_CHEESE_IMAGE
  );
}

function App() {
  const [mode, setMode] = useState("store");
  const [alert, setAlert] = useState("");
  const [imageOverrides, setImageOverrides] = useState(() => readImageOverrides());

  const showAlert = (text) => {
    setAlert(text);
    window.clearTimeout(showAlert.timer);
    showAlert.timer = window.setTimeout(() => setAlert(""), 2800);
  };

  const saveImageOverride = (cheeseId, url) => {
    const idKey = String(cheeseId);
    setImageOverrides((prev) => {
      const next = { ...prev };
      if (url && url.trim()) {
        next[idKey] = url.trim();
      } else {
        delete next[idKey];
      }
      window.localStorage.setItem(IMAGE_OVERRIDES_KEY, JSON.stringify(next));
      return next;
    });
  };

  return (
    <div className="app">
      <header className="top">
        <div className="brand-block">
          <p className="mini">Каталог и управление сырами</p>
          <h1 className="brand-title">Сырная Карта</h1>
        </div>
        <div className="top-art" aria-hidden="true">
          <div className="blob blob-a" />
          <div className="blob blob-b" />
          <div className="blob blob-c" />
        </div>
        <div className="mode-switch">
          <button type="button" className={mode === "store" ? "active" : ""} onClick={() => setMode("store")}>
            Покупатель
          </button>
          <button type="button" className={mode === "admin" ? "active" : ""} onClick={() => setMode("admin")}>
            Администратор
          </button>
        </div>
      </header>

      {alert ? <div className="flash">{alert}</div> : null}

      {mode === "store" ? (
        <StoreFront showAlert={showAlert} imageOverrides={imageOverrides} />
      ) : (
        <AdminPanel showAlert={showAlert} imageOverrides={imageOverrides} saveImageOverride={saveImageOverride} />
      )}
    </div>
  );
}

function StoreFront({ showAlert, imageOverrides }) {
  const [cheeses, setCheeses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [query, setQuery] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("all");
  const [expandedCheeseId, setExpandedCheeseId] = useState(null);
  const [reviewsByCheese, setReviewsByCheese] = useState({});
  const [reviewForms, setReviewForms] = useState({});

  const loadCatalog = async () => {
    setLoading(true);
    setError("");
    try {
      try {
        const list = await apiRequest("/api/cheeses/graph");
        setCheeses(list ?? []);
      } catch {
        const fallback = await apiRequest("/api/cheeses");
        setCheeses(fallback ?? []);
      }
    } catch (err) {
      setError(err.message);
      setCheeses([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCatalog();
  }, []);

  const categories = useMemo(() => {
    const names = new Set();
    cheeses.forEach((cheese) => {
      (cheese.categories ?? []).forEach((category) => names.add(category.name));
    });
    return Array.from(names).sort((a, b) => a.localeCompare(b));
  }, [cheeses]);

  const visibleCheeses = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    return cheeses
      .filter((cheese) => {
        const byНазвание = cheese.name?.toLowerCase().includes(normalizedQuery);
        const byDesc = cheese.description?.toLowerCase().includes(normalizedQuery);
        const matchQuery = !normalizedQuery || byНазвание || byDesc;
        const matchCategory =
          categoryFilter === "all" || (cheese.categories ?? []).some((category) => category.name === categoryFilter);
        return matchQuery && matchCategory;
      })
      .sort((a, b) => (a.name ?? "").localeCompare(b.name ?? ""));
  }, [cheeses, query, categoryFilter]);

  const openReviews = async (cheeseId) => {
    const nextId = expandedCheeseId === cheeseId ? null : cheeseId;
    setExpandedCheeseId(nextId);

    if (!nextId) {
      return;
    }

    try {
      const reviews = await apiRequest(`/api/reviews/cheese/${cheeseId}`);
      setReviewsByCheese((prev) => ({ ...prev, [cheeseId]: reviews ?? [] }));
    } catch (err) {
      setError(err.message);
    }
  };

  const updateReviewForm = (cheeseId, field, value) => {
    const current = reviewForms[cheeseId] ?? emptyReviewForm;
    setReviewForms((prev) => ({ ...prev, [cheeseId]: { ...current, [field]: value } }));
  };

  const submitReview = async (cheeseId) => {
    const form = reviewForms[cheeseId] ?? emptyReviewForm;
    try {
      await apiRequest(`/api/reviews/cheese/${cheeseId}`, {
        method: "POST",
        body: JSON.stringify({
          author: form.author.trim(),
          rating: Number(form.rating),
          comment: form.comment.trim()
        })
      });

      showAlert("Отзыв добавлен");
      setReviewForms((prev) => ({ ...prev, [cheeseId]: emptyReviewForm }));
      const reviews = await apiRequest(`/api/reviews/cheese/${cheeseId}`);
      setReviewsByCheese((prev) => ({ ...prev, [cheeseId]: reviews ?? [] }));
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="layout store-only">
      <div className="catalog">
        <div className="panel">
          <h2>Каталог сыров</h2>
          <div className="filters">
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Поиск по названию или описанию"
            />
            <select value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)}>
              <option value="all">Все категории</option>
              {categories.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
            <button type="button" className="ghost" onClick={loadCatalog}>
              Обновить
            </button>
          </div>
          {loading ? <p className="state">Загрузка...</p> : null}
          {error ? <p className="state error">{error}</p> : null}
        </div>

        <div className="cards">
          {visibleCheeses.map((cheese) => {
            const isOpen = expandedCheeseId === cheese.id;
            const form = reviewForms[cheese.id] ?? emptyReviewForm;
            const reviews = reviewsByCheese[cheese.id] ?? cheese.reviews ?? [];
            const imageUrl = resolveCheeseImage(cheese, imageOverrides);

            return (
              <article className="cheese-card" key={cheese.id}>
                <img
                  className="cheese-image"
                  src={imageUrl}
                  alt={cheese.name ?? "Cheese"}
                  loading="lazy"
                  onError={(event) => {
                    event.currentTarget.src = FALLBACK_CHEESE_IMAGE;
                  }}
                />
                <h3>{cheese.name}</h3>
                <p className="desc">{cheese.description || "Описание is not available yet."}</p>
                <p className="meta">Жирность: {cheese.fats}%</p>
                <p className="meta">Производитель: {cheese.producer?.name ?? "Не указан"}</p>
                <div className="tags">
                  {(cheese.categories ?? []).map((category) => (
                    <span key={category.id ?? category.name}>{category.name}</span>
                  ))}
                </div>
                <div className="buy-row">
                  <strong>{Number(cheese.price || 0).toFixed(2)} BYN</strong>
                  <button type="button" className="ghost" onClick={() => openReviews(cheese.id)}>
                    {isOpen ? "Скрыть отзывы" : "Отзывы"}
                  </button>
                </div>

                {isOpen ? (
                  <div className="review-block">
                    <h4>Отзывы</h4>
                    {reviews.length === 0 ? <p className="state">Пока нет отзывов</p> : null}
                    <ul className="review-list">
                      {reviews.map((review) => (
                        <li key={review.id}>
                          <strong>{review.author}</strong> - {review.rating}/5
                          <p>{review.comment}</p>
                        </li>
                      ))}
                    </ul>
                    <div className="review-form">
                      <input
                        placeholder="Ваше имя"
                        value={form.author}
                        onChange={(event) => updateReviewForm(cheese.id, "author", event.target.value)}
                      />
                      <select
                        value={form.rating}
                        onChange={(event) => updateReviewForm(cheese.id, "rating", event.target.value)}
                      >
                        <option value="5">5</option>
                        <option value="4">4</option>
                        <option value="3">3</option>
                        <option value="2">2</option>
                        <option value="1">1</option>
                      </select>
                      <textarea
                        rows={2}
                        placeholder="Комментарий"
                        value={form.comment}
                        onChange={(event) => updateReviewForm(cheese.id, "comment", event.target.value)}
                      />
                      <button type="button" onClick={() => submitReview(cheese.id)}>
                        Отправить отзыв
                      </button>
                    </div>
                  </div>
                ) : null}
              </article>
            );
          })}
        </div>
      </div>
    </section>
  );
}

function AdminPanel({ showAlert, imageOverrides, saveImageOverride }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [cheeses, setCheeses] = useState([]);
  const [producers, setProducers] = useState([]);
  const [shops, setShops] = useState([]);
  const [categories, setCategories] = useState([]);
  const [cheeseForm, setCheeseForm] = useState(emptyCheeseForm);
  const [producerForm, setProducerForm] = useState(emptyProducerForm);
  const [shopForm, setShopForm] = useState(emptyShopForm);
  const [categoryForm, setCategoryForm] = useState(emptyCategoryForm);
  const [pagedShops, setPagedShops] = useState([]);
  const [shopsPage, setShopsPage] = useState(0);
  const [shopsTotalPages, setShopsTotalPages] = useState(0);
  const [shopsPageLoading, setShopsPageLoading] = useState(false);
  const [cheeseImageUrl, setCheeseImageUrl] = useState("");

  const uniqueCategories = useMemo(() => {
    const seen = new Set();
    return categories.filter((category) => {
      const key = (category.name ?? "").trim().toLowerCase();
      if (!key || seen.has(key)) {
        return false;
      }
      seen.add(key);
      return true;
    });
  }, [categories]);

  const isИзменитьingCheese = Boolean(cheeseForm.id);

  const loadShopsPage = async (pageNumber) => {
    setShopsPageLoading(true);
    try {
      const page = await apiRequest(`/api/shops?page=${pageNumber}&size=5&sortBy=id&ascending=true`);
      const content = page?.content ?? [];
      const meta = page?.page ?? null;
      const currentPage = page?.number ?? meta?.number ?? 0;
      const totalPages = page?.totalPages ?? meta?.totalPages ?? 0;

      setPagedShops(content);
      setShopsPage(currentPage);
      setShopsTotalPages(totalPages);
    } catch (err) {
      setError((prev) => [prev, `Shops pagination: ${err.message}`].filter(Boolean).join(" | "));
    } finally {
      setShopsPageLoading(false);
    }
  };

  const loadAdminData = async () => {
    setLoading(true);
    setError("");
    const errors = [];

    try {
      const list = await apiRequest("/api/cheeses");
      setCheeses(list ?? []);
    } catch (err) {
      setCheeses([]);
      errors.push(`Cheeses: ${err.message}`);
    }

    try {
      const list = await apiRequest("/api/producers");
      setProducers(list ?? []);
    } catch (err) {
      setProducers([]);
      errors.push(`Producers: ${err.message}`);
    }

    try {
      const page = await apiRequest("/api/shops?page=0&size=200&sortBy=id&ascending=true");
      setShops(page?.content ?? []);
    } catch (err) {
      setShops([]);
      errors.push(`Shops: ${err.message}`);
    }

    try {
      const list = await apiRequest("/api/categories");
      setCategories(list ?? []);
    } catch (err) {
      setCategories([]);
      errors.push(`Categories: ${err.message}`);
    }

    setError(errors.join(" | "));
    setLoading(false);
    await loadShopsPage(0);
  };

  useEffect(() => {
    loadAdminData();
  }, []);

  const saveCheese = async (event) => {
    event.preventDefault();
    setError("");
    try {
      const payload = {
        name: cheeseForm.name.trim(),
        fats: toNumber(cheeseForm.fats),
        description: cheeseForm.description.trim(),
        price: toNumber(cheeseForm.price)
      };

      if (isИзменитьingCheese) {
        const updated = await apiRequest(`/api/cheeses/${cheeseForm.id}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
        const updatedId = updated?.id ?? cheeseForm.id;
        saveImageOverride(updatedId, cheeseImageUrl);
        showAlert("Сыр обновлен");
      } else {
        if (!cheeseForm.producerId || !cheeseForm.shopId) {
          throw new Error("Выбрать producer and shop before creating cheese.");
        }
        const created = await apiRequest(`/api/cheeses/${cheeseForm.shopId}/${cheeseForm.producerId}`, {
          method: "POST",
          body: JSON.stringify(payload)
        });
        if (created?.id) {
          saveImageOverride(created.id, cheeseImageUrl);
        }
        showAlert("Сыр добавлен");
      }

      setCheeseForm(emptyCheeseForm);
      setCheeseImageUrl("");
      await loadAdminData();
    } catch (err) {
      setError(err.message);
    }
  };

  const createProducer = async (event) => {
    event.preventDefault();
    setError("");
    try {
      await apiRequest("/api/producers", {
        method: "POST",
        body: JSON.stringify({
          name: producerForm.name.trim(),
          country: producerForm.country.trim(),
          description: producerForm.description.trim()
        })
      });
      showAlert("Производитель добавлен");
      setProducerForm(emptyProducerForm);
      await loadAdminData();
    } catch (err) {
      setError(err.message);
    }
  };

  const createShop = async (event) => {
    event.preventDefault();
    setError("");
    try {
      await apiRequest("/api/shops", {
        method: "POST",
        body: JSON.stringify({
          name: shopForm.name.trim(),
          address: shopForm.address.trim(),
          phone: shopForm.phone.trim()
        })
      });
      showAlert("Магазин добавлен");
      setShopForm(emptyShopForm);
      await loadAdminData();
    } catch (err) {
      setError(err.message);
    }
  };

  const createCategory = async (event) => {
    event.preventDefault();
    setError("");
    try {
      if (!categoryForm.cheeseId) {
        throw new Error("Выбрать сыр before attaching category.");
      }
      await apiRequest(`/api/categories/${categoryForm.cheeseId}`, {
        method: "POST",
        body: JSON.stringify({
          name: categoryForm.name.trim(),
          description: categoryForm.description.trim()
        })
      });
      showAlert("Категория добавлена к сыру");
      setCategoryForm(emptyCategoryForm);
      await loadAdminData();
    } catch (err) {
      setError(err.message);
    }
  };

  const applyExistingCategoryTemplate = (categoryId) => {
    const selected = categories.find((category) => String(category.id) === String(categoryId));
    if (!selected) {
      setCategoryForm((prev) => ({ ...prev, categoryId }));
      return;
    }

    setCategoryForm((prev) => ({
      ...prev,
      categoryId: String(selected.id),
      name: selected.name ?? "",
      description: selected.description ?? ""
    }));
  };

  const startИзменитьCheese = (cheese) => {
    const currentOverride = imageOverrides[String(cheese.id)] ?? "";
    setCheeseForm({
      id: String(cheese.id),
      name: cheese.name ?? "",
      fats: cheese.fats ?? "",
      description: cheese.description ?? "",
      price: cheese.price ?? "",
      producerId: String(cheese.producer?.id ?? ""),
      shopId: ""
    });
    setCheeseImageUrl(currentOverride);
  };

  const deleteCheese = async (id) => {
    try {
      await apiRequest(`/api/cheeses/${id}`, { method: "DELETE" });
      showAlert("Сыр удален");
      await loadAdminData();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="layout admin">
      <div className="panel">
        <h2>Список доступных магазинов</h2>
        <div className="shops-pagination-header">
          <p className="state">
            Страница {shopsPage + 1} из {Math.max(shopsTotalPages, 1)}
          </p>
          <div className="shops-pagination-actions">
            <button
              type="button"
              className="ghost"
              disabled={shopsPageLoading || shopsPage <= 0}
              onClick={() => loadShopsPage(Math.max(shopsPage - 1, 0))}
            >
              Назад
            </button>
            <button
              type="button"
              className="ghost"
              disabled={shopsPageLoading || shopsPage + 1 >= shopsTotalPages}
              onClick={() => loadShopsPage(shopsPage + 1)}
            >
              Вперед
            </button>
          </div>
        </div>
        <div className="shops-grid">
          {pagedShops.map((shop) => (
            <article key={shop.id} className="shop-card">
              <h3>
                #{shop.id} {shop.name}
              </h3>
              <p>{shop.address}</p>
              <p className="state">{shop.phone}</p>
            </article>
          ))}
        </div>
      </div>

      <div className="panel">
        <h2>Панель администратора</h2>
        <p className="state">Создание производителей, магазинов и управление сырами.</p>
        {loading ? <p className="state">Загрузка...</p> : null}
        {error ? <p className="state error">{error}</p> : null}

        <div className="admin-grid">
          <form className="admin-form compact" onSubmit={createProducer}>
            <h3>Новый производитель</h3>
            <input
              placeholder="Название"
              value={producerForm.name}
              onChange={(event) => setProducerForm({ ...producerForm, name: event.target.value })}
              required
            />
            <input
              placeholder="Страна"
              value={producerForm.country}
              onChange={(event) => setProducerForm({ ...producerForm, country: event.target.value })}
              required
            />
            <textarea
              rows={2}
              placeholder="Описание"
              value={producerForm.description}
              onChange={(event) => setProducerForm({ ...producerForm, description: event.target.value })}
            />
            <button type="submit">Добавить производителя</button>
          </form>

          <form className="admin-form compact" onSubmit={createShop}>
            <h3>Новый магазин</h3>
            <input
              placeholder="Название магазина"
              value={shopForm.name}
              onChange={(event) => setShopForm({ ...shopForm, name: event.target.value })}
              required
            />
            <input
              placeholder="Адрес"
              value={shopForm.address}
              onChange={(event) => setShopForm({ ...shopForm, address: event.target.value })}
              required
            />
            <input
              placeholder="Телефон"
              value={shopForm.phone}
              onChange={(event) => setShopForm({ ...shopForm, phone: event.target.value })}
              required
            />
            <button type="submit">Добавить магазин</button>
          </form>

          <form className="admin-form compact" onSubmit={createCategory}>
            <h3>Новая категория</h3>
            <select
              value={categoryForm.cheeseId}
              onChange={(event) => setCategoryForm({ ...categoryForm, cheeseId: event.target.value })}
              required
            >
              <option value="">Выбрать сыр</option>
              {cheeses.map((cheese) => (
                <option key={cheese.id} value={cheese.id}>
                  {cheese.id} - {cheese.name}
                </option>
              ))}
            </select>
            <select
              value={categoryForm.categoryId}
              onChange={(event) => applyExistingCategoryTemplate(event.target.value)}
            >
              <option value="">Новая категория вручную</option>
              {uniqueCategories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.id} - {category.name}
                </option>
              ))}
            </select>
            <input
              placeholder="Название категории"
              value={categoryForm.name}
              onChange={(event) => setCategoryForm({ ...categoryForm, name: event.target.value })}
              required
            />
            <textarea
              rows={2}
              placeholder="Описание категории"
              value={categoryForm.description}
              onChange={(event) => setCategoryForm({ ...categoryForm, description: event.target.value })}
            />
            <button type="submit">Добавить категорию</button>
          </form>
        </div>
      </div>

      <div className="panel">
        <h2>{isИзменитьingCheese ? "Изменить cheese" : "Добавить сыр"}</h2>
        <form className="admin-form" onSubmit={saveCheese}>
          <label>
            Название
            <input value={cheeseForm.name} onChange={(event) => setCheeseForm({ ...cheeseForm, name: event.target.value })} required />
          </label>
          <label>
            Жирность
            <input
              type="number"
              step="0.01"
              value={cheeseForm.fats}
              onChange={(event) => setCheeseForm({ ...cheeseForm, fats: event.target.value })}
              required
            />
          </label>
          <label className="full">
            Описание
            <textarea
              rows={3}
              value={cheeseForm.description}
              onChange={(event) => setCheeseForm({ ...cheeseForm, description: event.target.value })}
            />
          </label>
          <label>
            Цена (BYN)
            <input
              type="number"
              step="0.01"
              value={cheeseForm.price}
              onChange={(event) => setCheeseForm({ ...cheeseForm, price: event.target.value })}
              required
            />
          </label>
          <label className="full">
            URL картинки (опционально)
            <input
              type="url"
              placeholder="https://example.com/cheese.jpg"
              value={cheeseImageUrl}
              onChange={(event) => setCheeseImageUrl(event.target.value)}
            />
          </label>
          <label>
            Производитель (для создания)
            <select
              value={cheeseForm.producerId}
              onChange={(event) => setCheeseForm({ ...cheeseForm, producerId: event.target.value })}
              disabled={isИзменитьingCheese}
            >
              <option value="">Выбрать</option>
              {producers.map((producer) => (
                <option key={producer.id} value={producer.id}>
                  {producer.id} - {producer.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Магазин (для создания)
            <select
              value={cheeseForm.shopId}
              onChange={(event) => setCheeseForm({ ...cheeseForm, shopId: event.target.value })}
              disabled={isИзменитьingCheese}
            >
              <option value="">Выбрать</option>
              {shops.map((shop) => (
                <option key={shop.id} value={shop.id}>
                  {shop.id} - {shop.name}
                </option>
              ))}
            </select>
          </label>
          <div className="admin-actions">
            <button type="submit">{isИзменитьingCheese ? "Сохранить изменения" : "Добавить сыр"}</button>
            <button
              type="button"
              className="ghost"
              onClick={() => {
                setCheeseForm(emptyCheeseForm);
                setCheeseImageUrl("");
              }}
            >
              Очистить
            </button>
            <button type="button" className="ghost" onClick={loadAdminData}>
              Обновить
            </button>
          </div>
        </form>
      </div>

      <div className="panel">
        <h2>Управление сырами</h2>
        <div className="admin-list">
          {cheeses
            .slice()
            .sort((a, b) => (a.name ?? "").localeCompare(b.name ?? ""))
            .map((cheese) => (
              <article key={cheese.id}>
                <div>
                  <h3>
                    #{cheese.id} {cheese.name}
                  </h3>
                  <p>{cheese.description}</p>
                  <p className="state">
                    {Number(cheese.price || 0).toFixed(2)} BYN - {cheese.fats}% - {cheese.producer?.name ?? "Без производителя"}
                  </p>
                </div>
                <div className="inline-actions">
                  <button type="button" className="ghost" onClick={() => startИзменитьCheese(cheese)}>
                    Изменить
                  </button>
                  <button type="button" className="danger" onClick={() => deleteCheese(cheese.id)}>
                    Удалить
                  </button>
                </div>
              </article>
            ))}
        </div>
      </div>
    </section>
  );
}

export default App;
