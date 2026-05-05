import { useEffect, useMemo, useState } from "react";
import cheeseImages from "./data/cheeseImages.json";

const API_BASE = import.meta.env.VITE_API_BASE || "";
const IMAGE_OVERRIDES_KEY = "cheese_image_overrides_v1";
const FALLBACK_CHEESE_IMAGE =
  "data:image/svg+xml;utf8," +
  encodeURIComponent(
    "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 800 500'><defs><linearGradient id='g' x1='0' y1='0' x2='1' y2='1'><stop offset='0%' stop-color='#f9efe0'/><stop offset='100%' stop-color='#f5d39f'/></linearGradient></defs><rect width='800' height='500' fill='url(#g)'/><circle cx='220' cy='170' r='26' fill='#e8b86d'/><circle cx='360' cy='230' r='18' fill='#e8b86d'/><circle cx='520' cy='160' r='24' fill='#e8b86d'/><path d='M90 390 L700 390 L625 130 Z' fill='#ffcc73' stroke='#d39b3f' stroke-width='8'/></svg>"
  );
const CATALOG_PAGE_SIZE = 4;
const ADMIN_CHEESES_PAGE_SIZE = 4;

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
            Каталог
          </button>
          <button type="button" className={mode === "admin" ? "active" : ""} onClick={() => setMode("admin")}>
            Управление
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
  const [catalogPage, setCatalogPage] = useState(0);
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
      .sort((a, b) => (a.name ?? "").localeCompare(b.name ?? "", "ru", { sensitivity: "base" }));
  }, [cheeses, query, categoryFilter]);

  const catalogTotalPages = Math.max(Math.ceil(visibleCheeses.length / CATALOG_PAGE_SIZE), 1);

  const visibleCheesesPage = useMemo(() => {
    const start = catalogPage * CATALOG_PAGE_SIZE;
    return visibleCheeses.slice(start, start + CATALOG_PAGE_SIZE);
  }, [visibleCheeses, catalogPage]);

  useEffect(() => {
    setCatalogPage(0);
  }, [query, categoryFilter]);

  useEffect(() => {
    const lastPage = Math.max(catalogTotalPages - 1, 0);
    if (catalogPage > lastPage) {
      setCatalogPage(lastPage);
    }
  }, [catalogPage, catalogTotalPages]);

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

        {visibleCheeses.length === 0 ? <p className="state">Ничего не найдено</p> : null}
        <div className="cards">
          {visibleCheesesPage.map((cheese) => {
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
                <div className="cheese-title-row">
                  <h3>{cheese.name}</h3>
                  <div className="tags">
                    {(cheese.categories ?? []).map((category) => (
                      <span key={category.id ?? category.name}>{category.name}</span>
                    ))}
                  </div>
                </div>
                <p className="desc">{cheese.description || "Описание is not available yet."}</p>
                <p className="meta">Жирность: {cheese.fats}%</p>
                <p className="meta">Производитель: {cheese.producer?.name ?? "Не указан"}</p>
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
        <div className="shops-pagination-header">
          <p className="state">
            Страница {catalogPage + 1} из {catalogTotalPages}
          </p>
          <div className="shops-pagination-actions">
            <button
              type="button"
              className="ghost"
              disabled={loading || catalogPage <= 0}
              onClick={() => setCatalogPage((prev) => Math.max(prev - 1, 0))}
            >
              Назад
            </button>
            <button
              type="button"
              className="ghost"
              disabled={loading || catalogPage + 1 >= catalogTotalPages}
              onClick={() => setCatalogPage((prev) => Math.min(prev + 1, catalogTotalPages - 1))}
            >
              Вперед
            </button>
          </div>
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
  const [pagedCheeses, setPagedCheeses] = useState([]);
  const [cheesesPage, setCheesesPage] = useState(0);
  const [cheesesTotalPages, setCheesesTotalPages] = useState(0);
  const [cheesesPageLoading, setCheesesPageLoading] = useState(false);
  const [pagedShops, setPagedShops] = useState([]);
  const [shopsPage, setShopsPage] = useState(0);
  const [shopsTotalPages, setShopsTotalPages] = useState(0);
  const [shopsPageLoading, setShopsPageLoading] = useState(false);
  const [cheeseImageUrl, setCheeseImageUrl] = useState("");
  const [adminModal, setAdminModal] = useState(null);
  const [showQuickProducer, setShowQuickProducer] = useState(false);
  const [showQuickShop, setShowQuickShop] = useState(false);
  const [showNewCategoryForm, setShowNewCategoryForm] = useState(false);
  const [newCategoryForCheese, setNewCategoryForCheese] = useState({ name: "", description: "" });
  const [selectedCategoryForCheeseIds, setSelectedCategoryForCheeseIds] = useState([]);

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

  const mergeUniqueCategoryDrafts = (categoryDrafts) => {
    const seen = new Set();
    return categoryDrafts.filter((category) => {
      const name = (category?.name ?? "").trim();
      if (!name) {
        return false;
      }
      const key = name.toLowerCase();
      if (seen.has(key)) {
        return false;
      }
      seen.add(key);
      return true;
    });
  };

  const getCurrentCategoryDrafts = () => {
    const selectedCategoryTemplates = uniqueCategories.filter((category) =>
      selectedCategoryForCheeseIds.includes(String(category.id))
    );
    return mergeUniqueCategoryDrafts(selectedCategoryTemplates.map((category) => ({
      name: (category.name ?? "").trim(),
      description: (category.description ?? "").trim()
    })));
  };

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

  const loadCheesesPage = async (pageNumber) => {
    setCheesesPageLoading(true);
    try {
      const page = await apiRequest(
        `/api/cheeses/paged?page=${pageNumber}&size=${ADMIN_CHEESES_PAGE_SIZE}&sortBy=name&ascending=true`
      );
      const content = page?.content ?? [];
      const meta = page?.page ?? null;
      const currentPage = page?.number ?? meta?.number ?? 0;
      const totalPages = page?.totalPages ?? meta?.totalPages ?? 0;

      if (totalPages > 0 && pageNumber >= totalPages) {
        const lastPage = Math.max(totalPages - 1, 0);
        const fallback = await apiRequest(
          `/api/cheeses/paged?page=${lastPage}&size=${ADMIN_CHEESES_PAGE_SIZE}&sortBy=name&ascending=true`
        );
        const fallbackMeta = fallback?.page ?? null;
        setPagedCheeses(fallback?.content ?? []);
        setCheesesPage(fallback?.number ?? fallbackMeta?.number ?? lastPage);
        setCheesesTotalPages(fallback?.totalPages ?? fallbackMeta?.totalPages ?? totalPages);
        return;
      }

      setPagedCheeses(content);
      setCheesesPage(currentPage);
      setCheesesTotalPages(totalPages);
    } catch (err) {
      setPagedCheeses([]);
      setError((prev) => [prev, `Cheeses pagination: ${err.message}`].filter(Boolean).join(" | "));
    } finally {
      setCheesesPageLoading(false);
    }
  };

  const loadAdminData = async (targetCheesesPage = cheesesPage) => {
    setLoading(true);
    setError("");
    const errors = [];

    try {
      try {
        const listWithGraph = await apiRequest("/api/cheeses/graph");
        setCheeses(listWithGraph ?? []);
      } catch {
        const fallbackList = await apiRequest("/api/cheeses");
        setCheeses(fallbackList ?? []);
      }
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
    await loadCheesesPage(targetCheesesPage);
    await loadShopsPage(0);
  };

  useEffect(() => {
    loadAdminData();
  }, []);

  useEffect(() => {
    const intervalId = window.setInterval(async () => {
      loadAdminData(cheesesPage);
    }, 15000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [cheesesPage]);

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
        if (!cheeseForm.producerId || !cheeseForm.shopId) {
          throw new Error("Выберите производителя и магазин для обновления сыра.");
        }
        const oldCheeseId = Number(cheeseForm.id);
        let replacementCheeseId = null;
        try {
          const replacement = await apiRequest(`/api/cheeses/${cheeseForm.shopId}/${cheeseForm.producerId}`, {
            method: "POST",
            body: JSON.stringify(payload)
          });
          if (!replacement?.id) {
            throw new Error("Не удалось создать обновленную версию сыра.");
          }
          replacementCheeseId = replacement.id;
          saveImageOverride(replacementCheeseId, cheeseImageUrl);

          const categoriesToAttach = getCurrentCategoryDrafts();
          for (const categoryPayload of categoriesToAttach) {
            await apiRequest(`/api/categories/${replacementCheeseId}`, {
              method: "POST",
              body: JSON.stringify(categoryPayload)
            });
          }

          const oldReviews = (await apiRequest(`/api/reviews/cheese/${oldCheeseId}`)) ?? [];
          for (const review of oldReviews) {
            await apiRequest(`/api/reviews/cheese/${replacementCheeseId}`, {
              method: "POST",
              body: JSON.stringify({
                author: (review.author ?? "").trim(),
                rating: Number(review.rating),
                comment: (review.comment ?? "").trim()
              })
            });
          }

          await apiRequest(`/api/cheeses/${oldCheeseId}`, { method: "DELETE" });
          showAlert("Сыр обновлен");
        } catch (updateError) {
          if (replacementCheeseId) {
            try {
              await apiRequest(`/api/cheeses/${replacementCheeseId}`, { method: "DELETE" });
            } catch {
              // ignore cleanup error; original error is more important
            }
          }
          throw updateError;
        }
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
          const uniqueCategoriesToAttach = getCurrentCategoryDrafts();

          for (const categoryPayload of uniqueCategoriesToAttach) {
            await apiRequest(`/api/categories/${created.id}`, {
              method: "POST",
              body: JSON.stringify(categoryPayload)
            });
          }

          if (uniqueCategoriesToAttach.length > 0) {
            showAlert("Сыр и категории добавлены");
          } else {
            showAlert("Сыр добавлен");
          }
        } else {
          showAlert("Сыр добавлен");
        }
      }

      setCheeseForm(emptyCheeseForm);
      setCheeseImageUrl("");
      setNewCategoryForCheese({ name: "", description: "" });
      setSelectedCategoryForCheeseIds([]);
      setShowQuickProducer(false);
      setShowQuickShop(false);
      setShowNewCategoryForm(false);
      setAdminModal(null);
      await loadAdminData();
    } catch (err) {
      setError(err.message);
    }
  };

  const createProducerForCheese = async () => {
    setError("");
    try {
      const payload = {
        name: producerForm.name.trim(),
        country: producerForm.country.trim(),
        description: producerForm.description.trim()
      };
      if (!payload.name || !payload.country) {
        throw new Error("Заполните название и страну производителя.");
      }

      const created = await apiRequest("/api/producers", {
        method: "POST",
        body: JSON.stringify(payload)
      });

      if (created?.id) {
        setProducers((prev) =>
          [...prev, created].sort((a, b) => (a.name ?? "").localeCompare(b.name ?? "", "ru", { sensitivity: "base" }))
        );
        setCheeseForm((prev) => ({ ...prev, producerId: String(created.id) }));
      } else {
        await loadAdminData();
      }

      setProducerForm(emptyProducerForm);
      setShowQuickProducer(false);
      showAlert("Производитель создан и выбран");
    } catch (err) {
      setError(err.message);
    }
  };

  const createShopForCheese = async () => {
    setError("");
    try {
      const payload = {
        name: shopForm.name.trim(),
        address: shopForm.address.trim(),
        phone: shopForm.phone.trim()
      };
      if (!payload.name || !payload.address || !payload.phone) {
        throw new Error("Заполните название, адрес и телефон магазина.");
      }

      const created = await apiRequest("/api/shops", {
        method: "POST",
        body: JSON.stringify(payload)
      });

      if (created?.id) {
        setShops((prev) => [...prev, created]);
        setCheeseForm((prev) => ({ ...prev, shopId: String(created.id) }));
      } else {
        await loadAdminData();
      }

      setShopForm(emptyShopForm);
      setShowQuickShop(false);
      showAlert("Магазин создан и выбран");
    } catch (err) {
      setError(err.message);
    }
  };

  const addCategoryToExistingList = async () => {
    const hostCheeseId = String(cheeses[0]?.id ?? "");
    if (!hostCheeseId) {
      showAlert("Сначала должен существовать хотя бы один сыр для привязки новой категории.");
      return;
    }
    if (!newCategoryForCheese.name.trim()) {
      showAlert("Введите название новой категории.");
      return;
    }
    try {
      const created = await apiRequest(`/api/categories/${hostCheeseId}`, {
        method: "POST",
        body: JSON.stringify({
          name: newCategoryForCheese.name.trim(),
          description: newCategoryForCheese.description.trim()
        })
      });

      if (created?.id) {
        setCategories((prev) => [...prev, created]);
        setSelectedCategoryForCheeseIds((prev) => {
          const createdId = String(created.id);
          return prev.includes(createdId) ? prev : [...prev, createdId];
        });
      } else {
        await loadAdminData();
      }

      setNewCategoryForCheese({ name: "", description: "" });
      setShowNewCategoryForm(false);
      showAlert("Категория добавлена и доступна в списке чекбоксов");
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
      setAdminModal(null);
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
      setAdminModal(null);
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
      setAdminModal(null);
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

  const startИзменитьCheese = async (cheese) => {
    const currentOverride = imageOverrides[String(cheese.id)] ?? "";
    let sourceCheese = cheese;

    try {
      const detailed = await apiRequest(`/api/cheeses/${cheese.id}`);
      if (detailed?.id) {
        sourceCheese = detailed;
      }
    } catch {
      // fallback to data from list if detailed fetch fails
    }

    setCheeseForm({
      id: String(sourceCheese.id ?? cheese.id),
      name: sourceCheese.name ?? "",
      fats: sourceCheese.fats ?? "",
      description: sourceCheese.description ?? "",
      price: sourceCheese.price ?? "",
      producerId: String(sourceCheese.producer?.id ?? ""),
      shopId: String(sourceCheese.shop?.id ?? "")
    });
    setCheeseImageUrl(currentOverride);
    setSelectedCategoryForCheeseIds((sourceCheese.categories ?? []).map((category) => String(category.id)));
    setNewCategoryForCheese({ name: "", description: "" });
    setShowQuickProducer(false);
    setShowQuickShop(false);
    setShowNewCategoryForm(false);
    setAdminModal("cheese");
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
        <h2>Панель администратора</h2>
        <p className="state">Создание производителей, магазинов и управление сырами.</p>
        {loading ? <p className="state">Загрузка...</p> : null}
        {error ? <p className="state error">{error}</p> : null}

        <div className="admin-tools">
          <button
            type="button"
            onClick={() => {
              setCheeseForm(emptyCheeseForm);
              setCheeseImageUrl("");
              setNewCategoryForCheese({ name: "", description: "" });
              setSelectedCategoryForCheeseIds([]);
              setShowQuickProducer(false);
              setShowQuickShop(false);
              setShowNewCategoryForm(false);
              setAdminModal("cheese");
            }}
          >
            Добавить сыр
          </button>
          <button
            type="button"
            className="ghost"
            onClick={() => {
              setProducerForm(emptyProducerForm);
              setAdminModal("producer");
            }}
          >
            Новый производитель
          </button>
          <button
            type="button"
            className="ghost"
            onClick={() => {
              setShopForm(emptyShopForm);
              setAdminModal("shop");
            }}
          >
            Новый магазин
          </button>
          <button
            type="button"
            className="ghost"
            onClick={() => {
              setCategoryForm(emptyCategoryForm);
              setAdminModal("category");
            }}
          >
            Новая категория
          </button>
        </div>
      </div>

      <div className="panel">
        <h2>Управление сырами</h2>
        <div className="admin-list">
          {pagedCheeses.map((cheese) => (
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
        <div className="shops-pagination-header">
          <p className="state">
            Страница {cheesesPage + 1} из {Math.max(cheesesTotalPages, 1)}
          </p>
          <div className="shops-pagination-actions">
            <button
              type="button"
              className="ghost"
              disabled={cheesesPageLoading || cheesesPage <= 0}
              onClick={() => loadCheesesPage(Math.max(cheesesPage - 1, 0))}
            >
              Назад
            </button>
            <button
              type="button"
              className="ghost"
              disabled={cheesesPageLoading || cheesesPage + 1 >= cheesesTotalPages}
              onClick={() => loadCheesesPage(cheesesPage + 1)}
            >
              Вперед
            </button>
          </div>
        </div>
      </div>

      <div className="panel">
        <h2>Список доступных магазинов</h2>
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
      </div>

      {adminModal ? (
        <div className="modal-overlay" role="dialog" aria-modal="true">
          <div className="modal-window">
            <div className="modal-header">
              <h3>
                {adminModal === "cheese"
                  ? isИзменитьingCheese
                    ? "Изменить сыр"
                    : "Добавить сыр"
                  : adminModal === "producer"
                    ? "Новый производитель"
                    : adminModal === "shop"
                      ? "Новый магазин"
                      : "Новая категория"}
              </h3>
              <button type="button" className="ghost small" onClick={() => setAdminModal(null)}>
                Закрыть
              </button>
            </div>

            {adminModal === "producer" ? (
              <form className="admin-form" onSubmit={createProducer}>
                <label>
                  Название
                  <input
                    value={producerForm.name}
                    onChange={(event) => setProducerForm({ ...producerForm, name: event.target.value })}
                    required
                  />
                </label>
                <label>
                  Страна
                  <input
                    value={producerForm.country}
                    onChange={(event) => setProducerForm({ ...producerForm, country: event.target.value })}
                    required
                  />
                </label>
                <label className="full">
                  Описание
                  <textarea
                    rows={3}
                    value={producerForm.description}
                    onChange={(event) => setProducerForm({ ...producerForm, description: event.target.value })}
                  />
                </label>
                <div className="admin-actions">
                  <button type="submit">Сохранить</button>
                </div>
              </form>
            ) : null}

            {adminModal === "shop" ? (
              <form className="admin-form" onSubmit={createShop}>
                <label>
                  Название магазина
                  <input value={shopForm.name} onChange={(event) => setShopForm({ ...shopForm, name: event.target.value })} required />
                </label>
                <label>
                  Адрес
                  <input
                    value={shopForm.address}
                    onChange={(event) => setShopForm({ ...shopForm, address: event.target.value })}
                    required
                  />
                </label>
                <label className="full">
                  Телефон
                  <input value={shopForm.phone} onChange={(event) => setShopForm({ ...shopForm, phone: event.target.value })} required />
                </label>
                <div className="admin-actions">
                  <button type="submit">Сохранить</button>
                </div>
              </form>
            ) : null}

            {adminModal === "category" ? (
              <form className="admin-form" onSubmit={createCategory}>
                <label>
                  Сыр
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
                </label>
                <label>
                  Шаблон категории
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
                </label>
                <label className="full">
                  Название категории
                  <input
                    value={categoryForm.name}
                    onChange={(event) => setCategoryForm({ ...categoryForm, name: event.target.value })}
                    required
                  />
                </label>
                <label className="full">
                  Описание категории
                  <textarea
                    rows={3}
                    value={categoryForm.description}
                    onChange={(event) => setCategoryForm({ ...categoryForm, description: event.target.value })}
                  />
                </label>
                <div className="admin-actions">
                  <button type="submit">Сохранить</button>
                </div>
              </form>
            ) : null}

            {adminModal === "cheese" ? (
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
                <label className="full">
                  Производитель
                  <select
                    value={cheeseForm.producerId}
                    onChange={(event) => setCheeseForm({ ...cheeseForm, producerId: event.target.value })}
                  >
                    <option value="">Выбрать</option>
                    {producers.map((producer) => (
                      <option key={producer.id} value={producer.id}>
                        {producer.id} - {producer.name}
                      </option>
                    ))}
                  </select>
                </label>
                {!isИзменитьingCheese ? (
                  <div className="full quick-create-wrap">
                    <button
                      type="button"
                      className="ghost small quick-toggle"
                      onClick={() => setShowQuickProducer((prev) => !prev)}
                    >
                      {showQuickProducer ? "Скрыть форму производителя" : "Создать производителя прямо здесь"}
                    </button>
                    {showQuickProducer ? (
                      <div className="quick-create-grid">
                        <input
                          placeholder="Название производителя"
                          value={producerForm.name}
                          onChange={(event) => setProducerForm({ ...producerForm, name: event.target.value })}
                        />
                        <input
                          placeholder="Страна"
                          value={producerForm.country}
                          onChange={(event) => setProducerForm({ ...producerForm, country: event.target.value })}
                        />
                        <textarea
                          rows={2}
                          placeholder="Описание"
                          value={producerForm.description}
                          onChange={(event) => setProducerForm({ ...producerForm, description: event.target.value })}
                        />
                        <button type="button" className="small" onClick={createProducerForCheese}>
                          Создать и выбрать
                        </button>
                      </div>
                    ) : null}
                  </div>
                ) : null}
                <label className="full">
                  Магазин
                  <select
                    value={cheeseForm.shopId}
                    onChange={(event) => setCheeseForm({ ...cheeseForm, shopId: event.target.value })}
                  >
                    <option value="">Выбрать</option>
                    {shops.map((shop) => (
                      <option key={shop.id} value={shop.id}>
                        {shop.id} - {shop.name}
                      </option>
                    ))}
                  </select>
                </label>
                {!isИзменитьingCheese ? (
                  <div className="full quick-create-wrap">
                    <button
                      type="button"
                      className="ghost small quick-toggle"
                      onClick={() => setShowQuickShop((prev) => !prev)}
                    >
                      {showQuickShop ? "Скрыть форму магазина" : "Создать магазин прямо здесь"}
                    </button>
                    {showQuickShop ? (
                      <div className="quick-create-grid">
                        <input
                          placeholder="Название магазина"
                          value={shopForm.name}
                          onChange={(event) => setShopForm({ ...shopForm, name: event.target.value })}
                        />
                        <input
                          placeholder="Адрес"
                          value={shopForm.address}
                          onChange={(event) => setShopForm({ ...shopForm, address: event.target.value })}
                        />
                        <input
                          placeholder="Телефон"
                          value={shopForm.phone}
                          onChange={(event) => setShopForm({ ...shopForm, phone: event.target.value })}
                        />
                        <button type="button" className="small" onClick={createShopForCheese}>
                          Создать и выбрать
                        </button>
                      </div>
                    ) : null}
                  </div>
                ) : null}
                <label className="full">
                  <span className="category-section-title">
                    Категории сыра
                    <span className="category-count">Выбрано: {selectedCategoryForCheeseIds.length}</span>
                  </span>
                  <div className="category-checkbox-list">
                    {uniqueCategories.map((category) => (
                      <label
                        key={category.id}
                        className={
                          selectedCategoryForCheeseIds.includes(String(category.id))
                            ? "category-checkbox-item selected"
                            : "category-checkbox-item"
                        }
                      >
                        <input
                          type="checkbox"
                          checked={selectedCategoryForCheeseIds.includes(String(category.id))}
                          onChange={(event) => {
                            const categoryId = String(category.id);
                            setSelectedCategoryForCheeseIds((prev) =>
                              event.target.checked ? [...prev, categoryId] : prev.filter((id) => id !== categoryId)
                            );
                          }}
                        />
                        <span>{category.name}</span>
                      </label>
                    ))}
                  </div>
                </label>
                {!isИзменитьingCheese ? (
                  <>
                    <div className="full quick-create-wrap">
                      <button
                        type="button"
                        className="ghost small quick-toggle"
                        onClick={() => setShowNewCategoryForm((prev) => !prev)}
                      >
                        {showNewCategoryForm ? "Скрыть форму новой категории" : "Создать новую категорию"}
                      </button>
                      {showNewCategoryForm ? (
                        <div className="quick-create-grid">
                          <label>
                            Новая категория для этого сыра
                            <input
                              placeholder="Например: Твердый"
                              value={newCategoryForCheese.name}
                              onChange={(event) =>
                                setNewCategoryForCheese((prev) => ({
                                  ...prev,
                                  name: event.target.value
                                }))
                              }
                            />
                          </label>
                          <label>
                            Описание категории (опционально)
                            <textarea
                              rows={2}
                              value={newCategoryForCheese.description}
                              onChange={(event) =>
                                setNewCategoryForCheese((prev) => ({
                                  ...prev,
                                  description: event.target.value
                                }))
                              }
                            />
                          </label>
                          <button type="button" className="small" onClick={addCategoryToExistingList}>
                            Добавить категорию
                          </button>
                        </div>
                      ) : null}
                    </div>
                  </>
                ) : null}
                <div className="admin-actions">
                  <button type="submit">{isИзменитьingCheese ? "Сохранить изменения" : "Добавить сыр"}</button>
                </div>
              </form>
            ) : null}
          </div>
        </div>
      ) : null}
    </section>
  );
}

export default App;
